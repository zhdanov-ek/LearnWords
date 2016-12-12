/**
 * Активити для загрузки и выгрузки слов.
 * Поддерживается два варианта работы с файлами:
 * 1) текстовый файл с английским и русским словом разделенным \t
 * 2) json файл, который хранит все данные с БД, в том числе статистику ответов
 *
 * */

//todo Подумать над тем, что бы переделать загрузку слов с файла через BufferReader (как выгрузка)

package com.example.gek.learnwords.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gek.learnwords.R;
import com.example.gek.learnwords.data.Consts;
import com.example.gek.learnwords.data.DB;
import com.example.gek.learnwords.data.MyWord;
import com.example.gek.learnwords.data.SimpleWord;
import com.example.gek.learnwords.dialog.ResultDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class IEActivity extends AppCompatActivity implements View.OnClickListener{
    Button btnLoadNewWords, btnUnLoadNewWords, btnUnLoadNewDB, btnLoadNewDB;
    private EditText etFileLoadWords, etFileLoadDB;
    private ScrollView svContent;

    private LinearLayout llProgressContent;
    private TextView tvStatus;

    private File sdPathAbsolute;                     // карта памяти
    final static String TAG = IEActivity.class.getSimpleName();
    private Handler mHandler;
    Context mCtx;

    private ArrayList<SimpleWord> mResultDetail;          // полный журнал последней операции по импорту/экспорту
    private String mResultTotal;

    private int mShortAnimationDuration;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ie);
        mCtx = this;

        mResultDetail = new ArrayList<>();

        mHandler = new Handler();

        svContent = (ScrollView)findViewById(R.id.svContent);

        etFileLoadWords = (EditText) findViewById(R.id.etFileLoadWords);
        etFileLoadDB = (EditText) findViewById(R.id.etFileLoadDB);

        btnLoadNewWords = (Button)findViewById(R.id.btnLoadNewWords);
        btnLoadNewWords.setOnClickListener(this);
        btnUnLoadNewWords = (Button)findViewById(R.id.btnUnLoadNewWords);
        btnUnLoadNewWords.setOnClickListener(this);
        btnUnLoadNewDB = (Button)findViewById(R.id.btnUnLoadDB);
        btnUnLoadNewDB.setOnClickListener(this);
        btnLoadNewDB = (Button)findViewById(R.id.btnLoadDB);
        btnLoadNewDB.setOnClickListener(this);


        llProgressContent = (LinearLayout)findViewById(R.id.llProgressContent);
        tvStatus = (TextView)findViewById(R.id.tvStatus);

        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = 1000;
//                getResources().getInteger(android.R.integer.config_shortAnimTime);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            // Загрузка слов идет в отдельном потоке с отображением хода загрузки
            // Выгрузка происходит в основном потоке - очень быстро

            case R.id.btnLoadNewWords:
                if (checkSDCard()) {
                    // Скрываем основной интерфейс
                    svContent.setVisibility(View.GONE);

                    // Загружаем в другом потоке слова и отображаем с него текущее состояние
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            loadNewWords();
                        }
                    }).start();
                }
                break;
            case R.id.btnUnLoadNewWords:
                if (checkSDCard()) {
                    unLoadWords();
                }
                break;

            case R.id.btnUnLoadDB:
                if (checkSDCard()) {
                   unLoadDB();
                }
                break;

            case R.id.btnLoadDB:
                if (checkSDCard()) {
                    // Скрываем основной интерфейс,
                    svContent.setVisibility(View.GONE);

                    // Загружаем в другом потоке БД и отображаем с него текущее состояние
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            loadDB();
                        }
                    }).start();
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

    // Загрузка новых слов с текстового файла
    // Формат: English + симфолы табуляции + Russian
    // Слово добавляется только если его еще нет в базе
    private void loadNewWords(){
        File fileLoadWords = new File(sdPathAbsolute, etFileLoadWords.getText().toString());
//        String result = "";
        mResultDetail.clear();
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
                    final String status = words[0] + " - " + words[1];
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            tvStatus.setText(status);
                        }
                    });
                    counter++;
                    mResultDetail.add(new SimpleWord(words[0], words[1]));
                }
            }
            db.close();
            mResultTotal = counter +" new words added from \n" + fileLoadWords.getName();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
        finally {
            if (br != null ) {
                try {
                    br.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    showAnimation();
                    showSnackBar();
                }
            });
        }
    }

    // Выгрузка словаря в файл на карте памяти с разделителями TAB
    private void unLoadWords(){
        File fileUnLoadWords = new File(sdPathAbsolute, etFileLoadWords.getText().toString());
        BufferedWriter bw = null;
        try {
            // открываем поток для записи
            bw = new BufferedWriter(new FileWriter(fileUnLoadWords, false));
            // Подключаемся к базе
            DB db = new DB(this);
            db.open();
            int counter = 0;        //считаем сколько выгрузили в файл слов
            Cursor cursor = db.getAllData(Consts.LIST_TYPE_ALL, Consts.ORDER_BY_ABC, null);
            String str;
            String eng;
            String rus;

            mResultDetail.clear();
            // Перебираем каждую запись, формируем строку и пишем в файл
            if (cursor.moveToFirst()){
                do {
                    eng = cursor.getString(cursor.getColumnIndex(DB.COLUMN_ENG));
                    rus = cursor.getString(cursor.getColumnIndex(DB.COLUMN_RUS));
                    str = eng + "\t\t" + rus + "\n";
                    bw.write(str);
                    mResultDetail.add(new SimpleWord(eng, rus));
                    counter++;
                } while  (cursor.moveToNext());
            }
            mResultTotal = counter + " unloaded words in file \n" +
                    fileUnLoadWords.getName();
            showSnackBar();
            db.close();
        }
        catch (IOException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
        finally {
            if (bw != null ) {
                try {
                    bw.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }
    }

    // Выгружаем всю базу в файл формата JSON
    // выгружается информация по статистике ответов
    private void unLoadDB() {
        File fileUnLoadDB = new File(sdPathAbsolute, etFileLoadDB.getText().toString());
        BufferedWriter bw = null;
        try {
            // открываем поток для записи
            bw = new BufferedWriter(new FileWriter(fileUnLoadDB, false));
            // Подключаемся к базе
            DB db = new DB(this);
            db.open();
            int counter = 0;        //считаем сколько выгрузили в файл слов
            Cursor cursor = db.getAllData(Consts.LIST_TYPE_ALL, Consts.ORDER_BY_ABC, null);
            String eng;
            String rus;
            String answer_true;
            String answer_false;
            mResultDetail.clear();

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
                    mResultDetail.add(new SimpleWord(eng, rus));
                    counter++;
                } while (cursor.moveToNext());

                // Добавляем весь массив слов и записываем объект Json в файл
                jsonBase.put(Consts.ATT_WORDS, jsonWords);
                bw.write(jsonBase.toString());
            }
            mResultTotal = counter + " unloaded words in file \n" + fileUnLoadDB.getName();
            showSnackBar();
            db.close();

        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }
    }

    // Загружаем данные с Json файла
    private void loadDB() {
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
            mResultDetail.clear();

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
                        mResultDetail.add(new SimpleWord(eng, rus));

                        // показываем ход загрузки
                        final String status = eng + " - " + rus;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                tvStatus.setText(status);
                            }
                        });
                        counter++;
                    }
                }

            } catch (JSONException e){
                Log.e(TAG, e.toString());
                e.printStackTrace();
            }
            db.close();
            mResultTotal = counter +" new words added from \n" + fileLoadDB.getName();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    showAnimation();
                    showSnackBar();
                }
            });
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
        finally {
            if (br != null ) {
                try {
                    br.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    showAnimation();
                }
            });
        }
    }

    // Показываем плавно контент активити и скрыавем прогресбар
    private void showAnimation(){
        //todo пересмотреть тип анимации и задержку
        svContent.setAlpha(0f);
        svContent.setVisibility(View.VISIBLE);
        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        svContent.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);

        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        llProgressContent.animate()
                .alpha(0f)
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        llProgressContent.setVisibility(View.GONE);
                    }
                });

    }

    private void showSnackBar(){
        Snackbar snackbar = Snackbar.make(svContent, mResultTotal, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.details, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO показать диалоговое окно со списком слов, прокруткой и т.д.
                FragmentManager fragmentManager = getSupportFragmentManager();
                ResultDialogFragment resultDialogFragment =
                        ResultDialogFragment.newInstance(mResultTotal, mResultDetail);
                resultDialogFragment.show(fragmentManager, "results");

            }
        });
        snackbar.show();
    }
}
