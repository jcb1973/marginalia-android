package com.jcb1973.marginalia.ui.library

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jcb1973.marginalia.domain.model.ReadingStatus
import com.jcb1973.marginalia.domain.model.SortOrder
import com.jcb1973.marginalia.ui.components.BookCard
import com.jcb1973.marginalia.ui.components.ConfirmDeleteDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    initialStatus: String?,
    onNavigateBack: () -> Unit,
    onNavigateToBookDetail: (Long) -> Unit,
    onNavigateToBookForm: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var deleteBookId by remember { mutableStateOf<Long?>(null) }
    var searchActive by remember { mutableStateOf(false) }

    LaunchedEffect(initialStatus) {
        if (initialStatus != null) {
            viewModel.setStatusFilter(ReadingStatus.fromValue(initialStatus))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Library") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("libraryBackButton")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { searchActive = !searchActive },
                        modifier = Modifier.testTag("searchButton")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.testTag("sortButton")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                            SortOrder.entries.forEach { order ->
                                DropdownMenuItem(
                                    text = { Text(order.displayName) },
                                    onClick = {
                                        viewModel.setSortOrder(order)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                    Box {
                        IconButton(
                            onClick = { showFilterMenu = true },
                            modifier = Modifier.testTag("filterButton")
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                        DropdownMenu(expanded = showFilterMenu, onDismissRequest = { showFilterMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("All Statuses") },
                                onClick = { viewModel.setStatusFilter(null); showFilterMenu = false }
                            )
                            ReadingStatus.entries.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status.displayName) },
                                    onClick = { viewModel.setStatusFilter(status); showFilterMenu = false }
                                )
                            }
                            DropdownMenuItem(text = { Text("---") }, onClick = {})
                            DropdownMenuItem(
                                text = { Text("All Ratings") },
                                onClick = { viewModel.setRatingFilter(null); showFilterMenu = false }
                            )
                            for (r in 1..5) {
                                DropdownMenuItem(
                                    text = { Text("$r Star${if (r > 1) "s" else ""}") },
                                    onClick = { viewModel.setRatingFilter(r); showFilterMenu = false }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToBookForm,
                modifier = Modifier.testTag("addBookFab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add book")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (searchActive) {
                SearchBar(
                    query = state.searchQuery,
                    onQueryChange = viewModel::search,
                    onSearch = { searchActive = false },
                    active = false,
                    onActiveChange = {},
                    placeholder = { Text("Search books, authors, notes...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("librarySearchBar")
                ) {}
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (state.allTags.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.allTags) { tag ->
                        FilterChip(
                            selected = tag.id in state.selectedTags,
                            onClick = { viewModel.toggleTag(tag.id) },
                            label = { Text(tag.displayName) },
                            modifier = Modifier.testTag("tagFilter_${tag.name}")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (state.isLoading && state.books.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.testTag("libraryLoading"))
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(state.books, key = { _, book -> book.id }) { index, book ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    deleteBookId = book.id
                                }
                                false
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {},
                            enableDismissFromStartToEnd = false
                        ) {
                            BookCard(
                                book = book,
                                onClick = { onNavigateToBookDetail(book.id) }
                            )
                        }

                        if (index == state.books.lastIndex && state.hasMore) {
                            LaunchedEffect(Unit) { viewModel.loadMore() }
                        }
                    }

                    if (state.isLoading && state.books.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }

    deleteBookId?.let { id ->
        ConfirmDeleteDialog(
            title = "Delete Book",
            message = "This will permanently delete this book and all its notes and quotes.",
            onConfirm = {
                viewModel.deleteBook(id)
                deleteBookId = null
            },
            onDismiss = { deleteBookId = null }
        )
    }
}
