package com.mohmd_bh.magicyvoice.ui;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.text.SpannableString;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.mohmd_bh.magicyvoice.ARApplication;
import com.mohmd_bh.magicyvoice.R;
import com.mohmd_bh.magicyvoice.util.AndroidUtils;
import com.mohmd_bh.magicyvoice.util.AnimationUtil;
import com.mohmd_bh.magicyvoice.util.TimeUtils;
import com.mohmd_bh.magicyvoice.widget.WaveformView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MainContract.View {

    private MainContract.UserActionListener presenter;
    private static final String TAG = "MainActivity";
    TextView textV_Time;
    ImageButton btnPlay;
    ImageButton btnStop;
    Button btn_downLink;
    private TextView textV_Paragraph;
    private WaveformView waveformView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize All Views
        initializeViews();
        // Initialize All onClick Views
        setOnClick_Views();

        waveformView.setOnSeekListener(new WaveformView.OnSeekListener() {
            @Override
            public void onStartSeek() {
                presenter.disablePlaybackProgressListener();

            }

            @Override
            public void onSeek(int px, long mills) {
                presenter.restHighlightByPositionOfWords(presenter.getPositionOfWordModelByCurrentTime(mills/1000.0));
                presenter.enablePlaybackProgressListener();
                presenter.seekPlayback(px);
                //use this if you added seekBar
                /*int length = waveformView.getWaveformLength();
                if (length > 0) {
                    playProgress.setProgress(1000 * (int) AndroidUtils.pxToDp(px) / length);
                }*/

                textV_Time.setText(TimeUtils.formatTimeIntervalHourMinSec2(mills));
            }

            @Override
            public void onSeeking(int px, long mills) {
                int length = waveformView.getWaveformLength();
                if (length > 0) {
                    //playProgress.setProgress(1000 * (int) AndroidUtils.pxToDp(px) / length);
                    //presenter.restHighlightByPositionOfWords(presenter.getPositionOfWordModelByCurrentTime(mills/1000.0));
                }
                textV_Time.setText(TimeUtils.formatTimeIntervalHourMinSec2(mills));
            }
        });

        presenter = ARApplication.getInjector().provideMainPresenter();
        presenter.executeFirstRun(this);
    }

    private void setOnClick_Views() {
        btnPlay.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btn_downLink.setOnClickListener(this);
    }

    @SuppressLint("WrongConstant")
    private void initializeViews() {
        btnPlay = findViewById(R.id.btnPlay);
        textV_Time = findViewById(R.id.textV_Time);
        btn_downLink = findViewById(R.id.btn_downLink);
        textV_Paragraph = findViewById(R.id.textV_Paragraph);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            textV_Paragraph.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        }
        else
            Toast.makeText(this, "Android Version Less 26(O)", Toast.LENGTH_SHORT).show();
        btnStop = findViewById(R.id.btnStop);
        waveformView = findViewById(R.id.waveformView);
    }

    @Override
    public void setTextInParagraph(String textInParagraph){ textV_Paragraph.setText(textInParagraph); }

    @Override
    public TextView getTextV_Paragraph(){ return textV_Paragraph; }

    @Override
    public void showPlayStart(boolean animate) {
        if (animate) {
            AnimationUtil.viewAnimationX(btnPlay, -75f, new Animator.AnimatorListener() {
                @Override public void onAnimationStart(Animator animation) { }
                @Override public void onAnimationEnd(Animator animation) {
                    btnStop.setVisibility(View.VISIBLE);
                    btnPlay.setImageResource(R.drawable.ic_pause);
                }
                @Override public void onAnimationCancel(Animator animation) { }
                @Override public void onAnimationRepeat(Animator animation) { }
            });
        } else {
            btnPlay.setTranslationX(-75f);
            btnStop.setVisibility(View.VISIBLE);
            btnPlay.setImageResource(R.drawable.ic_pause);
        }
    }

    @SuppressLint("LogNotTimber")
    public void showWaveForm(int[] waveForm, long duration) {
        if (waveForm.length > 0) {
            //Log.d("new_Activity: ", "waveForm Length is More 0: " + waveForm.length);
            btnPlay.setVisibility(View.VISIBLE);

        } else {
            //Log.d("new_Activity: ", "waveForm Length is less 0: " + waveForm.length);
            btnPlay.setVisibility(View.INVISIBLE);

        }
        ////Log.d("new_Activity: ", "waveForm is NULL: " + (waveForm != null));
        waveformView.setWaveform(waveForm);
        waveformView.setPxPerSecond(AndroidUtils.dpToPx(ARApplication.getDpPerSecond((float) duration / 1000000f)));
    }

    @Override
    public void showDuration(String duration) {}

    @Override
    public void showName(String name) {}

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPlay:
                presenter.startPlayback();
                    //Timber.tag("!isPlay: ").d("InSide");
                break;
            case R.id.btnStop:
                presenter.stopPlayback();
                break;
            case R.id.btn_downLink:

                break;
        }
    }

    @Override
    public void showPlayStop() {
        btnPlay.setImageResource(R.drawable.ic_play);
        waveformView.moveToStart();
        textV_Time.setText(TimeUtils.formatTimeIntervalHourMinSec2(0));

        AnimationUtil.viewAnimationX(btnPlay, 0f, new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) { }
            @Override public void onAnimationEnd(Animator animation) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnStop.setVisibility(View.GONE);
                    }
                });
            }
            @Override public void onAnimationCancel(Animator animation) { }
            @Override public void onAnimationRepeat(Animator animation) { }
        });

    }

    @Override
    public void onPlayProgress(final long mills, int px, int percent) {
        //Timber.tag(TAG).d("onPlayProgress");
        textV_Time.setText(TimeUtils.formatTimeIntervalHourMinSec2(mills));
        //Timber.tag(TAG).d("dpPerSecond %s", percent);
        waveformView.setPlayback(px);
    }

    @Override
    public void showPlayPause() {
        btnPlay.setImageResource(R.drawable.ic_play);
    }

    @Override
    public void setSpan_ForText(SpannableString spannableString) { textV_Paragraph.setText(spannableString); }

    @Override
    public void removeSpanFromText(SpannableString spannableString) {
        textV_Paragraph.setText(spannableString);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(presenter != null){
            presenter.unbindView();
            waveformView.setPlayback(-1);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //textV_Paragraph.setText("");
        presenter.bindView(this);
        presenter.loadActiveFile();

    }

    @Override
    public void showProgress() {

    }

    @Override
    public void hideProgress() {

    }

    @Override
    public void showError(int resId) {

    }
}
