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

/**
 * A simple {@link Fragment} subclass.
 */
public class AddPhoneOrEmailFragment extends Fragment {

    @BindView(R.id.next_btn)
    Button nextBtn;

    //    @BindView(R.id.add_phone_edt)
//    TextInputEditText add_phone_edt;
    @BindView(R.id.add_email_edt)
    TextInputEditText add_email_edt;
    @BindView(R.id.add_password_edt)
    TextInputEditText add_password_edt;
    private Context mContext;
//    @BindView(R.id.country_picker)
//    CountryCodePicker country_picker;

    public AddPhoneOrEmailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_phone_or_email, container, false);
        ButterKnife.bind(this, view);
        mContext = getContext();
        return view;
    }

    @OnClick(R.id.next_btn)
    public void onViewClicked() {
//        if (inputValid(add_email_edt) && inputValid(add_password_edt))
//
//        else {
//            if (inputValid(add_phone_edt)) {
//                SignUpActivity.code = country_picker.getSelectedCountryCode();
//                SignUpActivity.phone = add_phone_edt.getText().toString();
//                SignUpActivity.registerUser();
//            } else
        if (inputValid(add_email_edt) && inputValid(add_email_edt)) {
            SignUpActivity.email = add_email_edt.getText().toString();
            SignUpActivity.password = add_password_edt.getText().toString();
            SignUpActivity.registerUser();
        }
    }
//    }

    private boolean inputValid(TextInputEditText editText) {
        if (editText.getText().toString().isEmpty()) {
            add_email_edt.setError(getResources().getString(R.string.required));
            return false;
        }
        return true;
    }
}
