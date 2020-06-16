package com.arashivision.sdk.demo.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.arashivision.sdk.demo.R;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;

public class HomeActivity extends AppCompatActivity {
    private LinearLayout mLlOrder;
    private LinearLayout mLlLive;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        BarUtils.setStatusBarColor(this, Color.TRANSPARENT); // 沉浸式

        mLlOrder = findViewById(R.id.ll_order);
        mLlLive = findViewById(R.id.ll_live);

        mLlOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityUtils.startActivity(OrderActivity.class);
            }
        });

        mLlLive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityUtils.startActivity(MainActivity.class);
            }
        });

    }
}
