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
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class DroidWallpaper extends WallpaperService {
    private int numberOfImages, currentImage;
    private File[] files;

    @Override
    public WallpaperService.Engine onCreateEngine() {
            return new DroidWallpaperEngine();
    }

    private Bitmap initMainBitmap() throws IOException{

        File path = Environment.getExternalStorageDirectory();
        File dir = new File(path + "/WallpaperSlideshow/");
//            dir.mkdirs();
        files = dir.listFiles();
        numberOfImages = files.length;
        if (currentImage >= numberOfImages)
            currentImage = 0;
        FileInputStream streamIn = new FileInputStream(files[currentImage++]);
        Bitmap bitmap = BitmapFactory.decodeStream(streamIn); //This gets the image

        streamIn.close();

        return bitmap;
    }

    private class DroidWallpaperEngine extends WallpaperService.Engine {

        private final long frameDuration = TimeUnit.SECONDS.toMillis(5);
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
                    makeErrorNotification("error in drawing picture");
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
                    canvas.drawBitmap(thumbnail, new Matrix(), null);
                    canvas.restore();

                    holder.unlockCanvasAndPost(canvas);
                }
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

    private void makeErrorNotification(String not){
        System.out.println(not);
        Toast.makeText(getApplicationContext(), not, Toast.LENGTH_LONG).show();
    }
}


// bitmap doest exchanges!!!!!!!!!!!!!!!!!!!!!!!