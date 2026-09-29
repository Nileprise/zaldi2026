package com.example.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.CustomerSubScreen
import com.example.ui.viewmodel.ZaldiViewModel

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun CustomerRatingScreen(
    viewModel: ZaldiViewModel,
    modifier: Modifier = Modifier
) {
    val activeBooking by viewModel.activeCustomerBooking.collectAsState()
    var rating by remember { mutableIntStateOf(5) }
    var reviewText by remember { mutableStateOf("") }
    val selectedTags = remember { mutableStateOf(setOf("Fast Arrival", "Careful Handling")) }

    val tags = listOf(
        "Fast Arrival",
        "Careful Handling",
        "Courteous Driver",
        "Fair Pricing",
        "Clean Vehicle",
        "Safe Driving"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Rate Your Delivery Experience",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.White
        )
        Text(
            text = "How was driver partner ${activeBooking?.driverName ?: "Venkatesh"}?",
            fontSize = 13.sp,
            color = Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Interactive 5 Star Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            (1..5).forEach { star ->
                val isSelected = star <= rating
                Icon(
                    imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "$star stars",
                    tint = if (isSelected) Color(0xFFF59E0B) else Color(0xFF475569),
                    modifier = Modifier
                        .size(42.dp)
                        .clickable { rating = star }
                        .testTag("star_rating_$star")
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Feedback Tags
        Text(
            text = "What went well?",
            fontSize = 13.sp,
            color = Color(0xFFCBD5E1),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        androidx.compose.foundation.layout.FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.forEach { tag ->
                val isTagSelected = selectedTags.value.contains(tag)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isTagSelected) Color(0xFFF59E0B) else Color(0xFF1E293B))
                        .clickable {
                            val newSet = selectedTags.value.toMutableSet()
                            if (isTagSelected) newSet.remove(tag) else newSet.add(tag)
                            selectedTags.value = newSet
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("tag_$tag")
                ) {
                    Text(
                        text = tag,
                        fontSize = 12.sp,
                        fontWeight = if (isTagSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isTagSelected) Color(0xFF0F172A) else Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Review Text Field
        OutlinedTextField(
            value = reviewText,
            onValueChange = { reviewText = it },
            placeholder = { Text("Write additional feedback for driver...", color = Color(0xFF64748B), fontSize = 13.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .testTag("rating_review_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFF59E0B),
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF1E293B)
            )
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                val fullReview = if (reviewText.isNotBlank()) {
                    "$reviewText [${selectedTags.value.joinToString(", ")}]"
                } else {
                    selectedTags.value.joinToString(", ")
                }
                activeBooking?.let {
                    viewModel.submitRating(it.id, rating, fullReview)
                } ?: viewModel.setCustomerSubScreen(CustomerSubScreen.HISTORY)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B), contentColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("submit_rating_button")
        ) {
            Text("Submit Rating", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
