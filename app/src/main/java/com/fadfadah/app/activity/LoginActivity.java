package com.fadfadah.app.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fadfadah.app.R;
import com.fadfadah.app.callback.DataBaseTablesConstants;
import com.fadfadah.app.fragment.SmsConfrimFragment;
import com.fadfadah.app.helper.SharedPereferenceClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Locale;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.arabic_lang)
    Button arabicLang;
    @BindView(R.id.english_lang)
    Button englishLang;
        @BindView(R.id.forget_password_txt)
        TextView forgetPasswordTxt;
        @BindView(R.id.signIn_btn)
        Button signInBtn;
        @BindView(R.id.signUp_txt)
        TextView signUpTxt;
        @BindView(R.id.password_edt)
        TextInputEditText password_edt;
        @BindView(R.id.phoneOrMail_edt)
        TextInputEditText phoneOrMail_edt;
        private static final String TAG = "LoginActivity";
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


    SharedPereferenceClass sharedPereferenceClass = new SharedPereferenceClass();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectLanguage(sharedPereferenceClass.getLng(getApplicationContext()));
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        if (sharedPereferenceClass.getLng(getApplicationContext()).equals("en")) {
            unSelect(arabicLang);
            select(englishLang);

        } else {
            select(arabicLang);
            unSelect(englishLang);
        }
    }

    @OnClick({R.id.arabic_lang, R.id.english_lang, R.id.forget_password_txt, R.id.signIn_btn, R.id.signUp_txt})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.arabic_lang:
                sharedPereferenceClass.storeKey(LoginActivity.this, "AppLanguage", "arabic");
                select(arabicLang);
                unSelect(englishLang);
                recreate();
                break;
            case R.id.english_lang:
                sharedPereferenceClass.storeKey(LoginActivity.this, "AppLanguage", "english");
                unSelect(arabicLang);
                select(englishLang);
                recreate();
                break;
            case R.id.forget_password_txt:
                Intent forget = new Intent(LoginActivity.this, SmsConfrimFragment.class);
                forget.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(forget);
                break;
            case R.id.signIn_btn:
                signin();
                break;
            case R.id.signUp_txt:
                Intent signIntent = new Intent(LoginActivity.this, SignUpActivity.class);
                signIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(signIntent);
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser()!=null ){
            Intent intent=new Intent(getApplicationContext(),HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    private void signin() {
    //        int x = new Random().nextInt(6);
    //        if (x % 2 == 0) {
    //            phoneOrMail_edt.setText("mohamedmzd0@gmail.com");
    //            password_edt.setText("123456");
    //        } else  if (x % 3 == 0) {
    //            phoneOrMail_edt.setText("mzizo041@gmail.com");
    //            password_edt.setText("123456");
    //        } else {
    //            phoneOrMail_edt.setText("mohamedmzd0@yahoo.com");
    //            password_edt.setText("123456");
    //        }


                if (inputValid(phoneOrMail_edt) && inputValid(password_edt)) {
                if (isEmailValid(phoneOrMail_edt.getText())) {
                    startLoading();
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(phoneOrMail_edt.getText().toString(),
                            password_edt.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult().getUser().isEmailVerified()) {

                                    updateToken();

                                } else {
                                    stopLoading();
                                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.check_email_confirm), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                stopLoading();
                                Log.d(TAG, "onComplete: " + task.getException().getMessage().toString().replace("There is no user record corresponding to this identifier. ", ""));
                                Toast.makeText(LoginActivity.this, task.getException().getMessage().toString().replace("There is no user record corresponding to this identifier. ", ""), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else if (isPhoneValid(phoneOrMail_edt.getText())) {
    //                FirebaseAuth.getInstance().signinw
                }
            }
        }

    private void updateToken() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                FirebaseDatabase.getInstance().
                        getReference(DataBaseTablesConstants.USERS).child("" + FirebaseAuth.getInstance().getUid())
                        .child("userToken").setValue("" + task.getResult().getToken()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
                        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(homeIntent);
                        finish();
                    }
                });

            }
        });

    }

    private void selectLanguage(String lng) {
        Locale locale = new Locale(lng);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    boolean isPhoneValid(CharSequence email) {
        return Patterns.PHONE.matcher(email).matches();
    }

    private boolean inputValid(TextInputEditText editText) {
        if (editText.getText().toString().isEmpty()) {
            editText.setError(getResources().getString(R.string.required));
            return false;
        }
        return true;
    }

    private void select(Button button) {
        button.setBackgroundResource(R.drawable.rounded_button_blue);
        button.setTextColor(Color.WHITE);
    }

    private void unSelect(Button button) {
        button.setTextColor(Color.BLACK);
        button.setBackgroundResource(R.drawable.rounded_button_white);
    }

}
