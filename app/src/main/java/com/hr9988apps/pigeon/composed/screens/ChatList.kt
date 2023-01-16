@file:Suppress("FunctionName")

package com.hr9988apps.pigeon.composed.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hr9988apps.pigeon.R
import com.hr9988apps.pigeon.composed.utils.DEFAULT_USER_IMAGE
import com.hr9988apps.pigeon.composed.utils.loadPicture
import com.hr9988apps.pigeon.composed.view_model.ChatListViewModel
import com.hr9988apps.pigeon.ui.theme.*

@Composable
fun MainChatListComposable(viewModel: ChatListViewModel) {
    val users = viewModel.user.collectAsState()
    Column {

        CustomTopBarComposable(viewModel)

        CustomSearchBar()

        // chat list
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(users.value) {

                CustomListItem(
                    it.name,
                    it.phoneNumber,
                    "12:00",
                    23L,
                    imageUrl = it.profileImage,
                ) {
                    //TODO: go to chat room
                }
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
private fun CustomTopBarComposable(viewModel: ChatListViewModel) {

    val title: String = "Chats"
    val titleSize: Int = 28

    val profilePicUrl : String? = null
    val profilePicSize: Int = 42
    val imageFunction = fun(){}

    val buttonFunction = viewModel::resetContact

    val appBarHeight: Int = 84

    Row(
        modifier = Modifier.fillMaxWidth()
            .height(appBarHeight.dp)
            .offset(y = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(20.dp))

            // User's own profile image
            AvatarComposable(size = profilePicSize, imageUrl = profilePicUrl, onClick = imageFunction)
            Spacer(modifier = Modifier.width(18.dp))
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = titleSize.sp,
                letterSpacing = 1.sp
            )
        }
        Row {

            // Add Button
            ButtonComposable(buttonFunction)

            Spacer(modifier = Modifier.width(20.dp))
        }
    }
}

@Composable
private fun AvatarComposable(
    size: Int,
    imageUrl: String? = null,
    onClick: () -> Unit = fun() {}
) {
    Card(
        modifier = Modifier.size(size.dp)
            .shadow(
                elevation = 6.dp,
                ambientColor = Color.Black,
                spotColor = Color.Black,
                shape = CircleShape
            )
            .clickable {
                onClick()
            },
        backgroundColor = darkGray,
        shape = CircleShape,
        elevation = 6.dp,
    ) {
        val bitmap = loadPicture(imageUrl, DEFAULT_USER_IMAGE)
        bitmap.value?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.padding((size / 10).dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun ButtonComposable(onClick: () -> Unit = fun() {}) {

    val size: Int = 33
    @DrawableRes val icon: Int = R.drawable.add_icon

    Card(
        modifier = Modifier.size(size.dp)
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
            )
            .clickable {
                onClick()
            },
        elevation = 0.dp,
        backgroundColor = Color.Transparent,
        shape = CircleShape,

        ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.padding(all = 3.dp)

        )
    }
}

@Composable
private fun CustomSearchBar() {

    val height: Int = 56
    val searchHint: String = "Search"
    Card(
        modifier = Modifier.fillMaxWidth()
            .height(height.dp)
            .padding(start = 30.dp, end = 30.dp, bottom = 8.dp)
            .clip(RoundedCornerShape((height/2).dp))
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        searchBarLight,
                        searchBarDark
                    )
                )
            ).padding(start = (height/2).dp, end = (height/2).dp)
            .shadow(
                elevation = (-6).dp,
                shape = RoundedCornerShape((height/2).dp),
                clip = true,
                ambientColor = Color.Black,
                spotColor = Color.Black,
            ),
        backgroundColor = Color.Transparent,
        elevation = (-6).dp
    ) {
        // Edit search field
        Text(
            text = searchHint,
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
    unseenCount: Long,
    imageUrl: String? = null,
    onClick: () -> Unit = fun() {},
) {
    val unseenNotifyColor = if (unseenCount != 0L) notifRed else Color.White

    val imageFunction = fun(){}

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(24.dp))

        // List item user icon
        AvatarComposable(60, imageUrl, imageFunction)

        Spacer(modifier = Modifier.width(15.dp))

        NameAndLastMessageComposable(name, lastMessage, unseenNotifyColor)

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

                // list item user last message time
                Text(text = time, color = unseenNotifyColor, fontSize = 14.sp)
                UnseenCountComposable(unseenCount)
            }
            Spacer(modifier = Modifier.width(24.dp))
        }
    }
}

@Composable
private fun UnseenCountComposable(unseenCount: Long) {
    if (unseenCount != 0L) {
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

            // List item user's messages unseen count
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

@Composable
private fun NameAndLastMessageComposable(name: String, lastMessage: String, color: Color) {
    Column(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth(0.7f)
    ) {

        // List item user name
        Text(
            text = name,
            color = Color.White,
            maxLines = 1,
            fontSize = 18.sp
        )

        // List item user's last message
        Text(
            text = lastMessage,
            color = color,
            maxLines = 1,
            fontSize = 15.sp
        )
    }
}
