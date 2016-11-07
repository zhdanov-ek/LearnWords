package com.example.gek.learnwords;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Адаптер для наполения списка всех слов
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private ArrayList<MyWord> listWords;
    private Context ctx;

    public RecyclerViewAdapter(Context ctx, ArrayList<MyWord> listWords){
        this.listWords = listWords;
        this.ctx = ctx;
    }


    /** Реализация абстрактного класса ViewHolder, хранящего ссылки на виджеты.
     / Он же реализует функцию OnClickListener, что бы не создавать их на каждое поле
     / при прокрутке в onBindViewHolder. Максимум таких холдеров будет на два больше
     / чем вмещается на экране */
    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tvListEng;
        private TextView tvListRus;

        public ViewHolder(View itemView) {
            super(itemView);
            tvListEng = (TextView) itemView.findViewById(R.id.tvListEng);
            tvListRus = (TextView) itemView.findViewById(R.id.tvListRus);
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
    }

    @Override
    public int getItemCount() {
        return listWords.size();
    }


}
