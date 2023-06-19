package com.sanjeev.youtubemp3;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 1;
    private static final int REQUEST_CODE_FOLDER_SELECTION = 2;
    private static boolean DownloadedConfirmed = false;
    private TextInputLayout YoutubeLink;
    private View DownloadButtonView;
    private ProgressBar GrabbingProgressBar;
    private TextView GrabbingTextView;
    private String FileName = "";
    private Handler handler;
    private ConstraintLayout GrabbingConstraintLayout, DownloadingConstraintLayout;
    private LinearProgressIndicator DownloadingProgressbar;

    private String DownloadUrl = "";
    private Uri DestinationPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the default night mode to disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Set the layout for the activity
        setContentView(R.layout.activity_main);

        // Log the path to external storage directory
        Log.d("FFMPEG", Environment.getExternalStorageDirectory().getPath());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Log the absolute path to external storage directory for Android 11 and above
            Log.d("FFMPEG", Environment.getExternalStorageDirectory().getAbsolutePath());
        }

        // Request necessary permissions
        requestpermission();

        // Check and request required permissions
        checkPermissions();

        // Initialize UI elements
        handler = new Handler(Looper.getMainLooper());
        DownloadButtonView = findViewById(R.id.DownloadButton);
        GrabbingProgressBar = DownloadButtonView.findViewById(R.id.progressBar);
        GrabbingTextView = DownloadButtonView.findViewById(R.id.buttonText);
        GrabbingConstraintLayout = findViewById(R.id.GrabbingConstraintLayout);
        DownloadingConstraintLayout = findViewById(R.id.DownloadingConstraintLayout);
        DownloadingProgressbar = findViewById(R.id.DownloadingProgressbar);
        YoutubeLink = findViewById(R.id.YoutubeLink);

        // Start Python if not already started
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        try {
            // Set click listener for the destination folder edit text
            ((TextInputEditText) (findViewById(R.id.DestinationFolderEditText))).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Open folder picker when clicked
                    if (checkPermissions()) {
                        openFolderPicker();
                    } else if (checkPermissions()) {
                        openFolderPicker();
                    } else {
                        openFolderPicker();
                    }
                }
            });

            // Set click listener for the download button
            (DownloadButtonView).setOnClickListener(v -> {
                // Activate progress bar and initiate video grabbing process
                ActivateProgressBar();
                String videoUrl = null;
                boolean errorfree = true;

                try {
                    // Get the video URL
                    videoUrl = getVideoUrl();
                } catch (Exception e) {
                    EnableUserInteraction();
                    errorfree = false;
                    ((TextView) findViewById(R.id.Error)).setText(e.getMessage());
                    DeactivateProgressBar();
                }

                if (errorfree) {
                    String finalVideoUrl = videoUrl;
                    new Thread(() -> {
                        grabVideoInfo(finalVideoUrl);
                    }).start();
                }
            });
        } catch (Exception e) {
            Log.d("Error", e.getMessage());
        }
    }


    private void createFolderInSelectedDirectory(Uri directoryUri) throws FileNotFoundException {
        // You can create a new folder within the selected directory here
        // For example:
        String folderName = "YoutubeMp3";

        // Extract the document ID from the URI
        String documentId = DocumentsContract.getTreeDocumentId(directoryUri);

        // Build the URI for the new folder
        Uri newFolderUri = DocumentsContract.buildDocumentUriUsingTree(directoryUri, documentId);

        // Create a new document with the given folder name
        DestinationPath = DocumentsContract.createDocument(getContentResolver(), newFolderUri, DocumentsContract.Document.MIME_TYPE_DIR, folderName);

    }


    private void EnableUserInteraction() {
        ((TextInputEditText) (findViewById(R.id.DestinationFolderEditText))).setEnabled(true);
        ((TextInputEditText) (findViewById(R.id.YoutubeLinkEditText))).setEnabled(true);
        DownloadButtonView.setEnabled(true);
    }

    private void openFolderPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        startActivityForResult(intent, REQUEST_CODE_FOLDER_SELECTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_FOLDER_SELECTION && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    try {
                        createFolderInSelectedDirectory(uri);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    String folderPath = uri.getPath(); // Retrieve the selected folder path
                    ((TextInputEditText) (findViewById(R.id.DestinationFolderEditText))).setText(folderPath);
                }
            }
        }
    }

    private void ActivateProgressBar() {
        handler.post(() -> {
            GrabbingProgressBar.setVisibility(View.VISIBLE);
            GrabbingTextView.setText("Grabbing...");
            ConstraintLayout constraintLayout = findViewById(R.id.ConstraintLayout);
            constraintLayout.setBackgroundColor(Color.parseColor("#9D7CC8"));
            DisableUserInteraction();
        });

    }

    private void DisableUserInteraction() {
        ((TextInputEditText) (findViewById(R.id.DestinationFolderEditText))).setEnabled(false);
        ((TextInputEditText) (findViewById(R.id.YoutubeLinkEditText))).setEnabled(false);
        DownloadButtonView.setEnabled(false);
    }


    private String getVideoUrl() throws Exception {

        String videoUrl = ((TextInputEditText) findViewById(R.id.YoutubeLinkEditText)).getText().toString();
        // Validating the videoUrl
        if (videoUrl.isEmpty()) {
            throw new Exception("Invalid YouTube video URL. Please provide a valid URL.");
        }
        return videoUrl;
    }

    private void grabVideoInfo(String videoUrl) {
        Python py = Python.getInstance();
        PyObject module = py.getModule("grabber");
        try {
            PyObject result = module.callAttr("get_video_info", videoUrl);
            Log.d("MainActivity", result.toString());
            JSONObject jsonObject = new JSONObject(result.toString());
            String title = jsonObject.getString("Title");
            FileName = title.replace(",", "").replace(" ", "_").replace("|", " ").replace("—", " ").replace("+", " ").trim() + ".mp4";
            int views = jsonObject.getInt("Views");
            Log.d("FileName", FileName);
            int likes = jsonObject.getInt("Likes");
            String error = jsonObject.getString("Error");
            String uploader = jsonObject.getString("Uploader");
            String channelUrl = jsonObject.getString("Channel URL");
            String thumbnail = jsonObject.getString("High Quality Thumbnail");
            handler.post(() -> {
                DeactivateProgressBar();
                DownloadedConfirmed = true;
                MoveToDownloadingPanel(title, thumbnail, likes, views, videoUrl);
            });
        } catch (PyException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void MoveToDownloadingPanel(String title, String Thumbnail, int Likes, int Views, String videourl) {
        DownloadingConstraintLayout.setVisibility(View.VISIBLE);
        GrabbingConstraintLayout.setVisibility(View.INVISIBLE);
        ImageView ThumbnailImageView = findViewById(R.id.Thumbnail);
        TextView Title = findViewById(R.id.Title);
        Title.setText(title);
        TextView LikeCount = findViewById(R.id.LikeCount);
        if (Likes > 1000) {
            Likes /= 1000;
            LikeCount.setText(Likes + "k Likes");
        } else {
            LikeCount.setText(Likes + " Likes");
        }

        TextView ViewsCount = findViewById(R.id.ViewsCount);
        if (Views > 1000) {
            Views /= 1000;
            ViewsCount.setText(Views + "k Views");
        } else {
            ViewsCount.setText(Views + " Views");
        }
        try {
            Picasso.get().load(Thumbnail).into(ThumbnailImageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
        DownloadTheFile(videourl);

    }

    private void DownloadTheFile(String videoUrl) {
        Python py = Python.getInstance();
        PyObject module = py.getModule("grabber");
        try {
            PyObject result = module.callAttr("get_download_url", videoUrl);
            DownloadUrl = result.toString();
            startDownloadTask();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void requestpermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE}, 1);
    }


    private boolean checkPermissions() {
        // Check if the necessary permissions are granted
        boolean writeExternalStoragePermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean readExternalStoragePermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean manageExternalStoragePermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return manageExternalStoragePermissionGranted && writeExternalStoragePermissionGranted && readExternalStoragePermissionGranted;
    }

    private void DeactivateProgressBar() {
        handler.post(() -> {
            GrabbingProgressBar.setVisibility(View.INVISIBLE);
            GrabbingTextView.setText("Download");
            ConstraintLayout constraintLayout = findViewById(R.id.ConstraintLayout);
            constraintLayout.setBackgroundColor(Color.parseColor("#892EFF"));
            EnableUserInteraction();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            // Check if all permissions are granted
            boolean allPermissionsGranted = true;

            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                // All permissions granted, proceed with your code
                // Start downloading or perform other operations
//                Toast.makeText(this, "Permissions are granted :)", Toast.LENGTH_SHORT).show();

            } else {
                // Permissions not granted, handle accordingly (e.g., show a message or exit the app)
                // You can display a message explaining the need for permissions and exit the app or handle it in your desired way
//                Toast.makeText(this, "Permissions are not granted :(", Toast.LENGTH_SHORT).show();
                requestpermission();
            }
        }
    }

    private void startDownloadTask() {
        DownloadingProgressbar.setIndicatorColor(Color.parseColor("#3690FA"));
        // Start your DownloadTask here
        DownloadTask downloadTask = new DownloadTask(DestinationPath, FileName);
        downloadTask.execute(DownloadUrl);
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
//        Toast.makeText(this, "DownloadUrl copied to clipboard", Toast.LENGTH_SHORT).show();
    }


    private void DownloadFailed(String contextmessage) {
        ((TextView) findViewById(R.id.progressingstatus)).setText(contextmessage);
        DownloadingProgressbar.setIndicatorColor(Color.RED);
        DownloadingProgressbar.setProgress(100);
    }

    private void ConvertingStart(int pro) {
        ((TextView) findViewById(R.id.progressingstatus)).setText("Converting ...");
        DownloadingProgressbar.setIndicatorColor(Color.parseColor("#FFBB0E"));
        DownloadingProgressbar.setProgress(pro);
    }

    private void SavingState(String filePath) {

        File file = new File(filePath);
        try {
            boolean deleted = file.delete();
            if (deleted) {
                ((TextView) findViewById(R.id.progressingstatus)).setText("Success");
                DownloadingProgressbar.setIndicatorColor(Color.parseColor("#72BD6C"));
                DownloadingProgressbar.setProgress(100);

            } else {
                ((TextView) findViewById(R.id.progressingstatus)).setText("Failed");
                DownloadingProgressbar.setIndicatorColor(Color.parseColor("#DC4A4A"));
                DownloadingProgressbar.setProgress(100);
            }
        } catch (SecurityException e) {
            Log.e("FileDeletion", "SecurityException: " + e.getMessage());
        } catch (Exception e) {
            Log.e("FileDeletion", "Exception: " + e.getMessage());
        }

    }

    private class DownloadTask extends AsyncTask<String, Integer, Boolean> {
        private final Uri destinationUri;
        private final String fileName;
        private long fileSize;
        private Uri Finalmp4Uri;

        public DownloadTask(Uri destinationUri, String fileName) {
            this.destinationUri = destinationUri;
            this.fileName = fileName;
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(strings[0]).build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    fileSize = response.body().contentLength();
                    long downloadedSize = 0;
                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    String folderName = FileName.replace(".mp4", "");
                    Uri newFolderUri = DocumentsContract.createDocument(getContentResolver(), destinationUri, DocumentsContract.Document.MIME_TYPE_DIR, folderName);

                    Uri outputFileUri = DocumentsContract.createDocument(getContentResolver(), newFolderUri, null, fileName);

                    Finalmp4Uri = outputFileUri;

                    if (outputFileUri != null) {
                        OutputStream outputStream = getContentResolver().openOutputStream(outputFileUri);

                        if (outputStream != null) {
                            while ((bytesRead = response.body().byteStream().read(buffer)) != -1) {
                                downloadedSize += bytesRead;
                                publishProgress((int) (downloadedSize * 100 / fileSize));
                                outputStream.write(buffer, 0, bytesRead);
                            }

                            outputStream.flush();
                            outputStream.close();
                            response.body().close();

                            return true;
                        } else {
                            Log.e("DownloadTask", "Failed to open output stream for file: " + fileName);
                            return false;
                        }
                    } else {
                        Log.e("DownloadTask", "Failed to create document for file: " + fileName);
                        return false;
                    }
                } else {
                    // Response was not successful
                    Log.e("DownloadTask", "Response not successful. Code: " + response.code());
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("DownloadTask", "Exception occurred: " + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int progress = values[0];
            DownloadingProgressbar.setProgress(progress);

            // Calculate the downloaded file size in bytes
            long downloadedSize = (progress * fileSize) / 100;
            String progressText = String.format(Locale.getDefault(), "%.2f MB / %.2f MB", downloadedSize / (1024.0 * 1024.0), fileSize / (1024.0 * 1024.0));

            // Update the progress text or UI element with the downloaded file size and total file size
            ((TextView) (findViewById(R.id.progressingtext))).setText(progressText);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                // File download completed, perform any necessary actions
                Log.d("DownloadTask", "File download completed!");
                Log.d("DownloadTask", Finalmp4Uri.toString());
//                Toast.makeText(MainActivity.this, Finalmp4Uri.toString(), Toast.LENGTH_SHORT).show();
                // You can also check if the downloaded file exists at the specified destination

                ConvertingStart(0);
                ((TextView) (findViewById(R.id.progressingtext))).setText("");


                new Handler().postDelayed(() -> {
                    // Your code here
                    String InputPath = Environment.getExternalStorageDirectory() + "/" + Finalmp4Uri.getPath().split(":")[2];
                    String OutputPath = "";
                    int lastSlashIndex = InputPath.lastIndexOf('/');
                    if (lastSlashIndex != -1) {
                        OutputPath = InputPath.substring(0, lastSlashIndex + 1);
                    }
                    OutputPath += FileName.replace(".mp4", ".mp3");
                    if (FFmpegHelper.convertMp4ToMp3(InputPath, OutputPath)) {
                        ConvertingStart(100);
                        new Handler().postDelayed(() -> SavingState(InputPath), 3000); // Delay of 3 seconds (3000 milliseconds)
                    } else {
                        DownloadFailed("Conversion Failed");
                    }
                }, 5000);

                // Note: The output file Uri can be used directly without conversion to File
                if (DocumentsContract.getDocumentId(destinationUri) != null) {
                    Log.d("DownloadTask", "Downloaded file exists at: " + destinationUri.toString());
                } else {
                    Log.e("DownloadTask", "Downloaded file does not exist");
                }
            } else {
                // File download failed

                Log.e("DownloadTask", "File download failed!");
                DownloadFailed("Download Failed");
            }
        }
    }


}