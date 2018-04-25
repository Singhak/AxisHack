package com.developer.tms.themeansquare.Utility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by ANIL on 23-Apr-18.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "voiceauth";
    private static final String TABLE_AUTH = "voice";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_UUID = "uuid";
    private static final String KEY_PHRASE = "phrase";
    private static final String KEY_ENROLL_COUNT = "enroll_count";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_AUTH + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT UNIQUE,"
                + KEY_UUID + " TEXT," + KEY_PHRASE + " TEXT," +  KEY_ENROLL_COUNT + " INTEGER"+")";
        sqLiteDatabase.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    // Drop older table if existed
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_AUTH);

        // Create tables again
        onCreate(sqLiteDatabase);
    }

    public void addUser(AuthUser authUser){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, authUser.getUser_name());
        values.put(KEY_PHRASE, authUser.getPhrase());
        values.put(KEY_UUID, authUser.getUuid());
        values.put(KEY_ENROLL_COUNT, authUser.getEnroll_count());
        // Inserting Row
        db.insert(TABLE_AUTH, null, values);
        //2nd argument is String containing nullColumnHack
        db.close(); // Closing database connection
    }

    public int updateUser(AuthUser authUser){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, authUser.getUser_name());
        values.put(KEY_UUID, authUser.getUuid());
        values.put(KEY_PHRASE, authUser.getPhrase());
        values.put(KEY_ENROLL_COUNT, authUser.getEnroll_count());
        return db.update(TABLE_AUTH, values, KEY_NAME + " = ?",
                new String[] { String.valueOf(authUser.getUser_name()) });
    }

    public AuthUser getUser(String user_name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_AUTH, new String[] {
                        KEY_NAME, KEY_UUID, KEY_PHRASE, KEY_ENROLL_COUNT }, KEY_NAME + "=?",
                new String[] { user_name }, null, null, null, null);
        Log.d("Profile", "getUser: "+cursor.getCount());
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            AuthUser authUser = new AuthUser(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3));
            Log.d("Profile", "getUser: "+authUser.toString());
            return  authUser;
        }
        return null;
    }
}
