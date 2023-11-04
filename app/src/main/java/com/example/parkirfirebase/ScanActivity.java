package com.example.parkirfirebase;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

import kotlinx.coroutines.scheduling.Task;

public class ScanActivity extends AppCompatActivity {

    private MaterialButton cameraBtn,galeryBtn,scanBtn;
    private ImageView imageId;
    TextView resultTv;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 100;

    private String[] cameraPermissions;
    private String[] storagePermissions;
    private static final String TAG = "MAIN_TAG"

    private Uri imageUri = null;
    private BarcodeScannerOptions barcodeScannerOptions;
    private BarcodeScanner barcodeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        cameraBtn = findViewById(R.id.cameraBtn);
        galeryBtn = findViewById(R.id.galeryBtn);
        imageId = findViewById(R.id.imageId);
        scanBtn = findViewById(R.id.scanBtn);
        resultTv = findViewById(R.id.resultTv);

        cameraPermissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        barcodeScannerOptions = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                        .build();

        barcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions);

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(checkCameraPermission()){
                    pickImageCamera();
                }else {
                    requestCameraPermission();
                }
            }
        });

        galeryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkStoragePermission()){
                    pickImageGalery();
                }else{
                    requestStoragePermission();
                }
            }
        });

        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri == null){
                    Toast.makeText(ScanActivity.this,"pick Image First..",Toast.LENGTH_SHORT).show();
                }else {
                    detectResultFromImage();
                }

            }
        });
    }

    private void detectResultFromImage() {
        try {
            InputImage inputImage= InputImage.fromFilePath(this,imageUri);
            Task<List<Barcode>> barodeResult = barcodeScanner.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            extractBarCodeQRCodeInfo(barcodes);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ScanActivity.this,"Gagal scan"+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
        }catch (Exception e){
            Toast.makeText(this,"Gagal"+e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    private void extractBarCodeQRCodeInfo(List<Barcode> barcodes) {

        for (Barcode barcode : barcodes){
            Rect bounds = barcode.getBoundingBox();
            Point[] corners = barcode.getCornerPoints();

            String rawValue = barcode.getRawValue();
            Log.d(TAG,"extractBarCodeQRCodeInfo: rawValue: "+ rawValue);

            int valueType = barcode.getValueType();

            switch (valueType){
                case Barcode.TYPE_WIFI:{
                    Barcode.WiFi typeWifi = barcode.getWifi();

                    String ssid = ""+typeWifi.getSsid();
                    String password = ""+typeWifi.getPassword();
                    String encryptionType = ""+typeWifi.getEncryptionType();

                    Log.d(TAG, "extractBarCodeQRCodeInfo: ssid"+ssid);
                    Log.d(TAG, "extractBarCodeQRCodeInfo: password"+password);
                    Log.d(TAG, "extractBarCodeQRCodeInfo: encryptionType"+encryptionType);

                    resultTv.setText("TYPE: TYPE_WIFI\nssid"+ssid+"\npassword:"+password+"\nencryptionType"+encryptionType+"\nraw value: "+rawValue);

                }
                break;
                case Barcode.TYPE_URL:{
                    Barcode.UrlBookmark typeUrl = barcode.getUrl();

                    String title = ""+ typeUrl.getTitle();
                    String url = ""+ typeUrl.getUrl();

                    Log.d(TAG, "extractBarCodeQRCodeInfo: TYPE_URL");
                    Log.d(TAG, "extractBarCodeQRCodeInfo: title: "+ title);
                    Log.d(TAG, "extractBarCodeQRCodeInfo: url: "+ url);

                    resultTv.setText("TYPE: TYPE_URL\ntitle"+title+"\nurl:"+url+"\nraw value: "+rawValue);
                }
                break;
                case Barcode.TYPE_EMAIL:{
                    Barcode.Email typeEmail = barcode.getEmail();

                    String address = ""+ typeEmail.getAddress();
                    String body = ""+ typeEmail.getBody();
                    String subject = ""+ typeEmail.getSubject();

                    Log.d(TAG, "extractBarCodeQRCodeInfo: TYPE_EMAIL");
                    Log.d(TAG, "extractBarCodeQRCodeInfo: address: " + address);
                    Log.d(TAG, "extractBarCodeQRCodeInfo: body: "+body);
                    Log.d(TAG, "extractBarCodeQRCodeInfo: subject: " + subject);

                    resultTv.setText("TYPE: TYPE_EMAIL\naddress"+address+"\nbody:"+body+"\nsubject: " +subject+"\nraw value: "+rawValue);
                }
                break;
                case Barcode.TYPE_CONTACT_INFO:{

                    Barcode.ContactInfo typeContact = barcode.getContactInfo();

                    String title = ""+ typeContact.getTitle();
                    String organizer = ""+ typeContact.getOrganization();
                    String name = ""+ typeContact.getName();
                    String phone = ""+ typeContact.getPhones();

                    Log.d(TAG, "extractBarCodeQRCodeInfo: TYPE_CONTACT_INFO");
                    Log.d(TAG, "extractBarCodeQRCodeInfo: title" + title);
                    Log.d(TAG, "extractBarCodeQRCodeInfo: organizer" + organizer);
                    Log.d(TAG, "extractBarCodeQRCodeInfo: name" + name);
                    Log.d(TAG, "extractBarCodeQRCodeInfo: phone" + phone);


                    resultTv.setText("TYPE: TYPE_CONTACT_INFO\ntitle"+title+"\norganizer:"+organizer+"\nname: " +name+" \nphone: "+phone+  "\nraw value: "+rawValue);

                }
                break;
                default:{
                    resultTv.setText("raw value " + rawValue);
                }
            }
        }
    }


    private void pickImageGalery(){
        Intent intent = new Intent(Intent.ACTION_PICK);

        Intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        imageUri = data.getData();
                        Log.d(TAG,"onActivityResult: imageUri "+imageUri);
                        imageId.setImageURI(imageUri);
                    }
                    else {
                        Toast.makeText(ScanActivity.this,"cancelled",Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );
    private void pickImageCamera(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Sample Title");
        contentValues.put(MediaStore.Images.Media.TITLE,"Sample Image Description");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);

    }
    private final ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();

                        Log.d(TAG,"onActivityResult: imageUri"+ imageUri);
                        imageId.setImageURI(imageUri);
                    }
                    else {
                        Toast.makeText(ScanActivity.this,"Canceled..",Toast/Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );
    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
        return result;
    }

    private void requestStoragePermission(){

        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);
    }
    private boolean checkCameraPermission(){
        boolean resultCamera = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;

        boolean resultStorage = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;

        return  resultCamera && resultStorage;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{

                if (grantResults.length>0){
                    boolean cameraAccepted = grantResults[0]== PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1]== PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && storageAccepted){

                        pickImageCamera();
                    }else {
                        Toast.makeText(this,"Camera & storge permissions are required",Toast.LENGTH_SHORT).show();

                    }
                }

            }
            break;
            case STORAGE_REQUEST_CODE:{

                if (grantResults.length >0 ){

                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (storageAccepted){
                        pickImageGalery();
                    }else {
                        Toast.makeText(this,"Storage permission is reqquired",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }
}