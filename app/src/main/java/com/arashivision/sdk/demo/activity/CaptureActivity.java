package com.arashivision.sdk.demo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.arashivision.sdk.demo.R;
import com.arashivision.sdk.demo.TimeFormat;
import com.arashivision.sdkcamera.camera.callback.ICaptureStatusListener;
import com.arashivision.sdkcamera.camera.InstaCameraManager;

import androidx.annotation.Nullable;

public class CaptureActivity extends BaseObserveCameraActivity implements ICaptureStatusListener {

    private TextView mTvCaptureStatus;
    private TextView mTvCaptureTime;
    private TextView mTvCaptureCount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        bindViews();

        findViewById(R.id.btn_normal_capture).setOnClickListener(v -> {
            InstaCameraManager.getInstance().startNormalCapture(false);
        });

        findViewById(R.id.btn_hdr_capture).setOnClickListener(v -> {
            InstaCameraManager.getInstance().startHDRCapture(false);
        });

        findViewById(R.id.btn_interval_shooting_start).setOnClickListener(v -> {
            InstaCameraManager.getInstance().setIntervalTime(3000);
            InstaCameraManager.getInstance().startIntervalShooting();
        });

        findViewById(R.id.btn_interval_shooting_stop).setOnClickListener(v -> {
            InstaCameraManager.getInstance().stopIntervalShooting();
        });

        findViewById(R.id.btn_normal_record_start).setOnClickListener(v -> {
            InstaCameraManager.getInstance().startNormalRecord();
        });

        findViewById(R.id.btn_normal_record_stop).setOnClickListener(v -> {
            InstaCameraManager.getInstance().stopNormalRecord();
        });

        findViewById(R.id.btn_hdr_record_start).setOnClickListener(v -> {
            InstaCameraManager.getInstance().startHDRRecord();
        });

        findViewById(R.id.btn_hdr_record_stop).setOnClickListener(v -> {
            InstaCameraManager.getInstance().stopHDRRecord();
        });

        findViewById(R.id.btn_timelapse_start).setOnClickListener(v -> {
            InstaCameraManager.getInstance().startTimeLapse();
        });

        findViewById(R.id.btn_timelapse_stop).setOnClickListener(v -> {
            InstaCameraManager.getInstance().stopTimeLapse();
        });

        // Capture Status Callback
        InstaCameraManager.getInstance().setCaptureStatusListener(this);
    }

    private void bindViews() {
        mTvCaptureStatus = findViewById(R.id.tv_capture_status);
        mTvCaptureTime = findViewById(R.id.tv_capture_time);
        mTvCaptureCount = findViewById(R.id.tv_capture_count);
    }

    @Override
    public void onCameraStatusChanged(boolean enabled) {
        super.onCameraStatusChanged(enabled);
        if (!enabled) {
            finish();
        }
    }

    @Override
    public void onCaptureStarting() {
        mTvCaptureStatus.setText("Capture Starting...");
    }

    @Override
    public void onCaptureWorking() {
        mTvCaptureStatus.setText("Capture Working...");
    }

    @Override
    public void onCaptureStopping() {
        mTvCaptureStatus.setText("Capture Stopping...");
    }

    @Override
    public void onCaptureFinish() {
        mTvCaptureStatus.setText("Capture Finished");
        mTvCaptureTime.setVisibility(View.GONE);
        mTvCaptureCount.setVisibility(View.GONE);
    }

    @Override
    public void onCaptureTimeChanged(long captureTime) {
        mTvCaptureTime.setVisibility(View.VISIBLE);
        mTvCaptureTime.setText("CaptureTime: " + TimeFormat.durationFormat(captureTime));
    }

    @Override
    public void onCaptureCountChanged(int captureCount) {
        mTvCaptureCount.setVisibility(View.VISIBLE);
        mTvCaptureCount.setText("Capture Count: " + captureCount);
    }

}
