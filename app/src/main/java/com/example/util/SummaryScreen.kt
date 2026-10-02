package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DocumentWithDetails
import com.example.ui.theme.Amber
import com.example.ui.theme.Coral
import com.example.ui.theme.DisplayFont
import com.example.ui.theme.Ink
import com.example.ui.theme.MutedFg
import com.example.ui.theme.OnInk
import com.example.ui.theme.Paper
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.Teal
import com.example.ui.theme.Violet
import com.example.util.SummaryEngine

/**
 * Full-screen "Short notes" for one module: colourful cards a student can read quickly.
 */
@Composable
fun SummaryDialog(
    moduleName: String,
    subjectName: String,
    documents: List<DocumentWithDetails>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var progress by remember { mutableStateOf("Getting ready…") }
    var summary by remember { mutableStateOf<SummaryEngine.ModuleSummary?>(null) }

    LaunchedEffect(documents.map { it.document.id }) {
        summary = null
        summary = SummaryEngine.summarize(context, documents) { progress = it }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(color = Paper, modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Ink)
                    }
                    Text(
                        text = "Short notes",
                        fontFamily = DisplayFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Ink,
                        modifier = Modifier.weight(1f)
                    )
                    val s = summary
                    if (s != null && (s.keyPoints.isNotEmpty() || s.perDocument.isNotEmpty())) {
                        IconButton(onClick = {
                            clipboard.setText(AnnotatedString(SummaryEngine.toPlainText(moduleName, s)))
                            Toast.makeText(context, "Notes copied", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy notes", tint = Ink)
                        }
                    }
                }

                val s = summary
                if (s == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Violet)
                        Spacer(Modifier.height(20.dp))
                        Text(
                            "Making your short notes",
                            fontFamily = DisplayFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            color = Ink
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(progress, fontSize = 13.sp, color = MutedFg, textAlign = TextAlign.Center)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item { Banner(moduleName, subjectName, s) }

                        if (s.keyPoints.isEmpty() && s.perDocument.isEmpty()) {
                            item {
                                NoteCard(
                                    title = "No readable text found",
                                    icon = Icons.Filled.Description,
                                    tone = Coral
                                ) {
                                    Text(
                                        "Visor could not read any text in this module's files. " +
                                            "Try clearer PDFs or photos with printed or neat handwritten English text.",
                                        fontSize = 14.sp, lineHeight = 20.sp, color = Ink
                                    )
                                }
                            }
                        } else {
                            if (s.keywords.isNotEmpty()) item { KeywordCard(s.keywords) }
                            if (s.keyPoints.isNotEmpty()) item {
                                NoteCard("Key points", Icons.Filled.Lightbulb, Violet) {
                                    NumberedList(s.keyPoints, Violet)
                                }
                            }
                            if (s.terms.isNotEmpty()) item {
                                NoteCard("Key terms", Icons.Filled.MenuBook, Teal) {
                                    BulletList(s.terms, Teal)
                                }
                            }
                            if (s.formulas.isNotEmpty()) item {
                                NoteCard("Formulas", Icons.Filled.Functions, Amber) {
                                    BulletList(s.formulas, Amber, mono = true)
                                }
                            }
                            val tones = listOf(SkyBlue, Coral, Teal, Violet, Amber)
                            s.perDocument.forEachIndexed { i, d ->
                                item {
                                    NoteCard("From: ${d.title}", Icons.Filled.Description, tones[i % tones.size]) {
                                        BulletList(d.points, tones[i % tones.size])
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                "Notes are auto-generated from the text found in your files. " +
                                    "Always double-check important details with the original.",
                                fontSize = 11.sp,
                                color = MutedFg,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Banner(moduleName: String, subjectName: String, s: SummaryEngine.ModuleSummary) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(Violet, SkyBlue)))
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = OnInk, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(subjectName, color = OnInk.copy(alpha = 0.9f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                moduleName,
                color = OnInk,
                fontFamily = DisplayFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                lineHeight = 30.sp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "${s.docsRead} ${if (s.docsRead == 1) "document" else "documents"} · ${s.pagesRead} " +
                    "${if (s.pagesRead == 1) "page" else "pages"} read" +
                    if (s.docsWithoutText > 0) " · ${s.docsWithoutText} without readable text" else "",
                color = OnInk.copy(alpha = 0.9f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun NoteCard(
    title: String,
    icon: ImageVector,
    tone: Color,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = tone.copy(alpha = 0.09f),
        border = BorderStroke(1.dp, tone.copy(alpha = 0.28f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(tone)
                ) {
                    Icon(icon, contentDescription = null, tint = OnInk, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    title,
                    fontFamily = DisplayFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = Ink
                )
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KeywordCard(words: List<String>) {
    val tones = listOf(Coral, Violet, Teal, Amber, SkyBlue)
    NoteCard("Keywords", Icons.Filled.Tag, Coral) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            words.forEachIndexed { i, w ->
                val t = tones[i % tones.size]
                Surface(shape = RoundedCornerShape(50), color = t.copy(alpha = 0.16f)) {
                    Text(
                        w,
                        color = t,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NumberedList(items: List<String>, tone: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEachIndexed { i, text ->
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(tone)
                ) {
                    Text("${i + 1}", color = OnInk, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(10.dp))
                Text(text, fontSize = 14.sp, lineHeight = 20.sp, color = Ink, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun BulletList(items: List<String>, tone: Color, mono: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { text ->
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .padding(top = 7.dp)
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(tone)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Ink,
                    fontFamily = if (mono) FontFamily.Monospace else FontFamily.Default,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
