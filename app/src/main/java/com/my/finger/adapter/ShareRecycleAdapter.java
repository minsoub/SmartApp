package com.my.finger.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.my.finger.data.ShareMainItem;
import com.my.finger.R;
import java.util.List;

public class ShareRecycleAdapter extends RecyclerView.Adapter<ShareRecycleAdapter.ShareViewHolder> {
    private List<ShareMainItem> mDataList;
    private Context context;

    public ShareRecycleAdapter(Context context, List<ShareMainItem> data)
    {
        this.context = context;
        this.mDataList = data;
    }
    public List<ShareMainItem> getList() {
        return mDataList;
    }
    public void setList(List<ShareMainItem> list)
    {
        this.mDataList = list;
    }
    @NonNull
    @Override
    public ShareViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.share_recycler_main, null);

        return new ShareRecycleAdapter.ShareViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ShareViewHolder shareViewHolder, int i) {
        shareViewHolder.title.setText(mDataList.get(i).rgstYmd);

        ShareItemAdapter adapter = new ShareItemAdapter(context, mDataList.get(i).imageList);
        shareViewHolder.recyclerView.setHasFixedSize(true);
        shareViewHolder.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        shareViewHolder.recyclerView.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public class ShareViewHolder extends RecyclerView.ViewHolder {
        protected TextView title;
        protected RecyclerView recyclerView;

        public ShareViewHolder(View view)
        {
            super(view);
            title = view.findViewById(R.id.rgstYmd);
            this.recyclerView = (RecyclerView)view.findViewById(R.id.listDetailRecyclerView);
        }
    }


}
