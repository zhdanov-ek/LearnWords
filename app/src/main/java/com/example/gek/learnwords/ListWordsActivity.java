//todo переработать красиво список и добавить возможность удаления слова

package com.example.gek.learnwords;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;


public class ListWordsActivity extends AppCompatActivity {
    DB db;
    RecyclerViewAdapter adapter;
    ArrayList<MyWord> listWords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_words);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        // Задаем стандартный менеджер макетов
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Получаем из БД все слова и загружаем в список
        db = new DB(this);
        db.open();

        listWords = db.getFullListWords(db.getAllData(Consts.LIST_TYPE_ALL, null));
        // Создаем адаптер и подаем ему на вход активити для запуска startActivityForResult и список
        adapter = new RecyclerViewAdapter(this, listWords);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Consts.WORD_EDIT) {
            if (resultCode == RESULT_OK) {
                int pos = data.getIntExtra(Consts.ITEM_POSITION, 0);
                int id = data.getIntExtra(Consts.ATT_ITEM_ID, 0);
                MyWord changedWord = db.convertCvInMyWord(db.getItem(id));
                listWords.set(pos, changedWord);
                adapter.notifyItemChanged(pos);
            }
        }


    }


    /**
     * Закрытие базы перед уничтожением активити
     */
    protected void onDestroy() {
        super.onDestroy();
        // закрываем подключение при выходе
        db.close();
    }


}

