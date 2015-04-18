package com.example.PlanIT;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DBhelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MyDBName.db";
    public static final String TABLE_NAME = "timetable";

    public DBhelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + "(" +
                Constants.ROW_ID + " int, " +
                Constants.SUBJECT + " text, " +
                Constants.TIME_TABLE_NAME + " text );");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }

    public void addTimeTable(String timeTableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        // Inserting dummy value here.
        contentValues.put(Constants.ROW_ID, -1);
        contentValues.put(Constants.SUBJECT, "");
        contentValues.put(Constants.TIME_TABLE_NAME, timeTableName);
        db.insert(TABLE_NAME, null, contentValues);
        db.close();
    }

    public void insertSubject(int rowId, String subject, String timeTableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + Constants.ROW_ID + " = " + Integer.toString(rowId) + " AND " + Constants.TIME_TABLE_NAME + " = '" + timeTableName + "';");
        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.ROW_ID, rowId);
        contentValues.put(Constants.SUBJECT, subject);
        contentValues.put(Constants.TIME_TABLE_NAME, timeTableName);
        db.insert(TABLE_NAME, null, contentValues);
        db.close();
    }

    public ArrayList getTableNames() {
        ArrayList array_list = new ArrayList();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT DISTINCT " + Constants.TIME_TABLE_NAME + " FROM " + TABLE_NAME + ";", null);
        res.moveToFirst();
        while (res.isAfterLast() == false) {
            array_list.add(res.getString(res.getColumnIndex(Constants.TIME_TABLE_NAME)));
            res.moveToNext();
        }
        res.close();
        return array_list;
    }

    public ArrayList getItems(String timeTableName) {
        ArrayList<ContentValues> array_list = new ArrayList();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + Constants.TIME_TABLE_NAME + " = '" + timeTableName + "' ;", null);
        res.moveToFirst();
        while (res.isAfterLast() == false) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Constants.ROW_ID, res.getInt(res.getColumnIndex(Constants.ROW_ID)));
            contentValues.put(Constants.SUBJECT, res.getString(res.getColumnIndex(Constants.SUBJECT)));
            array_list.add(contentValues);
            res.moveToNext();
        }
        res.close();
        return array_list;
    }

    public String getJsonStringForAllData() {
        JSONArray ja = new JSONArray();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_NAME + ";", null);
        res.moveToFirst();
        while (res.isAfterLast() == false) {
            try {
                JSONObject jo = new JSONObject();
                jo.put(Constants.ROW_ID, res.getInt(res.getColumnIndex(Constants.ROW_ID)));
                jo.put(Constants.SUBJECT, res.getString(res.getColumnIndex(Constants.SUBJECT)));
                jo.put(Constants.TIME_TABLE_NAME, res.getString(res.getColumnIndex(Constants.TIME_TABLE_NAME)));
                ja.put(jo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            res.moveToNext();
        }
        JSONObject rootJO = new JSONObject();
        try {
            rootJO.put("CONTENT", ja);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        res.close();
        return rootJO.toString();
    }

    public void insertDataUsingJsonString(String jsonString) {
        SQLiteDatabase db = this.getWritableDatabase();
        // First delete all values.
        db.execSQL("DELETE FROM " + TABLE_NAME + ";");
        // Now add values.
        try {
            JSONObject rootJO = new JSONObject(jsonString);
            JSONArray ja = rootJO.getJSONArray("CONTENT");

            for (int i = 0 ; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);
                ContentValues contentValues = new ContentValues();
                contentValues.put(Constants.ROW_ID, jo.getInt(Constants.ROW_ID));
                contentValues.put(Constants.SUBJECT, jo.getString(Constants.SUBJECT));
                contentValues.put(Constants.TIME_TABLE_NAME, jo.getString(Constants.TIME_TABLE_NAME));
                db.insert(TABLE_NAME, null, contentValues);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        db.close();
    }
}