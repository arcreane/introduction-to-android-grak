package com.example.headsup.models;

import android.os.Parcel;
import android.os.Parcelable;

public class WordResult implements Parcelable {
    private final String word;
    private final boolean correct;
    private final long timeTaken; // in milliseconds

    public WordResult(String word, boolean correct, long timeTaken) {
        this.word = word;
        this.correct = correct;
        this.timeTaken = timeTaken;
    }

    protected WordResult(Parcel in) {
        word = in.readString();
        correct = in.readByte() != 0;
        timeTaken = in.readLong();
    }

    public static final Creator<WordResult> CREATOR = new Creator<WordResult>() {
        @Override
        public WordResult createFromParcel(Parcel in) {
            return new WordResult(in);
        }

        @Override
        public WordResult[] newArray(int size) {
            return new WordResult[size];
        }
    };

    public String getWord() {
        return word;
    }

    public boolean isCorrect() {
        return correct;
    }

    public long getTimeTaken() {
        return timeTaken;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(word);
        dest.writeByte((byte) (correct ? 1 : 0));
        dest.writeLong(timeTaken);
    }
} 