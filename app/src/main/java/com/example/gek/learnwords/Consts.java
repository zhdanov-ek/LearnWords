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

    // Глобальные константы
    public static final String URL_IMAGES = "http://vzpharm.com.ua/admin/pb/";
    public static final String URL_DB_XML = "http://vzpharm.com.ua/admin/pb/vzpharm.phb";
    public static final String URL_DB_VER_XML = "http://vzpharm.com.ua/admin/pb/vzpharmver.phb";
    public static final String DB_FILE = "vzpharm.phb";
    public static final String DB_CURRENT_VER_FILE = "vzpharmvercurrent.phb";
    public static final String DB_NEW_VER_FILE = "vzpharmvernew.phb";
    public static String FOLDER = "";

    // Режимы работы редактирования СЛОВА
    public static final String WORD_MODE = "mode";
    public static final int WORD_NEW = 0;
    public static final int WORD_EDIT = 1;

    // имена атрибутов для Map или Intent
    public static final String ATT_ITEM_ID = "_id";
    public static final String ATT_ENG = "eng";
    public static final String ATT_RUS = "rus";
    public static final String ATT_TRUE = "true";
    public static final String ATT_FALSE = "false";

    // Варианты отображения основного списка
    public static final int LIST_TYPE_ALL = 0;
    public static final int LIST_TYPE_SEARCH = 1;



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
