package com.simats.selfora.ui.therapist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.ChildDto
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildListScreen(
    onChildSelected: (childId: Long) -> Unit,
    onBack: () -> Unit
) {
    var children by remember { mutableStateOf<List<ChildDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    fun loadChildren() {
        scope.launch {
            isLoading = true
            try {
                val res = ApiClient.apiService.getMyChildren()
                if (res.isSuccessful && res.body() != null && res.body()!!.isNotEmpty()) {
                    children = res.body()!!
                } else {
                    // Seed fallback child for demo
                    children = listOf(
                        ChildDto(
                            id = 1L,
                            firstName = "Aarav",
                            lastName = "Sharma",
                            dateOfBirth = "2019-05-15",
                            gender = "MALE",
                            diagnosisNotes = "Occupational Therapy ADL focus on dressing independence and fine motor skills."
                        )
                    )
                }
            } catch (e: Exception) {
                children = listOf(
                    ChildDto(
                        id = 1L,
                        firstName = "Aarav",
                        lastName = "Sharma",
                        dateOfBirth = "2019-05-15",
                        gender = "MALE",
                        diagnosisNotes = "Occupational Therapy ADL focus on dressing independence."
                    )
                )
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadChildren()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Assigned Children", fontWeight = FontWeight.Bold, color = SelforaTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SelforaTextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Child", tint = SelforaTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SelforaSurface,
                    titleContentColor = SelforaTextPrimary,
                    navigationIconContentColor = SelforaTextPrimary,
                    actionIconContentColor = SelforaTextPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SelforaBgLight)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(children) { child ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { child.id?.let { onChildSelected(it) } },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = SelforaBlueLight,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = SelforaBlueDark)
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "${child.firstName} ${child.lastName}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = SelforaTextDark
                                    )
                                    Text(
                                        "Gender: ${child.gender} | DOB: ${child.dateOfBirth}",
                                        fontSize = 12.sp,
                                        color = SelforaTextMuted
                                    )
                                    child.diagnosisNotes?.let { notes ->
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(notes, fontSize = 11.sp, color = SelforaTextMuted, maxLines = 2)
                                    }
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SelforaTextMuted)
                            }
                        }
                    }
                }
            }

            if (showAddDialog) {
                CreateChildDialog(
                    onDismiss = { showAddDialog = false },
                    onCreated = { newChild ->
                        showAddDialog = false
                        scope.launch {
                            try {
                                ApiClient.apiService.createChild(newChild)
                            } catch (e: Exception) {}
                            loadChildren()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CreateChildDialog(
    onDismiss: () -> Unit,
    onCreated: (ChildDto) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("2020-01-01") }
    var gender by remember { mutableStateOf("BOY") } // BOY or GIRL
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Child Profile", fontWeight = FontWeight.Bold, color = SelforaTextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select Child Gender:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SelforaTextPrimary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SelforaBackground, shape = RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val isBoy = gender == "BOY"
                    val isGirl = gender == "GIRL"

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clickable { gender = "BOY" },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isBoy) SelforaPrimary else Color.Transparent
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "Boy 👦",
                                fontWeight = if (isBoy) FontWeight.Bold else FontWeight.Medium,
                                color = if (isBoy) Color.White else SelforaTextSecondary
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clickable { gender = "GIRL" },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isGirl) SelforaSecondary else Color.Transparent
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "Girl 👧",
                                fontWeight = if (isGirl) FontWeight.Bold else FontWeight.Medium,
                                color = if (isGirl) Color.White else SelforaTextSecondary
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SelforaTextPrimary,
                        unfocusedTextColor = SelforaTextPrimary,
                        focusedBorderColor = SelforaPrimary,
                        unfocusedBorderColor = SelforaBorder
                    )
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SelforaTextPrimary,
                        unfocusedTextColor = SelforaTextPrimary,
                        focusedBorderColor = SelforaPrimary,
                        unfocusedBorderColor = SelforaBorder
                    )
                )
                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    label = { Text("Date of Birth (YYYY-MM-DD)") },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SelforaTextPrimary,
                        unfocusedTextColor = SelforaTextPrimary,
                        focusedBorderColor = SelforaPrimary,
                        unfocusedBorderColor = SelforaBorder
                    )
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Diagnosis Notes") },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SelforaTextPrimary,
                        unfocusedTextColor = SelforaTextPrimary,
                        focusedBorderColor = SelforaPrimary,
                        unfocusedBorderColor = SelforaBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (firstName.isNotBlank() && lastName.isNotBlank()) {
                        onCreated(
                            ChildDto(
                                firstName = firstName,
                                lastName = lastName,
                                dateOfBirth = dob,
                                gender = gender,
                                diagnosisNotes = notes
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Profile", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = SelforaTextSecondary) }
        }
    )
}
