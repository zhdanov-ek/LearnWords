/**
 * Режим изучения слов в котором подаются на экран слова по случайно сформированному списку
 * Пользователь сам указывает знает он это слово или не знает. Ответы фиксируются в БД
 */

package com.example.gek.learnwords.activity;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gek.learnwords.R;
import com.example.gek.learnwords.data.Consts;
import com.example.gek.learnwords.data.DB;

import java.util.ArrayList;

//todo ДОбавить возможность переводить в направлении согласно  настроек программы. Отладить работу


public class InputActivity extends AppCompatActivity implements View.OnClickListener{
    private Button btnDontKnow, btnCheck;
    private TextView tvLearnEng, tvLearnRus;
    private EditText etTranslate;
    private ImageView ivResult;

    private static final String TAG = "GEK";

    private DB db;
    private Cursor cursor;
    private String eng, rus;                        // значения текущего слова
    private int id, counterTrue, counterFalse;      // значения текущего слова
    private int nextID = 0;                         // служит для перебора по рандомногому списку ID слов
    private ArrayList<Integer> wordsIDList;         // хранит рандомный список ID слов

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

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
        if (rus.contains(answer)) {
            return true;
        } else {
            return false;

        }


    }


    /** Выводим следующее слово на экран */
    private void showNextWord(int idWord){
        etTranslate.setText("");
        ContentValues cv = db.getItem(idWord);
        id = idWord;
        eng = cv.getAsString(DB.COLUMN_ENG);
        rus = cv.getAsString(DB.COLUMN_RUS);
        counterTrue = cv.getAsInteger(DB.COLUMN_TRUE);
        counterFalse = cv.getAsInteger(DB.COLUMN_FALSE);

        tvLearnEng.setText(eng);
        tvLearnRus.setText(rus);
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
