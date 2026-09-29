package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Image as ImageIcon
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.BookingOrder
import kotlinx.coroutines.launch

enum class ProofOfDeliveryMode {
    SIGNATURE,
    PHOTO
}

@Composable
fun ProofOfDeliveryDialog(
    order: BookingOrder,
    onDismiss: () -> Unit,
    onConfirmDelivery: (signaturePointsCount: Int, photoUri: String?) -> Unit
) {
    var selectedMode by remember { mutableStateOf(ProofOfDeliveryMode.SIGNATURE) }
    
    // State hoisted from the SignaturePad to track validity
    var signaturePointsCount by remember { mutableIntStateOf(0) }
    
    // Photo State
    var capturedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            capturedPhotoUri = it
            capturedBitmap = loadBitmapSafe(context, it)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            capturedBitmap = it
            capturedPhotoUri = null
        }
    }

    val isProofProvided = when (selectedMode) {
        ProofOfDeliveryMode.SIGNATURE -> signaturePointsCount > 10 // Require a meaningful stroke
        ProofOfDeliveryMode.PHOTO -> capturedPhotoUri != null || capturedBitmap != null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("proof_of_delivery_dialog"),
        shape = RoundedCornerShape(24.dp),
        title = {
            DialogHeader(
                orderId = order.id,
                fare = order.fare,
                selectedMode = selectedMode
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Mode Selection Tabs
                TabRow(
                    selectedTabIndex = selectedMode.ordinal,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedMode.ordinal]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("pod_tab_row")
                ) {
                    Tab(
                        selected = selectedMode == ProofOfDeliveryMode.SIGNATURE,
                        onClick = { selectedMode = ProofOfDeliveryMode.SIGNATURE },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Draw, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Signature", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedMode == ProofOfDeliveryMode.PHOTO,
                        onClick = { selectedMode = ProofOfDeliveryMode.PHOTO },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Photo", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Recipient Info Banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Recipient: ${order.customerName}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = order.dropoffAddress,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content Area
                when (selectedMode) {
                    ProofOfDeliveryMode.SIGNATURE -> {
                        Text(
                            text = "Customer Signature Required:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Isolated recomposition component
                        SignaturePad(
                            onPointsUpdated = { count -> signaturePointsCount = count }
                        )
                    }

                    ProofOfDeliveryMode.PHOTO -> {
                        Text(
                            text = "Parcel Delivery Photo Required:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        PhotoPickerArea(
                            capturedBitmap = capturedBitmap,
                            onLaunchCamera = {
                                try {
                                    cameraLauncher.launch(null)
                                } catch (e: Exception) {
                                    photoPickerLauncher.launch(
                                        androidx.activity.result.PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                }
                            },
                            onLaunchGallery = {
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            onClearPhoto = {
                                capturedBitmap = null
                                capturedPhotoUri = null
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val photoStr = capturedPhotoUri?.toString() 
                        ?: (if (capturedBitmap != null) "camera_bitmap_proof" else null)
                    onConfirmDelivery(signaturePointsCount, photoStr)
                },
                enabled = isProofProvided,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirm Delivery", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ============================================================================
// Extracted Sub-Components for Performance and Clarity
// ============================================================================

@Composable
private fun DialogHeader(orderId: String, fare: Double, selectedMode: ProofOfDeliveryMode) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (selectedMode == ProofOfDeliveryMode.SIGNATURE) Icons.Default.Draw else Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Proof of Delivery",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Order #$orderId",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Isolated Signature Pad. 
 * Prevents the entire dialog from recomposing during fast drag gestures.
 */
@Composable
private fun SignaturePad(
    onPointsUpdated: (Int) -> Unit
) {
    val signaturePaths = remember { mutableStateListOf<List<Offset>>() }
    var currentPath by remember { mutableStateOf<List<Offset>>(emptyList()) }
    
    val hasSignature = signaturePaths.isNotEmpty() || currentPath.isNotEmpty()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp, 
                color = if (hasSignature) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, 
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPath = listOf(offset)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentPath = currentPath + change.position
                        },
                        onDragEnd = {
                            if (currentPath.isNotEmpty()) {
                                signaturePaths.add(currentPath)
                                currentPath = emptyList()
                                onPointsUpdated(signaturePaths.sumOf { it.size })
                            }
                        }
                    )
                }
        ) {
            // Draw baseline guide
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(20f, size.height * 0.75f),
                end = Offset(size.width - 20f, size.height * 0.75f),
                strokeWidth = 2f
            )

            // Draw completed paths
            for (pathPoints in signaturePaths) {
                drawPathSegment(pathPoints)
            }

            // Draw ongoing path
            if (currentPath.isNotEmpty()) {
                drawPathSegment(currentPath)
            }
        }

        if (!hasSignature) {
            Text(
                text = "✍ Sign here",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            TextButton(
                onClick = {
                    signaturePaths.clear()
                    currentPath = emptyList()
                    onPointsUpdated(0)
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clear", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPathSegment(points: List<Offset>) {
    if (points.size > 1) {
        val p = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }
        drawPath(
            path = p,
            color = Color(0xFF1E293B), // Dark ink color regardless of theme
            style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun PhotoPickerArea(
    capturedBitmap: Bitmap?,
    onLaunchCamera: () -> Unit,
    onLaunchGallery: () -> Unit,
    onClearPhoto: () -> Unit
) {
    if (capturedBitmap != null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
        ) {
            Image(
                bitmap = capturedBitmap.asImageBitmap(),
                contentDescription = "Captured Delivery Proof",
                modifier = Modifier.fillMaxSize()
            )

            TextButton(
                onClick = onClearPhoto,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Clear, contentDescription = "Retake", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Retake", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SelectionCard(
                modifier = Modifier.weight(1f),
                title = "Take Photo",
                subtitle = "Camera",
                icon = Icons.Default.CameraAlt,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = onLaunchCamera
            )

            SelectionCard(
                modifier = Modifier.weight(1f),
                title = "Pick Image",
                subtitle = "Gallery",
                icon = Icons.Default.ImageIcon,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = onLaunchGallery
            )
        }
    }
}

@Composable
private fun SelectionCard(
    modifier: Modifier,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable(role = Role.Button) { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = contentColor,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = title, tint = containerColor, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = contentColor)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.8f))
        }
    }
}

// Helper to safely load bitmaps across Android versions
private fun loadBitmapSafe(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (e: Exception) {
        null
    }
}
