package com.conestoga.househunt;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.conestoga.househunt.Model.Property;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class DisplayActivity extends AppCompatActivity  implements BaseSliderView.OnSliderClickListener{

    Property property;
    TextView mTitle, mLocation, mAvailable, mPrice;
    private SliderLayout slider;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    MaterialRippleLayout lyt_chat;


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        initToolbar();

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        mTitle = findViewById(R.id.title);
        mLocation = findViewById(R.id.location);
        mAvailable = findViewById(R.id.available);
        mPrice = findViewById(R.id.price);
        slider = (SliderLayout)findViewById(R.id.slider);
        lyt_chat = findViewById(R.id.lyt_chat);

        Intent intent = getIntent();
        if (intent!=null){
            property = (Property) intent.getSerializableExtra("property");

            mTitle.setText(property.getType());
            SpannableString location = new SpannableString(property.getLocation());
            location.setSpan(new UnderlineSpan(), 0, location.length(), 0);
            mLocation.setText(location);
            mAvailable.setText(property.getAvailable());
            mPrice.setText(Integer.toString(property.getPrice()));
            getSupportActionBar().setTitle(property.getType());

            HashMap<String,String> url_maps = new HashMap<String, String>();

            for (int i=0 ; i < property.getPropertyimages().size() ; i++){
                url_maps.put(i+1 + "", property.getPropertyimages().get(i));
            }

            for(String name : url_maps.keySet()){
                TextSliderView textSliderView = new TextSliderView(this);
                // initialize a SliderLayout
                textSliderView
                        .description(name)
                        .image(url_maps.get(name))
                        .setScaleType(BaseSliderView.ScaleType.Fit)
                        .setOnSliderClickListener(this);

                //add your extra information
                textSliderView.bundle(new Bundle());
                textSliderView.getBundle()
                        .putString("extra",name);

                slider.addSlider(textSliderView);
            }
            slider.setPresetTransformer(SliderLayout.Transformer.Accordion);
            slider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
            slider.setCustomAnimation(new DescriptionAnimation());
            slider.setDuration(4000);
//            slider.addOnPageChangeListener(this);

            if (user.getUid().equals(property.getUserid())){
                lyt_chat.setVisibility(View.GONE);
            }
        }

        mLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + property.getLocation());
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        startActivity(mapIntent);
                    }
                }, 1000);
            }
        });

        lyt_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(DisplayActivity.this , ChatActivity.class);
                intent1.putExtra("postdata",property);
                startActivity(intent1);
            }
        });

    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onSliderClick(BaseSliderView slider) {
//        Toast.makeText(this,slider.getBundle().get("extra") + "", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        // To prevent a memory leak on rotation, make sure to call stopAutoCycle() on the slider before activity or fragment is destroyed
        slider.stopAutoCycle();
        super.onStop();
    }
}