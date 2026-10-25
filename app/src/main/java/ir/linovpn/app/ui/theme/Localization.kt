package ir.linovpn.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import ir.linovpn.app.domain.model.AppLanguage
import java.util.Locale

data class Strings(
    val edit: String,
    val done: String,
    val profileImported: String,
    val connected: String,
    val disconnected: String,
    val connecting: String,
    val notConnected: String,
    val tapToConnect: String,
    val sortModePing: String,
    val sortModeName: String,
    val testAllPings: String,
    val testing: String,
    val localProfiles: String,
    val updateSubscription: String,
    val testPings: String,
    val editSubscription: String,
    val deleteSubscription: String,
    val subscriptionName: String,
    val subscriptionUrl: String,
    val saveChanges: String,
    val cancel: String,
    val addConfigOrSub: String,
    val manualImportSub: String,
    val importClipboard: String,
    val scanQr: String,
    val manualConfig: String,
    val emptyProfilesNotice: String,
    val tapPlusToAdd: String,
    val serverDetails: String,
    val testDelay: String,
    val host: String,
    val port: String,
    val serverNameSni: String,
    val hostHeader: String,
    val path: String,
    val uuidOrPassword: String,
    val trafficDaysRemaining: String,
    val trafficRemaining: String,
    val subscriptionUpdated: String,
    val subscriptionDeleted: String,
    val serverSaved: String,
    val clipboardEmpty: String,
    val errorOccurred: String,
    val holdToManage: String
)

val PersianStrings = Strings(
    edit = "ویرایش",
    done = "اتمام",
    profileImported = "اشتراک اضافه شد",
    connected = "متصل شد",
    disconnected = "قطع شد",
    connecting = "در حال اتصال...",
    notConnected = "متصل نیست",
    tapToConnect = "برای اتصال لمس کنید",
    sortModePing = "مرتب‌سازی: پینگ",
    sortModeName = "مرتب‌سازی: نام",
    testAllPings = "تست پینگ همه",
    testing = "در حال تست...",
    localProfiles = "کانفیگ‌های محلی",
    updateSubscription = "بروزرسانی اشتراک",
    testPings = "تست پینگ سرورها",
    editSubscription = "ویرایش اشتراک",
    deleteSubscription = "حذف کامل این اشتراک",
    subscriptionName = "نام اشتراک",
    subscriptionUrl = "لینک ساب‌اسکریپشن",
    saveChanges = "ذخیره تغییرات",
    cancel = "انصراف",
    addConfigOrSub = "افزودن کانفیگ و اشتراک جدید",
    manualImportSub = "افزودن دستی لینک اشتراک",
    importClipboard = "افزودن از کلیپ‌بورد",
    scanQr = "اسکن کد QR",
    manualConfig = "تنظیم دستی کانفیگ",
    emptyProfilesNotice = "هنوز کانفیگ یا ساب‌اسکریپشنی اضافه نشده است",
    tapPlusToAdd = "برای افزودن اشتراک، علامت + بالای صفحه را بزنید",
    serverDetails = "مشخصات و ویرایش سرور",
    testDelay = "تست پینگ واقعی (Real Delay)",
    host = "آدرس سرور / هاست",
    port = "پورت",
    serverNameSni = "نام سرور (SNI)",
    hostHeader = "هدر هاست (Host Header)",
    path = "مسیر (Path)",
    uuidOrPassword = "شناسه کاربری (UUID / پسورد)",
    trafficDaysRemaining = "روز باقی‌مانده",
    trafficRemaining = "باقی‌مانده",
    subscriptionUpdated = "اشتراک با موفقیت بروزرسانی شد",
    subscriptionDeleted = "اشتراک حذف شد",
    serverSaved = "تغییرات سرور ذخیره شد",
    clipboardEmpty = "کلیپ‌بورد خالی است",
    errorOccurred = "خطا رخ داد",
    holdToManage = "برای مدیریت، روی اشتراک نگه دارید"
)

val EnglishStrings = Strings(
    edit = "Edit",
    done = "Done",
    profileImported = "Profile imported",
    connected = "CONNECTED",
    disconnected = "DISCONNECTED",
    connecting = "CONNECTING...",
    notConnected = "Not Connected",
    tapToConnect = "Tap to connect",
    sortModePing = "SORT BY: PING",
    sortModeName = "SORT BY: NAME",
    testAllPings = "TEST ALL PINGS",
    testing = "TESTING...",
    localProfiles = "LOCAL PROFILES",
    updateSubscription = "Update Subscription",
    testPings = "Test Server Pings",
    editSubscription = "Edit Subscription",
    deleteSubscription = "Delete Subscription",
    subscriptionName = "Subscription Name",
    subscriptionUrl = "Subscription URL",
    saveChanges = "Save Changes",
    cancel = "Cancel",
    addConfigOrSub = "Add Config & Subscription",
    manualImportSub = "Import Subscription Link",
    importClipboard = "Import from Clipboard",
    scanQr = "Scan QR Code",
    manualConfig = "Manual Configuration",
    emptyProfilesNotice = "No configurations or subscriptions added yet",
    tapPlusToAdd = "Tap + at the top to add a subscription",
    serverDetails = "Server Details & Edit",
    testDelay = "Test Real Delay Ping",
    host = "Host / Address",
    port = "Port",
    serverNameSni = "SNI / Server Name",
    hostHeader = "Host Header",
    path = "Path",
    uuidOrPassword = "UUID / Password",
    trafficDaysRemaining = "days remaining",
    trafficRemaining = "remaining",
    subscriptionUpdated = "Subscription updated successfully",
    subscriptionDeleted = "Subscription deleted",
    serverSaved = "Server changes saved",
    clipboardEmpty = "Clipboard is empty",
    errorOccurred = "An error occurred",
    holdToManage = "Press & hold to manage subscription"
)

val LocalStrings = compositionLocalOf { EnglishStrings }

@Composable
fun ProvideAppLocale(
    appLanguage: AppLanguage,
    content: @Composable () -> Unit
) {
    val isPersian = when (appLanguage) {
        AppLanguage.FA -> true
        AppLanguage.EN -> false
        AppLanguage.SYSTEM -> {
            val lang = Locale.getDefault().language
            lang.startsWith("fa") || lang.startsWith("ar")
        }
    }

    val strings = if (isPersian) PersianStrings else EnglishStrings
    val layoutDir = if (isPersian) LayoutDirection.Rtl else LayoutDirection.Ltr

    val context = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.runtime.LaunchedEffect(isPersian) {
        val targetLocale = if (isPersian) Locale("fa") else Locale.ENGLISH
        Locale.setDefault(targetLocale)
        val config = context.resources.configuration
        config.setLocale(targetLocale)
        config.setLayoutDirection(targetLocale)
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    CompositionLocalProvider(
        LocalStrings provides strings,
        LocalLayoutDirection provides layoutDir
    ) {
        content()
    }
}
