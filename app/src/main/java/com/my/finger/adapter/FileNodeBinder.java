package com.my.finger.adapter;

import android.view.View;
import android.widget.TextView;

import com.my.finger.data.DeptItem;
import com.my.finger.R;
import tellh.com.recyclertreeview_lib.TreeNode;
import tellh.com.recyclertreeview_lib.TreeViewBinder;

/**
 * Created by tlh on 2016/10/1 :)
 */

public class FileNodeBinder extends TreeViewBinder<FileNodeBinder.ViewHolder> {
    @Override
    public ViewHolder provideViewHolder(View itemView) {
        return new ViewHolder(itemView);
    }

    @Override
    public void bindView(ViewHolder holder, int position, TreeNode node) {
        DeptItem fileNode = (DeptItem) node.getContent();
        holder.tvName.setText(fileNode.deptName);
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_file;
    }

    public class ViewHolder extends TreeViewBinder.ViewHolder {
        public TextView tvName;

        public ViewHolder(View rootView) {
            super(rootView);
            this.tvName = (TextView) rootView.findViewById(R.id.tv_name);
        }
        public TextView getTvName() {
            return tvName;
        }
    }
}