package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.BrandSlate
import com.example.ui.theme.SurfaceTertiary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.BorderLight

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
            .testTag("role_switcher_pill"),
        shape = RoundedCornerShape(8.dp), // Sharper corporate corners
        color = SurfaceTertiary,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
        shadowElevation = 0.dp // Removed shadow for flat design
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoleItem(
                title = "Customer",
                icon = Icons.Default.Person,
                isSelected = currentRole == UserRole.CUSTOMER,
                onClick = { onRoleSelected(UserRole.CUSTOMER) },
                modifier = Modifier.weight(1f)
            )
            RoleItem(
                title = "Driver",
                icon = Icons.Default.DirectionsCar,
                isSelected = currentRole == UserRole.DRIVER,
                onClick = { onRoleSelected(UserRole.DRIVER) },
                modifier = Modifier.weight(1f)
            )
            RoleItem(
                title = "Admin",
                icon = Icons.Default.AdminPanelSettings,
                isSelected = currentRole == UserRole.ADMIN,
                onClick = { onRoleSelected(UserRole.ADMIN) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RoleItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) SurfaceCard else Color.Transparent,
        label = "role_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) AccentBlue else TextMuted,
        label = "role_content"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp)) // Inner items align with outer 8dp radius
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(16.dp).padding(end = 4.dp)
            )
            Text(
                text = title,
                color = if (isSelected) BrandSlate else contentColor, // Deep slate text for active
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}
