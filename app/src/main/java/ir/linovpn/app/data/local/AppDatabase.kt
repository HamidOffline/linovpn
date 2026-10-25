package ir.linovpn.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import ir.linovpn.app.data.local.dao.ProfileDao
import ir.linovpn.app.data.local.dao.ServerDao
import ir.linovpn.app.data.local.entity.ProfileEntity
import ir.linovpn.app.data.local.entity.ServerEntity

@Database(
    entities = [ServerEntity::class, ProfileEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serverDao(): ServerDao
    abstract fun profileDao(): ProfileDao
}
