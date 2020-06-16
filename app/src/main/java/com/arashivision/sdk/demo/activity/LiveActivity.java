package com.arashivision.sdk.demo.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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

import com.alivc.rtc.AliRtcAuthInfo;
import com.alivc.rtc.AliRtcEngine;
import com.alivc.rtc.AliRtcEngineEventListener;
import com.alivc.rtc.AliRtcEngineNotify;
import com.alivc.rtc.AliRtcRemoteUserInfo;
import com.arashivision.sdk.demo.R;
import com.arashivision.sdk.demo.utils.bean.TokenBean;
import com.arashivision.sdk.demo.utils.net.Api;
import com.arashivision.sdk.demo.utils.net.BaseSubscriber;
import com.arashivision.sdkcamera.camera.InstaCameraManager;
import com.arashivision.sdkcamera.camera.callback.ILiveStatusListener;
import com.arashivision.sdkcamera.camera.callback.IPreviewStatusListener;
import com.arashivision.sdkcamera.camera.live.LiveParamsBuilder;
import com.arashivision.sdkcamera.camera.resolution.PreviewStreamResolution;
import com.arashivision.sdkmedia.player.capture.CaptureParamsBuilder;
import com.arashivision.sdkmedia.player.capture.InstaCapturePlayerView;
import com.arashivision.sdkmedia.player.listener.PlayerViewListener;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.alivc.rtc.AliRtcEngine.AliRtcAudioTrack.AliRtcAudioTrackNo;
import static com.alivc.rtc.AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackBoth;
import static com.alivc.rtc.AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackCamera;
import static com.alivc.rtc.AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackNo;
import static com.alivc.rtc.AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackScreen;
import static org.webrtc.alirtcInterface.ErrorCodeEnum.ERR_ICE_CONNECTION_HEARTBEAT_TIMEOUT;
import static org.webrtc.alirtcInterface.ErrorCodeEnum.ERR_SESSION_REMOVED;

public class LiveActivity extends BaseObserveCameraActivity implements IPreviewStatusListener, ILiveStatusListener {
    private static final String TAG = LiveActivity.class.getName();

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
    private String videoW = "1920";
    private String videoH = "960";
    private String videoFPS = "30";
    private String videoBitrate = "30";
    private boolean isPanorama = true;

    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int PERMISSION_REQ_ID = 0x0002;

    /**
     * SDK提供的对音视频通话处理的引擎类
     */
    private AliRtcEngine mAliRtcEngine;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);

        BarUtils.setStatusBarColor(this, Color.TRANSPARENT);


        bindViews();
        restoreLiveData();

//        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
//                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
//                checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
//            // 初始化引擎以及打开预览界面
//            initRTCEngine();
//        }


        // Auto open preview after page gets focus
        List<PreviewStreamResolution> list = InstaCameraManager.getInstance().getSupportedPreviewStreamResolution(true);
        if (!list.isEmpty()) {
            mCurrentResolution = list.get(0);
            InstaCameraManager.getInstance().setPreviewStatusChangedListener(this);
            // mSpinnerResolution的onItemSelected会自动触发，故此处注释掉
//            InstaCameraManager.getInstance().startPreviewStream(mCurrentResolution, true);
        }
    }

    private void getVoiceToken() {

        Intent intent = getIntent();
        String orderId = intent.getStringExtra("OrderIdA");

        String orderId1 = SPUtils.getInstance().getString("OrderId");


        HashMap<String, Object> params = new HashMap<>();
        params.put("order_id", orderId1);
        params.put("type", "publisher");
        Api.getRetrofit().getToken(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseSubscriber<TokenBean>(this) {

                    @Override
                    public void onStart() {
                        super.onStart();
//                        recyclerView.refreshComplete();
                        Log.e("TAGPZR", "onStart");
                    }

                    @Override
                    public void onCompleted() {
                        super.onCompleted();
//                        recyclerView.refreshComplete();
                        Log.e("TAGPZR", "onCompleted");

                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
//                        showRec(false);
//                        recyclerView.refreshComplete();
                        Log.e("TAGPZR", "onError" + e.toString());

                    }

                    @Override
                    public void onNext(TokenBean bean) {
                        super.onNext(bean);

                        String appid = bean.getData().getAppid();
                        String nonce = bean.getData().getNonce();
                        String gslb = bean.getData().getGslb().get(0).toString();
                        int timestamp = bean.getData().getTimestamp();
                        String token = bean.getData().getToken();
                        String channel = bean.getData().getChannel();
                        String userid = bean.getData().getUserid();

//                        ToastUtils.showLong(channel);

                        joinChannel(appid, nonce, gslb, timestamp, token, channel, userid);
                    }
                });


    }


    private void initRTCEngine() {
        //默认不开启兼容H5
        AliRtcEngine.setH5CompatibleMode(1);
        // 防止初始化过多
        if (mAliRtcEngine == null) {
            //实例化,必须在主线程进行。
            mAliRtcEngine = AliRtcEngine.getInstance(getApplicationContext());
            //设置事件的回调监听
            mAliRtcEngine.setRtcEngineEventListener(mEventListener);
            //设置接受通知事件的回调
            mAliRtcEngine.setRtcEngineNotify(mEngineNotify);
            // 初始化本地视图
//            initLocalView();
            //开启预览
//            startPreview();

            //加入频道
            getVoiceToken();


        }
    }

    private void joinChannel(String appid, String nonce, String gslb, int timestamp, String token, String channel, String userid) {
        if (mAliRtcEngine == null) {
            return;
        }
        //从控制台生成的鉴权信息，具体内容请查阅:https://help.aliyun.com/document_detail/146833.html
        AliRtcAuthInfo userInfo = new AliRtcAuthInfo();
        userInfo.setAppid(appid);
        userInfo.setNonce(nonce);
        userInfo.setGslb(new String[]{gslb});
        userInfo.setTimestamp(timestamp);
        userInfo.setToken(token);
        userInfo.setConferenceId(channel);///
        userInfo.setUserId(userid);
        /*
         *设置自动发布和订阅，只能在joinChannel之前设置
         *参数1    true表示自动发布；false表示手动发布
         *参数2    true表示自动订阅；false表示手动订阅
         */
        mAliRtcEngine.setAutoPublishSubscribe(true, true);
        mAliRtcEngine.setAudioOnlyMode(true);// 纯音频
        // 加入频道，参数1:鉴权信息 参数2:用户名
        mAliRtcEngine.joinChannel(userInfo, "pzr123");/// 用户名设置

    }

    /**
     * 特殊错误码回调的处理方法
     *
     * @param error 错误码
     */
    private void processOccurError(int error) {
        switch (error) {
            case ERR_ICE_CONNECTION_HEARTBEAT_TIMEOUT:
            case ERR_SESSION_REMOVED:
                noSessionExit(error);
                break;
            default:
                break;
        }
    }

    /**
     * 错误处理
     *
     * @param error 错误码
     */
    private void noSessionExit(int error) {
        runOnUiThread(() -> new AlertDialog.Builder(LiveActivity.this)
                .setTitle("ErrorCode : " + error)
                .setMessage("发生错误，请退出房间")
                .setPositiveButton("确定", (dialog, which) -> {
                    dialog.dismiss();
                    onBackPressed();
                })
                .create()
                .show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAliRtcEngine != null) {
            mAliRtcEngine.destroy();
        }
    }

    /**
     * 用户操作回调监听(回调接口都在子线程)
     */
    private AliRtcEngineEventListener mEventListener = new AliRtcEngineEventListener() {

        /**
         * 加入房间的回调
         * @param result 结果码
         */
        @Override
        public void onJoinChannelResult(int result) {
            runOnUiThread(() -> {
                if (result == 0) {
                    showToast("加入频道成功");
                } else {
                    showToast("加入频道失败 错误码: " + result);
                }
            });
        }

        /**
         * 订阅成功的回调
         * @param s userid
         * @param i 结果码
         * @param aliRtcVideoTrack 视频的track
         * @param aliRtcAudioTrack 音频的track
         */
        @Override
        public void onSubscribeResult(String s, int i, AliRtcEngine.AliRtcVideoTrack aliRtcVideoTrack,
                                      AliRtcEngine.AliRtcAudioTrack aliRtcAudioTrack) {
            if (i == 0) {
//                updateRemoteDisplay(s, aliRtcAudioTrack, aliRtcVideoTrack);
            }
        }


        /**
         * 取消的回调
         * @param i 结果码
         * @param s userid
         */
        @Override
        public void onUnsubscribeResult(int i, String s) {
//            updateRemoteDisplay(s, AliRtcAudioTrackNo, AliRtcVideoTrackNo);
        }

        /**
         * 出现错误的回调
         * @param error 错误码
         */
        @Override
        public void onOccurError(int error) {
            //错误处理
            processOccurError(error);
        }
    };

    /**
     * SDK事件通知(回调接口都在子线程)
     */
    private AliRtcEngineNotify mEngineNotify = new AliRtcEngineNotify() {
        /**
         * 远端用户停止发布通知，处于OB（observer）状态
         * @param aliRtcEngine 核心引擎对象
         * @param s userid
         */
        @Override
        public void onRemoteUserUnPublish(AliRtcEngine aliRtcEngine, String s) {
//            updateRemoteDisplay(s, AliRtcAudioTrackNo, AliRtcVideoTrackNo);
        }

        /**
         * 远端用户上线通知
         * @param s userid
         */
        @Override
        public void onRemoteUserOnLineNotify(String s) {
//            addRemoteUser(s);
        }

        /**
         * 远端用户下线通知
         * @param s userid
         */
        @Override
        public void onRemoteUserOffLineNotify(String s) {
//            removeRemoteUser(s);
        }

        /**
         * 远端用户发布音视频流变化通知
         * @param s userid
         * @param aliRtcAudioTrack 音频流
         * @param aliRtcVideoTrack 相机流
         */
        @Override
        public void onRemoteTrackAvailableNotify(String s, AliRtcEngine.AliRtcAudioTrack aliRtcAudioTrack,
                                                 AliRtcEngine.AliRtcVideoTrack aliRtcVideoTrack) {
//            updateRemoteDisplay(s, aliRtcAudioTrack, aliRtcVideoTrack);
        }
    };


    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                showToast("Need permissions " + Manifest.permission.RECORD_AUDIO +
                        "/" + Manifest.permission.CAMERA + "/" + Manifest.permission.WRITE_EXTERNAL_STORAGE);
                finish();
                return;
            }
            initRTCEngine();
        }
    }

    private void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
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
                        videoW = "1920";
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

                if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                        checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
                        checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
                    // 初始化引擎
                    initRTCEngine();
                }

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
//        mAliRtcEngine.muteLocalMic(true);
        if (mAliRtcEngine != null) {
            mAliRtcEngine.destroy();
            mAliRtcEngine = null;
        }

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
