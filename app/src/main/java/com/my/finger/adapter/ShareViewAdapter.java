package com.my.finger.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.my.finger.R;
import com.my.finger.data.ImageItem;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ShareViewAdapter extends BaseAdapter{
    private final String TAG = "KDN_TAG";
    private List<ImageItem> mList;
    private Context mContext;
    public  ImageItem mSelectedDataItem;

    public ShareViewAdapter(Context context, List<ImageItem> list)
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
        TextView rgstYmd;
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
            holder.rgstYmd = (TextView)convertView.findViewById(R.id.rgstYmd);
            holder.text1 = (TextView)convertView.findViewById(R.id.itemText1);
            holder.text2 = (TextView)convertView.findViewById(R.id.itemText2);
            holder.text3 = (TextView)convertView.findViewById(R.id.itemText3);
            // 이미지
            holder.image1 = (ImageView) convertView.findViewById(R.id.itemImage1);
            holder.image2 = (ImageView)convertView.findViewById(R.id.itemImage2);
            holder.image3 = (ImageView)convertView.findViewById(R.id.itemImage3);

            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        mSelectedDataItem = mList.get(position);

        if (mSelectedDataItem.item1 != null) {
            holder.text1.setText(mSelectedDataItem.item1.imageSeqno);

            new DownloadImageTask(holder.image1).execute(mSelectedDataItem.item1.logFileName);
            //holder.image1.setImageBitmap(mSelectedDataItem.image1);
        }
        if (mSelectedDataItem.item2 != null) {
            holder.text2.setText(mSelectedDataItem.item2.imageSeqno);
            new DownloadImageTask(holder.image2).execute(mSelectedDataItem.item2.logFileName);
            //holder.image2.setImageBitmap(mSelectedDataItem.image2);
        }
        if (mSelectedDataItem.item3 != null) {
            holder.text3.setText(mSelectedDataItem.item3.imageSeqno);
            new DownloadImageTask(holder.image2).execute(mSelectedDataItem.item2.logFileName);
            //holder.image3.setImageBitmap(mSelectedDataItem.image3);
        }
        return convertView;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap bmp = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                bmp = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return bmp;
        }
        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
