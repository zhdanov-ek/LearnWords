package com.example.gek.learnwords;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class WordActivity extends AppCompatActivity {
    private Context context;
    private DB db;
    private Button btnOk, btnCancel;
    private TextView tvMode;
    private EditText etEng, etRus;
    private int mode;
    private int id;                          //id редактируемого элемента
    private int itemPositionRecyclerView;    // позиция в списке

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word);
        context = getApplicationContext();

        tvMode = (TextView)findViewById(R.id.tv_caption_word);
        btnOk = (Button) findViewById(R.id.btnOkWord);
        btnCancel = (Button) findViewById(R.id.btnCancelWord);
        etEng = (EditText) findViewById(R.id.etEng);
        etRus = (EditText) findViewById(R.id.etRus);

        // Смотрим наш интент и заносим все данные в Bundle
        Bundle bundle = getIntent().getExtras();

        // Смотрим как вызывали активити по параметру WORD_MODE
        // либо работает с новым словом либо редактируем существующее
        mode = bundle.getInt(Consts.WORD_MODE,0);
        switch (mode) {
            case Consts.WORD_NEW:
                tvMode.setText(R.string.caption_new_word);
                mode = Consts.WORD_NEW;
                break;

            case Consts.WORD_EDIT:
                etEng.setText(bundle.getString(Consts.ATT_ENG));
                etRus.setText(bundle.getString(Consts.ATT_RUS));
                mode = Consts.WORD_EDIT;
                id = bundle.getInt(Consts.ATT_ITEM_ID,0);
                itemPositionRecyclerView = bundle.getInt(Consts.ITEM_POSITION);
                tvMode.setText(getString(R.string.caption_edit_word) + " Item ID = " + id);
                break;
        }

        /** Просто закрываем активити ничего не добавляя и не меняя */
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        /** Записываем данные о записи в БД после редактирования или создания новой */
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String eng = etEng.getText().toString();
                String rus = etRus.getText().toString();

                Intent intentResult = new Intent();
                // Если есть незаполненные поля то выходим
                if ((etRus.getText().toString().length()==0) ||
                        (etEng.getText().toString().length()==0)) {
                    Toast.makeText(context, "Empty fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Поля заполненны - записываем данные
                    db = new DB(context);
                    db.open();

                    switch (mode){
                        case Consts.WORD_NEW:
                            db.addRec(eng, rus);
                            intentResult.putExtra(Consts.WORD_MODE, Consts.WORD_NEW);
                            break;
                        case Consts.WORD_EDIT:
                            ContentValues cv = new ContentValues();
                            cv.put(Consts.ATT_ENG, eng);
                            cv.put(Consts.ATT_RUS, rus);
                            db.changeRec(cv, Integer.toString(id));
                            intentResult.putExtra(Consts.WORD_MODE, Consts.WORD_EDIT);
                            intentResult.putExtra(Consts.ATT_ITEM_ID, id);
                            // передаем назад ID position элемента, который поменяли
                            intentResult.putExtra(Consts.ITEM_POSITION, itemPositionRecyclerView);
                            break;
                    }
                    intentResult.putExtra(Consts.ATT_ENG, eng);
                    intentResult.putExtra(Consts.ATT_RUS, rus);
                    setResult(RESULT_OK, intentResult);
                    db.close();
                    finish();
                }
            }
        });
    }

}
