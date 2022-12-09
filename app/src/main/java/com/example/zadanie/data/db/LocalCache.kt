package com.example.zadanie.data.db

import androidx.lifecycle.LiveData
import com.example.zadanie.data.db.model.BarItem
import com.example.zadanie.data.db.model.Follower
import com.example.zadanie.data.db.model.Friend

class LocalCache(private val dao: DbDao) {
    suspend fun insertBars(bars: List<BarItem>){
        dao.insertBars(bars)
    }

    suspend fun insertFriends(friends: List<Friend>){
        dao.insertFriends(friends)
    }

    suspend fun insertFollowers(followers: List<Follower>){
        dao.insertFollowers(followers)
    }

    suspend fun deleteFriends(){ dao.deleteFriends() }

    suspend fun deleteFollowers(){ dao.deleteFollowers() }

    suspend fun deleteFriend(friend: Friend){ dao.deleteFriend(friend) }

    fun getBarsSorted(isSortedByPocetAsc: Int, isSortedByNameAsc: Int): LiveData<List<BarItem>> = dao.getBarsSorted(isSortedByPocetAsc, isSortedByNameAsc)

    fun getFriends(): LiveData<List<Friend>?> = dao.getFriends()

    fun getFollowers(): LiveData<List<Follower>?> = dao.getFollowers()
}