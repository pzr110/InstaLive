package com.arashivision.sdk.demo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arashivision.sdk.demo.R;
import com.arashivision.sdk.demo.TimeFormat;
import com.arashivision.sdkcamera.camera.InstaCameraManager;
import com.arashivision.sdkmedia.export.ExportImageParamsBuilder;
import com.arashivision.sdkmedia.export.ExportUtils;
import com.arashivision.sdkmedia.export.ExportVideoParamsBuilder;
import com.arashivision.sdkmedia.export.IExportCallback;
import com.arashivision.sdkmedia.player.image.ImageParamsBuilder;
import com.arashivision.sdkmedia.player.image.InstaImagePlayerView;
import com.arashivision.sdkmedia.player.listener.PlayerGestureListener;
import com.arashivision.sdkmedia.player.listener.PlayerViewListener;
import com.arashivision.sdkmedia.player.listener.VideoStatusListener;
import com.arashivision.sdkmedia.player.video.InstaVideoPlayerView;
import com.arashivision.sdkmedia.player.video.VideoParamsBuilder;
import com.arashivision.sdkmedia.stitch.StitchUtils;
import com.arashivision.sdkmedia.work.WorkWrapper;

import java.lang.ref.WeakReference;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;

public class PlayAndExportActivity extends BaseObserveCameraActivity implements IExportCallback {

    private static final String WORK_URLS = "CAMERA_FILE_PATH";
    private static final String EXPORT_DIR_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/SDK_DEMO_EXPORT/";

    private InstaImagePlayerView mImagePlayerView;
    private InstaVideoPlayerView mVideoPlayerView;
    private RadioButton mRbNormal;
    private RadioButton mRbFisheye;
    private RadioButton mRbPerspective;
    private RadioButton mRbPlane;
    private Group mGroupProgress;
    private TextView mTvCurrent;
    private TextView mTvTotal;
    private SeekBar mSeekBar;
    private ToggleButton mBtnHDR;

    private WorkWrapper mWorkWrapper;
    private MaterialDialog mExportDialog;
    private int mCurrentExportId = -1;

    // HDR拼接
    private StitchTask mStitchTask;
    private String mHDROutputPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/SDK_DEMO_OSC/generate_hdr_" + System.currentTimeMillis() + ".jpg";
    private boolean mIsStitchHDRSuccessful;

    public static void launchActivity(Context context, String[] urls) {
        Intent intent = new Intent(context, PlayAndExportActivity.class);
        intent.putExtra(WORK_URLS, urls);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_and_export);

        String[] urls = getIntent().getStringArrayExtra(WORK_URLS);
        if (urls == null) {
            finish();
            Toast.makeText(this, "Empty path", Toast.LENGTH_SHORT).show();
            return;
        }

        mWorkWrapper = new WorkWrapper(urls);
        bindViews();
        if (mWorkWrapper.isVideo()) {
            playVideo(false);
        } else {
            playImage(false);
        }

        // HDR stitch
        mBtnHDR = findViewById(R.id.btn_hdr_stitch);
        mBtnHDR.setVisibility(mWorkWrapper.isHDRPhoto() ? View.VISIBLE : View.GONE);
        mBtnHDR.setOnClickListener(v -> {
            if (mWorkWrapper.isHDRPhoto()) {
                if (!mWorkWrapper.isLocalFile()) {
                    Toast.makeText(this, "Please download file to local first", Toast.LENGTH_SHORT).show();
                    mBtnHDR.setChecked(false);
                    return;
                }

                if (mBtnHDR.isChecked()) {
                    mStitchTask = new StitchTask(this);
                    mStitchTask.execute();
                } else {
                    mIsStitchHDRSuccessful = false;
                    playImage(mRbPlane.isChecked());
                }
            } else {
                Toast.makeText(this, "This is not HDR photo", Toast.LENGTH_SHORT).show();
                mBtnHDR.setChecked(false);
            }
        });

        // Export Panorama
        findViewById(R.id.btn_export_original).setOnClickListener(v -> {
            if (mWorkWrapper.isVideo()) {
                exportVideoOriginal();
            } else {
                exportImageOriginal();
            }
            showExportDialog();
        });

        // Export Thumbnail
        findViewById(R.id.btn_export_thumbnai).setOnClickListener(v -> {
            if (mWorkWrapper.isVideo()) {
                exportVideoThumbnail();
            } else {
                exportImageThumbnail();
            }
            showExportDialog();
        });
    }

    private void bindViews() {
        mVideoPlayerView = findViewById(R.id.player_video);
        mImagePlayerView = findViewById(R.id.player_image);
        mGroupProgress = findViewById(R.id.group_progress);
        mTvCurrent = findViewById(R.id.tv_current);
        mTvTotal = findViewById(R.id.tv_total);
        mSeekBar = findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mVideoPlayerView.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mVideoPlayerView.seekTo(seekBar.getProgress());
            }
        });

        mRbNormal = findViewById(R.id.rb_normal);
        mRbFisheye = findViewById(R.id.rb_fisheye);
        mRbPerspective = findViewById(R.id.rb_perspective);
        mRbPlane = findViewById(R.id.rb_plane);
        RadioGroup radioGroup = findViewById(R.id.rg_image_mode);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Need to restart the preview stream when switching between normal and plane
            if (checkedId == R.id.rb_plane) {
                if (mWorkWrapper.isVideo()) {
                    playVideo(true);
                } else {
                    playImage(true);
                }
                mRbFisheye.setEnabled(false);
                mRbPerspective.setEnabled(false);
            } else if (checkedId == R.id.rb_normal) {
                if (!mRbFisheye.isEnabled() || !mRbPerspective.isEnabled()) {
                    if (mWorkWrapper.isVideo()) {
                        playVideo(false);
                    } else {
                        playImage(false);
                    }
                    mRbFisheye.setEnabled(true);
                    mRbPerspective.setEnabled(true);
                } else {
                    // Switch to Normal Mode
                    mImagePlayerView.switchNormalMode();
                    mVideoPlayerView.switchNormalMode();
                }
            } else if (checkedId == R.id.rb_fisheye) {
                // Switch to Fisheye Mode
                mImagePlayerView.switchFisheyeMode();
                mVideoPlayerView.switchFisheyeMode();
            } else if (checkedId == R.id.rb_perspective) {
                // Switch to Perspective Mode
                mImagePlayerView.switchPerspectiveMode();
                mVideoPlayerView.switchPerspectiveMode();
            }
        });
    }

    private void playVideo(boolean isPlaneMode) {
        mGroupProgress.setVisibility(View.VISIBLE);
        mVideoPlayerView.setVisibility(View.VISIBLE);
        mVideoPlayerView.setLifecycle(getLifecycle());
        mVideoPlayerView.setPlayerViewListener(new PlayerViewListener() {
            @Override
            public void onLoadingStatusChanged(boolean isLoading) {
            }

            @Override
            public void onLoadingFinish() {
                Toast.makeText(PlayAndExportActivity.this, "Load finish", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFail(String desc) {
                String toast = "onFail  " + desc;
                Toast.makeText(PlayAndExportActivity.this, toast, Toast.LENGTH_LONG).show();
            }
        });
        mVideoPlayerView.setVideoStatusListener(new VideoStatusListener() {
            @Override
            public void onProgressChanged(long position, long length) {
                mSeekBar.setMax((int) length);
                mSeekBar.setProgress((int) position);
                mTvCurrent.setText(TimeFormat.durationFormat(position));
                mTvTotal.setText(TimeFormat.durationFormat(length));
            }

            @Override
            public void onPlayStateChanged(boolean isPlaying) {
            }

            @Override
            public void onSeekComplete() {
                mVideoPlayerView.resume();
            }

            @Override
            public void onCompletion() {
            }
        });
        mVideoPlayerView.setGestureListener(new PlayerGestureListener() {
            @Override
            public boolean onTap(MotionEvent e) {
                if (mVideoPlayerView.isPlaying()) {
                    mVideoPlayerView.pause();
                } else if (!mVideoPlayerView.isLoading() && !mVideoPlayerView.isSeeking()) {
                    mVideoPlayerView.resume();
                }
                return false;
            }
        });
        VideoParamsBuilder builder = new VideoParamsBuilder();
        if (isPlaneMode) {
            builder.setRenderModelType(VideoParamsBuilder.RENDER_MODE_PLANE_STITCH);
            builder.setScreenRatio(2, 1);
        }
        mVideoPlayerView.prepare(mWorkWrapper, builder);
        mVideoPlayerView.play();
    }

    private void playImage(boolean isPlaneMode) {
        mImagePlayerView.setVisibility(View.VISIBLE);
        mImagePlayerView.setLifecycle(getLifecycle());
        mImagePlayerView.setPlayerViewListener(new PlayerViewListener() {
            @Override
            public void onLoadingStatusChanged(boolean isLoading) {
            }

            @Override
            public void onLoadingFinish() {
                Toast.makeText(PlayAndExportActivity.this, "Load finish", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFail(String desc) {
                String toast = "onFail  " + desc;
                Toast.makeText(PlayAndExportActivity.this, toast, Toast.LENGTH_LONG).show();
            }
        });
        ImageParamsBuilder builder = new ImageParamsBuilder();
        if (isPlaneMode) {
            builder.setRenderModelType(ImageParamsBuilder.RENDER_MODE_PLANE_STITCH);
            builder.setScreenRatio(2, 1);
        }
        if (mIsStitchHDRSuccessful) {
            builder.setUrlForPlay(mHDROutputPath);
        }
        mImagePlayerView.prepare(mWorkWrapper, builder);
        mImagePlayerView.play();
    }

    private void exportVideoOriginal() {
        ExportVideoParamsBuilder builder = new ExportVideoParamsBuilder()
                .setExportMode(ExportUtils.ExportMode.PANORAMA)
                .setCameraType(InstaCameraManager.getInstance().getCameraType())
                .setTargetPath(EXPORT_DIR_PATH + System.currentTimeMillis() + ".mp4");
        // 导出视频对手机性能要求较高，如导出时遇到oom或者app被系统强制杀掉的情况，请自行设置较小宽高
//                .setWidth(2048)
//                .setHeight(1024)
//                .setBitrate(20 * 1024 * 1024);
        mCurrentExportId = ExportUtils.exportVideo(mWorkWrapper, builder, this);
    }

    private void exportImageOriginal() {
        ExportImageParamsBuilder builder = new ExportImageParamsBuilder()
                .setExportMode(ExportUtils.ExportMode.PANORAMA)
                .setCameraType(InstaCameraManager.getInstance().getCameraType())
                .setTargetPath(EXPORT_DIR_PATH + System.currentTimeMillis() + ".jpg");
        if (mBtnHDR.isChecked()) {
            builder.setUrlForExport(mHDROutputPath);
        }
        mCurrentExportId = ExportUtils.exportImage(mWorkWrapper, builder, this);
    }

    private void exportVideoThumbnail() {
        ExportImageParamsBuilder builder = new ExportImageParamsBuilder()
                .setExportMode(ExportUtils.ExportMode.SPHERE)
                .setCameraType(InstaCameraManager.getInstance().getCameraType())
                .setTargetPath(EXPORT_DIR_PATH + System.currentTimeMillis() + ".jpg")
                .setWidth(512)
                .setHeight(512)
                .setFov(mVideoPlayerView.getFov())
                .setDistance(mVideoPlayerView.getDistance())
                .setYaw(mVideoPlayerView.getYaw())
                .setPitch(mVideoPlayerView.getPitch());
        mCurrentExportId = ExportUtils.exportVideoToImage(mWorkWrapper, builder, this);
    }

    private void exportImageThumbnail() {
        ExportImageParamsBuilder builder = new ExportImageParamsBuilder()
                .setExportMode(ExportUtils.ExportMode.SPHERE)
                .setCameraType(InstaCameraManager.getInstance().getCameraType())
                .setTargetPath(EXPORT_DIR_PATH + System.currentTimeMillis() + ".jpg")
                .setWidth(512)
                .setHeight(512)
                .setFov(mImagePlayerView.getFov())
                .setDistance(mImagePlayerView.getDistance())
                .setYaw(mImagePlayerView.getYaw())
                .setPitch(mImagePlayerView.getPitch());
        if (mBtnHDR.isChecked()) {
            builder.setUrlForExport(mHDROutputPath);
        }
        mCurrentExportId = ExportUtils.exportImage(mWorkWrapper, builder, this);
    }

    private void stopExport() {
        if (mCurrentExportId != -1) {
            ExportUtils.stopExport(mCurrentExportId);
            mCurrentExportId = -1;
        }
    }

    private void showExportDialog() {
        if (mExportDialog == null) {
            mExportDialog = new MaterialDialog.Builder(this)
                    .cancelable(false)
                    .canceledOnTouchOutside(false)
                    .positiveText("OK")
                    .neutralText("Stop")
                    .build();
        }
        mExportDialog.setContent("Exporting...");
        mExportDialog.getActionButton(DialogAction.POSITIVE).setVisibility(View.GONE);
        mExportDialog.show();
        mExportDialog.getActionButton(DialogAction.NEUTRAL).setOnClickListener(v -> stopExport());
    }

    @Override
    public void onSuccess() {
        mExportDialog.setContent("Export successful.\nSave directory: " + EXPORT_DIR_PATH);
        mExportDialog.getActionButton(DialogAction.POSITIVE).setVisibility(View.VISIBLE);
        mExportDialog.getActionButton(DialogAction.NEUTRAL).setVisibility(View.GONE);
        mCurrentExportId = -1;
    }

    @Override
    public void onFail() {
        mExportDialog.setContent("Export failed");
        mExportDialog.getActionButton(DialogAction.POSITIVE).setVisibility(View.VISIBLE);
        mExportDialog.getActionButton(DialogAction.NEUTRAL).setVisibility(View.GONE);
        mCurrentExportId = -1;
    }

    @Override
    public void onCancel() {
        mExportDialog.setContent("Export Stopped");
        mExportDialog.getActionButton(DialogAction.POSITIVE).setVisibility(View.VISIBLE);
        mExportDialog.getActionButton(DialogAction.NEUTRAL).setVisibility(View.GONE);
        mCurrentExportId = -1;
    }

    @Override
    public void onProgress(float progress) {
        // callback only when exporting video
        mExportDialog.setContent("Export progress： " + String.format(Locale.CHINA, "%.1f", progress * 100) + "%");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mStitchTask != null) {
            mStitchTask.cancel(true);
        }
        mImagePlayerView.destroy();
        mVideoPlayerView.destroy();
    }

    private static class StitchTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<PlayAndExportActivity> activityWeakReference;
        private MaterialDialog mStitchDialog;

        private StitchTask(PlayAndExportActivity activity) {
            super();
            activityWeakReference = new WeakReference<>(activity);
            mStitchDialog = new MaterialDialog.Builder(activity)
                    .content("HDR Stitching...")
                    .progress(true, 100)
                    .canceledOnTouchOutside(false)
                    .cancelable(false)
                    .build();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mStitchDialog.show();
        }

        @Override
        protected Void doInBackground(Void... aVoid) {
            PlayAndExportActivity stitchActivity = activityWeakReference.get();
            if (stitchActivity != null && !isCancelled()) {
                // Start HDR stitching
                stitchActivity.mIsStitchHDRSuccessful =
                        StitchUtils.generateHDR(stitchActivity.mWorkWrapper, stitchActivity.mHDROutputPath);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            PlayAndExportActivity stitchActivity = activityWeakReference.get();
            if (stitchActivity != null && !isCancelled()) {
                stitchActivity.playImage(stitchActivity.mRbPlane.isChecked());
            }
            mStitchDialog.dismiss();
        }
    }

}
