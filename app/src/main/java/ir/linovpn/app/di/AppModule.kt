package ir.linovpn.app.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.linovpn.app.data.local.AppDatabase
import ir.linovpn.app.data.local.dao.ProfileDao
import ir.linovpn.app.data.local.dao.ServerDao
import ir.linovpn.clash.ClashEngine
import ir.linovpn.singbox.SingBoxEngine
import ir.linovpn.v2ray.V2RayEngine
import ir.linovpn.xray.XrayEngine
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "linovpn.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideServerDao(db: AppDatabase): ServerDao = db.serverDao()

    @Provides
    fun provideProfileDao(db: AppDatabase): ProfileDao = db.profileDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideXrayEngine(): XrayEngine = XrayEngine()

    @Provides
    @Singleton
    fun provideSingBoxEngine(): SingBoxEngine = SingBoxEngine()

    @Provides
    @Singleton
    fun provideClashEngine(): ClashEngine = ClashEngine()

    @Provides
    @Singleton
    fun provideV2RayEngine(): V2RayEngine = V2RayEngine()
}
