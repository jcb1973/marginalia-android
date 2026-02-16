package com.jcb1973.marginalia.ui.scanner

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class ScannerUiState(
    val scannedIsbn: String? = null,
    val hasCameraPermission: Boolean = false,
    val showRationale: Boolean = false
)

@HiltViewModel
class ScannerViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    fun onBarcodeDetected(isbn: String) {
        if (_uiState.value.scannedIsbn == null) {
            _uiState.value = _uiState.value.copy(scannedIsbn = isbn)
        }
    }

    fun setCameraPermission(granted: Boolean) {
        _uiState.value = _uiState.value.copy(hasCameraPermission = granted)
    }

    fun setShowRationale(show: Boolean) {
        _uiState.value = _uiState.value.copy(showRationale = show)
    }

    fun resetScan() {
        _uiState.value = _uiState.value.copy(scannedIsbn = null)
    }
}
