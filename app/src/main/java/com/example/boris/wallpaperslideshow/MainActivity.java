package com.example.boris.wallpaperslideshow;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.service.wallpaper.WallpaperService;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGES = 123;
    private static final int MY_PERMISSION = 666;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setDefaults();
        requirePermissions();

    }
    private void requirePermissions() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION);
        }else if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION);
        }else if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SET_WALLPAPER) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SET_WALLPAPER}, MY_PERMISSION);
        }
    }

    private void setDefaults() {
    }

    private String recordImage(Uri uri) {
        Bitmap img = null;
        InputStream fls;
        try {
            fls = getContentResolver().openInputStream(uri);
            byte[] image = new byte[fls.available()];
            fls.read(image);
            fls.close();
            img = BitmapFactory.decodeByteArray(image, 0, image.length);
        } catch (Exception e) {
            makeErrorNotification(e.toString());
        }

        File path = Environment.getExternalStorageDirectory();
        File dir = new File(path + "/WallpaperSlideshow/");
        dir.mkdirs();

        File file = new File(dir,System.currentTimeMillis() + ".jpg");
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            img.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            makeErrorNotification(e.toString());
        } catch (IOException e) {
            makeErrorNotification(e.toString());
        }

        //setWallpaper(file.getPath());

        return file.getPath();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        final List<String> imagesEncodedList;
        try {
            // When an Image is picked
            if (requestCode == PICK_IMAGES && resultCode == RESULT_OK
                    && null != data) {
                imagesEncodedList = new ArrayList<String>();
                if(data.getData()!=null){
                    imagesEncodedList.add(data.getData().toString());
                    makeErrorNotification("Selected Images: " + imagesEncodedList.size());
                } else {
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                        ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                        for (int i = 0; i < mClipData.getItemCount(); i++) {
                            imagesEncodedList.add(mClipData.getItemAt(i).getUri().toString());
                        }
                        makeErrorNotification("Selected Images: " + mArrayUri.size());
                    }
                }
                //record list, made function run on UI just for fun, I know about Looper.prepare();  :)
                final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                new Thread(){
                    @Override
                    public void run() {
                        runOnUiThread(() -> {
                                progressDialog.setMessage("recording files...");
                                progressDialog.show();
                            }
                        );
                        for (String url : imagesEncodedList){
                            recordImage(Uri.parse(url));
                        }
                        imagesEncodedList.clear();
                        runOnUiThread(() -> {
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                        });
                    }
                }.start();

            } else {
                makeErrorNotification("You haven't picked Image");
            }
        } catch (Exception e) {
            makeErrorNotification("Something went wrong");
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(MainActivity.this, "Permission garanted", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                    finish();
                }
            break;
        }
    }

    private void makeErrorNotification(String not){
        System.out.println(not);
        Toast.makeText(getApplicationContext(), not, Toast.LENGTH_LONG).show();
    }

    public void choose(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), PICK_IMAGES);
    }
}
/*    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
    startActivityForResult(intent, PICK_IMAGE);*/

/*private void setWallpaper(String url){
        WallpaperManager wm = WallpaperManager.getInstance(getApplicationContext());
        try {
            File path = Environment.getExternalStorageDirectory();
            wm.setBitmap(BitmapFactory.decodeFile(url));
        }catch (Exception e){
            makeErrorNotification(e.toString());
        }
    }*/



