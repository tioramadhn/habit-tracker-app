package com.example.habittracker.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.habittracker.HabitTrackerApplication

/** Pulls the app's dependency container out of [CreationExtras] inside a ViewModel factory. */
val CreationExtras.appContainer
    get() = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HabitTrackerApplication).container
