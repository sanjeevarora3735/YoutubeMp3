# YouTube to MP3 Conversion App

![Project Image](https://upload.wikimedia.org/wikipedia/commons/e/e1/Logo_of_YouTube_%282015-2017%29.svg)

> Brief description.
The YouTube to MP3 Conversion App is an Android application that allows users to easily convert YouTube videos into MP3 audio files. With a simple and intuitive interface, users can enter a YouTube video URL, select a destination folder, and initiate the conversion process. The app leverages a combination of Java and Python technologies to seamlessly handle the video conversion and audio extraction tasks.

## Table of Contents

- [About](#about)
- [Features](#features)
- [Getting Started](#getting-started)
- [Technologies](#technologies)

## About

The YouTube to MP3 Conversion App is an Android application developed using Java and Python. It provides a seamless way for users to convert YouTube videos into MP3 audio files. With a user-friendly interface, users can easily enter a YouTube video URL, select a destination folder, and initiate the conversion process.

## Features

- Input Stage:
  - User can provide a YouTube video URL and select a destination folder for the MP3 file.
- Grabbing Stage:
  - Utilizes Python-based yt-dlp library to fetch YouTube video information, including the title, thumbnail, number of videos, likes, and high-quality media stream containing the audio stream for the MP3 file.
- Download Stage:
  - Uses OkHTTP library to download the media stream file from YouTube's server into the user's mobile device.
  - Displays the video title, thumbnail, number of views, and likes.
- Convert Stage:
  - Leverages ffmpeg to convert the downloaded media stream file into an MP3 audio file.
  - Provides conversion progress updates to the user.
- Saving Stage:
  - Moves the processed MP3 file to the user-selected destination folder.
  - Displays the file moving progress.
- Success Screen:
  - Shown when the MP3 file is successfully processed and saved.
  - Includes a button to allow the user to convert another MP3 file.
- Failure Screen:
  - Displayed in case of any failures or errors during the MP3 file processing stage.
  - Provides an error message and a button to convert another MP3 file.

## Getting Started

To run the YouTube to MP3 Conversion App, follow these steps:

1. Clone the repository:

    git clone https://github.com/sanjeevarora3735/YoutubeMp3.git
    

2. Open the project in Android Studio or your preferred IDE.

3. Set up the required dependencies and configurations as per the IDE guidelines.

4. Build and run the app on an Android emulator or a physical device.

## Technologies

The YouTube to MP3 Conversion App utilizes the following technologies:

- **Java**: The primary language used for Android app development.
- **Python**: Utilized for interacting with the yt-dlp library to fetch YouTube video information.
- **yt-dlp**: A Python-based library for extracting video metadata from YouTube, including title, thumbnail, likes, and media stream containing the audio stream for the MP3 file.
- **OkHTTP**: A popular HTTP client library for Android used to download the media stream file from YouTube's server.
- **ffmpeg**: A powerful multimedia framework used for converting the downloaded media stream file into an MP3 audio file.

For more details, refer to the [YouTube to MP3 Conversion App GitHub Repository](https://github.com/sanjeevarora3735/YoutubeMp3).

---

Feel free to make further improvements to the README.md file, including adding screenshots, code snippets, or any other relevant information. The goal is to provide an informative and visually appealing document that effectively communicates the features and usage of your YouTube to MP3 Conversion App.
