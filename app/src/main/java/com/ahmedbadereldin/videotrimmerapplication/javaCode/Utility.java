package com.ahmedbadereldin.videotrimmerapplication.javaCode;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ahmedbadereldin.videotrimmerapplication.javaCode.customVideoViews.OnVideoTrimListener;
import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.FileDataSourceViaHeapImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class Utility {
    public static final String VIDEO_FORMAT = ".mp4";
    private static final String TAG = Utility.class.getSimpleName();

    public static void startTrim(@NonNull File src, @NonNull String dst, long startMs, long endMs,
                                 @NonNull OnVideoTrimListener callback) throws IOException {
        final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());

        File file = new File(dst);
        file.getParentFile().mkdirs();
        Log.d(TAG, "Generated file path " + dst);
        generateVideo(src, file, startMs, endMs, callback);
    }

    private static void generateVideo(@NonNull File src, @NonNull File dst, long startMs,
                                      long endMs, @NonNull OnVideoTrimListener callback) throws IOException {
        // NOTE: Switched to using FileDataSourceViaHeapImpl since it does not use memory mapping (VM).
        // Otherwise we get OOM with large movie files.
        Movie movie = MovieCreator.build(new FileDataSourceViaHeapImpl(src.getAbsolutePath()));

        List<Track> tracks = movie.getTracks();
        movie.setTracks(new LinkedList<Track>());
        // remove all tracks we will create new tracks from the old

        double startTime1 = startMs / 1000;
        double endTime1 = endMs / 1000;

        boolean timeCorrected = false;

        // Here we try to find a track that has sync samples. Since we can only start decoding
        // at such a sample we SHOULD make sure that the start of the new fragment is exactly
        // such a frame
        for (Track track : tracks) {
            if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
                if (timeCorrected) {
                    // This exception here could be a false positive in case we have multiple tracks
                    // with sync samples at exactly the same positions. E.g. a single movie containing
                    // multiple qualities of the same video (Microsoft Smooth Streaming file)

                    throw new RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.");
                }
//                startTime1 = correctTimeToSyncSample(track, startTime1, false);
//                endTime1 = correctTimeToSyncSample(track, endTime1, true);
                timeCorrected = true;
            }
        }

        for (Track track : tracks) {
            long currentSample = 0;
            double currentTime = 0;
            double lastTime = -1;
            long startSample1 = -1;
            long endSample1 = -1;

            for (int i = 0; i < track.getSampleDurations().length; i++) {
                long delta = track.getSampleDurations()[i];


                if (currentTime > lastTime && currentTime <= startTime1) {
                    // current sample is still before the new starttime
                    startSample1 = currentSample;
                }
                if (currentTime > lastTime && currentTime <= endTime1) {
                    // current sample is after the new start time and still before the new endtime
                    endSample1 = currentSample;
                }
                lastTime = currentTime;
                currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
                currentSample++;
            }
            movie.addTrack(new AppendTrack(new CroppedTrack(track, startSample1, endSample1)));
        }

        Container out = new DefaultMp4Builder().build(movie);

        FileOutputStream fos = new FileOutputStream(dst);
        FileChannel fc = fos.getChannel();
        out.writeContainer(fc);

        fc.close();
        fos.close();
        if (callback != null)
            callback.getResult(Uri.parse(dst.toString()));
    }


}
