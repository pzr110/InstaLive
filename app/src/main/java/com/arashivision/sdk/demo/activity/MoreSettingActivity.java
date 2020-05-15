package com.arashivision.sdk.demo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arashivision.sdk.demo.R;
import com.arashivision.sdkcamera.camera.callback.ICameraOperateCallback;
import com.arashivision.sdkcamera.camera.InstaCameraManager;
import com.xw.repo.BubbleSeekBar;

import androidx.annotation.Nullable;

public class MoreSettingActivity extends BaseObserveCameraActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_setting);
        initCameraEV();
        initCameraBeepSwitch();
        initCalibrateGyro();
        initFormatStorage();
    }

    private void initCameraEV() {
        TextView tvEV = findViewById(R.id.tv_ev_value);
        tvEV.setText(String.valueOf(InstaCameraManager.getInstance().getExposureEV(getCurrentFuncMode())));

        BubbleSeekBar sbEv = findViewById(R.id.sb_ev);
        sbEv.setProgress(InstaCameraManager.getInstance().getExposureEV(getCurrentFuncMode()));
        findViewById(R.id.btn_set_ev).setOnClickListener(v -> {
            InstaCameraManager.getInstance().setExposureEV(getCurrentFuncMode(), sbEv.getProgressFloat());
            tvEV.setText(String.valueOf(InstaCameraManager.getInstance().getExposureEV(getCurrentFuncMode())));
        });

        RadioGroup rgFunctionMode = findViewById(R.id.rg_function_mode);
        rgFunctionMode.setOnCheckedChangeListener((group, checkedId) -> {
            sbEv.setProgress(InstaCameraManager.getInstance().getExposureEV(getCurrentFuncMode()));
            tvEV.setText(String.valueOf(InstaCameraManager.getInstance().getExposureEV(getCurrentFuncMode())));
        });
    }

    private int getCurrentFuncMode() {
        RadioGroup rgFunctionMode = findViewById(R.id.rg_function_mode);
        switch (rgFunctionMode.getCheckedRadioButtonId()) {
            case R.id.rb_capture_normal:
            default:
                return InstaCameraManager.FUNCTION_MODE_CAPTURE_NORMAL;
            case R.id.rb_hdr_capturel:
                return InstaCameraManager.FUNCTION_MODE_HDR_CAPTURE;
            case R.id.rb_interval_shooting:
                return InstaCameraManager.FUNCTION_MODE_INTERVAL_SHOOTING;
            case R.id.rb_record_normal:
                return InstaCameraManager.FUNCTION_MODE_RECORD_NORMAL;
            case R.id.rb_hdr_record:
                return InstaCameraManager.FUNCTION_MODE_HDR_RECORD;
            case R.id.rb_bullet_time:
                return InstaCameraManager.FUNCTION_MODE_BULLETTIME;
            case R.id.rb_timelapse:
                return InstaCameraManager.FUNCTION_MODE_TIMELAPSE;
        }
    }

    private void initCameraBeepSwitch() {
        Switch switchCameraBeep = findViewById(R.id.switch_camera_beep);
        switchCameraBeep.setChecked(InstaCameraManager.getInstance().isCameraBeep());
        switchCameraBeep.setOnCheckedChangeListener((buttonView, isChecked) -> {
            InstaCameraManager.getInstance().setCameraBeepSwitch(isChecked);
        });
    }

    private void initCalibrateGyro() {
        findViewById(R.id.btn_calibrate_gyro).setOnClickListener(v -> {
            MaterialDialog dialog = new MaterialDialog.Builder(this)
                    .content("Before calibrate, please stand the camera upright on a stable and level surface.")
                    .negativeText("Cancel")
                    .positiveText("Start")
                    .show();

            dialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(v1 -> {
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setContent("Gyro calibrating...");
                dialog.getActionButton(DialogAction.NEGATIVE).setVisibility(View.GONE);
                dialog.getActionButton(DialogAction.POSITIVE).setVisibility(View.GONE);

                InstaCameraManager.getInstance().calibrateGyro(new ICameraOperateCallback() {
                    @Override
                    public void onSuccessful() {
                        updateDialog("Gyro calibrate successful");
                    }

                    @Override
                    public void onFailed() {
                        updateDialog("Gyro calibrate failed");
                    }

                    @Override
                    public void onCameraConnectError() {
                        updateDialog("Please connect to camera by WIFI first");
                    }

                    private void updateDialog(String content) {
                        dialog.setContent(content);
                        dialog.getActionButton(DialogAction.POSITIVE).setText("OK");
                        dialog.getActionButton(DialogAction.POSITIVE).setVisibility(View.VISIBLE);
                        dialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(v2 -> {
                            dialog.dismiss();
                        });
                    }
                });
            });
        });
    }

    private void initFormatStorage() {
        findViewById(R.id.btn_format_storage).setOnClickListener(v -> {
            MaterialDialog dialog = new MaterialDialog.Builder(this)
                    .content("Confirm format SD card?")
                    .negativeText("Cancel")
                    .positiveText("Sure")
                    .show();
            dialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(v1 -> {
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setContent("Formatting...");
                dialog.getActionButton(DialogAction.NEGATIVE).setVisibility(View.GONE);
                dialog.getActionButton(DialogAction.POSITIVE).setVisibility(View.GONE);

                InstaCameraManager.getInstance().formatStorage(new ICameraOperateCallback() {
                    @Override
                    public void onSuccessful() {
                        updateDialog("Format successful");
                    }

                    @Override
                    public void onFailed() {
                        updateDialog("Format failed");
                    }

                    @Override
                    public void onCameraConnectError() {
                        updateDialog("Please connect to camera first");
                    }

                    private void updateDialog(String content) {
                        dialog.setContent(content);
                        dialog.getActionButton(DialogAction.POSITIVE).setVisibility(View.VISIBLE);
                        dialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(v2 -> {
                            dialog.dismiss();
                        });
                    }
                });
            });
        });
    }

    @Override
    public void onCameraStatusChanged(boolean enabled) {
        super.onCameraStatusChanged(enabled);
        if (!enabled) {
            finish();
        }
    }
}
