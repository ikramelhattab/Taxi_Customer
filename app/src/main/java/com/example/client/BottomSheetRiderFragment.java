package com.example.client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheetRiderFragment extends BottomSheetDialogFragment {

    String mTag;

    public static BottomSheetRiderFragment newInstance(String tag){
        BottomSheetRiderFragment f =new BottomSheetRiderFragment();
        Bundle args=new Bundle();
        args.putString("TAG",tag);
        f.setArguments(args);
        return f;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTag=getArguments().getString("TAG");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.button_sheet_rider,container,false);
        return view;
    }
}
