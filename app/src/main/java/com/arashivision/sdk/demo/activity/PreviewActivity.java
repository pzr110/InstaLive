package com.arashivision.sdk.demo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.arashivision.sdk.demo.R;
import com.arashivision.sdkcamera.camera.InstaCameraManager;
import com.arashivision.sdkcamera.camera.callback.IPreviewStatusListener;
import com.arashivision.sdkcamera.camera.resolution.PreviewStreamResolution;
import com.arashivision.sdkmedia.player.capture.CaptureParamsBuilder;
import com.arashivision.sdkmedia.player.capture.InstaCapturePlayerView;
import com.arashivision.sdkmedia.player.listener.PlayerViewListener;

import androidx.annotation.Nullable;

public class PreviewActivity extends BaseObserveCameraActivity implements IPreviewStatusListener {

    private InstaCapturePlayerView mCapturePlayerView;
    private ToggleButton mBtnSwitch;
    private RadioButton mRbNormal;
    private RadioButton mRbFisheye;
    private RadioButton mRbPerspective;
    private RadioButton mRbPlane;
    private Spinner mSpinnerResolution;

    private PreviewStreamResolution mCurrentResolution;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        bindViews();

        // Auto open preview after page gets focus
        InstaCameraManager.getInstance().setPreviewStatusChangedListener(this);
        // mSpinnerResolution的onItemSelected会自动触发开启预览，故此处注释掉
//        InstaCameraManager.getInstance().startPreviewStream();
    }

    private void bindViews() {
        mCapturePlayerView = findViewById(R.id.player_capture);
        mCapturePlayerView.setLifecycle(getLifecycle());

        mBtnSwitch = findViewById(R.id.btn_switch);
        mBtnSwitch.setOnClickListener(v -> {
            if (mBtnSwitch.isChecked()) {
                if (mCurrentResolution == null) {
                    InstaCameraManager.getInstance().startPreviewStream();
                } else {
                    InstaCameraManager.getInstance().startPreviewStream(mCurrentResolution);
                }
            } else {
                InstaCameraManager.getInstance().closePreviewStream();
            }
        });

        mRbNormal = findViewById(R.id.rb_normal);
        mRbFisheye = findViewById(R.id.rb_fisheye);
        mRbPerspective = findViewById(R.id.rb_perspective);
        mRbPlane = findViewById(R.id.rb_plane);
        RadioGroup radioGroup = findViewById(R.id.rg_preview_mode);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Need to restart the preview stream when switching between normal and plane
            if (checkedId == R.id.rb_plane) {
                InstaCameraManager.getInstance().closePreviewStream();
                if (mCurrentResolution == null) {
                    InstaCameraManager.getInstance().startPreviewStream();
                } else {
                    InstaCameraManager.getInstance().startPreviewStream(mCurrentResolution);
                }
                mRbFisheye.setEnabled(false);
                mRbPerspective.setEnabled(false);
            } else if (checkedId == R.id.rb_normal) {
                if (!mRbFisheye.isEnabled() || !mRbPerspective.isEnabled()) {
                    InstaCameraManager.getInstance().closePreviewStream();
                    if (mCurrentResolution == null) {
                        InstaCameraManager.getInstance().startPreviewStream();
                    } else {
                        InstaCameraManager.getInstance().startPreviewStream(mCurrentResolution);
                    }
                    mRbFisheye.setEnabled(true);
                    mRbPerspective.setEnabled(true);
                } else {
                    // Switch to Normal Mode
                    mCapturePlayerView.switchNormalMode();
                }
            } else if (checkedId == R.id.rb_fisheye) {
                // Switch to Fisheye Mode
                mCapturePlayerView.switchFisheyeMode();
            } else if (checkedId == R.id.rb_perspective) {
                // Switch to Perspective Mode
                mCapturePlayerView.switchPerspectiveMode();
            }
        });

        mSpinnerResolution = findViewById(R.id.spinner_resolution);
        ArrayAdapter<PreviewStreamResolution> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
        adapter.addAll(InstaCameraManager.getInstance().getSupportedPreviewStreamResolution(false));
        mSpinnerResolution.setAdapter(adapter);
        mSpinnerResolution.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCurrentResolution = adapter.getItem(position);
                InstaCameraManager.getInstance().closePreviewStream();
                InstaCameraManager.getInstance().startPreviewStream(mCurrentResolution);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            // Auto close preview after page loses focus
            InstaCameraManager.getInstance().setPreviewStatusChangedListener(null);
            InstaCameraManager.getInstance().closePreviewStream();
            mCapturePlayerView.destroy();
        }
    }

    @Override
    public void onOpening() {
        // Preview Opening
        mBtnSwitch.setChecked(true);
    }

    @Override
    public void onOpened() {
        // Preview stream is on and can be played
        InstaCameraManager.getInstance().setStreamEncode();
        mCapturePlayerView.setPlayerViewListener(new PlayerViewListener() {
            @Override
            public void onLoadingFinish() {
                InstaCameraManager.getInstance().setPipeline(mCapturePlayerView.getPipeline());
            }
        });
        mCapturePlayerView.prepare(createParams());
        mCapturePlayerView.play();
        mCapturePlayerView.setKeepScreenOn(true);
    }

    private CaptureParamsBuilder createParams() {
        CaptureParamsBuilder builder = new CaptureParamsBuilder()
                .setCameraType(InstaCameraManager.getInstance().getCameraType())
                .setMediaOffset(InstaCameraManager.getInstance().getMediaOffset())
                .setCameraSelfie(InstaCameraManager.getInstance().isCameraSelfie());
        if (mCurrentResolution != null) {
            builder.setResolutionParams(mCurrentResolution.width, mCurrentResolution.height, mCurrentResolution.fps);
        }
        if (mRbPlane.isChecked()) {
            // Plane Mode
            builder.setRenderModelType(CaptureParamsBuilder.RENDER_MODE_PLANE_STITCH)
                    .setScreenRatio(2, 1);
        } else {
            // Normal Mode
            builder.setRenderModelType(CaptureParamsBuilder.RENDER_MODE_AUTO);
        }
        return builder;
    }

    @Override
    public void onIdle() {
        // Preview Stopped
        mCapturePlayerView.destroy();
        mCapturePlayerView.setKeepScreenOn(false);
    }

    @Override
    public void onError() {
        // Preview Failed
        mBtnSwitch.setChecked(false);
    }

}
