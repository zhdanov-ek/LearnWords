package com.example.gek.learnwords.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.gek.learnwords.R;
import com.example.gek.learnwords.data.RecyclerViewAdapter;
import com.example.gek.learnwords.data.Consts;
import com.example.gek.learnwords.data.DB;
import com.example.gek.learnwords.data.MyWord;

import java.util.ArrayList;


public class ListWordsActivity extends AppCompatActivity {
    DB db;
    Context mCtx;
    RecyclerViewAdapter mAdapter;
    RecyclerView mRrecyclerView;
    ArrayList<MyWord> mListWords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_words);

        mCtx = this;

        // Добавляем тулбар бар
        Toolbar myToolbar = (Toolbar) findViewById(R.id.tbListWord);
        setSupportActionBar(myToolbar);

        mRrecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        // Задаем стандартный менеджер макетов
        mRrecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Получаем из БД все слова и загружаем в список
        db = new DB(this);
        db.open();
        //todo Добавить порядок сортировки, который будет изыматся из настроек программы
        mListWords = db.getFullListWords(
                db.getAllData(Consts.LIST_TYPE_ALL, Consts.ORDER_BY_ABC, null));

        // Создаем адаптер и подаем ему на вход активити для запуска startActivityForResult и список
        mAdapter = new RecyclerViewAdapter(this, mListWords);
        mRrecyclerView.setAdapter(mAdapter);
    }


    // Указываем как нам формировать меню и описываем виджет SearchView
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_word_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.ab_search);

        SearchView searchView =(SearchView) MenuItemCompat.getActionView(searchItem);

        // Отрабатываем смену текста в окне поиска
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // Реакция на команду ввода (Enter)
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            // Непосредственно событие смены содержимого. Делаем запрос к БД по каждому изменению
            // в окне поиска
            @Override
            public boolean onQueryTextChange(String newText) {
                // Делаем выборку из БД после чего проверяем есть ли результат. Если нет то
                // делаем выборку всех слов
                Cursor cursor = db.getAllData(Consts.LIST_TYPE_SEARCH, Consts.ORDER_BY_ABC, newText);
                if ((cursor == null) || (cursor.getCount() == 0)) {
                    cursor = db.getAllData(Consts.LIST_TYPE_ALL, Consts.ORDER_BY_ABC, null);
                }
                mListWords = db.getFullListWords(cursor);
                mAdapter = new RecyclerViewAdapter((Activity)mCtx, mListWords);
                mRrecyclerView.setAdapter(mAdapter);
                return false;
            }
        });


        // По окончанию работы с SearchView отображаем все слова в алфавитном порядке
        // и в меню это отмечаем это в меню
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                MenuItem menuItem = menu.findItem(R.id.ab_sort_abc);
                menuItem.setChecked(true);
                return false;
            }
        });
        return true;
    }

    // Реакция на нажатие кнопок в меню
    // Формируем список слов в нужном порядке (согласно клику) и пересоздаем адаптер
    // который и подаем снова на RecyclerView для обновления списка
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.ab_sort_abc:
                if (!item.isChecked()) {
                    mListWords = db.getFullListWords(
                            db.getAllData(Consts.LIST_TYPE_ALL, Consts.ORDER_BY_ABC, null));
                    mAdapter = new RecyclerViewAdapter(this, mListWords);
                    mRrecyclerView.setAdapter(mAdapter);
                    item.setChecked(true);
                }
                break;

            case R.id.ab_sort_rating:
                if (!item.isChecked()){
                    mListWords = db.getFullListWords(
                            db.getAllData(Consts.LIST_TYPE_ALL, Consts.ORDER_BY_RATING, null));
                    mAdapter = new RecyclerViewAdapter(this, mListWords);
                    mRrecyclerView.setAdapter(mAdapter);
                    item.setChecked(true);
                }
                break;
            case R.id.ab_new_word:
                Intent intentAddWord = new Intent(mCtx, WordActivity.class);
                intentAddWord.putExtra(Consts.WORD_MODE, Consts.WORD_MODE_NEW_FROM_LIST);
                startActivityForResult(intentAddWord, Consts.WORD_MODE_NEW_FROM_LIST);
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
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
        if ((requestCode == Consts.WORD_MODE_EDIT)|| (requestCode == Consts.WORD_MODE_NEW_FROM_LIST)
                && resultCode == RESULT_OK) {
            int result = data.getIntExtra(Consts.WORD_RESULT_OPERATION, 0);
            switch (result){
                //todo добавить плавающую кнопку вместо кнопки в меню для добавления слова прямо в списке
                case Consts.WORD_ADD:
                    mListWords = db.getFullListWords(
                            db.getAllData(Consts.LIST_TYPE_ALL, Consts.ORDER_BY_ABC, null));
                    mAdapter = new RecyclerViewAdapter(this, mListWords);
                    mRrecyclerView.setAdapter(mAdapter);
                    break;

                case Consts.WORD_CHANGE:
                case Consts.WORD_REMOVE:
                    int pos = data.getIntExtra(Consts.ITEM_POSITION, 0);
                    int id = data.getIntExtra(Consts.ATT_ITEM_ID, 0);
                    if (result == Consts.WORD_REMOVE) {
                        mListWords.remove(pos);
                        mAdapter.notifyItemRemoved(pos);
                    } else {
                        MyWord changedWord = db.convertCvInMyWord(db.getItem(id));
                        mListWords.set(pos, changedWord);
                        mAdapter.notifyItemChanged(pos);
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

