package com.example.boris.wallpaperslideshow;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    private SeekBar seekBar;
    private TextView seekTime;
    private SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        seekBar = findViewById(R.id.seekBar);
        seekTime = findViewById(R.id.settingsSlideTime);
        seekBar.setOnSeekBarChangeListener(this);

        long defaultSeek = getSharedPreferences(MainActivity.PREFETNCE_KEY, MODE_PRIVATE).getLong("SlideTime", 0);
        seekBar.setProgress(defaultSeek <= 60? (int)defaultSeek/5 : (int)(defaultSeek/30) + 10);

        editor = getSharedPreferences(MainActivity.PREFETNCE_KEY, MODE_PRIVATE).edit();
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            double time = formatProgressTime(progress);
            if (time <= 60)
                seekTime.setText(((int)time) + "sec");
            else
                seekTime.setText((time/60) + "min");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBar.getProgress() == 0){
            makeErrorNotification("slide time can't be 0");
            return;
        }
        editor.putLong("SlideTime", formatProgressTime(seekBar.getProgress()));
        editor.commit();
    }

    private int formatProgressTime(int progress){
           if (progress <= 12){
               return progress * 5;
           }else{
               return ((progress - 10) * 30);
           }
    }

    public void makeErrorNotification(String not) {
        System.out.println(not);
        Toast.makeText(getApplicationContext(), not, Toast.LENGTH_LONG).show();
    }

}
