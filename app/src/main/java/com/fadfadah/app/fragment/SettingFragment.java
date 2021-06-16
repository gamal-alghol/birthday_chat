package com.fadfadah.app.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fadfadah.app.R;
import com.fadfadah.app.activity.HomeActivity;
import com.fadfadah.app.callback.DataBaseTablesConstants;
import com.fadfadah.app.callback.FragmentSelectionListener;
import com.fadfadah.app.helper.SharedPereferenceClass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends Fragment {
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.language_spinner)
    Spinner languageSpinner;
    private FragmentSelectionListener selectionListener;
    private Context mContext;
    @BindView(R.id.friend_rdioBtn)
    RadioButton friend_rdioBtn;
    @BindView(R.id.public_rdioBtn)
    RadioButton public_rdioBtn;
    SharedPereferenceClass sharedPereferenceClass = new SharedPereferenceClass();
    private static final String TAG = "SettingFragment";
    public SettingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        ButterKnife.bind(this, view);
        mContext = getContext();
        return view;
    }

    int selected = 0;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences sharedEdit = mContext.getSharedPreferences("user_data", Context.MODE_PRIVATE);
        if (sharedEdit.getInt("getPrivacy", 0) == 0)
            friend_rdioBtn.setChecked(true);
        else public_rdioBtn.setChecked(true);

        if (sharedPereferenceClass.getStoredKey(mContext, "AppLanguage").equals("arabic")) {
            selected = 1;
            languageSpinner.setSelection(1);
        } else if (sharedPereferenceClass.getStoredKey(mContext, "AppLanguage").equals("english")) {
            selected = 0;
            languageSpinner.setSelection(0);
        }


        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    sharedPereferenceClass.storeKey(mContext, "AppLanguage", "english");
                } else if (position == 1) {
                    sharedPereferenceClass.storeKey(mContext, "AppLanguage", "arabic");
                }
                Log.d(TAG, "onItemSelected: " + position);
                if (selected != position) {
                    selected = position;
                    Intent intent=new Intent(mContext, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        friend_rdioBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    setPrivacy(0);
            }
        });
        public_rdioBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    setPrivacy(1);
            }
        });
    }

    private void setPrivacy(int privacy) {
        FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.USERS).child("" + FirebaseAuth.getInstance().getUid())
                .child("privacy").setValue(privacy);
    }

    @OnClick(R.id.iv_back)
    public void back() {
        getActivity().onBackPressed();
    }

}
