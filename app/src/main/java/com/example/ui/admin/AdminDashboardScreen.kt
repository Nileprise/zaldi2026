package com.example.ui.admin

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.example.data.model.AuditLog
import com.example.data.model.Booking
import com.example.data.model.Complaint
import com.example.data.model.Driver
import com.example.data.model.PricingRule
import com.example.data.model.PromoCode
import com.example.data.model.VehicleType
import com.example.ui.components.InteractiveMapView
import com.example.ui.viewmodel.AdminTab
import com.example.ui.viewmodel.ZaldiViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    viewModel: ZaldiViewModel,
    modifier: Modifier = Modifier
) {
    val adminTab by viewModel.adminTab.collectAsState()
    val allDrivers by viewModel.allDrivers.collectAsState()
    val onlineDrivers by viewModel.onlineDrivers.collectAsState()
    val allBookings by viewModel.allBookings.collectAsState()
    val pricingRules by viewModel.pricingRules.collectAsState()
    val promoCodes by viewModel.promoCodes.collectAsState()
    val complaints by viewModel.complaints.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val activeBooking by viewModel.activeCustomerBooking.collectAsState()

    var selectedMapDriver by remember { mutableStateOf<Driver?>(null) }
    var editingPricingRule by remember { mutableStateOf<PricingRule?>(null) }
    var showAddPromoDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .testTag("admin_dashboard_screen")
    ) {
        // Top 4 Metrics Summary Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdminMetricMiniCard(
                label = "ONLINE DRIVERS",
                value = "${onlineDrivers.size + 120}",
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
            AdminMetricMiniCard(
                label = "ACTIVE BOOKINGS",
                value = "${allBookings.count { !it.isTerminalState } + 47}",
                color = Color(0xFF38BDF8),
                modifier = Modifier.weight(1f)
            )
            AdminMetricMiniCard(
                label = "TODAY'S ORDERS",
                value = "${allBookings.size + 386}",
                color = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f)
            )
            val totalRev = allBookings.sumOf { it.totalFare } + 42500.0
            AdminMetricMiniCard(
                label = "REVENUE",
                value = "₹${totalRev.toInt()}",
                color = Color(0xFFFBBF24),
                modifier = Modifier.weight(1f)
            )
        }

        // Sub Tabs Scrollable Row
        ScrollableTabRow(
            selectedTabIndex = adminTab.ordinal,
            containerColor = Color(0xFF1E293B),
            contentColor = Color(0xFFF59E0B),
            edgePadding = 12.dp,
            modifier = Modifier.fillMaxWidth().testTag("admin_tabs_row")
        ) {
            AdminTab.values().forEach { tab ->
                val isSelected = adminTab == tab
                Tab(
                    selected = isSelected,
                    onClick = { viewModel.setAdminTab(tab) },
                    text = {
                        Text(
                            text = tab.title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFFF59E0B) else Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.testTag("admin_tab_${tab.name.lowercase()}")
                )
            }
        }

        // Tab Content
        Box(modifier = Modifier.fillMaxSize()) {
            when (adminTab) {
                AdminTab.OVERVIEW_MAP -> {
                    AdminLiveMapTab(
                        drivers = allDrivers,
                        activeBooking = activeBooking,
                        selectedDriver = selectedMapDriver,
                        onDriverSelected = { selectedMapDriver = it }
                    )
                }

                AdminTab.DRIVERS -> {
                    AdminDriversTab(
                        drivers = allDrivers,
                        onToggleKyc = { driver, newStatus ->
                            viewModel.updateDriverKyc(driver.id, newStatus)
                        },
                        onToggleDuty = { driver, isOnline ->
                            viewModel.toggleDriverDuty(driver.id, isOnline)
                        }
                    )
                }

                AdminTab.CUSTOMERS -> {
                    AdminCustomersTab()
                }

                AdminTab.BOOKINGS -> {
                    AdminBookingsTab(
                        bookings = allBookings,
                        onCancelOrder = { id -> viewModel.cancelActiveBooking(id, "Cancelled by Admin Dispatch") }
                    )
                }

                AdminTab.PRICING -> {
                    AdminPricingTab(
                        rules = pricingRules,
                        onEdit = { editingPricingRule = it }
                    )
                }

                AdminTab.PROMOS -> {
                    AdminPromosTab(
                        promos = promoCodes,
                        onToggle = { code, isActive -> viewModel.togglePromoStatus(code, isActive) },
                        onAddNew = { showAddPromoDialog = true }
                    )
                }

                AdminTab.COMPLAINTS -> {
                    AdminComplaintsTab(
                        complaints = complaints,
                        onResolve = { id, notes -> viewModel.resolveComplaint(id, notes) }
                    )
                }

                AdminTab.REPORTS -> {
                    AdminReportsTab()
                }

                AdminTab.AUDIT_LOGS -> {
                    AdminAuditLogsTab(logs = auditLogs)
                }
            }
        }
    }

    // Edit Pricing Rule Dialog
    if (editingPricingRule != null) {
        EditPricingDialog(
            rule = editingPricingRule!!,
            onDismiss = { editingPricingRule = null },
            onSave = { updated ->
                viewModel.updatePricingRule(updated)
                editingPricingRule = null
            }
        )
    }

    // Add Promo Code Dialog
    if (showAddPromoDialog) {
        AddPromoDialog(
            onDismiss = { showAddPromoDialog = false },
            onAdd = { promo ->
                viewModel.addPromoCode(promo)
                showAddPromoDialog = false
            }
        )
    }
}

@Composable
fun AdminMetricMiniCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), maxLines = 1)
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
fun AdminLiveMapTab(
    drivers: List<Driver>,
    activeBooking: Booking?,
    selectedDriver: Driver?,
    onDriverSelected: (Driver) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().testTag("admin_live_map_view")) {
        InteractiveMapView(
            modifier = Modifier.fillMaxSize(),
            activeBooking = activeBooking,
            allDrivers = drivers,
            showAllDrivers = true,
            selectedDriver = selectedDriver,
            onDriverSelected = onDriverSelected
        )

        // Fleet legend HUD
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.9f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(text = "LIVE FLEET DISPATCH", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF94A3B8))
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("🟢 Available Drivers", fontSize = 11.sp, color = Color.White)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF3B82F6)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("🔵 Active On-Trip", fontSize = 11.sp, color = Color.White)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("🔴 Offline Drivers", fontSize = 11.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AdminDriversTab(
    drivers: List<Driver>,
    onToggleKyc: (Driver, String) -> Unit,
    onToggleDuty: (Driver, Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(drivers) { driver ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth().testTag("admin_driver_item_${driver.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = driver.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "(${driver.id})", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            }
                            Text(
                                text = "${driver.vehicleType} • ${driver.vehicleModel} • ${driver.vehiclePlate}",
                                fontSize = 12.sp,
                                color = Color(0xFFCBD5E1)
                            )
                            Text(
                                text = "Trips: ${driver.tripsCount} • Rating: ${driver.rating}★ • Wallet: ₹${driver.walletBalance.toInt()}",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        // Duty status badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (driver.isOnline) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (driver.isOnline) "ONLINE" else "OFFLINE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (driver.isOnline) Color(0xFF34D399) else Color(0xFFF87171)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // KYC Action
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "KYC: ", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = driver.kycStatus,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (driver.kycStatus == Driver.KYC_VERIFIED) Color(0xFF10B981) else Color(0xFFF59E0B)
                            )
                        }

                        Row {
                            if (driver.kycStatus != Driver.KYC_VERIFIED) {
                                Button(
                                    onClick = { onToggleKyc(driver, Driver.KYC_VERIFIED) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Approve KYC", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            OutlinedButton(
                                onClick = { onToggleDuty(driver, !driver.isOnline) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(if (driver.isOnline) "Force Offline" else "Force Online", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminBookingsTab(
    bookings: List<Booking>,
    onCancelOrder: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(bookings) { booking ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth().testTag("admin_booking_${booking.bookingCode}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "ORDER ${booking.bookingCode}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            Text(text = "Customer: ${booking.customerName} (${booking.customerPhone})", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                        Text(
                            text = booking.status,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = when (booking.status) {
                                Booking.STATUS_DELIVERED -> Color(0xFF10B981)
                                Booking.STATUS_CANCELLED -> Color(0xFFEF4444)
                                else -> Color(0xFFF59E0B)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Pickup: ${booking.pickupTitle}", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                    Text(text = "Drop: ${booking.dropTitle} (${booking.distanceKm} km)", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                    Text(text = "Vehicle: ${booking.vehicleType} • Driver: ${booking.driverName ?: "Unassigned"}", fontSize = 11.sp, color = Color(0xFF94A3B8))

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Fare: ₹${booking.totalFare.toInt()} (${booking.paymentMethod})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFF59E0B))

                        if (!booking.isTerminalState) {
                            OutlinedButton(
                                onClick = { onCancelOrder(booking.id) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Cancel Order", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminPricingTab(
    rules: List<PricingRule>,
    onEdit: (PricingRule) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Vehicle Pricing Engine",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
            Text(
                text = "Configure base fares and per-KM rates dynamically across vehicle categories",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(rules) { rule ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth().testTag("pricing_card_${rule.vehicleType}")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = rule.vehicleType, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Base Fare: ₹${rule.baseFare.toInt()}", fontSize = 13.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.SemiBold)
                        Text(text = "Rate per KM: ₹${rule.perKmFare.toInt()}/km", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                        Text(text = "Min Fare: ₹${rule.minFare.toInt()}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }

                    IconButton(
                        onClick = { onEdit(rule) },
                        modifier = Modifier.clip(CircleShape).background(Color(0xFF334155))
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Pricing", tint = Color(0xFFF59E0B))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminPromosTab(
    promos: List<PromoCode>,
    onToggle: (String, Boolean) -> Unit,
    onAddNew: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Promotions & Discount Vouchers", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    Text(text = "Active discount codes applied at checkout", fontSize = 12.sp, color = Color(0xFF94A3B8))
                }
                Button(
                    onClick = onAddNew,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B), contentColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_promo_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Promo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(promos) { promo ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth().testTag("promo_card_${promo.code}")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = promo.code, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFFF59E0B))
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (promo.isActive) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (promo.isActive) "ACTIVE" else "DISABLED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (promo.isActive) Color(0xFF34D399) else Color(0xFFF87171)
                                )
                            }
                        }
                        Text(text = promo.description, fontSize = 12.sp, color = Color.White)
                        Text(
                            text = if (promo.flatDiscount > 0) "Flat ₹${promo.flatDiscount.toInt()} OFF" else "${promo.discountPercent}% OFF (Max ₹${promo.maxDiscount.toInt()})",
                            fontSize = 11.sp,
                            color = Color(0xFF38BDF8)
                        )
                        Text(text = "Min Order Value: ₹${promo.minOrderValue.toInt()}", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }

                    Switch(
                        checked = promo.isActive,
                        onCheckedChange = { onToggle(promo.code, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF10B981),
                            checkedTrackColor = Color(0xFF10B981).copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AdminComplaintsTab(
    complaints: List<Complaint>,
    onResolve: (Long, String) -> Unit
) {
    var resolvingComplaint by remember { mutableStateOf<Complaint?>(null) }
    var notesText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(text = "Customer & Driver Support Tickets", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
            Text(text = "Incident dispute resolution desk", fontSize = 12.sp, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (complaints.isEmpty()) {
            item {
                Text(text = "No open customer tickets.", color = Color(0xFF64748B), fontSize = 13.sp)
            }
        }

        items(complaints) { ticket ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Ticket #${ticket.id} (${ticket.submittedByRole})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        Text(
                            text = ticket.status,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (ticket.status == Complaint.STATUS_RESOLVED) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    }
                    Text(text = "Order: ${ticket.bookingCode} • Subject: ${ticket.subject}", fontSize = 12.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.SemiBold)
                    Text(text = ticket.description, fontSize = 12.sp, color = Color(0xFFCBD5E1))

                    if (ticket.resolutionNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Resolution: ${ticket.resolutionNotes}", fontSize = 11.sp, color = Color(0xFF34D399))
                    }

                    if (ticket.status == Complaint.STATUS_OPEN) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { resolvingComplaint = ticket },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Resolve Dispute", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (resolvingComplaint != null) {
        AlertDialog(
            onDismissRequest = { resolvingComplaint = null },
            containerColor = Color(0xFF1E293B),
            title = { Text("Resolve Ticket #${resolvingComplaint!!.id}", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    placeholder = { Text("Enter resolution notes / action taken", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFF59E0B)
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResolve(resolvingComplaint!!.id, notesText.ifBlank { "Dispute resolved with customer compensation." })
                        resolvingComplaint = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.White)
                ) {
                    Text("Confirm Resolution")
                }
            }
        )
    }
}

@Composable
fun AdminAuditLogsTab(logs: List<AuditLog>) {
    val dateFormat = SimpleDateFormat("HH:mm:ss • dd MMM", Locale.getDefault())

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(text = "System Security & Dispatch Audit Logs", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
            Text(text = "Immutable event log of driver dispatches, rate revisions, payments", fontSize = 12.sp, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(logs) { log ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth().testTag("audit_log_${log.id}")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when (log.category) {
                                    "DISPATCH" -> Color(0xFF38BDF8)
                                    "PAYMENT" -> Color(0xFF10B981)
                                    "PRICING" -> Color(0xFFF59E0B)
                                    else -> Color(0xFFA855F7)
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = log.action, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Text(text = dateFormat.format(Date(log.timestamp)), fontSize = 10.sp, color = Color(0xFF94A3B8))
                        }
                        Text(text = log.details, fontSize = 11.sp, color = Color(0xFFCBD5E1))
                    }
                }
            }
        }
    }
}

@Composable
fun EditPricingDialog(
    rule: PricingRule,
    onDismiss: () -> Unit,
    onSave: (PricingRule) -> Unit
) {
    var baseFare by remember { mutableStateOf(rule.baseFare.toString()) }
    var perKm by remember { mutableStateOf(rule.perKmFare.toString()) }
    var minFare by remember { mutableStateOf(rule.minFare.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        title = { Text("Update Pricing: ${rule.vehicleType}", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = baseFare,
                    onValueChange = { baseFare = it },
                    label = { Text("Base Fare (₹)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = perKm,
                    onValueChange = { perKm = it },
                    label = { Text("Per KM Fare (₹)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = minFare,
                    onValueChange = { minFare = it },
                    label = { Text("Minimum Fare (₹)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = rule.copy(
                        baseFare = baseFare.toDoubleOrNull() ?: rule.baseFare,
                        perKmFare = perKm.toDoubleOrNull() ?: rule.perKmFare,
                        minFare = minFare.toDoubleOrNull() ?: rule.minFare
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B), contentColor = Color(0xFF0F172A))
            ) {
                Text("Save Pricing", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}

@Composable
fun AddPromoDialog(
    onDismiss: () -> Unit,
    onAdd: (PromoCode) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("50") }
    var minVal by remember { mutableStateOf("150") }
    var desc by remember { mutableStateOf("Special seasonal delivery discount") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        title = { Text("Create New Promo Code", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    label = { Text("Coupon Code (e.g. ZALDI100)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = discount,
                    onValueChange = { discount = it },
                    label = { Text("Flat Discount (₹)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = minVal,
                    onValueChange = { minVal = it },
                    label = { Text("Min Order Value (₹)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (code.isNotBlank()) {
                        val promo = PromoCode(
                            code = code.trim().uppercase(),
                            flatDiscount = discount.toDoubleOrNull() ?: 50.0,
                            minOrderValue = minVal.toDoubleOrNull() ?: 100.0,
                            maxDiscount = discount.toDoubleOrNull() ?: 50.0,
                            description = desc,
                            isActive = true
                        )
                        onAdd(promo)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B), contentColor = Color(0xFF0F172A))
            ) {
                Text("Create Promo", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}

@Composable
fun AdminCustomersTab() {
    val customers = listOf(
        Triple("Rajesh Sharma", "+91 98765 43210", "42 orders • ₹18,450 spent • 4.9★"),
        Triple("Priya Sundaram", "+91 97421 00982", "28 orders • ₹12,100 spent • 5.0★"),
        Triple("Metro Logistics & FMCG", "+91 99002 44118", "116 orders • ₹94,500 spent • 4.8★"),
        Triple("Anand Retail Appliances", "+91 98450 33819", "64 orders • ₹45,200 spent • 4.9★"),
        Triple("Kavita Enterprises", "+91 96110 55214", "19 orders • ₹8,900 spent • 4.7★")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(text = "Customer Account Directory", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
            Text(text = "Verified shippers & corporate dispatch accounts", fontSize = 12.sp, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(customers) { (name, phone, stats) ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth().testTag("customer_item_$name")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name.firstOrNull()?.toString() ?: "C",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        Text(text = phone, fontSize = 11.sp, color = Color(0xFF38BDF8))
                        Text(text = stats, fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "VERIFIED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminReportsTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(text = "Fleet & Business Intelligence Reports", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
            Text(text = "Key performance indicators & operational efficiency", fontSize = 12.sp, color = Color(0xFF94A3B8))
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Delivery Performance KPIs", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("On-Time Delivery Success Rate", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                        Text("97.6%", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF10B981))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { 0.976f },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF10B981),
                        trackColor = Color(0xFF334155)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Average Driver Pickup Arrival Time", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                        Text("3.8 mins", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF38BDF8))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { 0.85f },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF38BDF8),
                        trackColor = Color(0xFF334155)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Customer Net Promoter Score (NPS)", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                        Text("74.2 (Excellent)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFF59E0B))
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Demand Distribution by Vehicle Category", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(10.dp))

                    listOf(
                        "Mini Truck (7ft Tata Ace)" to (42 to Color(0xFFF59E0B)),
                        "Courier Bike" to (28 to Color(0xFF38BDF8)),
                        "Cargo Auto 3-Wheeler" to (18 to Color(0xFF10B981)),
                        "Heavy Truck (14ft+)" to (12 to Color(0xFFA855F7))
                    ).forEach { (label, data) ->
                        val (pct, color) = data
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(label, fontSize = 12.sp, color = Color(0xFFCBD5E1))
                            Text("$pct%", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = color)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        androidx.compose.material3.LinearProgressIndicator(
                            progress = { pct / 100f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = color,
                            trackColor = Color(0xFF334155)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Dispatch & Geofence Settings", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Search Radius", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        Text("3.0 km (Auto-expanding to 5.0 km)", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Platform Commission", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        Text("15.0% (Driver gets 85%)", fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Surge Pricing Multiplier", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        Text("1.0x (Standard - No Surge)", fontSize = 12.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
