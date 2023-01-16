package com.hr9988apps.pigeon.composed.data_classes

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var name: String = "",
    var phoneNumber: String = "",
    var profileImage: String = "",
    var token: String = "",
    var uid: String = "",
)
