package com.conestoga.househunt.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.conestoga.househunt.R;

import java.io.File;
import java.util.ArrayList;

public class GalleryAdapter extends BaseAdapter{
    private ArrayList<Uri> users;
    private Context context;

    public GalleryAdapter(ArrayList<Uri> users, Context context) {
        this.users = users;
        this.context = context;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view=layoutInflater.inflate(R.layout.gallery_layout,null);
        ImageView photo,option;
        if(view==null){
            photo=new ImageView(context);
        }
        photo=(ImageView)view.findViewById(R.id.photo);

        Uri file = Uri.fromFile(new File(String.valueOf(users.get(i))));
        Glide.with(context).load(file).into(photo);

        return view;
    }

}
