package com.example.gek.learnwords;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnLearn, btnWords, btnAddWord, btnImportExport;
    public DB db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLearn = (Button)findViewById(R.id.btnLearn);
        btnLearn.setOnClickListener(this);

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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnAddWord:
                Intent intentAddWord = new Intent(getBaseContext(), WordActivity.class);
                intentAddWord.putExtra(Consts.WORD_MODE, Consts.WORD_NEW);
                startActivityForResult(intentAddWord, Consts.WORD_NEW);
                break;
            case R.id.btnWords:
                Intent intentList = new Intent(getBaseContext(), ListWordsActivity.class);
                startActivity(intentList);
                break;

        }

    }

    /**  Обязательные абстрактыне методы для актвити с буфером*/
    protected void onDestroy() {
        super.onDestroy();
        // закрываем подключение при выходе
        db.close();
    }
}
