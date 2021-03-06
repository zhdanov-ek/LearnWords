package com.example.gek.learnwords.data;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.gek.learnwords.R;
import com.example.gek.learnwords.activity.WordActivity;

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
        private RatingBar rbLevel;

        public ViewHolder(View itemView) {
            super(itemView);
            llItem = (LinearLayout)itemView.findViewById(R.id.llItem);
            llItem.setOnClickListener(this);
            tvListEng = (TextView) itemView.findViewById(R.id.tvListEng);
            tvListRus = (TextView) itemView.findViewById(R.id.tvListRus);
            rbLevel = (RatingBar) itemView.findViewById(R.id.rbLevel);
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
        // меняем цвет звездочкам
        RatingBar ratingBar = (RatingBar) v.findViewById(R.id.rbLevel);
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(
                ContextCompat.getColor(v.getContext(), R.color.colorLogoBook),
                PorterDuff.Mode.SRC_ATOP);

        ViewHolder vh = new ViewHolder(v);



        return vh;
    }


    // Заполнение данными с позицией position наших вью элементов
    @Override
    public void onBindViewHolder(RecyclerViewAdapter.ViewHolder holder, int position) {
        final MyWord myWord = listWords.get(position);
        holder.tvListEng.setText(myWord.getEng());
        holder.tvListRus.setText(myWord.getRus());
        int rating = myWord.getLevel();
        if (rating < 0) {
            rating = 0;
        }
        holder.rbLevel.setRating(rating);
    }

    @Override
    public int getItemCount() {
        return listWords.size();
    }


}
