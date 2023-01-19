@file:Suppress("FunctionName")

package com.hr9988apps.pigeon.composed.screens

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hr9988apps.pigeon.activity.MainActivity
import com.hr9988apps.pigeon.composed.utils.OTPState
import com.hr9988apps.pigeon.composed.utils.getCountryFromCode
import com.hr9988apps.pigeon.composed.view_model.SignUpViewModel

@Composable
fun SignUpComposable(viewModel: SignUpViewModel, activity: Activity) {
    val otpState = viewModel.otpState
    otpState.value.apply {
        when (this) {
            is OTPState.PhoneNumberRegistration -> {
                PhoneNumberRegisterComposable(viewModel, activity)
            }
            is OTPState.WrongCountryCode -> {
                PhoneNumberRegisterComposable(viewModel, activity)
                NotifyUser("Country code incorrect")
            }
            is OTPState.WrongPhoneNumber -> {
                PhoneNumberRegisterComposable(viewModel, activity)
                NotifyUser("Phone number invalid")
            }
            is OTPState.SendingOTP -> {
                PhoneNumberRegisterComposable(viewModel, activity)
                NotifyUser("Sending OTP")
            }
            is OTPState.OTPVerification -> {
                OTPComposable(viewModel)
            }
            is OTPState.OTPVerifying -> {
                OTPComposable(viewModel)
                NotifyUser("Verifying")
            }
            is OTPState.WrongOTP -> {
                OTPComposable(viewModel)
                NotifyUser("Wrong OTP")
            }
            is OTPState.OTPVerified -> {
                GoToChatList(activity)
            }
            is OTPState.AuthenticationFailed -> {
                PhoneNumberRegisterComposable(viewModel, activity)
                NotifyUser("Authentication failed, Please try again")
            }
        }
    }
}


@Composable
private fun PhoneNumberRegisterComposable(viewModel: SignUpViewModel, activity: Activity) {
    val phoneNumberFocusRequester = remember { FocusRequester() }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(start = 32.dp, end = 32.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CountryCodeComposable(viewModel, phoneNumberFocusRequester)
            Spacer(Modifier.width(10.dp))
            PhoneNumberComposable(viewModel, phoneNumberFocusRequester, activity)
        }
    }

}

@Composable
private fun CountryCodeComposable(
    viewModel: SignUpViewModel,
    nextFieldRequester: FocusRequester
) {
    val focus = remember { FocusRequester() }
    Box(
        modifier = Modifier
            .height(84.dp)
            .fillMaxWidth(0.25f)
    ) {
        OutlinedTextField(
            value = viewModel.phoneCode.value,
            onValueChange = {
                viewModel.setCode(it)

            },
            label = {
                Text(getCountryFromCode(viewModel.phoneCode.value) ?: "Error")
            },
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focus.freeFocus()
                    nextFieldRequester.requestFocus()
                }
            ),
            modifier = Modifier.focusRequester(focus),
            colors = outlinedTextFieldColors(),
            textStyle = TextStyle(
                textAlign = TextAlign.Start,
                fontSize = 20.sp

            )
        )
    }
}

@Composable
private fun PhoneNumberComposable(
    viewModel: SignUpViewModel,
    focusRequester: FocusRequester,
    activity: Activity
) {
    Box(
        modifier = Modifier
            .height(84.dp)
            .fillMaxWidth()
    ) {
        OutlinedTextField(
            value = viewModel.phoneNumber.value,
            onValueChange = {
                viewModel.setPhoneNumber(it)

            },
            label = {
                Text("Phone Number")
            },
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Go
            ),
            keyboardActions = KeyboardActions(
                onGo = {
                    focusRequester.freeFocus()
                    viewModel.sendVerificationCode(activity)
                }
            ),
            modifier = Modifier.focusRequester(focusRequester),
            colors = outlinedTextFieldColors(),
            textStyle = TextStyle(
                textAlign = TextAlign.Start,
                fontSize = 20.sp
            )
        )
    }
}

@Composable
private fun OTPComposable(viewModel: SignUpViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            OTPBoxComposable(viewModel)
        }
    }
}

@Composable
private fun OTPBoxComposable(viewModel: SignUpViewModel) {
    val focus = FocusRequester()
    val widthOfOtpBox = animateDpAsState(
        targetValue = (100 + 5*viewModel.otp.value.length).dp
    )
    Box(
        modifier = Modifier
            .height(84.dp)
            .width(widthOfOtpBox.value)
    ) {
        OutlinedTextField(
            value = viewModel.otp.value,
            onValueChange = {
                viewModel.setOtp(it)
            },
            label = {
                Text("OTP")
            },
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Go
            ),
            keyboardActions = KeyboardActions(
                onGo = {
                    focus.freeFocus()
                    viewModel.verifyOTP()
                }
            ),
            modifier = Modifier.focusRequester(focus),
            colors = outlinedTextFieldColors(),
            textStyle = TextStyle(
                textAlign = TextAlign.Center,
                fontSize = 24.sp
            )
        )
        focus.requestFocus()
    }
}

@Composable
private fun NotifyUser(message: String) {
    Toast.makeText(LocalContext.current, message, Toast.LENGTH_SHORT).show()
}

@Composable
private fun GoToChatList(activity: Activity) {
    val intent = Intent(LocalContext.current, MainActivity::class.java)
    activity.startActivity(intent)
    activity.finish()
}


@Composable
private fun outlinedTextFieldColors() = TextFieldDefaults.outlinedTextFieldColors(
    textColor = Color.White,
    disabledTextColor = Color.White,
    backgroundColor = Color.Transparent,
    cursorColor = Color.White,
    errorCursorColor = Color.White,
    focusedBorderColor = Color.White,
    unfocusedBorderColor = Color.White,
    disabledBorderColor = Color.White,
    errorBorderColor = Color.White,
    leadingIconColor = Color.White,
    disabledLeadingIconColor = Color.White,
    errorLeadingIconColor = Color.White,
    trailingIconColor = Color.White,
    disabledTrailingIconColor = Color.White,
    errorTrailingIconColor = Color.White,
    focusedLabelColor = Color.White,
    unfocusedLabelColor = Color.White,
    disabledLabelColor = Color.White,
    errorLabelColor = Color.White,
    placeholderColor = Color.White,
    disabledPlaceholderColor = Color.White,
)
