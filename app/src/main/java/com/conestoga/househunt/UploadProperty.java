package com.conestoga.househunt;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.conestoga.househunt.Adapters.GalleryAdapter;
import com.conestoga.househunt.Model.Property;
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
import com.gun0912.tedpicker.Config;
import com.gun0912.tedpicker.ImagePickerActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class UploadProperty extends AppCompatActivity {

    //Variables
    EditText txtType,txtAddress,txtAvailableFor, txtPrice;
    Button btnChooseImage,btnSubmit;
    StorageReference strRef;
    ArrayList<Uri> image_uris;
    StorageTask taskStorage;
    DatabaseReference dbRef;
    DatabaseReference dbReflisting;
    Property objProperty;
    ProgressBar progressBar;
    public String imageId;
    ImageView ivback;
    LinearLayout llpropertyimages;

    Date currentTime;

    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    //voice input request codes
    private static final int REQUEST_CODE_TYPE = 11;
    private static final int REQUEST_CODE_AVAILABLEFOR = 12;
    private static final int REQUEST_CODE_ADDRESS = 13;
    private static final int REQUEST_CODE_PRICE = 14;

    private static final int INTENT_REQUEST_GET_IMAGES = 15;

    private GridView gallery;
    private GalleryAdapter galleryAdapter;


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
        btnChooseImage = findViewById(R.id.btnChooseImage);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);
        ivback = findViewById(R.id.ivback);
        llpropertyimages = findViewById(R.id.llpropertyimages);
        gallery = findViewById(R.id.gallery);

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
            //upload image
            Config config = new Config();
//            config.setCameraHeight(ViewGroup.LayoutParams.MATCH_PARENT);
            config.setSelectionMin(1);
            config.setSelectionLimit(10);
            config.setSelectedCloseImage(R.drawable.ic_check);
            config.setSelectedBottomColor(R.color.colorPrimaryDark);

            ImagePickerActivity.setConfig(config);

            Intent intent  = new Intent(UploadProperty.this, ImagePickerActivity.class);
            startActivityForResult(intent,INTENT_REQUEST_GET_IMAGES);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ArrayList<String> matches = new ArrayList<>();
        if (data != null){
            matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        }

        if(resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case INTENT_REQUEST_GET_IMAGES:
                    image_uris = data.getParcelableArrayListExtra(ImagePickerActivity.EXTRA_IMAGE_URIS);
                    galleryAdapter=new GalleryAdapter(image_uris,this);
                    gallery.setAdapter(galleryAdapter);
                    setGridViewHeightBasedOnChildren( gallery , 3);
                    break;
                case REQUEST_CODE_TYPE:
                    txtType.setText(matches.get(0));
                    break;
                case REQUEST_CODE_AVAILABLEFOR:
                    txtAvailableFor.setText(matches.get(0));
                    break;
                case REQUEST_CODE_ADDRESS:
                    txtAddress.setText(matches.get(0));
                    break;
                case REQUEST_CODE_PRICE:
                    txtPrice.setText(matches.get(0));
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
            progressBar.setVisibility(View.VISIBLE);
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
        } else if (image_uris == null || image_uris.size() == 0){
            Toast.makeText(this,"Please Upload Property Picture!",Toast.LENGTH_LONG).show();
        } else {

            objProperty.setUserid(user.getUid());
            objProperty.setType(type);
            objProperty.setLocation(address);
            objProperty.setAvailable(available);
            objProperty.setPrice(Integer.parseInt(price));
            objProperty.setDateofpost(date);
            objProperty.setUploader_name(user.getDisplayName());
            objProperty.setUploader_profile_pic(String.valueOf(user.getPhotoUrl()));
            objProperty.setUploader_email(user.getEmail());


            final ArrayList<String> image_list = new ArrayList<>();
            for (int i =0 ; i < image_uris.size(); i++) {
                Uri uri = Uri.parse("file://"+image_uris.get(i));
                final StorageReference ref = strRef.child(uri.getLastPathSegment());
                ref.putFile(uri)
                        .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                final Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                                while (!urlTask.isSuccessful());
                                final Uri taskResult = urlTask.getResult();
                                final String downloadUrl = taskResult.toString();
                                image_list.add(downloadUrl);

                                if (image_list.size() == image_uris.size()) {

                                    objProperty.setPropertyimages(image_list);

                                    dbReflisting.push().setValue(objProperty).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            progressBar.setVisibility(View.VISIBLE);
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    });

                                    dbRef.push().setValue(objProperty).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(UploadProperty.this, "Upload Complete!", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(UploadProperty.this, MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });

                                }
                            }
                        });
            }
        }
    }

    public void takevoiceinput(final View view) {

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                int Request_code = 1234;
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (txtType.getRight() - txtType.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        if (view.getId() == R.id.txtType) {
                            // FullName action
                            Request_code = REQUEST_CODE_TYPE;
                        } else if (view.getId() == R.id.txtAvailableFor) {
                            //Email action
                            Request_code = REQUEST_CODE_AVAILABLEFOR;
                        } else if (view.getId() == R.id.txtAddress) {
                            //Phone action
                            Request_code = REQUEST_CODE_ADDRESS;
                        }else if (view.getId() == R.id.txtPrice) {
                            //Phone action
                            Request_code = REQUEST_CODE_PRICE;
                        }
                        startVoiceRecognitionActivity(Request_code);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    /**
     * Fire an intent to start the voice recognition activity.
     */
    private void startVoiceRecognitionActivity(int request_code)
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice recognition Demo...");
        startActivityForResult(intent, request_code);
    }

    public void setGridViewHeightBasedOnChildren(GridView gridView, int columns) {
        ListAdapter listAdapter = gridView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int items = listAdapter.getCount();
        int rows = 0;

        View listItem = listAdapter.getView(0, null, gridView);
        listItem.measure(0, 0);
        totalHeight = listItem.getMeasuredHeight();

        float x = 1;
        if( items > columns ){
            x = items/columns;
            rows = (int) (x + 1);
            totalHeight *= rows;
        }

        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = totalHeight;
        gridView.setLayoutParams(params);

    }
}
