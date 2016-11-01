/**
 * Режим изучения слов в котором подаются на экран слова по случайно сформированному списку
 * Пользователь сам указывает знает он это слово или не знает. Ответы фиксируются в БД
 */

package com.example.gek.learnwords;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;


public class LearnActivity extends AppCompatActivity implements View.OnClickListener{
    private Button btnDontKnow, btnKnow;
    private TextView tvLearnEng, tvLearnRus, tvLearnResult;

    private DB db;
    private Cursor cursor;
    private String eng, rus;                        // значения текущего слова
    private int id, counterTrue, counterFalse;      // значения текущего слова
    private int nextID = 0;                         // служит для перебора по рандомногому списку ID слов
    private ArrayList<Integer> wordsIDList;         // хранит рандомный список ID слов

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        db = new DB(this);
        db.open();
        cursor = db.getAllData(Consts.LIST_TYPE_ALL, null);
        wordsIDList = db.getFullListID(cursor, false);

        tvLearnEng = (TextView) findViewById(R.id.tvLearnEng);
        tvLearnRus = (TextView) findViewById(R.id.tvLearnRus);
        tvLearnResult = (TextView) findViewById(R.id.tvLearnResult);

        btnDontKnow = (Button)findViewById(R.id.btnDontKnow);
        btnDontKnow.setOnClickListener(this);

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
            // Если на кнопке надпись ДАЛЕЕ то показываем новое слово, иначе - отрабатываем ответ
            case R.id.btnKnow:
                if (btnKnow.getText().toString().contentEquals(getResources().getString(R.string.next))) {
                    showNextWord(wordsIDList.get(nextID));
                } else {
                    registrationAnswer(true);
                }
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

        btnDontKnow.setEnabled(true);
        btnKnow.setText(R.string.know);
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
        cv.put(DB.COLUMN_LEVEL, counterTrue - counterFalse);
        db.changeRec(cv, Integer.toString(id));

        tvLearnRus.setVisibility(View.VISIBLE);

        // Инкрементируем указатель для выбора следующего слова в рандомном списке
        // если это еще не конец списка. Иначе - прекращаем работу в этом окне
        if (wordsIDList.size() == (nextID+1)) {
            // Если это последнее слово то блокируем кнопки и информируем юзера
            btnKnow.setVisibility(View.INVISIBLE);
            btnDontKnow.setVisibility(View.INVISIBLE);
        } else {
            nextID++;
            btnKnow.setText(R.string.next);
            btnDontKnow.setEnabled(false);
        }

    }

    /**  Закрытие базы перед уничтожением активити */
    protected void onDestroy() {
        super.onDestroy();
        // закрываем подключение при выходе
        db.close();
    }
}
