package com.testapp.audiosignal.ui;

import android.content.Context;
import android.text.SpannableString;
import android.widget.TextView;

import com.testapp.audiosignal.Contract;


public interface MainContract {

    interface View extends Contract.View{

        void showPlayStart(boolean animate);

        void showPlayPause();

        void showPlayStop();

        void onPlayProgress(long mills, int px, int percent);

        void showWaveForm(int[] waveForm, long duration);

        void showDuration(String duration);

        void showName(String name);

        void setSpan_ForText(SpannableString spannableString);

        TextView getTextV_Paragraph();

        void removeSpanFromText(SpannableString spannableString);

        void setTextInParagraph(String textInParagraph);


    }

    interface UserActionListener extends Contract.UserActionsListener<MainContract.View>{

        void executeFirstRun(Context context);

        void startPlayback();

        void loadActiveFile();

        void pausePlayback();

        void seekPlayback(int px);

        void stopPlayback();

        void disablePlaybackProgressListener();

        void enablePlaybackProgressListener();

        int getPositionOfWordModelByCurrentTime(double currentTime);

        void restHighlightByPositionOfWords(int position);
    }

}
