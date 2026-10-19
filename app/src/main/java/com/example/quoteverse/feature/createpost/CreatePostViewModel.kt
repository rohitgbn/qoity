package com.example.quoteverse.feature.createpost

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quoteverse.core.design.components.QuoteCanvasRenderer
import com.example.quoteverse.core.model.Category
import com.example.quoteverse.core.model.Post
import com.example.quoteverse.core.model.PostType
import com.example.quoteverse.core.model.QuoteDesignConfig
import com.example.quoteverse.core.result.Resource
import com.example.quoteverse.data.repository.AuthRepository
import com.example.quoteverse.data.repository.CategoryRepository
import com.example.quoteverse.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

enum class CreationMode {
    QUOTE_DESIGNER, // Designed image with custom backgrounds & typography
    TEXT_ONLY,      // Plain text post
    IMAGE_UPLOAD    // Photo picker upload
}

data class CreatePostUiState(
    val mode: CreationMode = CreationMode.QUOTE_DESIGNER,
    val quoteText: String = "",
    val caption: String = "",
    val selectedCategory: Category = Category.DEFAULT_CATEGORIES.first(),
    val categories: List<Category> = Category.DEFAULT_CATEGORIES,
    val designConfig: QuoteDesignConfig = QuoteDesignConfig(),
    val selectedImageUri: Uri? = null,
    val isPublishing: Boolean = false,
    val uploadProgress: Float = 0f,
    val errorMessage: String? = null
)

sealed class CreatePostEvent {
    object PostPublishedSuccess : CreatePostEvent()
    data class ShowError(val message: String) : CreatePostEvent()
}

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CreatePostEvent>()
    val events: SharedFlow<CreatePostEvent> = _events.asSharedFlow()

    init {
        loadCategories()
        initAuthorSignature()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getCategories().collect { res ->
                if (res is Resource.Success && res.data.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            categories = res.data,
                            selectedCategory = res.data.first()
                        )
                    }
                }
            }
        }
    }

    private fun initAuthorSignature() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            if (user != null) {
                _uiState.update {
                    it.copy(
                        designConfig = it.designConfig.copy(
                            authorSignature = user.displayName.ifBlank { "@${user.username}" }
                        )
                    )
                }
            }
        }
    }

    fun setMode(mode: CreationMode) {
        _uiState.update { it.copy(mode = mode, errorMessage = null) }
    }

    fun onQuoteTextChanged(text: String) {
        if (text.length <= 2000) {
            _uiState.update {
                it.copy(
                    quoteText = text,
                    designConfig = it.designConfig.copy(quoteText = text),
                    errorMessage = null
                )
            }
        }
    }

    fun onCaptionChanged(caption: String) {
        if (caption.length <= 500) {
            _uiState.update { it.copy(caption = caption) }
        }
    }

    fun selectCategory(category: Category) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setSelectedImageUri(uri: Uri?) {
        _uiState.update { it.copy(selectedImageUri = uri, errorMessage = null) }
    }

    // Design adjustments
    fun setBackgroundIndex(index: Int) {
        val preset = QuoteDesignConfig.BACKGROUND_PRESETS.getOrElse(index) {
            QuoteDesignConfig.BACKGROUND_PRESETS.first()
        }
        _uiState.update {
            it.copy(
                designConfig = it.designConfig.copy(
                    backgroundIndex = index,
                    textColorLong = preset.textColor
                )
            )
        }
    }

    fun setFontStyleIndex(index: Int) {
        _uiState.update {
            it.copy(designConfig = it.designConfig.copy(fontStyleIndex = index))
        }
    }

    fun setFontSizeSp(fontSize: Float) {
        _uiState.update {
            it.copy(designConfig = it.designConfig.copy(fontSizeSp = fontSize.coerceIn(16f, 36f)))
        }
    }

    fun setTextAlignment(alignment: String) {
        _uiState.update {
            it.copy(designConfig = it.designConfig.copy(textAlignment = alignment))
        }
    }

    fun toggleSignature(show: Boolean) {
        _uiState.update {
            it.copy(designConfig = it.designConfig.copy(showSignature = show))
        }
    }

    fun publishPost(context: Context) {
        val state = _uiState.value
        val currentUser = authRepository.currentUserId
        if (currentUser.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "You must be signed in to publish") }
            return
        }

        // Validate
        when (state.mode) {
            CreationMode.QUOTE_DESIGNER, CreationMode.TEXT_ONLY -> {
                if (state.quoteText.trim().isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Please enter your quote or shayari") }
                    return
                }
            }
            CreationMode.IMAGE_UPLOAD -> {
                if (state.selectedImageUri == null) {
                    _uiState.update { it.copy(errorMessage = "Please select an image to upload") }
                    return
                }
            }
        }

        if (state.isPublishing) return // Prevent duplicate submission

        viewModelScope.launch {
            _uiState.update { it.copy(isPublishing = true, errorMessage = null) }

            val author = authRepository.getCurrentUser()
            val preset = QuoteDesignConfig.BACKGROUND_PRESETS.getOrElse(state.designConfig.backgroundIndex) {
                QuoteDesignConfig.BACKGROUND_PRESETS.first()
            }
            val fontPreset = QuoteDesignConfig.FONT_PRESETS.getOrElse(state.designConfig.fontStyleIndex) {
                QuoteDesignConfig.FONT_PRESETS.first()
            }

            var imageFileToUpload: File? = null
            val postType: PostType

            when (state.mode) {
                CreationMode.QUOTE_DESIGNER -> {
                    postType = PostType.DESIGNED_IMAGE
                    // Render high resolution bitmap from quote canvas
                    val bitmap = QuoteCanvasRenderer.renderQuoteToBitmap(state.designConfig)
                    imageFileToUpload = QuoteCanvasRenderer.saveBitmapToTempFile(context, bitmap)
                }
                CreationMode.TEXT_ONLY -> {
                    postType = PostType.TEXT
                }
                CreationMode.IMAGE_UPLOAD -> {
                    postType = PostType.UPLOADED_IMAGE
                    // Copy picked image uri to temp file
                    state.selectedImageUri?.let { uri ->
                        val temp = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            FileOutputStream(temp).use { output -> input.copyTo(output) }
                        }
                        imageFileToUpload = temp
                    }
                }
            }

            val basePost = Post(
                authorId = currentUser,
                authorUsername = author?.username ?: "creator",
                authorDisplayName = author?.displayName ?: "Quote Creator",
                authorPhotoUrl = author?.profileImageUrl,
                text = state.quoteText.trim(),
                caption = state.caption.trim(),
                categoryId = state.selectedCategory.categoryId,
                categoryName = state.selectedCategory.name,
                postType = postType,
                backgroundColor = preset.primaryColor,
                textColor = state.designConfig.textColorLong,
                fontFamily = fontPreset.fontFamilyKey,
                textAlign = state.designConfig.textAlignment,
                gradientColors = if (preset.isGradient) listOf(preset.primaryColor, preset.secondaryColor) else emptyList()
            )

            postRepository.createPost(basePost, imageFileToUpload).collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isPublishing = true) }
                    is Resource.Success -> {
                        _uiState.update { it.copy(isPublishing = false) }
                        _events.emit(CreatePostEvent.PostPublishedSuccess)
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(isPublishing = false, errorMessage = result.message)
                        }
                        _events.emit(CreatePostEvent.ShowError(result.message))
                    }
                }
            }
        }
    }
}
