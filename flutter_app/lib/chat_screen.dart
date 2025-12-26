import 'package:flutter/material.dart';
import 'dart:math';

class Message {
  final String id;
  final String text;
  final bool isFromUser;
  final String timestamp;

  Message({
    String? id,
    required this.text,
    required this.isFromUser,
    required this.timestamp,
  }) : id = id ?? UniqueKey().toString();
}

class ChatItem {
  final String id;
  final String name;
  final bool isAI;
  final List<Message> messages;

  ChatItem({
    String? id,
    required this.name,
    this.isAI = false,
    this.messages = const [],
  }) : id = id ?? UniqueKey().toString();
}

class ChatListScreen extends StatefulWidget {
  final List<ChatItem> chats;
  final void Function(ChatItem) onChatClick;
  final void Function(String, bool) onNewChat;
  const ChatListScreen({
    super.key,
    required this.chats,
    required this.onChatClick,
    required this.onNewChat,
  });

  @override
  State<ChatListScreen> createState() => _ChatListScreenState();
}

class _ChatListScreenState extends State<ChatListScreen> {
  bool showDialog = false;
  String search = '';

  @override
  Widget build(BuildContext context) {
    final filtered = widget.chats
        .where((c) => c.name.toLowerCase().contains(search.toLowerCase()))
        .toList();
    return Scaffold(
      appBar: AppBar(
        title: const Text('Messages', style: TextStyle(fontWeight: FontWeight.bold)),
        actions: [
          IconButton(
            icon: const Icon(Icons.add),
            onPressed: () => setState(() => showDialog = true),
          ),
        ],
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(12.0),
            child: TextField(
              decoration: const InputDecoration(labelText: 'Search'),
              onChanged: (v) => setState(() => search = v),
            ),
          ),
          Expanded(
            child: ListView(
              children: filtered
                  .map((chat) => ChatRow(
                        chat: chat,
                        onClick: () => widget.onChatClick(chat),
                      ))
                  .toList(),
            ),
          ),
        ],
      ),
      floatingActionButton: showDialog
          ? null
          : FloatingActionButton(
              onPressed: () => setState(() => showDialog = true),
              child: const Icon(Icons.add),
            ),
    );
  }
}

class ChatRow extends StatelessWidget {
  final ChatItem chat;
  final VoidCallback onClick;
  const ChatRow({super.key, required this.chat, required this.onClick});

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onClick,
      child: Padding(
        padding: const EdgeInsets.all(12.0),
        child: Row(
          children: [
            CircleAvatar(
              radius: 24,
              child: Text(
                chat.name.isNotEmpty ? chat.name[0].toUpperCase() : '?',
                style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(chat.name, style: const TextStyle(fontWeight: FontWeight.bold)),
                  Text(
                    chat.messages.isNotEmpty ? chat.messages.last.text : 'No messages yet',
                    maxLines: 1,
                    style: const TextStyle(color: Colors.grey),
                  ),
                ],
              ),
            ),
            Text(
              chat.messages.isNotEmpty ? chat.messages.last.timestamp : '',
              style: Theme.of(context).textTheme.bodySmall,
            ),
          ],
        ),
      ),
    );
  }
}

class ConversationScreen extends StatefulWidget {
  final ChatItem chat;
  final VoidCallback onBack;
  final void Function(String) onSendMessage;
  const ConversationScreen({
    super.key,
    required this.chat,
    required this.onBack,
    required this.onSendMessage,
  });

  @override
  State<ConversationScreen> createState() => _ConversationScreenState();
}

class _ConversationScreenState extends State<ConversationScreen> {
  final TextEditingController _controller = TextEditingController();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.chat.name),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: widget.onBack,
        ),
      ),
      body: Column(
        children: [
          Expanded(
            child: ListView(
              reverse: true,
              children: widget.chat.messages.reversed
                  .map((msg) => MessageBubble(message: msg))
                  .toList(),
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: _controller,
                    decoration: const InputDecoration(hintText: 'Type a message...'),
                  ),
                ),
                const SizedBox(width: 8),
                ElevatedButton(
                  onPressed: () {
                    if (_controller.text.trim().isNotEmpty) {
                      widget.onSendMessage(_controller.text.trim());
                      _controller.clear();
                    }
                  },
                  child: const Text('Send'),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class MessageBubble extends StatelessWidget {
  final Message message;
  const MessageBubble({super.key, required this.message});

  @override
  Widget build(BuildContext context) {
    final isUser = message.isFromUser;
    return Row(
      mainAxisAlignment: isUser ? MainAxisAlignment.end : MainAxisAlignment.start,
      children: [
        Card(
          color: isUser ? Theme.of(context).colorScheme.primaryContainer : Theme.of(context).colorScheme.secondaryContainer,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
          child: Padding(
            padding: const EdgeInsets.all(12.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                Text(message.text),
                Text(
                  message.timestamp,
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(color: Colors.grey),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}

class NewConversationDialog extends StatefulWidget {
  final void Function(String, bool) onCreate;
  final VoidCallback onDismiss;
  const NewConversationDialog({super.key, required this.onCreate, required this.onDismiss});

  @override
  State<NewConversationDialog> createState() => _NewConversationDialogState();
}

class _NewConversationDialogState extends State<NewConversationDialog> {
  String name = '';
  bool isGroup = false;

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('New Conversation'),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          TextField(
            decoration: const InputDecoration(labelText: 'Name'),
            onChanged: (v) => setState(() => name = v),
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              Expanded(
                child: ElevatedButton(
                  onPressed: () => setState(() => isGroup = false),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: !isGroup ? Theme.of(context).colorScheme.primary : Theme.of(context).colorScheme.surfaceVariant,
                  ),
                  child: const Text('Individual'),
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: ElevatedButton(
                  onPressed: () => setState(() => isGroup = true),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: isGroup ? Theme.of(context).colorScheme.primary : Theme.of(context).colorScheme.surfaceVariant,
                  ),
                  child: const Text('Group'),
                ),
              ),
            ],
          ),
        ],
      ),
      actions: [
        TextButton(onPressed: widget.onDismiss, child: const Text('Cancel')),
        ElevatedButton(
          onPressed: name.isNotEmpty ? () => widget.onCreate(name, isGroup) : null,
          child: const Text('Create'),
        ),
      ],
    );
  }
}
