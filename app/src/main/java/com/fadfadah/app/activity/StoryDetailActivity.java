package com.fadfadah.app.activity;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.fadfadah.app.R;
import com.fadfadah.app.callback.StoryDetails;
import com.fadfadah.app.fragment.FragmentStatus;
import com.fadfadah.app.helper.SharedPereferenceClass;
import com.fadfadah.app.models.StoriesModel;
import com.fadfadah.app.view.CustomViewPager;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StoryDetailActivity extends AppCompatActivity implements StoryDetails {

    public static final String USER_KEY = "user";
    public static final String VIDEO = "video";
    public static final String STORY_KEY = "story";
    public static final String STORY_FILTER_POSITION = "filter_position";

    @BindView(R.id.viewpager)
    CustomViewPager viewpager;
    ArrayList<StoriesModel> models = new ArrayList<>();

    SharedPereferenceClass sharedPereferenceClass = new SharedPereferenceClass();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectLanguage(sharedPereferenceClass.getLng(getApplicationContext()));
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_status_detail);
        ButterKnife.bind(this);

        getWindow().setNavigationBarColor(Color.BLACK);


        if (getIntent().hasExtra(VIDEO)) {
            models = (ArrayList<StoriesModel>) getIntent().getSerializableExtra(VIDEO);
            if (models != null && models.size() > 0) {

                viewpager.setOffscreenPageLimit(0);
                viewpager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {


                    @NonNull
                    @Override
                    public Fragment getItem(int position) {
                        return new FragmentStatus(models.get(position),
                                getIntent().getStringExtra(USER_KEY), position, StoryDetailActivity.this::storyFinished);
                    }

                    @Override
                    public int getCount() {
                        return models.size();
                    }
                });
            }
        }

    }

    private void selectLanguage(String lng) {
        Locale locale = new Locale(lng);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    @Override
    public void storyFinished(int position) {
        position++;
        if (position >= models.size())
            finish();
        else
            viewpager.setCurrentItem(position, true);
    }
}