package com.conestoga.househunt.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.conestoga.househunt.ChatActivity;
import com.conestoga.househunt.Model.Property;
import com.conestoga.househunt.Model.SenderDetails;
import com.conestoga.househunt.R;
import com.conestoga.househunt.utils.Tools;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MessagesListAdapter extends RecyclerView.Adapter<MessagesListAdapter.MyViewHolder>{

    private Context mcontex;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    private ArrayList<SenderDetails> chat_message_list;
    private int lastPosition = -1;
    private boolean on_attach = true;

    public MessagesListAdapter(Context mcontex, ArrayList<SenderDetails> chat_message_list) {
        this.chat_message_list = chat_message_list;
        this.mcontex = mcontex;
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
    }

    @NonNull
    @Override
    public MessagesListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_people_chat, parent, false);
            return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessagesListAdapter.MyViewHolder holder, int position) {
        final SenderDetails senderDetails = chat_message_list.get(position);

        holder.tvusername.setText(senderDetails.getSendto());
        Picasso.with(mcontex).load(senderDetails.getSenderimage()).fit().placeholder(R.drawable.ic_person).centerInside().into(holder.image);
        holder.message_text.setText(senderDetails.getLastmessage());
        setAnimation(holder.itemView, position);

        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mcontex, ChatActivity.class);
                Property property = new Property();
                property.setUserid(senderDetails.getSendid());
                property.setUploader_name(senderDetails.getSendto());
                property.setUploader_profile_pic(senderDetails.getSenderimage());
                intent.putExtra("postdata",property);
                mcontex.startActivity(intent);
            }
        });

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                on_attach = false;
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        super.onAttachedToRecyclerView(recyclerView);
    }

    // get the size of the list
    @Override
    public int getItemCount() {
        return chat_message_list == null ? 0 : chat_message_list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView message_text,tvusername;
        ImageView image;
        LinearLayout lyt_parent;

        public MyViewHolder(View view) {
            super(view);
            image = view.findViewById(R.id.image);
            tvusername = (TextView) view.findViewById(R.id.tvusername);
            message_text = (TextView) view.findViewById(R.id.tvmessage);
            lyt_parent = view.findViewById(R.id.lyt_parent);
        }
    }

    private void setAnimation(View view, int position) {
        if (position > lastPosition) {
           Tools.animateRightLeft(view, on_attach ? position : -1);
            lastPosition = position;
        }
    }
}
