package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.launch

private data class GuidePage(
    val icon: ImageVector,
    val tone: Color,
    val title: String,
    val body: String
)

private val guidePages = listOf(
    GuidePage(
        Icons.Filled.Folder, Violet,
        "Welcome to Visor",
        "All your study PDFs and note photos in one calm place, sorted by subject and module."
    ),
    GuidePage(
        Icons.Filled.CreateNewFolder, Teal,
        "Start with a subject",
        "Open the Folders tab and tap New subject (for example Physics). Then add a module inside it for each chapter or unit."
    ),
    GuidePage(
        Icons.Filled.NoteAdd, Coral,
        "Add your files",
        "Tap Add at the top to bring in PDFs or photos of notes, and choose which module they belong to."
    ),
    GuidePage(
        Icons.Filled.AutoAwesome, Amber,
        "Summarize in one tap",
        "Open any module that has files and tap Summarize. Visor reads them and makes colourful short notes: key points, key terms and formulas."
    ),
    GuidePage(
        Icons.Filled.ZoomIn, SkyBlue,
        "Read, zoom and star",
        "Pinch or double-tap to zoom into pages. Star important documents and find them later in the Starred tab."
    )
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState { guidePages.size }
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == guidePages.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isLast) {
                Text(
                    "Skip",
                    color = MutedFg,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onFinish)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { index ->
            val page = guidePages[index]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(132.dp)
                        .clip(CircleShape)
                        .background(page.tone.copy(alpha = 0.14f))
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(page.tone)
                    ) {
                        Icon(page.icon, contentDescription = null, tint = OnInk, modifier = Modifier.size(44.dp))
                    }
                }
                Spacer(Modifier.height(36.dp))
                Text(
                    page.title,
                    fontFamily = DisplayFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                    color = Ink,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    page.body,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = MutedFg,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Page dots
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            guidePages.forEachIndexed { i, p ->
                val active = i == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(8.dp)
                        .size(width = if (active) 24.dp else 8.dp, height = 8.dp)
                        .clip(CircleShape)
                        .background(if (active) p.tone else MutedFg.copy(alpha = 0.25f))
                )
            }
        }

        Button(
            onClick = {
                if (isLast) onFinish()
                else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = OnInk),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(52.dp)
        ) {
            Text(if (isLast) "Get started" else "Next", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
        Spacer(Modifier.height(20.dp))
    }
}
