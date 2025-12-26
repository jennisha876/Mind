package com.example.mind

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mind.ui.theme.MindTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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

// --- Navigation & Data Models ---
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

data class Question(
    val text: String,
    val options: List<String>,
    val multi: Boolean
)

// Using String for avatar to represent emojis
data class Post(
    val id: Int,
    val user: String,
    val avatar: String, // Changed from Int resource to String emoji
    val text: String,
    val timestamp: String,
    var likes: Int,
    var youLiked: Boolean,
    val tags: List<String> = emptyList()
)

data class Message(
    val text: String,
    val isFromUser: Boolean,
    val timestamp: String
)

data class ChatItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isGroup: Boolean = false,
    val isAI: Boolean = false,
    val messages: List<Message> = emptyList()
)

// --- Constants ---
val QUESTIONS = listOf(
    Question("What motivated you to seek a mental health app?", listOf("Stress relief", "Anxiety management", "Mood tracking", "Community support", "Other"), true),
    Question("How often do you feel stressed or anxious?", listOf("Rarely", "Sometimes", "Often", "Almost always"), false),
    Question("What are your main goals for using Mindscape?", listOf("Reduce anxiety", "Improve mood", "Build healthy habits", "Talking to someone", "Listening to music", "Other"), false),
    Question("How do you usually cope with stress?", listOf("Exercise", "Meditation", "Talking to friends/family", "Professional help", "Other"), true),
    Question("How would you rate your current mental health?", listOf("Excellent", "Good", "Fair", "Poor"), false),
    Question("What time of day do you feel most stressed?", listOf("Morning", "Afternoon", "Evening", "Night"), false),
    Question("Which features are you most interested in?", listOf("Guided meditations", "Mood tracking", "Community forums", "AI chat support", "Daily tips"), true),
    Question("Do you have previous experience with mental health apps?", listOf("Yes", "No"), false),
    Question("Would you like to add anything else?", listOf("No, nothing", "Yes, I'll share later"), false)
)

val AVATARS = listOf("🌻", "🌹", "🌷", "🌼", "🌸", "🌺", "🪷")

// --- App Navigation ---
@Composable
fun MindScapeApp() {
    var currentScreen by remember { mutableStateOf(Screen.SPLASH) }
    var darkTheme by remember { mutableStateOf(true) }
    var userAvatar by remember { mutableStateOf(AVATARS.first()) }

    MindTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when (currentScreen) {
                Screen.SPLASH -> SplashScreen { currentScreen = Screen.AUTH }
                Screen.AUTH -> AuthScreen(
                    onLogin = { currentScreen = Screen.MAIN },
                    onSignup = { currentScreen = Screen.QUESTIONNAIRE }
                )
                Screen.QUESTIONNAIRE -> QuestionnaireScreen { currentScreen = Screen.AVATAR }
                Screen.AVATAR -> AvatarScreen {
                    userAvatar = it
                    currentScreen = Screen.MAIN
                }
                Screen.MAIN -> MainScreen(
                    userAvatar = userAvatar,
                    darkTheme = darkTheme,
                    onToggleTheme = { darkTheme = !darkTheme }
                )
            }
        }
    }
}

// --- Onboarding Screens ---
@Composable
fun SplashScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1500)
        onDone()
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = "MindScape Logo", modifier = Modifier.size(120.dp))
            Spacer(Modifier.height(16.dp))
            Text("MindScape", fontSize = 36.sp, fontWeight = FontWeight.Bold)
            Text("Heal • Grow • Connect")
        }
    }
}

@Composable
fun AuthScreen(onLogin: () -> Unit, onSignup: () -> Unit) {
    var isLogin by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = "MindScape Logo", modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(16.dp))
        Text("MindScape", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))

        Row {
            Text("Login", modifier = Modifier.clickable { isLogin = true }.padding(8.dp), fontWeight = if (isLogin) FontWeight.Bold else FontWeight.Normal, color = if (isLogin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.width(16.dp))
            Text("Sign Up", modifier = Modifier.clickable { isLogin = false }.padding(8.dp), fontWeight = if (!isLogin) FontWeight.Bold else FontWeight.Normal, color = if (!isLogin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground)
        }

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(username, { username = it }, label = { Text("Username") }, singleLine = true)
        if (!isLogin) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true)
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(password, { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
        Spacer(Modifier.height(16.dp))
        Button(onClick = { if (isLogin) onLogin() else onSignup() }) {
            Text(if (isLogin) "Login" else "Sign Up")
        }
    }
}

@Composable
fun QuestionnaireScreen(onFinished: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    val answers = remember { mutableMapOf<Int, List<String>>() }
    val question = QUESTIONS[step]
    var selectedOptions by remember(step) { mutableStateOf(answers[step] ?: emptyList()) }

    val onNext = {
        answers[step] = selectedOptions
        if (step < QUESTIONS.lastIndex) step++ else onFinished()
    }
    val onPrev = { if (step > 0) step-- }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Question ${step + 1} of ${QUESTIONS.size}", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(question.text, fontSize = 18.sp)
        Spacer(Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(question.options) { option ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable {
                        selectedOptions = if (question.multi) {
                            if (selectedOptions.contains(option)) selectedOptions - option else selectedOptions + option
                        } else {
                            listOf(option)
                        }
                    }.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (question.multi) {
                        Checkbox(selectedOptions.contains(option), onCheckedChange = null)
                    } else {
                        RadioButton(selectedOptions.contains(option), onClick = null)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(option)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            if (step > 0) {
                TextButton(onClick = onPrev) { Text("Back") }
            } else {
                Spacer(Modifier)
            }
            Row {
                TextButton(onClick = onNext) { Text("Skip") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onNext, enabled = selectedOptions.isNotEmpty()) { Text(if (step < QUESTIONS.lastIndex) "Next" else "Finish") }
            }
        }
    }
}

@Composable
fun AvatarScreen(onAvatarSelected: (String) -> Unit) {
    var selectedAvatar by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Choose Your Flower Avatar", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(AVATARS) { avatar ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { selectedAvatar = avatar }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = (selectedAvatar == avatar), onClick = { selectedAvatar = avatar })
                    Spacer(Modifier.width(16.dp))
                    Text(avatar, fontSize = 24.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { selectedAvatar?.let { onAvatarSelected(it) } },
            enabled = selectedAvatar != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue")
        }
    }
}

// --- Main App Screens ---
@Composable
fun MainScreen(userAvatar: String, darkTheme: Boolean, onToggleTheme: () -> Unit) {
    var currentDestination by remember { mutableStateOf(AppDestinations.HOME) }
    var currentChatId by remember { mutableStateOf<String?>(null) }

    val chats = remember {
        mutableStateListOf(
            ChatItem(id = "ai-1", name = "Mindscape AI Assistant", isAI = true, messages = listOf(Message("Hi! I'm here to help you 24/7 🤖", false, "9:00 AM"))),
            ChatItem(id = "user-1", name = "Sarah M.", messages = listOf(Message("Thank you for the support! 💙", true, "10:30 AM"))),
            ChatItem(id = "user-2", name = "Dr. Anderson", messages = listOf(Message("Your next appointment is confirmed", false, "Yesterday"))),
            ChatItem(id = "grp-1", name = "Wellness Group", isGroup = true, messages = listOf(Message("James: See you at today's session", false, "9:45 AM")))
        )
    }

    fun handleSendMessage(chatId: String, messageText: String) {
        val index = chats.indexOfFirst { it.id == chatId }
        if (index != -1) {
            val chat = chats[index]
            val updatedMessages = chat.messages + Message(messageText, true, "Now")
            chats[index] = if (chat.isAI) {
                chat.copy(messages = updatedMessages + Message("I hear you. Can you tell me more?", false, "Now"))
            } else {
                chat.copy(messages = updatedMessages)
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (currentChatId == null) {
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
        Box(Modifier.padding(innerPadding)) {
            if (currentChatId != null) {
                chats.find { it.id == currentChatId }?.let {
                    ConversationScreen(
                        chat = it,
                        onBack = { currentChatId = null },
                        onSendMessage = { msg -> handleSendMessage(it.id, msg) }
                    )
                }
            } else {
                when (currentDestination) {
                    AppDestinations.HOME -> HomeScreen()
                    AppDestinations.FEED -> FeedScreen(currentUserAvatar = userAvatar)
                    AppDestinations.CHAT -> ChatListScreen(
                        chats = chats,
                        onChatClick = { currentChatId = it.id },
                        onNewChat = { name, isGroup ->
                            val newChat = ChatItem(name = name, isGroup = isGroup)
                            chats.add(newChat)
                            currentChatId = newChat.id
                        }
                    )
                    AppDestinations.PROFILE -> ProfileScreen(darkTheme, onToggleTheme)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val featuredRooms = listOf(
        "Calm Cove" to "https://meet.jit.si/CalmCoveMindscape",
        "Focus Forest" to "https://meet.jit.si/FocusForestMindscape",
        "Social Lounge" to "https://meet.jit.si/SocialLoungeMindscape"
    )
    var currentRoomIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            currentRoomIndex = (currentRoomIndex + 1) % featuredRooms.size
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Text("Menu", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                NavigationDrawerItem(icon = { Icon(Icons.Filled.Book, null) }, label = { Text("Journal") }, selected = false, onClick = {})
                NavigationDrawerItem(icon = { Icon(Icons.Filled.Spa, null) }, label = { Text("Breathing") }, selected = false, onClick = {})
                NavigationDrawerItem(icon = { Icon(Icons.Filled.MusicNote, null) }, label = { Text("Listen Music") }, selected = false, onClick = {})
                NavigationDrawerItem(icon = { Icon(Icons.Filled.Person, null) }, label = { Text("Customize Avatar") }, selected = false, onClick = {})
                NavigationDrawerItem(icon = { Icon(Icons.Filled.Favorite, null) }, label = { Text("Vitals") }, selected = false, onClick = {})
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("${getGreeting()}, Jennisha 👋") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SectionCard(title = "Featured Rooms") {
                        Text(featuredRooms[currentRoomIndex].first)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = { currentRoomIndex = if (currentRoomIndex == 0) featuredRooms.lastIndex else currentRoomIndex - 1 }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                            }
                            Button(onClick = { openJitsi(context, featuredRooms[currentRoomIndex].second) }) {
                                Text("Join")
                            }
                            IconButton(onClick = { currentRoomIndex = (currentRoomIndex + 1) % featuredRooms.size }) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                            }
                        }
                    }
                }
                item { SectionCard(title = "Your Vitals") { Text("Heart Rate: 72 bpm\nMood: Calm 😊") } }
                item { SectionCard(title = "Tip of the Day") { Text("Pause for 60 seconds and take slow, deep breaths to reset your mind.") } }
                item {
                    SectionCard(title = "Upcoming Sessions") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            UpcomingRoomRow(title = "Guided Meditation – 6:00 PM", url = "https://meet.jit.si/MindscapeMeditation", context = context)
                            UpcomingRoomRow(title = "Group Chat – 8:00 PM", url = "https://meet.jit.si/MindscapeGroupChat", context = context)
                        }
                    }
                }
                item { SectionCard(title = "Today’s Tasks") { Text("✔ Journal for 5 minutes\n✔ Drink water\n⬜ Evening reflection") } }
                item { SectionCard(title = "Wellness Resources") { Text("• Anxiety coping tools\n• Sleep sounds\n• Crisis support") } }
            }
        }
    }
}

@Composable
fun UpcomingRoomRow(title: String, url: String, context: Context) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title)
        Button(onClick = { openJitsi(context, url) }) { Text("Join") }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

fun openJitsi(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}


@Composable
fun FeedScreen(currentUserAvatar: String) {
    val currentUser = "You"
    val posts = remember {
        mutableStateListOf(
            Post(1, "Sarah", "🧘", "Just finished a 10-minute meditation. Feeling calm.", "10:30 AM", 12, false, listOf("#Mindfulness")),
            Post(2, "John", "💪", "Hit the gym today. Progress over perfection!", "11:15 AM", 34, true, listOf("#SelfCare")),
            Post(3, currentUser, currentUserAvatar, "Feeling grateful for the small things today.", "11:45 AM", 5, true, listOf("#Gratitude"))
        )
    }

    var postText by remember { mutableStateOf("") }
    var selectedTopic by remember { mutableStateOf<String?>(null) }

    val filteredPosts = if (selectedTopic == null) posts else posts.filter { it.tags.contains(selectedTopic) }

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
                        posts.add(0, Post(id = posts.size + 1, user = currentUser, avatar = currentUserAvatar, text = postText, timestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()), likes = 0, youLiked = false, tags = postText.split(" ").filter { it.startsWith("#") }))
                        postText = ""
                    }
                }
            )
        }

        item { TrendingTopicsCard(selectedTopic = selectedTopic, onTopicClick = { selectedTopic = it }) }

        items(filteredPosts, key = { it.id }) { post ->
            PostCard(
                post = post,
                currentUser = currentUser,
                onLikeClicked = {
                    val index = posts.indexOfFirst { it.id == post.id }
                    if (index != -1) {
                        posts[index] = post.copy(likes = if (post.youLiked) post.likes - 1 else post.likes + 1, youLiked = !post.youLiked)
                    }
                },
                onDeleteClicked = { posts.remove(post) }
            )
        }
    }
}

@Composable
fun CreatePostCard(text: String, onTextChange: (String) -> Unit, onPostClick: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(12.dp)) {
            OutlinedTextField(value = text, onValueChange = onTextChange, placeholder = { Text("Share your thoughts...") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.weight(1f))
                Button(onClick = onPostClick) { Text("Post") }
            }
        }
    }
}

@Composable
fun TrendingTopicsCard(selectedTopic: String?, onTopicClick: (String?) -> Unit) {
    val topics = listOf("#Gratitude", "#SelfCare", "#Mindfulness")
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text("Trending Topics", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row {
                Text("All", modifier = Modifier.padding(end = 12.dp).clickable { onTopicClick(null) }, fontWeight = if (selectedTopic == null) FontWeight.Bold else FontWeight.Normal)
                topics.forEach { Text(it, modifier = Modifier.padding(end = 12.dp).clickable { onTopicClick(it) }, fontWeight = if (selectedTopic == it) FontWeight.Bold else FontWeight.Normal) }
            }
        }
    }
}

@Composable
fun PostCard(post: Post, currentUser: String, onLikeClicked: () -> Unit, onDeleteClicked: () -> Unit) {
    Card(shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(post.avatar, fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp)) // Display emoji
                Spacer(Modifier.width(8.dp))
                Text(post.user, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(post.timestamp, fontSize = 12.sp)
                if (post.user == currentUser) {
                    IconButton(onClick = onDeleteClicked) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(post.text)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLikeClicked) { Icon(if (post.youLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, contentDescription = "Like") }
                Text(post.likes.toString())
                Spacer(Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Comment")
                Spacer(Modifier.width(12.dp))
                Icon(Icons.Default.Share, contentDescription = "Share")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(chats: List<ChatItem>, onChatClick: (ChatItem) -> Unit, onNewChat: (name: String, isGroup: Boolean) -> Unit) {
    var showNewChatDialog by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    val filteredChats = chats.filter { it.name.contains(searchText, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages", fontWeight = FontWeight.Bold) },
                actions = { IconButton(onClick = { showNewChatDialog = true }) { Icon(Icons.Default.Add, contentDescription = "New Conversation") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(value = searchText, onValueChange = { searchText = it }, label = { Text("Search conversations...") }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp))
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredChats) { chat -> ChatRow(chat) { onChatClick(chat) } }
            }
        }
    }

    if (showNewChatDialog) {
        NewConversationDialog(onDismiss = { showNewChatDialog = false }) { name, isGroup ->
            onNewChat(name, isGroup)
            showNewChatDialog = false
        }
    }
}

@Composable
fun ChatRow(chat: ChatItem, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
            Text(getAvatarFor(chat), fontSize = 24.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(chat.name, fontWeight = FontWeight.Bold)
            Text(chat.messages.lastOrNull()?.text ?: "No messages", maxLines = 1)
        }
        Text(chat.messages.lastOrNull()?.timestamp ?: "", fontSize = 12.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(chat: ChatItem, onBack: () -> Unit, onSendMessage: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(chat.name, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        bottomBar = {
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = text, onValueChange = { text = it }, placeholder = { Text("Type a message...") }, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    if (text.isNotBlank()) {
                        onSendMessage(text)
                        text = ""
                    }
                }) { Text("Send") }
            }
        }
    ) { padding ->
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 8.dp), reverseLayout = true) {
            items(chat.messages.reversed()) { message -> MessageBubble(message) }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (message.isFromUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer), modifier = Modifier.widthIn(max = 300.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(message.text)
                Text(message.timestamp, fontSize = 12.sp, modifier = Modifier.align(Alignment.End))
            }
        }
    }
}

@Composable
fun NewConversationDialog(onDismiss: () -> Unit, onCreate: (name: String, isGroup: Boolean) -> Unit) {
    var name by remember { mutableStateOf("") }
    var isGroup by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Conversation") },
        text = {
            Column {
                Text("Start a new chat with someone or create a group")
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Contact name") })
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { isGroup = false }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if (!isGroup) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)) { Text("Individual") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { isGroup = true }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if (isGroup) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)) { Text("Group") }
                }
            }
        },
        confirmButton = { Button(onClick = { if (name.isNotBlank()) onCreate(name, isGroup) }, enabled = name.isNotBlank()) { Text("Create") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
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
        when (selectedTab) {
            0 -> ProfileSummaryTab()
            1 -> ProfileSettingsTab(darkTheme, onToggleTheme)
        }
    }
}

@Composable
fun ProfileSummaryTab() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Profile Summary", style = MaterialTheme.typography.headlineMedium)
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) { Text("Heart Rate Chart (Placeholder)", style = MaterialTheme.typography.bodyLarge) }
        }
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) { Text("Mood Pie Chart (Placeholder)", style = MaterialTheme.typography.bodyLarge) }
        }
    }
}

@Composable
fun ProfileSettingsTab(darkTheme: Boolean, onToggleTheme: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Settings", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Dark Mode", modifier = Modifier.weight(1f))
                Switch(checked = darkTheme, onCheckedChange = { onToggleTheme() })
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { throw RuntimeException("Test Crash") }, modifier = Modifier.fillMaxWidth()) { Text("Test Crash") }
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

fun getGreeting(): String {
    val calendar = Calendar.getInstance()
    return when (calendar.get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..20 -> "Good evening"
        else -> "Hello"
    }
}

// --- Previews ---
@Preview(showBackground = true, name = "Splash Screen")
@Composable
fun SplashScreenPreview() {
    MindTheme(darkTheme = true) { SplashScreen {} }
}

@Preview(showBackground = true, name = "Auth Screen")
@Composable
fun AuthScreenPreview() {
    MindTheme(darkTheme = true) { AuthScreen({}, {}) }
}

@Preview(showBackground = true, name = "Questionnaire")
@Composable
fun QuestionnaireScreenPreview() {
    MindTheme(darkTheme = true) { QuestionnaireScreen {} }
}

@Preview(showBackground = true, name = "Avatar Selection")
@Composable
fun AvatarScreenPreview() {
    MindTheme(darkTheme = true) { AvatarScreen {} }
}

@Preview(showBackground = true, name = "Home Screen")
@Composable
fun HomeScreenPreview() {
    MindTheme(darkTheme = true) { HomeScreen() }
}

@Preview(showBackground = true, name = "Feed Screen")
@Composable
fun FeedScreenPreview() {
    MindTheme(darkTheme = true) { FeedScreen(currentUserAvatar = AVATARS.first()) }
}

@Preview(showBackground = true, name = "Chat List")
@Composable
fun ChatListScreenPreview() {
    MindTheme(darkTheme = true) { ChatListScreen(chats = emptyList(), onChatClick = {}, onNewChat = {_,_ ->}) }
}

@Preview(showBackground = true, name = "Profile Screen")
@Composable
fun ProfileScreenPreview() {
    MindTheme(darkTheme = true) { ProfileScreen(darkTheme = true, onToggleTheme = {}) }
}
