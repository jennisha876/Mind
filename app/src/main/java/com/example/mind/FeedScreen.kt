package com.example.mind

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

data class FeedPost(
    val id: Int,
    val user: String,
    val avatar: String, // <-- Emoji instead of drawable
    val text: String,
    val timestamp: String,
    val likes: Int,
    val youLiked: Boolean,
    val tags: List<String>
)

@SuppressLint("RememberReturnType")
@Composable
fun FeedScreen(currentUserAvatar: String = "🌻") { // <-- Emoji avatar
    val currentUser = "You"

    val posts = remember {
        mutableStateListOf(
            FeedPost(
                id = 1,
                user = "Sarah",
                avatar = "🌹",
                text = "Just finished a 10-minute meditation. Feeling calm.",
                timestamp = "10:30 AM",
                likes = 12,
                youLiked = false,
                tags = listOf("#Mindfulness")
            ),
            FeedPost(
                id = 2,
                user = "John",
                avatar = "🌷",
                text = "Hit the gym today. Progress over perfection!",
                timestamp = "11:15 AM",
                likes = 34,
                youLiked = true,
                tags = listOf("#SelfCare")
            ),
            FeedPost(
                id = 3,
                user = currentUser,
                avatar = currentUserAvatar,
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
        // Create Post
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
                                avatar = currentUserAvatar,
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

        // Trending Topics
        item {
            FeedTrendingTopicsCard(
                selectedTopic = selectedTopic,
                onTopicClick = { selectedTopic = it }
            )
        }

        // Feed Posts
        items(filteredPosts, key = { post -> post.id }) { post ->
            FeedPostCard(
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
fun FeedCreatePostCard(
    text: String,
    onTextChange: (String) -> Unit,
    onPostClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text("What's on your mind?") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = onPostClick, modifier = Modifier.align(Alignment.End)) {
            Text("Post")
        }
    }
}

@Composable
fun FeedTrendingTopicsCard(selectedTopic: String?, onTopicClick: (String) -> Unit) {
    // Example static topics
    val topics = listOf("#Mindfulness", "#Gratitude", "#SelfCare")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        topics.forEach { topic ->
            Button(
                onClick = { onTopicClick(topic) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (topic == selectedTopic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(topic)
            }
        }
    }
}

@Composable
fun FeedPostCard(
    post: FeedPost,
    currentUser: String,
    onLikeClicked: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(post.avatar, fontSize = 32.sp)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(post.user, fontWeight = FontWeight.Bold)
                Text(post.timestamp, fontSize = 12.sp)
            }
            Spacer(Modifier.weight(1f))
            if (post.user == currentUser) {
                IconButton(onClick = onDeleteClicked) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(post.text)
        Spacer(Modifier.height(8.dp))
        Row {
            TextButton(onClick = onLikeClicked) {
                Text(if (post.youLiked) "❤️ ${post.likes}" else "🤍 ${post.likes}")
            }
            Spacer(Modifier.width(8.dp))
            post.tags.forEach { tag -> Text(tag, modifier = Modifier.padding(end = 4.dp), color = MaterialTheme.colorScheme.primary) }
        }
    }
}
