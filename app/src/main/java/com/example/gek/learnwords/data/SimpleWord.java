package com.example.gek.learnwords.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Класс для хранения данных во время импорта/экспорта
 */

public class SimpleWord implements Parcelable{
    private String eng;
    private String rus;

    // Обычный конструктор
    public SimpleWord(String eng, String rus) {
        this.eng = eng;
        this.rus = rus;
    }

    public SimpleWord() {
    }

    public String getEng() {
        return eng;
    }

    public void setEng(String eng) {
        this.eng = eng;
    }

    public String getRus() {
        return rus;
    }

    public void setRus(String rus) {
        this.rus = rus;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Конструктор для парсейбл принимает на вход массив
    public SimpleWord(Parcel in) {
        String[] data = new String[2];
        in.readStringArray(data);
        eng = data[0];
        rus = data[1];
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[]{eng, rus});
    }

    public static final Parcelable.Creator<SimpleWord> CREATOR = new Parcelable.Creator<SimpleWord>() {

        @Override
        public SimpleWord createFromParcel(Parcel source) {
            return new SimpleWord(source);
        }

        @Override
        public SimpleWord[] newArray(int size) {
            return new SimpleWord[size];
        }
    };
}