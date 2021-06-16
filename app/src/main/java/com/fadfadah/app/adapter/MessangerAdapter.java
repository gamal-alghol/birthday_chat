package com.fadfadah.app.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.danikula.videocache.HttpProxyCacheServer;
import com.fadfadah.app.App;
import com.fadfadah.app.R;
import com.fadfadah.app.activity.PostDetailActivity;
import com.fadfadah.app.models.SingleMessageObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import de.hdodenhof.circleimageview.CircleImageView;


public class MessangerAdapter extends RecyclerView.Adapter<MessangerAdapter.Filesviewholder> {
    public static final int MSG_TYPE_LEFT = 1;
    public static final int MSG_TYPE_RIGHT = 0;
    private final HttpProxyCacheServer proxy;
    List<SingleMessageObject> data;
    String imageuri;
    private Context context;
    private String myKey = "";
    private DatabaseReference inboxRef;
    RecyclerView reyclerviewMessageList;
    private CountDownTimer countTime = null;

    public MessangerAdapter(Context context, List<SingleMessageObject> data, String image, RecyclerView reyclerviewMessageList) {
        this.context = context;
        this.data = data;
        proxy = App.getProxy(context);
        this.reyclerviewMessageList = reyclerviewMessageList;
        this.imageuri = image;
        myKey = "" + FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public Filesviewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, viewGroup, false);
            return new Filesviewholder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, viewGroup, false);
            return new Filesviewholder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final Filesviewholder filesviewholder, int i) {
        final SingleMessageObject currentposition = data.get(i);
        if (currentposition.getImageUrl() != null && !currentposition.getImageUrl().isEmpty()) {
            try {
                filesviewholder.mess_img.setVisibility(View.VISIBLE);
                Glide.with(context).load(currentposition.getImageUrl()).into(filesviewholder.mess_img);
            } catch (Exception e) {
                filesviewholder.mess_img.setVisibility(View.GONE);
            }
        } else filesviewholder.mess_img.setVisibility(View.GONE);

        if (currentposition.getAudioUrl() != null && !currentposition.getAudioUrl().isEmpty()) {
            filesviewholder.showMessage.setVisibility(View.GONE);
            filesviewholder.linearlayout.setVisibility(View.VISIBLE);
            filesviewholder.frame_seek.setVisibility(View.VISIBLE);
            filesviewholder.seekbar.setProgress(0);
        } else {
            filesviewholder.showMessage.setVisibility(View.VISIBLE);
            filesviewholder.linearlayout.setVisibility(View.INVISIBLE);
            filesviewholder.frame_seek.setVisibility(View.INVISIBLE);
        }
        if (currentposition.getText().isEmpty())
            filesviewholder.showMessage.setVisibility(View.GONE);
        else
            filesviewholder.showMessage.setVisibility(View.VISIBLE);
        filesviewholder.showMessage.setText(currentposition.getText());

        if (playedPosition == i) {
            if (mp != null) {
                if (!mp.isPlaying())
                    filesviewholder.progress.setVisibility(View.VISIBLE);
else          filesviewholder.progress.setVisibility(View.INVISIBLE);
                filesviewholder.seekbar.setMax(mp.getDuration() / 100);
                filesviewholder.seekbar.setProgress((mp.getCurrentPosition() + 500) / 100);
                filesviewholder.play_pause.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
                if (((mp.getCurrentPosition() + 500) / 100) >= (mp.getDuration() / 100))
                    filesviewholder.play_pause.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            } else {
                release();
            }
        } else {
            filesviewholder.play_pause.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            filesviewholder.progress.setVisibility(View.INVISIBLE);
        }
        if (!myKey.equals(data.get(i).getSenderID())) {
            try {
                Glide.with(context).load(imageuri).placeholder(R.drawable.profile_img)
                        .into(filesviewholder.playerimage);
            } catch (Exception e) {
                e.printStackTrace();
            }
            filesviewholder.ivstatus.setVisibility(View.GONE);
        } else if (i == data.size() - 1) {
            filesviewholder.ivstatus.setVisibility(View.VISIBLE);
            if (data.get(i).getStatus() == 0)
                filesviewholder.ivstatus.setImageResource(R.drawable.ic_baseline_check_24);
            else if (data.get(i).getStatus() == 1)
                filesviewholder.ivstatus.setImageResource(R.drawable.ic_baseline_check_circle_outline_24);
            else if (data.get(i).getStatus() == 2)
                filesviewholder.ivstatus.setImageResource(R.drawable.ic_baseline_check_circle_24);
        } else filesviewholder.ivstatus.setVisibility(View.GONE);


    }

    public static String[] extractLinks(String text) {
        List<String> links = new ArrayList<String>();
        Matcher m = Patterns.WEB_URL.matcher(text);
        while (m.find()) {
            String url = m.group();
//            Log.d(TAG, "URL extracted: " + url);
            links.add(url);
        }

        return links.toArray(new String[links.size()]);
    }

    @Override
    public int getItemCount() {
        if (data == null)
            return 0;
        return data.size();
    }

    public void updateLastMessage(int status) {
        if (data != null && data.size() > 0) {
            data.get(data.size() - 1).setStatus(status);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemViewType(int position) {
        //String token = sharedPereferenceClass.getStoredKey(context, "api_token");
        if (data.get(position).getSenderID().equals(myKey)) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    public List<SingleMessageObject> getList() {
        return data;
    }


    public class Filesviewholder extends RecyclerView.ViewHolder {
        TextView showMessage;
        CircleImageView playerimage;
        ImageView ivstatus;
        ImageView mess_img;
        AppCompatSeekBar seekbar;
        FrameLayout frame_seek;
        LinearLayout linearlayout;
        ImageView play_pause;
        ProgressBar progress;

        public Filesviewholder(@NonNull View itemView) {
            super(itemView);
            showMessage = itemView.findViewById(R.id.showMessage);
            playerimage = itemView.findViewById(R.id.mess_profileImage);
            ivstatus = itemView.findViewById(R.id.iv_mess_status);
            mess_img = itemView.findViewById(R.id.mess_img);
            seekbar = itemView.findViewById(R.id.seekbar);
            linearlayout = itemView.findViewById(R.id.linearlayout);
            frame_seek = itemView.findViewById(R.id.frame_seek);
            play_pause = itemView.findViewById(R.id.play_pause);
            progress = itemView.findViewById(R.id.progress);
            frame_seek.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (playedPosition == getAdapterPosition() && mp != null && mp.isPlaying())
                            release();
                        else {
                            playAudio(data.get(getAdapterPosition()).getAudioUrl(), getAdapterPosition());
                            progress.setVisibility(View.VISIBLE);

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            mess_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, PostDetailActivity.class);
// Pass data object in the bundle and populate details activity.
                    intent.putExtra(PostDetailActivity.IMAGE, data.get(getAdapterPosition()).getImageUrl());
                    intent.putExtra(PostDetailActivity.TEXT, "");
                    ActivityOptionsCompat options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation((Activity) context, (View) mess_img, "transition");
                    context.startActivity(intent, options.toBundle());
                }
            });
//            richLinkView = itemView.findViewById(R.id.richLinkView);
        }

    }

    MediaPlayer mp = null;

    private void playAudio(String audioUrl, int adapterPosition) throws IOException {
        release();
        mp = new MediaPlayer();


        String url = proxy.getProxyUrl(audioUrl);
        try {
            mp.setDataSource(url);

            new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        mp.prepare();
                        mp.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            Log.d(TAG, "playAudio: ");
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.d(TAG, "onPrepared: ");
                    attachSeekbar(adapterPosition);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            release();
        }


    }

    private static final String TAG = "MessangerAdapter";
    private int playedPosition = -1;

    private void attachSeekbar(int position) {
        Log.d(TAG, "attachSeekbar: ");
        if (mp != null) {

            Log.d(TAG, "attachSeekbar: not null ");


            playedPosition = position;

            int total = mp.getDuration();

            Log.d(TAG, "attachSeekbar:total " + total);

            countTime = new CountDownTimer(mp.getDuration(), 500) {

                public void onTick(long millisUntilFinished) {
                    if (position != -1 && position < data.size())
                        notifyItemChanged(position);
                }

                public void onFinish() {
                    release();
                }

            };
            countTime.start();


        }
    }

    public void release() {
        if (mp != null) {
            if (mp.isPlaying())
                mp.stop();
            mp.release();

        }
        mp = null;
        if (countTime != null) {
            countTime.cancel();
            countTime = null;
        }

        if (playedPosition != -1 && playedPosition < data.size()) {
            notifyItemChanged(playedPosition);
            playedPosition = -1;
        }
    }


}
