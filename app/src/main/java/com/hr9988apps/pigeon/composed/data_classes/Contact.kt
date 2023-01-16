package com.hr9988apps.pigeon.composed.data_classes

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Contact(
    var name: String = "",
    var profileImage: String = "",
    var lastMessage: String = "",
    var unseenCount: Long = 0,
    var lastMessageTime: Long = 0,
    var uid: String = "",
)
