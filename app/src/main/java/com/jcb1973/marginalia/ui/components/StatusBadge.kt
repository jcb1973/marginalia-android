package com.jcb1973.marginalia.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.jcb1973.marginalia.domain.model.ReadingStatus

@Composable
fun StatusBadge(
    status: ReadingStatus,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor) = when (status) {
        ReadingStatus.TO_READ -> MaterialTheme.colorScheme.secondaryContainer to
                MaterialTheme.colorScheme.onSecondaryContainer
        ReadingStatus.READING -> MaterialTheme.colorScheme.primaryContainer to
                MaterialTheme.colorScheme.onPrimaryContainer
        ReadingStatus.READ -> MaterialTheme.colorScheme.tertiaryContainer to
                MaterialTheme.colorScheme.onTertiaryContainer
    }

    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.small,
        modifier = modifier.testTag("statusBadge")
    ) {
        Text(
            text = status.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
