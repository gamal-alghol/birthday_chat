package com.fadfadah.app.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.fadfadah.app.R;
import com.fadfadah.app.adapter.MPagerAdapter;
import com.fadfadah.app.helper.SharedPereferenceClass;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WelcomeActivity extends AppCompatActivity {

    @BindView(R.id.viewpager)
    ViewPager viewpager;
    @BindView(R.id.dotlayout)
    LinearLayout dotlayout;
    @BindView(R.id.start_btn)
    Button startBtn;
    private int[] layouts = {R.layout.third_slide, R.layout.seccend_slide, R.layout.first_slide};
    private MPagerAdapter mPagerAdapter;
    private ImageView[] dots;

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
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);
        if (FirebaseAuth.getInstance().getCurrentUser() != null ) {
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            finish();
        }

        mPagerAdapter = new MPagerAdapter(layouts, this);
        currntDots(WelcomeActivity.this, 2, dotlayout, layouts, dots);
        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    startBtn.setVisibility(View.VISIBLE);
                    currntDots(WelcomeActivity.this, 0, dotlayout, layouts, dots);
                    startBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getSharedPreferences("app_usage", MODE_PRIVATE)
                                    .edit().putBoolean("first_time", false).apply();

                            Intent loginIntent = new Intent(WelcomeActivity.this, LoginActivity.class);
                            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(loginIntent);
                            finish();
                        }
                    });
                } else {
                    startBtn.setVisibility(View.INVISIBLE);
                    currntDots(WelcomeActivity.this, position, dotlayout, layouts, dots);

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        viewpager.setAdapter(mPagerAdapter);
        viewpager.setCurrentItem(2);
    }

    private void currntDots(Context context, int position, LinearLayout dots_layout, int[] layouts, ImageView[] dots) {
        if (dots_layout != null)
            dots_layout.removeAllViews();
        dots = new ImageView[layouts.length];
        for (int i = 0; i < layouts.length; i++) {
            dots[i] = new ImageView(context);
            if (i == position) {
                dots[i].setImageDrawable(ContextCompat.getDrawable(context, R.drawable.active_page));
            } else {
                dots[i].setImageDrawable(ContextCompat.getDrawable(context, R.drawable.non_active_circle));
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT
                    , ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(4, 0, 4, 0);
            dots_layout.addView(dots[i], params);
        }
    }

}
