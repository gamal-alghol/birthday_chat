package com.fadfadah.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.fadfadah.app.R;
import com.fadfadah.app.callback.FragmentSelectionListener;
import com.fadfadah.app.callback.SelectedFragment;
import com.google.android.material.tabs.TabLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class SearchCategoryFragment extends Fragment {


  @BindView(R.id.tabs)
    TabLayout tabs;
    @BindView(R.id.viewpager)
    ViewPager viewPager;
    Toolbar toolbar;
    ConstraintLayout constraintLayout;
    public SearchCategoryFragment() {
        // Required empty public constructor
    }

    public SearchCategoryFragment(FragmentSelectionListener selectionListener) {
        this.selectionListener = selectionListener;
    }

    private FragmentSelectionListener selectionListener;

    @OnClick(R.id.tv_chat)
    public void onViewClicked() {
        if (selectionListener != null)
            selectionListener.selectFragment(SelectedFragment.CHAT);
    }
    @OnClick(R.id.tv_stories)
    void openStories() {
        if (selectionListener != null)
            selectionListener.selectFragment(SelectedFragment.STORIES);
    }
    @OnClick(R.id.tv_search)
    void gotoSearch() {
        if (selectionListener != null)
            selectionListener.search();
//            selectionListener.selectFragment(SelectedFragment.SEARCH);
    }
    @OnClick(R.id.tv_notification)
    void gotoNotification() {
        if (selectionListener != null)
            selectionListener.selectFragment(SelectedFragment.NOTIFICATION);
    }
    @OnClick(R.id.menu)
    void menu() {
        if (selectionListener != null)
            selectionListener.openDrawer();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_category, container, false);
//      ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewPager=view.findViewById(R.id.viewpager);
        toolbar=view.findViewById(R.id.toolbar_condition);
        constraintLayout=view.findViewById(R.id.ConstraintLayout);
        tabs=view.findViewById(R.id.tabs);
        super.onViewCreated(view, savedInstanceState);
        viewPager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                if (position == 0)
                    return new SearchNameFragment();
                return new SearchDateFragment(selectionListener);
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                if (position == 0)
                    return getResources().getString(R.string.search_by_name);
                return getResources().getString(R.string.search_by_day);

            }
        });

tabs.setupWithViewPager(viewPager);
    }
}