package com.conestoga.househunt.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.conestoga.househunt.BuildConfig;
import com.conestoga.househunt.DisplayActivity;
import com.conestoga.househunt.Interfaces.ItemClickListner;
import com.conestoga.househunt.Model.Property;
import com.conestoga.househunt.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class MyAdapter extends RecyclerView.Adapter<MyHolder> {

    private Context mContext;
    private List<Property> mUploads;
    StorageReference storageReference;
    DatabaseReference dbRef;
    FirebaseUser user;
    FirebaseAuth firebaseAuth;

    public MyAdapter(Context context, List<Property> uploads) {
        mContext = context;
        mUploads = uploads;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false);
        return new MyHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, int position) {

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference(user.getUid()).child("FavListing");

        final Property estateCurrent = mUploads.get(position);
        holder.itemType.setText(estateCurrent.getType());
        holder.itemLocation.setText(estateCurrent.getLocation());
        holder.itemPrice.setText(Integer.toString(estateCurrent.getPrice()));
        holder.uploader_name.setText(estateCurrent.getUploader_name());
        holder.upload_date.setText(estateCurrent.getDateofpost());

        Glide.with(mContext).load(estateCurrent.getPropertyimages().get(0)).into(holder.itemImg);
//        Picasso.get().load(estateCurrent.getPropertyimages().get(0)).fit().centerInside().into(holder.itemImg);

        if (!estateCurrent.getUploader_profile_pic().equals("null")){
//            Picasso.get().load(estateCurrent.getUploader_profile_pic()).fit().centerInside().into(holder.uploader_image);
            Glide.with(mContext).load(estateCurrent.getUploader_profile_pic()).into(holder.uploader_image);
        }

          holder.setItemClickListner(new ItemClickListner() {
            @Override
            public void onItemClickListner(View v, int position) {
                Intent intent = new Intent(mContext, DisplayActivity.class);
                intent.putExtra("property", estateCurrent);
                mContext.startActivity(intent);
            }
        });

        dbRef.orderByChild("dateofpost").equalTo(estateCurrent.getDateofpost())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            //Already exists in Database
                            holder.ivfav.setColorFilter(ContextCompat.getColor(mContext, R.color.red), android.graphics.PorterDuff.Mode.MULTIPLY);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        holder.ivshare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,
                        "Check this Housing Post: \n " + estateCurrent.getType() + "\n Address: " + estateCurrent.getLocation() + "\n Price: $" + estateCurrent.getPrice()  + "\n Download this app at: https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
                sendIntent.setType("text/plain");
                mContext.startActivity(sendIntent);
            }
        });


        holder.ivfav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dbRef.orderByChild("dateofpost").equalTo(estateCurrent.getDateofpost())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    //Already exists in Database
                                    holder.ivfav.setColorFilter(ContextCompat.getColor(mContext, R.color.grey_60), android.graphics.PorterDuff.Mode.MULTIPLY);

                                    for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                        appleSnapshot.getRef().removeValue();
                                    }

                                } else {
                                    // doesn't exists.
                                    dbRef.push().setValue(estateCurrent).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Log.i("Listing","Complete");
                                            holder.ivfav.setColorFilter(ContextCompat.getColor(mContext, R.color.red), android.graphics.PorterDuff.Mode.MULTIPLY);

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUploads.size();
    }
}
