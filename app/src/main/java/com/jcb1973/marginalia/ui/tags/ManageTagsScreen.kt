package com.jcb1973.marginalia.ui.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jcb1973.marginalia.domain.model.TagColor
import com.jcb1973.marginalia.ui.components.ConfirmDeleteDialog
import com.jcb1973.marginalia.ui.components.TagChip

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ManageTagsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ManageTagsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var deleteTag by remember { mutableStateOf<com.jcb1973.marginalia.domain.model.Tag?>(null) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Tags") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            // Create/edit form
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (state.editingTag != null) "Edit Tag" else "New Tag",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.newTagName,
                        onValueChange = viewModel::updateNewTagName,
                        label = { Text("Tag name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tagNameField"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Color", style = MaterialTheme.typography.labelMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        TagColor.entries.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color.color)
                                    .then(
                                        if (state.selectedColor == color) {
                                            Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                        } else Modifier
                                    )
                                    .clickable { viewModel.setSelectedColor(color) }
                                    .testTag("colorPicker_${color.name}"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (state.selectedColor == color) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                    Row {
                        if (state.editingTag != null) {
                            TextButton(
                                onClick = viewModel::cancelEditing,
                                modifier = Modifier.testTag("cancelEditButton")
                            ) {
                                Text("Cancel")
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(
                            onClick = {
                                if (state.editingTag != null) viewModel.updateTag()
                                else viewModel.createTag()
                            },
                            enabled = state.newTagName.isNotBlank(),
                            modifier = Modifier.testTag("saveTagButton")
                        ) {
                            Text(if (state.editingTag != null) "Update" else "Create")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(state.tags, key = { it.tag.id }) { tagWithCount ->
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("tagItem_${tagWithCount.tag.name}"),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TagChip(tag = tagWithCount.tag)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${tagWithCount.bookCount} book${if (tagWithCount.bookCount != 1) "s" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = { viewModel.startEditing(tagWithCount.tag) },
                                modifier = Modifier.testTag("editTag_${tagWithCount.tag.name}")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit ${tagWithCount.tag.displayName}")
                            }
                            IconButton(
                                onClick = { deleteTag = tagWithCount.tag },
                                modifier = Modifier.testTag("deleteTag_${tagWithCount.tag.name}")
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete ${tagWithCount.tag.displayName}",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    deleteTag?.let { tag ->
        ConfirmDeleteDialog(
            title = "Delete Tag",
            message = "Delete \"${tag.displayName}\"? It will be removed from all books.",
            onConfirm = { viewModel.deleteTag(tag); deleteTag = null },
            onDismiss = { deleteTag = null }
        )
    }
}
