package com.mohmd_bh.magicyvoice.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.util.Log;

import com.mohmd_bh.magicyvoice.ARApplication;
import com.mohmd_bh.magicyvoice.AppConstants;
import com.mohmd_bh.magicyvoice.Model.indexWords;
import com.mohmd_bh.magicyvoice.Model.metaModel;
import com.mohmd_bh.magicyvoice.Model.wordModel;
import com.mohmd_bh.magicyvoice.R;
import com.mohmd_bh.magicyvoice.audio.SoundFile;
import com.mohmd_bh.magicyvoice.audio.player.PlayerContract;
import com.mohmd_bh.magicyvoice.exception.AppException;
import com.mohmd_bh.magicyvoice.exception.ErrorParser;
import com.mohmd_bh.magicyvoice.repos.DataSource_JSON;
import com.mohmd_bh.magicyvoice.repos.Prefs;
import com.mohmd_bh.magicyvoice.repos.Record;
import com.mohmd_bh.magicyvoice.util.AndroidUtils;
import com.mohmd_bh.magicyvoice.util.TimeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

public class MainPresenter implements MainContract.UserActionListener {

    private static final String TAG = "MainPresenter";
    private MainContract.View view;
    private final PlayerContract.Player audioPlayer;
    private PlayerContract.PlayerCallback playerCallback;
    private final Prefs prefs;
    private long songDuration = 0;
    private float dpPerSecond = AppConstants.SHORT_RECORD_DP_PER_SECOND;
    private boolean listenPlaybackProgress = true;
    private Record record;
    private File tempfile;
    private Dialog dialog;
    private String dataSource = null;

    // JSON File
    private DataSource_JSON dataSource_json;
    private List<wordModel> wordModels_List = new ArrayList<>();
    private List<indexWords> indexWordModels_List = new ArrayList<>();
    private List<metaModel> speaker_List = new ArrayList<>();
    private static int count = 0;
    // JSON File


    public MainPresenter(Prefs prefs, PlayerContract.Player audioPlayer, DataSource_JSON dataSource_json) {

        this.audioPlayer = audioPlayer;
        this.prefs = prefs;
        this.dataSource_json = dataSource_json;

        setDataFromJSON();
    }

    private void setDataFromJSON() {
        indexWordModels_List = dataSource_json.getIndexWordModels_List();
        wordModels_List = dataSource_json.getWordModels_List();

    }

    private SpannableString setSpan_ForText(indexWords indexWords) {

        SpannableString spannableString = new SpannableString(view.getTextV_Paragraph().getText());

        //Get the previous spans and remove them
        BackgroundColorSpan[] backgroundSpans = spannableString.getSpans(0, spannableString.length(), BackgroundColorSpan.class);

        for (BackgroundColorSpan span : backgroundSpans) {
            spannableString.removeSpan(span);
        }

        spannableString.setSpan(new BackgroundColorSpan(Color.BLACK), indexWords.getStartIndex(), indexWords.getEndIndex() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableString;
    }

    private void startSetHighlight(double second) {
        SpannableString spannableString = null;
        if (wordModels_List.size() > count) { //wordModels_List.size()

            Timber.tag("mUpdateTime2: ").d("isHighlight: " + wordModels_List.get(count).isHighlight()
                    + " / wordsList : " + wordModels_List.get(count).getText() + " Count: " + count);

            //Timber.tag("mUpdateTime2: ").d("InSide");
            //currentDuration = mediaPlayer.getCurrentPosition();
            double millsSec = second;

            wordModel temp_wordModel = wordModels_List.get(count);

            if (temp_wordModel.isHighlight()) {
                Timber.tag(TAG).d("endTime: %s / currentTime: %s", temp_wordModel.getEnd_time(), millsSec);
                if (temp_wordModel.getEnd_time() < millsSec) {

                    count++;
                }
            } else if (temp_wordModel.getStart_time() <= millsSec) {
                Timber.tag(TAG).d("inSide StartTime <= millsSec");
                temp_wordModel.setHighlight(true);
                wordModels_List.set(count, temp_wordModel);

                spannableString = setSpan_ForText(indexWordModels_List.get(count));

                view.setSpan_ForText(spannableString);
                //return spannableString;
            }else {
                Timber.tag(TAG).d("startTime: %s > OR < currentTime: %s", temp_wordModel.getStart_time(), millsSec);
            }

        } else {
            Log.d("mUpdateTime2: ", "OutSide" + " sizeWordList: " + wordModels_List.size() + " Count: " + count);
            //stop();
            stopPlayback();
            restHighlight();
            removeSpan();

            count = 0;

        }
        //return null;
    }

    private void removeSpan() {
        SpannableString spannableString = new SpannableString(view.getTextV_Paragraph().getText());

        //Get the previous spans and remove them
        BackgroundColorSpan[] backgroundSpans = spannableString.getSpans(0, spannableString.length(), BackgroundColorSpan.class);

        for (BackgroundColorSpan span : backgroundSpans) {
            spannableString.removeSpan(span);
        }

        view.removeSpanFromText(spannableString);
        //textV_JSON.setText(spannableString);
    }

    @Override
    public int getPositionOfWordModelByCurrentTime(double currentTime) {
        DecimalFormat df = new DecimalFormat("##.#");
        currentTime = Double.valueOf(df.format(currentTime));


        Timber.tag(TAG).d("inSide getPositionOfWord - time is: %s", currentTime);
        wordModel wordModel;
        int pos = -1;
        for (int i = 0; i < wordModels_List.size(); i++) {

            wordModel = wordModels_List.get(i);
            //Timber.tag(TAG).d("inSide getPositionOfWord - time is: %s", currentTime);
            if (wordModel.getStart_time() <= currentTime && wordModel.getEnd_time() >= currentTime) {
                pos = i;
                break;
            }

        }
        Timber.tag(TAG).d("inSide getPositionOfWord - not Found word by Time");
        //if not Found
        return pos;
    }

    @Override
    public void restHighlightByPositionOfWords(int position) {
        removeSpan();
        if (position != -1)
            count = position;
        Timber.tag(TAG).d("restHighlight - Position: %s", position);
        for (int i = 0; i < wordModels_List.size(); i++) {


            if (i >= position && position != -1)
            {
                wordModels_List.get(i).setHighlight(false);
                Timber.tag(TAG).d("restHighlight - word: %s / isHighlight: %s"
                        ,wordModels_List.get(i).getText(), wordModels_List.get(i).isHighlight());
            }
            else {
                wordModels_List.get(i).setHighlight(true);
                Timber.tag(TAG).d("restHighlight - word: %s / isHighlight: %s"
                        ,wordModels_List.get(i).getText(), wordModels_List.get(i).isHighlight());
            }
        }

    }

    private void restHighlight() {
        int i = 0;
        while (wordModels_List.size() > i) {
            wordModels_List.get(i).setHighlight(false);
            i++;
        }
    }

    @Override
    public void bindView(final MainContract.View v) {
        this.view = v;

        String paragraph = dataSource_json.getDataFrom_StringBuilder();
        if (view.getTextV_Paragraph().length() < paragraph.length())
            view.setTextInParagraph(paragraph);

        if (playerCallback == null) {

            playerCallback = new PlayerContract.PlayerCallback() {
                @Override
                public void onPreparePlay() {

                }

                @Override
                public void onStartPlay() {
                    if (view != null) {
                        Timber.tag(TAG).d("onStartPlay");
                        view.showPlayStart(true);

                        // For add Playback Service Later...
                        if (record != null) {

                        }
                    }
                }

                @Override
                public void onPlayProgress(final long mills) {
                    //Timber.tag(TAG).d("inSide onPlayProgress");
                    if (view != null && listenPlaybackProgress) {
                        //Timber.tag(TAG).d("view & listenPlaybackProgress: %s", listenPlaybackProgress);

                        AndroidUtils.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                if (view != null) {
                                    long duration = songDuration / 1000;
                                    //Timber.tag(TAG).d("duration: %s - songDuration: %s", duration, songDuration);
                                    if (duration > 0) {
                                        //Timber.tag(TAG).d("onPlayProgress");
                                        //Timber.tag(TAG).d("dpPerSecond %s", dpPerSecond);
                                        view.onPlayProgress(mills, AndroidUtils.convertMillsToPx(mills
                                                , AndroidUtils.dpToPx(dpPerSecond)), (int) (1000 * mills / duration));

                                        Timber.tag(TAG).d("Current Time: %s", (mills / 1000.0));
                                        startSetHighlight(mills / 1000.0);
                                        //view.setSpan_ForText(startSetHighlight(mills));
                                    }
                                } else {
                                    Timber.tag(TAG).d("view is NULL");
                                }
                            }
                        });

                        /*AndroidUtils.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                if (view != null) {
                                    long duration = songDuration / 1000;
                                    if (duration > 0) {
                                        Timber.tag(TAG).d("onPlayProgress");
                                        view.onPlayProgress(mills, AndroidUtils.convertMillsToPx(mills
                                                , AndroidUtils.dpToPx(dpPerSecond)), (int) (1000 / mills / duration));
                                    }
                                }
                                else{
                                    Timber.tag(TAG).d("view is NULL");
                                }
                            }
                        });*/
                    }
                }

                @Override
                public void onStopPlay() {
                    if (view != null) {
                        Timber.tag(TAG).d("inSide onStopPlay");
                        view.showPlayStop();
                        view.showDuration(TimeUtils.formatTimeIntervalHourMinSec(songDuration / 1000));

                        count = 0;
                        restHighlight();
                        removeSpan();

                    }

                }

                @Override
                public void onPausePlay() {
                    if (view != null) {
                        Timber.tag(TAG).d("inSide onPausePlay");
                        view.showPlayPause();
                    }
                }

                @Override
                public void onSeek(long mills) {

                }

                @Override
                public void onError(AppException throwable) {
                    Timber.e(throwable);
                    if (view != null) {
                        view.showError(ErrorParser.parseException(throwable));
                    }
                }
            };

            removeSpan();
            restHighlight();
            count =0;
        }

        audioPlayer.addPlayerCallback(playerCallback);

        if (audioPlayer.isPlaying()) {
            view.showPlayStart(false);
        } else if (audioPlayer.isPause()) {
            if (view != null) {
                long duration = songDuration / 1000;
                if (duration > 0) {
                    long playProgressMills = audioPlayer.getPauseTime();
                    view.onPlayProgress(playProgressMills, AndroidUtils.convertMillsToPx(playProgressMills,
                            AndroidUtils.dpToPx(dpPerSecond)), (int) (1000 * playProgressMills / duration));
                }
                view.showPlayPause();
            }
        } else {
            audioPlayer.seek(0);
            view.showPlayStop();
        }


    }

    @Override
    public void unbindView() {
        if (view != null) {
            audioPlayer.removePlayerCallback(playerCallback);
            this.view = null;
        }
    }

    @Override
    public void clear() {
        if (view != null) {
            unbindView();
        }
        audioPlayer.release();


    }

    @Override
    public void executeFirstRun(final Context context) {
        if (prefs.isFirstRun()) {
            prefs.firstRunExecuted();

            //create tempFile On Storage
            if (inpStreamToFile(context)) {

                setLoading_Dialog(context);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //setFile_OnDatabase(context);
                        setAudio_File();
                        try {
                            if (dialog.isShowing())
                                dialog.dismiss();
                        } catch (IllegalArgumentException e) {
                            Timber.tag(TAG).d(e);
                        }
                    }
                }, 1000);

            }


        }

    }

    @Override
    public void loadActiveFile() {
        String path = prefs.getActiveFile();
        if (path != null)
            dataSource = path;

        if (record == null && path != null) {
            tempfile = new File(path);

            Timber.tag(TAG).d("tempFile Not NULL: %s / isExists: %s / Path: %s", tempfile != null,
                    Objects.requireNonNull(tempfile).exists(), tempfile.getAbsolutePath());


            setRecord();

            songDuration = record.getDuration();
            Timber.tag(TAG).d("is NULL - record Duration: %s / songDuration: %s", record.getDuration(), songDuration);

            dpPerSecond = ARApplication.getDpPerSecond((float) songDuration / 1000000f);

            AndroidUtils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (view != null) {
                        view.showWaveForm(record.getAmps(), songDuration);


                    }

                }
            });

        } else if (record != null) {
            songDuration = record.getDuration();
            Timber.tag(TAG).d("Not NULL - record Duration: %s / songDuration: %s", record.getDuration(), songDuration);
            dpPerSecond = ARApplication.getDpPerSecond((float) songDuration / 1000000f);

            AndroidUtils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (view != null) {
                        view.showWaveForm(record.getAmps(), songDuration);


                    }

                }
            });

        } else {
            AndroidUtils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (view != null) {
                        view.showWaveForm(new int[]{}, 0);
                        //view.showName("");
                        //view.showDuration(TimeUtils.formatTimeIntervalHourMinSec2(0));
                        //view.hideProgress();

                    }
                }
            });
        }


    }

    private void setAudio_File() {
        if (tempfile != null) {


            setRecord();
            //final Record rec = localRepository.getRecord((int) prefs.getActiveRecord());
            //record.setAmps(savedList);

            long duration = AndroidUtils.readRecordDuration(tempfile);
            Timber.tag(TAG).d("Duration is : %s", duration);

            dpPerSecond = ARApplication.getDpPerSecond((float) duration / 1000000f);

            //if(isSetSoundFile_String)
            view.showWaveForm(record.getAmps(), duration);
            //else
            Timber.tag("isSetSoundFile_String: ").d("is FALSE");

            dataSource = tempfile.getAbsolutePath();
        } else
            Timber.tag(TAG).d("File is Null");

    }

    private void setRecord() {
        long duration = AndroidUtils.readRecordDuration(tempfile);

        record = new Record(
                Record.NO_ID,
                tempfile.getName(),
                duration, //mills
                tempfile.lastModified(),
                new Date().getTime(),
                0,
                tempfile.getAbsolutePath(),
                false,
                true,
                new int[ARApplication.getLongWaveformSampleCount()]);

        //


        //Record record = getRecord(id);


        if (record != null) {
            String path = record.getPath();
            if (path != null && !path.isEmpty()) {
                final SoundFile soundFile;
                try {
                    soundFile = SoundFile.create(path);

                    if (soundFile != null) {
                        Record rec = new Record(
                                record.getId(),
                                record.getName(),
                                record.getDuration(),
                                record.getCreated(),
                                record.getAdded(),
                                record.getRemoved(),
                                record.getPath(),
                                record.isBookmarked(),
                                true,
                                soundFile.getFrameGains());
                        //boolean b = updateRecord(rec);


                        //SharedPreferences prefs = getPreferences(MODE_PRIVATE);
                        //int[] list = new int[10];


                                    /*
                                    StringBuilder str = new StringBuilder();
                                    for (int i = 0; i < soundFile.getFrameGains().length; i++) {
                                        str.append(soundFile.getFrameGains()[i]).append(",");
                                    }
                                    editor.putString(soundFile_String, str.toString()).apply();

                                    editor.commit();
                                    isSetSoundFile_String = true;
                                    */


                        record = rec;
                    } else {
                        Log.d(TAG, "Sound File" + "is NULL");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //return false;

        }

    }

    private void setLoading_Dialog(Context context) {

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_loading);
        dialog.show();
/*

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setView(R.layout.dialog_loading);

        dialog = builder.show();
*/


    }

    private Boolean inpStreamToFile(Context context) {
        InputStream initialStream = null;
        OutputStream outStream = null;
        //File tempfile = null;

        try {
            //initialStream = getAssets().open("audio18S.m4a");
            initialStream = context.getAssets().open("audio18S.m4a");
            //tempfile = File.createTempFile("temp_audio18S", ".m4a", getDir(Environment.DIRECTORY_MUSIC, 0));
            tempfile = File.createTempFile("temp_audio18S", ".mp3", context.getDir(Environment.DIRECTORY_MUSIC, 0));

            outStream = new FileOutputStream(tempfile);
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;

            while ((bytesRead = initialStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }

            /*    IOUtils.closeQuietly(initialStream);
                IOUtils.closeQuietly(outStream);*/

        } catch (IOException e) {
            e.printStackTrace();
            Timber.tag(TAG).d(e.toString());

        } finally {
            try {
                if (initialStream != null && outStream != null) {
                    initialStream.close();
                    outStream.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
                Timber.tag(TAG).d(e.toString());
            }

        }

        if (tempfile != null) {
            Timber.tag(TAG).d("Absolute Path: %s", tempfile.getAbsolutePath());

            Timber.tag(TAG).d("Size File: %s", (tempfile.length() / 1024));

            Timber.tag(TAG).d("File Name: %s", tempfile.getName());

            //isFirstTime = false;

            //editor.putBoolean(isConverted, isFirstTime).apply();

            //Timber.tag(TAG).d("%s - YES", isConverted);

            //file_Path = tempfile.getAbsolutePath();
            ///editor.putString(file_path_String, file_Path).apply();
            //editor.commit();

            prefs.setActiveFile(tempfile.getAbsolutePath());


            return true;
            //presenter.importAudioFile(this, Uri.fromFile(tempfile));
        }


        return false;
    }

    @Override
    public void startPlayback() {
        if (record != null && dataSource != null) {
            Timber.tag(TAG).d("inSide startPlayback");
            if (!audioPlayer.isPlaying()) {
                Timber.tag(TAG).d("inSide audioPlayer_isPlaying");
                audioPlayer.setData(dataSource);
            }

            audioPlayer.playOrPause();

        } else {
            Timber.tag(TAG).d("record OR dataSource is NULL");
        }
    }

    @Override
    public void pausePlayback() {

    }

    @Override
    public void seekPlayback(int px) {
        audioPlayer.seek(AndroidUtils.convertPxToMills(px, AndroidUtils.dpToPx(dpPerSecond)));
    }

    @Override
    public void stopPlayback() {
        audioPlayer.stop();
        count = 0;
        restHighlight();
        removeSpan();
    }

    @Override
    public void disablePlaybackProgressListener() {
        listenPlaybackProgress = false;
    }

    @Override
    public void enablePlaybackProgressListener() {
        listenPlaybackProgress = true;
    }

}
