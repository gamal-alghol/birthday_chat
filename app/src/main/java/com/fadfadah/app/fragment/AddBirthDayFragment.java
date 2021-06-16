package com.fadfadah.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

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
public class AddBirthDayFragment extends Fragment {

    @BindView(R.id.next_btn)
    Button nextBtn;
    @BindView(R.id.choose_date_datePicker)
    DatePicker chooseDateDatePicker;

    public AddBirthDayFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_birth_day, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.next_btn)
    public void onViewClicked() {

        SignUpActivity.day = (chooseDateDatePicker.getDayOfMonth());
        SignUpActivity.month = (chooseDateDatePicker.getMonth() + 1);
        SignUpActivity.year = (chooseDateDatePicker.getYear());
        loadFrgment(new AddPhoneOrEmailFragment(), getContext());
    }

//    void setupYear() {
//        Calendar calendar = Calendar.getInstance();
//        int currentYear = calendar.get(Calendar.YEAR);
//        years.clear();
//        for (int i = (currentYear - 50); i <= currentYear; i++) {
//            years.add(String.valueOf(i));
//        }
//        spinnerYear.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.simple_expandable_list_item_1, years));
//        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                setupMonth(Integer.parseInt(years.get(position)));
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//    }
//
//    void setupMonth(int year) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.YEAR, year);
//        months.clear();
//        for (int i = 1; i <= 12; i++) {
//            months.add(String.valueOf(i));
//        }
//        spinnerMonth.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.simple_expandable_list_item_1, months));
//        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                setupDays(year, position);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//    }
//
//    void setupDays(int year, int month) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.YEAR, year);
//        calendar.set(Calendar.MONTH, month);
//        days.clear();
//        for (int i = 1; i <= calendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
//            days.add(String.valueOf(i));
//        }
//        spinnerDay.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.simple_expandable_list_item_1, days));
//        spinnerDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//    }
}
