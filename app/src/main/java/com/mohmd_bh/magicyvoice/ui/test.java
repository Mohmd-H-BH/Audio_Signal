package com.mohmd_bh.magicyvoice.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mohmd_bh.magicyvoice.R;

import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

public class test extends AppCompatActivity implements View.OnClickListener{


    TextView textView;

    Button btn_play, btn_stop;

    static int count = 0;

    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        textView = findViewById(R.id.textV);

        btn_play = findViewById(R.id.btn_play);

        btn_stop = findViewById(R.id.btn_stop);

        btn_play.setOnClickListener(this);

        btn_stop.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.btn_play :
                setTimer();
                Log.d("btn_Play", "Clicked");

                break;

            case R.id.btn_stop :
                closeTimer();
                Log.d("btn_Stop", "Clicked");
                count=0;

                break;

        }

    }


    private void setTimer() {

        timer = new Timer("timer", true);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                startTask();


            }
        },1000,1000);

    }

    private void startTask() {
        if(count <= 10)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText(String.valueOf(count));
                    count++;
                }
            });
        }
        else {
            closeTimer();
            Timber.tag("startTask").d("Count: %s > 10", count);
        }
    }


    private void closeTimer() {
        timer.cancel();
        timer.purge();
    }
}
