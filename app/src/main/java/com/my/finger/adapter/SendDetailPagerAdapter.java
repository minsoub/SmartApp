package com.my.finger.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.my.finger.ImageDetailPagerActivity;
import com.my.finger.R;
import com.my.finger.SharePagerActivity;
import com.my.finger.data.ShareDataItem;
import com.my.finger.utils.TouchImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class SendDetailPagerAdapter   extends PagerAdapter {
    private Context mContext = null ;
    private ArrayList<String> mFileName = null;
    private ArrayList<String> mFileKey = null;
    private Display mDisplay = null;

    public SendDetailPagerAdapter(Context context)
    {
        mContext = context;
    }

    public void setDataArray(ArrayList<String> name, ArrayList<String> key)
    {
        mFileName = name;
        mFileKey = key;
    }

    public void setDisplay(Display display)
    {
        mDisplay = display;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = null;
        if (mContext != null)
        {
            // LayoutInflater를 통해 share_pager_detail.xml을 뷰로 생성한다.
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.send_pager_detail, container,false);
            TouchImageView imageView = (TouchImageView) view.findViewById(R.id.previewImage);
            // 이미지 로드 : position으로 로드한다.
            Point size = new Point();
            mDisplay.getSize(size);

            Bitmap bitmap = BitmapFactory.decodeFile(mFileName.get(position).toString());
            Bitmap newbitMap = Bitmap.createScaledBitmap(bitmap, size.x, size.y, true);
            imageView.setImageBitmap(newbitMap);
           // ((ImageDetailPagerActivity)mContext).setCurrentImage(mFileName.get(position), mFileKey.get(position), position);
        }
        container.addView(view);
        return view;
    }

    // Delete a page at a `position`
    public void deletePage(int position)
    {
        // Remove the corresponding item in the data set
        mFileName.remove(position);
        mFileKey.remove(position);
        // Notify the adapter that the data set is changed
        notifyDataSetChanged();
    }
    @Override
    public int getItemPosition(Object object){
        return PagerAdapter.POSITION_NONE;
    }
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }

    @Override
    public int getCount() {
        return mFileKey.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return (view == (View)o);
    }
}
