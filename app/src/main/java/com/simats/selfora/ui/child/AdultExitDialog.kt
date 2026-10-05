package com.simats.selfora.ui.child

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.ui.theme.*

@Composable
fun AdultExitDialog(
    onDismiss: () -> Unit,
    onConfirmExit: () -> Unit
) {
    var pinText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = SelforaPrimary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Supervising Adult Exit", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SelforaTextPrimary)
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Please enter the adult exit PIN (default: 1234) or confirm exit to return to the Adult Interface.",
                    fontSize = 13.sp,
                    color = SelforaTextSecondary,
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = pinText,
                    onValueChange = { if (it.length <= 4) pinText = it },
                    label = { Text("Adult PIN") },
                    placeholder = { Text("1234") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(errorMessage!!, fontSize = 12.sp, color = SelforaError)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pinText.isBlank() || pinText == "1234") {
                        onConfirmExit()
                    } else {
                        errorMessage = "Incorrect PIN. Default is 1234"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Exit Child Mode", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Stay in Child Mode", color = SelforaTextSecondary)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}
