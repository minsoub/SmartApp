package com.my.finger.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonUtil {
    public static String getCurrentDateFormat()
    {
        Date now = new Date();

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return format.format(now);
    }

    public static String getCurrentDayFormat()
    {
        Date now = new Date();

        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmssSSS");
        return format.format(now);
    }
}
