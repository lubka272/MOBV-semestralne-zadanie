package com.example.zadanie.ui.viewmodels

import androidx.lifecycle.*
import com.example.zadanie.data.DataRepository
import com.example.zadanie.data.db.model.Friend
import com.example.zadanie.helpers.Evento
import kotlinx.coroutines.launch

class FriendsViewModel(private val repository: DataRepository) : ViewModel() {
    private val _message = MutableLiveData<Evento<String>>()
    val message: LiveData<Evento<String>>
        get() = _message

    private val _userAdded = MutableLiveData<Evento<Boolean>>()
    val userAdded: LiveData<Evento<Boolean>> get() = _userAdded

    val loading = MutableLiveData(false)

    val friends: LiveData<List<Friend>?> =
        liveData {
            loading.postValue(true)
            repository.apiFriendList { _message.postValue(Evento(it)) }
            loading.postValue(false)
            emitSource(repository.dbFriends())
        }

    fun refreshData() {
        viewModelScope.launch {
            loading.postValue(true)
            repository.apiFriendList { _message.postValue(Evento(it)) }
            loading.postValue(false)
        }
    }

    fun addFriend(username: String) {
        viewModelScope.launch {
            loading.postValue(true)
            repository.apiAddFriend(
                username,
                { _message.postValue(Evento(it)) },
            )
            loading.postValue(false)
        }
    }

    fun show(msg: String) {
        _message.postValue(Evento(msg))
    }
}