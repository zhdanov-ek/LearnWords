package com.example.gek.learnwords;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class IEActivity extends AppCompatActivity implements View.OnClickListener{
    Button btnLoadNewWords;
    EditText etFileLoadWords;
    File sdPathAbsolute;        // карта памяти
    File fileLoadWords;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ie);

        etFileLoadWords = (EditText) findViewById(R.id.etFileLoadWords);

        btnLoadNewWords = (Button)findViewById(R.id.btnLoadNewWords);
        btnLoadNewWords.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            // Добавляем слова с локального текстового файла в корне флешки.
            // формат записи в файле:"cat   кот". Количество табов любое
            case R.id.btnLoadNewWords:
                if (checkSDCard()) {
                    fileLoadWords = new File(sdPathAbsolute, etFileLoadWords.getText().toString());
                    String result = "";
                    try {
                        // открываем поток для чтения
                        BufferedReader br = new BufferedReader(new FileReader(fileLoadWords));
                        // Подключаемся к базе
                        DB db = new DB(this);
                        db.open();
                        String str = "";
                        int counter = 0;        //считаем сколько добавилось слов
                        // читаем содержимое файл и парсим каждую строку методом String.split
                        while (((str = br.readLine()) != null) && (str.length()>5)) {
                            String delimiter = "[\t]+";
                            String[] words = str.split(delimiter);
                            // Проверяем нет ли такого слова уже в словаре и после этого добавляем
                            if (!db.checkIsPresentWord(words[0])) {
                                db.addRec(words[0], words[1]);
                                result = result + ++counter + ") " + words[0] + " - " + words[1] +"\n";
                            }
                        }
                        db.close();
                        Toast.makeText(this, counter +" new words added. \n" +
                           result, Toast.LENGTH_LONG).show();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }

    }

    private boolean checkSDCard(){
        // проверяем доступность SD карты и если она есть определяем путь к ней
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "SD card not found: " + Environment.getExternalStorageState(),
                    Toast.LENGTH_LONG).show();
            return false;

        } else {
            // получаем путь к SD от системы в объект типа File
            sdPathAbsolute = Environment.getExternalStorageDirectory();
            return true;
        }
    }

}
