package com.mohmd_bh.magicyvoice.Model;

public class metaModel {

    private String speaker;

    private boolean completed;

    private String notes;


    public metaModel(){
        speaker = "";
        completed = false;
        notes = "";
    }

    public metaModel(String speaker, boolean completed, String notes) {
        this.speaker = speaker;
        this.completed = completed;
        this.notes = notes;
    }

    public String getSpeaker() {
        return speaker;
    }

    public void setSpeaker(String speaker) {
        this.speaker = speaker;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
