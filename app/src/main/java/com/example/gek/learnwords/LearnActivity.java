package com.example.gek.learnwords;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by gek on 16.10.16.
 */

public class LearnActivity extends Activity implements View.OnClickListener{
    Button btnDontKnow, btnNext, btnKnow;
    TextView tvLearnEng, tvLearnRus, tvLearnResult;

    DB db;
    Cursor cursor;
    String eng, rus;                        // значения текущего слова
    int id, counterTrue, counterFalse;      // значения текущего слова
    int nextID = 0;                         // служит для перебора по рандомногому списку ID слов
    ArrayList<Integer> wordsIDList;         // хранит рандомный список ID слов

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.learn_layout);

        db = new DB(this);
        db.open();
        cursor = db.getAllData(Consts.LIST_TYPE_ALL, null);
        wordsIDList = db.getFullRandomListID(cursor);

        tvLearnEng = (TextView) findViewById(R.id.tvLearnEng);
        tvLearnRus = (TextView) findViewById(R.id.tvLearnRus);
        tvLearnResult = (TextView) findViewById(R.id.tvLearnResult);

        btnDontKnow = (Button)findViewById(R.id.btnDontKnow);
        btnDontKnow.setOnClickListener(this);

        btnNext = (Button)findViewById(R.id.btnNext);
        btnNext.setOnClickListener(this);

        btnKnow = (Button)findViewById(R.id.btnKnow);
        btnKnow.setOnClickListener(this);

        // показываем первое слово
        showNextWord(wordsIDList.get(nextID));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnDontKnow:
                registrationAnswer(false);
                break;
            case R.id.btnNext:
                showNextWord(wordsIDList.get(nextID));
                break;
            case R.id.btnKnow:
                registrationAnswer(true);
                break;

        }
    }



    /** Выводим следующее слово на экран */
    private void showNextWord(int idWord){
        ContentValues cv = db.getItem(idWord);
        id = idWord;
        eng = cv.getAsString(DB.COLUMN_ENG);
        rus = cv.getAsString(DB.COLUMN_RUS);
        counterTrue = cv.getAsInteger(DB.COLUMN_TRUE);
        counterFalse = cv.getAsInteger(DB.COLUMN_FALSE);

        tvLearnEng.setText(eng);
        tvLearnRus.setText(rus);
        tvLearnRus.setVisibility(View.INVISIBLE);

        tvLearnResult.setVisibility(View.INVISIBLE);

        btnDontKnow.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.INVISIBLE);
        btnKnow.setVisibility(View.VISIBLE);
    }

    /** Изменяем в БД кол-во правильных и неправильны ответов по конкретному слову */
    private void registrationAnswer(boolean answer){
        // Если нажали ЗНАЮ, то увеличиваем кол-во правильных ответов. В противном случае - неправильных
        if (answer)
            counterTrue++;
        else
            counterFalse++;
        ContentValues cv = new ContentValues();
        cv.put(DB.COLUMN_ENG, eng);
        cv.put(DB.COLUMN_RUS, rus);
        cv.put(DB.COLUMN_TRUE, counterTrue);
        cv.put(DB.COLUMN_FALSE, counterFalse);
        db.changeRec(cv, Integer.toString(id));

        tvLearnRus.setVisibility(View.VISIBLE);
        btnDontKnow.setVisibility(View.INVISIBLE);
        btnKnow.setVisibility(View.INVISIBLE);

        // Инкрементируем указатель для выбора следующего слова в рандомном списке
        // если это еще не конец списка. Иначе - прекращаем работу в этом окне
        if (wordsIDList.size() == (nextID+1)) {
            // Если это последнее слово то блокируем кнопки и информируем юзера
            btnNext.setVisibility(View.INVISIBLE);
        } else {
            nextID++;
            btnNext.setVisibility(View.VISIBLE);
        }

    }


    /**  Обязательные абстрактыне методы для актвити с буфером*/
    protected void onDestroy() {
        super.onDestroy();
        // закрываем подключение при выходе
        db.close();
    }

}
