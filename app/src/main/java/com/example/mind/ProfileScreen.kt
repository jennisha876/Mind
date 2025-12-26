package com.example.mind

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(darkTheme: Boolean, onToggleTheme: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Summary", "Settings")

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab row
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        // Content based on selected tab
        when (selectedTab) {
            0 -> ProfileSummaryTab()
            1 -> ProfileSettingsTab(darkTheme, onToggleTheme)
        }
    }
}

@Composable
fun ProfileSummaryTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Profile Summary",
            style = MaterialTheme.typography.headlineMedium
        )

        // Heart rate placeholder
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Heart Rate Chart (Placeholder)", style = MaterialTheme.typography.bodyLarge)
            }
        }

        // Mood chart placeholder
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Mood Pie Chart (Placeholder)", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun ProfileSettingsTab(darkTheme: Boolean, onToggleTheme: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Settings",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dark mode toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dark Mode", modifier = Modifier.weight(1f))
                Switch(
                    checked = darkTheme,
                    onCheckedChange = { onToggleTheme() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Test crash button
            Button(
                onClick = { throw RuntimeException("Test Crash") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Crash")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    ProfileScreen(darkTheme = true, onToggleTheme = {})
}
