package com.conestoga.househunt.Adapters;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.conestoga.househunt.R;
import com.mikhaellopez.circularimageview.CircularImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyHolder extends RecyclerView.ViewHolder {

    TextView itemType, itemLocation, itemPrice;
    ImageView itemImg;
    CircularImageView uploader_image;
    ImageButton ivfav,ivshare,tvchat;
    TextView upload_date,uploader_name;

    public MyHolder(@NonNull View itemView) {
        super(itemView);

        itemType = (TextView) itemView.findViewById(R.id.itemType);
        itemLocation = (TextView) itemView.findViewById(R.id.itemLocation);
        itemPrice = (TextView) itemView.findViewById(R.id.itemPrice);
        itemImg = (ImageView) itemView.findViewById(R.id.itemImg);
        uploader_image = (CircularImageView) itemView.findViewById(R.id.uploader_image);
        ivfav = (ImageButton) itemView.findViewById(R.id.ivfav);
        ivshare = (ImageButton) itemView.findViewById(R.id.ivshare);
        tvchat = (ImageButton) itemView.findViewById(R.id.tvchat);
        upload_date = (TextView) itemView.findViewById(R.id.upload_date);
        uploader_name = (TextView) itemView.findViewById(R.id.uploader_name);


    }


}
