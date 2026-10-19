package ir.linovpn.app.ui.screens.servers

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ir.linovpn.app.R
import ir.linovpn.app.ui.theme.LinoDarkNavy
import ir.linovpn.app.ui.theme.LinoPrimaryCyan
import ir.linovpn.app.ui.theme.LinoSurface
import ir.linovpn.app.ui.theme.LinoSurfaceBorder
import ir.linovpn.app.ui.theme.LinoTextMuted
import ir.linovpn.app.ui.theme.LinoTextPrimary

@Composable
fun AddServerScreen(
    navController: NavController,
    serversViewModel: ServersViewModel
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var inputLink by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LinoDarkNavy)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = LinoTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.add_server),
                color = LinoTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "لینک کانفیگ یا سابسکریپشن را وارد کنید:",
            color = LinoTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "پشتیبانی از VLESS (Reality), VMess, Trojan, Shadowsocks, TUIC, Hysteria2, WireGuard",
            color = LinoTextMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = inputLink,
            onValueChange = { inputLink = it },
            placeholder = { Text("vless://... یا vmess://...", color = LinoTextMuted) },
            trailingIcon = {
                IconButton(onClick = {
                    val clip = clipboardManager.getText()?.text
                    if (!clip.isNullOrBlank()) {
                        inputLink = clip
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Paste",
                        tint = LinoPrimaryCyan
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LinoSurface,
                unfocusedContainerColor = LinoSurface,
                focusedBorderColor = LinoPrimaryCyan,
                unfocusedBorderColor = LinoSurfaceBorder,
                focusedTextColor = LinoTextPrimary,
                unfocusedTextColor = LinoTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (inputLink.isNotBlank()) {
                    serversViewModel.importFromText(inputLink) { count ->
                        if (count > 0) {
                            Toast.makeText(context, "$count سرور با موفقیت افزوده شد", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } else {
                            Toast.makeText(context, "لینک نامعتبر است", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "لطفاً لینکی وارد کنید", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LinoPrimaryCyan)
        ) {
            Text(
                text = "افزودن و ذخیره",
                color = LinoDarkNavy,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
