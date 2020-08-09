package com.ahmedbadereldin.videotrimmerapplication.javaCode;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ahmedbadereldin.videotrimmerapplication.R;
import com.bumptech.glide.Glide;
import com.kcode.permissionslib.main.OnRequestPermissionsCallBack;
import com.kcode.permissionslib.main.PermissionCompat;

import java.io.File;
import java.util.Objects;

import iam.thevoid.mediapicker.rxmediapicker.Purpose;
import iam.thevoid.mediapicker.rxmediapicker.RxMediaPicker;
import rx.functions.Action1;


public class NewPostActivity extends AppCompatActivity {

    private ImageView videoBtn;
    private FrameLayout postImgLY;
    private ImageView postImg;
    private ImageView cancelImgBtn;
    Uri uriPostImg;
    String pathPostImg;
    private static final int REQUEST_TAKE_GALLERY_VIDEO = 100;
    private static final int VIDEO_TRIM = 101;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_post);
        initViews();
        setSharedInentData(getIntent());
        initClicks();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setSharedInentData(intent);
    }

    private void initViews() {
        videoBtn = findViewById(R.id.videoBtn);
        postImgLY = findViewById(R.id.postImgLY);
        postImg = findViewById(R.id.postImg);
        cancelImgBtn = findViewById(R.id.cancelImgBtn);
    }


    private void setSharedInentData(Intent sharedInentData) {

        String receivedAction = sharedInentData.getAction();
        String receivedType = sharedInentData.getType();

        if (receivedAction != null && receivedAction.equals(Intent.ACTION_SEND)) {
            //content is being shared

            assert receivedType != null;
            if (receivedType.startsWith("image/")) {
                //handle sent image
                uriPostImg = sharedInentData.getParcelableExtra(Intent.EXTRA_STREAM);
                if (uriPostImg != null) {
                    hideSoftKeyboard(this, 0);

                    pathPostImg = null;
                    Glide.with(this)
                            .load(uriPostImg)
                            .into(postImg);
                    postImgLY.setVisibility(View.VISIBLE);

                }
            } else if (receivedType.startsWith("video/")) {
//                GlobalData.Toast("video");
                //handle sent video
                uriPostImg = sharedInentData.getParcelableExtra(Intent.EXTRA_STREAM);
                if (uriPostImg != null) {
                    //set the video
                    //RESAMPLE YOUR IMAGE DATA BEFORE DISPLAYING
                    Glide.with(this)
                            .load(uriPostImg)
                            .into(postImg);
                    postImgLY.setVisibility(View.VISIBLE);
                }
            }
        }

    }

    private void initClicks() {

        cancelImgBtn.setOnClickListener(view -> {
            uriPostImg = null;
            pathPostImg = null;
            postImgLY.setVisibility(View.GONE);
        });

        videoBtn.setOnClickListener(view -> {
            try {

                PermissionCompat.Builder builder = new PermissionCompat.Builder(this);
                builder.addPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE});
                builder.addPermissionRationale(getString(R.string.should_allow_permission));
                builder.addRequestPermissionsCallBack(new OnRequestPermissionsCallBack() {
                    @Override
                    public void onGrant() {
                        RxMediaPicker.builder(NewPostActivity.this)
                                .pick(Purpose.Pick.VIDEO)
                                .take(Purpose.Take.VIDEO)
                                .build()
                                .subscribe(new Action1<Uri>() {
                                    @Override
                                    public void call(Uri uri) {
                                        loadIage(uri);
                                    }
                                });
                    }

                    @Override
                    public void onDenied(String permission) {
                        Toast.makeText(NewPostActivity.this, getString(R.string.some_permission_denied), Toast.LENGTH_SHORT).show();
                    }
                });
                builder.build().request();

            } catch (Exception e) {
                e.printStackTrace();
            }


        });
    }

    private void loadIage(Uri filepath) {
        // MEDIA GALLERY
        String path = getPath(filepath);
        Uri filea = Uri.fromFile(new File(path));
        String fileExt = MimeTypeMap.getFileExtensionFromUrl(filea.toString());

        if (fileExt.equalsIgnoreCase("MP4")) {
            File file = new File(path);
            if (file.exists()) {
                startActivityForResult(new Intent(NewPostActivity.this, VideoTrimmerActivity.class).putExtra("EXTRA_PATH", path), VIDEO_TRIM);
                overridePendingTransition(0, 0);
            } else {
                Toast.makeText(NewPostActivity.this, "Please select proper video", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.file_format) + " ," + fileExt, Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();

                // MEDIA GALLERY
                String path = getPath(selectedImageUri);
                Uri filea = Uri.fromFile(new File(path));
                String fileExt = MimeTypeMap.getFileExtensionFromUrl(filea.toString());
                Log.d("onActivityResultaaa", "onActivityResult: " + fileExt);

                if (fileExt.equalsIgnoreCase("MP4")) {
                    File file = new File(path);
                    if (file.exists()) {
                        startActivityForResult(new Intent(NewPostActivity.this, VideoTrimmerActivity.class).putExtra("EXTRA_PATH", path), VIDEO_TRIM);
                        overridePendingTransition(0, 0);
                    } else {
                        Toast.makeText(NewPostActivity.this, "Please select proper video", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.file_format) + " ," + fileExt, Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == VIDEO_TRIM) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    String videoPath = data.getExtras().getString("INTENT_VIDEO_FILE");
                    File file = new File(videoPath);
                    Log.d("onActivityResultAA", "onActivityResult: " + file.length());

                    pathPostImg = videoPath;

                    Glide.with(this)
                            .load(pathPostImg)
                            .into(postImg);
                    postImgLY.setVisibility(View.VISIBLE);

                }
            }
        }

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private String getPath(Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(this, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(this, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(this, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(this, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return "";
    }

    private String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            int currentApiVersion = Build.VERSION.SDK_INT;
            //TODO changes to solve gallery video issue
            if (currentApiVersion > Build.VERSION_CODES.M && uri.toString().contains(getString(R.string.app_provider))) {
                cursor = context.getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    final int column_index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (cursor.getString(column_index) != null) {
                        String state = Environment.getExternalStorageState();
                        File file;
                        if (Environment.MEDIA_MOUNTED.equals(state)) {
                            file = new File(Environment.getExternalStorageDirectory() + "/DCIM/", cursor.getString(column_index));
                        } else {
                            file = new File(context.getFilesDir(), cursor.getString(column_index));
                        }
                        return file.getAbsolutePath();
                    }
                    return "";
                }
            } else {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                        null);
                if (cursor != null && cursor.moveToFirst()) {
                    final int column_index = cursor.getColumnIndexOrThrow(column);
                    if (cursor.getString(column_index) != null) {
                        return cursor.getString(column_index);
                    }
                    return "";
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return "";
    }

    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static void hideSoftKeyboard(Activity activity, int show) {
        try {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) activity.getSystemService(
                            Activity.INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.hideSoftInputFromWindow(
                    Objects.requireNonNull(activity.getCurrentFocus()).getWindowToken(), 0);
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

}
