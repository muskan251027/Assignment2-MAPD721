package com.example.assignment2

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import com.example.assignment2.ui.theme.Assignment2Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // FUNCTIONALITY TO GIVE PERMISSIONS FROM THE APP
        val healthConnectClient = HealthConnectClient.getOrCreate(this)

        val PERMISSIONS =
            setOf(
                HealthPermission.getReadPermission(HeartRateRecord::class),
                HealthPermission.getWritePermission(HeartRateRecord::class),
                HealthPermission.getReadPermission(StepsRecord::class),
                HealthPermission.getWritePermission(StepsRecord::class)
            )

        // Create the permissions launcher
        val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()

        val requestPermissions = registerForActivityResult(requestPermissionActivityContract) { granted ->
            if (granted.containsAll(PERMISSIONS)) {
                // Permissions successfully granted
            } else {
                // Lack of required permissions
                runBlocking {
                    launch(Dispatchers.IO) {
                        val granted = healthConnectClient.permissionController.getGrantedPermissions()
                        if (granted.containsAll(PERMISSIONS)) {
                            // Permissions already granted; proceed with inserting or reading data
                        } else {

                        }
                    }
                }
            }
        }

        requestPermissions.launch(PERMISSIONS)

        setContent {
            Assignment2Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HealthConnect()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthConnect(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val heartRate = remember {
        mutableStateOf(TextFieldValue(""))
    }
    val selectedDateTime = remember { mutableStateOf(Calendar.getInstance()) }

    // DUMMY DATA
    val heartRateHistory = remember { mutableStateOf(
        List(18) { Pair("130bpm", "2024-01-12 18:00") }
    ) }


    // Main Interface
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            TextField(
                value = heartRate.value,
                onValueChange = {// Validate heart rate input
                    if (it.text.matches(Regex("\\d*")) && it.text.toIntOrNull() in 1..300) {
                        heartRate.value = it
                    } },
                label = { Text("HeartRate (1-300bpm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(15.dp))
            DateTimePicker(
                selectedDateTime = selectedDateTime.value,
                onDateTimeSelected = { selectedDateTime.value = it }
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Button(
                    onClick = {
                        // SAVING FUNCTIONALITY HERE
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Load")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        // LOADING FUNCTIONALITY HERE
                    },
                    modifier = Modifier.weight(1f),
                    enabled = true
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save")
                }

                Spacer(modifier = Modifier.width(8.dp))

            }

        }

        // HEARTRATE HISTORY SECTION
        Text(
            text = "Heart Rate History"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp) // Optional: Add padding to the content
            ) {
                items(heartRateHistory.value) { heartRateItem ->
                    HeartRateRow(heartRate = heartRateItem.first, dateTime = heartRateItem.second)
                }
            }
        }

        AboutSection()

    }
}

@Composable
private fun HeartRateRow(heartRate: String, dateTime: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = heartRate)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = dateTime)
    }
}

// DATETIME PICKER FUNCTIONALITY
@Composable
private fun DateTimePicker(selectedDateTime: Calendar, onDateTimeSelected: (Calendar) -> Unit) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, monthOfYear, dayOfMonth ->
                val newDateTime = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, monthOfYear)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        newDateTime.apply {
                            set(Calendar.HOUR_OF_DAY, hourOfDay)
                            set(Calendar.MINUTE, minute)
                        }
                        onDateTimeSelected(newDateTime)
                    },
                    selectedDateTime.get(Calendar.HOUR_OF_DAY),
                    selectedDateTime.get(Calendar.MINUTE),
                    false
                ).show()
            },
            selectedDateTime.get(Calendar.YEAR),
            selectedDateTime.get(Calendar.MONTH),
            selectedDateTime.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("DateTime: ${dateFormat.format(selectedDateTime.time)}")
        Spacer(modifier = Modifier.width(5.dp))
        Button(onClick = { showDialog = true }) {
            Text("Pick Datetime")
        }
    }
}

@Composable
private fun AboutSection() {
    Spacer(modifier = Modifier.height(50.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(imageVector = Icons.Default.Person, contentDescription = null)
        Spacer(modifier = Modifier.width(4.dp))
        Text("Student name: Muskan Aggarwal", color = Color.Black)
    }
    Spacer(modifier = Modifier.height(5.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(imageVector = Icons.Default.Edit, contentDescription = null)
        Spacer(modifier = Modifier.width(4.dp))
        Text("Student ID: 301399676", color = Color.Black)
    }
}

@Preview(showBackground = true)
@Composable
fun HealthConnectPreview() {
    Assignment2Theme {
        HealthConnect()
    }
}