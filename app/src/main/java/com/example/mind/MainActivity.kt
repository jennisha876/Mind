package com.example.mind

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
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
            MindTheme(darkTheme = true) { MindScapeApp() }
        }
    }
}

// --- Data Models ---
data class Post(val id: Int, val user: String, val avatar: String, val text: String, val timestamp: String, var likes: Int, var youLiked: Boolean, val hashtags: List<String>)
data class Message(val text: String, val isFromUser: Boolean, val timestamp: String)
data class ChatItem(val id: String = UUID.randomUUID().toString(), val name: String, val isGroup: Boolean = false, val isAI: Boolean = false, val messages: List<Message> = emptyList())
data class Task(val id: Int, val text: String, var isCompleted: Boolean)
data class WellnessResource(val title: String, val preview: String, val imageUrl: String, val url: String)
data class JournalEntry(val id: Int, val text: String, val timestamp: String)
data class Song(val title: String, val artist: String, val coverUrl: String)
data class Question(val text: String, val options: List<String>, val multi: Boolean)

// --- Navigation Enums ---
enum class Screen { SPLASH, AUTH, AVATAR, MAIN, JOURNAL, BREATHING, MUSIC, VITALS, QUESTIONNAIRE }
enum class AppDestinations(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Filled.Home), FEED("Feed", Icons.AutoMirrored.Filled.Article), CHAT("Chat", Icons.AutoMirrored.Filled.Chat), PROFILE("Profile", Icons.Filled.Person)
}

// --- Constants ---
val AVATARS = listOf("🌻", "🌹", "🌷", "🌼", "🌸", "🌺", "🪷")
val MOODS = listOf("😊", "😔", "😠", "😐", "😁")
val WELLNESS_RESOURCES = listOf(WellnessResource("Breathing Exercises for Anxiety", "Learn techniques that instantly reduce stress...", "https://i.imgur.com/h3YkW7f.jpg", "https://www.healthline.com/health/breathing-exercises-for-anxiety"))
val SONGS = listOf(
    Song("Weightless", "Marconi Union", "https://i.scdn.co/image/ab67616d0000b273b7a5a81053c48a7199738c8c"),
    Song("Clair de Lune", "Claude Debussy", "https://i.scdn.co/image/ab67616d0000b273e51a2a74e5a953932223788d"),
    Song("Canzonetta Sul'aria", "Mozart", "https://i.scdn.co/image/ab67616d0000b2737a4c47e09e25a2e37e1b9338")
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

// --- App Navigation ---
@Composable
fun MindScapeApp() {
    var currentScreen by remember { mutableStateOf(Screen.SPLASH) }
    var darkTheme by remember { mutableStateOf(true) }
    var userAvatar by remember { mutableStateOf(AVATARS.first()) }
    val username = "Jennisha"
    val userEmail = "jennisha.smith@example.com"
    val userJoinedDate = "Joined since 2024"

    MindTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when (currentScreen) {
                Screen.SPLASH -> SplashScreen { currentScreen = Screen.AUTH }
                Screen.AUTH -> AuthScreen(onLogin = { currentScreen = Screen.MAIN }, onSignup = { currentScreen = Screen.QUESTIONNAIRE })
                Screen.QUESTIONNAIRE -> QuestionnaireScreen { currentScreen = Screen.AVATAR }
                Screen.AVATAR -> AvatarScreen { userAvatar = it; currentScreen = Screen.MAIN }
                Screen.MAIN -> MainScreen(userAvatar, username, userEmail, userJoinedDate, darkTheme, { darkTheme = !darkTheme }, { screen -> currentScreen = screen }, onLogout = { currentScreen = Screen.AUTH })
                Screen.JOURNAL -> JournalScreen { currentScreen = Screen.MAIN }
                Screen.BREATHING -> BreathingScreen { currentScreen = Screen.MAIN }
                Screen.MUSIC -> MusicScreen { currentScreen = Screen.MAIN }
                Screen.VITALS -> VitalsScreen { currentScreen = Screen.MAIN }
            }
        }
    }
}

// --- Reusable & Onboarding Screens ---
@Composable fun SplashScreen(onDone: () -> Unit) { LaunchedEffect(Unit) { delay(1500); onDone() }; Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("MindScape", fontSize = 36.sp, fontWeight = FontWeight.Bold) } }

@Composable
fun AuthScreen(onLogin: () -> Unit, onSignup: () -> Unit) {
    var isLogin by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("MindScape", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        Row {
            Text("Login", modifier = Modifier.clickable { isLogin = true }.padding(8.dp), fontWeight = if (isLogin) FontWeight.Bold else FontWeight.Normal)
            Spacer(Modifier.width(16.dp))
            Text("Sign Up", modifier = Modifier.clickable { isLogin = false }.padding(8.dp), fontWeight = if (!isLogin) FontWeight.Bold else FontWeight.Normal)
        }
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(username, { username = it }, label = { Text("Username") })
        if (!isLogin) { Spacer(Modifier.height(8.dp)); OutlinedTextField("", { }, label = { Text("Email") }) }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(password, { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation())
        Spacer(Modifier.height(16.dp))
        Button(onClick = { if (isLogin) onLogin() else onSignup() }) { Text(if (isLogin) "Login" else "Sign Up") }
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
                Row(modifier = Modifier.fillMaxWidth().clickable { selectedAvatar = avatar }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = (selectedAvatar == avatar), onClick = { selectedAvatar = avatar })
                    Spacer(Modifier.width(16.dp))
                    Text(avatar, fontSize = 24.sp)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { selectedAvatar?.let { onAvatarSelected(it) } }, enabled = selectedAvatar != null, modifier = Modifier.fillMaxWidth()) { Text("Continue") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionnaireScreen(onFinished: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    val question = QUESTIONS[step]
    var selectedOptions by remember(step) { mutableStateOf(emptyList<String>()) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Question ${step + 1} of ${QUESTIONS.size}", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(question.text, fontSize = 18.sp)
        Spacer(Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(question.options) { option ->
                Row(modifier = Modifier.fillMaxWidth().clickable { selectedOptions = if (question.multi) { if (selectedOptions.contains(option)) selectedOptions - option else selectedOptions + option } else listOf(option) }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (question.multi) Checkbox(selectedOptions.contains(option), onCheckedChange = null) else RadioButton(selectedOptions.contains(option), onClick = null)
                    Spacer(Modifier.width(8.dp))
                    Text(option)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            if (step > 0) { TextButton(onClick = { step-- }) { Text("Back") } } else { Spacer(Modifier) }
            Button(onClick = { if (step < QUESTIONS.lastIndex) step++ else onFinished() }, enabled = selectedOptions.isNotEmpty()) { Text(if (step < QUESTIONS.lastIndex) "Next" else "Finish") }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(userAvatar: String, username: String, email: String, joinedDate: String, darkTheme: Boolean, onToggleTheme: () -> Unit, onNavigate: (Screen) -> Unit, onLogout: () -> Unit) {
    var currentDestination by remember { mutableStateOf(AppDestinations.HOME) }
    Scaffold(bottomBar = { NavigationBar { AppDestinations.entries.forEach { destination -> NavigationBarItem(selected = currentDestination == destination, onClick = { currentDestination = destination }, label = { Text(destination.label) }, icon = { Icon(destination.icon, null) }) } } }) {
        Box(Modifier.padding(it)) {
            when (currentDestination) {
                AppDestinations.HOME -> HomeScreen(username, onNavigate)
                AppDestinations.FEED -> FeedScreen(userAvatar, username)
                AppDestinations.CHAT -> ChatListScreen()
                AppDestinations.PROFILE -> ProfileScreen(userAvatar, username, email, joinedDate, darkTheme, onToggleTheme, onLogout)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(username: String, onNavigate: (Screen) -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val tasks = remember { mutableStateListOf(Task(1, "Journal for 5 minutes", false), Task(2, "Drink water", true)) }
    val featuredRooms = listOf("Calm Cove" to "https://meet.jit.si/CalmCoveMindscape", "Focus Forest" to "https://meet.jit.si/FocusForestMindscape")
    var currentRoomIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { while (true) { delay(4000); currentRoomIndex = (currentRoomIndex + 1) % featuredRooms.size } }

    ModalNavigationDrawer(drawerState = drawerState, drawerContent = { ModalDrawerSheet {
        Spacer(Modifier.height(12.dp)); Text("Menu", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
        NavigationDrawerItem(icon = { Icon(Icons.Filled.Book, null) }, label = { Text("Journal") }, selected = false, onClick = { onNavigate(Screen.JOURNAL) })
        NavigationDrawerItem(icon = { Icon(Icons.Filled.Spa, null) }, label = { Text("Breathing") }, selected = false, onClick = { onNavigate(Screen.BREATHING) })
        NavigationDrawerItem(icon = { Icon(Icons.Filled.MusicNote, null) }, label = { Text("Listen Music") }, selected = false, onClick = { onNavigate(Screen.MUSIC) })
        NavigationDrawerItem(icon = { Icon(Icons.Filled.Favorite, null) }, label = { Text("Vitals") }, selected = false, onClick = { onNavigate(Screen.VITALS) })
    } }) {
        Scaffold(topBar = { TopAppBar(title = { Text("Good morning, $username 👋") }, navigationIcon = { IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Filled.Menu, "Menu") } }) }) { innerPadding ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item { SectionCard("Featured Rooms") { 
                    Text(featuredRooms[currentRoomIndex].first)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { currentRoomIndex = if (currentRoomIndex == 0) featuredRooms.lastIndex else currentRoomIndex - 1 }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous") }
                        Button(onClick = { openJitsi(context, featuredRooms[currentRoomIndex].second) }) { Text("Join") }
                        IconButton(onClick = { currentRoomIndex = (currentRoomIndex + 1) % featuredRooms.size }) { Icon(Icons.Default.ArrowForward, "Next") }
                    }
                } }
                item { SectionCard("How are you feeling?") { MoodCheckIn() } }
                item { SectionCard("Your Vitals") { Text("Heart Rate: 72 bpm\nMood: Calm 😊") } }
                item { SectionCard("Tip of the Day") { Text("Pause for 60 seconds and take slow, deep breaths to reset your mind.") } }
                item { SectionCard("Upcoming Sessions") { UpcomingRoomRow("Guided Meditation at 6 PM", "https://meet.jit.si/MindscapeMeditation", context) } }
                item { SectionCard("Today’s Tasks") { TodayTasks(tasks) } }
                item { SectionCard("Wellness Resources") { WellnessResourceCard(WELLNESS_RESOURCES.first(), context) } }
            }
        }
    }
}

@Composable
fun MoodCheckIn() { Row(Modifier.fillMaxWidth(), Arrangement.SpaceAround) { MOODS.forEach { Text(it, fontSize = 32.sp, modifier = Modifier.clickable { /* Mood selection logic */ }) } } }

@Composable
fun TodayTasks(tasks: MutableList<Task>) {
    tasks.forEachIndexed { i, task ->
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { tasks[i] = task.copy(isCompleted = !task.isCompleted) }) {
            Checkbox(task.isCompleted, { tasks[i] = task.copy(isCompleted = it) })
            Text(task.text)
        }
    }
}

@Composable
fun FeedScreen(currentUserAvatar: String, currentUserName: String) {
    val context = LocalContext.current
    val posts = remember { mutableStateListOf(Post(1, "Sarah", "🌸", "Just finished a 10-minute meditation. Feeling calm. #Mindfulness", "10:30 AM", 12, false, listOf("#Mindfulness"))) }
    var postText by remember { mutableStateOf("") }
    val trendingTopics by remember { derivedStateOf { posts.flatMap { it.hashtags }.groupingBy { it }.eachCount().toList().sortedByDescending { it.second }.take(5) } }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { CreatePostCard(postText, { postText = it }) { if (postText.isNotBlank()) { posts.add(0, Post(posts.size + 1, currentUserName, currentUserAvatar, postText, "Now", 0, false, postText.split(" ").filter { it.startsWith("#") })); postText = "" } } }
        item { TrendingTopicsCard(trendingTopics) }
        items(posts, key = { it.id }) { post ->
            PostCard(post, post.user == currentUserName, onLikeClicked = { post.youLiked = !post.youLiked; post.likes += if (post.youLiked) 1 else -1 }, onShareClicked = { val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, post.text) }; context.startActivity(Intent.createChooser(intent, "Share post")) }, onDeleteClicked = { posts.remove(post) })
        }
    }
}

@Composable fun TrendingTopicsCard(trends: List<Pair<String, Int>>) { /* ... */ }
@Composable fun CreatePostCard(text: String, onTextChange: (String) -> Unit, onPostClick: () -> Unit) { /* ... */ }
@Composable fun PostCard(post: Post, isOwnPost: Boolean, onLikeClicked: () -> Unit, onShareClicked: () -> Unit, onDeleteClicked: () -> Unit) { /* ... */ }

@Composable fun ChatListScreen() { Text("Chat Screen - Full Implementation Coming Soon") }

@Composable
fun ProfileScreen(avatar: String, name: String, email: String, joinedDate: String, darkTheme: Boolean, onToggleTheme: () -> Unit, onLogout: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        item { Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) { Text(avatar, fontSize = 72.sp); Spacer(Modifier.height(8.dp)); Text(name, style = MaterialTheme.typography.headlineMedium); Text(email, style = MaterialTheme.typography.bodyMedium); Text(joinedDate, style = MaterialTheme.typography.bodySmall) } }
        item { HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) }
        item { Text("Summary", style = MaterialTheme.typography.titleLarge) }
        item { Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) { Box(modifier = Modifier.fillMaxWidth().height(150.dp).padding(16.dp), contentAlignment = Alignment.Center) { Text("Mood Chart (Placeholder)") } } }
        item { HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) }
        item { Text("Settings", style = MaterialTheme.typography.titleLarge) }
        item { Row(verticalAlignment = Alignment.CenterVertically) { Text("Dark Mode", modifier = Modifier.weight(1f)); Switch(checked = darkTheme, onCheckedChange = { onToggleTheme() }) } }
        item { Button(onClick = onLogout, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) { Text("Logout") } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun RealScreen(screenTitle: String, onBack: () -> Unit) { /* ... */ }
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun VitalsScreen(onBack: () -> Unit) { /* ... */ }

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun JournalScreen(onBack: () -> Unit) { /* ... */ }

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun BreathingScreen(onBack: () -> Unit) { /* ... */ }

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun MusicScreen(onBack: () -> Unit) { /* ... */ }

@Composable fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) { Card(modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(title, style = MaterialTheme.typography.titleMedium); content() } } }
@Composable fun UpcomingRoomRow(title: String, url: String, context: Context) { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(title); Button(onClick = { openJitsi(context, url) }) { Text("Join") } } }
@Composable fun WellnessResourceCard(resource: WellnessResource, context: Context) { Card(modifier = Modifier.fillMaxWidth().clickable { openJitsi(context, resource.url) }) { Column { Image(painter = rememberAsyncImagePainter(resource.imageUrl), contentDescription = resource.title, modifier = Modifier.fillMaxWidth().height(120.dp), contentScale = ContentScale.Crop); Column(Modifier.padding(16.dp)) { Text(resource.title, fontWeight = FontWeight.Bold); Text(resource.preview, style = MaterialTheme.typography.bodySmall) } } } }
fun getGreeting(): String { return "Good morning" }
fun openJitsi(context: Context, url: String) { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }

// --- Previews ---
@Preview(showBackground = true) @Composable fun DefaultPreview() { MindTheme(darkTheme = true) { MindScapeApp() } }
@Preview(showBackground = true) @Composable fun MainScreenPreview() { MindTheme(darkTheme = true) { MainScreen(AVATARS.first(), "Jennisha", "jennisha.smith@example.com", "Joined since 2024", true, {}, {}, {}) } }
@Preview(showBackground = true) @Composable fun HomeScreenPreview() { MindTheme(darkTheme = true) { HomeScreen("Jennisha", {}) } }
@Preview(showBackground = true) @Composable fun FeedScreenPreview() { MindTheme(darkTheme = true) { FeedScreen(AVATARS.first(), "Jennisha") } }
@Preview(showBackground = true) @Composable fun ChatScreenPreview() { MindTheme(darkTheme = true) { ChatListScreen() } }
@Preview(showBackground = true) @Composable fun ProfileScreenPreview() { MindTheme(darkTheme = true) { ProfileScreen(AVATARS.first(), "Jennisha", "jennisha.smith@example.com", "Joined since 2024", true, {}, {}) } }
@Preview(showBackground = true) @Composable fun JournalScreenPreview() { MindTheme(darkTheme = true) { JournalScreen {} } }
@Preview(showBackground = true) @Composable fun BreathingScreenPreview() { MindTheme(darkTheme = true) { BreathingScreen {} } }
@Preview(showBackground = true) @Composable fun MusicScreenPreview() { MindTheme(darkTheme = true) { MusicScreen {} } }
