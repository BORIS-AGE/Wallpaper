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

public class SettingsActivity extends AppCompatActivity {

    private SeekBar timeSeekbar, sizeSeekbar, lengthSeekbar;
    private TextView seekTime, sizeText, lengthText;
    private SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        timeSeekbar = findViewById(R.id.seekBar);
        sizeSeekbar = findViewById(R.id.sizeSickbar);
        lengthSeekbar = findViewById(R.id.lengthSickbar);

        seekTime = findViewById(R.id.settingsSlideTime);
        sizeText = findViewById(R.id.sizeTextSettings);
        lengthText = findViewById(R.id.lengthTextSettings);

        timeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
        });
        sizeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sizeText.setText(progress + "px");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                editor.putInt("SnakeSize", seekBar.getProgress());
                editor.commit();
            }
        });
        lengthSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lengthText.setText((progress * 5) + "px");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                editor.putInt("SnakeLength", seekBar.getProgress() * 5);
                editor.commit();
            }
        });

        long defaultSeek = getSharedPreferences(MainActivity.PREFETNCE_KEY, MODE_PRIVATE).getLong("SlideTime", 0);
        timeSeekbar.setProgress(defaultSeek <= 60? (int)defaultSeek/5 : (int)(defaultSeek/30) + 10);
        double time = formatProgressTime(timeSeekbar.getProgress());
        if (time <= 60)
            seekTime.setText(((int)time) + "sec");
        else
            seekTime.setText((time/60) + "min");

        int defaultSize = getSharedPreferences(MainActivity.PREFETNCE_KEY, MODE_PRIVATE).getInt("SnakeSize", 30);
        sizeSeekbar.setProgress(defaultSize);
        sizeText.setText(defaultSize + "px");

        int defaultLength = getSharedPreferences(MainActivity.PREFETNCE_KEY, MODE_PRIVATE).getInt("SnakeLength", 60);
        lengthSeekbar.setProgress(defaultLength / 5);
        lengthText.setText(defaultLength + "px");

        editor = getSharedPreferences(MainActivity.PREFETNCE_KEY, MODE_PRIVATE).edit();
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









