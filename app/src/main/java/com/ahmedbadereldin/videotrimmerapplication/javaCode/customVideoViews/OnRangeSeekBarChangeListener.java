package com.ahmedbadereldin.videotrimmerapplication.javaCode.customVideoViews;
public interface OnRangeSeekBarChangeListener {
    void onCreate(CustomRangeSeekBar CustomRangeSeekBar, int index, float value);

    void onSeek(CustomRangeSeekBar CustomRangeSeekBar, int index, float value);

    void onSeekStart(CustomRangeSeekBar CustomRangeSeekBar, int index, float value);

    void onSeekStop(CustomRangeSeekBar CustomRangeSeekBar, int index, float value);
}
