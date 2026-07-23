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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun GroupsScreen(
    groupViewModel: GroupViewModel,
    username: String,
    isPersian: Boolean,
    onSelectGroup: (Group) -> Unit
) {
    val groups by groupViewModel.groups.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }

    LaunchedEffect(username) {
        groupViewModel.loadGroups(username)
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(LanguageManager.getString("create_group", isPersian)) },
            text = {
                Column {
                    CustomTextField(
                        value = newGroupName,
                        onValueChange = { newGroupName = it },
                        label = LanguageManager.getString("group_name", isPersian)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newGroupName.isNotBlank()) {
                        groupViewModel.createGroup(newGroupName, username, null)
                        newGroupName = ""
                        showCreateDialog = false
                    }
                }) {
                    Text(LanguageManager.getString("confirm", isPersian))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text(LanguageManager.getString("cancel", isPersian))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CustomTopBar(
            title = LanguageManager.getString("groups", isPersian),
            actions = {
                CustomButton(
                    onClick = { showCreateDialog = true },
                    height = 38.dp
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_groups),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(LanguageManager.getString("create_group", isPersian), style = MaterialTheme.typography.bodySmall)
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (groups.isEmpty()) {
                item {
                    CustomCard {
                        Text(
                            text = if (isPersian) "شما هنوز عضو هیچ گروهی نیستید. یک گروه جدید ایجاد کنید!"
                            else "You are not a member of any group. Create a new one!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(groups) { group ->
                    Card(
                        onClick = {
                            groupViewModel.selectGroup(group)
                            onSelectGroup(group)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = group.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${group.members.size} ${LanguageManager.getString("group_members", isPersian)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "مالک: @${group.owner}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                TextButton(
                                    onClick = { groupViewModel.leaveGroup(group.id, username) }
                                ) {
                                    Text(
                                        text = LanguageManager.getString("leave_group", isPersian),
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
