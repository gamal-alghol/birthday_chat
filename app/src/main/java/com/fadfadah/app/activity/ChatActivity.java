package com.fadfadah.app.activity;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fadfadah.app.R;
import com.fadfadah.app.adapter.MessangerAdapter;
import com.fadfadah.app.callback.DataBaseTablesConstants;
import com.fadfadah.app.helper.Constant;
import com.fadfadah.app.helper.NotificationHelper;
import com.fadfadah.app.helper.SharedPereferenceClass;
import com.fadfadah.app.models.SingleMessageObject;
import com.fadfadah.app.models.UsersModel;
import com.fadfadah.app.services.MyFirebaseMessagingService;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.fadfadah.app.helper.Helper.getCurrentTimeMilliSecond;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatActivity extends AppCompatActivity {

    @BindView(R.id.reyclerview_message_list)
    RecyclerView reyclerviewMessageList;
    @BindView(R.id.edittext_chatbox)
    EditText edittextChatbox;
    @BindView(R.id.send_img)
    ImageView sendImg;
    @BindView(R.id.add_photo)
    ImageView add_photo;
    @BindView(R.id.record_img)
    ImageView record_img;
    private String userKey = null, reciverName;
    private MessangerAdapter messangerAdapter;
    private List<SingleMessageObject> chatList = new ArrayList<>();

    public static final String USER_NAME = "username";

    public static final String USER_IMAGE = "user image";
    public static final String USER_TOKEN = "user token";
    private static final String TAG = "ChatActivity";
    private String chatKey = "", image = "", myKey = "";
    private String userToken = "";
    @BindView(R.id.image_thumb)
    ImageView image_thumb;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private ChildEventListener messageListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            try {
                SingleMessageObject message = dataSnapshot.getValue(SingleMessageObject.class);
                chatList.add(message);
                messangerAdapter.notifyDataSetChanged();
                reyclerviewMessageList.scrollToPosition(chatList.size() - 1);
                loading = true;
                checkLastMessage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    private DatabaseReference userRef;
    private Uri imageUri = null;
    private String filePath = "";
    private CountDownTimer countTime;

    public ChatActivity() {
        // Required empty public constructor
    }

    @OnClick(R.id.add_photo)
    void addPhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_pic)), 214);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 214) {
            if (resultCode == RESULT_OK) {
                imageUri = data.getData();
                image_thumb.setImageURI(imageUri);
                image_thumb.animate().translationY(0f);
                sendImg.setVisibility(View.VISIBLE);
                record_img.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void uploadRecord(String path) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        File file = new File(path);
        StorageReference riversRef = storageRef.child("record/" + Uri.fromFile(file).getLastPathSegment());
        UploadTask uploadTask = riversRef.putFile(Uri.fromFile(file));
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    task.getException().printStackTrace();
                }
                // Continue with the task to get the download URL
                return riversRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    sendMessages("", null, task.getResult().toString());
                } else {
                    Toast.makeText(ChatActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    // Handle failures
                    // ...

                }
            }
        });
    }

    private void uploadImage(String text) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        StorageReference riversRef = storageRef.child("messages_img/" + imageUri.getLastPathSegment());
        UploadTask uploadTask = riversRef.putFile(imageUri);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    task.getException().printStackTrace();
                }
                // Continue with the task to get the download URL
                return riversRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                imageUri = null;
                if (task.isSuccessful()) {
                    sendMessages(text, task.getResult().toString(), null);

                } else {
                    Toast.makeText(ChatActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    // Handle failures
                    // ...

                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyFirebaseMessagingService.chatForUserId = userKey;
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyFirebaseMessagingService.chatForUserId = "";
    }

    SharedPereferenceClass sharedPereferenceClass = new SharedPereferenceClass();

    private void loadUser(String userKey) {
        Log.d(TAG, "loadUser: load  " + userKey);
        if (userKey == null)
            return;
        if (userRef == null)
            userRef = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.USERS);
        userRef.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                try {
                    UsersModel usersModel = dataSnapshot.getValue(UsersModel.class);
                    if (usersModel != null) {

                        userToken = (usersModel.getUserToken());
                        reciverName = (usersModel.getFirstName());
                        image = usersModel.getImage();
                        messangerAdapter = new MessangerAdapter(ChatActivity.this, chatList, image, reyclerviewMessageList);
                        reyclerviewMessageList.setAdapter(messangerAdapter);
                        setTitle(reciverName);
                        checkStatus();

                        onStart();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "onDataChange: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void zoomIn() {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(record_img,
                "scaleX", 3f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(record_img,
                "scaleY", 3f);
        scaleDownX.setDuration(1000);
        scaleDownY.setDuration(1000);
        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.play(scaleDownX).with(scaleDownY);
        scaleDown.start();
    }

    private void zoomOut() {
        ObjectAnimator scaleDownX2 = ObjectAnimator.ofFloat(
                record_img, "scaleX", 1f);
        ObjectAnimator scaleDownY2 = ObjectAnimator.ofFloat(
                record_img, "scaleY", 1f);
        scaleDownX2.setDuration(1000);
        scaleDownY2.setDuration(1000);
        AnimatorSet scaleDown2 = new AnimatorSet();
        scaleDown2.play(scaleDownX2).with(scaleDownY2);
        scaleDown2.start();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectLanguage(sharedPereferenceClass.getLng(getApplicationContext()));
        setContentView(R.layout.fragment_chat);
        ButterKnife.bind(this);

        myKey = "" + FirebaseAuth.getInstance().getUid();


        userKey = getIntent().getStringExtra(Constant.USER_KEY);
        chatKey = getIntent().getStringExtra(Constant.CHAT_KEY);
        if (userKey.isEmpty() || chatKey.isEmpty()) {
            Toast.makeText(this, "not available now", Toast.LENGTH_SHORT).show();
            finish();
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        reyclerviewMessageList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false);
        reyclerviewMessageList.setLayoutManager(linearLayoutManager);
        reyclerviewMessageList.setItemViewCacheSize(50);

        if (getIntent().hasExtra(USER_NAME) && getIntent().hasExtra(USER_IMAGE) && getIntent().hasExtra(USER_TOKEN)
                && !getIntent().getStringExtra(USER_NAME).toLowerCase().isEmpty() &&
                !getIntent().getStringExtra(USER_IMAGE).toLowerCase().isEmpty() &&
                !getIntent().getStringExtra(USER_TOKEN).toLowerCase().isEmpty()) {
            Log.d(TAG, "onCreate: can get data");
            reciverName = getIntent().getStringExtra(USER_NAME);

            image = getIntent().getStringExtra(USER_IMAGE);
            userToken = getIntent().getStringExtra(USER_TOKEN);
            setTitle(reciverName);
            messangerAdapter = new MessangerAdapter(ChatActivity.this, chatList, image, reyclerviewMessageList);
            reyclerviewMessageList.setAdapter(messangerAdapter);

        } else {
            Log.d(TAG, "onCreate: must load from notificaiton ");
            loadUser(userKey);
        }
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddFriendActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(Constant.USER_KEY, userKey);
                startActivity(intent);
            }
        });


        edittextChatbox.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    {
                        sendImg.performClick();
                    }
                    return true;
                }
                return false;
            }
        });

        edittextChatbox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (edittextChatbox.getText().toString().trim().isEmpty() && imageUri == null) {
                    record_img.setVisibility(View.VISIBLE);
                    sendImg.setVisibility(View.INVISIBLE);
                } else {
                    record_img.setVisibility(View.INVISIBLE);
                    sendImg.setVisibility(View.VISIBLE);
                }
            }
        });
//        AudioRecorder audioRecorder = new AudioRecorder();
        record_img.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == 0) {
                    zoomIn();
                    record_img.performLongClick();
                } else if (event.getAction() == 1) {
zoomOut();
                    stopRecord();
                }
                return true;
            }
        });
        record_img.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                try {
                    if (allowedRecord())
                        startRecord();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });

    }

    private boolean allowedRecord() {
        if (ContextCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            return true;
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.RECORD_AUDIO)) {
                new AlertDialog.Builder(this)
                        .setMessage(getResources().getString(R.string.allow_camera_record))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(getResources().getString(R.string.goto_settings), new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", getPackageName(), null)));
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
                return false;
            } else {
                ActivityCompat
                        .requestPermissions(
                                this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                                123);
                return false;
            }
        }
    }

    MediaRecorder recorder;

    private void startRecord() throws IllegalStateException, IOException {
        // recorder = new MediaRecorder();
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);  //ok so I say audio source is the microphone, is it windows/linux microphone on the emulator?
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        File folder = new File("/sdcard/birthday_chat/");
        boolean success = true;
        if (!folder.exists()) {
            //Toast.makeText(MainActivity.this, "Directory Does Not Exist, Create It", Toast.LENGTH_SHORT).show();
            success = folder.mkdir();
        }
        if (success) {
            //Toast.makeText(MainActivity.this, "Directory Created", Toast.LENGTH_SHORT).show();


            filePath = "/sdcard/birthday_chat/" + System.currentTimeMillis() + ".dt";
            recorder.setOutputFile(filePath);
            recorder.prepare();
            recorder.start();

            if (messangerAdapter != null)
                messangerAdapter.release();

            countTime = new CountDownTimer(60000, 100) {

                public void onTick(long millisUntilFinished) {
                    edittextChatbox.setText(String.valueOf((millisUntilFinished / 1000)));
                }

                public void onFinish() {
                    stopRecord();
                }

            };
            countTime.start();
        } else {
            //Toast.makeText(MainActivity.this, "Failed - Error", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecord() {
        if (countTime != null) {
            countTime.cancel();
            countTime = null;
        }
        if (recorder != null) {
            edittextChatbox.setText("");
            try {
                recorder.stop();
                recorder.release();
                uploadRecord(filePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (this.menu == null) {
            this.menu = menu;
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_chat, menu);
        }
        menu.getItem(0).setVisible(true);
        if (allowed == 4)
            menu.getItem(0).setTitle(getResources().getString(R.string.unblock));
        else if (allowed == 3)
            menu.getItem(0).setVisible(false);
        else if (allowed < 3)
            menu.getItem(0).setTitle(getResources().getString(R.string.block_message));
        super.onCreateOptionsMenu(menu);
        if (allowed == -2)
            checkStatus();
        return true;
    }

    private int allowed = -2;
    private Menu menu;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        else if (item.getItemId() == R.id.menu_block_message) {
            if (allowed == 4)
                unblock();
            else if (allowed != 3)
                block();

        } else if (item.getItemId() == R.id.menu_block_user) {
//            if (allowed == 4)
//                unblock();
//            else if (allowed != 3)
//                block();
            Toast.makeText(this, "not developed yet", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void unblock() {
        if (inboxRef == null)
            inboxRef = FirebaseDatabase.getInstance().getReference().child(DataBaseTablesConstants.INBOX);
        inboxRef.child("" + myKey).child(userKey).child("status").setValue(-1);
        inboxRef.child(userKey).child("" + myKey).child("status").setValue(-1);
    }

    private void block() {
        new AlertDialog.Builder(this)
                .setMessage(getResources().getString(R.string.you_are_blocking_messages))
                .setPositiveButton(getResources().getString(R.string.block), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (inboxRef == null)
                            inboxRef = FirebaseDatabase.getInstance().getReference().child(DataBaseTablesConstants.INBOX);
                        inboxRef.child("" + myKey).child(userKey).child("status").setValue(4);
                        inboxRef.child(userKey).child("" + myKey).child("status").setValue(3);
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }


    private void checkStatus() {
        if (userKey == null || userKey.isEmpty())
            return;
        if (inboxRef == null)
            inboxRef = FirebaseDatabase.getInstance().getReference().child(DataBaseTablesConstants.INBOX);
        inboxRef.child("" + myKey).child(userKey).child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    allowed = dataSnapshot.getValue(Integer.class);
                    if (menu == null)
                        return;
                    if (allowed >= 3) {
                        edittextChatbox.setEnabled(false);
                        edittextChatbox.setText(getResources().getString(R.string.this_use_not_available));

                        sendImg.setEnabled(false);
                        record_img.setEnabled(false);
                        add_photo.setEnabled(false);
                        imageUri = null;
                        image_thumb.animate().translationY(1000);
                        onCreateOptionsMenu(menu);
                    } else if (allowed == 1) {
                        messangerAdapter.updateLastMessage(allowed);
                    } else if (allowed == 2) {
                        messangerAdapter.updateLastMessage(allowed);
                    } else if (allowed == -1) {
                        sendImg.setEnabled(true);
                        edittextChatbox.setEnabled(true);
                        record_img.setEnabled(true);
                        add_photo.setEnabled(true);
                        edittextChatbox.setText("");
                        onCreateOptionsMenu(menu);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @OnClick(R.id.send_img)
    public void onViewClicked() {
        if (edittextChatbox.getText().toString().trim().length() > 0 && imageUri == null) {
            sendMessages(edittextChatbox.getText().toString(), null, null);
            edittextChatbox.setText("");
            sendImg.setVisibility(View.INVISIBLE);
            record_img.setVisibility(View.VISIBLE);
        } else if (imageUri != null) {
            uploadImage(edittextChatbox.getText().toString());
            image_thumb.animate().setDuration(200).translationY(1000);

            edittextChatbox.setText("");
            sendImg.setVisibility(View.INVISIBLE);
            record_img.setVisibility(View.VISIBLE);
        } else edittextChatbox.requestFocus();
    }

    DatabaseReference inboxRef;

    private void sendMessages(String message, String image, String audio) {
        if (inboxRef == null)
            inboxRef = FirebaseDatabase.getInstance().getReference().child(DataBaseTablesConstants.INBOX);

//        sendNotification(userToken, message, SelectedFragment.CHAT.name());
        String body = message;
        if (body.trim().isEmpty() && audio != null && !audio.isEmpty()) {
            body = "🎧";
        } else if (body.trim().isEmpty() && image != null && !image.isEmpty()) {
            body = "🖼";
        }
        NotificationHelper.sendNotification(getApplicationContext(), userToken,
                body, Constant.CHAT_ACTION,
                "", chatKey, myKey);

        String key = inboxRef.push().getKey();

//        String senderID, String text, int status, long timeStamp
        SingleMessageObject singleMessageObject = new SingleMessageObject("" + myKey
                , message, 0, getCurrentTimeMilliSecond(), key, audio, image);
        if (myKey != null && !myKey.isEmpty() && userKey != null && !userKey.isEmpty()) {
            FirebaseDatabase.getInstance().getReference().child(DataBaseTablesConstants.MESSAGE).child(chatKey)
                    .child(key).setValue(singleMessageObject);
            Log.d("TAG", "sendMessages: " + userKey + "   my key " + myKey);
            Log.d(TAG, "sendMessages: set not seen ");

            inboxRef.child("" + myKey).child(userKey).child("lastMessage").setValue(body);
            inboxRef.child(userKey).child("" + myKey).child("lastMessage").setValue(body);
            inboxRef.child("" + myKey).child(userKey).child("timeStamp").setValue(getCurrentTimeMilliSecond());
            inboxRef.child(userKey).child("" + myKey).child("timeStamp").setValue(getCurrentTimeMilliSecond());
//        inboxRef.child(userKey).child("" + myKey).child("status").setValue(0);
            inboxRef.child(myKey).child("" + userKey).child("status").setValue(0);
        } else Log.d(TAG, "sendMessages:  key empty");

    }

    DatabaseReference messageRef;

    @Override
    protected void onStart() {
        super.onStart();
        try {
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(51);
        } catch (Exception e) {
        }
        readMessage();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (messangerAdapter != null)
            messangerAdapter.release();
        if (messageRef != null) {
            messageRef.removeEventListener(messageListener);
            messageRef = null;
        }
    }

    private void readMessage() {

        chatList.clear();
        if (messageRef != null) {
            messageRef.removeEventListener(messageListener);
        }

        if (messageRef == null)
            messageRef = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.MESSAGE)
                    .child(chatKey);
        if (userKey != null && !userKey.isEmpty()) {
            messageRef.limitToLast(50).addChildEventListener(messageListener);
        }

    }

    private void selectLanguage(String lng) {
        Locale locale = new Locale(lng);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    private boolean loading = false;
    private Handler delay = null;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            delay = null;
            loading = false;
            checkLastMessage();
        }
    };

    private void checkLastMessage() {
        if (loading) {
            Log.d(TAG, "checkLastMessage: loading wait ");
            if (delay == null) {
                delay = new Handler();
                delay.postDelayed(runnable, 500);
            }
        } else {
            if (inboxRef == null)
                inboxRef = FirebaseDatabase.getInstance().getReference().child(DataBaseTablesConstants.INBOX);

            Log.d(TAG, "run: set seen");

            if (!messangerAdapter.getList().get(messangerAdapter.getList().size() - 1).getSenderID().equals(myKey)) {
//            inboxRef.child("" + myKey).child(data.get(0).getSenderID())
//                    .child("status").setValue(2);
                String sendKey = messangerAdapter.getList().get(messangerAdapter.getList().size() - 1).getSenderID();
                if (sendKey != null && !sendKey.isEmpty() && myKey != null && !myKey.isEmpty())
                    inboxRef.child(sendKey)
                            .child(myKey)
                            .child("status").setValue(2);

                ;
            }

        }
    }

    //    class AudioRecordThread implements Runnable {
//        private int sampleAudioBitRate=20;
//        private AudioRecord audioRecord;
//        private boolean isAudioRecording;
//        private boolean isRecorderStart;
//        private AudioRecord recorder;
//
//
//        AudioRecordThread() throws IOException {
//        }
//
//        @Override
//        public void run() {
//            int bufferLength = 0;
//            int bufferSize;
//            short[] audioData;
//            int bufferReadResult;
//
//
//
//            MediaRecorder recorder = new MediaRecorder();
//
//            try {
//                bufferSize = AudioRecord.getMinBufferSize(sampleAudioBitRate,
//                        AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
//
//                if (bufferSize <= 2048) {
//                    bufferLength = 2048;
//                } else if (bufferSize <= 4096) {
//                    bufferLength = 4096;
//                }
//
//                /* set audio recorder parameters, and start recording */
//                audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleAudioBitRate,
//                        AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferLength);
//                audioData = new short[bufferLength];
//                audioRecord.startRecording();
//                Log.d(TAG, "audioRecord.startRecording()");
//
//                isAudioRecording = true;
//
//                /* ffmpeg_audio encoding loop */
//                while (isAudioRecording) {
//                    bufferReadResult = audioRecord.read(audioData, 0, audioData.length);
//
//                    if (bufferReadResult == 1024 && isRecorderStart) {
//                        Buffer realAudioData1024 = ShortBuffer.wrap(audioData,0,1024);
//
//                        recorder.record(realAudioData1024);
//
//                    } else if (bufferReadResult == 2048 && isRecorderStart) {
//                        Buffer realAudioData2048_1=ShortBuffer.wrap(audioData, 0, 1024);
//                        Buffer realAudioData2048_2=ShortBuffer.wrap(audioData, 1024, 1024);
//                        for (int i = 0; i < 2; i++) {
//                            if (i == 0) {
//                                recorder.record(realAudioData2048_1);
//
//                            } else if (i == 1) {
//                                recorder.record(realAudioData2048_2);
//
//
//                            }
//                        }
//                    }
//                }
//
//                /* encoding finish, release recorder */
//                if (audioRecord != null) {
//                    try {
//                        audioRecord.stop();
//                        audioRecord.release();
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    audioRecord = null;
//                }
//
//                if (recorder != null && isRecorderStart) {
//                    try {
//                        recorder.stop();
//                        recorder.release();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    recorder = null;
//                }
//            } catch (Exception e) {
//                Log.e(TAG, "get audio data failed:"+e.getMessage()+e.getCause()+e.toString());
//            }
//
//        }
//    }
    @Override
    public void onBackPressed() {
        if (image_thumb.getTranslationY() != 0)
            super.onBackPressed();
        else {
            imageUri = null;
            sendImg.setVisibility(View.INVISIBLE);
            record_img.setVisibility(View.VISIBLE);
            image_thumb.setImageURI(null);
            image_thumb.animate().setDuration(200).translationY(1000);
        }
    }
}
