package com.fadfadah.app.activity;

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.fadfadah.app.R;
import com.fadfadah.app.callback.DataBaseTablesConstants;
import com.fadfadah.app.fragment.CreateNewAccountFragment;
import com.fadfadah.app.fragment.SuccessSignFragment;
import com.fadfadah.app.helper.SharedPereferenceClass;
import com.fadfadah.app.models.UsersModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Calendar;
import java.util.Locale;

import static com.fadfadah.app.helper.Helper.getCurrentTimeMilliSecond;
import static com.fadfadah.app.helper.Helper.loadFrgment;

public class SignUpActivity extends AppCompatActivity {


    public static String firstName, lastName, email, image, password, city, country;
    public static int day, month, year, sex;
    public static AppCompatActivity context;
    public static String code = "";
    private static final String TAG = "SignUpActivity";
    private static ProgressDialog dialog;

    private static void startLoading() {
        dialog = ProgressDialog.show(context, "",
                context.getResources().getString(R.string.loading_wait), true);
        dialog.setCancelable(false);
    }

    private static void stopLoading() {
        dialog.dismiss();
        dialog = null;
    }

    SharedPereferenceClass sharedPereferenceClass = new SharedPereferenceClass();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectLanguage(sharedPereferenceClass.getLng(getApplicationContext()));
        setContentView(R.layout.activity_sign_up);
        context = this;
        addFragment(new CreateNewAccountFragment());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.create_account);
        }

//        addUser();
    }
    private void selectLanguage(String lng){
        Locale locale = new Locale(lng);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    public void addFragment(Fragment fr) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.signup_frame_content, fr);
        transaction.commit();
    }

    public static void registerUser() {
        startLoading();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (email != null && !email.isEmpty()) {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                addUser(auth.getUid());
                            }
                        });
                    } else {
                        Log.d(TAG, "onComplete: " + task.getException().getMessage().toString().replace("There is no user record corresponding to this identifier. ", ""));
                        Toast.makeText(context, task.getException().getMessage().toString().replace("There is no user record corresponding to this identifier. ", ""), Toast.LENGTH_SHORT).show();
                    stopLoading();
                    }
                }
            });
        }

    }

    private static void addUser(String uid) {
        /// aaa must be replaced with user id
        DatabaseReference userRef = FirebaseDatabase.getInstance().
                getReference(DataBaseTablesConstants.USERS);

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                UsersModel usersModel = new UsersModel(uid, firstName,
                        lastName, day, month, year, email,
                        task.getResult().getToken(),
                        image,
                        getCurrentTimeMilliSecond(), sex, 0, country, city,"");
                userRef.child(usersModel.getUserKey()).setValue(usersModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        addUserToTree( usersModel.getYear(),usersModel.getMonth(),usersModel.getDay(), usersModel.getUserKey());
                    }
                });

            }
        });
    }

    private static void addUserToTree(int year,int month,int day, String userKey) {
        DatabaseReference searchRef = FirebaseDatabase.getInstance().
                getReference(DataBaseTablesConstants.SEARCH);
        searchRef.child(String.valueOf(year)).child(String.valueOf(month)).child(String.valueOf(day))
                .child(userKey).setValue("1").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loadFrgment(new SuccessSignFragment(), context);
                stopLoading();
            }
        });
    }
}
