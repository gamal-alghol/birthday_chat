package com.fadfadah.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.fadfadah.app.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.fadfadah.app.helper.Helper.loadFrgment;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateNewAccountFragment extends Fragment {

    @BindView(R.id.next_btn)
    Button nextBtn;

    public CreateNewAccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_new_account, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.next_btn)
    public void onViewClicked() {
        loadFrgment( new AddNameFragment(),getContext());

    }
}
