package ir.linovpn.app.data.repository

import ir.linovpn.app.data.local.dao.ProfileDao
import ir.linovpn.app.data.local.dao.ServerDao
import ir.linovpn.app.data.local.entity.ProfileEntity
import ir.linovpn.app.data.local.entity.ServerEntity
import ir.linovpn.app.domain.model.Profile
import ir.linovpn.app.domain.repository.ProfileRepository
import ir.linovpn.app.domain.usecase.ParseSubscriptionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao,
    private val serverDao: ServerDao,
    private val parseSubscriptionUseCase: ParseSubscriptionUseCase,
    private val okHttpClient: OkHttpClient
) : ProfileRepository {

    override fun getAllProfiles(): Flow<List<Profile>> {
        return profileDao.getAllProfiles().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getProfileById(id: String): Profile? {
        return profileDao.getProfileById(id)?.toDomain()
    }

    override suspend fun insertProfile(profile: Profile) {
        profileDao.insertProfile(ProfileEntity.fromDomain(profile))
    }

    override suspend fun updateProfile(profile: Profile) {
        profileDao.updateProfile(ProfileEntity.fromDomain(profile))
    }

    override suspend fun deleteProfile(id: String) {
        serverDao.deleteServersByProfile(id)
        profileDao.deleteProfile(id)
    }

    override suspend fun fetchAndSaveSubscription(profile: Profile): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(profile.subscriptionUrl)
                .header("User-Agent", "LinoVPN/1.0.0 (Android; v2rayNG/1.8.23)")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error ${response.code}: ${response.message}"))
            }

            val body = response.body?.string() ?: ""
            if (body.isBlank()) {
                return@withContext Result.failure(Exception("Empty subscription response"))
            }

            // 1. Read Subscription-Userinfo header
            val userInfoHeader = response.header("Subscription-Userinfo")
                ?: response.header("subscription-userinfo")
                ?: response.header("Subscription-UserInfo")

            var upload = 0L
            var download = 0L
            var total = 0L
            var expire = 0L

            if (!userInfoHeader.isNullOrBlank()) {
                userInfoHeader.split(";").forEach { part ->
                    val kv = part.trim().split("=")
                    if (kv.size == 2) {
                        val key = kv[0].trim().lowercase()
                        val value = kv[1].trim().toLongOrNull() ?: 0L
                        when (key) {
                            "upload" -> upload = value
                            "download" -> download = value
                            "total" -> total = value
                            "expire" -> expire = value
                        }
                    }
                }
            }

            val servers = parseSubscriptionUseCase.parseSubscriptionContent(body, profile.id)
            if (servers.isEmpty()) {
                return@withContext Result.failure(Exception("No valid servers found in subscription"))
            }

            // 2. Compute traffic summary (e.g. "83.85 GB / 100 GB • 24 روز باقی‌مانده")
            var summary: String? = null
            if (total > 0) {
                val used = upload + download
                val rem = maxOf(0L, total - used)
                val remGb = String.format(java.util.Locale.US, "%.2f", rem.toDouble() / (1024 * 1024 * 1024))
                val totalGb = String.format(java.util.Locale.US, "%.1f", total.toDouble() / (1024 * 1024 * 1024))
                val daysLeft = if (expire > 0) {
                    val nowSec = System.currentTimeMillis() / 1000
                    val diff = expire - nowSec
                    if (diff > 0) (diff / 86400).toInt() else 0
                } else -1
                summary = if (daysLeft >= 0) {
                    "$remGb GB / $totalGb GB • $daysLeft روز باقی‌مانده"
                } else {
                    "$remGb GB / $totalGb GB باقی‌مانده"
                }
            } else {
                // Fallback: check if an info node or server name has volume (e.g. 👤Owner-83.85GB)
                val volumeServer = servers.firstOrNull { it.name.contains("GB", ignoreCase = true) || it.name.contains("MB", ignoreCase = true) }
                if (volumeServer != null) {
                    val match = Regex("(\\d+(?:\\.\\d+)?\\s*(?:GB|MB|TB))", RegexOption.IGNORE_CASE).find(volumeServer.name)
                    if (match != null) {
                        summary = "${match.value.trim()} باقی‌مانده"
                    }
                }
            }

            serverDao.deleteServersByProfile(profile.id)
            serverDao.insertServers(servers.map { ServerEntity.fromDomain(it) })

            val updated = profile.copy(
                serverCount = servers.size,
                lastUpdated = System.currentTimeMillis(),
                uploadBytes = upload,
                downloadBytes = download,
                totalBytes = total,
                expireTimestamp = expire,
                trafficSummary = summary
            )
            profileDao.updateProfile(ProfileEntity.fromDomain(updated))

            Result.success(servers.size)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
