package com.example.gek.learnwords;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Тестирование знаний: вопрос и 4 кнопки с вариантами ответов
 */

public class TestActivity extends Activity implements View.OnClickListener {
    private Button btn_next, btn_answer1, btn_answer2, btn_answer3, btn_answer4;
    private TextView tv_word;
    private DB db;
    private Cursor cursor;
    private String eng, rus;

    private int currentID = 0;              // текущий порядковый номер ID слова в списке всех ID
    ArrayList<Integer> wordsIDList;         // хранит рандомный список ID еще не протестированных слов
    private int[] threeFalseAnswerId;       // массив ложных альтернативных ID
    ArrayList<Integer> wordsIDListShowed;   // хранит рандомный список ID уже протестированных слов


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);
        db = new DB(this);
        db.open();

        tv_word = (TextView)findViewById(R.id.tv_word);
        btn_answer1 = (Button)findViewById(R.id.btn_answer1);
        btn_answer1.setOnClickListener(this);

        btn_answer2 = (Button)findViewById(R.id.btn_answer2);
        btn_answer2.setOnClickListener(this);

        btn_answer3 = (Button)findViewById(R.id.btn_answer3);
        btn_answer3.setOnClickListener(this);

        btn_answer4 = (Button)findViewById(R.id.btn_answer4);
        btn_answer4.setOnClickListener(this);

        btn_next = (Button)findViewById(R.id.btn_next);
        btn_next.setOnClickListener(this);

        // Получаем полный рандомный список ID всех слов со словаря
        wordsIDList = db.getFullRandomListID(db.getAllData(Consts.LIST_TYPE_ALL, null));

        showNextWord();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_next:
                showNextWord();
                break;
            default:
                break;
        }


    }

    // формируем и отображаем следующий вопрос с тремя ложными ответами
    private void showNextWord(){
        // получаем текущее слово
        ContentValues currentWord = db.getItem(wordsIDList.get(currentID));
        eng = currentWord.getAsString(DB.COLUMN_ENG);
        tv_word.setText(eng);
        rus = currentWord.getAsString(DB.COLUMN_RUS);
        // список где будут хранится все варианты ответа (для рандомной подачи на экран)
        ArrayList<String> answers = new ArrayList<>();
        answers.add(rus);

        // Получаем ID трех разных вариантов ложных ответов пропуская значение указаннов в currentID
        threeFalseAnswerId = Consts.getThreeId(wordsIDList.get(currentID), wordsIDList);
        for (int i = 0; i < threeFalseAnswerId.length; i++) {
            ContentValues answerFalse = db.getItem(threeFalseAnswerId[i]);
            answers.add(answerFalse.getAsString(DB.COLUMN_RUS));
        }

        ArrayList<Integer> randomNumList = db.makeRandomList(new ArrayList<Integer>(Arrays.asList(0,1,2,3)));

        btn_answer1.setText(answers.get(randomNumList.get(0)));
        btn_answer2.setText(answers.get(randomNumList.get(1)));
        btn_answer3.setText(answers.get(randomNumList.get(2)));
        btn_answer4.setText(answers.get(randomNumList.get(3)));

        // Пока номер текущего слова меньше размера массива всех ID слов можем выводить следующее слово
        if (wordsIDList.size() > (currentID +1)) {
            currentID++;
        } else
            btn_next.setEnabled(false);
    }
}
