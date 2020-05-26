package com.my.finger.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ImageView;

import com.my.finger.SendActivity;
import com.my.finger.data.FileDataItem;
import com.my.finger.R;
import com.my.finger.data.ImageItem;

public class ListShareAdapter extends BaseAdapter {
    private final String TAG = "KDN_TAG";
    private List<ImageItem> mList;
    private Context mContext;
    public  ImageItem mSelectedDataItem;

    public ListShareAdapter(Context context, List<ImageItem> list)
    {
        //super();
        this.mContext = context;
        this.mList = list;
    }
    public List<ImageItem> getList() {
        return mList;
    }
    public void setList(List<ImageItem> list)
    {
        this.mList = list;
    }

     @Override
    public int getViewTypeCount() {
        if(getCount() > 0){
            return getCount();
        }else{
            return super.getViewTypeCount();
        }
    }
    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    private class ViewHolder {
        ImageView image1;
        TextView text1;
        ImageView image2;
        TextView text2;
        ImageView image3;
        TextView text3;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
        {
            Log.d(TAG, "getView convertView null...");
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.listview_share, null, true);
            holder.text1 = convertView.findViewById(R.id.itemText1);
            holder.text2 = convertView.findViewById(R.id.itemText2);
            holder.text3 = convertView.findViewById(R.id.itemText3);
            // 이미지
            holder.image1 =  convertView.findViewById(R.id.itemImage1);
            holder.image2 = convertView.findViewById(R.id.itemImage2);
            holder.image3 = convertView.findViewById(R.id.itemImage3);

            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        mSelectedDataItem = mList.get(position);

        if (mSelectedDataItem.item1 != null) {
            holder.text1.setText(mSelectedDataItem.item1.imageSeqno);
            holder.image1.setImageBitmap(mSelectedDataItem.item1.image);
            holder.image1.setTag(mSelectedDataItem.item1.imageSeqno);
            Log.d(TAG, "item1 : " + mSelectedDataItem.item1.imageSeqno);
        }
        if (mSelectedDataItem.item2 != null) {
            holder.text2.setText(mSelectedDataItem.item2.imageSeqno);
            holder.image2.setImageBitmap(mSelectedDataItem.item2.image);
            holder.image2.setTag(mSelectedDataItem.item2.imageSeqno);
            Log.d(TAG, "item2 : " + mSelectedDataItem.item2.imageSeqno);
        }
        if (mSelectedDataItem.item3 != null) {
            holder.text3.setText(mSelectedDataItem.item3.imageSeqno);
            holder.image3.setImageBitmap(mSelectedDataItem.item3.image);
            holder.image3.setTag(mSelectedDataItem.item3.imageSeqno);
            Log.d(TAG, "item3 : " + mSelectedDataItem.item3.imageSeqno);
        }
//
//        holder.image1.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                ImageView selectedImage = (ImageView)v;
//                Log.d(TAG, (String)selectedImage.getTag());
//                ((SendActivity)mContext).setMoveDetailImage((String)selectedImage.getTag());
//            }
//        });
//        holder.image2.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                ImageView selectedImage = (ImageView)v;
//                Log.d(TAG, (String)selectedImage.getTag());
//                ((SendActivity)mContext).setMoveDetailImage((String)selectedImage.getTag());
//            }
//        });
//        holder.image3.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                ImageView selectedImage = (ImageView)v;
//                Log.d(TAG, (String)selectedImage.getTag());
//                ((SendActivity)mContext).setMoveDetailImage((String)selectedImage.getTag());
//            }
//        });

       return convertView;
    }
}
