package com.fadfadah.app.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fadfadah.app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class SmsConfrimFragment extends AppCompatActivity {

    @BindView(R.id.edt_sms_code)
    TextInputEditText edt_sms_code;
    @BindView(R.id.tv_send)
    Button tv_send;
    @BindView(R.id.tv_counter)
    TextView tv_counter;
    private ProgressDialog dialog;

    private void startLoading() {
        dialog = ProgressDialog.show(this, "",
                getResources().getString(R.string.loading_wait), true);
        dialog.setCancelable(false);
    }

    private void stopLoading() {
        dialog.dismiss();
        dialog = null;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_sms_confrim);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.tv_send)
    void send() {
        if (edt_sms_code.getText().toString().trim().isEmpty()) {
            edt_sms_code.setError(getResources().getString(R.string.required));
            return;
        }
        startLoading();
        FirebaseAuth.getInstance().sendPasswordResetEmail(edt_sms_code.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        stopLoading();
                        if (task.isSuccessful())
                            finish();
                        else Toast.makeText(SmsConfrimFragment.this,
                                (""+task.getException().getMessage()).replace("There is no user record corresponding to this identifier. ", ""), Toast.LENGTH_SHORT).show();

                    }
                });
    }
}