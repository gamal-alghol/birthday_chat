package com.fadfadah.app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fadfadah.app.R;
import com.fadfadah.app.callback.FragmentSelectionListener;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchDateFragment extends Fragment {

//    @BindView(R.id.choose_date_datePicker)
//    DatePicker chooseDateDatePicker;

    public SearchDateFragment() {
        // Required empty public constructor
    }

    public SearchDateFragment(FragmentSelectionListener listener) {
        this.listener = listener;
    }

    private FragmentSelectionListener listener;


    //    @BindView(R.id.day_spinner)
//    Spinner spinnerDay;
//    @BindView(R.id.months_spinner)
//    Spinner spinnerMonth;
    @BindView(R.id.sp_year)
    Spinner spinnerYear;
    @BindView(R.id.tv_search)
    TextView tvSearch;

    private Context mContext;
    //    ArrayList<String> months = new ArrayList<>();
    ArrayList<String> years = new ArrayList<>();
//    ArrayList<String> days = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_search_date, container, false);
        ButterKnife.bind(this, rootView);
        mContext = getContext();

        return rootView;
    }

    @OnClick(R.id.tv_search)
    void search() {
        if (listener != null)
            listener.search(String.valueOf(years.get(spinnerYear.getSelectedItemPosition())));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1950);
        Calendar now = Calendar.getInstance();

        for (int i = 0; i < (now.get(Calendar.YEAR) - calendar.get(Calendar.YEAR)); i++) {
            years.add("" + (1950 + i));
        }
        spinnerYear.setAdapter(new ArrayAdapter<String>(mContext, R.layout.simple_expandable_list_item_1, years));
    }
}