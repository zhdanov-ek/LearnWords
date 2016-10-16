package com.example.gek.learnwords;
        import android.content.ContentValues;
        import android.content.Context;
        import android.database.Cursor;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteOpenHelper;

class DB {

    private static final String DB_NAME = "learnwords";
    private static final int DB_VERSION = 1;
    private static final String DB_TABLE = "dictionary";

    static final String COLUMN_ID = "_id";
    static final String COLUMN_ENG = "eng";
    static final String COLUMN_RUS = "rus";
    static final String COLUMN_TRUE = "true";
    static final String COLUMN_FALSE = "false";


    private static final String DB_CREATE =
            "create table " + DB_TABLE + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_ENG + " text, " +
                    COLUMN_RUS + " text, " +
                    COLUMN_TRUE + " integer, " +
                    COLUMN_FALSE + " integer " +
                    ");";

    private final Context mCtx;


    // Объявляем вспомогательный класс для управления базой (подключение, создание, обновление и т.д.)
    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;

    public void setDir(String dir){
        String DIR = dir;
    }

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

    /** получить данные из таблицы DB_TABLE */
    public Cursor getAllData(int listType, String searchText) {
        String orderBy = null;              // сортировка
        String selection = null;            // условие отбора
        String[] selectionArgs = null;      // параметры испольуемые в отборе

        switch (listType) {
            case Consts.LIST_TYPE_ALL:
                orderBy = COLUMN_ENG + " ASC";
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

    /** Получить одну конкретную запись из таблицы DB_TABLE */
    public ContentValues getItem(int id){
        // стандартные переменные для query где задаются все ключи запроса
        String[] columns = null;
        String selection = null;
        String[] selectionArgs = null;
        String groupBy = null;
        String having = null;
        String orderBy = null;

        columns = new String[]{COLUMN_ID, COLUMN_ENG, COLUMN_RUS, COLUMN_TRUE, COLUMN_FALSE};
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
        }
        c.close();
        return cv;
    }


    /**  Внести изменения в запись */
    public void changeRec(ContentValues cv, String id){
        mDB.update(DB_TABLE, cv, COLUMN_ID + " = ?", new String[]{id});
    }

    /** добавить запись в DB_TABLE */
    public void addRec(String eng, String rus) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ENG, eng);
        cv.put(COLUMN_RUS, rus);
        cv.put(COLUMN_TRUE, 0);
        cv.put(COLUMN_FALSE, 0);
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
            db.insert(DB_TABLE, null, cv);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }


}
