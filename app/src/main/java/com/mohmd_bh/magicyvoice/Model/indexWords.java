package com.mohmd_bh.magicyvoice.Model;

public class indexWords {

    String text;

    int length;

    int startIndex;

    int endIndex;

    public indexWords(String text, int length, int startIndex, int endIndex) {
        this.text = text;
        this.length = length;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public indexWords() {
        this.text = "";
        this.length = 0;
        this.startIndex = 0;
        this.endIndex = 0;
    }



    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }
}
