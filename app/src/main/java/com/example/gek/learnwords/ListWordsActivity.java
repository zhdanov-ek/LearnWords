//todo переработать красиво список и добавить возможность удаления слова

package com.example.gek.learnwords;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;



public class ListWordsActivity extends AppCompatActivity {
    SimpleCursorAdapter scAdapter;
    private Cursor cursor;
    DB db;
    private ListView lvSimple;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_words);

        lvSimple = (ListView) findViewById(R.id.lvSimple);

        db = new DB(this);
        db.open();
        loadListView();
    }



    /** Обновление данными в ListView*/
    private void loadListView(){
        cursor = db.getAllData(Consts.LIST_TYPE_ALL, null);
        // массив имен атрибутов, из которых будут читаться данные
        String[] from = {DB.COLUMN_ENG, DB.COLUMN_RUS, DB.COLUMN_TRUE,
                DB.COLUMN_FALSE, DB.COLUMN_LEVEL};
        // массив ID View-компонентов, в которые будут вставлять данные
        int[] to = {R.id.tvListEng, R.id.tvListRus, R.id.tvListAnswerTrue,
                R.id.tvListAnswerFalse, R.id.tvListAnswerLevel};
        // создаем адаптер
        scAdapter = new SimpleCursorAdapter(this, R.layout.list_view_item, cursor, from, to);
        // определяем список и присваиваем ему адаптер
        lvSimple.setAdapter(scAdapter);
    }

    /**  Закрытие базы перед уничтожением активити */
    protected void onDestroy() {
        super.onDestroy();
        // закрываем подключение при выходе
        db.close();
    }
}

