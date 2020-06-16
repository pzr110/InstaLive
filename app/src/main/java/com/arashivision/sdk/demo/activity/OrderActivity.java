package com.arashivision.sdk.demo.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.arashivision.sdk.demo.R;
import com.arashivision.sdk.demo.utils.TokenUtils;
import com.arashivision.sdk.demo.utils.adapter.OrderAdapter;
import com.arashivision.sdk.demo.utils.bean.BaseBean;
import com.arashivision.sdk.demo.utils.bean.Data;
import com.arashivision.sdk.demo.utils.bean.OrderBean;
import com.arashivision.sdk.demo.utils.bean.TokenBean;
import com.arashivision.sdk.demo.utils.net.Api;
import com.arashivision.sdk.demo.utils.net.BaseSubscriber;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.arashivision.onestream.AppContextStab.getContext;

public class OrderActivity extends AppCompatActivity {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    private OrderAdapter mAdapter;
    private List<Data> mLists;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        BarUtils.setStatusBarColor(this, Color.TRANSPARENT); // 沉浸式

        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        mRecyclerView = findViewById(R.id.recyclerView);

        initView();
        getInfo(true);
        initListener();

    }

    private void initListener() {

        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                int id = mAdapter.getData().get(position).getId();
                SPUtils.getInstance().put("OrderId", Integer.toString(id));
                Intent intent = new Intent(OrderActivity.this, MainActivity.class);
                intent.putExtra("OrderId", Integer.toString(id));

                startActivity(intent);
//                finish();
//                getVoiceToken();
            }
        });

        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                int id = mAdapter.getData().get(position).getId();

                stopDialog(id);
            }
        });


        /**
         * 下拉刷新
         */
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                if (pageNum > 1) {
//                    pageNum = 1;
//                }

                getInfo(true);

            }
        });

    }

    private void stopDialog(int id) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_exit, null, false);
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();

        Window window = dialog.getWindow();
        //这一句消除白块
        window.setBackgroundDrawable(new BitmapDrawable());
        TextView tv_cancel = view.findViewById(R.id.tv_cancel);
        TextView tv_sure = view.findViewById(R.id.tv_sure);
        tv_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopOrder(id);
                dialog.dismiss();
            }
        });
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        dialog.show();
        dialog.getWindow().setLayout((ScreenUtils.getScreenWidth() / 4 * 3), LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private void stopOrder(int id) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("order_id", id);
        Api.getRetrofit().closeOrder(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseSubscriber<OrderBean>(this) {

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
                    public void onNext(OrderBean bean) {
                        super.onNext(bean);

                        int status = bean.getStatus();
                        if (status == 1) {
                            ToastUtils.showShort("预约已结束");
                        }


//                        int timestamp = bean.getData().getTimestamp();
//                        ToastUtils.showShort(timestamp);
//                        Log.e("AAA", "onNext" + timestamp);

                    }
                });
    }

    private void getVoiceToken() {
//        Intent intent = getIntent();
//        String orderId = intent.getStringExtra("OrderId");


        HashMap<String, Object> params = new HashMap<>();
        params.put("order_id", "3");
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

                        int timestamp = bean.getData().getTimestamp();
                        ToastUtils.showShort(timestamp);
                        Log.e("AAA", "onNext" + timestamp);

//
                    }
                });


    }

    private void getInfo(final boolean mIsRefresh) {

        HashMap<String, Object> params = new HashMap<>();
//        params.put("page", page + "");
        Api.getRetrofit().getOrderList(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseSubscriber<OrderBean>(this) {

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
                        if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }

                    }

                    @Override
                    public void onNext(OrderBean bean) {
                        super.onNext(bean);


                        if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
//
                        List<Data> list = bean.getData();
//
                        if (mIsRefresh) {
                            // 此时是刷新
                            mAdapter.setNewData(list);
                        } else {
                            mAdapter.addData(list);
                        }


                    }
                });

    }

    private void initView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mLists = new ArrayList<>();
        mAdapter = new OrderAdapter(mLists);

        View emptyView = getLayoutInflater().inflate(R.layout.item_empty, null);
        mAdapter.setEmptyView(emptyView);
        mRecyclerView.setAdapter(mAdapter);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.color_666666);//这个方法是设置SwipeRefreshLayout刷新圈颜色
    }
}
