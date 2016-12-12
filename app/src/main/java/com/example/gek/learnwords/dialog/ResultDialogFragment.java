package com.example.gek.learnwords.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.gek.learnwords.R;
import com.example.gek.learnwords.data.NewWordsAdapter;
import com.example.gek.learnwords.data.SimpleWord;

import java.util.ArrayList;
import java.util.List;

/**
 * Кастом дилог, который выводит информацию о загрузке или выгрузе слов
 */

public class ResultDialogFragment extends DialogFragment {

    private String result;
    private ArrayList<SimpleWord> words;
    private RecyclerView recyclerView;
    private NewWordsAdapter newWordsAdapter;

    private static final String TAG = "GEK";

    /**
     * Создаем экземпляр диалога с передачей значений
     */
    public static ResultDialogFragment newInstance(String result,
                                                   ArrayList<SimpleWord> words) {
        ResultDialogFragment rdf = new ResultDialogFragment();
        Bundle args = new Bundle();
        args.putString("result", result);
        args.putParcelableArrayList("words", words);
        rdf.setArguments(args);
        return rdf;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.result = getArguments().getString("result");
        this.words = getArguments().getParcelableArrayList("words");


        // Создаем вью по нашему лаяуту
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View root = inflater.inflate(R.layout.dialog_result, null);

        recyclerView = (RecyclerView)root.findViewById(R.id.recyclerView);

        // Задаем стандартный менеджер макетов
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        newWordsAdapter = new NewWordsAdapter(words);
        recyclerView.setAdapter(newWordsAdapter);

        // Строим диалог на добавляя кнопку и размещая текст в окне
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(root)
                .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });
        return builder.create();
    }
}
