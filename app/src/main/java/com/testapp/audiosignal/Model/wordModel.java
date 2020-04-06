package com.testapp.audiosignal.Model;

public class wordModel {

    double start_time;

    double end_time;

    String text;

    double confidence;

    boolean strikethrough;

    boolean highlight;

    boolean bold;

    String color;


    public wordModel() {
    }

    public wordModel(double start_time, double end_time, String text, double confidence, boolean strikethrough, boolean highlight, boolean bold, String color) {
        this.start_time = start_time;
        this.end_time = end_time;
        this.text = text;
        this.confidence = confidence;
        this.strikethrough = strikethrough;
        this.highlight = highlight;
        this.bold = bold;
        this.color = color;
    }

    public double getStart_time() {
        return start_time;
    }

    public void setStart_time(double start_time) {
        this.start_time = start_time;
    }

    public double getEnd_time() {
        return end_time;
    }

    public void setEnd_time(double end_time) {
        this.end_time = end_time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public boolean isStrikethrough() {
        return strikethrough;
    }

    public void setStrikethrough(boolean strikethrough) {
        this.strikethrough = strikethrough;
    }

    public boolean isHighlight() {
        return highlight;
    }

    public void setHighlight(boolean highlight) {
        this.highlight = highlight;
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
