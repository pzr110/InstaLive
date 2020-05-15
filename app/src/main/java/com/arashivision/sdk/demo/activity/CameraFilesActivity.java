package com.arashivision.sdk.demo.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.arashivision.sdk.demo.R;
import com.arashivision.sdk.demo.glide.GlideApp;
import com.arashivision.sdkcamera.camera.InstaCameraManager;
import com.arashivision.sdkmedia.work.WorkUtils;
import com.arashivision.sdkmedia.work.WorkWrapper;
import com.bumptech.glide.Priority;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CameraFilesActivity extends BaseObserveCameraActivity {

    private CameraFileAdapter mCameraFileAdapter;
    private SearchCameraTask mSearchCameraTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_files);

        RecyclerView rvMedia = findViewById(R.id.rv_media);
        rvMedia.setLayoutManager(new GridLayoutManager(this, 3));
        rvMedia.setAdapter(mCameraFileAdapter = new CameraFileAdapter());

        // Asynchronous scanning, WIFI connection scanning speed is fast, USB is slightly slower
        mSearchCameraTask = new SearchCameraTask(this);
        mSearchCameraTask.execute();

        findViewById(R.id.btn_all).setOnClickListener(v -> {
            mCameraFileAdapter.showAll();
        });

        findViewById(R.id.btn_image).setOnClickListener(v -> {
            mCameraFileAdapter.showImages();
        });

        findViewById(R.id.btn_video).setOnClickListener(v -> {
            mCameraFileAdapter.showVideos();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSearchCameraTask != null) {
            mSearchCameraTask.cancel(true);
        }
    }

    @Override
    public void onCameraStatusChanged(boolean enabled) {
        super.onCameraStatusChanged(enabled);
        if (!enabled) {
            if (mSearchCameraTask != null) {
                mSearchCameraTask.cancel(true);
            }
            if (mCameraFileAdapter != null) {
                mCameraFileAdapter.clear();
            }
        }
    }

    private class CameraFileAdapter extends RecyclerView.Adapter<CameraFileAdapter.MyHolder> {

        private List<WorkWrapper> allList = new ArrayList<>();
        private List<WorkWrapper> shownList = new ArrayList<>();

        private void setData(List<WorkWrapper> workList) {
            allList.clear();
            allList.addAll(workList);
            showAll();
        }

        private void showAll() {
            shownList.clear();
            shownList.addAll(allList);
            notifyDataSetChanged();
        }

        private void showImages() {
            shownList.clear();
            for (WorkWrapper workWrapper : allList) {
                if (workWrapper.isPhoto()) {
                    shownList.add(workWrapper);
                }
            }
            notifyDataSetChanged();
        }

        private void showVideos() {
            shownList.clear();
            for (WorkWrapper workWrapper : allList) {
                if (workWrapper.isVideo()) {
                    shownList.add(workWrapper);
                }
            }
            notifyDataSetChanged();
        }

        private void clear() {
            this.allList.clear();
            this.shownList.clear();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycler_item_camera_file, parent, false);
            return new MyHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyHolder holder, int position) {
            WorkWrapper workWrapper = shownList.get(position);
            GlideApp.with(CameraFilesActivity.this)
                    .load(workWrapper)
                    .circleCrop()
                    .placeholder(new ColorDrawable(Color.GRAY))
                    .priority(Priority.HIGH)
                    .into(holder.ivThumbnail);
            holder.itemView.setOnClickListener(v -> {
                // Enter play page
                PlayAndExportActivity.launchActivity(CameraFilesActivity.this, workWrapper.getUrls());
            });
        }

        @Override
        public int getItemCount() {
            return shownList == null ? 0 : shownList.size();
        }

        class MyHolder extends RecyclerView.ViewHolder {
            ImageView ivThumbnail;

            MyHolder(@NonNull View itemView) {
                super(itemView);
                ivThumbnail = itemView.findViewById(R.id.iv_thumbnail);
            }
        }
    }

    private static class SearchCameraTask extends AsyncTask<Void, Void, List<WorkWrapper>> {
        private WeakReference<CameraFilesActivity> activityWeakReference;
        private MaterialDialog mDialog;

        private SearchCameraTask(CameraFilesActivity activity) {
            super();
            activityWeakReference = new WeakReference<>(activity);
            mDialog = new MaterialDialog.Builder(activity)
                    .progress(true, 100)
                    .canceledOnTouchOutside(false)
                    .cancelable(false)
                    .build();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.show();
        }

        @Override
        protected List<WorkWrapper> doInBackground(Void... voids) {
            // Scan all media files of camera and return to WorkWrapper list
            return WorkUtils.getAllCameraWorks(
                    InstaCameraManager.getInstance().getCameraHttpPrefix(),
                    InstaCameraManager.getInstance().getCameraInfoMap(),
                    InstaCameraManager.getInstance().getAllUrlList(),
                    InstaCameraManager.getInstance().getRawUrlList());
        }

        @Override
        protected void onPostExecute(List<WorkWrapper> result) {
            super.onPostExecute(result);
            CameraFilesActivity cameraFilesActivity = activityWeakReference.get();
            if (cameraFilesActivity != null && !isCancelled()) {
                cameraFilesActivity.mCameraFileAdapter.setData(result);
            }
            mDialog.dismiss();
        }
    }
}
