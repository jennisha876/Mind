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

data class Post(
    val id: Int,
    val user: String,
    val avatarRes: Int,   // 👈 IMAGE RESOURCE
    val text: String,
    val timestamp: String,
    val likes: Int,
    val youLiked: Boolean,
    val tags: List<String> = emptyList()
)

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
                R.drawable.avatar_female_2,
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

@Composable
fun CreatePostCard(
    text: String,
    onTextChange: (String) -> Unit,
    onPostClick: () -> Unit
) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(12.dp)) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text("Share your thoughts...") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.weight(1f))
                Button(onClick = onPostClick) {
                    Text("Post")
                }
            }
        }
    }
}

@Composable
fun TrendingTopicsCard(
    selectedTopic: String?,
    onTopicClick: (String?) -> Unit
) {
    val topics = listOf("#Gratitude", "#SelfCare", "#Mindfulness")

    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text("Trending Topics", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row {
                Text(
                    "All",
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .clickable { onTopicClick(null) },
                    fontWeight = if (selectedTopic == null) FontWeight.Bold else FontWeight.Normal
                )
                topics.forEach {
                    Text(
                        it,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable { onTopicClick(it) },
                        fontWeight = if (selectedTopic == it) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun PostCard(
    post: Post,
    currentUser: String,
    onLikeClicked: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    Card(shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                Image(
                    painter = painterResource(post.avatarRes),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    post.user,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Text(post.timestamp, fontSize = 12.sp)

                if (post.user == currentUser) {
                    IconButton(onClick = onDeleteClicked) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(post.text)

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLikeClicked) {
                    Icon(
                        if (post.youLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Like"
                    )
                }
                Text(post.likes.toString())
                Spacer(Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Comment")
                Spacer(Modifier.width(12.dp))
                Icon(Icons.Default.Share, contentDescription = "Share")
            }
        }
    }
}
