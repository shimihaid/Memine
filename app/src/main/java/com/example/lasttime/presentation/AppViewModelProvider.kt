package com.example.lasttime.presentation

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.lasttime.LastTimeApplication
import com.example.lasttime.presentation.addactivity.AddActivityViewModel
import com.example.lasttime.presentation.history.HistoryViewModel
import com.example.lasttime.presentation.home.HomeViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            AddActivityViewModel(lastTimeApplication().container.activityRepository)
        }
        initializer {
            // createSavedStateHandle traz o argumento activityId da rota
            HistoryViewModel(
                lastTimeApplication().container.activityRepository,
                createSavedStateHandle()
            )
        }
        initializer {
            val container = lastTimeApplication().container
            HomeViewModel(container.activityRepository, container.calendarRepository)
        }
    }
}

private fun CreationExtras.lastTimeApplication(): LastTimeApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as LastTimeApplication