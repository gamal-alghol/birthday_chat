package com.fadfadah.app.fragment;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.fadfadah.app.R;
import com.fadfadah.app.callback.DataBaseTablesConstants;
import com.fadfadah.app.models.UsersModel;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {
    @BindView(R.id.iv_back)
    ImageView ivBack;

    @BindView(R.id.profile_img)
    CircleImageView profile_img;
CircleImageView imageUser;

    @BindView(R.id.name_edt)
    EditText name_edt;
    @BindView(R.id.gender_edt)
    EditText gender_edt;
    @BindView(R.id.email_edt)
    EditText email_edt;

    private static final String TAG = "ProfileFragment";
    @BindView(R.id.country_edt)
    EditText country_edt;


    @BindView(R.id.savebtn)
    Button saveBtn;
    //    @BindView(R.id.day_spinner)
//    Spinner spinnerDay;
//    @BindView(R.id.months_spinner)
//    Spinner spinnerMonth;
//    @BindView(R.id.year_spinner)
//    Spinner spinnerYear;
    @BindView(R.id.last_name_edt)
    EditText lastNameEdt;
    @BindView(R.id.add_profile_btn)
    Button addProfileBtn;

    @BindView(R.id.edit_date)
    TextView editDate;
    @BindView(R.id.day_edt)
    TextView dayEdt;
    @BindView(R.id.months_edt)
    TextView monthsEdt;
    @BindView(R.id.year_edt)
    TextView yearEdt;
    private Uri imageUri = null;
    ProgressDialog dialog;
    UsersModel user;
    private Context mContext;
    DatePickerDialog picker;
    @BindView(R.id.background_imageView)
    ImageView background_imageView;
    private Uri coverUri = null;

    @OnClick(R.id.add_profile_btn)
    void addProfile() {
    }


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);
        mContext = getContext();
        return view;
    }

    @BindView(R.id.city_edt)
    EditText city_edt;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        update();
    }

    @OnClick(R.id.iv_back)
    public void onViewClicked() {
        getActivity().onBackPressed();
    }

    public void update() {
        if (profile_img != null)
            getUserProfilInfo();
    }

    private boolean inputValid(EditText editText) {
        if (editText.getText().toString().isEmpty()) {
            editText.setError(mContext.getResources().getString(R.string.required));
            return false;
        }
        return true;
    }

    @OnClick(R.id.savebtn)
    public void saveBtn() {
        if (inputValid(name_edt) && inputValid(lastNameEdt))
            if (imageUri != null)
                uploadImage();
            else if (coverUri != null)
                uploadCover(null);
            else
                updateUser(null, null);

    }

    @OnClick(R.id.add_profile_btn)
    public void addProfileBtnn() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_pic)), 215);
    }

    @OnClick(R.id.iv_cover)
    public void addCoverBtnn() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_pic)), 216);
    }

    private void getUserProfilInfo() {

        Log.d(TAG, "getUserProfilInfo: ");
        FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.USERS).
                child("" + FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            user = dataSnapshot.getValue(UsersModel.class);
                            if (user != null) {
                                name_edt.setText(user.getFirstName());
                                lastNameEdt.setText(user.getLastName());
                                email_edt.setText(user.getEmailAddress());
                                if (user.getSex() == 0) {
                                    gender_edt.setText(getResources().getString(R.string.male));
                                } else {
                                    gender_edt.setText(getResources().getString(R.string.female));
                                }
                                dayEdt.setText(user.getDay() + "");
                                monthsEdt.setText(user.getMonth() + "");
                                yearEdt.setText(user.getYear() + "");
                                country_edt.setText(user.getCountry());
                                city_edt.setText(user.getCity());

                                try {
                                    Glide.with(mContext).load(user.getImage()).placeholder(R.drawable.profile_img).into(profile_img);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    Glide.with(mContext).load(user.getCover())
                                            .into(background_imageView);
                                } catch (Exception e) {
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 215) {
            Log.d(TAG, "onActivityResult: code success");
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "onActivityResult: resutl ok ");
                imageUri = data.getData();
                profile_img.setImageURI(null);
                profile_img.setImageURI(data.getData());


            } else Log.d(TAG, "onActivityResult: result faild");
        } else if (requestCode == 216) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "onActivityResult: resutl ok ");
                coverUri = data.getData();
                background_imageView.setImageURI(null);
                background_imageView.setImageURI(data.getData());


            } else Log.d(TAG, "onActivityResult: result faild");
        }
    }

    private void uploadCover(String url) {
        startLoading();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference riversRef = storageRef.child("cover/" + coverUri.getLastPathSegment());
        UploadTask uploadTask = riversRef.putFile(coverUri);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    stopLoading();
                    task.getException().printStackTrace();
                }
                // Continue with the task to get the download URL
                return riversRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    updateUser(url, task.getResult().toString());

                } else {
                    Toast.makeText(mContext, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    // Handle failures
                    // ...
                    stopLoading();
                }
            }
        });
    }

    private void uploadImage() {
        startLoading();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference riversRef = storageRef.child("profileImages/" + imageUri.getLastPathSegment());
        UploadTask uploadTask = riversRef.putFile(imageUri);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    stopLoading();
                    task.getException().printStackTrace();
                }
                // Continue with the task to get the download URL
                return riversRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    if (coverUri == null)
                        updateUser(task.getResult().toString(), null);
                    else uploadCover(task.getResult().toString());

                } else {
                    Toast.makeText(mContext, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    // Handle failures
                    // ...
                    stopLoading();
                }
            }
        });
    }

    private void updateUser(String image, String cover) {

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.USERS).
                child("" + FirebaseAuth.getInstance().getCurrentUser().getUid());
        user.setFirstName(name_edt.getText().toString());
        user.setLastName(lastNameEdt.getText().toString());
        user.setMonth(Integer.parseInt(monthsEdt.getText().toString()));
        user.setYear(Integer.parseInt(yearEdt.getText().toString()));
        user.setDay(Integer.parseInt(dayEdt.getText().toString()));
        user.setCountry(country_edt.getText().toString());
        user.setCity(city_edt.getText().toString());
        if (image != null)
            user.setImage(image);
        if (cover != null)
            user.setCover(cover);
        userRef.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                imageUri = null;
                stopLoading();
                Toast.makeText(mContext, getResources().getString(R.string.profile_update), Toast.LENGTH_SHORT).show();
                ivBack.performClick();
            }
        });
    }

    private void startLoading() {
        if (dialog == null) {
            dialog = ProgressDialog.show(mContext, "",
                    mContext.getResources().getString(R.string.loading_wait), true);
            dialog.setCancelable(false);
        }
        dialog.show();
    }

    private void stopLoading() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @OnClick(R.id.edit_date)
    public void editDate() {
        final Calendar cldr = Calendar.getInstance();
        int day = cldr.get(Calendar.DAY_OF_MONTH);
        int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);
        // date picker dialog
        picker = new DatePickerDialog(getActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        dayEdt.setText(dayOfMonth + "");
                        monthsEdt.setText((monthOfYear + 1) + "");
                        yearEdt.setText(year + "");

                    }
                }, year, month, day);
        picker.show();
    }
}
