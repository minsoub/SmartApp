package com.my.finger.adapter;

import java.util.ArrayList;
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

public class ListViewAdapter extends BaseAdapter {
    private final String TAG = "KDN_TAG";
    private ArrayList<FileDataItem> mList;
    private Context mContext;
    public  FileDataItem mSelectedDataItem;

    public ListViewAdapter(Context context, ArrayList<FileDataItem> list)
    {
        //super();
        this.mContext = context;
        this.mList = list;
    }
    public ArrayList<FileDataItem> getList() {
        return mList;
    }
    public void setList(ArrayList<FileDataItem> list)
    {
        this.mList = list;
    }

    /**
     * 전체 아이템에 대해서 체크박스를 설정한다.
     *
     * @param checked
     */
    public void setChecked(boolean checked)
    {
        for (int i=0; i<mList.size(); i++)
        {
            FileDataItem item = mList.get(i);
            item.isChecked1 = checked;
            item.isChecked2 = checked;
            item.isChecked3 = checked;
            mList.set(i, item);
        }
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
        ImageView radio1;
        ImageView image2;
        TextView text2;
        ImageView radio2;
        ImageView image3;
        TextView text3;
        ImageView radio3;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
        {
            Log.d(TAG, "getView convertView null...");
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.listview_row, null, true);
            holder.text1 = (TextView)convertView.findViewById(R.id.text1);
            holder.text2 = (TextView)convertView.findViewById(R.id.text2);
            holder.text3 = (TextView)convertView.findViewById(R.id.text3);
            // 이미지
            holder.image1 = (ImageView) convertView.findViewById(R.id.image1);
            holder.image2 = (ImageView)convertView.findViewById(R.id.image2);
            holder.image3 = (ImageView)convertView.findViewById(R.id.image3);

            holder.radio1 = (ImageView) convertView.findViewById(R.id.radio1);
            holder.radio2 = (ImageView)convertView.findViewById(R.id.radio2);
            holder.radio3 = (ImageView)convertView.findViewById(R.id.radio3);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        mSelectedDataItem = mList.get(position);
        //Log.d(TAG, "ListViewAdapter select : " + selectedDataItem.toString());
        if (mSelectedDataItem.text1 != null) {
            holder.text1.setText(mSelectedDataItem.text1);
            holder.image1.setImageBitmap(mSelectedDataItem.image1);
            holder.image1.setTag(mSelectedDataItem.key1);
            holder.radio1.setTag(mSelectedDataItem.key1);
            if (mSelectedDataItem.isChecked1) {
                holder.radio1.setImageResource(R.mipmap.obj_chck_on);
            }else {
                holder.radio1.setImageResource(R.mipmap.obj_chck_off);
            }
        }else {
            holder.radio1.setImageDrawable(null);
        }
        if (mSelectedDataItem.text2 != null) {
            holder.text2.setText(mSelectedDataItem.text2);
            holder.image2.setImageBitmap(mSelectedDataItem.image2);
            holder.image2.setTag(mSelectedDataItem.key2);
            holder.radio2.setTag(mSelectedDataItem.key2);
            if (mSelectedDataItem.isChecked2) {
                holder.radio2.setImageResource(R.mipmap.obj_chck_on);
            }else {
                holder.radio2.setImageResource(R.mipmap.obj_chck_off);
            }
        }else {
            holder.radio2.setImageDrawable(null);
        }
        if (mSelectedDataItem.text3 != null) {
            holder.text3.setText(mSelectedDataItem.text3);
            holder.image3.setImageBitmap(mSelectedDataItem.image3);
            holder.image3.setTag(mSelectedDataItem.key3);
            holder.radio3.setTag(mSelectedDataItem.key3);
            if (mSelectedDataItem.isChecked3) {
                holder.radio3.setImageResource(R.mipmap.obj_chck_on);
            }else {
                holder.radio3.setImageResource(R.mipmap.obj_chck_off);
            }
        }else {
            holder.radio3.setImageDrawable(null);
        }

        holder.image1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ImageView selectedImage = (ImageView)v;
                Log.d(TAG, (String)selectedImage.getTag());
                ((SendActivity)mContext).setMoveDetailImage((String)selectedImage.getTag());
            }
        });
        holder.image2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ImageView selectedImage = (ImageView)v;
                Log.d(TAG, (String)selectedImage.getTag());
                ((SendActivity)mContext).setMoveDetailImage((String)selectedImage.getTag());
            }
        });
        holder.image3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ImageView selectedImage = (ImageView)v;
                Log.d(TAG, (String)selectedImage.getTag());
                ((SendActivity)mContext).setMoveDetailImage((String)selectedImage.getTag());
            }
        });

        holder.radio1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ImageView selectedImage = (ImageView)v;
                Log.d(TAG, (String)selectedImage.getTag());
                FileDataItem selectedItem = mList.get(position);
                if (selectedItem.isChecked1) {
                    selectedImage.setImageResource(R.mipmap.obj_chck_off);
                    selectedItem.isChecked1 = false;
                }else {
                    selectedImage.setImageResource(R.mipmap.obj_chck_on);
                    selectedItem.isChecked1 = true;
                }
                Log.d(TAG, "Option1 position [" + position+"] "+selectedItem.toString());
                mList.set(position, selectedItem);
            }
        });
        holder.radio2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ImageView selectedImage = (ImageView)v;
                FileDataItem selectedItem = mList.get(position);
                Log.d(TAG, (String)selectedImage.getTag());
                if (selectedItem.isChecked2) {
                    selectedImage.setImageResource(R.mipmap.obj_chck_off);
                    selectedItem.isChecked2 = false;
                }else {
                    selectedImage.setImageResource(R.mipmap.obj_chck_on);
                    selectedItem.isChecked2 = true;
                }
                Log.d(TAG, "Option2 position [" + position+"] "+selectedItem.toString());
                mList.set(position, selectedItem);
            }
        });
        holder.radio3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ImageView selectedImage = (ImageView)v;
                FileDataItem selectedItem = mList.get(position);
                Log.d(TAG, (String)selectedImage.getTag());
                if (selectedItem.isChecked3) {
                    selectedImage.setImageResource(R.mipmap.obj_chck_off);
                    selectedItem.isChecked3 = false;
                }else {
                    selectedImage.setImageResource(R.mipmap.obj_chck_on);
                    selectedItem.isChecked3 = true;
                }
                Log.d(TAG, "Option3 position [" + position+"] "+selectedItem.toString());
                mList.set(position, selectedItem);
            }
        });

        return convertView;
    }
}
