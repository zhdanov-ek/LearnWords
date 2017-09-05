package com.example.gek.learnwords.activity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.gek.learnwords.R;
import com.example.gek.learnwords.data.Consts;
import com.example.gek.learnwords.data.DB;
import com.example.gek.learnwords.data.MyWord;

import java.util.List;

public class WordActivity extends AppCompatActivity {
    private Context context;
    private DB db;
    private Button btnOk, btnCancel, btnRemove;
    private EditText etEng, etRus;
    private int mode;
    private int id;                          //mId редактируемого элемента
    private int itemPositionRecyclerView;    // позиция в списке

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word);
        context = getApplicationContext();

        // Добавляем наш тулбар. Для того, что бы нормально изменить тайтл нужно подать пустое значение
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolBar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);


        btnOk = (Button) findViewById(R.id.btnOkWord);
        btnCancel = (Button) findViewById(R.id.btnCancelWord);
        btnRemove = (Button) findViewById(R.id.btnRemoveWord);
        etEng = (EditText) findViewById(R.id.etEng);
        etRus = (EditText) findViewById(R.id.etRus);

        // Смотрим наш интент и заносим все данные в Bundle
        Bundle bundle = getIntent().getExtras();

        // Смотрим как вызывали активити по параметру WORD_MODE
        // либо работает с новым словом либо редактируем существующее
        mode = bundle.getInt(Consts.WORD_MODE,0);
        switch (mode) {
            case Consts.WORD_MODE_NEW:
                myToolbar.setTitle(R.string.caption_new_word);
                mode = Consts.WORD_MODE_NEW;
                btnRemove.setVisibility(View.GONE);
                break;

            case Consts.WORD_MODE_EDIT:
                etEng.setText(bundle.getString(Consts.ATT_ENG));
                etRus.setText(bundle.getString(Consts.ATT_RUS));
                mode = Consts.WORD_MODE_EDIT;
                id = bundle.getInt(Consts.ATT_ITEM_ID,0);
                itemPositionRecyclerView = bundle.getInt(Consts.ITEM_POSITION);
                myToolbar.setTitle(R.string.caption_edit_word);
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
                saveWord();
            }
        });

        /** Удаляем слово в БД  */
        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeWord();
            }
        });
    }


    private void saveWord(){
        String eng = etEng.getText().toString();
        String rus = etRus.getText().toString();
        boolean canClose = true;

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
                case Consts.WORD_MODE_NEW:
                    List<MyWord> searchResult = findWord(eng);
                    if (searchResult == null || searchResult.size() == 0){
                        db.addRec(eng, rus);
                        // результат в окно со списком всех слов словаря
                        intentResult.putExtra(Consts.WORD_MODE, Consts.WORD_MODE_NEW);
                        intentResult.putExtra(Consts.WORD_RESULT_OPERATION, Consts.WORD_ADD);
                    } else {
                        Toast.makeText(getApplicationContext(), "The word already is in dictionary", Toast.LENGTH_SHORT).show();
                        canClose = false;
                    }
                    break;

                case Consts.WORD_MODE_EDIT:
                    ContentValues cv = new ContentValues();
                    cv.put(Consts.ATT_ENG, eng);
                    cv.put(Consts.ATT_RUS, rus);
                    db.changeRec(cv, Integer.toString(id));
                    // Результат в окно со списком слов
                    intentResult.putExtra(Consts.WORD_RESULT_OPERATION, Consts.WORD_CHANGE);
                    intentResult.putExtra(Consts.ATT_ITEM_ID, id);
                    // передаем назад ID position элемента, который поменяли
                    intentResult.putExtra(Consts.ITEM_POSITION, itemPositionRecyclerView);
                    break;
            }
            db.close();
            if (canClose){
                intentResult.putExtra(Consts.ATT_ENG, eng);
                intentResult.putExtra(Consts.ATT_RUS, rus);
                setResult(RESULT_OK, intentResult);
                finish();
            }

        }
    }

    private void removeWord(){
        db = new DB(context);
        db.open();
        db.delRec(id);
        Intent intentResult = new Intent();
        intentResult.putExtra(Consts.WORD_MODE, Consts.WORD_MODE_EDIT);
        intentResult.putExtra(Consts.WORD_RESULT_OPERATION, Consts.WORD_REMOVE);

        // передаем назад ID position элемента, который был удален
        intentResult.putExtra(Consts.ITEM_POSITION, itemPositionRecyclerView);
        setResult(RESULT_OK, intentResult);
        db.close();
        finish();
    }

    private List<MyWord> findWord(String newWord){
        return db.getFullListWords(db.getAllData(Consts.LIST_TYPE_SEARCH, Consts.ORDER_BY_ABC, newWord));
    }

}
