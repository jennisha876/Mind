package com.example.mind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

data class Question(
    val text: String,
    val options: List<String>,
    val multi: Boolean
)

val QUESTIONS = listOf(
    Question("Why are you using Mindscape?", listOf("Stress", "Anxiety", "Mood", "Support"), true),
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

val AVATARS = listOf(
    R.drawable.avatar_female_1,
    R.drawable.avatar_female_2,
    R.drawable.avatar_female_3,
    R.drawable.avatar_male_1,
    R.drawable.avatar_male_2,
    R.drawable.avatar_male_3
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MindTheme {
                MindScapeApp()
            }
        }
    }
}

@Composable
fun MindTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
        typography = Typography(),
        content = content
    )
}

@Composable
fun MindScapeApp() {
    var screen by remember { mutableStateOf("splash") }

    when (screen) {
        "splash" -> SplashScreen { screen = "auth" }
        "auth" -> AuthScreen(onLogin = { screen = "questions" }, onSignup = { screen = "questions" })
        "questions" -> QuestionnaireScreen { screen = "avatar" }
        "avatar" -> AvatarScreen { screen = "home" }
        "home" -> HomeScreen()
    }
}

@Composable
fun SplashScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) { delay(1500); onDone() }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
        Row {
            Text("Login", modifier = Modifier.clickable { isLogin = true }.padding(8.dp), fontWeight = if (isLogin) FontWeight.Bold else FontWeight.Normal)
            Spacer(Modifier.width(16.dp))
            Text("Sign Up", modifier = Modifier.clickable { isLogin = false }.padding(8.dp), fontWeight = if (!isLogin) FontWeight.Bold else FontWeight.Normal)
        }

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(username, { username = it }, label = { Text("Username") })
        if (!isLogin) { Spacer(Modifier.height(8.dp)); OutlinedTextField(email, { email = it }, label = { Text("Email") }) }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(password, { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation())
        Spacer(Modifier.height(16.dp))
        Button(onClick = { if (isLogin) onLogin() else onSignup() }) { Text(if (isLogin) "Login" else "Sign Up") }
    }
}

@Composable
fun QuestionnaireScreen(onFinished: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<List<String>>(emptyList()) }
    val question = QUESTIONS[step]

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Question ${step + 1} of ${QUESTIONS.size}", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(question.text, fontSize = 18.sp)
        Spacer(Modifier.height(16.dp))

        question.options.forEach { option ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable {
                    selected = if (question.multi) {
                        if (selected.contains(option)) selected - option else selected + option
                    } else listOf(option)
                }.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (question.multi) Checkbox(selected.contains(option), null) else RadioButton(selected.contains(option), null)
                Spacer(Modifier.width(8.dp))
                Text(option)
            }
        }

        Spacer(Modifier.weight(1f))
        Button(onClick = {
            if (step < QUESTIONS.lastIndex) { step++; selected = emptyList() } else { onFinished() }
        }, enabled = selected.isNotEmpty(), modifier = Modifier.fillMaxWidth()) {
            Text(if (step < QUESTIONS.lastIndex) "Next" else "Finish")
        }
    }
}

@Composable
fun AvatarScreen(onDone: () -> Unit) {
    var selectedAvatar by remember { mutableStateOf<Int?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Choose Your Avatar", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        LazyVerticalGrid(columns = GridCells.Fixed(3), contentPadding = PaddingValues(8.dp), modifier = Modifier.weight(1f)) {
            items(AVATARS.size) { index ->
                val avatar = AVATARS[index]
                Box(
                    modifier = Modifier.padding(8.dp).clickable { selectedAvatar = avatar }.background(
                        if (selectedAvatar == avatar) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(painter = painterResource(avatar), contentDescription = "Avatar", modifier = Modifier.size(90.dp))
                }
            }
        }

        Button(onClick = onDone, enabled = selectedAvatar != null, modifier = Modifier.fillMaxWidth()) { Text("Continue") }
    }
}

@Composable
fun HomeScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("${getGreeting()}, User 👋", fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Spacer(Modifier.height(16.dp))
        Text("Welcome to your MindScape home!")
        Spacer(Modifier.height(8.dp))
        Text("Here will be your feed, featured rooms, and wellness resources...")
    }
}

fun getGreeting(): String {
    val hour = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
    return when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..20 -> "Good evening"
        else -> "Hello"
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMindScapeApp() {
    MindTheme {
        MindScapeApp()
    }
}
