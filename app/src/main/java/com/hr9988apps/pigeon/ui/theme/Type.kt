package com.hr9988apps.pigeon.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.hr9988apps.pigeon.R

val RobotoFontFamily = FontFamily(
    listOf(
        Font(
            resId = R.font.roboto_regular
        ),
        Font(
            resId = R.font.roboto_italic,
            style = FontStyle.Italic
        ),
        Font(
            resId = R.font.roboto_bold,
            weight = FontWeight.Bold
        ),
        Font(
            resId = R.font.roboto_bold_italic,
            style = FontStyle.Italic,
            weight = FontWeight.Bold
        ),
        Font(
            resId = R.font.roboto_light,
            weight = FontWeight.Light
        ),
        Font(
            resId = R.font.roboto_light_italic,
            style = FontStyle.Italic,
            weight = FontWeight.Light
        ),
        Font(
            resId = R.font.roboto_medium,
            weight = FontWeight.Medium
        ),
        Font(
            resId = R.font.roboto_medium_italic,
            style = FontStyle.Italic,
            weight = FontWeight.Medium
        ),
        Font(
            resId = R.font.roboto_thin,
            weight = FontWeight.Thin
        ),
        Font(
            resId = R.font.roboto_thin_italic,
            style = FontStyle.Italic,
            weight = FontWeight.Thin
        ),
        Font(
            resId = R.font.roboto_black,
            weight = FontWeight.Black
        ),
        Font(
            resId = R.font.roboto_black_italic,
            style = FontStyle.Italic,
            weight = FontWeight.Black
        )
    )
)

val MoonDance = FontFamily(
    listOf(
        Font(
            resId = R.font.moondance_regular
        )
    )
)

private val CustomFontFamily = RobotoFontFamily

// Set of Material typography styles to start with
val Typography = Typography(
    body1 = TextStyle(
        fontFamily = CustomFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
    /* Other default text styles to override
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
    */
)


