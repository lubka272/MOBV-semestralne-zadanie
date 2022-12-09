package com.example.zadanie.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.zadanie.data.api.*
import com.example.zadanie.data.db.LocalCache
import com.example.zadanie.data.db.model.BarItem
import com.example.zadanie.data.db.model.Follower
import com.example.zadanie.data.db.model.Friend
import com.example.zadanie.ui.viewmodels.data.MyLocation
import com.example.zadanie.ui.viewmodels.data.NearbyBar
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.experimental.and


class DataRepository(
    private val service: RestApi,
    private val cache: LocalCache,
    private val context: Context
) {

    suspend fun apiUserCreate(
        name: String,
        password: String,
        onMessage: (message: String) -> Unit,
        onStatus: (success: UserResponse?) -> Unit
    ) {
        try {
            val encryptedPassword = encrypt(password)
            if(encryptedPassword.isEmpty()) {
                onMessage("Registrácia zlyhala. Nastala neočakávaná chyba pri šifrovaní.")
            }
            val resp =
                service.userCreate(UserCreateRequest(name = name, password = encryptedPassword))
            if (resp.isSuccessful) {
                resp.body()?.let { user ->
                    if (user.uid == "-1") {
                        onStatus(null)
                        onMessage("Registrácia zlyhala. Zadané používateľské meno už existuje.")
                    } else {
                        onStatus(user)
                    }
                }
            } else {
                onMessage("Registrácia zlyhala. Skúste neskôr.")
                onStatus(null)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            onMessage("Registrácia zlyhala. Skontrolujte internetové pripojenie.")
            onStatus(null)
        } catch (ex: Exception) {
            ex.printStackTrace()
            onMessage("Registrácia zlyhala. Nastala neočakávaná chyba.")
            onStatus(null)
        }
    }

    suspend fun apiUserLogin(
        name: String,
        password: String,
        onMessage: (message: String) -> Unit,
        onStatus: (success: UserResponse?) -> Unit
    ) {
        try {
            val encryptedPassword = encrypt(password)
            if(encryptedPassword.isEmpty()) {
                onMessage("Prihlásenie zlyhalo. Nastala neočakávaná chyba pri šifrovaní.")
            }
            val resp = service.userLogin(UserLoginRequest(name = name, password = encryptedPassword))
            if (resp.isSuccessful) {
                resp.body()?.let { user ->
                    if (user.uid == "-1") {
                        onStatus(null)
                        onMessage("Prihlásenie zlyhalo. Nesprávne prihlasovacie údaje.")
                    } else {
                        onStatus(user)
                    }
                }
            } else {
                onMessage("Prihlásenie zlyhalo. Skúste neskôr.")
                onStatus(null)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            onMessage("Prihlásenie zlyhalo. Skontrolujte internetové pripojenie.")
            onStatus(null)
        } catch (ex: Exception) {
            ex.printStackTrace()
            onMessage("Prihlásenie zlyhalo. Nastala neočakávaná chyba.")
            onStatus(null)
        }
    }

    suspend fun apiBarCheckin(
        bar: NearbyBar,
        onMessage: (message: String) -> Unit,
        onSuccess: (success: Boolean) -> Unit
    ) {
        try {
            val resp =
                service.barMessage(BarMessageRequest(bar.id, bar.name, bar.type, bar.lat, bar.lon))
            if (resp.isSuccessful) {
                resp.body()?.let {
                    onSuccess(true)
                }
            } else {
                onMessage("Zapísanie sa do podniku zlyhalo.")
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            onMessage("Zapísanie sa do podniku zlyhalo. Skontrolujte internetové pripojenie.")
        } catch (ex: Exception) {
            ex.printStackTrace()
            onMessage("Zapísanie sa do podniku zlyhalo. Nastala neočakávaná chyba.")
        }
    }

    suspend fun apiAddFriend(
        username: String,
        onMessage: (message: String) -> Unit,
    ) {
        try {
            val resp = service.addFriend(AddFriendRequest(username))
            if (resp.isSuccessful) {
                onMessage("Pridanie priateľa prebehlo úspešne. Obnovte stránku.")
            } else {
                onMessage("Pridanie priateľa zlyhalo. Pravdepodobne zadané meno neexistuje.")
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            onMessage("Pridanie priateľa zlyhalo.  Skontrolujte internetové pripojenie.")
        } catch (ex: Exception) {
            ex.printStackTrace()
            onMessage("Pridanie priateľa zlyhalo.")
        }
    }

    suspend fun apiDeleteFriend(
        friend: Friend,
    ) {
        service.deleteFriend(AddFriendRequest(friend.name))
        cache.deleteFriend(friend)
    }

    suspend fun apiBarList(
        onMessage: (message: String) -> Unit
    ) {
        try {
            val resp = service.barList()
            if (resp.isSuccessful) {
                resp.body()?.let { bars ->
                    val b = bars.map {
                        BarItem(
                            it.bar_id,
                            it.bar_name,
                            it.bar_type,
                            it.lat,
                            it.lon,
                            it.users
                        )
                    }
                    cache.insertBars(b)
                } ?: onMessage("Načítanie zoznamu podnikov zlyhalo. Skúste to znovu neskôr.")
            } else {
                onMessage("Načítanie zoznamu podnikov zlyhalo. Skúste to znovu neskôr.")
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            onMessage("Načítanie zoznamu podnikov zlyhalo. Skontrolujte internetové pripojenie.")
        } catch (ex: Exception) {
            ex.printStackTrace()
            onMessage("Načítanie zoznamu podnikov zlyhalo. Nastala neočakávaná chyba.")
        }
    }

    suspend fun apiFriendList(
        onMessage: (message: String) -> Unit
    ) {
        try {
            val resp = service.friendsList()
            if (resp.isSuccessful) {
                resp.body()?.let { friends ->
                    val f = friends.map {
                        Friend(
                            it.user_id,
                            it.user_name,
                            it.bar_id,
                            it.bar_name,
                            it.bar_lat,
                            it.bar_lon
                        )
                    }
                    cache.deleteFriends()
                    cache.insertFriends(f)
                } ?: onMessage("Načítanie zoznamu priateľov zlyhalo.")
            } else {
                onMessage("Načítanie zoznamu priateľov zlyhalo.")
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            onMessage("Načítanie zoznamu priateľov zlyhalo. Skontrolujte internetové pripojenie.")
        } catch (ex: Exception) {
            ex.printStackTrace()
            onMessage("Načítanie zoznamu priateľov zlyhalo. Nastala neočakávaná chyba.")
        }
    }

    suspend fun apiFollowerList(
        onMessage: (message: String) -> Unit
    ) {
        try {
            val resp = service.followersList()
            if (resp.isSuccessful) {
                resp.body()?.let { followers ->
                    val f = followers.map {
                        Follower(
                            it.user_id,
                            it.user_name,
                            it.bar_id,
                            it.bar_name,
                            it.bar_lat,
                            it.bar_lon
                        )
                    }
                    cache.deleteFollowers()
                    cache.insertFollowers(f)
                } ?: onMessage("Načítanie zoznamu sledujúcich zlyhalo.")
            } else {
                onMessage("Načítanie zoznamu sledujúcich zlyhalo.")
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            onMessage("Načítanie zoznamu sledujúcich zlyhalo. Skontrolujte internetové pripojenie.")
        } catch (ex: Exception) {
            ex.printStackTrace()
            onMessage("Načítanie zoznamu sledujúcich zlyhalo. Nastala neočakávaná chyba.")
        }
    }

    suspend fun apiNearbyBars(
        lat: Double, lon: Double,
        onMessage: (message: String) -> Unit
    ): List<NearbyBar> {
        var nearby = listOf<NearbyBar>()
        try {
            val q =
                "[out:json];node(around:250,$lat,$lon);(node(around:250)[\"amenity\"~\"^pub$|^bar$|^restaurant$|^cafe$|^fast_food$|^stripclub$|^nightclub$\"];);out body;>;out skel;"
            val resp = service.barNearby(q)
            if (resp.isSuccessful) {
                resp.body()?.let { bars ->
                    nearby = bars.elements.map {
                        NearbyBar(
                            it.id,
                            it.tags.getOrDefault("name", ""),
                            it.tags.getOrDefault("amenity", ""),
                            it.lat,
                            it.lon,
                            it.tags
                        ).apply {
                            distance = distanceTo(MyLocation(lat, lon))
                        }
                    }
                    nearby = nearby.filter { it.name.isNotBlank() }.sortedBy { it.distance }
                } ?: onMessage("Načítanie zoznamu okolitých podnikov zlyhalo.")
            } else {
                onMessage("Načítanie zoznamu okolitých podnikov zlyhalo.")
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            onMessage("Načítanie zoznamu okolitých podnikov zlyhalo. Skontrolujte internetové pripojenie.")
        } catch (ex: Exception) {
            ex.printStackTrace()
            onMessage("Načítanie zoznamu okolitých podnikov zlyhalo. Nastala neočakávaná chyba.")
        }
        return nearby
    }

    suspend fun apiBarDetail(
        id: String,
        onMessage: (message: String) -> Unit
    ): NearbyBar? {
        var nearby: NearbyBar? = null
        try {
            val q = "[out:json];node($id);out body;>;out skel;"
            val resp = service.barDetail(q)
            if (resp.isSuccessful) {
                resp.body()?.let { bars ->
                    if (bars.elements.isNotEmpty()) {
                        val b = bars.elements.get(0)
                        nearby = NearbyBar(
                            b.id,
                            b.tags.getOrDefault("name", ""),
                            b.tags.getOrDefault("amenity", ""),
                            b.lat,
                            b.lon,
                            b.tags
                        )
                    }
                } ?: onMessage("Načítanie detailu podniku zlyhalo.")
            } else {
                onMessage("Načítanie detailu podniku zlyhalo.")
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            onMessage("Načítanie detailu podniku zlyhalo. Skontrolujte internetové pripojenie.")
        } catch (ex: Exception) {
            ex.printStackTrace()
            onMessage("Načítanie detailu podniku zlyhalo. Nastala neočakávaná chyba.")
        }
        return nearby
    }

    fun dbFriends(): LiveData<List<Friend>?> {
        return cache.getFriends()
    }

    fun dbFollowers(): LiveData<List<Follower>?> {
        return cache.getFollowers()
    }

    companion object {
        @Volatile
        private var INSTANCE: DataRepository? = null

        fun getInstance(service: RestApi, cache: LocalCache, context: Context): DataRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: DataRepository(service, cache, context).also { INSTANCE = it }
            }
    }

    private fun encrypt(password: String): String {
        return try {
            val md: MessageDigest = MessageDigest.getInstance("SHA-512")
            val bytes: ByteArray = md.digest(password.toByteArray())
            val sb = StringBuilder()
            for (i in bytes.indices) {
                sb.append(((bytes[i] and 0xff.toByte()) + 0x100).toString(16).substring(1))
            }
            sb.toString()
        } catch (e: NoSuchAlgorithmException) {
            Log.e("Encryption message", e.printStackTrace().toString())
            ""
        }
    }
}