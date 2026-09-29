package com.example.ui.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.data.model.UserRole

@Composable
fun RoleSwitcherPill(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .selectableGroup() // A11y: Tells screen readers this is a grouped selection area
            .testTag("role_switcher_pill"),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant, // Replaced SurfaceTertiary
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) // Replaced BorderLight
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Future-proofing: Iterate over entries so new roles appear automatically
            UserRole.entries.forEach { role ->
                RoleItem(
                    role = role,
                    isSelected = currentRole == role,
                    onClick = { onRoleSelected(role) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RoleItem(
    role: UserRole,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine title and icon locally to keep the parent clean
    val title = role.name.lowercase().replaceFirstChar { it.uppercase() }
    val icon = when (role) {
        UserRole.CUSTOMER -> Icons.Default.Person
        UserRole.DRIVER -> Icons.Default.DirectionsCar
        UserRole.ADMIN -> Icons.Default.AdminPanelSettings
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "role_bg"
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 200),
        label = "role_content"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            // A11y: selectable is the correct modifier for tab/radio behavior
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.Tab
            )
            .padding(vertical = 8.dp, horizontal = 6.dp)
            .testTag("role_item_${role.name}"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null, // Handled by text for screen readers
                tint = contentColor,
                modifier = Modifier
                    .size(16.dp)
                    .padding(end = 4.dp)
            )
            Text(
                text = title,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else contentColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

// ============================================================================
// UI Previews
// ============================================================================

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun RoleSwitcherPillPreview() {
    MaterialTheme {
        var selectedRole by remember { mutableStateOf(UserRole.CUSTOMER) }
        
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            RoleSwitcherPill(
                currentRole = selectedRole,
                onRoleSelected = { selectedRole = it }
            )
        }
    }
}
