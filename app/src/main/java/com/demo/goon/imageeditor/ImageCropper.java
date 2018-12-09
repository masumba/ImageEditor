package com.demo.goon.imageeditor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageCropper extends AppCompatActivity {

    private static ImageCropper _selfInstance = null;
    private static final int REQUEST_PERMISSION_ACCEPTED = 555;
    private static final int REQUEST_IMAGE = 100;

    Intent cameraIntent;
    File cameraFile;
    String cameraImageFilePath;
    Uri cameraUri;

    public ImageCropper() {
    }

    public static ImageCropper getInstance(){
        if (_selfInstance == null){
            _selfInstance = new ImageCropper();
        }
        return _selfInstance;
    }

    /*startscroppermethods*/
    public void startCropperTask(){
        try {
            checkAppPermission();
            startCamera();
        } catch (Exception e){
            Toast.makeText(this,"Something Went Wrong",Toast.LENGTH_SHORT).show();
            checkAppPermission();
        }
    }
    /*Checks Device Permissions*/
    public void checkAppPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                requestPermissions(new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                },555);
            } catch (Exception e){
                Toast.makeText(this,"Something Went Wrong",Toast.LENGTH_SHORT).show();
            }

        } else {
            checkAppPermission();
        }
    }

    /*Permission State Results Response*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != REQUEST_PERMISSION_ACCEPTED
                && grantResults[0] != PackageManager.PERMISSION_GRANTED){
            checkAppPermission();
        }
    }

    /*Starts Camera*/
    public void startCamera(){
        cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION,true);
        if (cameraIntent.resolveActivity(getPackageManager()) != null){
            File pictureFile = null;
            try {
                pictureFile = createImage();
            } catch (IOException ex){
                Toast.makeText(this,
                        "Photo File can't be Created, Please Try Again",
                        Toast.LENGTH_SHORT).show();

                return;
            }
            if (pictureFile != null){
                cameraUri = FileProvider.getUriForFile(this,getPackageName()+".provider",pictureFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
                cameraIntent.putExtra("return-data",true);
                startActivityForResult(cameraIntent,REQUEST_IMAGE);
            }
        }
    }

    /*Creates Image File and Saves It in Location*/
    private File createImage() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_"+timeStamp+"_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg",storageDir);
        cameraImageFilePath = image.getAbsolutePath();

        return image;
    }

    /*Starts Cropping Tool (Needs Uri To File Being Cropped)*/
    private void startCropImageActivity(Uri imageUri){
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .start(this);
    }

    /*Processes The Intent Made Requests*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        /*Request Code Processors*/
        if (requestCode == REQUEST_IMAGE){/*Result From Camera*/
            cameraFile = new File(cameraImageFilePath);
            if (cameraFile.exists()){
                startCropImageActivity(cameraUri);
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){/*Result From Croping Activity*/
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), result.getUri());
                    storeImage(bitmap);
                    //code to send it to an image view can be added here;
                } catch (IOException e){
                    e.printStackTrace();
                    Toast.makeText(this,"An Error Occured When Saving Cropped File",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /*Saves Cropped File*/
    private void storeImage(Bitmap bitmap) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null){
            Toast.makeText(this,"Error Creating Media File",Toast.LENGTH_SHORT);
            return;
        }
        try{
            FileOutputStream fileOutputStream = new FileOutputStream(pictureFile);
            bitmap.compress(Bitmap.CompressFormat.PNG,90,fileOutputStream);
            fileOutputStream.close();
        } catch (FileNotFoundException e){
            Toast.makeText(this,"File not Found",Toast.LENGTH_SHORT).show();
            System.out.println("File not Found: "+ e.getMessage());
        } catch (IOException e){
            Toast.makeText(this,"Error Accessing File",Toast.LENGTH_SHORT).show();
            System.out.println("Error Accessing File: "+e.getMessage());
        }
    }

    /*Creates File That Is to Be Saved*/
    private File getOutputMediaFile() {
        String pathname = "/Android/data/"+getApplicationContext().getPackageName()+"/Files";
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory() + pathname);

        /*Creates Directory if it does not Exist*/
        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                return null;
            }
        }
        /*Sets File Name*/
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        String mImageName = "MI_"+ timeStamp +".jpg";/*Sets Image file name and format*/
        File mediaFile = new File(mediaStorageDir.getPath()+File.separator+mImageName);
        return mediaFile;
    }
}
