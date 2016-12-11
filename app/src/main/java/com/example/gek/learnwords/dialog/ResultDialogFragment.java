package com.example.gek.learnwords.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.gek.learnwords.R;

/**
 * Кастом дилог, который выводит информацию о загрузке или выгрузе слов
 */

public class ResultDialogFragment extends DialogFragment {

    private TextView tvResult;
    private String result;


    /**
     * Создаем экземпляр диалога с передачея значения result
     */
    public static ResultDialogFragment newInstance(String result) {
        ResultDialogFragment rdf = new ResultDialogFragment();
        Bundle args = new Bundle();
        args.putString("result", result);
        rdf.setArguments(args);
        return rdf;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.result = getArguments().getString("result");

        // Создаем вью по нашему лаяуту
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View root = inflater.inflate(R.layout.dialog_result, null);


        // Строим диалог на добавляя кнопку и размещая текст в окне
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(root)
                .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });

        tvResult = (TextView) root.findViewById(R.id.tvResult);
        tvResult.setText(result);
        return builder.create();
    }
}
