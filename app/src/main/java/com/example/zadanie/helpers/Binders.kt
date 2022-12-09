package com.example.zadanie.helpers

import android.graphics.Color
import android.view.View
import androidx.databinding.BindingAdapter
import com.google.android.material.snackbar.Snackbar

@BindingAdapter(
    "showTextToast"
)
fun applyShowTextToast(
    view: View,
    message: Evento<String>?
) {
    message?.getContentIfNotHandled()?.let {
        val snackbar = Snackbar.make(view, it, Snackbar.LENGTH_SHORT)
        snackbar.setBackgroundTint(Color.rgb(242, 242, 242))
        snackbar.show()
    }
}