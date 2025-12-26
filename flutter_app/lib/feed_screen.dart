import 'package:flutter/material.dart';

class Post {
  final int id;
  final String user;
  final String avatarAsset;
  final String text;
  final String timestamp;
  final int likes;
  final bool youLiked;
  final List<String> tags;

  Post({
    required this.id,
    required this.user,
    required this.avatarAsset,
    required this.text,
    required this.timestamp,
    required this.likes,
    required this.youLiked,
    this.tags = const [],
  });

  Post copyWith({
    int? id,
    String? user,
    String? avatarAsset,
    String? text,
    String? timestamp,
    int? likes,
    bool? youLiked,
    List<String>? tags,
  }) {
    return Post(
      id: id ?? this.id,
      user: user ?? this.user,
      avatarAsset: avatarAsset ?? this.avatarAsset,
      text: text ?? this.text,
      timestamp: timestamp ?? this.timestamp,
      likes: likes ?? this.likes,
      youLiked: youLiked ?? this.youLiked,
      tags: tags ?? this.tags,
    );
  }
}

class FeedScreen extends StatefulWidget {
  final String currentUserAvatar;
  const FeedScreen({super.key, this.currentUserAvatar = 'assets/images/avatar_female_1.png'});

  @override
  State<FeedScreen> createState() => _FeedScreenState();
}

class _FeedScreenState extends State<FeedScreen> {
  final String currentUser = 'You';
  late List<Post> posts;
  String postText = '';
  String? selectedTopic;

  @override
  void initState() {
    super.initState();
    posts = [
      Post(
        id: 1,
        user: 'Sarah',
        avatarAsset: 'assets/images/avatar_female_2.png',
        text: 'Just finished a 10-minute meditation. Feeling calm.',
        timestamp: '10:30 AM',
        likes: 12,
        youLiked: false,
        tags: ['#Mindfulness'],
      ),
      Post(
        id: 2,
        user: 'John',
        avatarAsset: 'assets/images/avatar_male_1.png',
        text: 'Hit the gym today. Progress over perfection!',
        timestamp: '11:15 AM',
        likes: 34,
        youLiked: true,
        tags: ['#SelfCare'],
      ),
      Post(
        id: 3,
        user: currentUser,
        avatarAsset: widget.currentUserAvatar,
        text: 'Feeling grateful for the small things today.',
        timestamp: '11:45 AM',
        likes: 5,
        youLiked: true,
        tags: ['#Gratitude'],
      ),
    ];
  }

  @override
  Widget build(BuildContext context) {
    final filteredPosts = selectedTopic == null
        ? posts
        : posts.where((p) => p.tags.contains(selectedTopic)).toList();
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        CreatePostCard(
          text: postText,
          onTextChange: (v) => setState(() => postText = v),
          onPostClick: () {
            if (postText.trim().isNotEmpty) {
              setState(() {
                posts.insert(
                  0,
                  Post(
                    id: posts.length + 1,
                    user: currentUser,
                    avatarAsset: widget.currentUserAvatar,
                    text: postText,
                    timestamp: TimeOfDay.now().format(context),
                    likes: 0,
                    youLiked: false,
                    tags: postText.split(' ').where((w) => w.startsWith('#')).toList(),
                  ),
                );
                postText = '';
              });
            }
          },
        ),
        const SizedBox(height: 16),
        TrendingTopicsCard(
          selectedTopic: selectedTopic,
          onTopicClick: (topic) => setState(() => selectedTopic = topic),
        ),
        const SizedBox(height: 16),
        ...filteredPosts.map((post) => Padding(
              padding: const EdgeInsets.only(bottom: 16),
              child: PostCard(
                post: post,
                currentUser: currentUser,
                onLikeClicked: () {
                  setState(() {
                    final idx = posts.indexWhere((p) => p.id == post.id);
                    if (idx != -1) {
                      posts[idx] = post.copyWith(
                        likes: post.youLiked ? post.likes - 1 : post.likes + 1,
                        youLiked: !post.youLiked,
                      );
                    }
                  });
                },
                onDeleteClicked: () {
                  setState(() {
                    posts.removeWhere((p) => p.id == post.id);
                  });
                },
              ),
            )),
      ],
    );
  }
}

class CreatePostCard extends StatelessWidget {
  final String text;
  final ValueChanged<String> onTextChange;
  final VoidCallback onPostClick;
  const CreatePostCard({super.key, required this.text, required this.onTextChange, required this.onPostClick});

  @override
  Widget build(BuildContext context) {
    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          children: [
            TextField(
              decoration: const InputDecoration(hintText: 'Share your thoughts...'),
              onChanged: onTextChange,
              controller: TextEditingController(text: text),
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                const Spacer(),
                ElevatedButton(onPressed: onPostClick, child: const Text('Post')),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class TrendingTopicsCard extends StatelessWidget {
  final String? selectedTopic;
  final ValueChanged<String?> onTopicClick;
  const TrendingTopicsCard({super.key, required this.selectedTopic, required this.onTopicClick});

  @override
  Widget build(BuildContext context) {
    final topics = ['#Gratitude', '#SelfCare', '#Mindfulness'];
    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('Trending Topics', style: TextStyle(fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            Row(
              children: [
                GestureDetector(
                  onTap: () => onTopicClick(null),
                  child: Text(
                    'All',
                    style: TextStyle(
                      fontWeight: selectedTopic == null ? FontWeight.bold : FontWeight.normal,
                      color: Theme.of(context).colorScheme.primary,
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                ...topics.map((topic) => Padding(
                      padding: const EdgeInsets.only(right: 12),
                      child: GestureDetector(
                        onTap: () => onTopicClick(topic),
                        child: Text(
                          topic,
                          style: TextStyle(
                            fontWeight: selectedTopic == topic ? FontWeight.bold : FontWeight.normal,
                            color: Theme.of(context).colorScheme.primary,
                          ),
                        ),
                      ),
                    )),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class PostCard extends StatelessWidget {
  final Post post;
  final String currentUser;
  final VoidCallback onLikeClicked;
  final VoidCallback onDeleteClicked;
  const PostCard({super.key, required this.post, required this.currentUser, required this.onLikeClicked, required this.onDeleteClicked});

  @override
  Widget build(BuildContext context) {
    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                CircleAvatar(
                  radius: 20,
                  backgroundImage: AssetImage(post.avatarAsset),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(post.user, style: const TextStyle(fontWeight: FontWeight.bold)),
                ),
                Text(post.timestamp, style: const TextStyle(fontSize: 12)),
                if (post.user == currentUser)
                  IconButton(
                    icon: const Icon(Icons.delete),
                    onPressed: onDeleteClicked,
                  ),
              ],
            ),
            const SizedBox(height: 8),
            Text(post.text),
            Row(
              children: [
                IconButton(
                  icon: Icon(post.youLiked ? Icons.favorite : Icons.favorite_border),
                  onPressed: onLikeClicked,
                ),
                Text(post.likes.toString()),
                const Spacer(),
                const Icon(Icons.chat_bubble_outline),
                const SizedBox(width: 12),
                const Icon(Icons.share),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
