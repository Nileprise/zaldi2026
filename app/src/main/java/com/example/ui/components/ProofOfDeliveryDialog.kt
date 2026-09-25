package com.example.ui.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookingOrder
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BorderLight
import com.example.ui.theme.LogisticsBlue
import com.example.ui.theme.LogisticsBlueContainer
import com.example.ui.theme.SuccessContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.SurfaceTertiary
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted

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
    val signaturePaths = remember { mutableStateListOf<List<Offset>>() }
    var currentPath by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var capturedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            capturedPhotoUri = uri
            try {
                capturedBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            } catch (e: Exception) {
                // Keep capturedPhotoUri recorded
            }
        }
    }

    // Camera launcher contract for on-the-spot delivery snap
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            capturedPhotoUri = null
        }
    }

    val hasSignature = signaturePaths.isNotEmpty() || currentPath.isNotEmpty()
    val hasPhoto = capturedPhotoUri != null || capturedBitmap != null
    val isProofProvided = when (selectedMode) {
        ProofOfDeliveryMode.SIGNATURE -> hasSignature
        ProofOfDeliveryMode.PHOTO -> hasPhoto
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("proof_of_delivery_dialog"),
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = LogisticsBlueContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (selectedMode == ProofOfDeliveryMode.SIGNATURE) Icons.Default.Draw else Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = LogisticsBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Proof of Delivery (POD)",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = "Order: ${order.id}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextMuted
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SuccessContainer
                    ) {
                        Text(
                            text = "₹${order.fare.toInt()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = SuccessGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mode Tabs: Signature vs Photo
                TabRow(
                    selectedTabIndex = selectedMode.ordinal,
                    containerColor = SurfaceTertiary,
                    contentColor = LogisticsBlue,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedMode.ordinal]),
                            color = LogisticsBlue
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
                                Icon(
                                    imageVector = Icons.Default.Draw,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Customer Signature", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        },
                        modifier = Modifier.testTag("pod_tab_signature")
                    )

                    Tab(
                        selected = selectedMode == ProofOfDeliveryMode.PHOTO,
                        onClick = { selectedMode = ProofOfDeliveryMode.PHOTO },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Parcel Photo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        },
                        modifier = Modifier.testTag("pod_tab_photo")
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Dropoff recipient address reminder
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceLight,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(SuccessGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Recipient: ${order.customerName}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = order.dropoffAddress,
                                fontSize = 10.sp,
                                color = TextMuted,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (selectedMode) {
                    ProofOfDeliveryMode.SIGNATURE -> {
                        Text(
                            text = "Ask recipient to sign within the box below:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDark
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Signature Drawing Canvas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, if (hasSignature) LogisticsBlue else BorderLight, RoundedCornerShape(12.dp))
                                .testTag("pod_signature_canvas")
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragStart = { offset ->
                                                currentPath = listOf(offset)
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                val newOffset = change.position
                                                currentPath = currentPath + newOffset
                                            },
                                            onDragEnd = {
                                                if (currentPath.isNotEmpty()) {
                                                    signaturePaths.add(currentPath)
                                                    currentPath = emptyList()
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
                                    if (pathPoints.size > 1) {
                                        val p = Path()
                                        p.moveTo(pathPoints.first().x, pathPoints.first().y)
                                        for (i in 1 until pathPoints.size) {
                                            p.lineTo(pathPoints[i].x, pathPoints[i].y)
                                        }
                                        drawPath(
                                            path = p,
                                            color = Color(0xFF1E293B),
                                            style = Stroke(
                                                width = 6f,
                                                cap = StrokeCap.Round,
                                                join = StrokeJoin.Round
                                            )
                                        )
                                    }
                                }

                                // Draw ongoing path
                                if (currentPath.size > 1) {
                                    val p = Path()
                                    p.moveTo(currentPath.first().x, currentPath.first().y)
                                    for (i in 1 until currentPath.size) {
                                        p.lineTo(currentPath[i].x, currentPath[i].y)
                                    }
                                    drawPath(
                                        path = p,
                                        color = Color(0xFF1E293B),
                                        style = Stroke(
                                            width = 6f,
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )
                                }
                            }

                            if (!hasSignature) {
                                Text(
                                    text = "✍ Sign here with finger or stylus",
                                    fontSize = 12.sp,
                                    color = TextMuted.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                )
                            } else {
                                TextButton(
                                    onClick = {
                                        signaturePaths.clear()
                                        currentPath = emptyList()
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .testTag("pod_clear_signature_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = Color.Red,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Clear", fontSize = 11.sp, color = Color.Red)
                                }
                            }
                        }
                    }

                    ProofOfDeliveryMode.PHOTO -> {
                        Text(
                            text = "Take a photo of the delivered goods at destination:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDark
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (hasPhoto) {
                            // Show preview
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceTertiary)
                                    .border(1.dp, SuccessGreen, RoundedCornerShape(12.dp))
                                    .testTag("pod_photo_preview")
                            ) {
                                if (capturedBitmap != null) {
                                    androidx.compose.foundation.Image(
                                        bitmap = capturedBitmap!!.asImageBitmap(),
                                        contentDescription = "Captured Delivery Proof",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(160.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(160.dp)
                                            .background(SurfaceTertiary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = SuccessGreen,
                                                modifier = Modifier.size(36.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Photo Selected Successfully",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextDark
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SuccessGreen,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Photo Captured", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        capturedBitmap = null
                                        capturedPhotoUri = null
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .testTag("pod_retake_photo_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Remove",
                                        tint = Color.Red,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Retake", fontSize = 11.sp, color = Color.Red)
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Camera snap button
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(140.dp)
                                        .testTag("pod_camera_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = LogisticsBlueContainer),
                                    onClick = {
                                        try {
                                            cameraLauncher.launch(null)
                                        } catch (e: Exception) {
                                            // Fallback to media picker if direct camera preview unavailable
                                            photoPickerLauncher.launch(
                                                androidx.activity.result.PickVisualMediaRequest(
                                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                                )
                                            )
                                        }
                                    }
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = LogisticsBlue,
                                            modifier = Modifier.size(42.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.CameraAlt,
                                                    contentDescription = "Take Photo",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Take Photo",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = LogisticsBlue
                                        )
                                        Text(
                                            text = "Camera snap",
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                    }
                                }

                                // Photo gallery picker (Zero permission Android Photo Picker)
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(140.dp)
                                        .testTag("pod_gallery_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = AmberContainer),
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            androidx.activity.result.PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    }
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = AmberPrimary,
                                            modifier = Modifier.size(42.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Image,
                                                    contentDescription = "Choose from Gallery",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Pick Image",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = AmberPrimary
                                        )
                                        Text(
                                            text = "Photo library",
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pointsCount = signaturePaths.sumOf { it.size }
                    val photoStr = capturedPhotoUri?.toString() ?: (if (capturedBitmap != null) "camera_bitmap_proof" else null)
                    onConfirmDelivery(pointsCount, photoStr)
                },
                enabled = isProofProvided,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuccessGreen,
                    disabledContainerColor = SuccessGreen.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("pod_confirm_delivery_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Confirm & Complete",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("pod_cancel_button")
            ) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}
