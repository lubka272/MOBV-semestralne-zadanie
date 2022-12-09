package com.example.zadanie.ui.viewmodels

import android.app.Dialog
import androidx.lifecycle.*
import com.example.zadanie.data.DataRepository
import com.example.zadanie.helpers.Evento
import com.example.zadanie.ui.viewmodels.data.MyLocation
import com.example.zadanie.ui.viewmodels.data.NearbyBar
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class LocateViewModel(private val repository: DataRepository) : ViewModel() {
    private val _message = MutableLiveData<Evento<String>>()
    val message: LiveData<Evento<String>>
        get() = _message

    val loading = MutableLiveData(false)

    val myLocation = MutableLiveData<MyLocation>(null)
    val myBar = MutableLiveData<NearbyBar>(null)

    private val _checkedIn = MutableLiveData<Evento<Boolean>>()
    val checkedIn: LiveData<Evento<Boolean>>
        get() = _checkedIn


    val bars: LiveData<List<NearbyBar>> = myLocation.switchMap {
        liveData {
            loading.postValue(true)
            it?.let {
                val b = repository.apiNearbyBars(it.lat, it.lon) { _message.postValue(Evento(it)) }
                emit(b)
                if (myBar.value == null) {
                    myBar.postValue(b.firstOrNull())
                }
            } ?: emit(listOf())
            loading.postValue(false)
        }
    }

    fun checkMe(mDialog: Dialog) {
        viewModelScope.launch {
            mDialog.show()
            loading.postValue(true)
            myBar.value?.let {
                repository.apiBarCheckin(
                    it,
                    { _message.postValue(Evento(it)) },
                    { _checkedIn.postValue(Evento(it)) })
            }
            loading.postValue(false)
            Executors.newSingleThreadScheduledExecutor().schedule({
                mDialog.hide()
            }, 3, TimeUnit.SECONDS)
        }
    }

    fun show(msg: String) {
        _message.postValue(Evento(msg))
    }
}