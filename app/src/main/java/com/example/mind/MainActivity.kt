package com.example.mind

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import java.util.*
import androidx.core.net.toUri

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MindTheme(darkTheme = true) {
                MindScapeApp()
            }
        }
    }
}

enum class Screen { SPLASH, AUTH, QUESTIONNAIRE, AVATAR, MAIN }

enum class AppDestinations(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Filled.Home),
    FEED("Feed", Icons.AutoMirrored.Filled.Article),
    CHAT("Chat", Icons.AutoMirrored.Filled.Chat),
    PROFILE("Profile", Icons.Filled.Person),
}

data class Question(val text: String, val options: List<String>, val multi: Boolean)
data class Message(val text: String, val isFromUser: Boolean, val timestamp: String)
data class ChatItem(val id: String = UUID.randomUUID().toString(), val name: String, val isGroup: Boolean = false, val isAI: Boolean = false, val messages: List<Message> = emptyList())

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

@Composable
fun MindScapeApp() {
    var currentScreen by remember { mutableStateOf(Screen.SPLASH) }
    var darkTheme by remember { mutableStateOf(true) }
    var userAvatar by remember { mutableStateOf(AVATARS.first()) }
    var username by remember { mutableStateOf("Jennisha") }

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
                    username = username,
                    darkTheme = darkTheme,
                    onToggleTheme = { darkTheme = !darkTheme }
                )
            }
        }
    }
}

@Composable
fun SplashScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) { delay(1500); onDone() }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
        if (!isLogin) { Spacer(Modifier.height(8.dp)); OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true) }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(password, { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
        Spacer(Modifier.height(16.dp))
        Button(onClick = { if (isLogin) onLogin() else onSignup() }) { Text(if (isLogin) "Login" else "Sign Up") }
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
                        } else listOf(option)
                    }.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (question.multi) Checkbox(selectedOptions.contains(option), onCheckedChange = null)
                    else RadioButton(selectedOptions.contains(option), onClick = null)
                    Spacer(Modifier.width(8.dp))
                    Text(option)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            if (step > 0) TextButton(onClick = onPrev) { Text("Back") } else Spacer(Modifier)
            Row {
                TextButton(onClick = onNext as () -> Unit) { Text("Skip") }
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
        ) { Text("Continue") }
    }
}

// --- MainScreen now includes username ---
@Composable
fun MainScreen(userAvatar: String, username: String, darkTheme: Boolean, onToggleTheme: () -> Unit) {
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
            chats[index] = if (chat.isAI) chat.copy(messages = updatedMessages + Message("I hear you. Can you tell me more?", false, "Now")) else chat.copy(messages = updatedMessages)
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
                    ConversationScreen(chat = it, onBack = { currentChatId = null }) { msg -> handleSendMessage(it.id, msg) }
                }
            } else {
                when (currentDestination) {
                    AppDestinations.HOME -> HomeScreen(username)
                    AppDestinations.FEED -> FeedScreen(currentUserAvatar = userAvatar)
                    AppDestinations.CHAT -> ChatListScreen(chats = chats, onChatClick = { currentChatId = it.id }) { name, isGroup ->
                        val newChat = ChatItem(name = name, isGroup = isGroup)
                        chats.add(newChat)
                        currentChatId = newChat.id
                    }
                    AppDestinations.PROFILE -> ProfileScreen(darkTheme, onToggleTheme)
                }
            }
        }
    }
}

// --- HomeScreen now uses username in greeting ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(username: String) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val featuredRooms = listOf(
        "Calm Cove" to "https://meet.jit.si/CalmCoveMindscape",
        "Focus Forest" to "https://meet.jit.si/FocusForestMindscape",
        "Social Lounge" to "https://meet.jit.si/SocialLoungeMindscape"
    )
    var currentRoomIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { while (true) { delay(4000); currentRoomIndex = (currentRoomIndex + 1) % featuredRooms.size } }

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
                    title = { Text("${getGreeting()}, $username 👋") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Filled.Menu, contentDescription = "Menu") }
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
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            IconButton(onClick = { currentRoomIndex = if (currentRoomIndex == 0) featuredRooms.lastIndex else currentRoomIndex - 1 }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                            }
                            Button(onClick = { openJitsi(context, featuredRooms[currentRoomIndex].second) }) { Text("Join") }
                            IconButton(onClick = { currentRoomIndex = (currentRoomIndex + 1) % featuredRooms.size }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                            }
                        }
                    }
                }
                item { SectionCard(title = "Your Vitals") { Text("Heart Rate: 72 bpm\nMood: Calm 😊") } }
                item { SectionCard(title = "Tip of the Day") { Text("Pause for 60 seconds and take slow, deep breaths to reset your mind.") } }
                item { SectionCard(title = "Upcoming Sessions") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        UpcomingRoomRow(url = "https://meet.jit.si/MindscapeMeditation", context = context)
                        UpcomingRoomRow(url = "https://meet.jit.si/MindscapeGroup", context = context)
                    }
                } }
                item { SectionCard(title = "Today's Tasks") { Text("• Journal entry\n• 10 min meditation\n• Connect with a friend") } }
                item { SectionCard(title = "Wellness Resources") { Text("Check our articles and tips for better mental health") } }
            }
        }
    }
}

@Composable
fun UpcomingRoomRow(url: String, context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(url.substringAfterLast("/"), fontSize = 16.sp)
        Button(onClick = { openJitsi(context, url) }) {
            Text("Join")
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}
fun openJitsi(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    context.startActivity(intent)
}

// --- Helper Functions ---
fun getAvatarFor(chat: ChatItem): String {
    return when {
        chat.isAI -> "🤖"
        chat.isGroup -> "👥"
        else -> chat.name.firstOrNull()?.uppercase() ?: " "
    }
}

// Remove duplicate getGreeting() and keep one
fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }
}

// Corrected preview with username
@Preview(showBackground = true, name = "Home Screen")
@Composable
fun HomeScreenPreview() {
    MindTheme(darkTheme = true) { HomeScreen(username = "Jennisha") }
}

