package com.fadfadah.app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.fadfadah.app.R;
import com.fadfadah.app.activity.SignUpActivity;
import com.google.android.material.textfield.TextInputEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.fadfadah.app.helper.Helper.loadFrgment;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddNameFragment extends Fragment {

    @BindView(R.id.next_btn)
    Button nextBtn;

    @BindView(R.id.edt_firstname)
    TextInputEditText edt_firstname;
    @BindView(R.id.edt_lasttname)
    TextInputEditText edt_lasttname;
    @BindView(R.id.edt_city)
    TextInputEditText edt_city;
    @BindView(R.id.edt_country)
    TextInputEditText edt_country;
    private Context mContext;

    public AddNameFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_name, container, false);
        ButterKnife.bind(this, view);
        mContext = getContext();
        return view;
    }

    @OnClick(R.id.next_btn)
    public void onViewClicked() {
        if (inputValid(edt_firstname) && inputValid(edt_lasttname)) {
            SignUpActivity.firstName = edt_firstname.getText().toString();
            SignUpActivity.lastName = edt_lasttname.getText().toString();
            SignUpActivity.country = edt_country.getText().toString();
            SignUpActivity.city = edt_city.getText().toString();
            loadFrgment(new GenderFragment(), getContext());
        }

    }

    private boolean inputValid(TextInputEditText editText) {
        if (editText.getText().toString().isEmpty()) {
            editText.setError(mContext.getResources().getString(R.string.required));
            return false;
        }
        return true;
    }
}
