package com.my.finger.adapter;



import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.my.finger.SendActivity;
import com.my.finger.ShareActivity;
import com.my.finger.data.ImageItem;
import com.my.finger.R;
import com.my.finger.data.ShareMainItem;

import java.util.List;

public class ShareItemAdapter  extends RecyclerView.Adapter<ShareItemAdapter.HorizontalViewHolder> {
    private List<ImageItem> mDataList;
    private Context mContext;
    private final String TAG = "KDN_TAG";

    public ShareItemAdapter(Context context, List<ImageItem> data)
    {
        this.mContext = context;
        this.mDataList = data;
    }

    @NonNull
    @Override
    public HorizontalViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.share_recycler_title_rows, null);

        return new ShareItemAdapter.HorizontalViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HorizontalViewHolder horizontalViewHolder, int i) {
        if (mDataList.get(i).item1 != null) {
            horizontalViewHolder.image1.setImageBitmap(mDataList.get(i).item1.image);
            horizontalViewHolder.text1.setText(mDataList.get(i).item1.imageSeqno);
        }
        if (mDataList.get(i).item2 != null) {
            horizontalViewHolder.image2.setImageBitmap(mDataList.get(i).item2.image);
            horizontalViewHolder.text2.setText(mDataList.get(i).item2.imageSeqno);
        }
        if (mDataList.get(i).item3 != null) {
            horizontalViewHolder.image3.setImageBitmap(mDataList.get(i).item3.image);
            horizontalViewHolder.text3.setText(mDataList.get(i).item3.imageSeqno);
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public class HorizontalViewHolder extends RecyclerView.ViewHolder {
        protected ImageView image1;
        protected TextView text1;
        protected ImageView image2;
        protected TextView text2;
        protected ImageView image3;
        protected TextView text3;

        public HorizontalViewHolder(View view)
        {
            super(view);
            image1 = view.findViewById(R.id.itemImage1);
            text1 = view.findViewById(R.id.itemText1);
            image2 = view.findViewById(R.id.itemImage2);
            text2 = view.findViewById(R.id.itemText2);
            image3 = view.findViewById(R.id.itemImage3);
            text3 = view.findViewById(R.id.itemText3);

            image1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO : process click event.
                    int pos = getAdapterPosition();     // Item Position
                    String imgUrl = mDataList.get(pos).item1.logFileName;
                    Log.d(TAG, "Detail View : " + imgUrl);
                    ((ShareActivity)mContext).setMoveDetailImage(imgUrl, mDataList.get(pos).item1.imageSeqno);
                }
            });
            image2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO : process click event.
                    int pos = getAdapterPosition();     // Item Position
                    String imgUrl = mDataList.get(pos).item2.logFileName;
                    Log.d(TAG, "Detail View : " + imgUrl);
                    ((ShareActivity)mContext).setMoveDetailImage(imgUrl, mDataList.get(pos).item2.imageSeqno);
                }
            });
            image3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO : process click event.
                    int pos = getAdapterPosition();     // Item Position
                    String imgUrl = mDataList.get(pos).item3.logFileName;
                    Log.d(TAG, "Detail View : " + imgUrl);
                    ((ShareActivity)mContext).setMoveDetailImage(imgUrl, mDataList.get(pos).item3.imageSeqno);
                }
            });
        }
    }
}
