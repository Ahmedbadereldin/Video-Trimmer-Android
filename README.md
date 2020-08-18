# Video-Trimmer-Android #
Trim the video by adjusting starting point and ending point in Android.


<a target="_blank" href="LICENSE"><img src="https://img.shields.io/badge/licence-MIT-brightgreen.svg" alt="license : MIT"></a>
<a target="_blank" href="https://www.cmarix.com/android-application-development-services.html"><img src="https://img.shields.io/badge/platform-android-blue.svg" alt="license : MIT"></a>
[![](https://jitpack.io/v/Ahmedbadereldin/Video-Trimmer-Android.svg)](https://jitpack.io/#Ahmedbadereldin/Video-Trimmer-Android)

## Screen Shots ##

<p align="center">
<img src="screenshots/sample.gif" width="270" height="500" /> &nbsp;&nbsp;
</p>

## Core Features ##

 - Video can be trimmed/shortened and played on the same screen
 - Video can be trimmed by selecting the starting point and ending point, and it will display the video size and video duration based on the selected position
 - Seekbar moves as per selected video for trimming
 - You can specify the length of the video before cut.

## How it works ##

 - User can record video from camera or select video from gallery
 - Set the selected video in the trimming screen
 - Trim the video by dragging starting point and end point
 - View trimmed video on the trimming screen
 - Save video, it will show image from video in next screen.
 - You can use the video after that as you like, like upload to server.

## Purpose of this code ##

 - Whenever it is required to crop thr video, this code can help you
 - Whenever you are having a limiation of video recording such as allow users to record video for 1 min, this code can help you

## Gradle ##
```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

```
dependencies {
    ...
	implementation 'com.github.Ahmedbadereldin:Video-Trimmer-Android:1.0.1'
}
```

## Setup your code ##

 - In order to use the library without problems, add these codes.
   
 * AndroidManifest.xml 
 ```
     <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
     <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
     <uses-permission android:name="android.permission.CAMERA" />
 
     <application
        ...
         android:requestLegacyExternalStorage="true"
        ...
     >

    ...

    <activity
            android:name=".YourActivity"
            android:configChanges="orientation|screenSize"
            android:launchMode="singleTask"
            android:screenOrientation="portrait"
            android:windowSoftInputMode="adjustPan">
            <intent-filter android:label="@string/app_name">
                <action android:name="android.intent.action.SEND" />
                <category android:name="android.intent.category.DEFAULT" />
                <!-- <data android:mimeType="*/*" /> -->
                <data android:mimeType="video/*" />
                <data android:mimeType="image/*" />
                <data android:mimeType="text/*" />
            </intent-filter>
        </activity>

        <activity
            android:name=".VideoTrimmerActivity"
            android:configChanges="orientation|screenSize"
            android:launchMode="singleTask"
            android:screenOrientation="portrait"
            android:theme="@style/AppTheme.SlidrActivityTheme"
            android:windowSoftInputMode="adjustPan" />
    ...

 ```

 * In YourActivity
  ```
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //  Open VideoTrimmerActivity
        if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) { //recive 
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();
                // MEDIA GALLERY
                String path = getPath(selectedImageUri);
                Uri uriFile = Uri.fromFile(new File(path));
                String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uriFile.toString());
                Log.d(TAG, "onActivityResult: " + fileExtension);

                if (fileExtension.equalsIgnoreCase("MP4")) {
                    File file = new File(path);
                    if (file.exists()) {
                        startActivityForResult(new Intent(YourActivity.this, VideoTrimmerActivity.class).putExtra("EXTRA_PATH", path), VIDEO_TRIM);
                        overridePendingTransition(0, 0);
                    } else {
                        Toast.makeText(NewPostActivity.this, "Please select proper video", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.file_format) + " ," + fileExtension, Toast.LENGTH_SHORT).show();
                }
            }
        }

        //  ForResult from VideoTrimmerActivity
        if (requestCode == VIDEO_TRIM) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    String videoPath = data.getExtras().getString("INTENT_VIDEO_FILE");
                    File file = new File(videoPath);
                    Log.d(TAG, "onActivityResult: " + file.length());

                    pathPostImg = videoPath;

                    Glide.with(this)
                            .load(pathPostImg)
                            .into(postImg);
                    postImgLY.setVisibility(View.VISIBLE);

                }
            }
        }

    }
  ```
   
## Proguard ##
 
  ```
    ### RxJava, RxAndroid for media-picker-android library
    -keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
        long producerIndex;
        long consumerIndex;
    }
    
    ### mp4parser 
    -keep class * implements com.coremedia.iso.boxes.Box {* ; }
    -dontwarn com.coremedia.iso.boxes.*
    -dontwarn com.googlecode.mp4parser.authoring.tracks.mjpeg.**
    -dontwarn com.googlecode.mp4parser.authoring.tracks.ttml.**
  ```

## Requirements ##

 - Android 4.1+
  

## Contact me via e-mail ##
For any inquiries or clarifications, you can contact me via e-mail. Just send an email to [ahmedmbadereldin@gmail.com](mailto:ahmedmbadereldin@gmail.com "ahmedmbadereldin@gmail.com").

## Contact me via social media ##

[Facebook](https://www.facebook.com/AhmedMBaderElDin) | [Twitter](https://twitter.com/AhmedBaderEDin) | [Linkedin](https://www.linkedin.com/in/ahmed-m-bader-el-din-0ba48bb5/)
 
## Libraries ##

 - [SDP - a scalable size unit](https://github.com/intuit/sdp)
 - [SSP - a scalable size unit for texts](https://github.com/intuit/ssp)
 - [Android RunTime Permission](https://github.com/fccaikai/AndroidPermissionX)
 - [read, write and create MP4 files](https://github.com/sannies/mp4parser)
 - [Pick image or video](https://github.com/iamthevoid/media-picker-android)
 - [View images](https://github.com/bumptech/glide)
 