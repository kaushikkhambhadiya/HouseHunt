package com.conestoga.househunt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.conestoga.househunt.Adapters.ChatAdapter;
import com.conestoga.househunt.Model.ChatMessage;
import com.conestoga.househunt.Model.Property;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ChatActivity extends AppCompatActivity {

    RecyclerView chat_recycleview;
    EditText etmsg;
    Button btsend;
    Toolbar toolbar;

    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    Property postdata;

    private DatabaseReference mDatabase;
    private ArrayList<ChatMessage> chat_list;
    private ChatAdapter mAdapter;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        if (intent != null){
            postdata = (Property) intent.getSerializableExtra("postdata");
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference("Chat_Room");

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        chat_recycleview = findViewById(R.id.chat_recycleview);
        etmsg = findViewById(R.id.etmsg);
        btsend = findViewById(R.id.btsend);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Chat with " + postdata.getUploader_name());

        mDatabase.child(user.getUid()).child(postdata.getUserid()).child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    chat_list = new ArrayList<>();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        ChatMessage messageModel = postSnapshot.getValue(ChatMessage.class);
                        chat_list.add(messageModel);
                    }
                    mAdapter = new ChatAdapter(ChatActivity.this, chat_list);
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                    chat_recycleview.setLayoutManager(mLayoutManager);
                    chat_recycleview.setItemAnimator(new DefaultItemAnimator());
                    chat_recycleview.setAdapter(mAdapter);
                    chat_recycleview.scrollToPosition(chat_list.size() - 1);
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        btsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message_text = etmsg.getText().toString();
                if (!message_text.equals("")) {
                    Date today = new Date();
                    SimpleDateFormat format = new SimpleDateFormat("dd MMM hh:mm a");
                    String currentTime = format.format(today);
                    ChatMessage message_model = new ChatMessage(user.getDisplayName(), message_text, currentTime);

                    mDatabase.child(user.getUid()).child(postdata.getUserid()).child("sendto").setValue(postdata.getUploader_name());
                    mDatabase.child(postdata.getUserid()).child(user.getUid()).child("sendto").setValue(user.getDisplayName());
                    if (postdata.getUploader_profile_pic() == null){
                        mDatabase.child(user.getUid()).child(postdata.getUserid()).child("senderimage").setValue("");
                    }else {
                        mDatabase.child(user.getUid()).child(postdata.getUserid()).child("senderimage").setValue(postdata.getUploader_profile_pic());

                    }
                    if (user.getPhotoUrl() == null){
                        mDatabase.child(postdata.getUserid()).child(user.getUid()).child("senderimage").setValue("");
                    }else {
                        mDatabase.child(postdata.getUserid()).child(user.getUid()).child("senderimage").setValue(user.getPhotoUrl().toString());
                    }
                    mDatabase.child(user.getUid()).child(postdata.getUserid()).child("sendid").setValue(postdata.getUserid());
                    mDatabase.child(postdata.getUserid()).child(user.getUid()).child("sendid").setValue(user.getUid());

                    mDatabase.child(user.getUid()).child(postdata.getUserid()).child("messages").push().setValue(message_model);
                    mDatabase.child(postdata.getUserid()).child(user.getUid()).child("messages").push().setValue(message_model);
                    etmsg.setText("");
                } else {
                    Toast.makeText(ChatActivity.this, "Please Write Message before Send", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}