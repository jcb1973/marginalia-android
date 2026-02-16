package com.jcb1973.marginalia.ui.bookform

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jcb1973.marginalia.domain.model.ReadingStatus
import com.jcb1973.marginalia.ui.components.CoverImage
import com.jcb1973.marginalia.ui.components.RatingBar
import com.jcb1973.marginalia.ui.components.TagChip

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookFormScreen(
    onNavigateBack: () -> Unit,
    onNavigateToScanner: () -> Unit,
    viewModel: BookFormViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showTagSuggestions by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onNavigateBack()
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (state.isEditing) "Edit Book" else "Add Book")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("bookFormBackButton")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ISBN Section
            Text(
                text = "ISBN",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = state.isbn,
                    onValueChange = viewModel::updateIsbn,
                    label = { Text("ISBN") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("isbnField"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (state.isLooking) {
                    CircularProgressIndicator(modifier = Modifier.testTag("lookupProgress"))
                } else {
                    IconButton(
                        onClick = viewModel::lookupIsbn,
                        modifier = Modifier.testTag("lookupButton")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Lookup ISBN")
                    }
                }
                IconButton(
                    onClick = onNavigateToScanner,
                    modifier = Modifier.testTag("scanBarcodeButton")
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Scan barcode")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cover preview
            if (state.coverImageUrl != null || state.coverImagePath != null) {
                CoverImage(
                    coverImagePath = state.coverImagePath,
                    coverImageUrl = state.coverImageUrl,
                    size = 120.dp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Title
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::updateTitle,
                label = { Text("Title *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("titleField"),
                singleLine = true,
                isError = state.title.isBlank() && state.message != null
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Authors
            Text("Authors", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.authors.forEach { author ->
                    AssistChip(
                        onClick = { viewModel.removeAuthor(author) },
                        label = { Text(author) },
                        trailingIcon = {
                            Icon(Icons.Default.Close, contentDescription = "Remove $author")
                        }
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = state.authorInput,
                    onValueChange = viewModel::updateAuthorInput,
                    label = { Text("Add author") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("authorInputField"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { viewModel.addAuthor() })
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = viewModel::addAuthor,
                    modifier = Modifier.testTag("addAuthorButton")
                ) {
                    Text("Add")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tags
            Text("Tags", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.selectedTags.forEach { tag ->
                    TagChip(tag = tag, onRemove = { viewModel.removeTag(tag) })
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = state.tagInput,
                    onValueChange = {
                        viewModel.updateTagInput(it)
                        showTagSuggestions = it.isNotBlank()
                    },
                    label = { Text("Add tag") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tagInputField"),
                    singleLine = true
                )
            }
            if (showTagSuggestions) {
                DropdownMenu(
                    expanded = showTagSuggestions,
                    onDismissRequest = { showTagSuggestions = false }
                ) {
                    state.allTags
                        .filter {
                            it.displayName.contains(state.tagInput, ignoreCase = true) &&
                                    state.selectedTags.none { s -> s.id == it.id }
                        }
                        .forEach { tag ->
                            DropdownMenuItem(
                                text = { Text(tag.displayName) },
                                onClick = {
                                    viewModel.addTag(tag)
                                    showTagSuggestions = false
                                }
                            )
                        }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details
            Text(
                text = "Details",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = state.publisher,
                onValueChange = viewModel::updatePublisher,
                label = { Text("Publisher") },
                modifier = Modifier.fillMaxWidth().testTag("publisherField"),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.publishedDate,
                onValueChange = viewModel::updatePublishedDate,
                label = { Text("Published date") },
                modifier = Modifier.fillMaxWidth().testTag("publishedDateField"),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.pageCount,
                onValueChange = viewModel::updatePageCount,
                label = { Text("Page count") },
                modifier = Modifier.fillMaxWidth().testTag("pageCountField"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::updateDescription,
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .testTag("descriptionField"),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status
            Text("Status", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ReadingStatus.entries.forEachIndexed { index, status ->
                    SegmentedButton(
                        selected = state.status == status,
                        onClick = { viewModel.setStatus(status) },
                        shape = SegmentedButtonDefaults.itemShape(index, ReadingStatus.entries.size),
                        modifier = Modifier.testTag("status_${status.value}")
                    ) {
                        Text(status.displayName)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rating
            Text("Rating", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            RatingBar(
                rating = state.rating,
                onRatingChanged = viewModel::setRating,
                size = 36.dp,
                modifier = Modifier.testTag("bookFormRating")
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save
            Button(
                onClick = viewModel::save,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("saveBookButton"),
                enabled = state.title.isNotBlank()
            ) {
                Text(if (state.isEditing) "Update Book" else "Add Book")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
