package com.hr9988apps.pigeon.composed.utils

sealed class OTPState {
    object PhoneNumberRegistration: OTPState()
    object WrongPhoneNumber: OTPState()
    object WrongCountryCode: OTPState()
    object SendingOTP: OTPState()
    object OTPVerification: OTPState()
    object OTPVerifying: OTPState()
    object WrongOTP: OTPState()
    object AuthenticationFailed: OTPState()
    object OTPVerified: OTPState()
}
