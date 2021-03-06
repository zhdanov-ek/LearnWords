package com.example.gek.learnwords.data;
        import android.content.ContentValues;
        import android.content.Context;
        import android.database.Cursor;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteOpenHelper;
        import android.util.Log;

        import java.util.ArrayList;
        import java.util.Random;

public class DB {

    private static final String TAG = "MY_LOG:";

    private static final String DB_NAME = "learnwords";
    private static final int DB_VERSION = 1;
    private static final String DB_TABLE = "dictionary";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ENG = "eng";
    public static final String COLUMN_RUS = "rus";
    public static final String COLUMN_TRUE = "true";
    public static final String COLUMN_FALSE = "false";
    // коэфициент показывающий знание этого слова. Вычисляется как: COLUMN_TRUE - COLUMN_FALSE
    public static final String COLUMN_LEVEL = "level";


    // SQL код для создания таблицы в БД. Используется один раз
    private static final String DB_CREATE =
            "create table " + DB_TABLE + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_ENG + " text, " +
                    COLUMN_RUS + " text, " +
                    COLUMN_TRUE + " integer, " +
                    COLUMN_FALSE + " integer, " +
                    COLUMN_LEVEL + " integer " +
                    ");";

    private final Context mCtx;


    // Объявляем вспомогательный класс для управления базой (подключение, создание, обновление и т.д.)
    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;

    public DB(Context ctx) {
        mCtx = ctx;
    }

    // открыть подключение
    public void open() {
        // создаем помощника для работы с БД где указываем инфу о нашей БД
        mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VERSION);
        // открываем нашу БД если она есть или создаем если ее нет. Ссылка на нее в mDB
        mDB = mDBHelper.getWritableDatabase();
    }

    // закрыть подключение
    public void close() {
        if (mDBHelper!=null) mDBHelper.close();
    }

    /** Ищем в базе английское слово и если находим, что-то возвращаем TRUE*/
    public boolean checkIsPresentWord(String eng){
        String selection = COLUMN_ENG + " LIKE ?";            // условие отбора
        String[] selectionArgs =  new String[] {eng};

        // Если что-то нашли то возвращаем TRUE, иначе - FALSE
        Cursor c = mDB.query(DB_TABLE, null, selection, selectionArgs, null, null, null);
        if  (c.moveToFirst())
            return true;
        else
            return false;
    }

    /** Получаем список всех слов или ищем по указанному значению */
    public Cursor getAllData(int listType, int orderByWant, String searchText) {
        String orderBy = null;              // сортировка
        String selection = null;            // условие отбора
        String[] selectionArgs = null;      // параметры испольуемые в отборе

        switch (listType) {
            case Consts.LIST_TYPE_ALL:
                if (orderByWant == Consts.ORDER_BY_ABC) {
                    orderBy = COLUMN_ENG + " ASC";
                } else {
                    orderBy = COLUMN_LEVEL;
                }
                break;

            // Ищем слово как в английском так и в русском и для этого вводим
            // параметр 2 раза
            case Consts.LIST_TYPE_SEARCH:
                selection = COLUMN_ENG + " LIKE ? OR " + COLUMN_RUS +" LIKE ?";
                selectionArgs = new String[] {"%" + searchText + "%", "%" + searchText + "%", };
                orderBy = COLUMN_ENG + " ASC";
        }

        // Выполняем запрос SQL и возвращаем данные
        return mDB.query(
                // имя таблицы
                DB_TABLE,
                // String[] columnNames — список имен возвращаемых полей (массив).
                // При передаче null возвращаются все столбцы;
                null,
                // String selection — параметр, формирующий выражение WHERE (исключая сам оператор WHERE).
                // Значение null возвращает все строки. Например: _id = 19 and summary = ?
                selection,
                // String[] selectionArgs — значения аргументов фильтра.
                // Вы можете включить ? в "selection"". Подставляется в запрос из заданного массива;
                selectionArgs,
                // String[] groupBy - фильтр для группировки, формирующий выражение GROUP BY
                // (исключая сам оператор GROUP BY). Если GROUP BY не нужен, передается null;
                null,
                // String[] having — фильтр для группировки, формирующий выражение HAVING
                null,
                // String[] orderBy — параметр, формирующий выражение ORDER BY
                // (исключая сам оператор ORDER BY). При сортировке по умолчанию передается null.
                orderBy);
    }


    /** Возвращает количество записей в словаре
     * todo заменить запрос на функцию SQL Count()*/
    public int getNumberWords(){
        return getAllData(Consts.LIST_TYPE_ALL, Consts.ORDER_BY_ABC, null).getCount();

    }


    /** Получить одну конкретную запись из таблицы DB_TABLE */
    public ContentValues getItem(int id){
        // стандартные переменные для query где задаются все ключи запроса
        String[] columns = null;
        String selection = null;
        String[] selectionArgs = null;
        String groupBy = null;
        String having = null;
        String orderBy = null;

        columns = new String[]{COLUMN_ID, COLUMN_ENG, COLUMN_RUS, COLUMN_TRUE, COLUMN_FALSE, COLUMN_LEVEL};
        selection = COLUMN_ID + " == " + id;

        Cursor c = mDB.query(DB_TABLE, columns, selection, null, null, null, null);
        ContentValues cv = new ContentValues();
        if (c != null) {
            // Обязательно ставим курсор в начало даже если в результате поиска одна запись
            c.moveToFirst();
            cv.put(Consts.ATT_ITEM_ID, c.getString(c.getColumnIndex(COLUMN_ID)));
            cv.put(Consts.ATT_ENG, c.getString(c.getColumnIndex(COLUMN_ENG)));
            cv.put(Consts.ATT_RUS, c.getString(c.getColumnIndex(COLUMN_RUS)));
            cv.put(Consts.ATT_TRUE, c.getString(c.getColumnIndex(COLUMN_TRUE)));
            cv.put(Consts.ATT_FALSE, c.getString(c.getColumnIndex(COLUMN_FALSE)));
            cv.put(Consts.ATT_LEVEL, c.getString(c.getColumnIndex(COLUMN_LEVEL)));
        }
        c.close();
        return cv;
    }

    /** Преобразование записи с ContentValues в MyWord формат */
    public MyWord convertCvInMyWord(ContentValues item){
        MyWord itemWord = new MyWord();
        itemWord.setId(item.getAsInteger(Consts.ATT_ITEM_ID));
        itemWord.setEng(item.getAsString(Consts.ATT_ENG));
        itemWord.setRus(item.getAsString(Consts.ATT_RUS));
        itemWord.setAnswerTrue(item.getAsInteger(Consts.ATT_TRUE));
        itemWord.setAnswerFalse(item.getAsInteger(Consts.ATT_FALSE));
        itemWord.setLevel(item.getAsInteger(Consts.ATT_LEVEL));
        return itemWord;
    }

    /**  Внести изменения в запись */
    public void changeRec(ContentValues cv, String id){
        // приводим слова к нижнему регистру
        if (cv.containsKey(COLUMN_ENG)){
            String englishLower = cv.getAsString(COLUMN_ENG).toLowerCase();
            String russianLower = cv.getAsString(COLUMN_RUS).toLowerCase();
            cv.put(COLUMN_ENG, englishLower );
            cv.put(COLUMN_RUS, russianLower);
        }

        mDB.update(DB_TABLE, cv, COLUMN_ID + " = ?", new String[]{id});
    }

    /** добавить запись в DB_TABLE */
    public void addRec(String eng, String rus) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ENG, eng.toLowerCase());
        cv.put(COLUMN_RUS, rus.toLowerCase());
        cv.put(COLUMN_TRUE, 0);
        cv.put(COLUMN_FALSE, 0);
        cv.put(COLUMN_LEVEL, 0);
        mDB.insert(DB_TABLE, null, cv);
    }

    /** добавить запись в DB_TABLE с ответами */
    public void addRec(String eng, String rus, int answerTrue, int answerFalse) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ENG, eng.toLowerCase());
        cv.put(COLUMN_RUS, rus.toLowerCase());
        cv.put(COLUMN_TRUE, answerTrue);
        cv.put(COLUMN_FALSE, answerFalse);
        cv.put(COLUMN_LEVEL, answerTrue - answerFalse);
        mDB.insert(DB_TABLE, null, cv);
    }

    /** Удалить запись из DB_TABLE */
    public void delRec(long id) {
        mDB.delete(DB_TABLE, COLUMN_ID + " = " + id, null);
    }

    /** Удалить все записи из DB_TABLE */
    public void delAllRec() {
        mDB.delete(DB_TABLE, null, null);
    }

    /** Формируем список ID элементов БД*/
    public ArrayList<Integer> getFullListID(Cursor cursor, boolean randomLogic){
        // сначала получаем весь список ID из курсора
        ArrayList<Integer> fullList = new ArrayList<>();
        cursor.moveToFirst();
        do {
            fullList.add(cursor.getInt(cursor.getColumnIndex(DB.COLUMN_ID)));
        } while (cursor.moveToNext());
        // В зависимости от второго параметра делаем рандомный список
        if (randomLogic) {
            return makeRandomList(fullList);
        } else {
            return fullList;
        }

    }

    /** Формируем ArrayList всех слов со всеми полями*/
    public ArrayList<MyWord> getFullListWords(Cursor cursor){
        ArrayList<MyWord> listMyWords = new ArrayList<>();
        cursor.moveToFirst();
        do {
            MyWord currentWord = new MyWord();
            currentWord.setId(cursor.getInt(cursor.getColumnIndex(DB.COLUMN_ID)));
            currentWord.setEng(cursor.getString(cursor.getColumnIndex(DB.COLUMN_ENG)));
            currentWord.setRus(cursor.getString(cursor.getColumnIndex(DB.COLUMN_RUS)));
            currentWord.setAnswerTrue(cursor.getInt(cursor.getColumnIndex(DB.COLUMN_TRUE)));
            currentWord.setAnswerFalse(cursor.getInt(cursor.getColumnIndex(DB.COLUMN_FALSE)));
            currentWord.setLevel(cursor.getInt(cursor.getColumnIndex(DB.COLUMN_LEVEL)));
            listMyWords.add(currentWord);
        } while (cursor.moveToNext());
        return listMyWords;
    }





    /** Перемещиваем список */
    public ArrayList<Integer> makeRandomList(ArrayList<Integer> wordsID){
        ArrayList<Integer> wordsIDRandom = new ArrayList<>();
        int size = wordsID.size();
        Random random = new Random();
        int r;
        for (int i = 0; i < size; i++) {
            r = random.nextInt(size - i);
            wordsIDRandom.add(wordsID.get(r));
            wordsID.remove(r);
        }
        return wordsIDRandom;
    }



    // Создадим наш класс по созданию и управлению БД на основе базового SQLiteOpenHelper
    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                        int version) {
            super(context, name, factory, version);
        }

        // Задаем метод создания БД если ее еще нет. Если БД существует то этот метод не вызовется
        @Override
        public void onCreate(SQLiteDatabase db) {
            // Выполняем SQL запрос по созданию таблицы со всеми полями
            db.execSQL(DB_CREATE);

            // Наполняем таблицу для наглядности
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_ENG, "program");
            cv.put(COLUMN_RUS, "программа");
            cv.put(COLUMN_TRUE, "0");
            cv.put(COLUMN_FALSE, "0");
            cv.put(COLUMN_LEVEL, "0");
            db.insert(DB_TABLE, null, cv);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }


}
