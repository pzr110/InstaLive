package com.arashivision.sdk.demo.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.arashivision.sdk.demo.R;
import com.arashivision.sdk.demo.utils.bean.Data;
import com.arashivision.sdk.demo.utils.bean.OrderBean;
import com.arashivision.sdk.demo.utils.bean.TokenBean;
import com.arashivision.sdk.demo.utils.net.Api;
import com.arashivision.sdk.demo.utils.net.BaseSubscriber;
import com.arashivision.sdkcamera.camera.InstaCameraManager;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.squareup.haha.perflib.Main;
import com.wang.avi.AVLoadingIndicatorView;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.util.HashMap;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends BaseObserveCameraActivity {

    private AVLoadingIndicatorView mAvi;
    private String mOrderId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        mOrderId = intent.getStringExtra("OrderId");

        mAvi = findViewById(R.id.avi);
        mAvi.bringToFront();
        mAvi.hide();

        BarUtils.setStatusBarColor(this, Color.TRANSPARENT); // 沉浸式

        checkStoragePermission();

        if (InstaCameraManager.getInstance().getCameraConnectedType() != InstaCameraManager.CONNECT_TYPE_NONE) {
            onCameraStatusChanged(true);
        }

        findViewById(R.id.tv_help).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, HelpActivity.class));
        });

        findViewById(R.id.btn_connect_by_wifi).setOnClickListener(v -> {
            InstaCameraManager.getInstance().openCamera(InstaCameraManager.CONNECT_TYPE_WIFI);
        });

        findViewById(R.id.btn_connect_by_usb).setOnClickListener(v -> {
            InstaCameraManager.getInstance().openCamera(InstaCameraManager.CONNECT_TYPE_USB);
        });

        findViewById(R.id.iv_live).setOnClickListener(v -> {
            mAvi.show();

            int cameraConnectedType = InstaCameraManager.getInstance().getCameraConnectedType();

            if (cameraConnectedType == -1) {
                InstaCameraManager.getInstance().openCamera(InstaCameraManager.CONNECT_TYPE_USB);
            } else if (cameraConnectedType == 1) {
//                startActivity(new Intent(MainActivity.this, LiveActivity.class));
                startLiveActivity();
            }

        });

        findViewById(R.id.btn_close_camera).setOnClickListener(v -> {
            InstaCameraManager.getInstance().closeCamera();
        });

        findViewById(R.id.btn_capture).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CaptureActivity.class));
        });

        findViewById(R.id.btn_preview).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, PreviewActivity.class));
        });

        findViewById(R.id.btn_live).setOnClickListener(v -> {
//            startActivity(new Intent(MainActivity.this, LiveActivity.class));
            startLiveActivity();
        });

        findViewById(R.id.btn_osc).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, OscActivity.class));
        });

        findViewById(R.id.btn_list_camera_file).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CameraFilesActivity.class));
        });

        findViewById(R.id.btn_settings).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, MoreSettingActivity.class));
        });

        findViewById(R.id.btn_stitch).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, StitchActivity.class));
        });
    }


    private void checkStoragePermission() {
        AndPermission.with(this)
                .runtime()
                .permission(Permission.Group.STORAGE)
                .onDenied(permissions -> {
                    if (AndPermission.hasAlwaysDeniedPermission(this, permissions)) {
                        AndPermission.with(this)
                                .runtime()
                                .setting()
                                .start(1000);
                    }
                    finish();
                })
                .start();

    }

    private void startLiveActivity() {


        Intent intent = new Intent(MainActivity.this, LiveActivity.class);
        intent.putExtra("OrderIdA", mOrderId);
        startActivity(intent);
    }


    @Override
    public void onCameraStatusChanged(boolean enabled) {
        super.onCameraStatusChanged(enabled);
        findViewById(R.id.btn_capture).setEnabled(enabled);
        findViewById(R.id.btn_preview).setEnabled(enabled);
        findViewById(R.id.btn_live).setEnabled(enabled);
        findViewById(R.id.btn_osc).setEnabled(enabled);
        findViewById(R.id.btn_list_camera_file).setEnabled(enabled);
        findViewById(R.id.btn_settings).setEnabled(enabled);
        if (enabled) {
            Toast.makeText(this, "相机已连接", Toast.LENGTH_SHORT).show();
//            findViewById(R.id.btn_live).setOnClickListener(v -> {
            mAvi.hide();

//            startActivity(new Intent(MainActivity.this, LiveActivity.class));
            startLiveActivity();
//            });
        } else {
            Toast.makeText(this, "相机已断开", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCameraConnectError() {
        super.onCameraConnectError();
        mAvi.hide();
        Toast.makeText(this, "相机连接失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraSDCardStateChanged(boolean enabled) {
        super.onCameraSDCardStateChanged(enabled);
        if (enabled) {
            Toast.makeText(this, "SD卡已插入", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "SD卡已移除", Toast.LENGTH_SHORT).show();
        }
    }

}
