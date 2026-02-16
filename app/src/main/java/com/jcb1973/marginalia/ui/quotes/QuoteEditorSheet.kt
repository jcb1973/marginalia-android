package com.jcb1973.marginalia.ui.quotes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jcb1973.marginalia.ui.components.ConfirmDeleteDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteEditorSheet(
    onDismiss: () -> Unit,
    onPhotographPage: () -> Unit,
    viewModel: QuoteEditorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSaved, state.isDeleted) {
        if (state.isSaved || state.isDeleted) onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (state.isEditing) "Edit Quote" else "New Quote",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.text,
                onValueChange = viewModel::updateText,
                label = { Text("Quote text") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .testTag("quoteTextField"),
                maxLines = 8
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onPhotographPage,
                modifier = Modifier.testTag("photographPageButton")
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Photograph Page")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.comment,
                onValueChange = viewModel::updateComment,
                label = { Text("Comment (optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quoteCommentField"),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.pageNumber,
                onValueChange = viewModel::updatePageNumber,
                label = { Text("Page number (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quotePageField"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                if (state.isEditing) {
                    TextButton(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.testTag("deleteQuoteButton")
                    ) {
                        Text("Delete")
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = viewModel::save,
                    enabled = state.text.isNotBlank(),
                    modifier = Modifier.testTag("saveQuoteButton")
                ) {
                    Text("Save")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            title = "Delete Quote",
            message = "Are you sure you want to delete this quote?",
            onConfirm = { viewModel.delete(); showDeleteDialog = false },
            onDismiss = { showDeleteDialog = false }
        )
    }
}
