# CANLight

Android app to manage chord patterns
- connects to Spotify (premium accound required) and YouTube.
- import chord patterns, tabs and lyrics from UltimateGuitar
- transpose and edit the chords
- auto scrolling
- sets the program on your Keyboard using MIDI to fit the current song

Building:
1. clone the project
2. create the file 
  `MyApplication/app/src/main/res/values/secrets.xml`
3. import the project in android-studio
4. compile and deploy!

Content of `secrets.xml`:
  ```<?xml version="1.0" encoding="utf-8"?>
  <resources>
    <string name="google_developer_key">xxx</string>
    <string name="spotify_client_secret">xxx</string>
    <string name="spotify_client_id">xxx</string>
  </resources>
  ```
replace the xxx with real values
