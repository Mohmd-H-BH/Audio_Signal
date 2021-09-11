package com.mohmd_bh.magicyvoice.repos;

public interface Prefs {

    boolean isFirstRun();
    void firstRunExecuted();

    void setActiveFile(String path);

    String getActiveFile();

}
