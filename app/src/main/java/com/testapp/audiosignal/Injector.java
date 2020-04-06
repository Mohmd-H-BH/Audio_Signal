package com.testapp.audiosignal;

import android.content.Context;

import com.testapp.audiosignal.audio.player.AudioPlayer;
import com.testapp.audiosignal.audio.player.PlayerContract;
import com.testapp.audiosignal.repos.DataSource_JSON;
import com.testapp.audiosignal.repos.Prefs;
import com.testapp.audiosignal.repos.PrefsImpl;
import com.testapp.audiosignal.ui.MainContract;
import com.testapp.audiosignal.ui.MainPresenter;

import timber.log.Timber;

public class Injector {

    private static final String TAG = "Injector";
    private Context context;

    private DataSource_JSON dataSource_json;

    private MainContract.UserActionListener mainPresenter;

    public Injector(Context context){
        this.context = context;
    }

    public DataSource_JSON provideDataSource_JSON(){
        if( dataSource_json == null){
            dataSource_json = new DataSource_JSON();
            Timber.tag(TAG).d("inSide provideDataSource_JSON");
            DataSource_JSON dataSourceJSON = new DataSource_JSON();
            dataSourceJSON.getDataFromJSON_File(context);

            return dataSourceJSON;
        }
        return  dataSource_json;
    }


    public MainContract.UserActionListener provideMainPresenter() {
        if (mainPresenter == null) {
            mainPresenter = new MainPresenter(providePrefs(), provideAudioPlayer(), provideDataSource_JSON());
            return mainPresenter;
        }
        return mainPresenter;
    }

    private PlayerContract.Player provideAudioPlayer() {
        return AudioPlayer.getInstance();
    }

    private Prefs providePrefs() {
        return PrefsImpl.getInstance(context);
    }

    public void releaseMainPresenter(){
        if(mainPresenter != null){
            mainPresenter.clear();
            mainPresenter = null;
        }
    }
}
