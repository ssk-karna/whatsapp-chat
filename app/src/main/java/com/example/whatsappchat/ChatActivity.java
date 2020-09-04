package com.example.whatsappchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.whatsappchat.Chat.MessageAdapter;
import com.example.whatsappchat.Chat.MessageObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView mChat;
    private RecyclerView.Adapter mChatAdapter;
    private RecyclerView.LayoutManager mChatLayoutManager;
    private ArrayList<MessageObject> messageList;
    String chatID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Button mSend = findViewById(R.id.send);

        chatID = getIntent().getExtras().getString("chatID");

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        initializeRecyclerView();;
    }

    private void sendMessage() {
        EditText mMessage = findViewById(R.id.message);

        if(!mMessage.getText().toString().isEmpty()){
            DatabaseReference newMessageDb = FirebaseDatabase.getInstance().getReference().child("chat").child(chatID).push();

            Map newMessageMap = new HashMap<>();
            newMessageMap.put("text",mMessage.getText().toString());
            newMessageMap.put("creator", FirebaseAuth.getInstance().getUid());

            newMessageDb.updateChildren(newMessageMap);
        }
        mMessage.setText(null);
    }

    private void initializeRecyclerView() {
        messageList = new ArrayList<>();
        mChat = findViewById(R.id.messageList);
        mChat.setNestedScrollingEnabled(false);
        mChat.setHasFixedSize(false);
        mChatLayoutManager = new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL,
                false);
        mChat.setLayoutManager(mChatLayoutManager);
        mChatAdapter = new MessageAdapter(messageList);
        mChat.setAdapter(mChatAdapter);
        mChatAdapter.notifyDataSetChanged();

    }
}