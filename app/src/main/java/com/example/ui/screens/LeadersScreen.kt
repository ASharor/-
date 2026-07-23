package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.CustomCard
import com.example.ui.components.CustomTopBar
import com.example.ui.components.ProfileDialog
import com.example.utils.DateUtils
import com.example.utils.LanguageManager
import com.example.viewmodel.LeadersViewModel

@Composable
fun LeadersScreen(
    leadersViewModel: LeadersViewModel,
    isPersian: Boolean
) {
    val leaderboard by leadersViewModel.leaderboard.collectAsState()
    val selectedProfile by leadersViewModel.selectedProfile.collectAsState()

    // Profile Dialog when user clicks a row
    selectedProfile?.let { profile ->
        ProfileDialog(
            profile = profile,
            isOnline = true,
            lastSeen = System.currentTimeMillis(),
            totalStudyMinutes = 320,
            todayStudyMinutes = 90,
            isPersian = isPersian,
            onDismiss = { leadersViewModel.clearSelectedProfile() }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CustomTopBar(title = LanguageManager.getString("leaders", isPersian))

        Column(modifier = Modifier.padding(16.dp)) {
            CustomCard(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = LanguageManager.getString("top_users", isPersian),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(leaderboard) { entry ->
                    Card(
                        onClick = { leadersViewModel.selectUserForProfile(entry.username) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Medal / Rank
                            when (entry.rank) {
                                1 -> Icon(
                                    painter = painterResource(id = R.drawable.ic_gold),
                                    contentDescription = "Gold",
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(28.dp)
                                )
                                2 -> Icon(
                                    painter = painterResource(id = R.drawable.ic_silver),
                                    contentDescription = "Silver",
                                    tint = Color(0xFFC0C0C0),
                                    modifier = Modifier.size(28.dp)
                                )
                                3 -> Icon(
                                    painter = painterResource(id = R.drawable.ic_bronze),
                                    contentDescription = "Bronze",
                                    tint = Color(0xFFCD7F32),
                                    modifier = Modifier.size(28.dp)
                                )
                                else -> Text(
                                    text = "#${entry.rank}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "@${entry.username}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (entry.username.equals("ARMIN", ignoreCase = true) || entry.username.equals("DIYAKO", ignoreCase = true)) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_verified),
                                            contentDescription = "Verified",
                                            tint = Color(0xFF2196F3),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "مطالعه امروز: ${DateUtils.formatDuration(entry.todayStudy, isPersian)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = DateUtils.formatDuration(entry.totalStudy, isPersian),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
