package com.v2ray.ang.lino

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.v2ray.ang.R
import com.v2ray.ang.util.Utils

@Composable
fun LinoOnboardingDialog(
    onSubmit: (String) -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onSkip,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isDark = com.v2ray.ang.ui.compose.LocalDarkTheme.current
                Image(
                    painter = painterResource(
                        if (isDark) R.drawable.ic_lino_logo_dark else R.drawable.ic_lino_logo_light
                    ),
                    contentDescription = stringResource(R.string.app_name),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .height(68.dp)
                        .padding(top = 4.dp)
                )
                Text(
                    text = stringResource(R.string.lino_onboarding_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.lino_onboarding_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = input,
                    onValueChange = {
                        input = it
                        errorMsg = null
                    },
                    label = { Text(stringResource(R.string.lino_onboarding_input_label)) },
                    placeholder = { Text(stringResource(R.string.lino_onboarding_input_hint)) },
                    isError = errorMsg != null,
                    supportingText = errorMsg?.let { msg -> { Text(msg) } },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = {
                        val clip = Utils.getClipboard(context).trim()
                        if (clip.isNotEmpty()) {
                            input = clip
                            errorMsg = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.lino_paste_clipboard))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = input.trim()
                    if (trimmed.isEmpty()) {
                        errorMsg = context.getString(R.string.lino_onboarding_empty_error)
                    } else {
                        onSubmit(trimmed)
                    }
                }
            ) {
                Text(stringResource(R.string.lino_onboarding_activate))
            }
        },
        dismissButton = {
            TextButton(onClick = onSkip) {
                Text(stringResource(R.string.lino_skip))
            }
        }
    )
}
