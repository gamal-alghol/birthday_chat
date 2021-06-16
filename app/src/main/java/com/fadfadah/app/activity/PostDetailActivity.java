package com.fadfadah.app.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media.session.MediaButtonReceiver;

import com.bogdwellers.pinchtozoom.ImageMatrixTouchHandler;
import com.bumptech.glide.Glide;
import com.danikula.videocache.HttpProxyCacheServer;
import com.fadfadah.app.App;
import com.fadfadah.app.R;
import com.fadfadah.app.helper.SharedPereferenceClass;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostDetailActivity extends AppCompatActivity implements ExoPlayer.EventListener {

    public static final String IMAGE = "image";
    public static final String VIDEO = "video";
    public static final String TEXT = "text";
    @BindView(R.id.image)
    ImageView viewZoom;
    @BindView(R.id.tv_post_content)
    TextView tv_post_content;
    @BindView(R.id.player)
    SimpleExoPlayerView simpleExoPlayerView;
    SimpleExoPlayer simpleExoPlayer;
    MediaSessionCompat mediaSessionCompat;
    PlaybackStateCompat.Builder builder;
    HttpProxyCacheServer proxy;

    private void selectLanguage(String lng) {
        Locale locale = new Locale(lng);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    SharedPereferenceClass sharedPereferenceClass = new SharedPereferenceClass();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectLanguage(sharedPereferenceClass.getLng(getApplicationContext()));
        setContentView(R.layout.activity_post_detail);
        ButterKnife.bind(this);

        if (getIntent().hasExtra(IMAGE) && getIntent().getStringExtra(IMAGE) != null) {
            viewZoom.setOnTouchListener(new ImageMatrixTouchHandler(viewZoom.getContext()));
           try {
               Glide.with(this).load(getIntent().getStringExtra(IMAGE)).into(viewZoom);
           } catch (Exception e) {
               e.printStackTrace();
           }
            simpleExoPlayerView.setVisibility(View.GONE);
        } else if (getIntent().hasExtra(VIDEO) && getIntent().getStringExtra(VIDEO) != null) {
            proxy = App.getProxy(getApplicationContext());

            String url = proxy.getProxyUrl(getIntent().getStringExtra(VIDEO));
            initializePlayer(url);
        }

        if (getIntent().hasExtra(TEXT) && getIntent().getStringExtra(TEXT) != null
                && getIntent().getStringExtra(TEXT).trim().length() > 0) {
            tv_post_content.setText(getIntent().getStringExtra(TEXT));
        } else
            tv_post_content.setVisibility(View.GONE);
    }

    private void initializePlayer(String url) {
        if (simpleExoPlayer == null) {
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(getApplicationContext(), trackSelector, loadControl);
            simpleExoPlayerView.setPlayer(simpleExoPlayer);

            String userAgent = Util.getUserAgent(getApplicationContext(), "video");
            MediaSource mediaSource = new ExtractorMediaSource(Uri.parse(url)
                    , new DefaultDataSourceFactory(getApplicationContext(), userAgent),
                    new DefaultExtractorsFactory(), null, null);
            simpleExoPlayer.prepare(mediaSource);
            simpleExoPlayer.setPlayWhenReady(true);
        }

    }

    private void initializeMediaSession() {
        String TAG = PostDetailActivity.class.getSimpleName();
        mediaSessionCompat = new MediaSessionCompat(getApplicationContext(), TAG);

        mediaSessionCompat.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSessionCompat.setMediaButtonReceiver(null);
        builder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mediaSessionCompat.setPlaybackState(builder.build());
        mediaSessionCompat.setCallback(new mediasessioncall());
        mediaSessionCompat.setActive(true);

    }


    @Override
    public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroupArray, TrackSelectionArray trackSelectionArray) {

    }

    @Override
    public void onLoadingChanged(boolean b) {

    }

    private void releasePlayer() {

        if (simpleExoPlayer != null) {
            simpleExoPlayer.stop();
            simpleExoPlayer.release();
        }
        simpleExoPlayer = null;
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if ((playbackState == ExoPlayer.STATE_READY) && playWhenReady) {
            builder.setState(PlaybackStateCompat.STATE_PLAYING,
                    simpleExoPlayer.getCurrentPosition(), 1f);
        } else if ((playbackState == ExoPlayer.STATE_READY)) {
            builder.setState(PlaybackStateCompat.STATE_PAUSED,
                    simpleExoPlayer.getCurrentPosition(), 1f);
        }
        mediaSessionCompat.setPlaybackState(builder.build());

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }


    @Override
    public void onPause() {
        super.onPause();
        if (simpleExoPlayer != null)
            mediaSessionCompat.setActive(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (simpleExoPlayer != null) {
            initializeMediaSession();
            initializePlayer(getIntent().getStringExtra(VIDEO));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (simpleExoPlayer != null) {
            releasePlayer();
            mediaSessionCompat.setActive(false);
        }
    }


    class mediasessioncall extends MediaSessionCompat.Callback {
        public mediasessioncall() {
            super();

        }

        @Override
        public void onPlay() {
            super.onPlay();
            simpleExoPlayer.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            super.onPause();
            simpleExoPlayer.setPlayWhenReady(false);
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            simpleExoPlayer.seekTo(0);
        }
    }

    public class myreciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MediaButtonReceiver.handleIntent(mediaSessionCompat, intent);
        }
    }

}