package com.arashivision.sdk.demo.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.arashivision.sdk.demo.R;
import com.blankj.utilcode.util.BarUtils;

public class HelpActivity extends BaseObserveCameraActivity {
    private ImageView mIvBack;
    private TextView mTvCompany;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        BarUtils.setStatusBarColor(this, Color.TRANSPARENT);

        mTvCompany = findViewById(R.id.tv_company);
        mTvCompany.bringToFront();

        mIvBack = findViewById(R.id.iv_back);
        mIvBack.bringToFront();
        mIvBack.setOnClickListener(v -> {
            finish();
        });
    }
}
