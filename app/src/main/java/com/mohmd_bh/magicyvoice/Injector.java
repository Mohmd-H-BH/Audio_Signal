package com.mohmd_bh.magicyvoice;

import android.content.Context;

import com.mohmd_bh.magicyvoice.audio.player.AudioPlayer;
import com.mohmd_bh.magicyvoice.audio.player.PlayerContract;
import com.mohmd_bh.magicyvoice.repos.DataSource_JSON;
import com.mohmd_bh.magicyvoice.repos.Prefs;
import com.mohmd_bh.magicyvoice.repos.PrefsImpl;
import com.mohmd_bh.magicyvoice.ui.MainContract;
import com.mohmd_bh.magicyvoice.ui.MainPresenter;

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
