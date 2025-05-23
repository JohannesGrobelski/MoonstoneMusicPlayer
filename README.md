# Moonstone Music Player

Moonstone Music Player is an Android application that allows users to search for, display, and play songs on their Android devices. It also provides the functionality to create and manage playlists, enabling users to curate their own personalized collections of songs.
Features

    Song Search: Search for songs by title, artist, or album.
    Song Display: View detailed information about songs, including title, artist, album, and duration.
    Song Playback: Play songs within the app using the built-in media player.
    Playlist Creation: Create and manage playlists to organize and categorize songs.
    Playlist Management: Add songs to playlists, rename playlists, delete playlists, and reorder songs within playlists.

Installation

    Clone the repository to your local machine using the following command:

    bash

    git clone https://github.com/your-username/moonstone-music-player.git

    Open the project in Android Studio.

    Build and run the project on an Android device or emulator.

    Add a api_keys.xml in the values folder in res. Add the api key like this:
    '''<?xml version="1.0" encoding="utf-8"?>
        <resources>
            <string name="api_key">your_actual_api_key_here</string>
        </resources>'''

Usage

    Launch the Moonstone Music Player app on your Android device.

    Use the search functionality to find songs by title, artist, or album.

    Tap on a song from the search results to view its detailed information.

    Press the play button to start playing the selected song using the built-in media player.

    To create a playlist, navigate to the playlist management section and select the "Create Playlist" option.

    Provide a name for the new playlist and tap on the create button.

    To add songs to a playlist, go to the song display section, select a song, and choose the "Add to Playlist" option. Select the desired playlist from the available options.

    To manage playlists, go to the playlist management section. From there, you can rename playlists, delete playlists, and reorder songs within playlists.


## Licensing

This project uses a dual licensing approach:

1. Original Code:
   - All rights reserved
   - Modifications and derivative works are not permitted
   - The code can only be used as provided

2. Third-Party Dependencies:
   - All dependencies maintain their original licenses (MIT and Apache 2.0)
   - See THIRD_PARTY_LICENSES file for detailed information

Please note that while you cannot modify the original code of MoonStone Music Player, 
the third-party libraries used in this project maintain their open-source licenses 
and can be used according to their respective terms.
