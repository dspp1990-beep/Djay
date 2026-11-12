package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.ui.theme.Air
import com.example.ui.theme.CheckCircle
import androidx.compose.material.icons.filled.Close
import com.example.ui.theme.Grass
import com.example.ui.theme.Speed
import com.example.ui.theme.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubmainValveEntity
import com.example.data.model.ValveState
import com.example.ui.theme.ValveActiveGreen
import com.example.ui.theme.ValveActiveGreenBg
import com.example.ui.theme.ValveAlertRed
import com.example.ui.theme.ValveAlertRedBg
import com.example.ui.theme.ValveClosedGray
import com.example.ui.theme.ValveFlushingBlue
import com.example.ui.theme.ValveFlushingBlueBg
import com.example.ui.theme.ValveScheduledAmber
import com.example.ui.theme.ValveScheduledAmberBg

@Composable
fun ValveCard(
    valve: SubmainValveEntity,
    onToggleControlValve: (Boolean) -> Unit,
    onToggleFlushValve: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isIrrigating = valve.isControlValveOpen && !valve.isFlushValveOpen
    val isFlushing = valve.isFlushValveOpen

    val borderColor by animateColorAsState(
        targetValue = when {
            valve.hasAlert -> ValveAlertRed
            isIrrigating -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            isFlushing -> ValveFlushingBlue.copy(alpha = 0.5f)
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        },
        label = "border"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("valve_card_${valve.tag}")
            .border(1.dp, borderColor, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Icon Container + Tag + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Icon in rounded-xl container (Directly from Professional Polish HTML: bg-[#EADDFF] or bg-[#F3EDF7])
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                when {
                                    valve.hasAlert -> ValveAlertRed.copy(alpha = 0.15f)
                                    isIrrigating -> MaterialTheme.colorScheme.primaryContainer
                                    isFlushing -> ValveFlushingBlueBg
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isIrrigating) Icons.Default.WaterDrop else Icons.Default.Speed,
                            contentDescription = null,
                            tint = when {
                                valve.hasAlert -> ValveAlertRed
                                isIrrigating -> MaterialTheme.colorScheme.primary
                                isFlushing -> ValveFlushingBlue
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = valve.tag,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 17.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(50))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Shift ${valve.shiftNumber}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = "${valve.zoneName} • ${valve.pumpHouseId} • ${valve.systemId} • GPS: %.4f°N, %.4f°E".format(valve.latitude, valve.longitude),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status label/pill (Professional Polish HTML: text-[10px] font-bold text-[#006D3B])
                ValveStatusBadge(
                    state = when {
                        valve.hasAlert -> ValveState.FAULT_ALERT
                        isIrrigating -> ValveState.OPEN_IRRIGATING
                        isFlushing -> ValveState.FLUSHING
                        else -> ValveState.CLOSED
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Submain Specifications Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "AREA",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${valve.areaHa} ha",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Column {
                    Text(
                        text = "DESIGN FLOW",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${valve.designFlowLps} lps",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                Column {
                    Text(
                        text = "ACTUAL FLOW",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${String.format("%.2f", valve.actualFlowLps)} lps",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isIrrigating) ValveActiveGreen else MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
                Column {
                    Text(
                        text = "LENGTH",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${valve.submainLengthM.toInt()}m",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Live Sensor Telemetry on this submain
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Pressure",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${valve.linePressureBar} bar",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Grass,
                        contentDescription = "Soil Moisture",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Soil: ${valve.soilMoisture20cmPct}%",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Air,
                        contentDescription = "System",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Shift ${valve.shiftNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Valve Switches Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Control Valve Switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Switch(
                        checked = valve.isControlValveOpen,
                        onCheckedChange = onToggleControlValve,
                        modifier = Modifier.testTag("switch_control_${valve.tag}"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ValveActiveGreen,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Control Valve",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (valve.isControlValveOpen) "OPEN (Irrigating)" else "CLOSED",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = if (valve.isControlValveOpen) ValveActiveGreen else ValveClosedGray
                        )
                    }
                }

                // Flush Valve Switch
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = valve.isFlushValveOpen,
                        onCheckedChange = onToggleFlushValve,
                        modifier = Modifier.testTag("switch_flush_${valve.tag}"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ValveFlushingBlue,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Flush Valve",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (valve.isFlushValveOpen) "OPEN (Flushing)" else "CLOSED",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = if (valve.isFlushValveOpen) ValveFlushingBlue else ValveClosedGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ValveStatusBadge(state: ValveState) {
    val (bg, fg, label) = when (state) {
        ValveState.OPEN_IRRIGATING -> Triple(ValveActiveGreen.copy(alpha = 0.12f), ValveActiveGreen, "OPEN")
        ValveState.CLOSED -> Triple(MaterialTheme.colorScheme.surfaceVariant, ValveClosedGray, "CLOSED")
        ValveState.FLUSHING -> Triple(ValveFlushingBlueBg, ValveFlushingBlue, "FLUSHING")
        ValveState.SCHEDULED -> Triple(ValveScheduledAmberBg, ValveScheduledAmber, "SCHEDULED")
        ValveState.FAULT_ALERT -> Triple(ValveAlertRed.copy(alpha = 0.12f), ValveAlertRed, "ALERT")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, fg.copy(alpha = 0.35f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(fg)
            )
            Text(
                text = label,
                color = fg,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}
