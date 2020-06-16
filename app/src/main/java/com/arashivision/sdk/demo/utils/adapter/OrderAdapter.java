package com.arashivision.sdk.demo.utils.adapter;

import androidx.annotation.Nullable;

import com.arashivision.sdk.demo.R;
import com.arashivision.sdk.demo.utils.bean.Data;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

public class OrderAdapter extends BaseQuickAdapter<Data, BaseViewHolder> {

    public OrderAdapter(@Nullable List<Data> data) {
        super(R.layout.item_order, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Data item) {

        helper.setText(R.id.tv_name, "姓名：" + item.getName1() + item.getName2())
                .setText(R.id.tv_phone, "电话：" + item.getTel())
                .setText(R.id.tv_order_time, "预约时间：" + item.getOrder_time())
                .setText(R.id.tv_join_time, "加入时间：" + item.getCreat_time());

        helper.addOnClickListener(R.id.tv_join);
    }
}
