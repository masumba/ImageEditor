package com.demo.goon.imageeditor;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.util.UUID;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    Button btnCamera,btnGallery;
    ImageView imageView;
    File file;
    Uri uri;
    Intent camIntent, galIntent,cropIntent;
    final int requestPermissionCode=1;
    DisplayMetrics displayMetrics;
    int width,height;

    public static final String IMAGE_EXTENSION = "jpg";
    public static final int REQUEST_PERMISSION = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCamera = (Button)findViewById(R.id.btnCamera);
        btnGallery = (Button)findViewById(R.id.btn_gallery);
        imageView = (ImageView)findViewById(R.id.imgView);

        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
        if (permissionCheck == PackageManager.PERMISSION_DENIED){
            RequestRuntimePermission();
        }

        /**/
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        }
        /**/

        /*cameraOpen();
        galleryOpen();*/

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,0);*/

                cameraOpen();
            }
        });

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                galleryOpen();
            }
        });
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = (Bitmap)data.getExtras().get("data");
        imageView.setImageBitmap(bitmap);
    }*/

    private void galleryOpen() {
        galIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(galIntent,"Select image File"),2);
    }

    private void cameraOpen() {

        /*strict mode*/
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());


        camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        /*file = new File(Environment.getExternalStorageDirectory(),
                "file"+String.valueOf(System.currentTimeMillis())+".jpg");*/
        file = new File(Environment.getExternalStorageDirectory(),
                "file"+String.valueOf(UUID.randomUUID())+".JPEG");


        //uri = Uri.fromFile(file);
        String authorities = getApplicationContext().getPackageName()+".fileprovider";

        /**/

        Toast.makeText(this,file.toString(),Toast.LENGTH_LONG).show();
        System.out.println("FileLocation="+file);
        /**/

        uri = FileProvider.getUriForFile(this, authorities, file);


        camIntent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
        camIntent.putExtra("return-data",true);

        startActivityForResult(camIntent,0);
    }

    private void RequestRuntimePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.CAMERA)){
            Toast.makeText(this,"Success",Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA}, requestPermissionCode);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK){
            cropImage();
        } else if (requestCode == 2){
            if (data !=null){
                uri = data.getData();
                cropImage();
            }
        } else if (requestCode == 1){
            if (data != null){
                Bundle bundle = data.getExtras();
                Bitmap bitmap = bundle.getParcelable("data");
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    private void cropImage() {
        try{
            cropIntent = new Intent("com.android.camera.action.CROP");
            //cropIntent.setDataAndType(uri,"image/*");

            /**/
            cropIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            /**/

            cropIntent.putExtra("crop","true");
            cropIntent.putExtra("outputX",180);
            cropIntent.putExtra("outputY",180);
            cropIntent.putExtra("aspectX",3);
            cropIntent.putExtra("aspectY",4);
            cropIntent.putExtra("scaleUpIfNeeded",true);
            cropIntent.putExtra("return-data",true);

            /**/
            cropIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            cropIntent.putExtra("noFaceDetection", true);
            /**/

            startActivityForResult(cropIntent,1);

        } catch (ActivityNotFoundException ex){
            ex.getStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case requestPermissionCode:
            {if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this,"Permission Denied",Toast.LENGTH_LONG).show();}
        }
    }
}
