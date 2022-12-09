package com.example.zadanie.ui.viewmodels

import android.content.Context
import androidx.lifecycle.*
import com.example.zadanie.data.DataRepository
import com.example.zadanie.data.datastore.AppSettingsRepository
import com.example.zadanie.data.db.AppRoomDatabase
import com.example.zadanie.data.db.LocalCache
import com.example.zadanie.data.db.model.BarItem
import com.example.zadanie.helpers.Evento
import kotlinx.coroutines.launch

class BarsViewModel(context: Context, private val repository: DataRepository) : ViewModel() {
    val bars: LiveData<List<BarItem>>
    private val appSettingsRepository: AppSettingsRepository

    private val _message = MutableLiveData<Evento<String>>()
    private val localCache: LocalCache
    val message: LiveData<Evento<String>>
        get() = _message

    val loading = MutableLiveData(false)

    init {
        val pubDao = AppRoomDatabase.getInstance(context).appDao()
        localCache = LocalCache(pubDao)
        appSettingsRepository = AppSettingsRepository(context)

        bars =
            Transformations.switchMap(appSettingsRepository.getIsSortedByName.asLiveData()) { isSortedByName ->
                Transformations.switchMap(appSettingsRepository.getIsSortedByPocet.asLiveData()) { isSortedByPocet ->
                    Transformations.switchMap(appSettingsRepository.getIsPubListFetched.asLiveData()) { isPubListFetched ->
                        if (isPubListFetched) {
                            localCache.getBarsSorted(isSortedByPocet, isSortedByName)
                        } else {
                            liveData {
                                loading.postValue(true)
                                repository.apiBarList { _message.postValue(Evento(it)) }
                                appSettingsRepository.saveIsPubListFetched(true)
                                loading.postValue(false)
                                emitSource(localCache.getBarsSorted(2, 2))
                            }
                        }
                    }
                }
            }
    }

    fun refreshData() {
        viewModelScope.launch {
            loading.postValue(true)
            repository.apiBarList { _message.postValue(Evento(it)) }
            loading.postValue(false)
        }
    }

    fun show(msg: String) {
        _message.postValue(Evento(msg))
    }
}