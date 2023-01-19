package com.hr9988apps.pigeon.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import com.hr9988apps.pigeon.R
import com.hr9988apps.pigeon.composed.screens.SignUpComposable
import com.hr9988apps.pigeon.composed.view_model.SignUpViewModel
import com.hr9988apps.pigeon.databinding.ActivitySignUpBinding
import com.hr9988apps.pigeon.ui.theme.Pigeon
import com.hr9988apps.pigeon.ui.theme.appBackground

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val activity: Activity = this

        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent{
                val viewModel: SignUpViewModel by viewModels()
                Pigeon {
                    Column (
                        modifier = Modifier.fillMaxSize()
                            .background(appBackground).systemBarsPadding()
                    ) {
                        SignUpComposable(viewModel, activity)
                    }
                }
            }
        }
    }
}
