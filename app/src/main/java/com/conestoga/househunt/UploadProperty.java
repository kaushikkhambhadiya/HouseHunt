package com.conestoga.househunt;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.conestoga.househunt.Model.Property;
import com.conestoga.househunt.utils.Tools;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class UploadProperty extends AppCompatActivity {

    //Variables
    EditText txtType,txtAddress,txtAvailableFor, txtPrice;
    Button btnChooseImage,btnSubmit;
    ImageView imgProperty;
    StorageReference strRef;
    Uri imageURI;
    StorageTask taskStorage;
    DatabaseReference dbRef;
    DatabaseReference dbReflisting;
    Property objProperty;
    ProgressBar progressBar;
    public String imageId;
    ImageView ivback;

    Date currentTime;

    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    public static final int TAKE_PIC_REQUEST_CODE = 0;
    public static final int CHOOSE_PIC_REQUEST_CODE = 1;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_property);

        //Hooks
        txtType = findViewById(R.id.txtType);
        txtAddress = findViewById(R.id.txtAddress);
        txtPrice = findViewById(R.id.txtPrice);
        txtAvailableFor = findViewById(R.id.txtAvailableFor);
        imgProperty = findViewById(R.id.imgProperty);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);
        ivback = findViewById(R.id.ivback);

        ivback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        strRef = FirebaseStorage.getInstance().getReference("Images");
        dbRef = FirebaseDatabase.getInstance().getReference("AllProperty");
        if (user!=null){
            if (user.getEmail()!=null){
                dbReflisting = FirebaseDatabase.getInstance().getReference(user.getUid()).child("MyListing");
            }
        }
        objProperty = new Property();

        btnChooseImage.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadDetails();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void SelectImage() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
        }
        else{
            //show dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Upload or Take a photo");
            builder.setPositiveButton("Upload", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //upload image
                    Intent choosePictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    choosePictureIntent.setType("image/*");
                    startActivityForResult(choosePictureIntent, CHOOSE_PIC_REQUEST_CODE);
                }
            });
            builder.setNegativeButton("Take Photo", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //take photo
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, TAKE_PIC_REQUEST_CODE);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case TAKE_PIC_REQUEST_CODE:
                    if (resultCode == Activity.RESULT_OK && data != null)
                    {
                        Bitmap photo = (Bitmap) data.getExtras().get("data");
                        imageURI = Tools.getImageUri(this,photo);
                        imgProperty.setImageBitmap(photo);
                    }

                    break;
                case CHOOSE_PIC_REQUEST_CODE:
                    if (resultCode == RESULT_OK && data != null) {
                        imageURI = data.getData();
                        try {
                            Bitmap photo = MediaStore.Images.Media.getBitmap(getContentResolver(), imageURI);
                            imgProperty.setImageBitmap(photo);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }

    private void UploadDetails() {
        if (taskStorage != null && taskStorage.isInProgress()){
            progressBar.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Is in progress!", Toast.LENGTH_SHORT).show();
        }
        else {
            progressBar.setVisibility(View.GONE);
            ImageUploader();
        }
    }

    private void ImageUploader() {

        String type = txtType.getText().toString().trim();
        String address = txtAddress.getText().toString().trim();
        String available = txtAvailableFor.getText().toString().trim();
        String price = txtPrice.getText().toString().trim();

        currentTime = Calendar.getInstance().getTime();
        Date newDate = currentTime;
        SimpleDateFormat format = new SimpleDateFormat("MMM dd,yyyy hh:mm a");
        String date = format.format(newDate);
        Log.i("newdateformat",date);

        if (type.isEmpty() || address.isEmpty() || available.isEmpty() || price.isEmpty()){
            Toast.makeText(this,"All Field is Required!",Toast.LENGTH_LONG).show();
        } else if (imageURI == null){
            Toast.makeText(this,"Please Upload Property Picture!",Toast.LENGTH_LONG).show();
        } else {
            imageId = System.currentTimeMillis() + "." + findExtension(imageURI);
            objProperty.setUserid(user.getUid());
            objProperty.setType(type);
            objProperty.setLocation(address);
            objProperty.setAvailable(available);
            objProperty.setPrice(Integer.parseInt(price));
            objProperty.setImageFileName(imageId);
            objProperty.setDateofpost(date);
            objProperty.setUploader_name(user.getDisplayName());
            objProperty.setUploader_profile_pic(String.valueOf(user.getPhotoUrl()));
            objProperty.setUploader_email(user.getEmail());

            StorageReference refStorage = strRef.child(imageId);
            taskStorage = refStorage.putFile(imageURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful());
                    final Uri taskResult = urlTask.getResult();
                    final String downloadUrl = taskResult.toString();

                    objProperty.setImageId(downloadUrl);

                    dbReflisting.push().setValue(objProperty).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressBar.setVisibility(View.VISIBLE);
                            Log.i("Listing","Complete");
                        }
                    });

                    dbRef.push().setValue(objProperty).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressBar.setVisibility(View.VISIBLE);
                            Toast.makeText(UploadProperty.this, "Upload Complete!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(UploadProperty.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            });
        }
    }

    private String findExtension(Uri imgUri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imgUri));
    }
}
