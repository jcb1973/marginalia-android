package com.jcb1973.marginalia.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jcb1973.marginalia.ui.about.AboutDialog
import com.jcb1973.marginalia.ui.bookform.BookFormScreen
import com.jcb1973.marginalia.ui.detail.BookDetailScreen
import com.jcb1973.marginalia.ui.home.HomeScreen
import com.jcb1973.marginalia.ui.library.LibraryScreen
import com.jcb1973.marginalia.ui.notes.AllNotesScreen
import com.jcb1973.marginalia.ui.notes.NoteEditorSheet
import com.jcb1973.marginalia.ui.quotes.AllQuotesScreen
import com.jcb1973.marginalia.ui.quotes.QuoteEditorSheet
import com.jcb1973.marginalia.ui.scanner.ScannerScreen
import com.jcb1973.marginalia.ui.tags.ManageTagsScreen

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    var showAboutDialog by remember { mutableStateOf(false) }

    NavHost(navController = navController, startDestination = Screen.Home.route, modifier = modifier) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToLibrary = { status ->
                    navController.navigate(Screen.Library.createRoute(status))
                },
                onNavigateToBookDetail = { bookId ->
                    navController.navigate(Screen.BookDetail.createRoute(bookId))
                },
                onNavigateToBookForm = {
                    navController.navigate(Screen.BookForm.createRoute())
                },
                onExportCsv = { /* TODO: launch SAF file picker */ },
                onImportCsv = { /* TODO: launch SAF file picker */ },
                onShowAbout = { showAboutDialog = true }
            )
        }

        composable(
            route = Screen.Library.route,
            arguments = listOf(
                navArgument("status") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val status = backStackEntry.arguments?.getString("status")
            LibraryScreen(
                initialStatus = status,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBookDetail = { bookId ->
                    navController.navigate(Screen.BookDetail.createRoute(bookId))
                },
                onNavigateToBookForm = {
                    navController.navigate(Screen.BookForm.createRoute())
                }
            )
        }

        composable(
            route = Screen.BookDetail.route,
            arguments = listOf(navArgument("bookId") { type = NavType.LongType })
        ) {
            BookDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { bookId ->
                    navController.navigate(Screen.BookForm.createRoute(bookId))
                },
                onNavigateToAllNotes = { bookId ->
                    navController.navigate(Screen.AllNotes.createRoute(bookId))
                },
                onNavigateToAllQuotes = { bookId ->
                    navController.navigate(Screen.AllQuotes.createRoute(bookId))
                },
                onNavigateToNoteEditor = { bookId, noteId ->
                    navController.navigate(Screen.NoteEditor.createRoute(bookId, noteId))
                },
                onNavigateToQuoteEditor = { bookId, quoteId ->
                    navController.navigate(Screen.QuoteEditor.createRoute(bookId, quoteId))
                }
            )
        }

        composable(
            route = Screen.BookForm.route,
            arguments = listOf(
                navArgument("bookId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val scannedIsbn = backStackEntry.savedStateHandle.get<String>("scannedIsbn")
            BookFormScreen(
                scannedIsbn = scannedIsbn,
                onScannedIsbnConsumed = {
                    backStackEntry.savedStateHandle.remove<String>("scannedIsbn")
                },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToScanner = {
                    navController.navigate(Screen.Scanner.route)
                }
            )
        }

        composable(
            route = Screen.AllNotes.route,
            arguments = listOf(navArgument("bookId") { type = NavType.LongType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
            AllNotesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNoteEditor = { bId, noteId ->
                    navController.navigate(Screen.NoteEditor.createRoute(bId, noteId))
                }
            )
        }

        composable(
            route = Screen.NoteEditor.route,
            arguments = listOf(
                navArgument("bookId") { type = NavType.LongType },
                navArgument("noteId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) {
            NoteEditorSheet(
                onDismiss = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AllQuotes.route,
            arguments = listOf(navArgument("bookId") { type = NavType.LongType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
            AllQuotesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToQuoteEditor = { bId, quoteId ->
                    navController.navigate(Screen.QuoteEditor.createRoute(bId, quoteId))
                }
            )
        }

        composable(
            route = Screen.QuoteEditor.route,
            arguments = listOf(
                navArgument("bookId") { type = NavType.LongType },
                navArgument("quoteId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) {
            QuoteEditorSheet(
                onDismiss = { navController.popBackStack() },
                onPhotographPage = { /* TODO: launch camera for OCR */ }
            )
        }

        composable(Screen.Scanner.route) {
            ScannerScreen(
                onIsbnScanned = { isbn ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("scannedIsbn", isbn)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(Screen.ManageTags.route) {
            ManageTagsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}
