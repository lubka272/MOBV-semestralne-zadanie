package com.example.zadanie.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val PREFERENCE_NAME = "my_preference"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = PREFERENCE_NAME
)

class AppSettingsRepository(private val context: Context) {

    private val isPubListFetched = booleanPreferencesKey("is_pub_list_fetched")
    private val isPubListSortedByName = intPreferencesKey("is_pub_list_sorted_by_name_int")
    private val isPubListSortedByPocet = intPreferencesKey("is_pub_list_sorted_by_pocet_int")

    suspend fun saveIsPubListFetched(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[isPubListFetched] = value
        }
    }

    suspend fun saveIsSortedByName(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[isPubListSortedByName] = value
        }
    }

    suspend fun saveIsSortedByPocet(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[isPubListSortedByPocet] = value
        }
    }

    val getIsPubListFetched: Flow<Boolean> = context.dataStore.data
        .catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[isPubListFetched] ?: false
        }

    val getIsSortedByName: Flow<Int> = context.dataStore.data
        .catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[isPubListSortedByName] ?: 2
        }

    val getIsSortedByPocet: Flow<Int> = context.dataStore.data
        .catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[isPubListSortedByPocet] ?: 2
        }

}
