package com.example.gek.learnwords.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

/**
 * Класс содержит все глобальные константы
 */

// final запрещает создавать классы основанные на нашем
public final class Consts {

    // Режимы работы редактирования СЛОВА и варианты завершения
    public static final String WORD_MODE = "mode";
    public static final int WORD_MODE_NEW = 1;
    public static final int WORD_MODE_NEW_FROM_LIST = 2;
    public static final int WORD_MODE_EDIT = 3;


    public static final String WORD_RESULT_OPERATION = "word_result_operation";
    public static final int WORD_CANCEL = 0;
    public static final int WORD_ADD = 1;
    public static final int WORD_CHANGE = 2;
    public static final int WORD_REMOVE = 3;

    public static final String ITEM_POSITION = "position";

    // имена атрибутов для Map или Intent
    public static final String ATT_ITEM_ID = "_id";
    public static final String ATT_ENG = "eng";
    public static final String ATT_RUS = "rus";
    public static final String ATT_TRUE = "true";
    public static final String ATT_FALSE = "false";
    public static final String ATT_LEVEL = "level";

    // for JSON file
    public static final String ATT_WORDS = "words";

    // Варианты отображения основного списка
    public static final int LIST_TYPE_ALL = 0;
    public static final int LIST_TYPE_SEARCH = 1;
    public static final int ORDER_BY_ABC = 0;
    public static final int ORDER_BY_RATING = 1;


    public static String readLineFromFile(String nameFile){
        String line = "";
        try {
            File file = new File(nameFile);
            BufferedReader br = new BufferedReader(new FileReader(file));
            line = br.readLine();
            br.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return line;
    }

    public static int[] getThreeId(int exclusionId,  ArrayList<Integer> listId){
        Random r = new Random();
        int num1, num2, num3;
        do {
            num1 = listId.get(r.nextInt(listId.size()-1));
        } while (exclusionId == num1);

        do {
            num2 = listId.get(r.nextInt(listId.size()-1));
        } while ((exclusionId == num2) || (num1 == num2));

        do {
            num3 = listId.get(r.nextInt(listId.size()-1));
        } while ((exclusionId == num3) || (num1 == num3) || (num2 == num3));

        return new int[]{num1, num2, num3};

    }

}
