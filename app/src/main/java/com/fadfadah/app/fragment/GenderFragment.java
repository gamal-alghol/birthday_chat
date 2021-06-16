package com.fadfadah.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;

import androidx.fragment.app.Fragment;

import com.fadfadah.app.R;
import com.fadfadah.app.activity.SignUpActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.fadfadah.app.helper.Helper.loadFrgment;

/**
 * A simple {@link Fragment} subclass.
 */
public class GenderFragment extends Fragment {

    @BindView(R.id.next_btn)
    Button nextBtn;

    @BindView(R.id.male_rdioBtn)
    RadioButton male_rdioBtn;

    public GenderFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gender, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.next_btn)
    public void onViewClicked() {
        if (male_rdioBtn.isChecked())
            SignUpActivity.sex = 0;
        else SignUpActivity.sex = 1;

        loadFrgment(new AddBirthDayFragment(), getContext());

    }
}
