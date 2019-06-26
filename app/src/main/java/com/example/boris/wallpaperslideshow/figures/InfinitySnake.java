package com.example.boris.wallpaperslideshow.figures;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;

import com.example.boris.wallpaperslideshow.R;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;

public class InfinitySnake {
    private Context context;
    private float fireX, fireY, X = 0.01f, fingerX, fingerY, distanceX, distanceY, lastX, lastY;
    private List<Float> arrX = new ArrayList<>(), arrY = new ArrayList<>();
    private int width = Resources.getSystem().getDisplayMetrics().widthPixels, height = Resources.getSystem().getDisplayMetrics().heightPixels;

    private float speedOfCircles = 0.02f;
    private int numberOfCircles = 60, sizeOfCircles = 30;
    private boolean normalMode = true, flyToFinger = false, flyBack = false, firstReady = false;

    public InfinitySnake(Context context) {
        this.context = context;
    }

    public void drowFire(Canvas canvas, Path path1, Paint paint) {
        if (normalMode){
            arrX.add(getX(X));
            arrY.add(getY(X));
            if(arrX.size() > numberOfCircles){
                float i = 0;
                for (int n = arrX.size() - numberOfCircles - 1; n < arrX.size() ; n++) {
                    path1.addCircle(arrX.get(n), arrY.get(n), 5 + (i++ / 100 * sizeOfCircles), Path.Direction.CW);
                }
            }else{
                int i = 0;
                for (float n = 0; n < numberOfCircles / 100; n += 0.02f) {
                    path1.addCircle(getX(X + n), getY(X + n), 5 + (n * sizeOfCircles), Path.Direction.CW);
                    arrX.add(i, getX(X + n)); //record last value X
                    arrY.add(i++, getY(X + n)); //record last value Y
                }
            }
            X += speedOfCircles;
        }
        if (flyToFinger) {
            arrX.add(getXfly());
            arrY.add(getYfly());
            float i = 0;
            for (int n = arrX.size() - numberOfCircles - 1; n < arrX.size(); n++) {
                path1.addCircle(arrX.get(n), arrY.get(n), 5 + (i++ / 100 * sizeOfCircles), Path.Direction.CW);
            }
        }
        if (flyBack) {
            arrX.add(getXfly());
            arrY.add(getYfly());
            float i = 0;
            for (int n = arrX.size() - numberOfCircles - 1; n < arrX.size(); n++) {
                path1.addCircle(arrX.get(n), arrY.get(n), 5 + (i++ / 100 * sizeOfCircles), Path.Direction.CW);
            }
        }
        canvas.drawPath(path1, paint);
        path1.reset();
    }

    public void setPaint(Paint paint){
        // Gradient Shade colors
        paint.setStrokeWidth(8);
        paint.setShader(new RadialGradient(
                width/2, height/2,
                250,
                ContextCompat.getColor(context,R.color.red),
                ContextCompat.getColor(context,R.color.orange),
                Shader.TileMode.MIRROR));
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    private float getX(float i) {
        //float scale = 2 / (3 - (float)Math.cos(2 * i));
        fireX = (float) Math.cos(i) * 270 + width / 2;
        lastX = fireX;
        return fireX;
    }

    private float getY(float i) {
        //double scale = 2 / (3 - Math.cos(2 * i));
        fireY = (float) ((Math.sin(2 * i) / 2) * 270 + height / 2);
        lastY = fireY;
        return fireY;
    }

    private float getXfly() {
        if (flyToFinger) {
            if ((fingerX - fireX > 0 && fingerX - fireX > 10) || (fingerX - fireX < 0 && fingerX - fireX < 10))
                fireX += distanceX / 60;
        }
        if (flyBack) {
            if ((lastX - fireX > 0 && lastX - fireX > 5) || (lastX - fireX < 0 && lastX - fireX < 5))
                fireX += distanceX / 60;
            else if (firstReady) {
                flyBack = false;
                normalMode = true;
                firstReady = false;
            }else{
                firstReady = true;
            }
        }
        return fireX;
    }

    private float getYfly() {
        if (flyToFinger) {
            if ((fingerY - fireY > 0 && fingerY - fireY > 10) || (fingerY - fireY < 0 && fingerY - fireY < 10))
                fireY += distanceY / 60;
        }
        if (flyBack) {
            if ((lastY - fireY > 0 && lastY - fireY > 5) || (lastY - fireY < 0 && lastY - fireY < 5))
                fireY += distanceY / 60;
            else if (firstReady) {
                flyBack = false;
                normalMode = true;
                firstReady = false;
            }else{
                firstReady = true;
            }
        }

        return fireY;
    }

    public void setMode(boolean normalMode, boolean flyToFinger, boolean flyBack) {
        this.normalMode = normalMode;
        this.flyToFinger = flyToFinger;
        this.flyBack = flyBack;
    }

    public void clearArrays(){
        arrX.clear();
        arrY.clear();
    }

    public void setFingers(float fingerX, float fingerY){
        this.fingerX = fingerX;
        this.fingerY = fingerY;
    }

    public void setDistances() {
        if (flyToFinger) {
            distanceX = fingerX - fireX;
            distanceY = fingerY - fireY;
        }
        if (flyBack){
            distanceX = lastX - fireX;
            distanceY = lastY - fireY;
        }
    }

    public void setSizeOfCircles(int sizeOfCircles) {
        this.sizeOfCircles = sizeOfCircles;
    }
}
