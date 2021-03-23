package com.conestoga.househunt.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.conestoga.househunt.Model.ChatMessage;
import com.conestoga.househunt.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {

    private Context mcontex;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    private ArrayList<ChatMessage> chat_message_list;
    private static final int TYPE_ONE = 1;
    private static final int TYPE_TWO = 2;

    public ChatAdapter(Context mcontex, ArrayList<ChatMessage> chat_message_list) {
        this.chat_message_list = chat_message_list;
        this.mcontex = mcontex;
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
    }

    // determine which layout to use for the row
    @Override
    public int getItemViewType(int position) {
        ChatMessage item = chat_message_list.get(position);
        if (item.getName().equals(user.getDisplayName())) {
            return TYPE_ONE;
        } else {
            return TYPE_TWO;
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ONE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_message_item, parent, false);
            return new MyViewHolder(view);
        } else if (viewType == TYPE_TWO) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);
            return new MyViewHolder(view);
        } else {
            throw new RuntimeException("The type has to be ONE or TWO");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ChatMessage message_model = chat_message_list.get(position);
        holder.message_text.setText(message_model.getMessage());
        holder.tvusername.setText(message_model.getName());
        holder.tvtime.setText(message_model.getTime());

    }

    // get the size of the list
    @Override
    public int getItemCount() {
        return chat_message_list == null ? 0 : chat_message_list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView message_text,tvusername,tvtime;

        public MyViewHolder(View view) {
            super(view);
            message_text = (TextView) view.findViewById(R.id.tvmessage);
            tvusername = (TextView) view.findViewById(R.id.tvusername);
            tvtime = (TextView) view.findViewById(R.id.tvtime);
        }
    }

}
