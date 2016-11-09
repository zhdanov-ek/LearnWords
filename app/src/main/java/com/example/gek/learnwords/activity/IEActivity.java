/**
 * Активити для загрузки и выгрузки слов.
 * Поддерживается два варианта работы с файлами:
 * 1) текстовый файл с английским и русским словом разделенным \t
 * 2) json файл, который хранит все данные с БД, в том числе статистику ответов
 *
 * */

//todo Вынести загрузку и выгрузку данных в отдельные потоки с отображением хода загрузки

package com.example.gek.learnwords.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.gek.learnwords.R;
import com.example.gek.learnwords.data.Consts;
import com.example.gek.learnwords.data.DB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class IEActivity extends AppCompatActivity implements View.OnClickListener{
    Button btnLoadNewWords, btnUnLoadNewWords, btnUnLoadNewDB, btnLoadNewDB;
    private EditText etFileLoadWords, etFileLoadDB;
    private File sdPathAbsolute;        // карта памяти
    final static String LOG_TAG = IEActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ie);

        etFileLoadWords = (EditText) findViewById(R.id.etFileLoadWords);
        etFileLoadDB = (EditText) findViewById(R.id.etFileLoadDB);


        btnLoadNewWords = (Button)findViewById(R.id.btnLoadNewWords);
        btnLoadNewWords.setOnClickListener(this);
        btnUnLoadNewWords = (Button)findViewById(R.id.btnUnLoadNewWords);
        btnUnLoadNewWords.setOnClickListener(this);
        btnUnLoadNewDB = (Button)findViewById(R.id.btnUnLoadNewDB);
        btnUnLoadNewDB.setOnClickListener(this);
        btnLoadNewDB = (Button)findViewById(R.id.btnLoadNewDB);
        btnLoadNewDB.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            // Добавляем слова с локального текстового файла в корне флешки.
            // формат записи в файле:"cat   кот". Количество табов любое
            case R.id.btnLoadNewWords:
                if (checkSDCard()) {
                    File fileLoadWords = new File(sdPathAbsolute, etFileLoadWords.getText().toString());
                    String result = "";
                    BufferedReader br = null;
                    try {
                        // открываем поток для чтения
                        br = new BufferedReader(new FileReader(fileLoadWords));
                        // Подключаемся к базе
                        DB db = new DB(this);
                        db.open();
                        String str;
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
                    } catch (IOException e) {
                        Log.e(LOG_TAG, e.toString());
                        e.printStackTrace();
                    }
                    finally {
                        if (br != null ) {
                            try {
                                br.close();
                            } catch (final IOException e) {
                                Log.e(LOG_TAG, "Error closing stream", e);
                            }
                        }
                    }
                }
                break;

            // Выгрузка словаря в файл на карте памяти с разделителями TAB
            case R.id.btnUnLoadNewWords:
                if (checkSDCard()) {
                    File fileUnLoadWords = new File(sdPathAbsolute, etFileLoadWords.getText().toString());
                    BufferedWriter bw = null;
                    try {
                        // открываем поток для записи
                        bw = new BufferedWriter(new FileWriter(fileUnLoadWords, false));
                        // Подключаемся к базе
                        DB db = new DB(this);
                        db.open();
                        int counter = 0;        //считаем сколько выгрузили в файл слов
                        Cursor cursor = db.getAllData(Consts.LIST_TYPE_ALL, null);
                        String str;
                        String eng;
                        String rus;
                        cursor.moveToFirst();

                        // Перебираем каждую запись, формируем строку и пишем в файл
                        while (cursor.moveToNext()) {
                            eng = cursor.getString(cursor.getColumnIndex(DB.COLUMN_ENG));
                            rus = cursor.getString(cursor.getColumnIndex(DB.COLUMN_RUS));
                            str = eng + "\t\t" + rus + "\n";
                            bw.write(str);
                            counter++;
                        }
                        Toast.makeText(this, counter + " unloaded words in file \n" +
                                        fileUnLoadWords.getAbsolutePath(),
                                Toast.LENGTH_LONG).show();
                        db.close();
                    }
                    catch (IOException e) {
                        Log.e(LOG_TAG, e.toString());
                        e.printStackTrace();
                    }
                    finally {
                        if (bw != null ) {
                            try {
                                bw.close();
                            } catch (final IOException e) {
                                Log.e(LOG_TAG, "Error closing stream", e);
                            }
                        }
                    }
                }
                break;

            // Выгружаем всю базу в файл формата JSON
            case R.id.btnUnLoadNewDB:
                if (checkSDCard()) {
                    File fileUnLoadDB = new File(sdPathAbsolute, etFileLoadDB.getText().toString());
                    BufferedWriter bw = null;
                    try {
                        // открываем поток для записи
                        bw = new BufferedWriter(new FileWriter(fileUnLoadDB, false));
                        // Подключаемся к базе
                        DB db = new DB(this);
                        db.open();
                        int counter = 0;        //считаем сколько выгрузили в файл слов
                        Cursor cursor = db.getAllData(Consts.LIST_TYPE_ALL, null);
                        String eng;
                        String rus;
                        String answer_true;
                        String answer_false;

                        // Работаем если вообще есть первый элемент в базе данных
                        if (cursor.moveToFirst()) {
                            JSONObject jsonBase = new JSONObject();
                            JSONArray jsonWords = new JSONArray();
                            // Перебираем курсор, формируем Json объект по слову и добавляем его в массив
                            do {
                                JSONObject jsonWord = new JSONObject();
                                eng = cursor.getString(cursor.getColumnIndex(DB.COLUMN_ENG));
                                jsonWord.put(Consts.ATT_ENG, eng);
                                rus = cursor.getString(cursor.getColumnIndex(DB.COLUMN_RUS));
                                jsonWord.put(Consts.ATT_RUS, rus);
                                answer_true = cursor.getString(cursor.getColumnIndex(DB.COLUMN_TRUE));
                                jsonWord.put(Consts.ATT_TRUE, answer_true);
                                answer_false = cursor.getString(cursor.getColumnIndex(DB.COLUMN_FALSE));
                                jsonWord.put(Consts.ATT_FALSE, answer_false);

                                // ДОбавляем слово
                                jsonWords.put(jsonWord);
                                counter++;
                            } while (cursor.moveToNext());

                            // Добавляем весь массив слов и записываем объект Json в файл
                            jsonBase.put(Consts.ATT_WORDS, jsonWords);
                            bw.write(jsonBase.toString());
                        }
                        Toast.makeText(this, counter +" unloaded words in file \n" +
                                        fileUnLoadDB.getAbsolutePath(),
                                Toast.LENGTH_LONG).show();
                        db.close();

                    }
                    catch (JSONException e) {
                        Log.e(LOG_TAG, e.toString());
                        e.printStackTrace();
                    }
                    catch (IOException e) {
                        Log.e(LOG_TAG, e.toString());
                        e.printStackTrace();
                    }
                    finally {
                        if (bw != null ) {
                            try {
                                bw.close();
                            } catch (final IOException e) {
                                Log.e(LOG_TAG, "Error closing stream", e);
                            }
                        }
                    }
                }
                break;

            // Загружаем данные с Json файла
            case R.id.btnLoadNewDB:
                if (checkSDCard()) {
                    File fileLoadDB = new File(sdPathAbsolute, etFileLoadDB.getText().toString());
                    String jsonStr = "";
                    BufferedReader br = null;
                    try {
                        // открываем поток для чтения
                        br = new BufferedReader(new FileReader(fileLoadDB));

                        // Сначала читаем наш json файл
                        String str;
                        while ((str = br.readLine()) != null){
                            jsonStr += str;
                        }

                        // Подключаемся к базе
                        DB db = new DB(this);
                        db.open();
                        int counter = 0;

                        try {
                            // Создаем объект Json и получаем массив слов. Перебираем каждое слово
                            JSONObject jsonBase = new JSONObject(jsonStr);
                            JSONArray jsonWords = jsonBase.getJSONArray(Consts.ATT_WORDS);
                            JSONObject jsonWord;
                            String eng;
                            String rus;
                            int answer_true;
                            int answer_false;


                            for (int i = 0; i < jsonWords.length(); i++) {
                                jsonWord = jsonWords.getJSONObject(i);
                                eng = jsonWord.getString(Consts.ATT_ENG);
                                rus = jsonWord.getString(Consts.ATT_RUS);
                                answer_true = jsonWord.getInt((Consts.ATT_TRUE));
                                answer_false = jsonWord.getInt((Consts.ATT_FALSE));
                                if (!db.checkIsPresentWord(eng)) {
                                    db.addRec(eng, rus, answer_true, answer_false);
                                    counter++;
                                }
                            }

                        } catch (JSONException e){
                            Log.e(LOG_TAG, e.toString());
                            e.printStackTrace();
                        }
                        db.close();
                        Toast.makeText(this, counter +" new words added from \n" +
                                fileLoadDB.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, e.toString());
                        e.printStackTrace();
                    }
                    finally {
                        if (br != null ) {
                            try {
                                br.close();
                            } catch (final IOException e) {
                                Log.e(LOG_TAG, "Error closing stream", e);
                            }
                        }
                    }
                }
                break;

            default:
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
