package lk.sliit.androidalarmsystem.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import lk.sliit.androidalarmsystem.AlarmDatabaseHelper;
import lk.sliit.androidalarmsystem.QuestionDatabaseHelper;
import lk.sliit.androidalarmsystem.domain.Alarm;
import lk.sliit.androidalarmsystem.R;
import lk.sliit.androidalarmsystem.domain.Question;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class AlarmRingActivity extends AppCompatActivity {

    private final static String TAG = "APP-AlarmRingActivity";
    // This HashMap maps the resIds to RadioButton Number
    private HashMap<Integer, Integer> radioIdMap = new HashMap<>();
    // Maps the RadioButton Number to choiceId
    private HashMap<Integer, Long> choiceIdMap = new HashMap<>();

    private MediaPlayer mediaPlayer;

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    //    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
//            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */

    private Button checkButton;
    private TextView alarmName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Started Activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_ring);
        hide();
        mVisible = true;
        mContentView = findViewById(R.id.alarm_ring_content);
        alarmName = findViewById(R.id.alarmNameTextView);
        checkButton = findViewById(R.id.submitBtn);

        AlarmDatabaseHelper helper = new AlarmDatabaseHelper(getApplicationContext());

        Intent intent = getIntent();
        String idAsString = intent.getStringExtra("alarmId");
        long id = Long.parseLong(idAsString);
        Alarm alarm = helper.read(id);

        alarmName.setText(alarm.getName());

        mediaPlayer = MediaPlayer.create(this, Alarm.getToneResId((int) alarm.getAlarmToneId()));
        mediaPlayer.start();

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
//        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        // Waking up the device
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        refreshQuestion();
    }

    private void refreshQuestion() {
        QuestionDatabaseHelper db = new QuestionDatabaseHelper(getApplicationContext());
        int count = db.getQuestionsCount();

        Random random = new Random();
        int questionId = random.nextInt(count) + 1;

        final Question question = db.getQuestion(questionId);
        TreeMap<Long, String> choices = question.getChoices();

        TextView questionTxtView = findViewById(R.id.question);
        TextView choice_1TxtView = findViewById(R.id.choice_1);
        TextView choice_2TxtView = findViewById(R.id.choice_2);
        TextView choice_3TxtView = findViewById(R.id.choice_3);
        TextView choice_4TxtView = findViewById(R.id.choice_4);

        radioIdMap.put(R.id.choice_1, 0);
        radioIdMap.put(R.id.choice_2, 1);
        radioIdMap.put(R.id.choice_3, 2);
        radioIdMap.put(R.id.choice_4, 3);

        questionTxtView.setText(question.getQuestion());
        Set<Long> ids = choices.keySet();
        ArrayList<String> choicesList = new ArrayList<>();
        int x = 0;
        for (Long choiceId : ids) {
            choicesList.add(choices.get(choiceId));
            choiceIdMap.put(x, choiceId);
            x++;
        }

        choice_1TxtView.setText(choicesList.get(0));
        choice_2TxtView.setText(choicesList.get(1));
        choice_3TxtView.setText(choicesList.get(2));
        choice_4TxtView.setText(choicesList.get(3));

        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioGroup radioGroup = findViewById(R.id.choice_radio_group);
                int radioBtnId = radioGroup.getCheckedRadioButtonId();

                int radioBtnNumber = radioIdMap.get(radioBtnId);
                long choiceId = choiceIdMap.get(radioBtnNumber);

                long answerId = question.getAnswer();

                if (answerId == choiceId) {
                    mediaPlayer.stop();
                    finish();
                } else {
                    refreshQuestion();
                    Snackbar.make(findViewById(R.id.alarm_ring_content),
                            "Selected Answer is Incorrect", Snackbar.LENGTH_LONG).show();
                }

            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
//        hide();
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "Back Pressed");
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
