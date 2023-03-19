package com.riopapa.eggcook;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    int boilTime, waitTime, runTime, decrease = 5;
    ImageView ivGif;

    boolean isGoing = false;
    Timer timer;
    Activity mActivity;
    TextToSpeech myTTS;
    SharedPreferences sharePref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;
        TextView tvReady = findViewById(R.id.ready);
        tvReady.setVisibility(View.GONE);
        sharePref = this.getSharedPreferences("sayText", MODE_PRIVATE);
        editor = sharePref.edit();

        boilTime = sharePref.getInt("boil", 7 * 60);
        waitTime = sharePref.getInt("wait", 4 * 60);;

        showGif(false);

        TextView tvBoil = findViewById(R.id.txt_boil);
        tvBoil.setText(time2Str(boilTime));
        TextView tvBoilM = findViewById(R.id.txt_boil_m);
        tvBoilM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boilTime -= 10;
                tvBoil.setText(time2Str(boilTime));
                editor.putInt("boil", boilTime);
                editor.apply();
            }
        });
        TextView tvBoilP = findViewById(R.id.txt_boil_p);
        tvBoilP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boilTime += 10;
                tvBoil.setText(time2Str(boilTime));
                editor.putInt("boil", boilTime);
                editor.apply();
            }
        });

        TextView tvWait = findViewById(R.id.txt_wait);
        tvWait.setText(time2Str(waitTime));
        TextView tvWaitM = findViewById(R.id.txt_wait_m);
        tvWaitM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                waitTime -= 10;
                tvWait.setText(time2Str(waitTime));
                editor.putInt("wait", waitTime);
                editor.apply();
            }
        });

        TextView tvWaitP = findViewById(R.id.txt_wait_p);
        tvWaitP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                waitTime += 10;
                tvWait.setText(time2Str(waitTime));
                editor.putInt("wait", waitTime);
                editor.apply();
            }
        });

        TextView tvTextTime = findViewById(R.id.text_time);
        tvTextTime.setText(getApplicationContext().getText(R.string.ready));
        tvTextTime.setSingleLine(true);
        tvTextTime.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        tvTextTime.setSelected(true);

        ImageView ivGo = findViewById(R.id.start_stop);
        ivGo.setImageResource(R.drawable.starting);
        ivGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isGoing = !isGoing;
               if (isGoing) {
                   startBoil();
               } else {
                    finishTimer("");
               }
            }
        });

        myTTS = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                initializeTTS();
            }
        });

    }

    private void showGif(boolean animate) {
        ivGif = findViewById(R.id.egg_gif);
        pl.droidsonroids.gif.GifImageView ani = findViewById(R.id.egg_ani);
        if (animate) {
            ivGif.setVisibility(View.GONE);
            ani.setVisibility(View.VISIBLE);
        } else {
            ivGif.setVisibility(View.VISIBLE);
            ani.setVisibility(View.GONE);
        }
    }

    String time2Str (int t) {
        int min = t / 60;
        int sec = t - min * 60;
        return min +" 분 "+  sec + " 초";
    }

    void startBoil() {
        showGif(true);
        String say = "달걀 제대로 익히기를 시작합니다 "+time2Str(boilTime)+" 간 기다리세요";
        myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);

        ImageView ivGo = findViewById(R.id.start_stop);
        ivGo.setImageResource(R.drawable.stop);
        runTime = boilTime;
        final TimerTask countDown = new TimerTask() {
            @Override
            public void run() {
                sayTime("달걀 익히기 " + time2Str(runTime));
                runTime -= decrease;
                if (runTime < 1) {
                    timer.cancel();
                    startWait();
                }
            }
        };
        timer = new Timer();
        timer.schedule(countDown, 1000, decrease * 1000L);
    }

    void startWait() {

        new Sounds().beep(this, Sounds.BEEP.NOTY);
        new Timer().schedule(new TimerTask() {
            public void run() {
                String say = "달걀 뜸들이기를 시작합니다. 불끄고 "+time2Str(waitTime)+ " 만큼 기다리세요";
                myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);
            }
        }, 2000);   // after beep

        runTime = waitTime;
        final TimerTask countDown = new TimerTask() {
            @Override
            public void run() {
                sayTime("뜸 들이기 "+time2Str(runTime));
                runTime -= decrease;
                if (runTime < 1) {
                    finishTimer("이제 차가운 물에 10초정도 넣었다가 껍질을 벗기면 됩니다");
                }

            }
        };
        timer = new Timer();
        timer.schedule(countDown, 1000, decrease * 1000L);
    }

    void sayTime(String s) {
        mActivity.runOnUiThread(() -> {
            TextView tv = findViewById(R.id.text_time);
            tv.setText(s);
        });
    }
    void finishTimer(String say) {
        timer.cancel();
        mActivity.runOnUiThread(() -> {
            TextView tvTextTime = findViewById(R.id.text_time);
            tvTextTime.setText(getApplicationContext().getText(R.string.ready));
            ImageView ivGo = findViewById(R.id.start_stop);
            ivGo.setImageResource(R.drawable.starting);
            showGif(false);
        });
        if (say.length() > 1) {
            new Sounds().beep(this, Sounds.BEEP.FIN);
            new Timer().schedule(new TimerTask() {
                public void run() {
                    myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);
                }
            }, 2000);   // after beep
        }
    }

    String TTSId = "";

    private void initializeTTS() {

        myTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                TTSId = utteranceId;
            }

            @Override
            // this method will always called from a background thread.
            public void onDone(String utteranceId) {
                if (myTTS.isSpeaking())
                    return;
                myTTS.stop();
            }

            @Override
            public void onError(String utteranceId) { }
        });

        int result = myTTS.setLanguage(Locale.getDefault());
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Toast.makeText(this, "Not supported Language", Toast.LENGTH_SHORT).show();
        } else {
            myTTS.setPitch(1.2f);
            myTTS.setSpeechRate(1.3f);
        }
    }

}