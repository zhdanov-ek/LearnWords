//todo переработать красиво список

package com.example.gek.learnwords.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.gek.learnwords.R;
import com.example.gek.learnwords.RecyclerViewAdapter;
import com.example.gek.learnwords.data.Consts;
import com.example.gek.learnwords.data.DB;
import com.example.gek.learnwords.data.MyWord;

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


    // Смотрим наш ли ответ и анализируем WORD_RESULT_OPERATION, который содержит значение
    // состояния что именно сделали с записью: изменили, удалили, отменили (просто посмотрев)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Нажата кнопка CANCEL или аппаратная кнопка BACK (нет интента ответного)
        if (data == null) {
            return;
        }
        if ((requestCode == Consts.WORD_MODE_EDIT) && resultCode == RESULT_OK) {
            int result = data.getIntExtra(Consts.WORD_RESULT_OPERATION, 0);
            switch (result){
                //todo добавить плавающую кнопку материал для добавления слова прямо в списке
                case Consts.WORD_ADD:
                    break;

                case Consts.WORD_CHANGE:
                case Consts.WORD_REMOVE:
                    int pos = data.getIntExtra(Consts.ITEM_POSITION, 0);
                    int id = data.getIntExtra(Consts.ATT_ITEM_ID, 0);
                    if (result == Consts.WORD_REMOVE) {
                        listWords.remove(pos);
                        adapter.notifyItemRemoved(pos);
                    } else {
                        MyWord changedWord = db.convertCvInMyWord(db.getItem(id));
                        listWords.set(pos, changedWord);
                        adapter.notifyItemChanged(pos);
                    }
                    break;
                default:
                    break;
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

