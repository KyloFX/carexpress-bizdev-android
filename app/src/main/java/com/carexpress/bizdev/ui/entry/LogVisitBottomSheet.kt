package com.carexpress.bizdev.ui.entry

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.carexpress.bizdev.data.local.Segment
import com.carexpress.bizdev.data.local.Stage
import com.carexpress.bizdev.data.local.VisitType
import com.carexpress.bizdev.data.seed.TargetDirectory
import com.carexpress.bizdev.ui.theme.AccentTeal
import com.carexpress.bizdev.ui.theme.BrandTeal
import com.carexpress.bizdev.ui.theme.Divider
import com.carexpress.bizdev.ui.theme.InkSecondary
import com.carexpress.bizdev.ui.theme.MintSurface
import com.carexpress.bizdev.ui.theme.colors
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val stampFmt = DateTimeFormatter.ofPattern("MMM d, yyyy • hh:mm a")
private val dateFmt = DateTimeFormatter.ofPattern("EEE, d MMM yyyy")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LogVisitBottomSheet(
    vm: VisitEditorViewModel,
    onDismiss: () -> Unit,
) {
    val form by vm.form.collectAsState()
    val orgs by vm.organisationSuggestions.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showFollowUpPicker by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    fun close() = scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        sheetMaxWidth = 840.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MintSurface,
    ) {
        Column(Modifier.fillMaxWidth().focusable().imePadding()) {
            // Header
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(if (form.isEdit) "Edit Visit" else "Log Field Visit", style = MaterialTheme.typography.titleLarge, color = BrandTeal)
                    Text(if (form.isEdit) "Update stage, value or notes" else "Captured offline • syncs to your KPIs instantly",
                        style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                }
                if (form.isEdit) IconButton(onClick = { confirmDelete = true }) {
                    Icon(Icons.Outlined.Delete, "Delete visit", tint = Color(0xFF991B1B))
                }
            }
            Spacer(Modifier.height(12.dp))
            // Auto timestamp chip
            Row(
                Modifier.padding(horizontal = 20.dp).clip(RoundedCornerShape(50)).background(Color.White)
                    .border(1.dp, Divider, RoundedCornerShape(50)).clickable { showDatePicker = true }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.CalendarMonth, null, tint = AccentTeal, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    (if (form.timestampOverridden) "Visit: " else "Auto-Stamped: ") + form.timestamp.format(stampFmt),
                    style = MaterialTheme.typography.labelLarge, color = BrandTeal,
                )
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Outlined.Edit, "Change visit date", tint = InkSecondary, modifier = Modifier.size(14.dp))
            }

            // Scrollable form
            Column(
                Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                // 1. Organisation autocomplete
                OrgAutocomplete(form.organisation, orgs, form.showErrors && form.orgError, vm::setOrganisation)

                // 2. Segment
                Field("Segment", error = form.showErrors && form.segmentError) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Segment.entries.forEach { s ->
                            SelectPill(s.label, form.segment == s, s.colors.fg) { vm.update { copy(segment = s) } }
                        }
                    }
                }

                // 3. Key person + role
                Field("Key Person Met & Role") {
                    AppTextField(form.keyPerson, { v -> vm.update { copy(keyPerson = v) } }, "Name (e.g. Mrs. L. Mapfumo)", Icons.Outlined.Person,
                        capitalization = KeyboardCapitalization.Words)
                    Spacer(Modifier.height(8.dp))
                    AppTextField(form.role, { v -> vm.update { copy(role = v) } }, "Role / title", null, capitalization = KeyboardCapitalization.Words)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TargetDirectory.roleChips.forEach { r -> SelectPill(r, form.role == r) { vm.update { copy(role = r) } } }
                    }
                }

                // 4. Visit type
                Field("Visit Type") {
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        VisitType.entries.forEachIndexed { i, t ->
                            SegmentedButton(
                                selected = form.visitType == t,
                                onClick = { vm.update { copy(visitType = t) } },
                                shape = SegmentedButtonDefaults.itemShape(i, VisitType.entries.size),
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = BrandTeal, activeContentColor = Color.White,
                                    inactiveContainerColor = Color.White, activeBorderColor = BrandTeal, inactiveBorderColor = Divider,
                                ),
                                icon = {},
                                label = { Text(t.label, style = MaterialTheme.typography.labelSmall, maxLines = 1) },
                            )
                        }
                    }
                }

                // 5. Stage
                Field("Conversation Outcome / Stage") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Stage.entries.forEach { st ->
                            val sel = form.stage == st
                            val bg by animateColorAsState(if (sel) st.colors.bg else Color.White, label = "stage")
                            Text(
                                st.label, style = MaterialTheme.typography.labelLarge, color = st.colors.fg,
                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.clip(RoundedCornerShape(50)).background(bg)
                                    .border(if (sel) 2.dp else 1.dp, if (sel) st.colors.fg else Divider, RoundedCornerShape(50))
                                    .clickable { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); vm.update { copy(stage = st) } }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                            )
                        }
                    }
                }

                // 6. Scope & value
                Field("Scope & Value") {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppTextField(form.coveredLives, { v -> vm.update { copy(coveredLives = v.filter(Char::isDigit).take(7)) } },
                            "Covered lives", Icons.Outlined.Groups, KeyboardType.Number, Modifier.weight(1f))
                        AppTextField(form.monthlyValue, { v -> vm.update { copy(monthlyValue = sanitizeMoney(v)) } },
                            "Monthly $", Icons.Outlined.AttachMoney, KeyboardType.Decimal, Modifier.weight(1f))
                    }
                }

                // 7. Lead score
                Field("Lead Score") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        (1..5).forEach { n ->
                            IconButton(onClick = { haptics.performHapticFeedback(HapticFeedbackType.LongPress); vm.update { copy(leadScore = n) } }) {
                                Icon(if (n <= form.leadScore) Icons.Rounded.Star else Icons.Rounded.StarOutline, "Score $n",
                                    tint = if (n <= form.leadScore) (if (form.leadScore >= 4) BrandTeal else Color(0xFFD97706)) else InkSecondary,
                                    modifier = Modifier.size(32.dp))
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(when (form.leadScore) { 5 -> "Hot"; 4 -> "Warm"; 3 -> "Neutral"; 2 -> "Cool"; else -> "Cold" },
                            style = MaterialTheme.typography.labelLarge, color = InkSecondary)
                    }
                }

                // 8. Notes
                Field("Short Conversation Notes") {
                    AppTextField(form.notes, { v -> vm.update { copy(notes = v) } }, "Key takeaways, objections, decision makers…",
                        null, singleLine = false, minLines = 3, capitalization = KeyboardCapitalization.Sentences)
                }

                // 9. Next action + follow-up
                Field("Next Action Step & Follow-Up") {
                    AppTextField(form.nextAction, { v -> vm.update { copy(nextAction = v) } }, "e.g. Send revised tariff", null,
                        capitalization = KeyboardCapitalization.Sentences)
                    Spacer(Modifier.height(8.dp))
                    val today = LocalDate.now()
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("+2 Days" to today.plusDays(2), "+1 Week" to today.plusWeeks(1), "Next Month" to today.plusMonths(1))
                            .forEach { (label, d) -> SelectPill(label, form.followUpDate == d) { vm.update { copy(followUpDate = d) } } }
                        SelectPill("Custom Date", false) { showFollowUpPicker = true }
                    }
                    form.followUpDate?.let {
                        Spacer(Modifier.height(6.dp))
                        Text("Follow-up: ${it.format(dateFmt)}", style = MaterialTheme.typography.labelLarge, color = AccentTeal)
                    }
                }
            }

            // Sticky CTA
            HorizontalDivider(color = Divider)
            Box(Modifier.fillMaxWidth().background(Color.White).navigationBarsPadding().padding(16.dp)) {
                Button(
                    onClick = { vm.save { close() } },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandTeal),
                ) { Text(if (form.isEdit) "Update Visit" else "Save Visit", style = MaterialTheme.typography.titleMedium) }
            }
        }
    }

    // --- Pickers ---
    if (showDatePicker) {
        val st = rememberDatePickerState(form.timestamp.toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    st.selectedDateMillis?.let { ms ->
                        val d = Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate()
                        vm.setTimestamp(d.atTime(form.timestamp.toLocalTime()))
                    }
                    showDatePicker = false; showTimePicker = true
                }) { Text("Next: Time") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) { DatePicker(st) }
    }
    if (showTimePicker) {
        val tp = rememberTimePickerState(form.timestamp.hour, form.timestamp.minute)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    vm.setTimestamp(form.timestamp.withHour(tp.hour).withMinute(tp.minute)); showTimePicker = false
                }) { Text("Set") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Keep") } },
            title = { Text("Meeting time") },
            text = { TimePicker(tp) },
        )
    }
    if (showFollowUpPicker) {
        val st = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showFollowUpPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    st.selectedDateMillis?.let { ms -> vm.update { copy(followUpDate = Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate()) } }
                    showFollowUpPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showFollowUpPicker = false }) { Text("Cancel") } },
        ) { DatePicker(st) }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete this visit?") },
            text = { Text("${form.organisation} will be removed and KPIs recalculated.") },
            confirmButton = { TextButton(onClick = { confirmDelete = false; vm.delete { close() } }) { Text("Delete", color = Color(0xFF991B1B)) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    }
}

private fun sanitizeMoney(v: String): String {
    val cleaned = v.filter { it.isDigit() || it == '.' }
    val dot = cleaned.indexOf('.')
    return if (dot < 0) cleaned.take(9) else cleaned.substring(0, dot + 1) + cleaned.substring(dot + 1).replace(".", "").take(2)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrgAutocomplete(value: String, options: List<String>, error: Boolean, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val filtered = remember(value, options) {
        if (value.isBlank()) options else options.filter { it.contains(value, true) && !it.equals(value, true) }
    }
    Field("Target Organisation", error = error) {
        ExposedDropdownMenuBox(expanded = expanded && filtered.isNotEmpty(), onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = value,
                onValueChange = { onChange(it); expanded = true },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable),
                placeholder = { Text("Search or type a new organisation") },
                leadingIcon = { Icon(Icons.Outlined.Business, null, tint = AccentTeal) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                singleLine = true,
                isError = error,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                colors = fieldColors(),
            )
            ExposedDropdownMenu(expanded = expanded && filtered.isNotEmpty(), onDismissRequest = { expanded = false }) {
                filtered.take(8).forEach { org ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(org, style = MaterialTheme.typography.bodyMedium)
                                TargetDirectory.organisations[org]?.let {
                                    Text(it.label, style = MaterialTheme.typography.labelSmall, color = it.colors.fg)
                                }
                            }
                        },
                        onClick = { onChange(org); expanded = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun Field(label: String, error: Boolean = false, content: @Composable () -> Unit) {
    Column {
        Text(label.uppercase() + if (error) "  • required" else "", style = MaterialTheme.typography.labelSmall,
            color = if (error) Color(0xFF991B1B) else AccentTeal, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun SelectPill(text: String, selected: Boolean, dot: Color? = null, onClick: () -> Unit) {
    val bg by animateColorAsState(if (selected) BrandTeal else Color.White, label = "pill")
    Row(
        Modifier.clip(RoundedCornerShape(50)).background(bg)
            .border(1.dp, if (selected) BrandTeal else Divider, RoundedCornerShape(50))
            .clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (dot != null) {
            Box(Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(if (selected) Color.White else dot))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, color = if (selected) Color.White else BrandTeal)
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AccentTeal, unfocusedBorderColor = Divider,
    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
)

@Composable
private fun AppTextField(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector?,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier.fillMaxWidth(),
    singleLine: Boolean = true,
    minLines: Int = 1,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier,
        placeholder = { Text(placeholder, maxLines = 1) },
        leadingIcon = icon?.let { { Icon(it, null, tint = AccentTeal) } },
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(14.dp),
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, capitalization = capitalization),
        colors = fieldColors(),
    )
}
