/**
 * Тестирование знаний: вопрос и 4 кнопки с вариантами ответов
 */

package com.example.gek.learnwords.activity;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gek.learnwords.R;
import com.example.gek.learnwords.data.Consts;
import com.example.gek.learnwords.data.DB;

import java.util.ArrayList;
import java.util.Arrays;


public class TestActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn_next, btn_answer1, btn_answer2, btn_answer3, btn_answer4;
    private TextView tv_word;
    private DB db;
    private String eng, rus;                // значения текущего слова
    int id, counterTrue, counterFalse;

    private int currentID = 0;              // текущий порядковый номер ID слова в списке всех ID
    ArrayList<Integer> wordsIDList;         // хранит рандомный список ID еще не протестированных слов
    private int[] threeFalseAnswerId;       // массив ложных альтернативных ID



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
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
        // wordsIDList = db.getFullRandomListID(db.getAllData(Consts.LIST_TYPE_ALL, null));

        wordsIDList = db.getFullListID(db.getAllData(Consts.LIST_TYPE_ALL, null), false);

        showNextWord();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_next:
                showNextWord();
                break;
            case R.id.btn_answer1:
            case R.id.btn_answer2:
            case R.id.btn_answer3:
            case R.id.btn_answer4:
                checkAnswer((Button)findViewById(v.getId()));
                break;
            default:
                break;
        }


    }

    /** формируем и отображаем следующий вопрос с тремя ложными ответами */
    private void showNextWord(){
        btn_answer1.setBackgroundResource(R.drawable.bg_button_grey);
        btn_answer2.setBackgroundResource(R.drawable.bg_button_grey);
        btn_answer3.setBackgroundResource(R.drawable.bg_button_grey);
        btn_answer4.setBackgroundResource(R.drawable.bg_button_grey);
        setAnswersClickable(true);


        // получаем текущее слово
        ContentValues currentWord = db.getItem(wordsIDList.get(currentID));
        eng = currentWord.getAsString(DB.COLUMN_ENG);
        rus = currentWord.getAsString(DB.COLUMN_RUS);
        id = wordsIDList.get(currentID);
        counterTrue = currentWord.getAsInteger(DB.COLUMN_TRUE);
        counterFalse = currentWord.getAsInteger(DB.COLUMN_FALSE);

        tv_word.setText(eng);
        // список где будут хранится все варианты ответа (для рандомной подачи на экран)
        ArrayList<String> answers = new ArrayList<>();
        answers.add(rus);

        // Получаем ID трех разных вариантов ложных ответов пропуская значение указанные в currentID
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
        } else {
            //todo Тут лучше скрыть кнопки и вывести сообщение или статистику по ответам
            btn_next.setEnabled(false);
            btn_answer1.setEnabled(false);
            btn_answer2.setEnabled(false);
            btn_answer3.setEnabled(false);
            btn_answer4.setEnabled(false);
            Toast.makeText(this, "This is last word!", Toast.LENGTH_SHORT).show();
        }

    }

    /** Проверяем правильно ли выбран ответ */
    private  void checkAnswer(Button b){
        if (b.getText().toString().contentEquals(rus)){
            Toast.makeText(this, "Правильно", Toast.LENGTH_SHORT).show();
            //todo Написать метод, который будет вместо сообщения "Правильно" выводить какую-то анимацию или подарки
            registrationAnswer(true);   // отмечаем в БД, что дан правильный ответ
            showNextWord();
        } else {
            b.setBackgroundResource(R.drawable.bg_button_red);
            Toast.makeText(this, "Не верно", Toast.LENGTH_SHORT).show();
            if (btn_answer1.getText() == rus)
                btn_answer1.setBackgroundResource(R.drawable.bg_button_green);
            if (btn_answer2.getText() == rus)
                btn_answer2.setBackgroundResource(R.drawable.bg_button_green);
            if (btn_answer3.getText() == rus)
                btn_answer3.setBackgroundResource(R.drawable.bg_button_green);
            if (btn_answer4.getText() == rus)
                btn_answer4.setBackgroundResource(R.drawable.bg_button_green);
            registrationAnswer(false);   // отмечаем в БД, что дан ложный ответ
            setAnswersClickable(false);
        }


    }

    /** Включение/отключение кнопок в зависимости от того был ли выбран вариант */
    private void setAnswersClickable(Boolean b){
        btn_answer1.setClickable(b);
        btn_answer2.setClickable(b);
        btn_answer3.setClickable(b);
        btn_answer4.setClickable(b);
        btn_next.setEnabled(!b);
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
    }

    /**  Закрытие базы перед уничтожением активити */
    protected void onDestroy() {
        super.onDestroy();
        // закрываем подключение при выходе
        db.close();
    }
}
