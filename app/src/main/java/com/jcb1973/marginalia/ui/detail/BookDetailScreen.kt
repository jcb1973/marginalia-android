package com.jcb1973.marginalia.ui.detail

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jcb1973.marginalia.domain.model.Note
import com.jcb1973.marginalia.domain.model.Quote
import com.jcb1973.marginalia.ui.components.ConfirmDeleteDialog
import com.jcb1973.marginalia.ui.components.CoverImage
import com.jcb1973.marginalia.ui.components.ExpandableText
import com.jcb1973.marginalia.ui.components.RatingBar
import com.jcb1973.marginalia.ui.components.StatusBadge
import com.jcb1973.marginalia.ui.components.TagChip
import com.jcb1973.marginalia.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToAllNotes: (Long) -> Unit,
    onNavigateToAllQuotes: (Long) -> Unit,
    onNavigateToNoteEditor: (Long, Long?) -> Unit,
    onNavigateToQuoteEditor: (Long, Long?) -> Unit,
    viewModel: BookDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showTagMenu by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("detailBackButton")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    state.book?.let { book ->
                        IconButton(
                            onClick = { onNavigateToEdit(book.id) },
                            modifier = Modifier.testTag("editBookButton")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.testTag("deleteBookButton")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val book = state.book ?: return@Scaffold

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Cover + basic info
            Row {
                CoverImage(
                    coverImagePath = book.coverImagePath,
                    coverImageUrl = book.coverImageUrl,
                    size = 120.dp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.semantics { heading() }
                    )
                    if (book.authors.isNotEmpty()) {
                        Text(
                            text = book.authors.joinToString(", "),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    StatusBadge(
                        status = book.status,
                        modifier = Modifier
                            .clickable { viewModel.cycleStatus() }
                            .testTag("cycleStatusButton")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RatingBar(
                        rating = book.rating,
                        onRatingChanged = viewModel::setRating,
                        size = 28.dp,
                        modifier = Modifier.testTag("detailRatingBar")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metadata
            if (book.publisher != null || book.publishedDate != null || book.pageCount != null || book.isbn != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        book.publisher?.let { MetadataRow("Publisher", it) }
                        book.publishedDate?.let { MetadataRow("Published", it) }
                        book.pageCount?.let { MetadataRow("Pages", it.toString()) }
                        book.isbn?.let { MetadataRow("ISBN", it) }
                        book.dateAdded.let { MetadataRow("Added", DateUtils.formatShort(it)) }
                        book.dateStarted?.let { MetadataRow("Started", DateUtils.formatShort(it)) }
                        book.dateFinished?.let { MetadataRow("Finished", DateUtils.formatShort(it)) }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Description
            if (!book.description.isNullOrBlank()) {
                ExpandableText(text = book.description, maxLines = 4)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Tags
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tags", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.weight(1f))
                Box {
                    IconButton(
                        onClick = { showTagMenu = true },
                        modifier = Modifier.testTag("addTagButton")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add tag")
                    }
                    DropdownMenu(expanded = showTagMenu, onDismissRequest = { showTagMenu = false }) {
                        state.allTags.filter { available -> book.tags.none { it.id == available.id } }
                            .forEach { tag ->
                                DropdownMenuItem(
                                    text = { Text(tag.displayName) },
                                    onClick = {
                                        viewModel.addTag(tag)
                                        showTagMenu = false
                                    }
                                )
                            }
                    }
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                book.tags.forEach { tag ->
                    TagChip(tag = tag, onRemove = { viewModel.removeTag(tag) })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes & Quotes tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Notes (${state.notes.size})", modifier = Modifier.padding(12.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Quotes (${state.quotes.size})", modifier = Modifier.padding(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedTab) {
                0 -> {
                    Row {
                        Card(
                            modifier = Modifier
                                .height(80.dp)
                                .width(100.dp)
                                .clickable { onNavigateToNoteEditor(book.id, null) }
                                .testTag("addNoteCard"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Add, contentDescription = "Add note")
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(end = 16.dp)
                        ) {
                            items(state.notes.take(5)) { note ->
                                NotePreviewCard(note) { onNavigateToNoteEditor(book.id, note.id) }
                            }
                        }
                    }
                    TextButton(
                        onClick = { onNavigateToAllNotes(book.id) },
                        modifier = Modifier.testTag("viewAllNotesButton")
                    ) {
                        Text("View all notes")
                    }
                }
                1 -> {
                    Row {
                        Card(
                            modifier = Modifier
                                .height(80.dp)
                                .width(100.dp)
                                .clickable { onNavigateToQuoteEditor(book.id, null) }
                                .testTag("addQuoteCard"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Add, contentDescription = "Add quote")
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(end = 16.dp)
                        ) {
                            items(state.quotes.take(5)) { quote ->
                                QuotePreviewCard(quote) { onNavigateToQuoteEditor(book.id, quote.id) }
                            }
                        }
                    }
                    TextButton(
                        onClick = { onNavigateToAllQuotes(book.id) },
                        modifier = Modifier.testTag("viewAllQuotesButton")
                    ) {
                        Text("View all quotes")
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            title = "Delete Book",
            message = "This will permanently delete \"${state.book?.title}\" and all its notes and quotes.",
            onConfirm = { viewModel.deleteBook(); showDeleteDialog = false },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun NotePreviewCard(note: Note, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(80.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = DateUtils.formatRelative(note.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuotePreviewCard(quote: Quote, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(80.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = quote.text,
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = DateUtils.formatRelative(quote.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
