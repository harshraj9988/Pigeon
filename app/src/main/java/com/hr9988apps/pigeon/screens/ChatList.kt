@file:Suppress("FunctionName")

package com.hr9988apps.pigeon.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hr9988apps.pigeon.R
import com.hr9988apps.pigeon.ui.theme.*

@Composable
fun MainChatListComposable() {
    Column {
        CustomTopBarComposable()
        CustomSearchBar()
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(100) {
                CustomListItem(
                    "Louis Phillip",
                    "what's you doing",
                    "12:00",
                    100
                )
                Box(
                    modifier = Modifier.fillMaxWidth(0.95f)
                        .height(1.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f))
                )
            }
        }
    }
}

@Composable
private fun CustomTopBarComposable() {
    Row(
        modifier = Modifier.fillMaxWidth()
            .height(84.dp)
            .offset(y = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(20.dp))
            AvatarComposable(size = 42)
            Spacer(modifier = Modifier.width(18.dp))
            Text(
                text = "Chats",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                letterSpacing = 1.sp
            )
        }
        Row {
            ButtonComposable()
            Spacer(modifier = Modifier.width(20.dp))
        }
    }
}

@Composable
private fun AvatarComposable(size: Int, imageUrl: String? = null) {
    Card(
        modifier = Modifier.size(size.dp)
            .shadow(
                elevation = 6.dp,
                ambientColor = Color.Black,
                spotColor = Color.Black,
                shape = CircleShape
            ),
        backgroundColor = darkGray,
        shape = CircleShape,
        elevation = 6.dp,
    ) {
        Image(
            painter = painterResource(R.drawable.user_icon),
            contentDescription = null,
            modifier = Modifier.padding((size / 10).dp).clip(CircleShape)
        )
    }
}

@Composable
private fun ButtonComposable() {
    Card(
        modifier = Modifier.size(33.dp)
            .shadow(
                elevation = 6.dp,
                ambientColor = Color.Black,
                spotColor = Color.Black,
                shape = CircleShape
            )
            .background(
                brush = Brush.horizontalGradient(
                    listOf(darkRed, lightRed)
                )
            ),
        elevation = 0.dp,
        backgroundColor = Color.Transparent,
        shape = CircleShape,

        ) {
        Image(
            painter = painterResource(R.drawable.add_icon),
            contentDescription = null,
            modifier = Modifier.padding(all = 3.dp)
        )
    }
}

@Composable
private fun CustomSearchBar() {
    Card(
        modifier = Modifier.fillMaxWidth()
            .height(56.dp)
            .padding(start = 30.dp, end = 30.dp, bottom = 8.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        searchBarLight,
                        searchBarDark
                    )
                )
            ).padding(start = 20.dp, end = 20.dp)
            .shadow(
                elevation = (-6).dp,
                shape = RoundedCornerShape(20.dp),
                clip = true,
                ambientColor = Color.Black,
                spotColor = Color.Black,
            ),
        backgroundColor = Color.Transparent,
        elevation = (-6).dp
    ) {
        Text(
            text = "Search",
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        )
    }
}

@Composable
private fun CustomListItem(
    name: String,
    lastMessage: String,
    time: String,
    unseenCount: Int,
    imageUrl: String? = null
) {
    val color = if (unseenCount != 0) notifRed else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(24.dp))
        AvatarComposable(60)
        Spacer(modifier = Modifier.width(15.dp))
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth(0.7f)
        ) {
            Text(
                text = name,
                color = Color.White,
                maxLines = 1,
                fontSize = 18.sp
            )
            Text(
                text = lastMessage,
                color = color,
                maxLines = 1,
                fontSize = 15.sp
            )
        }
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = time, color = color, fontSize = 14.sp)
                if (unseenCount != 0) {
                    Card(
                        modifier = Modifier
                            .wrapContentWidth()
                            .wrapContentHeight()
                            .clip(shape = RoundedCornerShape(6.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(
                                        notifRed,
                                        notifRedDark
                                    )
                                )
                            ).padding(
                                start = 4.dp,
                                end = 4.dp,
                                top = 1.dp,
                                bottom = 2.dp
                            )
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(6.dp),
                                clip = true,
                                ambientColor = Color.Black,
                                spotColor = Color.Black
                            ),
                        backgroundColor = Color.Transparent,
                        elevation = 0.dp
                    ) {
                        Text(
                            text = unseenCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .wrapContentHeight()
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(24.dp))
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainChatListComposablePreview() {
    Pigeon {
        Column(Modifier.background(appBackground)) { MainChatListComposable() }
    }
}
