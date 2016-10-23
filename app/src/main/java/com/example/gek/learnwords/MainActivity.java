package com.example.gek.learnwords;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_number_words;
    private Button btnLearn, btnTest, btnWords, btnAddWord, btnImportExport;
    private DB db;
    Context ctx;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ctx = getApplicationContext();
        tv_number_words = (TextView)findViewById(R.id.tv_number_words);

        btnLearn = (Button)findViewById(R.id.btnLearn);
        btnLearn.setOnClickListener(this);


        btnTest = (Button)findViewById(R.id.btnTest);
        btnTest.setOnClickListener(this);

        btnWords = (Button)findViewById(R.id.btnWords);
        btnWords.setOnClickListener(this);

        btnAddWord = (Button)findViewById(R.id.btnAddWord);
        btnAddWord.setOnClickListener(this);

        btnImportExport = (Button)findViewById(R.id.btnImportExport);
        btnImportExport.setOnClickListener(this);

        // открываем подключение к БД
        db = new DB(this);
        db.open();


    }

    // перед отрисовкой основного меню (при запуске или при закрытии других активити программы)
    // выводим кол-во слов в словаре, а также блокируем режим тестирования если слов мало
    @Override
    public void onResume(){
        super.onResume();
        int num = db.getNumberWords();
        if (num < 5)
            btnTest.setEnabled(false);
        else btnTest.setEnabled(true);

        tv_number_words.setText("In dictionary " + num + " words" );
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnLearn:
                Intent intentLearn = new Intent(ctx, LearnActivity.class);
                startActivity(intentLearn);
                break;

            case R.id.btnTest:
                Intent intentTest = new Intent(ctx, TestActivity.class);
                startActivity(intentTest);
                break;

            case R.id.btnAddWord:
                Intent intentAddWord = new Intent(ctx, WordActivity.class);
                intentAddWord.putExtra(Consts.WORD_MODE, Consts.WORD_NEW);
                startActivityForResult(intentAddWord, Consts.WORD_NEW);
                break;
            case R.id.btnWords:
                Intent intentList = new Intent(ctx, ListWordsActivity.class);
                startActivity(intentList);
                break;
            case R.id.btnImportExport:
                Intent intentIE = new Intent(ctx,IEActivity.class);
                startActivity(intentIE);
                break;


        }

    }

    /**  Закрытие базы переду уничтожением активити */
    protected void onDestroy() {
        super.onDestroy();
        // закрываем подключение при выходе
        db.close();
    }
}
