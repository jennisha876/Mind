package com.example.mind

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mind.R
import java.text.SimpleDateFormat
import java.util.*



@Composable
fun FeedScreen(
    currentUserAvatar: Int = R.drawable.ic_launcher_foreground // ← fallback avatar
) {
    val currentUser = "You"

    val posts = remember {
        mutableStateListOf(
            FeedPost(
                id = 1,
                user = "Sarah",
                avatarRes = R.drawable.ic_launcher_foreground,
                text = "Just finished a 10-minute meditation. Feeling calm.",
                timestamp = "10:30 AM",
                likes = 12,
                youLiked = false,
                tags = listOf("#Mindfulness")
            ),
            FeedPost(
                id = 2,
                user = "John",
                avatarRes = R.drawable.ic_launcher_foreground,
                text = "Hit the gym today. Progress over perfection!",
                timestamp = "11:15 AM",
                likes = 34,
                youLiked = true,
                tags = listOf("#SelfCare")
            ),
            FeedPost(
                id = 3,
                user = currentUser,
                avatarRes = currentUserAvatar,
                text = "Feeling grateful for the small things today.",
                timestamp = "11:45 AM",
                likes = 5,
                youLiked = true,
                tags = listOf("#Gratitude")
            )
        )
    }

    var postText by remember { mutableStateOf("") }
    var selectedTopic by remember { mutableStateOf<String?>(null) }

    val filteredPosts =
        if (selectedTopic == null) posts
        else posts.filter { post -> post.tags.contains(selectedTopic) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            FeedCreatePostCard(
                text = postText,
                onTextChange = { postText = it },
                onPostClick = {
                    if (postText.isNotBlank()) {
                        posts.add(
                            0,
                            FeedPost(
                                id = posts.size + 1,
                                user = currentUser,
                                avatarRes = currentUserAvatar,
                                text = postText,
                                timestamp = SimpleDateFormat(
                                    "hh:mm a",
                                    Locale.getDefault()
                                ).format(Date()),
                                likes = 0,
                                youLiked = false,
                                tags = postText.split(" ").filter { word -> word.startsWith("#") }
                            )
                        )
                        postText = ""
                    }
                }
            )
        }
        item {
            FeedTrendingTopicsCard(
                selectedTopic = selectedTopic,
                onTopicClick = { selectedTopic = it }
            )
        }
        items(filteredPosts, key = { post -> post.id }) { post ->
            FeedPostCard(
                post = post,
                currentUser = currentUser,
                onLikeClicked = {
                    val index = posts.indexOfFirst { p -> p.id == post.id }
                    if (index != -1) {
                        posts[index] = post.copy(
                            likes = if (post.youLiked) post.likes - 1 else post.likes + 1,
                            youLiked = !post.youLiked
                        )
                    }
                },
                onDeleteClicked = {
                    posts.remove(post)
                }
            )
        }
    }
}


