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

// Post data class is defined in MainActivity.kt or another shared file.

@Composable
fun FeedScreen(
    currentUserAvatar: Int = R.drawable.avatar_female_1 // ← from avatar selection
) {
    val currentUser = "You"

    val posts = remember {
        mutableStateListOf(
            Post(
                1,
                "Sarah",
                avatar =
                    "Just finished a 10-minute meditation. Feeling calm.",
                "10:30 AM",
                12,
                false,
                listOf("#Mindfulness")
            ),
            Post(
                2,
                "John",
                R.drawable.avatar_male_1,
                "Hit the gym today. Progress over perfection!",
                "11:15 AM",
                34,
                true,
                listOf("#SelfCare")
            ),
            Post(
                3,
                currentUser,
                currentUserAvatar,
                "Feeling grateful for the small things today.",
                "11:45 AM",
                5,
                true,
                listOf("#Gratitude")
            )
        )
    }

    var postText by remember { mutableStateOf("") }
    var selectedTopic by remember { mutableStateOf<String?>(null) }

    val filteredPosts =
        if (selectedTopic == null) posts
        else posts.filter { it.tags.contains(selectedTopic) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            CreatePostCard(
                text = postText,
                onTextChange = { postText = it },
                onPostClick = {
                    if (postText.isNotBlank()) {
                        posts.add(
                            0,
                            Post(
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
                                tags = postText.split(" ").filter { it.startsWith("#") }
                            )
                        )
                        postText = ""
                    }
                }
            )
        }

        item {
            TrendingTopicsCard(
                selectedTopic = selectedTopic,
                onTopicClick = { selectedTopic = it }
            )
        }

        items(filteredPosts, key = { it.id }) { post ->
            PostCard(
                post = post,
                currentUser = currentUser,
                onLikeClicked = {
                    val index = posts.indexOfFirst { it.id == post.id }
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

// CreatePostCard is defined in MainActivity.kt or another shared file.

// TrendingTopicsCard is defined in MainActivity.kt or another shared file.

// PostCard is defined in MainActivity.kt or another shared file.
