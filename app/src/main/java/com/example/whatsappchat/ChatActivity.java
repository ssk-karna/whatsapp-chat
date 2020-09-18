package com.example.whatsappchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.whatsappchat.Chat.MediaAdapter;
import com.example.whatsappchat.Chat.MessageAdapter;
import com.example.whatsappchat.Chat.MessageObject;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView mChat,mMedia;
    private RecyclerView.Adapter mChatAdapter,mMediaAdapter;
    private RecyclerView.LayoutManager mChatLayoutManager,mMediaLayoutManager;

    private ArrayList<MessageObject> messageList;

    String chatID;

    DatabaseReference mChatDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Button mSend = findViewById(R.id.send);
        Button mAddMedia = findViewById(R.id.addMedia);

        chatID = getIntent().getExtras().getString("chatID");

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        mAddMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        mChatDb = FirebaseDatabase.getInstance().getReference().child("chat").child(chatID);

        initializeMessage();
        initializeMedia();
        getChatMessages();
    }



    private void getChatMessages() {

        mChatDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                if(snapshot.exists()){
                    String text = "";
                    String creatorID = "";

                    if(snapshot.child("text").getValue() != null)
                        text = snapshot.child("text").getValue().toString();
                    if(snapshot.child("creator").getValue() != null)
                        creatorID = snapshot.child("creator").getValue().toString();

                    MessageObject mMessage = new MessageObject(snapshot.getKey(),creatorID,text);
                    messageList.add(mMessage);
                    mChatLayoutManager.scrollToPosition(messageList.size()-1);
                    mChatAdapter.notifyDataSetChanged();

                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    ArrayList<String> mediaIdList = new ArrayList<>();
    int totalMediaUploaded = 0;
    EditText mMessage;

    private void sendMessage() {
        mMessage = findViewById(R.id.message);

            String messageId = mChatDb.push().getKey();
            final DatabaseReference newMessageDb = mChatDb.child(messageId);

           final Map newMessageMap = new HashMap<>();
            newMessageMap.put("creator", FirebaseAuth.getInstance().getUid());

            if(!mMessage.getText().toString().isEmpty())
                newMessageMap.put("text", mMessage.getText().toString());


            if(!mediaUriList.isEmpty()){

                for (String mediaUri : mediaUriList){

                    String mediaId = newMessageDb.child("media").push().getKey();
                    mediaIdList.add(mediaId);
                    final StorageReference filepath = FirebaseStorage.getInstance().getReference().child("chat").child(chatID).child(messageId).child(mediaId);

                    UploadTask uploadTask = filepath.putFile(Uri.parse(mediaUri));

                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    newMessageMap.put("/media/"+mediaIdList.get(totalMediaUploaded)+"/",uri.toString());

                                    totalMediaUploaded++;

                                    if(totalMediaUploaded == mediaUriList.size()){
                                        updateDatabaseWithNewMessage(newMessageDb,newMessageMap);

                                    }

                                }
                            });
                        }
                    });

                }

            }else{
                if(!mMessage.getText().toString().isEmpty())
                    updateDatabaseWithNewMessage(newMessageDb,newMessageMap);
            }
    }

    private void updateDatabaseWithNewMessage(DatabaseReference newMessageDb, Map newMessageMap) {

        newMessageDb.updateChildren(newMessageMap);
        mMessage.setText(null);
        mediaIdList.clear();
        mediaUriList.clear();
        mMediaAdapter.notifyDataSetChanged();
    }

    private void initializeMessage() {
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
    int PICK_IMAGE_INTENT = 1;
    ArrayList <String> mediaUriList = new ArrayList<>();

    private void initializeMedia() {
        mediaUriList = new ArrayList<>();
        mMedia = findViewById(R.id.mediaList);
        mMedia.setNestedScrollingEnabled(false);
        mMedia.setHasFixedSize(false);
        mMediaLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        mMedia.setLayoutManager(mMediaLayoutManager);
        mMediaAdapter = new MediaAdapter(getApplicationContext(),mediaUriList);
        mMedia.setAdapter(mMediaAdapter);
        mMediaAdapter.notifyDataSetChanged();
    }
    private void openGallery() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Picture(s)"), PICK_IMAGE_INTENT);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == PICK_IMAGE_INTENT){
                if(data.getClipData() == null){
                    mediaUriList.add(data.getData().toString());
                }
                else{
                    for(int i=0; i<data.getClipData().getItemCount(); i++){
                         mediaUriList.add(data.getClipData().getItemAt(i).getUri().toString());

                    }
                }

                mMediaAdapter.notifyDataSetChanged();

            }
        }


    }
}