package com.example.gek.learnwords.data;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.gek.learnwords.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Адаптер для вывода в диалоговом окне списка загруженных или выгруженных слов
 */

public class NewWordsAdapter extends RecyclerView.Adapter<NewWordsAdapter.WordViewHolder>{
    private static final String TAG = "GEK";

    private List<SimpleWord> words = new ArrayList<>();

    public NewWordsAdapter(ArrayList<SimpleWord> words){
        this.words = words;
    }


    /** Реализация абстрактного класса ViewHolder, хранящего ссылки на виджеты.*/
    public class WordViewHolder extends RecyclerView.ViewHolder{
        private TextView tvEng;
        private TextView tvRus;

        public WordViewHolder(View itemView) {
            super(itemView);
            tvEng = (TextView) itemView.findViewById(R.id.tvEng);
            tvRus = (TextView) itemView.findViewById(R.id.tvRus);
        }
    }

    @Override
    public WordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_ie, parent, false);

        // тут можно программно менять атрибуты лэйаута (size, margins, paddings и др.)
        NewWordsAdapter.WordViewHolder wvh = new NewWordsAdapter.WordViewHolder(v);
        return wvh;
    }

    @Override
    public void onBindViewHolder(WordViewHolder holder, int position) {
        final SimpleWord simpleWord = words.get(position);
        holder.tvEng.setText(simpleWord.getEng());
        holder.tvRus.setText(simpleWord.getRus());
    }


    @Override
    public int getItemCount() {
        return words.size();
    }
}
