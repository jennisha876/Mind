package com.example.mind

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

data class Post(
    val id: Int,
    val user: String,
    val avatar: String,
    val text: String,
    val timestamp: String,
    var likes: Int,
    var youLiked: Boolean,
    val tags: List<String> = emptyList()
)

data class FeaturedRoom(val name: String, val desc: String, val link: String)
data class WellnessResource(val image: Int, val title: String, val preview: String, val url: String)
data class UpcomingRoom(val title: String, val time: String, val icon: String)
data class Task(val id: Int, val text: String, var isCompleted: Boolean)
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
val AVATARS = listOf("Sunflower", "Rose", "Lily", "Tulip", "Daisy", "Orchid", "Lotus", "Peony", "Violet")

@Composable
fun MindScapeApp(darkTheme: Boolean = true, onToggleTheme: () -> Unit = {}) {
    var currentScreen by remember { mutableStateOf(Screen.SPLASH) }

    when (currentScreen) {
        Screen.SPLASH -> SplashScreen { currentScreen = Screen.AUTH }
        Screen.AUTH -> AuthScreen(
            onLogin = { currentScreen = Screen.MAIN },
            onSignup = { currentScreen = Screen.QUESTIONNAIRE }
        )
        Screen.QUESTIONNAIRE -> QuestionnaireScreen { currentScreen = Screen.AVATAR }
        Screen.AVATAR -> AvatarScreen { currentScreen = Screen.MAIN }
        Screen.MAIN -> MainScreen(darkTheme = darkTheme, onToggleTheme = onToggleTheme)
    }
}

@Composable
fun SplashScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1500)
        onDone()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "MindScape Logo",
                modifier = Modifier.size(120.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text("MindScape", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text("Heal • Grow • Connect", color = MaterialTheme.colorScheme.onBackground)
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
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "MindScape Logo",
            modifier = Modifier.size(80.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("MindScape", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(20.dp))

        Row {
            Text(
                "Login",
                modifier = Modifier
                    .clickable { isLogin = true }
                    .padding(8.dp),
                fontWeight = if (isLogin) FontWeight.Bold else FontWeight.Normal,
                color = if (isLogin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.width(16.dp))
            Text(
                "Sign Up",
                modifier = Modifier
                    .clickable { isLogin = false }
                    .padding(8.dp),
                fontWeight = if (!isLogin) FontWeight.Bold else FontWeight.Normal,
                color = if (!isLogin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(20.dp))

        if (isLogin) {
            OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
        } else {
            OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
        }

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

    val onNext: () -> Unit = {
        if (step < QUESTIONS.lastIndex) {
            step++
        } else {
            onFinished()
        }
    }

    val onPrev: () -> Unit = { if (step > 0) step-- }

    val question = QUESTIONS[step]
    var selected by remember(step) { mutableStateOf(answers[step] ?: emptyList()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text("Question ${step + 1} / ${QUESTIONS.size}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))
        Text(question.text, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)

        Spacer(Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(question.options) { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selected = if (question.multi) {
                                if (selected.contains(option)) selected - option else selected + option
                            } else {
                                listOf(option)
                            }
                            answers[step] = selected
                        }
                        .padding(vertical = 4.dp)
                ) {
                    if (question.multi) {
                        Checkbox(checked = selected.contains(option), onCheckedChange = null)
                    } else {
                        RadioButton(selected = selected.contains(option), onClick = null)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(option, color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (step > 0) {
                TextButton(onClick = onPrev) { Text("Back") }
            } else {
                Spacer(Modifier)
            }

            Row {
                TextButton(onClick = onNext) { Text("Skip") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onNext, enabled = selected.isNotEmpty()) { Text(if (step < QUESTIONS.lastIndex) "Next" else "Finish") }
            }
        }
    }
}

@Composable
fun AvatarScreen(onDone: () -> Unit) {
    var selected by remember { mutableStateOf(AVATARS.first()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text("Choose Your Flower Avatar", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(AVATARS) { avatar ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selected = avatar },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selected == avatar, onClick = { selected = avatar })
                    Spacer(Modifier.width(8.dp))
                    Text(avatar, color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
            Text("Continue")
        }
    }
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
                val aiResponse = Message("I hear you. It sounds like you're going through a lot. Can you tell me more?", false, "Now")
                updatedChat = updatedChat.copy(messages = newMessages + aiResponse)
            }
            chats[chatIndex] = updatedChat
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Text("Menu", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                NavigationDrawerItem(icon = { Icon(Icons.Filled.Book, null) }, label = { Text("Journal") }, selected = false, onClick = { /*TODO*/ })
                NavigationDrawerItem(icon = { Icon(Icons.Filled.Spa, null) }, label = { Text("Breathing") }, selected = false, onClick = { /*TODO*/ })
                NavigationDrawerItem(icon = { Icon(Icons.Filled.MusicNote, null) }, label = { Text("Listen Music") }, selected = false, onClick = { /*TODO*/ })
                NavigationDrawerItem(icon = { Icon(Icons.Filled.Person, null) }, label = { Text("Customize Character/Avatar") }, selected = false, onClick = { /*TODO*/ })
                NavigationDrawerItem(icon = { Icon(Icons.Filled.Favorite, null) }, label = { Text("Vitals") }, selected = false, onClick = { /*TODO*/ })
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("${getGreeting()}, Jennisha 👋") },
                    actions = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Open Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Text("Welcome to MindScape!", style = MaterialTheme.typography.headlineSmall) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    chats: List<ChatItem>,
    onChatClick: (ChatItem) -> Unit,
    onNewChat: (name: String, isGroup: Boolean) -> Unit
) {
    var showNewChatDialog by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    val filteredChats = chats.filter { it.name.contains(searchText, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showNewChatDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "New Conversation")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Search conversations...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredChats) { chat ->
                    ChatRow(chat) { onChatClick(chat) }
                }
            }
        }
    }

    if (showNewChatDialog) {
        NewConversationDialog(
            onDismiss = { showNewChatDialog = false },
            onCreate = { name, isGroup ->
                onNewChat(name, isGroup)
                showNewChatDialog = false
            }
        )
    }
}

@Composable
fun ChatRow(chat: ChatItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(getAvatarFor(chat), fontSize = 24.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(chat.name, fontWeight = FontWeight.Bold)
            Text(chat.messages.lastOrNull()?.text ?: "No messages", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, maxLines = 1)
        }
        Text(chat.messages.lastOrNull()?.timestamp ?: "", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Featured rooms slideshow state
    val featuredRooms = listOf(
        "Calm Cove" to "https://meet.jit.si/CalmCoveMindscape",
        "Focus Forest" to "https://meet.jit.si/FocusForestMindscape",
        "Social Lounge" to "https://meet.jit.si/SocialLoungeMindscape"
    )
    var currentRoomIndex by remember { mutableStateOf(0) }

    // Automatic slideshow
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
                    SectionCard(title = "Your Vitals") {
                        Text("Heart Rate: 72 bpm\nMood: Calm 😊")
                    }
                }

                // Tip of the Day
                item {
                    SectionCard(title = "Tip of the Day") {
                        Text("Pause for 60 seconds and take slow, deep breaths to reset your mind.")
                    }
                }

                // Upcoming Rooms / Sessions
                item {
                    SectionCard(title = "Upcoming Sessions") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            UpcomingRoom(
                                title = "Guided Meditation – 6:00 PM",
                                url = "https://meet.jit.si/MindscapeMeditation",
                                context = context
                            )
                            UpcomingRoom(
                                title = "Group Chat – 8:00 PM",
                                url = "https://meet.jit.si/MindscapeGroupChat",
                                context = context
                            )
                        }
                    }
                }

                // Today’s Tasks
                item {
                    SectionCard(title = "Today’s Tasks") {
                        Text("✔ Journal for 5 minutes\n✔ Drink water\n⬜ Evening reflection")
                    }
                }

                // Wellness Resources
                item {
                    SectionCard(title = "Wellness Resources") {
                        Text("• Anxiety coping tools\n• Sleep sounds\n• Crisis support")
                    }
                }
            }
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

                    fun getGreeting(): String {
                        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                        return when (hour) {
                            in 5..11 -> "Good morning"
                            in 12..16 -> "Good afternoon"
                            in 17..20 -> "Good evening"
                            else -> "Hello"
                        }
                    }
                    }
                },
                navigationIcon = {
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
fun MessageBubble(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = if(message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if(message.isFromUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(message.text)
                Text(message.timestamp, style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.align(Alignment.End))
            }
        }
    }
}

@Composable
fun NewConversationDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, isGroup: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var isGroup by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Conversation") },
        text = {
            Column {
                Text("Start a new chat with someone or create a group")
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Contact name") }
                )
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { isGroup = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (!isGroup) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                    ) { Text("Individual") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { isGroup = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isGroup) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                    ) { Text("Group") }
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onCreate(name, isGroup) }, enabled = name.isNotBlank()) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
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
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Summary Content", style = MaterialTheme.typography.headlineMedium)
        // Placeholder for charts
        Spacer(modifier = Modifier.height(16.dp))
        Text("Heart Rate Chart (Placeholder)")
        Spacer(modifier = Modifier.height(16.dp))
        Text("Mood Pie Chart (Placeholder)")
    }
}

@Composable
fun ProfileSettingsTab(darkTheme: Boolean, onToggleTheme: () -> Unit) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Text("Settings", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Dark Mode", modifier = Modifier.weight(1f))
                Switch(checked = darkTheme, onCheckedChange = { onToggleTheme() })
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { throw RuntimeException("Test Crash") }) { 
                Text("Test Crash") 
            }
        }
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

fun getGreeting(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MindTheme(darkTheme = true) {
        MindScapeApp()
    }
}
