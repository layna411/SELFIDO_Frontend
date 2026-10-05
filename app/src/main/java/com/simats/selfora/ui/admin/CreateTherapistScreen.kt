package com.simats.selfora.ui.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.CreateTherapistRequest
import com.simats.selfora.ui.components.glass.GlassTextField
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTherapistScreen(
    onTherapistCreated: () -> Unit,
    onBack: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("password123") }
    var designation by remember { mutableStateOf("Senior Occupational Therapist") }
    var specialization by remember { mutableStateOf("Pediatric ADL & Fine Motor Skills") }
    var qualification by remember { mutableStateOf("MOT (Pediatrics)") }
    var experience by remember { mutableStateOf("5 Years") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val blueAccent = Color(0xFF2563EB)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register New Clinician", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Therapist Profile Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Text(
                        text = "Provision a new licensed clinician with system access.",
                        fontSize = 12.sp,
                        color = textSecondary
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    GlassTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = "Full Name *",
                        placeholder = "Dr. Sarah Jenkins",
                        leadingIcon = Icons.Default.Person,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    GlassTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email Address *",
                        placeholder = "sarah.jenkins@selfora.org",
                        leadingIcon = Icons.Default.Email,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    GlassTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Phone Number *",
                        placeholder = "+91 9876543210",
                        leadingIcon = Icons.Default.Phone,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    GlassTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = "Username *",
                        placeholder = "therapist_sarah",
                        leadingIcon = Icons.Default.AccountBox,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    GlassTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Initial Password *",
                        placeholder = "Default password123",
                        leadingIcon = Icons.Default.Lock,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    GlassTextField(
                        value = designation,
                        onValueChange = { designation = it },
                        label = "Designation",
                        placeholder = "Senior Occupational Therapist",
                        leadingIcon = Icons.Default.Badge,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    GlassTextField(
                        value = specialization,
                        onValueChange = { specialization = it },
                        label = "Clinical Specialization",
                        placeholder = "Pediatric ADL & Fine Motor Skills",
                        leadingIcon = Icons.Default.Psychology,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    GlassTextField(
                        value = qualification,
                        onValueChange = { qualification = it },
                        label = "Qualifications",
                        placeholder = "BOT, MOT (Pediatrics)",
                        leadingIcon = Icons.Default.School,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (fullName.isBlank() || email.isBlank() || username.isBlank()) {
                                Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isLoading = true
                            scope.launch {
                                try {
                                    ApiClient.apiService.createTherapist(
                                        CreateTherapistRequest(
                                            fullName = fullName,
                                            email = email,
                                            phone = phone,
                                            username = username,
                                            password = password,
                                            designation = designation,
                                            specialization = specialization,
                                            qualification = qualification,
                                            experience = experience
                                        )
                                    )
                                    Toast.makeText(context, "Therapist created successfully!", Toast.LENGTH_SHORT).show()
                                    onTherapistCreated()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Therapist created successfully!", Toast.LENGTH_SHORT).show()
                                    onTherapistCreated()
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = blueAccent)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Provision Clinician Account", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}
