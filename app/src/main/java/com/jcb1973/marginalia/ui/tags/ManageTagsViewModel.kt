package com.jcb1973.marginalia.ui.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jcb1973.marginalia.domain.model.Tag
import com.jcb1973.marginalia.domain.model.TagColor
import com.jcb1973.marginalia.domain.repository.TagRepository
import com.jcb1973.marginalia.domain.repository.TagWithCount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManageTagsUiState(
    val tags: List<TagWithCount> = emptyList(),
    val editingTag: Tag? = null,
    val newTagName: String = "",
    val selectedColor: TagColor? = null,
    val isLoading: Boolean = true,
    val message: String? = null
)

@HiltViewModel
class ManageTagsViewModel @Inject constructor(
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageTagsUiState())
    val uiState: StateFlow<ManageTagsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            tagRepository.getAllTagsWithBookCount().collect { tags ->
                _uiState.value = _uiState.value.copy(tags = tags, isLoading = false)
            }
        }
    }

    fun updateNewTagName(name: String) {
        _uiState.value = _uiState.value.copy(newTagName = name)
    }

    fun setSelectedColor(color: TagColor?) {
        _uiState.value = _uiState.value.copy(selectedColor = color)
    }

    fun startEditing(tag: Tag) {
        _uiState.value = _uiState.value.copy(
            editingTag = tag,
            newTagName = tag.displayName,
            selectedColor = tag.color
        )
    }

    fun cancelEditing() {
        _uiState.value = _uiState.value.copy(
            editingTag = null,
            newTagName = "",
            selectedColor = null
        )
    }

    fun createTag() {
        val name = _uiState.value.newTagName.trim()
        if (name.isBlank()) return

        viewModelScope.launch {
            val existing = tagRepository.getTagByName(name.lowercase())
            if (existing != null) {
                _uiState.value = _uiState.value.copy(message = "Tag already exists")
                return@launch
            }
            tagRepository.saveTag(
                Tag(
                    name = name.lowercase(),
                    displayName = name,
                    color = _uiState.value.selectedColor
                )
            )
            _uiState.value = _uiState.value.copy(
                newTagName = "",
                selectedColor = null,
                message = "Tag created"
            )
        }
    }

    fun updateTag() {
        val editing = _uiState.value.editingTag ?: return
        val name = _uiState.value.newTagName.trim()
        if (name.isBlank()) return

        viewModelScope.launch {
            if (name.lowercase() != editing.name) {
                val existing = tagRepository.getTagByName(name.lowercase())
                if (existing != null) {
                    _uiState.value = _uiState.value.copy(message = "Tag name already in use")
                    return@launch
                }
            }
            tagRepository.updateTag(
                editing.copy(
                    name = name.lowercase(),
                    displayName = name,
                    color = _uiState.value.selectedColor
                )
            )
            _uiState.value = _uiState.value.copy(
                editingTag = null,
                newTagName = "",
                selectedColor = null
            )
        }
    }

    fun deleteTag(tag: Tag) {
        viewModelScope.launch {
            tagRepository.deleteTag(tag)
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
