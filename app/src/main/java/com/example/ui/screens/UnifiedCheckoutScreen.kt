package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.getTransportModeDetails
import com.example.ui.theme.*
import com.example.viewmodel.GoWeaveUiState
import com.example.viewmodel.GoWeaveViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedCheckoutScreen(
    uiState: GoWeaveUiState,
    viewModel: GoWeaveViewModel,
    onBack: () -> Unit,
    onBookingSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val route = uiState.selectedRoute ?: return
    var selectedPaymentMethod by remember { mutableStateOf("Namma Transit UPI") }
    var isProcessing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Unified Journey Checkout",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TransitNavy
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TransitNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Combined Fare",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "₹${route.totalFare.toInt()}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TransitNavy
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            isProcessing = true
                            viewModel.completeUnifiedCheckout(selectedPaymentMethod) {
                                isProcessing = false
                                onBookingSuccess()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("pay_and_issue_passes_button"),
                        enabled = !isProcessing,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TransitNavy)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Issuing Digital Passes...", color = Color.White, fontWeight = FontWeight.Bold)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = TransitTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pay ₹${route.totalFare.toInt()} & Issue All Passes",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Prototype Security Notice
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = BorderStroke(1.dp, Color(0xFFBBF7D0))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Unified Single Checkout: All operator tickets (BMRCL, BMTC, Namma Yatri) are issued in 1 transaction without extra convenience fees.",
                        fontSize = 11.sp,
                        color = Color(0xFF14532D)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Segment Breakdown Card
            Text(
                text = "Itemized Journey Segments",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TransitNavy
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    route.segments.forEachIndexed { idx, seg ->
                        val (icon, color) = getTransportModeDetails(seg.mode)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = color,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = seg.operatorName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TransitNavy
                                    )
                                    Text(
                                        text = "${seg.fromStation} → ${seg.toStation}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            Text(
                                text = if (seg.fare > 0) "₹${seg.fare.toInt()}" else "Free",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TransitNavy
                            )
                        }

                        if (idx < route.segments.size - 1) {
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }
                    }

                    HorizontalDivider(
                        color = Color(0xFFCBD5E1),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Platform Convenience Fee",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "₹0 (Waived)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Payment Methods
            Text(
                text = "Select Payment Method",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TransitNavy
            )
            Spacer(modifier = Modifier.height(8.dp))

            listOf(
                Triple("Namma Transit UPI", "GPay, PhonePe, Paytm, BHIM", Icons.Default.QrCode),
                Triple("GoWeave Transit Wallet", "Balance: ₹450 (1-Tap Auto Debit)", Icons.Default.AccountBalanceWallet),
                Triple("Debit / Credit Card", "Visa, Mastercard, RuPay", Icons.Default.CreditCard)
            ).forEach { (name, subtitle, icon) ->
                val isSelected = selectedPaymentMethod == name
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { selectedPaymentMethod = name },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFF0FDF4) else Color.White
                    ),
                    border = BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) TransitTeal else Color(0xFFE2E8F0)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedPaymentMethod = name },
                            colors = RadioButtonDefaults.colors(selectedColor = TransitTeal)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = TransitNavy,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TransitNavy
                            )
                            Text(
                                text = subtitle,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cancellation policy statement
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Cancellation & Refund Guarantee",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TransitNavy
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "If an operator experiences major schedule cancellations or delays greater than 20 mins, unboarded legs are 100% refundable instantly.",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
