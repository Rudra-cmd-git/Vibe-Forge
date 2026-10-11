package com.vibecoder.ui.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GitHub
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public

@Composable
fun ProjectCard(
    project: ProjectSummary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Project icon
            Icon(
                Icons.Default.FolderOpen,
                contentDescription = "",
                tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            spacer(Modifier.width(16.dp))
            
            // Project info
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        project.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.TextOverflow.Ellipsis
                    )
                    project.githubRepo?.let { repo ->
                        if (repo.contains("/")) {
                            val parts = repo.split("/")
                            if (parts.size == 2) {
                                // Would check if private from stored data
                                Icon(Icons.Default.GitHub, contentDescription = "GitHub", tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                
                project.description?.let { desc ->
                    Text(desc, fontSize = 14.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = androidx.compose.ui.text.TextOverflow.Ellipsis)
                }
                
                Text(
                    "Last opened: ${formatTime(project.lastOpened)}",
                    fontSize = 12.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            Icon(Icons.Default.ArrowBack, contentDescription = "", tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000} min ago"
        diff < 86400000 -> "${diff / 3600000} hours ago"
        diff < 604800000 -> "${diff / 86400000} days ago"
        else -> {
            val format = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
            format.format(java.util.Date(timestamp))
        }
    }
}