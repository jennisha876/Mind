import 'package:flutter/material.dart';

class ProfileScreen extends StatefulWidget {
  final bool darkTheme;
  final VoidCallback onToggleTheme;
  const ProfileScreen({super.key, required this.darkTheme, required this.onToggleTheme});

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> with SingleTickerProviderStateMixin {
  int selectedTab = 0;
  final tabs = ['Summary', 'Settings'];
  late TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: tabs.length, vsync: this);
    _tabController.addListener(() {
      setState(() {
        selectedTab = _tabController.index;
      });
    });
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        TabBar(
          controller: _tabController,
          tabs: tabs.map((title) => Tab(text: title)).toList(),
        ),
        Expanded(
          child: IndexedStack(
            index: selectedTab,
            children: [
              ProfileSummaryTab(),
              ProfileSettingsTab(
                darkTheme: widget.darkTheme,
                onToggleTheme: widget.onToggleTheme,
              ),
            ],
          ),
        ),
      ],
    );
  }
}

class ProfileSummaryTab extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Profile Summary',
            style: Theme.of(context).textTheme.headlineMedium,
          ),
          const SizedBox(height: 16),
          Card(
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Text('Heart Rate Chart (Placeholder)', style: Theme.of(context).textTheme.bodyLarge),
            ),
          ),
          const SizedBox(height: 16),
          Card(
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Text('Mood Pie Chart (Placeholder)', style: Theme.of(context).textTheme.bodyLarge),
            ),
          ),
        ],
      ),
    );
  }
}

class ProfileSettingsTab extends StatelessWidget {
  final bool darkTheme;
  final VoidCallback onToggleTheme;
  const ProfileSettingsTab({required this.darkTheme, required this.onToggleTheme});

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(16.0),
      children: [
        Text(
          'Settings',
          style: Theme.of(context).textTheme.headlineMedium,
        ),
        const SizedBox(height: 16),
        Row(
          children: [
            const Expanded(child: Text('Dark Mode')),
            Switch(
              value: darkTheme,
              onChanged: (_) => onToggleTheme(),
            ),
          ],
        ),
        const SizedBox(height: 16),
        ElevatedButton(
          onPressed: () => throw Exception('Test Crash'),
          child: const Text('Test Crash'),
        ),
      ],
    );
  }
}
