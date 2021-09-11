package com.mohmd_bh.magicyvoice.audio.player;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;

import com.mohmd_bh.magicyvoice.AppConstants;
import com.mohmd_bh.magicyvoice.exception.AppException;
import com.mohmd_bh.magicyvoice.exception.PermissionDeniedException;
import com.mohmd_bh.magicyvoice.exception.PlayerDataSourceException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

public class AudioPlayer implements PlayerContract.Player, MediaPlayer.OnPreparedListener {


    //private static final String TAG = "AudioPlayer";
    private List<PlayerContract.PlayerCallback> actionsListeners = new ArrayList<>();
    private MediaPlayer mediaPlayer;
    private Timer timerProgress;
    private boolean isPrepared = false;
    private boolean isPause = false;
    private long seekPos = 0;
    private long pausePos = 0;
    private String dataSource;


    private static class SingletonHolder{
        private static AudioPlayer singleton = new AudioPlayer();

        private static AudioPlayer getSingleton(){
            return singleton;
        }

    }

    public static AudioPlayer getInstance() {

        return SingletonHolder.getSingleton();
    }

    private AudioPlayer() {}


    @Override
    public boolean isPlaying() {
        try {
            return mediaPlayer != null && mediaPlayer.isPlaying();
        } catch (IllegalStateException e) {
            //Timber.tag(TAG).d(e, "Player is not initialized!");
        }
        return false;
    }

    @Override
    public boolean isPause() {
        return isPause;
    }

    @Override
    public long getPauseTime() {
        return seekPos;
    }

    @Override
    public void addPlayerCallback(PlayerContract.PlayerCallback callback) {
        if (callback != null) {
            actionsListeners.add(callback);
        }
    }

    @Override
    public boolean removePlayerCallback(PlayerContract.PlayerCallback callback) {
        if (callback != null) {
            return actionsListeners.remove(callback);
        }
        return false;
    }

    @Override
    public void setData(String data) {
        if (mediaPlayer != null && dataSource != null && dataSource.equals(data)) {
            //Do nothing
        } else {
            //Timber.tag(TAG).d("inSide setData - is Changed or value is NULL");
            dataSource = data;
            restartPlayer();
        }

    }

    @Override
    public void playOrPause() {

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                pause();
            } else {
                //Timber.tag("play&Pause :").d("is Playing : FALSE");
                isPause = false;

                if (!isPrepared) {
                    try {
                        mediaPlayer.setOnPreparedListener(this);
                        mediaPlayer.prepareAsync();
                    } catch (IllegalStateException ex) {
                        //Timber.tag("Prepare Player : ").d(String.valueOf(ex));
                        restartPlayer();
                        mediaPlayer.setOnPreparedListener(this);
                        try {
                            mediaPlayer.prepareAsync();
                        } catch (IllegalStateException e) {
                            //Timber.e(e);
                            restartPlayer();
                        }
                    }

                } else {
                    //Timber.tag("play&Pause :").d("is Prepared");
                    mediaPlayer.start();
                    mediaPlayer.seekTo((int) pausePos);

                    onStartPlay();
                    //showPlayStart(true);

                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            stop();
                            onStopPlay();
                        }
                    });

                    //Timber.tag("play&Pause :").d("Pause POS: %s", pausePos);

                    setTimer();

                    //mTextView.post(startLis);

                    //txtView.post(mUpdateTime);
                }
                pausePos = 0;
            }
        }
        //Timber.tag("play&Pause :").d("mediaPlayer is : Null");

    }

    @Override
    public void seek(long mills) {
        /*try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {

                mediaPlayer.seekTo(AndroidUtils.convertPxToMills(px, AndroidUtils.dpToPx(dpPerSecond)));
                //onSeek((int) seekPos);
            }

        } catch (IllegalStateException e) {
            Timber.e(e, "Player is not initialized!");
        }*/

        seekPos = mills;
        if (isPause) {
            pausePos = mills;
        }
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.seekTo((int) seekPos);
                onSeek((int) seekPos);
            }
        } catch (IllegalStateException e) {
            Timber.e(e, "Player is not initialized!");
        }

    }


    @Override
    public void stop() {

        //showPlayStop();
        //count = 0;
        if (timerProgress != null) {
            timerProgress.cancel();
            timerProgress.purge();
        }


        //mTextView.removeCallbacks(startLis);
        //txtView.removeCallbacks(mUpdateTime);

        isPrepared = false;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.setOnCompletionListener(null);
            isPrepared = false;
            onStopPlay();
            mediaPlayer.getCurrentPosition();
            seekPos = 0;
        }
        isPause = false;

        pausePos = 0;
        //waveformView.moveToStart();

        //restHighlight();

        //txtView.removeCallbacks(mUpdateTime);
        //mTextView.removeCallbacks(startLis);
/*

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtView.setText("0.00");
            }
        });

        //restartPlayer();
        removeSpan();
*/

    }

    @Override
    public void pause() {
        if (timerProgress != null) {
            timerProgress.cancel();
            timerProgress.purge();
        }

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();

                onPausePlay();
                //showPlayPause();

                seekPos = mediaPlayer.getCurrentPosition();
                isPause = true;
                pausePos = seekPos;
            }
            //txtView.removeCallbacks(mUpdateTime);
            //mTextView.removeCallbacks(startLis);
        }
    }

    private void restartPlayer() {
        //dataSource = Uri.fromFile(tempfile);
        try {
            //InputStream inputStream = getAssets().open("audio18S.m4a");
            isPrepared = false;
            mediaPlayer = new MediaPlayer();
            //mediaPlayer.setDataSource(AudioURL);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //mediaPlayer.setDataSource(getAssets().openFd("audio18S.m4a"));
                mediaPlayer.setDataSource(dataSource);

            }

            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        } catch (IOException | IllegalArgumentException | IllegalStateException | SecurityException e) {

            Timber.e(e);
            if (Objects.requireNonNull(e.getMessage()).contains("Permission denied")) {
                onError(new PermissionDeniedException());
            } else {
                onError(new PlayerDataSourceException());
            }

        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mediaPlayer != mp) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = mp;
        }
        isPrepared = true;
        mediaPlayer.start();
        mediaPlayer.seekTo((int) seekPos);

        onPreparedPlay();
        onStartPlay();
        //showPlayStart(true);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stop();
                onStopPlay();
                //showPlayStop();
            }
        });

        //mTextView.post(startLis);
        setTimer();
        //txtView.post(mUpdateTime);

    }

    private void setTimer() {
        if (!isPause) {
            //count = 0;
        }
        timerProgress = new Timer("timer", true);
        timerProgress.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        //int curPos = mediaPlayer.getCurrentPosition();
                        //onPlayProgress(curPos);
                        //waveformView.setPlayback(AndroidUtils.convertMillsToPx(mediaPlayer.getCurrentPosition(),AndroidUtils.dpToPx(dpPerSecond)));
                        //startLis();
                        //Timber.tag(TAG).d("inTimerProgress");
                        int cur = mediaPlayer.getCurrentPosition();
                        if(cur >0)
                        onPlayProgress(cur);

                    }

                } catch (IllegalStateException e) {
                    Timber.e(e, "Player is not initialized!");
                }
            }
        }, 0, AppConstants.VISUALIZATION_INTERVAL); // 0 check value if have any problem


    }

    @Override
    public void release() {
        stop();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isPrepared = false;
        isPause = false;
        dataSource = null;
        actionsListeners.clear();

    }

    private void onPreparedPlay() {
        if (!actionsListeners.isEmpty()) {
            for (int i = 0; i < actionsListeners.size(); i++) {
                actionsListeners.get(i).onPreparePlay();
            }
        }
    }

    private void onStartPlay() {
        if (!actionsListeners.isEmpty()) {
            for (int i = 0; i < actionsListeners.size(); i++) {
                actionsListeners.get(i).onStartPlay();
            }
        }
    }

    private void onPlayProgress(long mills) {
        if (!actionsListeners.isEmpty()) {
            for (int i = 0; i < actionsListeners.size(); i++) {
                actionsListeners.get(i).onPlayProgress(mills);
            }
        }
    }

    private void onStopPlay() {
        if (!actionsListeners.isEmpty()) {
            for (int i = 0; i < actionsListeners.size(); i++) {
                actionsListeners.get(i).onStopPlay();
            }
        }
    }

    private void onPausePlay() {
        if (!actionsListeners.isEmpty()) {
            for (int i = 0; i < actionsListeners.size(); i++) {
                actionsListeners.get(i).onPausePlay();
            }
        }
    }

    private void onSeek(long mills) {
        if (!actionsListeners.isEmpty()) {
            for (int i = 0; i < actionsListeners.size(); i++) {
                actionsListeners.get(i).onSeek(mills);
            }
        }
    }

    private void onError(AppException throwable) {

        if (!actionsListeners.isEmpty()) {
            for (int i = 0; i < actionsListeners.size(); i++) {
                actionsListeners.get(i).onError(throwable);
            }
        }
    }
}
