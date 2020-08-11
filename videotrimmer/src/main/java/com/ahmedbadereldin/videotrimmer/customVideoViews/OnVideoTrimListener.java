package com.ahmedbadereldin.videotrimmer.customVideoViews;

import android.net.Uri;

public interface OnVideoTrimListener {

    void onTrimStarted();

    void getResult(final Uri uri);

    void cancelAction();

    void onError(final String message);
}
