package com.sanjeev.youtubemp3;

import android.util.Log;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;

public class FFmpegHelper {
    public static boolean convertMp4ToMp3(String inputPath, String outputPath) {
        // FFmpeg command for converting mp4 to mp3
        String[] cmd = {"-i", inputPath, "-vn", "-ar", "44100", "-ac", "2", "-b:a", "192k", outputPath};

        // Execute the FFmpeg command
        int rc = FFmpeg.execute(cmd);

        if (rc == Config.RETURN_CODE_SUCCESS) {
            // Conversion successful
            Log.d("FFmpegHelper", "Conversion Done");
            return true;
        } else {
            // Conversion failed
            Log.d("FFmpegHelper", "Conversion Failed");
            return false;
        }
    }
}
