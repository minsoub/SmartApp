package com.my.finger.utils;

public class Constant {
    public static final String UPLOAD_URL = "http://220.126.231.124:28180/image/fileUpload";
    public static final String LOGIN_URL = "http://220.126.231.124:28180/mLoginProc";
    public static final String IMAGE_URL = "http://220.126.231.124:28180/mobileImageListAjax";
    public static final String DEPT_URL  = "http://220.126.231.124:28180/common/getDeptTreeData";
    public enum ReturnCode { noPicture, unknown, http201, http400, http401, http403, http404, http500};
}
