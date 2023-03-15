package com.hr9988apps.pigeon.utils

sealed interface AuthState {
    object SignedOut: AuthState
    object SignedIn: AuthState
}