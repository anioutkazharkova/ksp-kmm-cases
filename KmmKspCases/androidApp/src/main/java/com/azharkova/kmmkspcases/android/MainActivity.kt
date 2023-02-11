package com.azharkova.kmmkspcases.android

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.azharkova.core.IInteractor
import com.azharkova.kmmkspcases.*

class MainActivity : Activity(), ITestView {
    override var interactor: IInteractor? by interactors(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (interactor as? TestInteractor)?.calculate()

    }
}
