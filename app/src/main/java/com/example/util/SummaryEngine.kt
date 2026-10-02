package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.data.model.DocumentFile
import com.example.data.model.DocumentWithDetails
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.math.sqrt

/**
 * Fully on-device "short notes" generator.
 *  1. Reads the text of PDFs / photos with ML Kit OCR (works for scanned notes too).
 *  2. Builds an extractive summary: key points, key terms, formulas, keywords.
 * No internet and no API key needed. English / Latin-script text only.
 */
object SummaryEngine {

    private const val MAX_PAGES_PER_PDF = 15

    data class DocNotes(val title: String, val points: List<String>)

    data class ModuleSummary(
        val keywords: List<String>,
        val keyPoints: List<String>,
        val terms: List<String>,
        val formulas: List<String>,
        val perDocument: List<DocNotes>,
        val docsRead: Int,
        val docsWithoutText: Int,
        val pagesRead: Int
    )

    private data class DocText(val title: String, val text: String, val pages: Int)

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    // ---------------------------------------------------------------- public API

    suspend fun summarize(
        context: Context,
        documents: List<DocumentWithDetails>,
        onProgress: (String) -> Unit
    ): ModuleSummary = withContext(Dispatchers.Default) {
        val texts = mutableListOf<DocText>()
        documents.forEachIndexed { index, item ->
            onProgress("Reading ${index + 1} of ${documents.size}: ${item.document.title}")
            texts += extractText(context, item.document) { page, total ->
                onProgress("Reading ${index + 1} of ${documents.size} · page $page of $total")
            }
        }
        onProgress("Writing short notes…")

        val usable = texts.filter { it.text.length >= 80 }
        val combined = usable.joinToString("\n") { it.text }

        val allSentences = splitSentences(combined)
        val keyCount = when {
            allSentences.size > 120 -> 10
            allSentences.size > 50 -> 8
            else -> 6
        }

        ModuleSummary(
            keywords = keywords(allSentences, 12),
            keyPoints = topSentences(allSentences, keyCount),
            terms = definitions(allSentences, 6),
            formulas = formulas(combined, 6),
            perDocument = usable.map { d ->
                DocNotes(d.title, topSentences(splitSentences(d.text), 4))
            }.filter { it.points.isNotEmpty() },
            docsRead = usable.size,
            docsWithoutText = texts.size - usable.size,
            pagesRead = texts.sumOf { it.pages }
        )
    }

    fun toPlainText(moduleName: String, s: ModuleSummary): String = buildString {
        appendLine("Short notes — $moduleName")
        appendLine()
        if (s.keywords.isNotEmpty()) {
            appendLine("KEYWORDS")
            appendLine(s.keywords.joinToString(", "))
            appendLine()
        }
        if (s.keyPoints.isNotEmpty()) {
            appendLine("KEY POINTS")
            s.keyPoints.forEachIndexed { i, p -> appendLine("${i + 1}. $p") }
            appendLine()
        }
        if (s.terms.isNotEmpty()) {
            appendLine("KEY TERMS")
            s.terms.forEach { appendLine("• $it") }
            appendLine()
        }
        if (s.formulas.isNotEmpty()) {
            appendLine("FORMULAS")
            s.formulas.forEach { appendLine("• $it") }
            appendLine()
        }
        s.perDocument.forEach { d ->
            appendLine("FROM: ${d.title}")
            d.points.forEach { appendLine("• $it") }
            appendLine()
        }
    }.trim()

    // ---------------------------------------------------------------- text extraction

    private suspend fun extractText(
        context: Context,
        doc: DocumentFile,
        onPage: (Int, Int) -> Unit
    ): DocText = withContext(Dispatchers.IO) {
        val file = FileManager.getInternalFile(context, doc.internalFilePath)
        if (!file.exists()) return@withContext DocText(doc.title, "", 0)

        val cacheDir = File(context.cacheDir, "summary_text").apply { mkdirs() }
        val cacheFile = File(cacheDir, "${doc.id}_${file.lastModified()}.txt")
        if (cacheFile.exists()) {
            val pages = if (isPdf(doc)) minOf(PdfHelper.getPageCount(file), MAX_PAGES_PER_PDF) else 1
            return@withContext DocText(doc.title, cacheFile.readText(), pages)
        }

        val sb = StringBuilder()
        var pagesRead = 0
        if (isPdf(doc)) {
            val total = minOf(PdfHelper.getPageCount(file), MAX_PAGES_PER_PDF)
            for (i in 0 until total) {
                onPage(i + 1, total)
                val bmp = PdfHelper.renderPageToBitmap(file, i, maxDimension = 1800) ?: continue
                sb.appendLine(recognize(bmp))
                bmp.recycle()
                pagesRead++
            }
        } else {
            onPage(1, 1)
            decodeSampled(file, 2000)?.let { bmp ->
                sb.appendLine(recognize(bmp))
                bmp.recycle()
                pagesRead = 1
            }
        }
        val text = sb.toString()
        if (text.isNotBlank()) runCatching { cacheFile.writeText(text) }
        DocText(doc.title, text, pagesRead)
    }

    private fun isPdf(doc: DocumentFile): Boolean =
        doc.fileType.equals("PDF", true) || doc.mimeType.contains("pdf", true)

    private fun decodeSampled(file: File, maxDim: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxDim) sample *= 2
        return BitmapFactory.decodeFile(
            file.absolutePath,
            BitmapFactory.Options().apply { inSampleSize = sample }
        )
    }

    private suspend fun recognize(bitmap: Bitmap): String =
        suspendCancellableCoroutine { cont ->
            recognizer.process(InputImage.fromBitmap(bitmap, 0))
                .addOnSuccessListener { if (cont.isActive) cont.resume(it.text) }
                .addOnFailureListener { if (cont.isActive) cont.resume("") }
        }

    // ---------------------------------------------------------------- summarising

    private val STOP = setOf(
        "the", "and", "for", "are", "but", "not", "you", "all", "any", "can", "had", "her", "was", "one",
        "our", "out", "has", "have", "him", "his", "how", "its", "may", "new", "now", "old", "see", "two",
        "who", "did", "get", "let", "put", "say", "she", "too", "use", "that", "with", "this", "from", "they",
        "will", "would", "there", "their", "what", "when", "where", "which", "while", "about", "into", "than",
        "then", "them", "these", "those", "been", "were", "also", "such", "some", "more", "most", "other",
        "only", "over", "very", "just", "each", "both", "does", "done", "being", "because", "should", "could",
        "using", "used", "uses", "between", "through", "after", "before", "under", "again", "further", "here",
        "your", "yours", "ours", "shall", "must", "many", "much", "same", "like", "make", "made", "page"
    )

    private val DEFINITION_CUE = Regex(
        "\\b(is defined as|are defined as|is called|are called|is known as|are known as|refers to|means|is a|is an|is the|are the)\\b",
        RegexOption.IGNORE_CASE
    )

    private fun tokens(s: String): List<String> =
        s.lowercase().split(Regex("[^a-z]+")).filter { it.length >= 3 && it !in STOP }

    private fun splitSentences(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        // keep bullets / numbered items as their own sentences, join wrapped lines otherwise
        val marked = text.replace("\r", "\n")
            .replace(Regex("\\n\\s*(?=[•●▪◦*\\-–]\\s|\\d{1,2}[.)]\\s)"), "\n§")
            .replace(Regex("(?<![.!?:;])\\n(?!§)"), " ")
            .replace("\n§", "\n")
        val seen = HashSet<String>()
        return marked
            .split(Regex("(?<=[.!?])\\s+|\\n"))
            .map { it.replace(Regex("^[•●▪◦*\\-–§\\d.)\\s]+"), "").replace(Regex("\\s+"), " ").trim() }
            .filter { s ->
                val words = s.split(' ').size
                val letters = s.count { it.isLetter() }
                s.length in 30..260 && words >= 5 && letters >= s.length * 0.6
            }
            .filter { seen.add(it.lowercase().take(60)) }
    }

    private fun termFreq(sentences: List<String>): Map<String, Double> {
        val counts = HashMap<String, Int>()
        sentences.forEach { s -> tokens(s).forEach { counts[it] = (counts[it] ?: 0) + 1 } }
        val max = (counts.values.maxOrNull() ?: 1).toDouble()
        return counts.mapValues { it.value / max }
    }

    private fun topSentences(sentences: List<String>, n: Int): List<String> {
        if (sentences.isEmpty()) return emptyList()
        val tf = termFreq(sentences)
        val scored = sentences.mapIndexed { i, s ->
            val toks = tokens(s).distinct()
            var score = toks.sumOf { tf[it] ?: 0.0 } / (sqrt(toks.size.toDouble()) + 1.0)
            if (i < 2) score *= 1.15
            if (DEFINITION_CUE.containsMatchIn(s)) score *= 1.2
            Triple(i, s, score)
        }.sortedByDescending { it.third }

        val chosen = mutableListOf<Triple<Int, String, Double>>()
        for (c in scored) {
            if (chosen.size >= n) break
            val ct = tokens(c.second).toSet()
            val duplicate = chosen.any { o ->
                val ot = tokens(o.second).toSet()
                val inter = ct.intersect(ot).size.toDouble()
                val union = ct.union(ot).size.toDouble().coerceAtLeast(1.0)
                inter / union > 0.6
            }
            if (!duplicate) chosen += c
        }
        return chosen.sortedBy { it.first }.map { tidy(it.second) }
    }

    private fun definitions(sentences: List<String>, n: Int): List<String> {
        val tf = termFreq(sentences)
        return sentences
            .filter { s ->
                val m = DEFINITION_CUE.find(s)
                m != null && s.length <= 220 && s.substring(0, m.range.first).trim().split(' ').size in 1..6
            }
            .sortedByDescending { s -> tokens(s).distinct().sumOf { tf[it] ?: 0.0 } }
            .take(n)
            .map { tidy(it) }
    }

    private fun formulas(text: String, n: Int): List<String> {
        val seen = HashSet<String>()
        return text.lines()
            .map { it.trim() }
            .filter { l ->
                l.length in 5..80 &&
                    (l.contains('=') || l.contains('∑') || l.contains('∫') || l.contains('√')) &&
                    l.any { it.isLetter() } && l.any { it.isDigit() || it in "=+-*/^()" } &&
                    l.count { it.isLetter() } < l.length * 0.8
            }
            .filter { seen.add(it.replace(" ", "").lowercase()) }
            .take(n)
    }

    private fun keywords(sentences: List<String>, n: Int): List<String> {
        val counts = HashMap<String, Int>()
        sentences.forEach { s -> tokens(s).filter { it.length >= 4 }.forEach { counts[it] = (counts[it] ?: 0) + 1 } }
        val minCount = if (sentences.size > 15) 2 else 1
        return counts.entries
            .filter { it.value >= minCount }
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .take(n)
            .map { it.key.replaceFirstChar { c -> c.uppercase() } }
    }

    private fun tidy(s: String, max: Int = 200): String {
        val t = s.trim()
        if (t.length <= max) return t
        val cut = t.lastIndexOf(' ', max)
        return t.substring(0, if (cut > 60) cut else max).trimEnd(',', ';', ':') + "…"
    }
}
