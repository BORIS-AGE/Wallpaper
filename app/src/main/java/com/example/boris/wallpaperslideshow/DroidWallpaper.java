package com.example.boris.wallpaperslideshow;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class DroidWallpaper extends WallpaperService {
    private int numberOfImages, currentImage;
    private File[] files;
    private long lastPressTime = 0, lastTimeAll = 0;
    private Matrix matrix = new Matrix();
    private long duration = 0;
    private boolean endThread = false;
    private int width = Resources.getSystem().getDisplayMetrics().widthPixels, height = Resources.getSystem().getDisplayMetrics().heightPixels;
    private double fireX = 0.01;

    private float numberOfCircles = 10, sizeOfCircles = 20, speedOfCircles = 0.04f;


    @Override
    public WallpaperService.Engine onCreateEngine() {
        return new DroidWallpaperEngine();
    }

    private Bitmap initMainBitmap() throws IOException {

        File path = Environment.getExternalStorageDirectory();
        File dir = new File(path + "/WallpaperSlideshow/");
        dir.mkdirs();
        files = dir.listFiles();

        numberOfImages = files.length;
        if (numberOfImages == 0) {
            return BitmapFactory.decodeResource(getResources(), R.drawable.pic11);
        }

        if (currentImage >= numberOfImages)
            currentImage = 0;
        FileInputStream streamIn = new FileInputStream(files[currentImage++]);
        Bitmap bitmap = BitmapFactory.decodeStream(streamIn); //This gets the image
        streamIn.close();
        bitmap = ThumbnailUtils.extractThumbnail(bitmap,
                Resources.getSystem().getDisplayMetrics().widthPixels, Resources.getSystem().getDisplayMetrics().heightPixels);

        return bitmap;
    }

    private class DroidWallpaperEngine extends WallpaperService.Engine {

        private long frameDuration = TimeUnit.SECONDS.toMillis(5);
        private SurfaceHolder holder;
        private boolean visible;
        private Handler handler;

        public DroidWallpaperEngine() {
            handler = new Handler();
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            this.holder = surfaceHolder;
        }

        private Runnable drawImage = () -> draw();

        private void draw() {
            if (visible) {
                Bitmap bitmap = null;
                try {
                    bitmap = initMainBitmap();
                } catch (IOException e) {
                    makeErrorNotification("error in drawing picture\n" + e.toString());
                }

                if (bitmap == null) {
                    makeErrorNotification("bitmap is null");
                    return;
                }


                //makeErrorNotification("drawing: " + currentImage + " of " + numberOfImages + " \nname: " + files[currentImage - 1].getName() + "\nbitmap: " + bitmap.toString());
                Bitmap finalBitmap = bitmap;
                Paint paint = new Paint();
                paint.setStrokeWidth(8);
                paint.setStyle(Paint.Style.FILL);
                Path path1 = new Path();
                new Thread(() -> {
                    while (lastTimeAll + frameDuration > System.currentTimeMillis() && !endThread) {
                        if (!holder.isCreating()) {
                            Canvas canvas = holder.lockCanvas();
                            canvas.save();
                            canvas.drawBitmap(finalBitmap, matrix, null); // draw main picture
                            canvas.restore();

                            //draw fire
                            drowFire(canvas, path1, paint);

                            holder.unlockCanvasAndPost(canvas);
                        }
                    }
                    endThread = false;
                }).start();


                lastTimeAll = System.currentTimeMillis();
                //get next slide duration
                duration = getSharedPreferences(MainActivity.PREFETNCE_KEY, MODE_PRIVATE).getLong("SlideTime", 0);
                if (duration != 0) {
                    frameDuration = TimeUnit.SECONDS.toMillis(duration);
                }

                handler.removeCallbacks(drawImage); // remove other handlers
                handler.postDelayed(drawImage, frameDuration);// run next slide
                //handler.post(drawImage);// run next slide
            }
        }

        private void drowFire(Canvas canvas, Path path1, Paint paint) { //274
            for (float n = 0; n < numberOfCircles/10; n+=speedOfCircles) {

                path1.addCircle(getX(fireX + n), getY(fireX + n), 5 + (n * sizeOfCircles), Path.Direction.CW);
                System.out.println("X = " + fireX + " - " + getX(fireX) + " Y = " + fireX + " - " + getY(fireX));
            }
            fireX += 0.01;

            canvas.drawPath(path1, paint);
            path1.reset();
        }

        private float getX(double i) {
            //double scale = 2 / (3 - Math.cos(2 * i));
            return (float) ( Math.cos(i) * 270 + width/2);
        }

        private float getY(double i) {
            //double scale = 2 / (3 - Math.cos(2 * i));
            return (float) ((Math.sin(2 * i) / 2) * 270 + height/2);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            if (visible) {
                if (lastTimeAll + TimeUnit.SECONDS.toMillis(duration) < System.currentTimeMillis())
                    handler.post(drawImage);
            } else {
                handler.removeCallbacks(drawImage);
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            handler.removeCallbacks(drawImage);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            visible = false;
            endThread = true;
            handler.removeCallbacks(drawImage);
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);

            //double click event
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (System.currentTimeMillis() - lastPressTime < 500) {
                    lastPressTime = 0;
                    endThread = true;
                    draw();
                } else {
                    lastPressTime = System.currentTimeMillis();
                }
            }
        }
    }

    private void makeErrorNotification(String not) {
        System.out.println(not);
        Toast.makeText(getApplicationContext(), not, Toast.LENGTH_LONG).show();
    }
}


// bitmap doest exchanges!!!!!!!!!!!!!!!!!!!!!!!

/* private void drowFire(Canvas canvas, Path path1, Paint paint) { //274
            for (int n = 0; n < numberOfCircles; n += 10) {
                switch (part) {
                    case 1:
                        if (getX(fireX) > width / 2){
                            System.out.println(2 + "");
                            part = 2;
                            fireX = 0;
                            fireY = 0;
                        }
                        break;
                    case 2:
                        if (getX(fireX) > width * 7 / 8){
                            System.out.println(3 + "");
                            part = 3;
                            fireX = 0;
                            fireY = 0;
                        }
                        break;
                    case 3:
                        if (getX(fireX) < width / 2){
                            System.out.println(4 + "" + "\nX = " + getX(fireX) + " width = " + width/2);
                            part = 4;
                            fireX = 0;
                            fireY = 0;
                        }
                        break;
                    case 4:
                        if (getX(fireX) < width / 8){
                            System.out.println(1 + "");
                            part = 1;
                            fireX = 0;
                            fireY = 0;
                        }
                        break;
                }

                path1.addCircle(getX(fireX + n), getY(fireY + n), 30, Path.Direction.CW);
            }
            if (part == 1 || part == 2) fireX++; else fireX--;
            fireY++;
            canvas.drawPath(path1, paint);
            path1.reset();
        }

        private float getX(int i) {
            float koef = 1.2f;
            switch (part) {
                case 1:
                    return (float) ((i*koef) + width / 8);
                case 2:
                    return (float) ((i*koef) + width / 8 + width / 2);
                case 3:
                    return (float) ((i*koef) + width * 7 / 8);
                case 4:
                    return (float) ((i*koef) + width * 7 / 8);
            }
            return (float) ((i) + width / 8);
        }

        private float getY(int i) {
            switch (part) {
                case 1:
                    return (float) Math.cos((i) * 0.019) * 100 + height / 2;
                case 2:
                    return (float) Math.cos((-i) * 0.019) * 100 + height / 2;
                case 3:
                    return (float) Math.cos((i) * 0.019) * 100 + height / 2;
                case 4:
                    return (float) Math.cos((-i) * 0.019) * 100 + height / 2;
            }
            return (float) Math.cos((i) * 0.019) * 100 + height / 2;
        }*/