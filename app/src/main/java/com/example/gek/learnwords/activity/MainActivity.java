package com.example.gek.learnwords.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.gek.learnwords.R;
import com.example.gek.learnwords.data.Consts;
import com.example.gek.learnwords.data.DB;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_number_words;
    private Button btnTest;
    private DB db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Добавляем тулбар бар
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolBar);
        myToolbar.setLogo(R.mipmap.icon);
        setSupportActionBar(myToolbar);

        tv_number_words = (TextView)findViewById(R.id.tv_number_words);

        findViewById(R.id.btnInput).setOnClickListener(this);
        findViewById(R.id.btnWords).setOnClickListener(this);

        btnTest = (Button)findViewById(R.id.btnVariant);
        btnTest.setOnClickListener(this);

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
                Intent intentLearn = new Intent(this, InputActivity.class);
                startActivity(intentLearn);
                break;
            case R.id.btnVariant:
                Intent intentTest = new Intent(this, VariantActivity.class);
                startActivity(intentTest);
                break;
            case R.id.btnWords:
                Intent intentList = new Intent(this, ListWordsActivity.class);
                startActivity(intentList);
                break;
        }
    }

    // Указываем как нам формировать меню
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // Реакция на нажатие кнопок в меню
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.ab_settings:
                if (!item.isChecked()) {
                    Intent intentSet = new Intent(this, SettingsActivity.class);
                    startActivity(intentSet);
                }
                break;
            case R.id.ab_ie:
                Intent intentIE = new Intent(this,IEActivity.class);
                startActivity(intentIE);
                break;
            case R.id.ab_add:
                Intent intentAddWord = new Intent(this, WordActivity.class);
                intentAddWord.putExtra(Consts.WORD_MODE, Consts.WORD_MODE_NEW);
                startActivityForResult(intentAddWord, Consts.WORD_MODE_NEW);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**  Закрытие базы переду уничтожением активити */
    protected void onDestroy() {
        super.onDestroy();
        // закрываем подключение при выходе
        db.close();
    }
}
