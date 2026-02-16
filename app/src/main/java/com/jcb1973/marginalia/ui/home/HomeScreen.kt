package com.jcb1973.marginalia.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jcb1973.marginalia.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jcb1973.marginalia.domain.model.Book
import com.jcb1973.marginalia.ui.components.CoverImage
import com.jcb1973.marginalia.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToLibrary: (String?) -> Unit,
    onNavigateToBookDetail: (Long) -> Unit,
    onNavigateToBookForm: () -> Unit,
    onExportCsv: () -> Unit,
    onImportCsv: () -> Unit,
    onShowAbout: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showMenu by remember { mutableStateOf(false) }

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
                    Image(
                        painter = painterResource(R.drawable.logo),
                        contentDescription = "Marginalia",
                        modifier = Modifier.size(40.dp)
                    )
                },
                actions = {
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.testTag("homeMenuButton")
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Export CSV") },
                                onClick = { showMenu = false; onExportCsv() },
                                leadingIcon = { Icon(Icons.Default.Upload, null) },
                                modifier = Modifier.testTag("exportCsvButton")
                            )
                            DropdownMenuItem(
                                text = { Text("Import CSV") },
                                onClick = { showMenu = false; onImportCsv() },
                                leadingIcon = { Icon(Icons.Default.Download, null) },
                                modifier = Modifier.testTag("importCsvButton")
                            )
                            DropdownMenuItem(
                                text = { Text("About") },
                                onClick = { showMenu = false; onShowAbout() },
                                leadingIcon = { Icon(Icons.Default.Info, null) },
                                modifier = Modifier.testTag("aboutButton")
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.totalBooks == 0 && !state.isLoading) {
            GettingStartedCard(
                onAddBook = onNavigateToBookForm,
                modifier = Modifier.padding(padding)
            )
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                if (state.currentlyReading.isNotEmpty()) {
                    SectionHeader("Currently Reading")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.currentlyReading, key = { it.id }) { book ->
                            CurrentlyReadingCard(
                                book = book,
                                onClick = { onNavigateToBookDetail(book.id) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                SectionHeader("Your Library")
                StatusCountBar(
                    toRead = state.toReadCount,
                    reading = state.readingCount,
                    read = state.readCount,
                    onStatusClick = onNavigateToLibrary
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (state.recentNotes.isNotEmpty() || state.recentQuotes.isNotEmpty()) {
                    SectionHeader("Latest Notes & Quotes")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.recentNotes) { note ->
                            RecentItemCard(
                                type = "Note",
                                preview = note.content,
                                date = DateUtils.formatRelative(note.createdAt),
                                onClick = { onNavigateToBookDetail(note.bookId) }
                            )
                        }
                        items(state.recentQuotes) { quote ->
                            RecentItemCard(
                                type = "Quote",
                                preview = quote.text,
                                date = DateUtils.formatRelative(quote.createdAt),
                                onClick = { onNavigateToBookDetail(quote.bookId) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .semantics { heading() }
    )
}

@Composable
private fun CurrentlyReadingCard(book: Book, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
            .testTag("currentlyReadingCard_${book.id}"),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CoverImage(
                coverImagePath = book.coverImagePath,
                coverImageUrl = book.coverImageUrl,
                size = 100.dp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = book.title,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.height(32.dp)
            )
            if (book.authors.isNotEmpty()) {
                Text(
                    text = book.authors.first(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun StatusCountBar(
    toRead: Int,
    reading: Int,
    read: Int,
    onStatusClick: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatusCountItem("To Read", toRead, Modifier.testTag("toReadCount")) {
            onStatusClick("toRead")
        }
        StatusCountItem("Reading", reading, Modifier.testTag("readingCount")) {
            onStatusClick("reading")
        }
        StatusCountItem("Read", read, Modifier.testTag("readCount")) {
            onStatusClick("read")
        }
    }
}

@Composable
private fun StatusCountItem(
    label: String,
    count: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = count.toString(), style = MaterialTheme.typography.headlineSmall)
            Text(text = label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun RecentItemCard(
    type: String,
    preview: String,
    date: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row {
                Text(
                    text = type,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = preview,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun GettingStartedCard(onAddBook: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth()
                .testTag("gettingStartedCard"),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Getting Started",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.semantics { heading() }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Scan a barcode to look up a book", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Type an ISBN to search online", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Add a book manually", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Save quotes & notes as you read", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.material3.FilledTonalButton(
                    onClick = onAddBook,
                    modifier = Modifier.testTag("addFirstBookButton")
                ) {
                    Text("Add Your First Book")
                }
            }
        }
    }
}
