
package com.example.mind

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

    var entries by remember { mutableStateOf(setOf<String>()) }
    val scope = rememberCoroutineScope()
    val key = stringSetPreferencesKey("entries")

    // Load entries on start
    LaunchedEffect(Unit) {
        val prefs = context.journalDataStore.data.first()
        entries = prefs[key] ?: emptySet()
    }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Journal", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = entryText,
            onValueChange = { entryText = it },
            label = { Text("Write your thoughts...") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            if (entryText.isNotBlank()) {
                scope.launch {
                    context.journalDataStore.edit { prefs ->
                        val updated = (prefs[key] ?: emptySet()) + entryText
                        prefs[key] = updated
                        entries = updated
                        entryText = ""
                    }
                }
            }
        }) {
            Text("Save Entry")
        }
        Spacer(Modifier.height(24.dp))
        Text("Previous Entries:", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f)) {
            items(entries.toList().sortedByDescending { it }) { entry ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(entry, Modifier.padding(12.dp))
                }
            }
        }
    }
}

// --- Free Vitals API Helper ---
@Serializable
data class VitalsResponse(
    @SerialName("heart_rate") val heartRate: Int? = null,
    @SerialName("steps") val steps: Int? = null
)

suspend fun fetchRandomVitals(): VitalsResponse? {
    val url = "https://random-data-api.com/api/v2/heart_rates?size=1"
    return try {
        val response: List<VitalsResponse> = hfClient.get(url).body()
        response.firstOrNull()
    } catch (e: Exception) {
        null
    }
}

// --- AI Chat Helper ---
@Serializable
data class HFRequest(val inputs: String)

@Serializable
data class HFResponse(val generated_text: String? = null)

val hfClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}

suspend fun getAIResponse(message: String): String {
    // You can get a free token from HuggingFace or use the public endpoint for some models
    val apiUrl = "https://api-inference.huggingface.co/models/microsoft/DialoGPT-medium"
    val token = "" // Optional: Add your HF token here for higher rate limits
    return try {
        val response: HttpResponse = hfClient.post(apiUrl) {
            if (token.isNotBlank()) header("Authorization", "Bearer $token")
            setBody(HFRequest(inputs = message))
        }
        val json = response.bodyAsText()
        // The response is a list of objects, get the first generated_text
        val match = Regex("\"generated_text\":\s*\"(.*?)\"").find(json)
        match?.groups?.get(1)?.value ?: "Sorry, I couldn't understand."
    } catch (e: Exception) {
        "Sorry, I couldn't connect to the AI bot."
    }
}


// --- Main Activity ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MindTheme(darkTheme = true) { // Dark theme enabled by default
                MindScapeApp()
            }
        }
    }
}

// --- Navigation Enums ---
enum class Screen {
    SPLASH, AUTH, QUESTIONNAIRE, AVATAR, MAIN
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Filled.Home),
    FEED("Feed", Icons.AutoMirrored.Filled.Article),
    CHAT("Chat", Icons.AutoMirrored.Filled.Chat),
    PROFILE("Profile", Icons.Filled.Person),
}

// --- Data Classes ---
data class Question(
    val text: String,
    val options: List<String>,
    val multi: Boolean
)

// --- Restored Data Classes ---
data class Message(
    val text: String,
    val isUser: Boolean,
    val timestamp: String
)

data class ChatItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val isAI: Boolean = false,
    val isGroup: Boolean = false,
    val messages: List<Message> = emptyList()
)

data class Post(
    val id: Int,
    val user: String,
    val avatar: String,
    val text: String,
    val timestamp: String,
    val likes: Int,
    val youLiked: Boolean,

    val tags: List<String> = emptyList()
)

// --- Helper for Featured Rooms (restored) ---
val featuredRooms = listOf(
    "Mindfulness Meditation" to "https://meet.jit.si/mindfulness-meditation",
    "Gratitude Circle" to "https://meet.jit.si/gratitude-circle",
    "Wellness Chat" to "https://meet.jit.si/wellness-chat"
)

// --- SectionCard Helper (restored) ---
@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun UpcomingRoom(title: String, url: String, context: android.content.Context) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title)
        Button(onClick = { openJitsi(context, url) }) {
            Text("Join")
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

fun openJitsi(context: android.content.Context, url: String) {
    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
    context.startActivity(intent)
}


// ...existing code for Auth/Login/Signup screens if needed...

@Composable
fun QuestionnaireScreen(onFinished: () -> Unit) {
    // TODO: Implement QuestionnaireScreen logic here
}



@Composable
fun MainScreen(darkTheme: Boolean, onToggleTheme: () -> Unit) {
    var currentDestination by remember { mutableStateOf(AppDestinations.HOME) }
    var currentChatId by remember { mutableStateOf<String?>(null) }

    val chats = remember {
        mutableStateListOf(
            ChatItem(
                id = "ai-1",
                name = "Mindscape AI Assistant",
                isAI = true,
                messages = listOf(Message("Hi! I'm here to help you 24/7 🤖", false, "9:00 AM"))
            ),
            ChatItem(id = "user-1", name = "Sarah M.", messages = listOf(Message("Thank you for the support! 💙", true, "10:30 AM"))),
            ChatItem(id = "user-2", name = "Dr. Anderson", messages = listOf(Message("Your next appointment is confirmed", false, "Yesterday"))),
            ChatItem(
                id = "grp-1",
                name = "Wellness Group",
                isGroup = true,
                messages = listOf(Message("James: See you at today's session", false, "9:45 AM"))
            ),
             ChatItem(id = "user-3", name = "Maya P.", messages = listOf(Message("That breathing exercise really helped!", true, "Yesterday")))
        )
    }

    fun handleSendMessage(chatId: String, messageText: String) {
        val chatIndex = chats.indexOfFirst { it.id == chatId }
        if (chatIndex != -1) {
            val chat = chats[chatIndex]
            val newMessages = chat.messages + Message(messageText, true, "Now")
            var updatedChat = chat.copy(messages = newMessages)

            if (chat.isAI) {
                // Launch coroutine to call AI API
                val scope = rememberCoroutineScope()
                scope.launch {
                    val aiReply = getAIResponse(messageText)
                    val aiMessage = Message(aiReply, false, "Now")
                    chats[chatIndex] = updatedChat.copy(messages = updatedChat.messages + aiMessage)
                }
            } else {
                chats[chatIndex] = updatedChat
            }
        }
    }

    val showBottomBar by remember(currentChatId) {
        derivedStateOf { currentChatId == null }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    AppDestinations.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentDestination == destination,
                            onClick = { currentDestination = destination },
                            label = { Text(destination.label) },
                            icon = { Icon(destination.icon, contentDescription = destination.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (currentChatId != null) {
                val chat = chats.find { it.id == currentChatId }
                chat?.let {
                    ConversationScreen(
                        chat = it,
                        onBack = { currentChatId = null },
                        onSendMessage = { messageText -> handleSendMessage(it.id, messageText) }
                    )
                }
            } else {
                when (currentDestination) {
                    AppDestinations.HOME -> HomeScreen()
                    AppDestinations.FEED -> FeedScreen()
                    AppDestinations.CHAT -> ChatListScreen(
                        chats = chats,
                        onChatClick = { chat -> currentChatId = chat.id },
                        onNewChat = { name, isGroup ->
                            val newChat = ChatItem(name = name, isGroup = isGroup)
                            chats.add(newChat)
                            currentChatId = newChat.id
                        }
                    )
                    AppDestinations.PROFILE -> ProfileScreen(darkTheme = darkTheme, onToggleTheme = onToggleTheme)
                }
            }
        }
    }
}


// ...existing code for ChatListScreen, ConversationScreen, and other helpers...

@Composable
fun ChatRow(chat: ChatItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),


// ...existing code...
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Featured Rooms (Manual + Automatic Slideshow)
                item {
                    SectionCard(title = "Featured Rooms") {
                        Text(featuredRooms[currentRoomIndex].first)
                        Spacer(Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = {
                                currentRoomIndex =
                                    if (currentRoomIndex == 0) featuredRooms.lastIndex else currentRoomIndex - 1
                            }) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Previous")
                            }

                            Button(onClick = {
                                openJitsi(context, featuredRooms[currentRoomIndex].second)
                            }) {
                                Text("Join")
                            }

                            IconButton(onClick = {
                                currentRoomIndex = (currentRoomIndex + 1) % featuredRooms.size
                            }) {
                                Icon(Icons.Filled.ArrowForward, contentDescription = "Next")
                            }
                        }
                    }
                }

                // Vitals
                item {

                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Type a message...") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    if (text.isNotBlank()) {
                        onSendMessage(text)
                        text = ""
                    }
                }) {
                    Text("Send")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 8.dp),
            reverseLayout = true
        ) {
            items(chat.messages.reversed()) { message ->
                MessageBubble(message)
            }
        }
    }
}





@Composable
fun ProfileScreen(darkTheme: Boolean, onToggleTheme: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Summary", "Settings")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        // TODO: Implement ProfileSummaryTab and ProfileSettingsTab here if needed
    }
}

@Composable
fun FeedScreen() {
    val currentUser = "You"
    val postsState = remember {
        mutableStateOf(
            listOf(
                Post(1, "Sarah", "🧘", "Just finished a 10-minute meditation. Feeling so much calmer now.", "10:30 AM", 12, false, listOf("#Mindfulness", "#SelfCare")),
                Post(2, "John", "💪", "Hit the gym today and pushed my limits. Progress, not perfection!", "11:15 AM", 34, true),
                Post(3, currentUser, "🌻", "Feeling grateful for the small things today.", "11:45 AM", 5, true, listOf("#Gratitude")),
                Post(4, "Mike", "🧠", "Loving the #Gratitude challenge! It's changing my perspective.", "12:05 PM", 22, false, listOf("#Gratitude"))
            )
        )
    }
    val posts = postsState.value
    var postText by remember { mutableStateOf("") }
    var selectedTopic by remember { mutableStateOf<String?>(null) }

    val filteredPosts = if (selectedTopic == null) posts else posts.filter { it.tags.contains(selectedTopic) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            CreatePostCard(postText, onTextChange = { postText = it }, onPostClick = {
                if (postText.isNotBlank()) {
                    val newPost = Post(
                        id = posts.size + 1,
                        user = currentUser,
                        avatar = "🌻",
                        text = postText,
                        timestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
                        likes = 0,
                        youLiked = false,
                        tags = postText.split(" ").filter { it.startsWith("#") }
                    )
                    postsState.value = listOf(newPost) + posts
                    postText = ""
                }
            })
        }

        item {
            TrendingTopicsCard(selectedTopic, onTopicClick = { topic ->
                selectedTopic = if (selectedTopic == topic) null else topic
            })
        }

        items(filteredPosts, key = { it.id }) { post ->
            PostCard(
                post = post,
                currentUser = currentUser,
                onLikeClicked = {
                    val updatedPosts = posts.map {
                        if (it.id == post.id) {
                            it.copy(likes = if (post.youLiked) post.likes - 1 else post.likes + 1, youLiked = !post.youLiked)
                        } else {
                            it
                        }
                    }
                    postsState.value = updatedPosts
                },
                onDeleteClicked = {
                    postsState.value = posts.filter { it.id != post.id }
                }
            )
        }
    }
}

@Composable
fun CreatePostCard(text: String, onTextChange: (String) -> Unit, onPostClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text("Share your thoughts...") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { /* TODO: Font Style Menu */ }) {
                    Icon(Icons.Default.TextFields, contentDescription = "Format Text", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { /* TODO: Emoji Picker */ }) {
                    Icon(Icons.Default.EmojiEmotions, contentDescription = "Add Emoji", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.weight(1f))
                Button(onClick = onPostClick) {
                    Text("Post")
                }
            }
        }
    }
}

@Composable
fun TrendingTopicsCard(selectedTopic: String?, onTopicClick: (String?) -> Unit) {
    val topics = listOf("#Gratitude", "#SelfCare", "#Mindfulness")
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Trending Topics", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row {
                Text(
                    text = "All",
                    color = if (selectedTopic == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    fontWeight = if (selectedTopic == null) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .clickable { onTopicClick(null) }
                )
                topics.forEach { topic ->
                    Text(
                        text = topic,
                        color = if (selectedTopic == topic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        fontWeight = if (selectedTopic == topic) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable { onTopicClick(topic) }
                    )
                }
            }
        }
    }
}


@Composable
fun PostCard(post: Post, currentUser: String, onLikeClicked: () -> Unit, onDeleteClicked: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(post.avatar, fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                Text(post.user, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                Text(post.timestamp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                if (post.user == currentUser) {
                    IconButton(onClick = onDeleteClicked, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Post", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Text(post.text, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(vertical = 8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLikeClicked) {
                    Icon(
                        imageVector = if (post.youLiked) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.youLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(post.likes.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Comment", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// --- Helper Functions ---
fun getAvatarFor(chat: ChatItem): String {
    return when {
        chat.isAI -> "🤖"
        chat.isGroup -> "👥"
        else -> chat.name.firstOrNull()?.uppercase() ?: " "
    }
}



