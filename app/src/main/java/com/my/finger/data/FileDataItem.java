package com.my.finger.data;

import android.graphics.Bitmap;

public class FileDataItem {
    public String text1;
    public String text2;
    public String text3;
    public Bitmap image1;
    public Bitmap image2;
    public Bitmap image3;
    public boolean isChecked1 = false;
    public boolean isChecked2 = false;
    public boolean isChecked3 = false;
    public String file1;
    public String file2;
    public String file3;

    public String toString()
    {
        return  " isChecked1 : " + isChecked1 + " text1 : " + text1 +
                " isChecked2 : " + isChecked2 + " text2 : " + text2 +
                " isChecked3 : " + isChecked3 + " text3 : " + text3 +
                ", image1 : " + file1 + ", image2 : " + file2 + ", image3 : " + file3;
    }
}
