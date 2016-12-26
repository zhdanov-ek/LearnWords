/**
 * Тестирование знаний: вопрос и 4 кнопки с вариантами ответов
 * Слова подаются на экран в порядке основанном на кол-ве правильных и не правильных ответов.
 * Первыми появляются слова с наибольшим кол-вом не правильных ответов
 * Каждый ответ фиксируется в БД
 */

package com.example.gek.learnwords.activity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gek.learnwords.R;
import com.example.gek.learnwords.data.Consts;
import com.example.gek.learnwords.data.DB;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


public class VariantActivity extends AppCompatActivity
        implements View.OnClickListener, MediaPlayer.OnPreparedListener {
    private Button btn_next, btn_answer1, btn_answer2, btn_answer3, btn_answer4;
    private TextView tv_word;
    private ImageView iv_correctly;
    private DB mDb;

    private String mWordOriginal, mWordTranslate;                // значения текущего слова
    int mId, mCounterTrue, mCounterFalse;

    private Animation mAnim;
    private int mPrefDelay;                  // задержка до показа следующего слова
    private String mPrefDirection;           // направление перевода
    private Boolean mSound;
    private Boolean mVibration;

    private String mColumnWordOriginal;      // Значение поля (rus/eng) с которого переводим текущее слово
    private int mTotalTrueAnswers, mTotalFalseAnswers;

    private Handler handler;
    private Boolean mHasRunCallback;         // состояние есть ли колбек

    private int mCurrentID = 0;              // текущий порядковый номер ID слова в списке всех ID
    ArrayList<Integer> wordsIDList;          // хранит рандомный список ID еще не протестированных слов
    private int[] threeFalseAnswerId;        // массив ложных альтернативных ID
    private Context ctx;
    private MediaPlayer mMediaPlayer;


    private  static final String TAG = "VariantActivity - ";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_variant);

        ctx = this;

        // с этого момента регулировка звука относится к указанному звуковому потоку
        setVolumeControlStream(AudioManager.STREAM_MUSIC);


        // Добавляем тулбар бар
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(myToolbar);
        myToolbar.setTitle(R.string.caption_input);

        mDb = new DB(this);
        mDb.open();

        tv_word = (TextView)findViewById(R.id.tv_word);
        iv_correctly = (ImageView) findViewById(R.id.iv_correctly);
        mAnim = AnimationUtils.loadAnimation(this, R.anim.alpha);

        btn_answer1 = (Button)findViewById(R.id.btn_answer1);
        btn_answer1.setOnClickListener(this);

        btn_answer2 = (Button)findViewById(R.id.btn_answer2);
        btn_answer2.setOnClickListener(this);

        btn_answer3 = (Button)findViewById(R.id.btn_answer3);
        btn_answer3.setOnClickListener(this);

        btn_answer4 = (Button)findViewById(R.id.btn_answer4);
        btn_answer4.setOnClickListener(this);

        btn_next = (Button)findViewById(R.id.btn_next);
        btn_next.setOnClickListener(this);

        // Получаем полный рандомный список ID всех слов со словаря
        // wordsIDList = mDb.getFullRandomListID(mDb.getAllData(Consts.LIST_TYPE_ALL, null));

        wordsIDList = mDb.getFullListID(mDb.getAllData(Consts.LIST_TYPE_ALL, Consts.ORDER_BY_RATING, null), false);

    }


    // Указываем как нам формировать меню
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    // Реакция на нажатие кнопок в меню
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.ab_settings:
                if (!item.isChecked()) {
                    Intent intentSet = new Intent(ctx, SettingsActivity.class);
                    startActivity(intentSet);
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    /** Получаем с настроек значение задержки до появления нового слова */
    @Override
    protected void onStart() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String delay = prefs.getString(
                getResources().getString(R.string.pref_delay_key),
                getResources().getString(R.string.pref_delay_default));
        mPrefDelay = Integer.parseInt(delay);

        mPrefDirection = prefs.getString(
                getResources().getString(R.string.pref_direction_key),
                getResources().getString(R.string.pref_direction_default));

        mVibration = prefs.getBoolean(
                getResources().getString(R.string.pref_vibration_key),
                false);

        mSound = prefs.getBoolean(
                getResources().getString(R.string.pref_sound_key),
                false);

        setDirectionTranslate();

        // выводим очередное слово. Нужно если меняется язык через настройки.
        // todo Поставить проверку на случай сворачивания и разворачивания приложения
        showNextWord();
        super.onStart();
    }


    // В зависисости от режима перевода слов определяем какое поле будет базовым
    private void setDirectionTranslate(){
        switch (mPrefDirection){
            case "direction_rus":
                mColumnWordOriginal = DB.COLUMN_ENG;
                break;
            case "direction_eng":
                mColumnWordOriginal = DB.COLUMN_RUS;
                break;
            case "direction_mix":
                Random random = new Random();
                if (random.nextBoolean()){
                    mColumnWordOriginal = DB.COLUMN_ENG;
                } else {
                    mColumnWordOriginal = DB.COLUMN_RUS;
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_next:
                showNextWord();
                break;
            case R.id.btn_answer1:
            case R.id.btn_answer2:
            case R.id.btn_answer3:
            case R.id.btn_answer4:
                checkAnswer((Button)findViewById(v.getId()));
                break;
            default:
                break;
        }
    }

    /** формируем и отображаем следующий вопрос с тремя ложными ответами */
    private void showNextWord(){
        setDirectionTranslate();

        iv_correctly.setVisibility(View.INVISIBLE);
        btn_answer1.setBackgroundResource(R.drawable.bg_button_simple);
        btn_answer2.setBackgroundResource(R.drawable.bg_button_simple);
        btn_answer3.setBackgroundResource(R.drawable.bg_button_simple);
        btn_answer4.setBackgroundResource(R.drawable.bg_button_simple);
        setAnswersClickable(true);
        btn_next.setEnabled(false);

        // получаем текущее слово
        ContentValues currentWord = mDb.getItem(wordsIDList.get(mCurrentID));

        // в зависимости от направления перевода определяем где оригинальное слово, а где перевод
        if (mColumnWordOriginal.contentEquals(DB.COLUMN_ENG)){
            mWordOriginal = currentWord.getAsString(DB.COLUMN_ENG);
            mWordTranslate = currentWord.getAsString(DB.COLUMN_RUS);
        } else {
            mWordOriginal = currentWord.getAsString(DB.COLUMN_RUS);
            mWordTranslate = currentWord.getAsString(DB.COLUMN_ENG);
        }

        mId = wordsIDList.get(mCurrentID);
        mCounterTrue = currentWord.getAsInteger(DB.COLUMN_TRUE);
        mCounterFalse = currentWord.getAsInteger(DB.COLUMN_FALSE);

        tv_word.setText(mWordOriginal);
        // список где будут хранится все варианты ответа (для рандомной подачи на экран)
        ArrayList<String> answers = new ArrayList<>();
        answers.add(mWordTranslate);

        // Получаем ID трех разных вариантов ложных ответов пропуская значение указанные в mCurrentID
        threeFalseAnswerId = Consts.getThreeId(wordsIDList.get(mCurrentID), wordsIDList);
        for (int i = 0; i < threeFalseAnswerId.length; i++) {
            ContentValues answerFalse = mDb.getItem(threeFalseAnswerId[i]);
            if (mColumnWordOriginal.contentEquals(DB.COLUMN_ENG)){
                answers.add(answerFalse.getAsString(DB.COLUMN_RUS));
            } else {
                answers.add(answerFalse.getAsString(DB.COLUMN_ENG));
            }

        }

        ArrayList<Integer> randomNumList = mDb.makeRandomList(new ArrayList<Integer>(Arrays.asList(0,1,2,3)));

        btn_answer1.setText(answers.get(randomNumList.get(0)));
        btn_answer2.setText(answers.get(randomNumList.get(1)));
        btn_answer3.setText(answers.get(randomNumList.get(2)));
        btn_answer4.setText(answers.get(randomNumList.get(3)));

        // Пока номер текущего слова меньше размера массива всех ID слов можем выводить следующее слово
        if (wordsIDList.size() > (mCurrentID +1)) {
            mCurrentID++;
        } else {
            btn_next.setVisibility(View.GONE);
            btn_answer1.setVisibility(View.GONE);
            btn_answer2.setVisibility(View.GONE);
            btn_answer3.setVisibility(View.GONE);
            btn_answer4.setVisibility(View.GONE);
            tv_word.setText(showResult());
        }
        // Callback отработал
        mHasRunCallback = false;
    }

    Runnable runnableShowNextWord = new Runnable() {
        @Override
        public void run() {
            showNextWord();
        }
    };


    /** Формируем итоги */
    private String showResult(){
        String message = getResources().getString(R.string.answers_true) + " = " + mTotalTrueAnswers + "\n" +
                getResources().getString(R.string.answers_false) + " = " + mTotalFalseAnswers;
        mTotalFalseAnswers = 0;
        mTotalTrueAnswers = 0;
        return message;
    }


    /** Проверяем правильно ли выбран ответ */
    private  void checkAnswer(Button b){
        setAnswersClickable(false);
        if (b.getText().toString().contentEquals(mWordTranslate)){
            if (mPrefDelay != 0) {
                b.setBackgroundResource(R.drawable.bg_button_green);
                iv_correctly.startAnimation(mAnim);
                iv_correctly.setVisibility(View.VISIBLE);
                btn_next.setEnabled(false);
            }
            registrationAnswer(true);   // отмечаем в БД, что дан правильный ответ

            playResult(true);



            // Показываем следующее слово через задержку mDelay взятую в параметрах ранее
            handler = new Handler();
            handler.postDelayed(runnableShowNextWord, mPrefDelay);
            mHasRunCallback = true;
        } else {

            // вибро если оно включенно в настройках
            if (mVibration){
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(300);
            }

            playResult(false);

            b.setBackgroundResource(R.drawable.bg_button_red);
            if (btn_answer1.getText() == mWordTranslate)
                btn_answer1.setBackgroundResource(R.drawable.bg_button_green);
            if (btn_answer2.getText() == mWordTranslate)
                btn_answer2.setBackgroundResource(R.drawable.bg_button_green);
            if (btn_answer3.getText() == mWordTranslate)
                btn_answer3.setBackgroundResource(R.drawable.bg_button_green);
            if (btn_answer4.getText() == mWordTranslate)
                btn_answer4.setBackgroundResource(R.drawable.bg_button_green);
            registrationAnswer(false);   // отмечаем в БД, что дан ложный ответ
            btn_next.setEnabled(true);
        }


    }

    /** Включение/отключение кнопок в зависимости от того был ли выбран вариант */
    private void setAnswersClickable(Boolean b){
        btn_answer1.setClickable(b);
        btn_answer2.setClickable(b);
        btn_answer3.setClickable(b);
        btn_answer4.setClickable(b);
    }


    /** Изменяем в БД кол-во правильных и неправильны ответов по конкретному слову */
    private void registrationAnswer(boolean answer){
        // Если нажали ЗНАЮ, то увеличиваем кол-во правильных ответов. В противном случае - неправильных
        if (answer) {
            mCounterTrue++;
            mTotalTrueAnswers++;
        }
        else {
            mCounterFalse++;
            mTotalFalseAnswers++;
        }

        ContentValues cv = new ContentValues();
        cv.put(DB.COLUMN_TRUE, mCounterTrue);
        cv.put(DB.COLUMN_FALSE, mCounterFalse);
        cv.put(DB.COLUMN_LEVEL, mCounterTrue - mCounterFalse);
        mDb.changeRec(cv, Integer.toString(mId));
    }


    /**  Закрытие базы и плеера перед уничтожением активити */
    protected void onDestroy() {
        // Проверяем не начат-ли показ следующего слова (с задержкой)
        // уничтожаем колбек если начат и закрываем базу
        if (mHasRunCallback){
            handler.removeCallbacks(runnableShowNextWord);
        }

        // Если результаты есть и они не показывались при переборе всех слов то показываем итоги
        if (!(mTotalTrueAnswers == 0) && (mTotalFalseAnswers == 0)){
            Toast.makeText(ctx, showResult(), Toast.LENGTH_LONG).show();
        }

        mDb.close();
        super.onDestroy();
        }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }

    /** Произываем семпл в зависимости от настроек и корректрости ответа */
    private void playResult(boolean answer){
        // проигрываем если в настройках звук включен
        if (mSound) {
            if (mMediaPlayer != null) {
                mMediaPlayer.release();
            }
            if (answer) {
                mMediaPlayer = MediaPlayer.create(ctx, R.raw.correct);
                mMediaPlayer.start();
            } else {
                mMediaPlayer = MediaPlayer.create(ctx, R.raw.incorrect);
                mMediaPlayer.start();
            }
        }

    }
}
