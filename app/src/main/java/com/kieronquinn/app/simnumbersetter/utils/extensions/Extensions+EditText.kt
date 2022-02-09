package com.kieronquinn.app.simnumbersetter.utils.extensions

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce

private const val TYPE_DEBOUNCE = 250L

fun EditText.onChanged() = callbackFlow {
    val textWatcher = object: TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            //No-op
        }

        override fun afterTextChanged(p0: Editable?) {
            //No-op
        }

        override fun onTextChanged(value: CharSequence?, p1: Int, p2: Int, p3: Int) {
            trySend(value)
        }
    }
    addTextChangedListener(textWatcher)
    awaitClose {
        removeTextChangedListener(textWatcher)
    }
}.debounce(TYPE_DEBOUNCE)