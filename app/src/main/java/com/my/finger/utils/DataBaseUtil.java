package com.my.finger.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseUtil extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "kdnapp.db";
    private static final int DATABASE_VERSION = 1;

    public DataBaseUtil(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 테이블을 Create 한다.
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS tb_files (id integer primary key, filename text, sts integer)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 업그레이드 하려면 여기서 작업을 해야 한다.
        // 스키마 버전 업그레이드 시 응용프로그램의 자료르 어떻게 이전할 것인지를 여기서 구현해야 한다.
        // 적절한 자료 이동 또는 삭제 수행
    }

    @Override
    public void onOpen(SQLiteDatabase db)
    {
        super.onOpen(db);
    }
}
