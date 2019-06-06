package com.example.boris.wallpaperslideshow;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.example.boris.wallpaperslideshow.adapters.MyRecycler;
import com.example.boris.wallpaperslideshow.models.PhotoModel;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

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
    private MyRecycler recycler;
    private RecyclerView recyclerView;
    private List<PhotoModel> photoModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requirePermissions();
        setDefaults();
        getImages();
        setRecycler();
    }

    private void setRecycler() {
        recycler = new MyRecycler(photoModels, this);
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(recycler);
    }

    private void getImages() {
        File path = Environment.getExternalStorageDirectory();
        File dir = new File(path + "/WallpaperSlideshow/");
//            dir.mkdirs();
        File[] files = dir.listFiles();
        int numberOfImages = files.length;
        if (numberOfImages == 0) {
            makeErrorNotification("no images found");
            return;
        }
        for (File f : files) photoModels.add(new PhotoModel(f));
    }

    private void requirePermissions() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION);
        } else if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION);
        } else if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SET_WALLPAPER) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SET_WALLPAPER}, MY_PERMISSION);
        } else if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSION);
        }

    }

    private void setDefaults() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recyclerView = findViewById(R.id.recycler);
        photoModels = new ArrayList<>();
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

        File file = new File(dir, System.currentTimeMillis() + ".jpg");
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

    private void recordImage(Bitmap bitmap) {
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        final File file = null;
        new Thread() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                            progressDialog.setMessage("recording files...");
                            progressDialog.show();
                        }
                );
                File path = Environment.getExternalStorageDirectory();
                File dir = new File(path + "/WallpaperSlideshow/");
                dir.mkdirs();

                File file = new File(dir, System.currentTimeMillis() + ".jpg");
                OutputStream out = null;
                try {
                    out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                } catch (FileNotFoundException e) {
                    runOnUiThread(() -> {
                                makeErrorNotification(e.toString());
                            }
                    );
                } catch (IOException e) {
                    runOnUiThread(() -> {
                                makeErrorNotification(e.toString());
                            }
                    );
                }
                runOnUiThread(() -> {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();

                    //resetting recycler
                    photoModels.add(new PhotoModel(file));
                    recycler.addElem(photoModels);
                });
            }
        }.start();
    }

    public void choose(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final List<String> imagesEncodedList;
        if (requestCode == PICK_IMAGES && resultCode == RESULT_OK) {
            if (null == data) {
                makeErrorNotification("You haven't picked Image");
                return;
            }
            try {
            // When an Image is picked
                imagesEncodedList = new ArrayList<String>();
                if (data.getData() != null) {
                    imagesEncodedList.add(data.getData().toString());
                } else { // for many pictures
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
                for (String url : imagesEncodedList) {
                    cropImage(Uri.parse(url));  // start cropping image
                }
                imagesEncodedList.clear();
            } catch (Exception e) {
                makeErrorNotification(e.toString() + "\nMainActivity -> onActivityResult");
            }
        } else
        //RESULT FROM CROPING ACTIVITY
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), result.getUri());
                    recordImage(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission garanted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    public void makeErrorNotification(String not) {
        System.out.println(not);
        Toast.makeText(getApplicationContext(), not, Toast.LENGTH_LONG).show();
    }

    private void cropImage(Uri uri) {
            //CROP REQUEST JAVA
            CropImage.activity(uri)
                    .setActivityMenuIconColor(R.color.textMain)
                    .setAspectRatio(Resources.getSystem().getDisplayMetrics().widthPixels, Resources.getSystem().getDisplayMetrics().heightPixels)
                    .setBackgroundColor(R.color.colorPrimary)
                    .setMultiTouchEnabled(true)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setMultiTouchEnabled(true)
                    .start(this);
        }


}













