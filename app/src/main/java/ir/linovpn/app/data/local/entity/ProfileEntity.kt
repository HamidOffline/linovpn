package ir.linovpn.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ir.linovpn.app.domain.model.Profile

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val subscriptionUrl: String,
    val serverCount: Int,
    val lastUpdated: Long,
    val autoUpdate: Boolean,
    val updateIntervalHours: Int,
    val uploadBytes: Long = 0L,
    val downloadBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val expireTimestamp: Long = 0L,
    val trafficSummary: String? = null
) {
    fun toDomain(): Profile = Profile(
        id = id,
        name = name,
        subscriptionUrl = subscriptionUrl,
        serverCount = serverCount,
        lastUpdated = lastUpdated,
        autoUpdate = autoUpdate,
        updateIntervalHours = updateIntervalHours,
        uploadBytes = uploadBytes,
        downloadBytes = downloadBytes,
        totalBytes = totalBytes,
        expireTimestamp = expireTimestamp,
        trafficSummary = trafficSummary
    )

    companion object {
        fun fromDomain(profile: Profile): ProfileEntity = ProfileEntity(
            id = profile.id,
            name = profile.name,
            subscriptionUrl = profile.subscriptionUrl,
            serverCount = profile.serverCount,
            lastUpdated = profile.lastUpdated,
            autoUpdate = profile.autoUpdate,
            updateIntervalHours = profile.updateIntervalHours,
            uploadBytes = profile.uploadBytes,
            downloadBytes = profile.downloadBytes,
            totalBytes = profile.totalBytes,
            expireTimestamp = profile.expireTimestamp,
            trafficSummary = profile.trafficSummary
        )
    }
}
