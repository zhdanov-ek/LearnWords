/**
 * Режим изучения слов в котором подаются на экран слова по случайно сформированному списку
 * Пользователь сам указывает знает он это слово или не знает. Ответы фиксируются в БД
 */

package com.example.gek.learnwords.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gek.learnwords.R;
import com.example.gek.learnwords.data.Consts;
import com.example.gek.learnwords.data.DB;
import com.example.gek.learnwords.data.RecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Random;


public class InputActivity extends AppCompatActivity implements View.OnClickListener{
    private Button btnDontKnow, btnCheck;
    private TextView tvLearnEng, tvLearnRus;
    private EditText etTranslate;
    private ImageView ivResult;

    private static final String TAG = "GEK";

    private String mPrefDirection;           // направление перевода
    private String mColumnWordOriginal;      // Значение поля (rus/eng) с которого переводим текущее слово

    //todo выводить статистику правильных и неправильных ответов в конце
    private int mTotalTrueAnswers, mTotalFalseAnswers;

    private Context ctx;

    private DB db;
    private Cursor cursor;

    private String mWordOriginal, mWordTranslate;                // значения текущего слова
    private int id, counterTrue, counterFalse;      // значения текущего слова
    private int nextID = 0;                         // служит для перебора по рандомногому списку ID слов
    private ArrayList<Integer> wordsIDList;         // хранит рандомный список ID слов

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        ctx = getBaseContext();

        // Добавляем тулбар бар
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(myToolbar);
        myToolbar.setTitle(R.string.caption_input);

        db = new DB(this);
        db.open();
        cursor = db.getAllData(Consts.LIST_TYPE_ALL, Consts.ORDER_BY_RATING, null);
        wordsIDList = db.getFullListID(cursor, false);

        ivResult = (ImageView) findViewById(R.id.ivResult);
        tvLearnEng = (TextView) findViewById(R.id.tvLearnEng);
        tvLearnRus = (TextView) findViewById(R.id.tvLearnRus);
        etTranslate = (EditText) findViewById(R.id.etTranslate);
        btnDontKnow = (Button)findViewById(R.id.btnDontKnow);
        btnDontKnow.setOnClickListener(this);
        btnCheck = (Button)findViewById(R.id.btnCheck);
        btnCheck.setOnClickListener(this);
    }

    // Указываем как нам формировать меню
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    // Реакция на нажатие кнопок в меню
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.ab_settings:
                if (!item.isChecked()) {
                    Intent intentSet = new Intent(ctx,SettingsActivity.class);
                    startActivity(intentSet);
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Получаем с настроек значение направления перевода и показываем первое слово*/
    @Override
    protected void onStart() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        mPrefDirection = prefs.getString(
                getResources().getString(R.string.pref_direction_key),
                getResources().getString(R.string.pref_direction_default));
        setDirectionTranslate();

        // показываем первое слово
        showNextWord(wordsIDList.get(nextID));
        super.onStart();
    }

    /** В зависисости от режима перевода слов определяем какое поле будет базовым */
    private void setDirectionTranslate(){
        switch (mPrefDirection){
            case "direction_rus":
                mColumnWordOriginal = DB.COLUMN_ENG;
                break;
            case "direction_eng":
                mColumnWordOriginal = DB.COLUMN_RUS;
                break;
            case "direction_mix":
                Random random = new Random();
                if (random.nextBoolean()){
                    mColumnWordOriginal = DB.COLUMN_ENG;
                } else {
                    mColumnWordOriginal = DB.COLUMN_RUS;
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnDontKnow:
                registrationAnswer(false);
                break;
            // Если на кнопке надпись ДАЛЕЕ то показываем новое слово, иначе - отрабатываем ответ
            case R.id.btnCheck:
                if (btnCheck.getText().toString().contentEquals(getResources().getString(R.string.next))) {
                    showNextWord(wordsIDList.get(nextID));
                } else {
                    if (checkAnswer()) {
                        registrationAnswer(true);
                    } else {
                        registrationAnswer(false);
                    }
                }
                break;
        }
    }

    /** Проверяем правильный ли дал пользователь ответ */
    private boolean checkAnswer(){
        String answer = etTranslate.getText().toString();
        if (mWordOriginal.contains(answer)) {
            return true;
        } else {
            return false;
        }
    }


    /** Выводим следующее слово на экран */
    private void showNextWord(int idWord){
        setDirectionTranslate();
        etTranslate.setText("");
        ContentValues cv = db.getItem(idWord);
        id = idWord;
        if (mColumnWordOriginal.contentEquals(DB.COLUMN_ENG)){
            mWordOriginal = cv.getAsString(DB.COLUMN_ENG);
            mWordTranslate = cv.getAsString(DB.COLUMN_RUS);
        } else {
            mWordOriginal = cv.getAsString(DB.COLUMN_RUS);
            mWordTranslate = cv.getAsString(DB.COLUMN_ENG);
        }

        counterTrue = cv.getAsInteger(DB.COLUMN_TRUE);
        counterFalse = cv.getAsInteger(DB.COLUMN_FALSE);

        tvLearnEng.setText(mWordOriginal);
        tvLearnRus.setText(mWordTranslate);
        tvLearnRus.setVisibility(View.INVISIBLE);

        btnDontKnow.setEnabled(true);
        btnCheck.setText(R.string.ok);
        ivResult.setVisibility(View.INVISIBLE);
    }

    /** Изменяем в БД кол-во правильных и неправильны ответов по конкретному слову */
    private void registrationAnswer(boolean answer){
        showIcon(answer);
        // Если нажали ЗНАЮ, то увеличиваем кол-во правильных ответов. В противном случае - неправильных
        if (answer) {
            counterTrue++;
        }
        else {
            counterFalse++;
        }
        ContentValues cv = new ContentValues();
        cv.put(DB.COLUMN_TRUE, counterTrue);
        cv.put(DB.COLUMN_FALSE, counterFalse);
        cv.put(DB.COLUMN_LEVEL, counterTrue - counterFalse);
        db.changeRec(cv, Integer.toString(id));

        tvLearnRus.setVisibility(View.VISIBLE);

        // Инкрементируем указатель для выбора следующего слова в рандомном списке
        // если это еще не конец списка. Иначе - прекращаем работу в этом окне
        if (wordsIDList.size() == (nextID+1)) {
            // Если это последнее слово то блокируем кнопки и информируем юзера
            btnCheck.setVisibility(View.INVISIBLE);
            btnDontKnow.setVisibility(View.INVISIBLE);
        } else {
            nextID++;
            btnCheck.setText(R.string.next);
            btnDontKnow.setEnabled(false);
        }

    }

    /**  Закрытие базы перед уничтожением активити */
    protected void onDestroy() {
        super.onDestroy();
        // закрываем подключение при выходе
        db.close();
    }


    /**  Отображение иконки  */
    private void showIcon(Boolean answer){
        if (answer) {
            ivResult.setImageResource(R.drawable.correctly_icon);
        } else {
            ivResult.setImageResource(R.drawable.not_correctly_icon);
        }
        ivResult.setVisibility(View.VISIBLE);
    }
}
