package com.arashivision.sdk.demo.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.arashivision.sdk.demo.R;
import com.arashivision.sdkcamera.camera.InstaCameraManager;
import com.arashivision.sdkcamera.camera.callback.ILiveStatusListener;
import com.arashivision.sdkcamera.camera.callback.IPreviewStatusListener;
import com.arashivision.sdkcamera.camera.live.LiveParamsBuilder;
import com.arashivision.sdkcamera.camera.resolution.PreviewStreamResolution;
import com.arashivision.sdkmedia.player.capture.CaptureParamsBuilder;
import com.arashivision.sdkmedia.player.capture.InstaCapturePlayerView;
import com.arashivision.sdkmedia.player.listener.PlayerViewListener;
import com.blankj.utilcode.util.BarUtils;

import java.util.List;
import java.util.regex.Pattern;

import androidx.annotation.Nullable;

public class LiveActivity extends BaseObserveCameraActivity implements IPreviewStatusListener, ILiveStatusListener {

    private EditText mEtRtmp;
    private EditText mEtWidth;
    private EditText mEtHeight;
    private EditText mEtFps;
    private EditText mEtBitrate;
    private CheckBox mCbPanorama;
    private ToggleButton mBtnLive;
    private TextView mTvLiveStatus;
    private Spinner mSpinnerResolution;
    private InstaCapturePlayerView mCapturePlayerView;

    private PreviewStreamResolution mCurrentResolution;

    private LinearLayout mLlSetting;
    private Spinner mIvResolution;
    private Spinner mIvFps;
    private Spinner mIvBps;
    private ImageView mIvSetting;
    private ImageView mIvBack;
    private TextView mTvCompany;


//    String url = "rtmp://192.168.0.250:1935/cmcc/stream1";
    String url = "rtmp://47.108.82.225:1935/cmcc/RPI1AOM6";
    private ArrayAdapter<String> adapter;
    private String videoW = "720";
    private String videoH = "1280";
    private String videoFPS = "30";
    private String videoBitrate = "30";
    private boolean isPanorama = true;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);

        BarUtils.setStatusBarColor(this, Color.TRANSPARENT);

        bindViews();
        restoreLiveData();


        // Auto open preview after page gets focus
        List<PreviewStreamResolution> list = InstaCameraManager.getInstance().getSupportedPreviewStreamResolution(true);
        if (!list.isEmpty()) {
            mCurrentResolution = list.get(0);
            InstaCameraManager.getInstance().setPreviewStatusChangedListener(this);
            // mSpinnerResolution的onItemSelected会自动触发，故此处注释掉
//            InstaCameraManager.getInstance().startPreviewStream(mCurrentResolution, true);
        }
    }

    boolean openOrClose = true;

    private void bindViews() {
        mCapturePlayerView = findViewById(R.id.player_capture);
        mCapturePlayerView.setLifecycle(getLifecycle());

        mEtRtmp = findViewById(R.id.et_rtmp);
        mEtWidth = findViewById(R.id.et_width);
        mEtHeight = findViewById(R.id.et_height);
        mEtFps = findViewById(R.id.et_fps);
        mEtBitrate = findViewById(R.id.et_bitrate);
        mCbPanorama = findViewById(R.id.cb_panorama);
        mBtnLive = findViewById(R.id.btn_live);
        mTvLiveStatus = findViewById(R.id.tv_live_status);


        mLlSetting = findViewById(R.id.ll_setting);
        mLlSetting.setVisibility(View.GONE);
        mIvResolution = findViewById(R.id.iv_resolution);
        mIvFps = findViewById(R.id.iv_fps);
        mIvBps = findViewById(R.id.iv_bps);
        mIvSetting = findViewById(R.id.iv_setting);
        mIvSetting.setOnClickListener(v -> {
            if (openOrClose) {
                mLlSetting.setVisibility(View.VISIBLE);
                mIvSetting.setBackgroundResource(R.drawable.ic_setting_true);
                openOrClose = !openOrClose;
            } else {
                mLlSetting.setVisibility(View.GONE);
                openOrClose = !openOrClose;
                mIvSetting.setBackgroundResource(R.drawable.ic_setting);
            }

        });

        mIvBack = findViewById(R.id.iv_back);
        mIvBack.bringToFront();
        mIvBack.setOnClickListener(v -> {
            finish();
        });

        mTvCompany = findViewById(R.id.tv_company);
        mTvCompany.bringToFront();

        mIvBps.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                    default: {
                        videoBitrate = "30";
                        break;
                    }
                    case 1: {
                        videoBitrate = "25";
                        break;
                    }
                    case 2: {
                        videoBitrate = "20";
                        break;
                    }
                    case 3: {
                        videoBitrate = "15";
                        break;
                    }
                    case 4: {
                        videoBitrate = "10";
                        break;
                    }
                    case 5: {
                        videoBitrate = "5";
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mIvFps.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
                    case 1: {
                        videoFPS = "24";
                        break;
                    }
                    case 2: {
                        videoFPS = "15";
                        break;
                    }
                    case 0:
                    default: {
                        videoFPS = "30";
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mIvResolution.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
                    case 0: {
                        videoW = "720";
                        videoH = "1280";
                        break;
                    }
                    case 1: {
                        videoW = "960";
                        videoH = "1280";
                        break;
                    }
                    case 2:
                        videoW = "1080";
                        videoH = "1920";
                        break;
                    case 3:
                    default:
                        videoW = "2160";
                        videoH = "3840";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


        mBtnLive.setEnabled(false);
        mBtnLive.setOnClickListener(v -> {
            if (mBtnLive.isChecked()) {
                saveLiveData();
                mBtnLive.setChecked(checkToStartLive());
                mIvResolution.setEnabled(false);
                mIvFps.setEnabled(false);
                mIvBps.setEnabled(false);
            } else {
                stopLive();
                mIvResolution.setEnabled(true);
                mIvFps.setEnabled(true);
                mIvBps.setEnabled(true);
            }
        });
        mBtnLive.setOnCheckedChangeListener((buttonView, isChecked) -> mSpinnerResolution.setEnabled(!isChecked));

        PreviewStreamResolution stream3840192030fps = PreviewStreamResolution.STREAM_3840_1920_30FPS;
        InstaCameraManager.getInstance().closePreviewStream();
        InstaCameraManager.getInstance().startPreviewStream(stream3840192030fps, true);

        mSpinnerResolution = findViewById(R.id.spinner_resolution);
        ArrayAdapter<PreviewStreamResolution> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
        adapter.addAll(InstaCameraManager.getInstance().getSupportedPreviewStreamResolution(true));
        mSpinnerResolution.setAdapter(adapter);
        mSpinnerResolution.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCurrentResolution = adapter.getItem(position);
                InstaCameraManager.getInstance().closePreviewStream();
                InstaCameraManager.getInstance().startPreviewStream(mCurrentResolution, true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private boolean checkToStartLive() {
        if (InstaCameraManager.getInstance().getCameraConnectedType() == InstaCameraManager.CONNECT_TYPE_USB) {

//

            String rtmp = url;
            String width = videoW;
            String height = videoH;
            String fps = videoFPS;
            String bitrate = videoBitrate;
            if (TextUtils.isEmpty(rtmp) || TextUtils.isEmpty(width) || TextUtils.isEmpty(height)
                    || TextUtils.isEmpty(fps) || TextUtils.isEmpty(bitrate)) {
                Toast.makeText(this, "Please input all parameters", Toast.LENGTH_SHORT).show();
            } else if (!Pattern.matches("(rtmp|rtmps)://([\\w.]+/?)\\S*", rtmp)) {
                Toast.makeText(this, "Rtmp address is not valid", Toast.LENGTH_SHORT).show();
            } else {
                LiveParamsBuilder builder = new LiveParamsBuilder()
                        .setRtmp(rtmp)
                        .setWidth(Integer.parseInt(width))
                        .setHeight(Integer.parseInt(height))
                        .setFps(Integer.parseInt(fps))
                        .setBitrate(Integer.parseInt(bitrate) * 1024 * 1024)
                        .setPanorama(isPanorama);
                InstaCameraManager.getInstance().startLive(builder, this);
                return true;
            }
        } else {
            Toast.makeText(this, "请先使用USB连接相机", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    private void getWH() {


    }

    private void stopLive() {
        InstaCameraManager.getInstance().stopLive();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            // Auto close preview after page loses focus
            InstaCameraManager.getInstance().stopLive();
            InstaCameraManager.getInstance().closePreviewStream();
            InstaCameraManager.getInstance().setPreviewStatusChangedListener(null);
            mCapturePlayerView.destroy();
        }
    }

    @Override
    public void onOpened() {
        // Preview stream is on and can be played
        InstaCameraManager.getInstance().setStreamEncode();
        mCapturePlayerView.setPlayerViewListener(new PlayerViewListener() {
            @Override
            public void onLoadingFinish() {
                mBtnLive.setEnabled(true);
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
                .setCameraSelfie(InstaCameraManager.getInstance().isCameraSelfie())
                .setLive(true)
                .setResolutionParams(mCurrentResolution.width, mCurrentResolution.height, mCurrentResolution.fps);
        return builder;
    }

    @Override
    public void onIdle() {
        // Preview Stopped
        mBtnLive.setEnabled(false);
        mCapturePlayerView.destroy();
        mCapturePlayerView.setKeepScreenOn(false);
    }

    @Override
    public void onLivePushStarted() {
        mTvLiveStatus.setText("推流开始");
    }

    @Override
    public void onLivePushFinished() {
        mBtnLive.setChecked(false);
        mTvLiveStatus.setText("推流结束");
    }

    @Override
    public void onLivePushError() {
        mBtnLive.setChecked(false);
        mTvLiveStatus.setText("推流异常");
    }

    @Override
    public void onLiveFpsUpdate(int fps) {
        mTvLiveStatus.setText("FPS: " + fps);
    }

    @Override
    public void onCameraStatusChanged(boolean enabled) {
        super.onCameraStatusChanged(enabled);
        if (!enabled) {
            mBtnLive.setChecked(false);
            mBtnLive.setEnabled(false);
        }
    }

    private void saveLiveData() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().putString("rtmp", url)
                .putString("width", videoW)
                .putString("height", videoH)
                .putString("fps", videoFPS)
                .putString("bitrate", videoBitrate)
                .putBoolean("panorama", isPanorama)
                .apply();
    }

    private void restoreLiveData() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        mEtRtmp.setText(sp.getString("rtmp", ""));
        mEtWidth.setText(sp.getString("width", ""));
        mEtHeight.setText(sp.getString("height", ""));
        mEtFps.setText(sp.getString("fps", ""));
        mEtBitrate.setText(sp.getString("bitrate", ""));
        mCbPanorama.setChecked(sp.getBoolean("panorama", true));
    }

}
