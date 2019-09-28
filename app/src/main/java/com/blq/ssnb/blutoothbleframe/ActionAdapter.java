package com.blq.ssnb.blutoothbleframe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * ================================================
 * 作者: BLQ_SSNB
 * 日期：2019-09-27
 * 邮箱: blq_ssnb@outlook.com
 * 修改次数: 1
 * 描述:
 *      添加描述
 * ================================================
 * </pre>
 */
public class ActionAdapter extends RecyclerView.Adapter<ActionAdapter.MViewHolder> {
    private List<ActionBean> mActionBeans;

    public ActionAdapter() {
        mActionBeans = new ArrayList<>();
    }

    @NonNull
    @Override
    public ActionAdapter.MViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_action_btn, parent, false);
        return new MViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MViewHolder holder, int position) {
        ActionBean bean = mActionBeans.get(position);
        holder.actionView.setText(bean.getTitle());
        holder.actionView.setOnClickListener(bean.getOnClickListener());
    }

    @Override
    public int getItemCount() {
        return mActionBeans.size();
    }

    public static class MViewHolder extends RecyclerView.ViewHolder {
        TextView actionView;

        public MViewHolder(@NonNull View itemView) {
            super(itemView);
            actionView = itemView.findViewById(R.id.tv_action_btn);
        }
    }

    public void addAction(ActionBean actionBean) {
        int index = mActionBeans.size();
        mActionBeans.add(actionBean);
        notifyItemInserted(index);
    }

    public void replaceData(List<ActionBean> data) {
        if (mActionBeans == data) {
            notifyDataSetChanged();
            return;
        }
        mActionBeans.clear();
        if (data != null) {
            mActionBeans.addAll(data);
        }
        notifyDataSetChanged();
    }
}
