@file:Suppress("FunctionName")

package com.hr9988apps.pigeon.composed.utils

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.hr9988apps.pigeon.R

const val DEFAULT_USER_IMAGE = R.drawable.user_icon

@Composable
fun loadPicture(url: String?, @DrawableRes defaultImage: Int): MutableState<Bitmap?> {

    val bitmapState: MutableState<Bitmap?> = remember { mutableStateOf(null) }
    val imgUrl: MutableState<String?> = remember { mutableStateOf(null) }

    // create a default bitmap image if the bitmap state is null
    if (bitmapState.value == null) {
        Glide.with(LocalContext.current)
            .asBitmap()
            .load(defaultImage)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    bitmapState.value = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }

    // load a new bitmap image if the url has changed
    if (imgUrl.value != url) {
        imgUrl.value = url
        imgUrl.value?.let {
            if (it.isNotBlank() && it.isNotEmpty()) {
                Glide.with(LocalContext.current)
                    .asBitmap()
                    .load(it)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            bitmapState.value = resource
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                        }
                    })
            }
        }
    }
    return bitmapState
}

// resizes bitmap according to the ratio
private fun resizeBitMap(image: Bitmap, size: Int): Bitmap {
    var width = image.width
    var height = image.height

    if (size != 0) {
        val ratio: Float = width.toFloat() / height.toFloat()
        if (ratio > 1F) {
            //width > height
            height = size
            width = (height.toFloat() * ratio).toInt()
        } else {
            //width <= height
            width = size
            height = (width.toFloat() / ratio).toInt()
        }
    }
    return Bitmap.createScaledBitmap(image, width, height, true)
}
