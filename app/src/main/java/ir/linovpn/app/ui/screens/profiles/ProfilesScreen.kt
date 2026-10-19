package ir.linovpn.app.ui.screens.profiles

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ir.linovpn.app.R
import ir.linovpn.app.domain.model.Profile
import ir.linovpn.app.ui.theme.LinoDarkNavy
import ir.linovpn.app.ui.theme.LinoErrorRed
import ir.linovpn.app.ui.theme.LinoPrimaryCyan
import ir.linovpn.app.ui.theme.LinoSurface
import ir.linovpn.app.ui.theme.LinoSurfaceBorder
import ir.linovpn.app.ui.theme.LinoSurfaceVariant
import ir.linovpn.app.ui.theme.LinoTextMuted
import ir.linovpn.app.ui.theme.LinoTextPrimary
import ir.linovpn.app.ui.theme.LinoTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfilesScreen(
    navController: NavController,
    viewModel: ProfilesViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.profiles_title),
                    color = LinoTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = { viewModel.updateAll() },
                    enabled = !state.isUpdatingAll && state.profiles.isNotEmpty()
                ) {
                    if (state.isUpdatingAll) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = LinoPrimaryCyan
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = stringResource(R.string.update_all),
                            tint = LinoPrimaryCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.profiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "هیچ اشتراکی ثبت نشده است.\nبا دکمه + آدرس اشتراک خود را اضافه کنید.",
                        color = LinoTextSecondary,
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.profiles, key = { it.id }) { profile ->
                        val isUpdating = state.updatingProfileIds.contains(profile.id)
                        ProfileCard(
                            profile = profile,
                            isUpdating = isUpdating,
                            onUpdate = { viewModel.updateProfile(profile) },
                            onDelete = { viewModel.deleteProfile(profile) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.openAddDialog() },
            containerColor = LinoPrimaryCyan,
            contentColor = LinoDarkNavy,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Profile")
        }

        if (state.showAddDialog) {
            var name by remember { mutableStateOf("") }
            var url by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { viewModel.closeAddDialog() },
                containerColor = LinoSurface,
                title = {
                    Text(
                        text = stringResource(R.string.add_profile),
                        color = LinoTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(stringResource(R.string.profile_name), color = LinoTextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = LinoSurfaceVariant,
                                unfocusedContainerColor = LinoSurfaceVariant,
                                focusedBorderColor = LinoPrimaryCyan,
                                unfocusedBorderColor = LinoSurfaceBorder,
                                focusedTextColor = LinoTextPrimary,
                                unfocusedTextColor = LinoTextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = url,
                            onValueChange = { url = it },
                            label = { Text(stringResource(R.string.profile_url), color = LinoTextMuted) },
                            trailingIcon = {
                                IconButton(onClick = {
                                    val clip = clipboardManager.getText()?.text
                                    if (!clip.isNullOrBlank()) {
                                        url = clip
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = "Paste",
                                        tint = LinoPrimaryCyan
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = LinoSurfaceVariant,
                                unfocusedContainerColor = LinoSurfaceVariant,
                                focusedBorderColor = LinoPrimaryCyan,
                                unfocusedBorderColor = LinoSurfaceBorder,
                                focusedTextColor = LinoTextPrimary,
                                unfocusedTextColor = LinoTextPrimary
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (name.isNotBlank() && url.isNotBlank()) {
                                viewModel.addProfile(name, url)
                            } else {
                                Toast.makeText(context, "نام و آدرس الزامی است", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LinoPrimaryCyan)
                    ) {
                        Text(text = "ذخیره و دریافت", color = LinoDarkNavy, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.closeAddDialog() }) {
                        Text(text = "انصراف", color = LinoTextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileCard(
    profile: Profile,
    isUpdating: Boolean,
    onUpdate: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }
    val lastUpdatedStr = if (profile.lastUpdated > 0) {
        dateFormat.format(Date(profile.lastUpdated))
    } else {
        "بروزرسانی نشده"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LinoSurface)
            .border(1.dp, LinoSurfaceBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    color = LinoTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${profile.serverCount} سرور فعال  •  $lastUpdatedStr",
                    color = LinoTextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = profile.subscriptionUrl,
                    color = LinoTextMuted,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onUpdate, enabled = !isUpdating) {
                    if (isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = LinoPrimaryCyan
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Update",
                            tint = LinoPrimaryCyan
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = LinoErrorRed
                    )
                }
            }
        }
    }
}
