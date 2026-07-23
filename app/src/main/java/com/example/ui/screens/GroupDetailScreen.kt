package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.models.Group
import com.example.ui.components.CustomButton
import com.example.ui.components.CustomCard
import com.example.ui.components.CustomTextField
import com.example.ui.components.CustomTopBar
import com.example.utils.LanguageManager
import com.example.viewmodel.GroupViewModel

@Composable
fun GroupDetailScreen(
    groupViewModel: GroupViewModel,
    group: Group,
    username: String,
    isPersian: Boolean,
    onBack: () -> Unit
) {
    val memberStatusMap by groupViewModel.memberStatusMap.collectAsState()
    var newMemberUsername by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CustomTopBar(
            title = group.name,
            onBackClick = onBack,
            actions = {
                TextButton(onClick = {
                    groupViewModel.leaveGroup(group.id, username)
                    onBack()
                }) {
                    Text(
                        text = LanguageManager.getString("leave_group", isPersian),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        )

        Column(modifier = Modifier.padding(16.dp)) {
            // Add Member Section
            CustomCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomTextField(
                        value = newMemberUsername,
                        onValueChange = { newMemberUsername = it },
                        label = LanguageManager.getString("add_member", isPersian),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    CustomButton(
                        onClick = {
                            if (newMemberUsername.isNotBlank()) {
                                groupViewModel.addMember(group.id, newMemberUsername, username)
                                newMemberUsername = ""
                            }
                        },
                        height = 50.dp
                    ) {
                        Text(if (isPersian) "افزودن" else "Add")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = LanguageManager.getString("group_members", isPersian),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(group.members) { index, memberName ->
                    val status = memberStatusMap[memberName.lowercase()]
                    val isOnline = status?.isOnline == true
                    val isStudying = status?.isStudying == true

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rank Medals
                            when (index) {
                                0 -> Icon(
                                    painter = painterResource(id = R.drawable.ic_gold),
                                    contentDescription = "Gold",
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(24.dp)
                                )
                                1 -> Icon(
                                    painter = painterResource(id = R.drawable.ic_silver),
                                    contentDescription = "Silver",
                                    tint = Color(0xFFC0C0C0),
                                    modifier = Modifier.size(24.dp)
                                )
                                2 -> Icon(
                                    painter = painterResource(id = R.drawable.ic_bronze),
                                    contentDescription = "Bronze",
                                    tint = Color(0xFFCD7F32),
                                    modifier = Modifier.size(24.dp)
                                )
                                else -> Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "@$memberName",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (memberName.equals("ARMIN", ignoreCase = true) || memberName.equals("DIYAKO", ignoreCase = true)) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_verified),
                                        contentDescription = "Verified",
                                        tint = Color(0xFF2196F3),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Status badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isStudying -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                            isOnline -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                            else -> Color.Gray.copy(alpha = 0.15f)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isStudying -> MaterialTheme.colorScheme.secondary
                                                isOnline -> Color(0xFF4CAF50)
                                                else -> Color.Gray
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when {
                                        isStudying -> LanguageManager.getString("studying", isPersian)
                                        isOnline -> LanguageManager.getString("online", isPersian)
                                        else -> LanguageManager.getString("offline", isPersian)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = when {
                                        isStudying -> MaterialTheme.colorScheme.secondary
                                        isOnline -> Color(0xFF4CAF50)
                                        else -> Color.Gray
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
