package com.hr9988apps.pigeon.composed.utils

sealed class AuthState{
    object LoggedIn : AuthState()
    object LoggedOut: AuthState()
}
