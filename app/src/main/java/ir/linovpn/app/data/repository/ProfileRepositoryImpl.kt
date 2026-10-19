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

            val servers = parseSubscriptionUseCase.parseSubscriptionContent(body, profile.id)
            if (servers.isEmpty()) {
                return@withContext Result.failure(Exception("No valid servers found in subscription"))
            }

            serverDao.deleteServersByProfile(profile.id)
            serverDao.insertServers(servers.map { ServerEntity.fromDomain(it) })

            val updated = profile.copy(
                serverCount = servers.size,
                lastUpdated = System.currentTimeMillis()
            )
            profileDao.updateProfile(ProfileEntity.fromDomain(updated))

            Result.success(servers.size)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
