package com.example.gek.learnwords;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.gek.learnwords.activity.WordActivity;
import com.example.gek.learnwords.data.Consts;
import com.example.gek.learnwords.data.MyWord;

import java.util.ArrayList;

/**
 * Адаптер для наполения списка всех слов
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private ArrayList<MyWord> listWords;
    private Context ctx;
    private Activity activity;


    public RecyclerViewAdapter(Activity activity, ArrayList<MyWord> listWords){
        this.listWords = listWords;
        this.ctx = activity;
        this.activity = activity;
    }


    /** Реализация абстрактного класса ViewHolder, хранящего ссылки на виджеты.
     / Он же реализует функцию OnClickListener, что бы не создавать их на каждое поле
     / при прокрутке в onBindViewHolder. Максимум таких холдеров будет на два больше
     / чем вмещается на экране */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private LinearLayout llItem;
        private TextView tvListEng;
        private TextView tvListRus;
        private TextView tvListAnswerTrue;
        private TextView tvListAnswerFalse;
        private TextView tvListAnswerLevel;

        public ViewHolder(View itemView) {
            super(itemView);
            llItem = (LinearLayout)itemView.findViewById(R.id.llItem);
            llItem.setOnClickListener(this);
            tvListEng = (TextView) itemView.findViewById(R.id.tvListEng);
            tvListRus = (TextView) itemView.findViewById(R.id.tvListRus);
            tvListAnswerTrue = (TextView) itemView.findViewById(R.id.tvListAnswerTrue);
            tvListAnswerFalse = (TextView) itemView.findViewById(R.id.tvListAnswerFalse);
            tvListAnswerLevel = (TextView) itemView.findViewById(R.id.tvListAnswerLevel);
        }

        // По клику на айтеме открываем окно с его редактированием.
        @Override
        public void onClick(View view) {
            MyWord editWord = listWords.get(getAdapterPosition());
            Intent intentEditWord = new Intent(ctx, WordActivity.class);
            intentEditWord.putExtra(Consts.WORD_MODE, Consts.WORD_MODE_EDIT);
            intentEditWord.putExtra(Consts.ATT_ENG, editWord.getEng());
            intentEditWord.putExtra(Consts.ATT_RUS, editWord.getRus());
            intentEditWord.putExtra(Consts.ATT_ITEM_ID, editWord.getId());

            // запоминаем номер позиции айтема
            intentEditWord.putExtra(Consts.ITEM_POSITION, getAdapterPosition());
            activity.startActivityForResult(intentEditWord, Consts.WORD_MODE_EDIT);
        }
    }


    // Создает новые views (элементы списка) (вызывается layout manager-ом)
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_item, parent, false);

        // тут можно программно менять атрибуты лэйаута (size, margins, paddings и др.)
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }


    // Заполнение данными с позицией position наших вью элементов
    @Override
    public void onBindViewHolder(RecyclerViewAdapter.ViewHolder holder, int position) {
        final MyWord myWord = listWords.get(position);
        holder.tvListEng.setText(myWord.getEng());
        holder.tvListRus.setText(myWord.getRus());
        holder.tvListAnswerTrue.setText(Integer.toString(myWord.getAnswerTrue()));
        holder.tvListAnswerFalse.setText(Integer.toString(myWord.getAnswerFalse()));
        holder.tvListAnswerLevel.setText(Integer.toString(myWord.getLevel()));
    }

    @Override
    public int getItemCount() {
        return listWords.size();
    }


}
