package com.example.zadanie.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.zadanie.data.db.model.BarItem
import com.example.zadanie.data.db.model.Follower
import com.example.zadanie.data.db.model.Friend

@Dao
interface DbDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBars(bars: List<BarItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriends(friends: List<Friend>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowers(followers: List<Follower>)

    @Query("DELETE FROM bars")
    suspend fun deleteBars()

    @Query("DELETE FROM friends")
    suspend fun deleteFriends()

    @Query("DELETE FROM followers")
    suspend fun deleteFollowers()

    @Delete
    suspend fun deleteFriend(friend: Friend)

    @Query(
        "SELECT * FROM bars order BY " +
        "CASE WHEN :sortByPocetAsc = 1 THEN users end asc, " +
        "CASE WHEN :sortByNameAsc = 1 THEN name end asc, " +
        "CASE WHEN :sortByNameAsc = 0 THEN name end desc, " +
        "CASE WHEN :sortByPocetAsc = 0 THEN users end desc; "
    )
    fun getBarsSorted(sortByPocetAsc: Int, sortByNameAsc: Int): LiveData<List<BarItem>>

    @Query("SELECT * FROM friends order by name DESC")
    fun getFriends(): LiveData<List<Friend>?>

    @Query("SELECT * FROM followers order by name ASC")
    fun getFollowers(): LiveData<List<Follower>?>
}