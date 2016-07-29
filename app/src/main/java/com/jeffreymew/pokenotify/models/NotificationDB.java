package com.jeffreymew.pokenotify.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by mew on 2016-07-26.
 */
public final class NotificationDB extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "NotificationDB.db";
    public static final String TABLE_NAME = "notifications";
    public static final String COLUMN_ENCOUNTER_ID = "encounterid";
    public static final String COLUMN_EXPIRY_TIMESTAMP = "expirytimestamp";
    public static final String TEXT_TYPE = " TEXT";
    public static final String LONG_TYPE = " LONG";

    public NotificationDB(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COLUMN_ENCOUNTER_ID + TEXT_TYPE + "," + COLUMN_EXPIRY_TIMESTAMP + LONG_TYPE + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertNotification(long encounterId, long expiryTimestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ENCOUNTER_ID, encounterId);
        contentValues.put(COLUMN_EXPIRY_TIMESTAMP, expiryTimestamp);
        db.insert(TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean isNotificationShownBefore(long encounterId) {
        SQLiteDatabase db = this.getReadableDatabase();
        //Cursor res = db.query(TABLE_NAME, new String[]{COLUMN_ENCOUNTER_ID}, COLUMN_ENCOUNTER_ID, new String[]{String.valueOf(encounterId)}, null, null, null);
        Cursor res = db.rawQuery("SELECT encounterid FROM notifications WHERE encounterid = ?", new String[]{String.valueOf(encounterId)});
        return res.getCount() != 0;
    }

    public void deleteExpiredNotifications() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.rawQuery("DELETE FROM " + TABLE_NAME + " WHERE encounterid <= ?", new String[]{String.valueOf(System.currentTimeMillis())});
        //db.delete(TABLE_NAME, COLUMN_EXPIRY_TIMESTAMP + " < ? ", new String[]{String.valueOf(currentTime)});
    }
}
