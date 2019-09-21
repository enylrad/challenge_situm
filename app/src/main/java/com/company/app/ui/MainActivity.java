package com.company.app.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.company.app.R;
import com.company.app.ui.base.BaseActivity;

public class MainActivity extends BaseActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
}