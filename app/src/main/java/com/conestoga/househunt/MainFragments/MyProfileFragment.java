package com.conestoga.househunt.MainFragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.conestoga.househunt.Adapters.SectionsPagerAdapter;
import com.conestoga.househunt.Fragments.FragmentTabsFavorite;
import com.conestoga.househunt.Fragments.FragmentTabsListing;
import com.conestoga.househunt.R;
import com.conestoga.househunt.utils.Tools;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.IOException;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;


public class MyProfileFragment extends Fragment {

    public static final int TAKE_PIC_REQUEST_CODE = 0;
    public static final int CHOOSE_PIC_REQUEST_CODE = 1;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    ViewPager view_pager;
    SectionsPagerAdapter viewPagerAdapter;
    TabLayout tab_layout;
    TextView tvname, tvemail;
    LinearLayout mChangeProfilePic;
    CircularImageView mPreviewImageView;

    FirebaseUser user;
    public String imageId;
    FirebaseAuth firebaseAuth;
    StorageReference strRef;
    StorageTask taskStorage;

    public MyProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View main_view = inflater.inflate(R.layout.fragment_my_profile, container, false);

        view_pager = (ViewPager) main_view.findViewById(R.id.view_pager);
        tab_layout = (TabLayout) main_view.findViewById(R.id.tab_layout);
        tvname = main_view.findViewById(R.id.tvname);
        tvemail = main_view.findViewById(R.id.tvemail);
        mChangeProfilePic = main_view.findViewById(R.id.mChangeProfilePic);
        mPreviewImageView = main_view.findViewById(R.id.mPreviewImageView);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            tvname.setText(user.getDisplayName());
            tvemail.setText(user.getEmail());
            if (user.getPhotoUrl() != null){
                Glide.with(getContext())
                        .load(user.getPhotoUrl())
                        .into(mPreviewImageView);
            }
        }

        //Change profile imagep
        //set onlClick to TextView
        mChangeProfilePic.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (Objects.requireNonNull(getActivity()).checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                    {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                    }
                    else{
                        //show dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
        });//End change profile image onClick Listener

        setupViewPager(view_pager);

        tab_layout.setupWithViewPager(view_pager);

        tab_layout.getTabAt(0).setIcon(R.drawable.ic_listing);
        tab_layout.getTabAt(1).setIcon(R.drawable.ic_favorites);

        // set icon color pre-selected
        tab_layout.getTabAt(0).getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        tab_layout.getTabAt(1).getIcon().setColorFilter(getResources().getColor(R.color.grey_20), PorterDuff.Mode.SRC_IN);

        tab_layout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(getResources().getColor(R.color.grey_20), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return main_view;
    }

    private void setupViewPager(ViewPager viewPager) {

        viewPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        viewPagerAdapter.addFragment(FragmentTabsListing.newInstance(), "My Listing");    // index 0
        viewPagerAdapter.addFragment(FragmentTabsFavorite.newInstance(), "Favorites");    // index 1
        viewPager.setAdapter(viewPagerAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case TAKE_PIC_REQUEST_CODE:
                    if (resultCode == Activity.RESULT_OK && data != null)
                    {
                        Bitmap photo = (Bitmap) data.getExtras().get("data");
                        mPreviewImageView.setImageBitmap(photo);
                        UploadImageToFirebase(photo);
                    }

                    break;
                case CHOOSE_PIC_REQUEST_CODE:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri imageUri = data.getData();
                        try {
                            Bitmap photo = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                            mPreviewImageView.setImageBitmap(photo);
                            UploadImageToFirebase(photo);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }

    private void UploadImageToFirebase(Bitmap photo) {
        Uri image_uri = Tools.getImageUri(getActivity(),photo);
        user = FirebaseAuth.getInstance().getCurrentUser();

        strRef = FirebaseStorage.getInstance().getReference("ProfileImages");
        imageId = System.currentTimeMillis() + "." + findExtension(image_uri);
        StorageReference refStorage = strRef.child(imageId);

        taskStorage = refStorage.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                final Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!urlTask.isSuccessful());
                final Uri taskResult = urlTask.getResult();
                final String downloadUrl = taskResult.toString();

                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setPhotoUri(Uri.parse(downloadUrl))
                        .build();

                user.updateProfile(profileUpdates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User profile updated.");
                                }
                            }
                        });
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(getContext(), "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, TAKE_PIC_REQUEST_CODE);
            }
            else
            {
                Toast.makeText(getContext(), "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private String findExtension(Uri imgUri) {
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imgUri));
    }

}