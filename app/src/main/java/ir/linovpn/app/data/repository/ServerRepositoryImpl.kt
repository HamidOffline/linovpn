package ir.linovpn.app.data.repository

import ir.linovpn.app.data.local.dao.ServerDao
import ir.linovpn.app.data.local.entity.ServerEntity
import ir.linovpn.app.domain.model.Server
import ir.linovpn.app.domain.repository.ServerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerRepositoryImpl @Inject constructor(
    private val serverDao: ServerDao
) : ServerRepository {

    override fun getAllServers(): Flow<List<Server>> {
        return serverDao.getAllServers().map { list -> list.map { it.toDomain() } }
    }

    override fun getServersByProfile(profileId: String): Flow<List<Server>> {
        return serverDao.getServersByProfile(profileId).map { list -> list.map { it.toDomain() } }
    }

    override fun getSelectedServer(): Flow<Server?> {
        return serverDao.getSelectedServer().map { it?.toDomain() }
    }

    override suspend fun getServerById(id: String): Server? {
        return serverDao.getServerById(id)?.toDomain()
    }

    override suspend fun insertServer(server: Server) {
        serverDao.insertServer(ServerEntity.fromDomain(server))
    }

    override suspend fun insertServers(servers: List<Server>) {
        serverDao.insertServers(servers.map { ServerEntity.fromDomain(it) })
    }

    override suspend fun updateServer(server: Server) {
        serverDao.updateServer(ServerEntity.fromDomain(server))
    }

    override suspend fun deleteServer(id: String) {
        serverDao.deleteServer(id)
    }

    override suspend fun deleteAllServers() {
        serverDao.deleteAllServers()
    }

    override suspend fun deleteServersByProfile(profileId: String) {
        serverDao.deleteServersByProfile(profileId)
    }

    override suspend fun setSelectedServer(id: String) {
        serverDao.clearSelection()
        serverDao.selectServer(id)
    }

    override suspend fun updateLatency(id: String, latency: Long) {
        serverDao.updateLatency(id, latency)
    }

    override suspend fun toggleFavorite(id: String) {
        serverDao.toggleFavorite(id)
    }
}
