package com.fadfadah.app.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daasuu.gpuv.player.GPUPlayerView;
import com.daasuu.gpuv.player.PlayerScaleType;
import com.danikula.videocache.HttpProxyCacheServer;
import com.fadfadah.app.App;
import com.fadfadah.app.Filter.FilterType;
import com.fadfadah.app.R;
import com.fadfadah.app.Video_Recording.MovieWrapperView;
import com.fadfadah.app.activity.StoryDetailActivity;
import com.fadfadah.app.adapter.StoreisSeenAdapter;
import com.fadfadah.app.callback.DataBaseTablesConstants;
import com.fadfadah.app.callback.StoryDetails;
import com.fadfadah.app.models.StoriesModel;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FragmentStatus extends Fragment implements ExoPlayer.EventListener {

    StoriesModel storiesModel;
    @BindView(R.id.player)
    SimpleExoPlayerView simpleExoPlayerView;
    SimpleExoPlayer simpleExoPlayer;
    MediaSessionCompat mediaSessionCompat;
    HttpProxyCacheServer proxy;
    Context mContext;
    String url;
    private DatabaseReference statusRef;
    @BindView(R.id.recycler_seen_by)
    RecyclerView recycler_seen_by;
    String userKey;
    private int position;
    private StoryDetails storyDetails;
    private static final String TAG = "FragmentStatus";
    MovieWrapperView filterVideo;


    //
    GPUPlayerView gpuPlayerView;
    final List<FilterType> filterTypes = FilterType.createFilterList();

    public FragmentStatus(StoriesModel storiesModel, String userKey, int position, StoryDetails storyDetails) {
        this.storiesModel = storiesModel;
        this.userKey = userKey;
        this.position = position;
        this.storyDetails = storyDetails;
    }


    public FragmentStatus() {
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        proxy = App.getProxy(mContext);
        url = proxy.getProxyUrl(storiesModel.getVideo());
        initializePlayer(url);

        recycler_seen_by.setLayoutManager(new LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, true));
        if (!userKey.equals("" + FirebaseAuth.getInstance().getUid())) {
            recycler_seen_by.setVisibility(View.GONE);
            setSeen(storiesModel.getFromID(), storiesModel.getStoryKey());
        } else {
            ArrayList<String> keys = new ArrayList<>();
            FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.STATUS)
                    .child(userKey).child(storiesModel.getStoryKey()).child(DataBaseTablesConstants.SEEN).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snap :
                            dataSnapshot.getChildren()) {
                        keys.add(snap.getKey());
                    }
                    recycler_seen_by.setAdapter(new StoreisSeenAdapter(mContext, keys));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void setSeen(String fromID, String storyKey) {
        if (statusRef == null)
            statusRef = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.STATUS);
        statusRef.child(fromID).child(storyKey).child(DataBaseTablesConstants.SEEN).child("" + FirebaseAuth.getInstance().getUid()).setValue(1);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stories_deailts, container, false);
     filterVideo=rootView.findViewById(R.id.layout_movie_wrapper);
        ButterKnife.bind(this, rootView);
        mContext = getContext();
        return rootView;
    }

    private void initializePlayer(String url) {
        if (simpleExoPlayer == null) {

            TrackSelector trackSelector = new DefaultTrackSelector();
            simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector);
            simpleExoPlayerView.setPlayer(simpleExoPlayer);

            String userAgent = Util.getUserAgent(mContext, "video");
            MediaSource mediaSource = new ExtractorMediaSource(Uri.parse(url)
                    , new DefaultDataSourceFactory(mContext, userAgent),
                    new DefaultExtractorsFactory(), null, null);
            simpleExoPlayer.prepare(mediaSource);
            simpleExoPlayer.setPlayWhenReady(true);
            simpleExoPlayerView.setUseController(false);




            //Todo Filter
            gpuPlayerView = new GPUPlayerView(getContext());

            gpuPlayerView.setPlayerScaleType(PlayerScaleType.RESIZE_FIT_WIDTH);
            //gpuPlayerView.setPlayerScaleType(PlayerScaleType.RESIZE_NONE);
            gpuPlayerView.setSimpleExoPlayer(simpleExoPlayer);
            gpuPlayerView.setLayoutParams(new RelativeLayout
                    .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            filterVideo.addView(gpuPlayerView);

            gpuPlayerView.onResume();
            gpuPlayerView.setGlFilter(FilterType.createGlFilter(filterTypes.get(storiesModel.getFilterPosition()), getContext()));


        }

    }

    private void initializeMediaSession() {
        String TAG = StoryDetailActivity.class.getSimpleName();
        mediaSessionCompat = new MediaSessionCompat(mContext, TAG);
        mediaSessionCompat.setMediaButtonReceiver(null);
        mediaSessionCompat.setActive(true);
        simpleExoPlayer.addListener(this);
//        simpleExoPlayerView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    pausePlayer();
//                } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                    startPlayer();
//                }
//
//                return true;
//            }
//        });


    }

    private void pausePlayer() {
        simpleExoPlayer.setPlayWhenReady(false);
        simpleExoPlayer.getPlaybackState();
    }

    private void startPlayer() {
        simpleExoPlayer.setPlayWhenReady(true);
        simpleExoPlayer.getPlaybackState();
    }

    @Override
    public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {
        Log.d(TAG, "onTimelineChanged: ");
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroupArray, TrackSelectionArray trackSelectionArray) {
        Log.d(TAG, "onTracksChanged: ");
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
        if (storyDetails != null)
            storyDetails.storyFinished(position);
    }

    @Override
    public void onPositionDiscontinuity(int reason) {
        Log.d(TAG, "onPositionDiscontinuity: ");
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
        Log.d(TAG, "onPlaybackParametersChanged: ");
    }

    @Override
    public void onSeekProcessed() {
        Log.d(TAG, "onSeekProcessed: ");
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        Log.d(TAG, "onPlayerStateChanged: "+playbackState);
        if (playbackState == ExoPlayer.STATE_ENDED) {
            if (storyDetails != null)
                storyDetails.storyFinished(position);
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
        Log.d(TAG, "onRepeatModeChanged: ");
    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
        Log.d(TAG, "onShuffleModeEnabledChanged: ");
    }


    @Override
    public void onStart() {
        super.onStart();
        if (simpleExoPlayer != null) {
            initializeMediaSession();
            initializePlayer(url);
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
}
