package com.jcb1973.marginalia.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.jcb1973.marginalia.domain.model.Tag

@Composable
fun TagChip(
    tag: Tag,
    onRemove: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val bgColor = tag.color?.color?.copy(alpha = 0.15f) ?: MaterialTheme.colorScheme.surfaceVariant
    val textColor = tag.color?.color ?: MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        color = bgColor,
        shape = MaterialTheme.shapes.small,
        modifier = modifier.testTag("tagChip_${tag.name}")
    ) {
        androidx.compose.foundation.layout.Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp, end = if (onRemove != null) 4.dp else 8.dp)
        ) {
            Text(
                text = tag.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            if (onRemove != null) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .size(20.dp)
                        .testTag("removeTag_${tag.name}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove ${tag.displayName}",
                        tint = textColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
