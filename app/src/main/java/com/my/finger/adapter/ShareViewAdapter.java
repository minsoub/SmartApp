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
import android.widget.ListView;
import android.widget.TextView;

import com.my.finger.R;
import com.my.finger.data.ImageItem;
import com.my.finger.data.ShareMainItem;

import java.io.InputStream;
import java.util.List;

public class ShareViewAdapter extends BaseAdapter{
    private final String TAG = "KDN_TAG";
    private List<ShareMainItem> mList;
    private Context mContext;
    public  ShareMainItem mSelectedDataItem;

    public ShareViewAdapter(Context context, List<ShareMainItem> list)
    {
        //super();
        this.mContext = context;
        this.mList = list;
    }
    public List<ShareMainItem> getList() {
        return mList;
    }
    public void setList(List<ShareMainItem> list)
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
        ListView listView;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
        {
            Log.d(TAG, "getView convertView null...");
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.share_main, null, true);
            holder.rgstYmd = convertView.findViewById(R.id.rgstYmd);
            holder.listView = convertView.findViewById(R.id.listDetailView);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        mSelectedDataItem = mList.get(position);

        Log.d(TAG, "rgstYmd : " + mSelectedDataItem.rgstYmd);
        holder.rgstYmd.setText(mSelectedDataItem.rgstYmd);

        ListView share =  holder.listView;
        ListShareAdapter adapter = new ListShareAdapter(mContext, mSelectedDataItem.imageList);
        share.setAdapter(adapter);

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
