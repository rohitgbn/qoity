package com.example.quoteverse.feature.createpost

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FormatAlignCenter
import androidx.compose.material.icons.outlined.FormatAlignLeft
import androidx.compose.material.icons.outlined.FormatAlignRight
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.quoteverse.core.model.QuoteDesignConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    viewModel: CreatePostViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        viewModel.setSelectedImageUri(uri)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CreatePostEvent.PostPublishedSuccess -> onNavigateBack()
                is CreatePostEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Create Post", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.publishPost(context) },
                        enabled = !uiState.isPublishing,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (uiState.isPublishing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Publish", fontWeight = FontWeight.Bold)
                        }
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
                .padding(bottom = 32.dp)
        ) {
            // Creation Mode Tabs
            PrimaryTabRow(
                selectedTabIndex = uiState.mode.ordinal,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                Tab(
                    selected = uiState.mode == CreationMode.QUOTE_DESIGNER,
                    onClick = { viewModel.setMode(CreationMode.QUOTE_DESIGNER) },
                    text = { Text("Quote Studio") },
                    icon = { Icon(Icons.Outlined.Palette, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = uiState.mode == CreationMode.TEXT_ONLY,
                    onClick = { viewModel.setMode(CreationMode.TEXT_ONLY) },
                    text = { Text("Text Only") },
                    icon = { Icon(Icons.Outlined.TextFields, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = uiState.mode == CreationMode.IMAGE_UPLOAD,
                    onClick = { viewModel.setMode(CreationMode.IMAGE_UPLOAD) },
                    text = { Text("Upload Photo") },
                    icon = { Icon(Icons.Outlined.Image, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error display
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // Mode Specific Content
            when (uiState.mode) {
                CreationMode.QUOTE_DESIGNER -> {
                    // LIVE DESIGN PREVIEW CARD
                    val preset = QuoteDesignConfig.BACKGROUND_PRESETS.getOrElse(uiState.designConfig.backgroundIndex) {
                        QuoteDesignConfig.BACKGROUND_PRESETS.first()
                    }
                    val fontPreset = QuoteDesignConfig.FONT_PRESETS.getOrElse(uiState.designConfig.fontStyleIndex) {
                        QuoteDesignConfig.FONT_PRESETS.first()
                    }

                    val bgBrush = if (preset.isGradient) {
                        Brush.linearGradient(listOf(Color(preset.primaryColor), Color(preset.secondaryColor)))
                    } else {
                        Brush.linearGradient(listOf(Color(preset.primaryColor), Color(preset.primaryColor)))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(bgBrush)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = when (uiState.designConfig.textAlignment) {
                                "start" -> Alignment.Start
                                "end" -> Alignment.End
                                else -> Alignment.CenterHorizontally
                            }
                        ) {
                            Text(
                                text = "“",
                                color = Color(uiState.designConfig.textColorLong).copy(alpha = 0.35f),
                                fontSize = 48.sp,
                                lineHeight = 36.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (uiState.quoteText.isNotBlank()) uiState.quoteText else "Type your original quote or shayari below...",
                                color = Color(uiState.designConfig.textColorLong),
                                fontSize = uiState.designConfig.fontSizeSp.sp,
                                lineHeight = (uiState.designConfig.fontSizeSp * 1.4f).sp,
                                textAlign = when (uiState.designConfig.textAlignment) {
                                    "start" -> TextAlign.Start
                                    "end" -> TextAlign.End
                                    else -> TextAlign.Center
                                },
                                fontFamily = when (fontPreset.fontFamilyKey) {
                                    "serif" -> FontFamily.Serif
                                    "monospace" -> FontFamily.Monospace
                                    "cursive" -> FontFamily.Cursive
                                    else -> FontFamily.Default
                                },
                                fontStyle = if (fontPreset.isItalic) FontStyle.Italic else FontStyle.Normal,
                                fontWeight = if (fontPreset.isBold) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (uiState.designConfig.showSignature && uiState.designConfig.authorSignature.isNotBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "— ${uiState.designConfig.authorSignature}",
                                    color = Color(uiState.designConfig.textColorLong).copy(alpha = 0.8f),
                                    fontSize = 13.sp,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quote Text Input
                    OutlinedTextField(
                        value = uiState.quoteText,
                        onValueChange = { viewModel.onQuoteTextChanged(it) },
                        label = { Text("Write your quote or shayari") },
                        placeholder = { Text("Express your thoughts in Hindi, Hinglish, or English...") },
                        minLines = 3,
                        maxLines = 6,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Background Preset Picker
                    Text(
                        text = "Choose Background",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(QuoteDesignConfig.BACKGROUND_PRESETS) { idx, p ->
                            val isSelected = uiState.designConfig.backgroundIndex == idx
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(Color(p.primaryColor), Color(p.secondaryColor)))
                                    )
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.setBackgroundIndex(idx) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Font Style Picker
                    Text(
                        text = "Typography Style",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(QuoteDesignConfig.FONT_PRESETS) { idx, font ->
                            val isSelected = uiState.designConfig.fontStyleIndex == idx
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setFontStyleIndex(idx) },
                                label = { Text(font.name) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Alignment & Text Size
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Alignment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row {
                            IconButton(onClick = { viewModel.setTextAlignment("start") }) {
                                Icon(Icons.Outlined.FormatAlignLeft, contentDescription = "Align Left")
                            }
                            IconButton(onClick = { viewModel.setTextAlignment("center") }) {
                                Icon(Icons.Outlined.FormatAlignCenter, contentDescription = "Align Center")
                            }
                            IconButton(onClick = { viewModel.setTextAlignment("end") }) {
                                Icon(Icons.Outlined.FormatAlignRight, contentDescription = "Align Right")
                            }
                        }
                    }

                    // Font size slider
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Text Size: ", style = MaterialTheme.typography.bodyMedium)
                        Slider(
                            value = uiState.designConfig.fontSizeSp,
                            onValueChange = { viewModel.setFontSizeSp(it) },
                            valueRange = 16f..36f,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Watermark Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Show Author Signature", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = uiState.designConfig.showSignature,
                            onCheckedChange = { viewModel.toggleSignature(it) }
                        )
                    }
                }

                CreationMode.TEXT_ONLY -> {
                    OutlinedTextField(
                        value = uiState.quoteText,
                        onValueChange = { viewModel.onQuoteTextChanged(it) },
                        label = { Text("Write your quote or shayari") },
                        placeholder = { Text("Enter your original lines in Hindi, Hinglish, or English...") },
                        minLines = 5,
                        maxLines = 10,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }

                CreationMode.IMAGE_UPLOAD -> {
                    if (uiState.selectedImageUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            AsyncImage(
                                model = uiState.selectedImageUri,
                                contentDescription = "Selected Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            IconButton(
                                onClick = { viewModel.setSelectedImageUri(null) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(Icons.Outlined.Close, contentDescription = "Remove", tint = Color.White)
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(horizontal = 16.dp)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Image,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Tap to choose photo from gallery",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Supports JPEG, PNG, WEBP (max 5MB)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Category Selection
            Text(
                text = "Select Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.categories, key = { it.categoryId }) { cat ->
                    val isSelected = uiState.selectedCategory.categoryId == cat.categoryId
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectCategory(cat) },
                        label = { Text(cat.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Optional Caption
            OutlinedTextField(
                value = uiState.caption,
                onValueChange = { viewModel.onCaptionChanged(it) },
                label = { Text("Caption (Optional)") },
                placeholder = { Text("Add hashtags, context, or thoughts...") },
                maxLines = 3,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
    }
}
