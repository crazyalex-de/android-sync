# android-sync
A short tool to sync a video playing on multiple android devices.

This tool helps to show a splitted video in sync on different android devices (e.g. for a videowall).

Functions:
- Play video in loop
- Ensure ntp sync at startup (within the first 5 minutes after boot)
- Choose video to play or set a default autorun video
- Re-Sync after 10 video plays

How it's processing:

Once the time is set to ntp - to ensure, turn ntp off and on to ensure android syncs immediatelly - the video starts playing at the next fill minute.
If the time is set on all devices to the same, the video starts the same time on all devices.

With that, no further network connection is needed and the devices can run standalone.

For my needs, this small tool fits well, feel free to enhace or change what ever is suitable for you.

