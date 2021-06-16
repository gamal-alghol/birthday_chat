package com.fadfadah.app.helper;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.fadfadah.app.R;

import java.util.Calendar;
import java.util.TimeZone;

public class Helper {


    public static void loadFrgment(Fragment fragment, Context context) {
        AppCompatActivity activity = (AppCompatActivity) context;
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.signup_frame_content, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public static String calcTime(long timeMillisecond, Context mContext) {
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(timeMillisecond);
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(getCurrentTimeMilliSecond());
        if (now.get(Calendar.MONTH) - time.get(Calendar.MONTH) > 1) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(now.getTimeInMillis() - time.getTimeInMillis());
            return (mContext.getResources().getString(R.string.more_month));
        } else if (now.get(Calendar.MONTH) - time.get(Calendar.MONTH) == 1) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(now.getTimeInMillis() - time.getTimeInMillis());
            return (mContext.getResources().getString(R.string.last_month));
        } else if (now.get(Calendar.WEEK_OF_MONTH) - time.get(Calendar.WEEK_OF_MONTH) >= 1) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(now.getTimeInMillis() - time.getTimeInMillis());
            return (calendar.get(Calendar.WEEK_OF_YEAR) + " " +
                    mContext.getResources().getString(R.string.weeks_ago));
        } else if (now.get(Calendar.DAY_OF_YEAR) - time.get(Calendar.DAY_OF_YEAR) >= 1) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(now.getTimeInMillis() - time.getTimeInMillis());
            return (calendar.get(Calendar.DAY_OF_YEAR) + " " +
                    mContext.getResources().getString(R.string.days_ago));
        } else if (now.get(Calendar.HOUR_OF_DAY) - time.get(Calendar.HOUR_OF_DAY) >= 1) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(now.getTimeInMillis() - time.getTimeInMillis());
            return (calendar.get(Calendar.HOUR_OF_DAY) + " " +
                    mContext.getResources().getString(R.string.hours_ago));
        } else if (now.get(Calendar.MINUTE) - time.get(Calendar.MINUTE) >= 1) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(now.getTimeInMillis() - time.getTimeInMillis());
            return (calendar.get(Calendar.MINUTE) + " " + mContext.getResources().getString(R.string.minutes_ago));
        } else
            return mContext.getResources().getString(R.string.just_now);
    }

    public static long getCurrentTimeMilliSecond() {
        Calendar aGMTCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        return aGMTCalendar.getTimeInMillis();
    }
}
