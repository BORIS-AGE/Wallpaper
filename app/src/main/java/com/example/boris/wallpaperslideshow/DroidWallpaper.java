package com.example.boris.wallpaperslideshow;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Environment;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DroidWallpaper extends WallpaperService {
    private Bitmap bitmap;
    @Override
    public WallpaperService.Engine onCreateEngine() {
        System.out.println("came!!!!!");
        try {
            File path = Environment.getExternalStorageDirectory();
            File dir = new File(path + "/WallpaperSlideshow/");
//            dir.mkdirs();

            File file = new File(dir,"name.jpg");
            FileInputStream streamIn = new FileInputStream(file);
            bitmap = BitmapFactory.decodeStream(streamIn); //This gets the image
            streamIn.close();

            //kostul
            initMainBitmap(getResources(), R.drawable.pic11);

            return new DroidWallpaperEngine(bitmap);
        } catch (IOException e) {
            System.out.println(e.toString());
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
            return null;
        }


    }

    private void initMainBitmap(Resources res, int resId){
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pic11);
    }

    private class DroidWallpaperEngine extends WallpaperService.Engine {

        private final int frameDuration = 20;
        private SurfaceHolder holder;
        private Bitmap bitmap;
        private boolean visible;
        private Handler handler;

        public DroidWallpaperEngine(Bitmap bitmap) {
            this.bitmap = bitmap;
            handler= new Handler();
            //initMainBitmap(getResources(), R.drawable.pic11);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            this.holder = surfaceHolder;
        }

        private Runnable drawImage = new Runnable() {
            @Override
            public void run() {
                draw();
            }
        };

        private void draw(){
            if (visible){
                Canvas canvas = getSurfaceHolder().lockCanvas();
                canvas.save();
                canvas.scale(1f,1f);
                canvas.drawBitmap(bitmap, new Matrix(), null);
                canvas.restore();
                holder.unlockCanvasAndPost(canvas);

                handler.removeCallbacks(drawImage); // remove other handlers
                handler.postDelayed(drawImage, frameDuration);// run next slide
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            if (visible){
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
    }
}

















