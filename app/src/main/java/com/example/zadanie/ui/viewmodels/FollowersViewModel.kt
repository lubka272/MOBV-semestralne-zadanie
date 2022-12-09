package com.example.zadanie.ui.viewmodels

import androidx.lifecycle.*
import com.example.zadanie.data.DataRepository
import com.example.zadanie.data.db.model.Follower
import com.example.zadanie.helpers.Evento
import kotlinx.coroutines.launch

class FollowersViewModel(private val repository: DataRepository) : ViewModel() {
    private val _message = MutableLiveData<Evento<String>>()
    val message: LiveData<Evento<String>>
        get() = _message

    val loading = MutableLiveData(false)

    val followers: LiveData<List<Follower>?> =
        liveData {
            loading.postValue(true)
            repository.apiFollowerList { _message.postValue(Evento(it)) }
            loading.postValue(false)
            emitSource(repository.dbFollowers())
        }

    fun refreshData() {
        viewModelScope.launch {
            loading.postValue(true)
            repository.apiFollowerList { _message.postValue(Evento(it)) }
            loading.postValue(false)
        }
    }

    fun show(msg: String) {
        _message.postValue(Evento(msg))
    }
}