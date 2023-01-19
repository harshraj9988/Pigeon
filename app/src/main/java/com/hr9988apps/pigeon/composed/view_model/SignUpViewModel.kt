package com.hr9988apps.pigeon.composed.view_model

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.hr9988apps.pigeon.composed.utils.OTPState
import com.hr9988apps.pigeon.composed.utils.getCountryFromCode
import java.util.concurrent.TimeUnit

class SignUpViewModel : ViewModel() {

    private val _otpState: MutableState<OTPState> = mutableStateOf(OTPState.PhoneNumberRegistration)
    val otpState: State<OTPState> = _otpState

    private val _phoneCode = mutableStateOf("+91")
    val phoneCode: State<String> = _phoneCode

    private val _phoneNumber = mutableStateOf("")
    val phoneNumber: State<String> = _phoneNumber

    private val _otp = mutableStateOf("")
    val otp: State<String> = _otp
    private val auth = FirebaseAuth.getInstance()

    private lateinit var storedVerificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    private var safeToVerify = false

    fun verifyOTP() {
       if(safeToVerify) { verifyCode(otp.value) }
    }

    private fun signInByCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    if (it.result.user != null) {
                        _otpState.value = OTPState.OTPVerified
                    } else {
                        _otpState.value = OTPState.WrongOTP
                    }
                } else if (it.isCanceled) {
                    _otpState.value = OTPState.AuthenticationFailed
                }
            }
    }

    private fun verifyCode(code: String) {
        _otpState.value = OTPState.OTPVerifying
        val credential: PhoneAuthCredential =
            PhoneAuthProvider.getCredential(storedVerificationId, code)
        signInByCredential(credential)
    }


    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            val code: String? = credential.smsCode
            if (code != null) {
                _otp.value = code
                verifyCode(code)
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.e("FirebaseException", e.stackTraceToString())
            _otpState.value = OTPState.AuthenticationFailed
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            super.onCodeSent(verificationId, token)
            storedVerificationId = verificationId
            resendToken = token
            safeToVerify = true
            _otpState.value = OTPState.OTPVerification
        }
    }

    fun sendVerificationCode(activity: Activity) {
        try {
            if (!correctCountryCode(phoneCode.value)) {
                _otpState.value = OTPState.WrongCountryCode
                return
            }
            if (!correctPhoneNumber(phoneNumber.value)) {
                _otpState.value = OTPState.WrongPhoneNumber
                return
            }
            _otpState.value = OTPState.SendingOTP
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber("${phoneCode.value}${phoneNumber.value}") // TODO: Set phone number with check
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (_: IllegalStateException) {
            _otpState.value = OTPState.AuthenticationFailed
        }
    }

    private fun correctCountryCode(code: String) = (getCountryFromCode(code) != null)

    private fun correctPhoneNumber(number: String): Boolean {
        if (number.length < 10) return false
        for (digit in number) {
            if (digit !in '0'..'9') return false
        }
        return true
    }

    fun setPhoneNumber(number: String) {
        _phoneNumber.value = number
    }

    fun setCode(code: String) {
        _phoneCode.value = code
    }

    fun setOtp(digit: String) {
        _otp.value = digit
    }
}
