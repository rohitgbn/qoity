package com.example.quoteverse.feature.moderation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityGuidelinesScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Community Guidelines", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Welcome to QuoteVerse",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "QuoteVerse is a safe, creative, and respectful social space where writers, poets, and thinkers share authentic quotes, shayari, and thoughts in Hindi, Hinglish, and English.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            GuidelineRuleItem(
                number = "1. Original Human Creativity",
                description = "QuoteVerse is dedicated to human expression. Users create their own original posts and quote designs. Plagiarism or impersonation of other authors is strictly prohibited."
            )

            GuidelineRuleItem(
                number = "2. Zero Tolerance for Hate Speech",
                description = "We prohibit hate speech, harassment, bullying, slurs, attacks based on religion, caste, gender, sexual orientation, disability, or nationality."
            )

            GuidelineRuleItem(
                number = "3. Appropriate Imagery & Content",
                description = "All uploaded photo backgrounds and designs must be safe for general audiences. Explicit sexual content, gore, or violence will result in immediate content removal and account suspension."
            )

            GuidelineRuleItem(
                number = "4. Respectful Comments & Interactions",
                description = "Feedback should be constructive and encouraging. Spam comments, scams, and commercial advertising are removed by our moderation system."
            )

            GuidelineRuleItem(
                number = "5. Reporting and Safety Tools",
                description = "Every post, comment, and profile has a direct Report button. If you encounter any abusive or violating content, report it immediately. You can also block any user to immediately hide their content from your feeds."
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Privacy & Data Protection",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your private saved bookmarks and blocklists are strictly isolated and never shared with other users or third parties. When you request account deletion, all your quotes, comments, and profile data are permanently erased.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun GuidelineRuleItem(number: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = number,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
