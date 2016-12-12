package com.example.gek.learnwords.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.gek.learnwords.R;
import com.example.gek.learnwords.data.Consts;
import com.example.gek.learnwords.data.DB;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_number_words;
    Button btnLearn, btnTest, btnWords, btnAddWord, btnImportExport, btnSettings;
    private DB db;
    Context ctx;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ctx = getApplicationContext();
        tv_number_words = (TextView)findViewById(R.id.tv_number_words);

        btnLearn = (Button)findViewById(R.id.btnInput);
        btnLearn.setOnClickListener(this);


        btnTest = (Button)findViewById(R.id.btnVariant);
        btnTest.setOnClickListener(this);

        btnWords = (Button)findViewById(R.id.btnWords);
        btnWords.setOnClickListener(this);

        btnAddWord = (Button)findViewById(R.id.btnAddWord);
        btnAddWord.setOnClickListener(this);

        btnImportExport = (Button)findViewById(R.id.btnImportExport);
        btnImportExport.setOnClickListener(this);

        btnSettings = (Button)findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(this);

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
        if (num < 5) {
            btnTest.setEnabled(false);
        }
        else {
            btnTest.setEnabled(true);
        }
        tv_number_words.setText("In dictionary " + num + " words" );



    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnInput:
                Intent intentLearn = new Intent(ctx, InputActivity.class);
                startActivity(intentLearn);
                break;
            case R.id.btnVariant:
                Intent intentTest = new Intent(ctx, VariantActivity.class);
                startActivity(intentTest);
                break;
            case R.id.btnAddWord:
                Intent intentAddWord = new Intent(ctx, WordActivity.class);
                intentAddWord.putExtra(Consts.WORD_MODE, Consts.WORD_MODE_NEW);
                startActivityForResult(intentAddWord, Consts.WORD_MODE_NEW);
                break;
            case R.id.btnWords:
                Intent intentList = new Intent(ctx, ListWordsActivity.class);
                startActivity(intentList);
                break;
            case R.id.btnImportExport:
                Intent intentIE = new Intent(ctx,IEActivity.class);
                startActivity(intentIE);
                break;
            case R.id.btnSettings:
                Intent intentSet = new Intent(ctx,SettingsActivity.class);
                startActivity(intentSet);
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
