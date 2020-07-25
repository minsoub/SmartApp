package com.my.finger.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.my.finger.R;
import com.my.finger.SharePagerActivity;
import com.my.finger.data.ShareDataItem;
import com.my.finger.utils.TouchImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


/**
 * SharePagerActivity class에서 사용하는 Adapter 클래스
 * 사진공유 -> 사진상세 페이지
 */
public class SharePagerAdapter  extends PagerAdapter {
    private final String TAG = "KDN_TAG";
    private Context mContext = null ;
    private ArrayList<ShareDataItem> mList = null;
    private Display mDisplay = null;
    private View mCurrentView = null;

    public SharePagerAdapter(Context context)
    {
        mContext = context;
    }

    public void setDataArray(ArrayList<ShareDataItem> list)
    {
        mList = list;
    }

    public void setDisplay(Display display)
    {
        mDisplay = display;
    }

//    public void setItemIndex(int position) {
//
//        ((ViewPager)mCurrentView).setCurrentItem(position);
//        ((SharePagerActivity)mContext).setCurrentImage(mList.get(position).oriFileName, mList.get(position).imageSeqno, position);
//    }
//
//    public View getCurrentView()
//    {
//        return mCurrentView;
//    }
//    @Override
//    public void setPrimaryItem(ViewGroup container, int position, Object object) {
//        mCurrentView = (View)object;
//        ((SharePagerActivity)mContext).setCurrentImage(mList.get(position).oriFileName, mList.get(position).imageSeqno, position);
//    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = null;
        if (mContext != null)
        {
            // LayoutInflater를 통해 share_pager_detail.xml을 뷰로 생성한다.
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.share_pager_detail, container,false);
            TouchImageView imageView = (TouchImageView) view.findViewById(R.id.previewImage);
            // 이미지 로드 : position으로 로드한다.
            Point size = new Point();
            mDisplay.getSize(size);

            InputStream in = null;
            try {
                //position = position - 1;
                Log.d(TAG, "instantiateItem postion => "+position);
                in = new java.net.URL(mList.get(position).logFileName).openStream();
                Bitmap bmp = BitmapFactory.decodeStream(in);
                //imageView.setImageBitmap(bmp);
                Bitmap bitmap = Bitmap.createScaledBitmap(bmp, size.x, size.y, true);
                imageView.setImageBitmap(bmp);

                // Textbox에 넣을 타이틀과 버튼 명령시 삭제할 키, 삭제이후 index제거를 위한 position을 넘겨준다.
               // ((SharePagerActivity)mContext).setCurrentImage(mList.get(position).oriFileName, mList.get(position).imageSeqno, position);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        container.addView(view);
        return view;
    }

    // Delete a page at a `position`
    public void deletePage(int position)
    {
        // Remove the corresponding item in the data set
        mList.remove(position);
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
        return mList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return (view == (View)o);
    }


}
