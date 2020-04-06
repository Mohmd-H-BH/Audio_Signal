package com.testapp.audiosignal.repos;

public interface Prefs {

    boolean isFirstRun();
    void firstRunExecuted();

    void setActiveFile(String path);

    String getActiveFile();

}
