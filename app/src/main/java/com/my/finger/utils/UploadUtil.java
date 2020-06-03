package com.my.finger.utils;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * 파일 업로드 유틸 클래스
 */
public class UploadUtil {
    private DataOutputStream dataStream = null;
    static String postUrl = Constant.UPLOAD_URL;
    static String CRLF = "\r\n";
    static String twoHyphens = "--";
    static String boundary = "*****b*o*u*n*d*a*r*y*****";
    private String pictureFileName = null;
    private String empid = null;
    private String deptCd = null;

    private final String TAG = "KDN_TAG";

    public UploadUtil(String id, String deptcd)
    {
        this.empid = id;
        this.deptCd = deptcd;
    }

    public Constant.ReturnCode fileUpload(String pictureFileName)
    {
        this.pictureFileName = pictureFileName;
        File uploadFile = new File(pictureFileName);

        if (uploadFile.exists()) {
            Log.d(TAG, "fileUpload function called..");
            try {
                Log.d(TAG, "URL : " + postUrl);
                Log.d(TAG, "upload file : " + pictureFileName);
                FileInputStream fileInputStream = new FileInputStream(uploadFile);
                URL connectURL = new URL(postUrl);
                HttpURLConnection conn = (HttpURLConnection) connectURL.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");

                conn.setRequestProperty("Connection","Keep-Alive");
                conn.setRequestProperty("Content-Type","multipart/form-data;boundary="+boundary);
                conn.connect();
                dataStream = new DataOutputStream(conn.getOutputStream());

                writeFormField("empid", empid);
                writeFormField("deptCd", deptCd);
                writeFileField("files", pictureFileName, "image/jpg", fileInputStream);
                dataStream.writeBytes(twoHyphens + boundary + twoHyphens + CRLF);

                fileInputStream.close();
                dataStream.flush();
                dataStream.close();
                dataStream = null;

                String response = getResponse(conn);

                Log.d(TAG, response);

                int responseCode = conn.getResponseCode();

                if (response.contains("OK"))
                    return Constant.ReturnCode.http201;
                else
                    // for now assume bad name/password
                    return Constant.ReturnCode.http401;
            } catch (MalformedURLException mue) {
                Log.e(TAG, "error: " + mue.getMessage(), mue);
                return Constant.ReturnCode.http400;
            } catch (IOException ioe) {
                Log.e(TAG, "error: " + ioe.getMessage(), ioe);
                return Constant.ReturnCode.http500;
            } catch (Exception e) {
                Log.e(TAG, "error: " + e.getMessage(), e);
                return Constant.ReturnCode.unknown;
            }
        }else    {
            Log.d(TAG, "파일 존재하지 않음.");
            return Constant.ReturnCode.noPicture;
        }
    }

    /**
     * 서버에서 응답 데이터를 받아온다.
     *
     * @param conn
     * @return
     */
    private String getResponse(HttpURLConnection conn)
    {
        try
        {
            DataInputStream dis = new DataInputStream(conn.getInputStream());
            byte []        data = new byte[1024];
            int             len = dis.read(data, 0, 1024);
            dis.close();
            int responseCode = conn.getResponseCode();
            if (len > 0)
                return new String(data, 0, len);
            else
                return "";
        }
        catch(Exception e)     {
            //System.out.println("AndroidUploader: "+e);
            Log.e(TAG, "AndroidUploader: "+e);
            return "";
        }
    }

    /**
     * write one form field to dataSream
     * @param fieldName
     * @param fieldValue
     */
    private void writeFormField(String fieldName, String fieldValue)  {
        try  {
            dataStream.writeBytes(twoHyphens + boundary + CRLF);
            dataStream.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"" + CRLF);
            dataStream.writeBytes(CRLF);
            dataStream.writeBytes(fieldValue);
            dataStream.writeBytes(CRLF);
        } catch(Exception e)   {
            //System.out.println("AndroidUploader.writeFormField: got: " + e.getMessage());
            Log.e(TAG, "AndroidUploader.writeFormField: " + e.getMessage());
        }
    }

    /**
     * write one file field to dataSream
     * @param fieldName - name of file field
     * @param fieldValue - file name
     * @param type - mime type
     * @param fis - stream of bytes that get sent up
     */
    private void writeFileField(
            String fieldName,
            String fieldValue,
            String type,
            FileInputStream fis)  {
        try {
            // opening boundary line
            dataStream.writeBytes(twoHyphens + boundary + CRLF);
            dataStream.writeBytes("Content-Disposition: form-data; name=\""
                    + fieldName
                    + "\";filename=\""
                    + fieldValue
                    + "\""
                    + CRLF);
            dataStream.writeBytes("Content-Type: " + type +  CRLF);
            dataStream.writeBytes(CRLF);

            // create a buffer of maximum size
            int bytesAvailable = fis.available();
            int maxBufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer = new byte[bufferSize];
            // read file and write it into form...
            int bytesRead = fis.read(buffer, 0, bufferSize);
            while (bytesRead > 0)   {
                dataStream.write(buffer, 0, bufferSize);
                bytesAvailable = fis.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fis.read(buffer, 0, bufferSize);
            }
            // closing CRLF
            dataStream.writeBytes(CRLF);
        }
        catch(Exception e)  {
            //System.out.println("GeoPictureUploader.writeFormField: got: " + e.getMessage());
            Log.e(TAG, "AndroidUploader.writeFormField: got: " + e.getMessage());
        }
    }
}
