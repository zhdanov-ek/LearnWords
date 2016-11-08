package com.example.gek.learnwords;

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

    // Режимы работы редактирования СЛОВА
    static final String WORD_MODE = "mode";
    static final int WORD_NEW = 0;
    static final int WORD_EDIT = 1;
    static final String ITEM_POSITION = "position";

    // имена атрибутов для Map или Intent
    static final String ATT_ITEM_ID = "_id";
    static final String ATT_ENG = "eng";
    static final String ATT_RUS = "rus";
    static final String ATT_TRUE = "true";
    static final String ATT_FALSE = "false";
    static final String ATT_LEVEL = "level";

    // for JSON file
    static final String ATT_WORDS = "words";

    // Варианты отображения основного списка
    static final int LIST_TYPE_ALL = 0;
    static final int LIST_TYPE_SEARCH = 1;


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
