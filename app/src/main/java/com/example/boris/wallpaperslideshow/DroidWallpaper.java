package com.example.boris.wallpaperslideshow;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
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

    @Override
    public WallpaperService.Engine onCreateEngine() {
            return new DroidWallpaperEngine();
    }

    private Bitmap initMainBitmap() throws IOException{

        File path = Environment.getExternalStorageDirectory();
        File dir = new File(path + "/WallpaperSlideshow/");
        dir.mkdirs();
        files = dir.listFiles();

        numberOfImages = files.length;
        if (numberOfImages == 0){
            return BitmapFactory.decodeResource(getResources(), R.drawable.pic11);
        }

        if (currentImage >= numberOfImages)
            currentImage = 0;
        FileInputStream streamIn = new FileInputStream(files[currentImage++]);
        Bitmap bitmap = BitmapFactory.decodeStream(streamIn); //This gets the image

        streamIn.close();

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

        private void draw(){
            if (visible){
                Bitmap bitmap = null;
                try {
                    bitmap = initMainBitmap();
                } catch (IOException e) {
                    makeErrorNotification("error in drawing picture\n" + e.toString());
                }

                if (bitmap == null){
                    makeErrorNotification("bitmap is null");
                    return;
                }

                //makeErrorNotification("drawing: " + currentImage + " of " + numberOfImages + " \nname: " + files[currentImage - 1].getName() + "\nbitmap: " + bitmap.toString());
                if (!holder.isCreating()){
                    Canvas canvas = holder.lockCanvas();
                    canvas.save();
                    Bitmap thumbnail = ThumbnailUtils.extractThumbnail(bitmap,
                            Resources.getSystem().getDisplayMetrics().widthPixels, Resources.getSystem().getDisplayMetrics().heightPixels);
                    canvas.drawBitmap(thumbnail, matrix, null);
                    canvas.restore();

                    lastTimeAll = System.currentTimeMillis();
                    holder.unlockCanvasAndPost(canvas);
                }

                //get next slide duration
                duration = getSharedPreferences(MainActivity.PREFETNCE_KEY, MODE_PRIVATE).getLong("SlideTime", 0);
                if (duration != 0){
                    frameDuration = TimeUnit.SECONDS.toMillis(duration);
                }

                handler.removeCallbacks(drawImage); // remove other handlers
                handler.postDelayed(drawImage, frameDuration);// run next slide
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            if (visible){
                if (lastTimeAll + TimeUnit.SECONDS.toMillis(duration) < System.currentTimeMillis())
                handler.post(drawImage);
            }else{
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
            handler.removeCallbacks(drawImage);
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);

            //double click event
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                if (System.currentTimeMillis() - lastPressTime < 500){
                    lastPressTime = 0;
                    draw();
                }else{
                    lastPressTime = System.currentTimeMillis();
                }
            }
        }
    }

    private void makeErrorNotification(String not){
        System.out.println(not);
        Toast.makeText(getApplicationContext(), not, Toast.LENGTH_LONG).show();
    }


}


// bitmap doest exchanges!!!!!!!!!!!!!!!!!!!!!!!