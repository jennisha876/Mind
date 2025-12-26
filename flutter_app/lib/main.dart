import 'feed_screen.dart';

void main() {
    runApp(const MindApp());
}

class MindApp extends StatelessWidget {
    const MindApp({super.key});

    @override
    Widget build(BuildContext context) {
        return MaterialApp(
            title: 'MindScape',
            theme: ThemeData.light(),
            darkTheme: ThemeData.dark(),
            home: const MindScapeApp(),
        );
    }
}

class MindScapeApp extends StatefulWidget {
    const MindScapeApp({super.key});

    @override
    State<MindScapeApp> createState() => _MindScapeAppState();
}

class _MindScapeAppState extends State<MindScapeApp> {
    String screen = 'splash';
    bool darkTheme = true;

    @override
    Widget build(BuildContext context) {
        switch (screen) {
            case 'splash':
                return SplashScreen(onDone: () => setState(() => screen = 'auth'));
            case 'auth':
                return AuthScreen(
                    onLogin: () => setState(() => screen = 'questions'),
                    onSignup: () => setState(() => screen = 'questions'),
                );
            case 'questions':
                return QuestionnaireScreen(onFinished: () => setState(() => screen = 'avatar'));
            case 'avatar':
                return AvatarScreen(onDone: () => setState(() => screen = 'home'));
            case 'home':
                return HomeScreen();
            default:
                return const SizedBox();
        }
    }
}

class SplashScreen extends StatefulWidget {
    final VoidCallback onDone;
    const SplashScreen({super.key, required this.onDone});

    @override
    State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen> {
    @override
    void initState() {
        super.initState();
        Future.delayed(const Duration(milliseconds: 1500), widget.onDone);
    }

    @override
    Widget build(BuildContext context) {
        return Scaffold(
            body: Center(
                child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: const [
                        Text('MindScape', style: TextStyle(fontSize: 36, fontWeight: FontWeight.bold)),
                        Text('Heal • Grow • Connect'),
                    ],
                ),
            ),
        );
    }
}

class AuthScreen extends StatefulWidget {
    final VoidCallback onLogin;
    final VoidCallback onSignup;
    const AuthScreen({super.key, required this.onLogin, required this.onSignup});

    @override
    State<AuthScreen> createState() => _AuthScreenState();
}

class _AuthScreenState extends State<AuthScreen> {
    bool isLogin = true;
    String username = '';
    String email = '';
    String password = '';

    @override
    Widget build(BuildContext context) {
        return Scaffold(
            body: Center(
                child: SingleChildScrollView(
                    padding: const EdgeInsets.all(24),
                    child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        crossAxisAlignment: CrossAxisAlignment.center,
                        children: [
                            Row(
                                mainAxisAlignment: MainAxisAlignment.center,
                                children: [
                                    GestureDetector(
                                        onTap: () => setState(() => isLogin = true),
                                        child: Padding(
                                            padding: const EdgeInsets.all(8.0),
                                            child: Text('Login', style: TextStyle(fontWeight: isLogin ? FontWeight.bold : FontWeight.normal)),
                                        ),
                                    ),
                                    const SizedBox(width: 16),
                                    GestureDetector(
                                        onTap: () => setState(() => isLogin = false),
                                        child: Padding(
                                            padding: const EdgeInsets.all(8.0),
                                            child: Text('Sign Up', style: TextStyle(fontWeight: !isLogin ? FontWeight.bold : FontWeight.normal)),
                                        ),
                                    ),
                                ],
                            ),
                            const SizedBox(height: 16),
                            TextField(
                                decoration: const InputDecoration(labelText: 'Username'),
                                onChanged: (v) => setState(() => username = v),
                            ),
                            if (!isLogin) ...[
                                const SizedBox(height: 8),
                                TextField(
                                    decoration: const InputDecoration(labelText: 'Email'),
                                    onChanged: (v) => setState(() => email = v),
                                ),
                            ],
                            const SizedBox(height: 8),
                            TextField(
                                decoration: const InputDecoration(labelText: 'Password'),
                                obscureText: true,
                                onChanged: (v) => setState(() => password = v),
                            ),
                            const SizedBox(height: 16),
                            ElevatedButton(
                                onPressed: () => isLogin ? widget.onLogin() : widget.onSignup(),
                                child: Text(isLogin ? 'Login' : 'Sign Up'),
                            ),
                        ],
                    ),
                ),
            ),
        );
    }
}

class Question {
    final String text;
    final List<String> options;
    final bool multi;
    const Question(this.text, this.options, this.multi);
}

const QUESTIONS = [
    Question('Why are you using Mindscape?', ['Stress', 'Anxiety', 'Mood', 'Support'], true),
    Question('What motivated you to seek a mental health app?', ['Stress relief', 'Anxiety management', 'Mood tracking', 'Community support', 'Other'], true),
    Question('How often do you feel stressed or anxious?', ['Rarely', 'Sometimes', 'Often', 'Almost always'], false),
    Question('What are your main goals for using Mindscape?', ['Reduce anxiety', 'Improve mood', 'Build healthy habits', 'Talking to someone', 'Listening to music', 'Other'], false),
    Question('How do you usually cope with stress?', ['Exercise', 'Meditation', 'Talking to friends/family', 'Professional help', 'Other'], true),
    Question('How would you rate your current mental health?', ['Excellent', 'Good', 'Fair', 'Poor'], false),
    Question('What time of day do you feel most stressed?', ['Morning', 'Afternoon', 'Evening', 'Night'], false),
    Question('Which features are you most interested in?', ['Guided meditations', 'Mood tracking', 'Community forums', 'AI chat support', 'Daily tips'], true),
    Question('Do you have previous experience with mental health apps?', ['Yes', 'No'], false),
    Question('Would you like to add anything else?', ['No, nothing', 
        'Yes, I\'ll share later'], false),
];

class QuestionnaireScreen extends StatefulWidget {
    final VoidCallback onFinished;
    const QuestionnaireScreen({super.key, required this.onFinished});

    @override
    State<QuestionnaireScreen> createState() => _QuestionnaireScreenState();
}

class _QuestionnaireScreenState extends State<QuestionnaireScreen> {
    int step = 0;
    List<String> selected = [];

    @override
    Widget build(BuildContext context) {
        final question = QUESTIONS[step];
        return Scaffold(
            body: Padding(
                padding: const EdgeInsets.all(24),
                child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                        Text('Question ${step + 1} of ${QUESTIONS.length}', style: const TextStyle(fontWeight: FontWeight.bold)),
                        const SizedBox(height: 8),
                        Text(question.text, style: const TextStyle(fontSize: 18)),
                        const SizedBox(height: 16),
                        ...question.options.map((option) => InkWell(
                                    onTap: () {
                                        setState(() {
                                            if (question.multi) {
                                                if (selected.contains(option)) {
                                                    selected.remove(option);
                                                } else {
                                                    selected.add(option);
                                                }
                                            } else {
                                                selected = [option];
                                            }
                                        });
                                    },
                                    child: Row(
                                        children: [
                                            question.multi
                                                    ? Checkbox(
                                                            value: selected.contains(option),
                                                            onChanged: (_) {
                                                                setState(() {
                                                                    if (selected.contains(option)) {
                                                                        selected.remove(option);
                                                                    } else {
                                                                        selected.add(option);
                                                                    }
                                                                });
                                                            },
                                                        )
                                                    : Radio<bool>(
                                                            value: true,
                                                            groupValue: selected.contains(option),
                                                            onChanged: (_) {
                                                                setState(() {
                                                                    selected = [option];
                                                                });
                                                            },
                                                        ),
                                            const SizedBox(width: 8),
                                            Text(option),
                                        ],
                                    ),
                                )),
                        const Spacer(),
                        ElevatedButton(
                            onPressed: selected.isNotEmpty
                                    ? () {
                                            if (step < QUESTIONS.length - 1) {
                                                setState(() {
                                                    step++;
                                                    selected = [];
                                                });
                                            } else {
                                                widget.onFinished();
                                            }
                                        }
                                    : null,
                            child: Text(step < QUESTIONS.length - 1 ? 'Next' : 'Finish'),
                            style: ElevatedButton.styleFrom(minimumSize: const Size.fromHeight(48)),
                        ),
                    ],
                ),
            ),
        );
    }
}

const AVATARS = [
    'assets/images/avatar_female_1.png',
    'assets/images/avatar_female_2.png',
    'assets/images/avatar_female_3.png',
    'assets/images/avatar_male_1.png',
    'assets/images/avatar_male_2.png',
    'assets/images/avatar_male_3.png',
];

class AvatarScreen extends StatefulWidget {
    final VoidCallback onDone;
    const AvatarScreen({super.key, required this.onDone});

    @override
    State<AvatarScreen> createState() => _AvatarScreenState();
}

class _AvatarScreenState extends State<AvatarScreen> {
    int? selectedAvatar;

    @override
    Widget build(BuildContext context) {
        return Scaffold(
            body: Padding(
                padding: const EdgeInsets.all(24),
                child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                        const Text('Choose Your Avatar', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
                        const SizedBox(height: 16),
                        Expanded(
                            child: GridView.count(
                                crossAxisCount: 3,
                                crossAxisSpacing: 8,
                                mainAxisSpacing: 8,
                                children: List.generate(AVATARS.length, (index) {
                                    final avatar = AVATARS[index];
                                    return GestureDetector(
                                        onTap: () => setState(() => selectedAvatar = index),
                                        child: Container(
                                            decoration: BoxDecoration(
                                                border: Border.all(
                                                    color: selectedAvatar == index ? Theme.of(context).colorScheme.primary : Colors.transparent,
                                                    width: 3,
                                                ),
                                                borderRadius: BorderRadius.circular(16),
                                            ),
                                            child: Image.asset(avatar, fit: BoxFit.cover),
                                        ),
                                    );
                                }),
                            ),
                        ),
                        ElevatedButton(
                            onPressed: selectedAvatar != null ? widget.onDone : null,
                            child: const Text('Continue'),
                            style: ElevatedButton.styleFrom(minimumSize: const Size.fromHeight(48)),
                        ),
                    ],
                ),
            ),
        );
    }
}

class HomeScreen extends StatelessWidget {
    @override
    Widget build(BuildContext context) {
        return Scaffold(
            body: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                        Text('${getGreeting()}, User 👋', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 24)),
                        const SizedBox(height: 16),
                        const Text('Welcome to your MindScape home!'),
                        const SizedBox(height: 8),
                        const Text('Here will be your feed, featured rooms, and wellness resources...'),
                    ],
                ),
            ),
        );
    }
}

String getGreeting() {
    final hour = DateTime.now().hour;
    if (hour >= 5 && hour <= 11) return 'Good morning';
    if (hour >= 12 && hour <= 16) return 'Good afternoon';
    if (hour >= 17 && hour <= 20) return 'Good evening';
    return 'Hello';
}


