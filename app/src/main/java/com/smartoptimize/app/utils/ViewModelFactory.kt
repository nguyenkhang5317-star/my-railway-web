package com.smartoptimize.app.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** Tiny factory helper so ViewModels can receive constructor dependencies. */
inline fun <reified T : ViewModel> simpleFactory(crossinline create: () -> T): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <M : ViewModel> create(modelClass: Class<M>): M = create() as M
    }
