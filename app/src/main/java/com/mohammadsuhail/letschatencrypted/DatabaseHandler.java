package com.mohammadsuhail.letschatencrypted;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;


public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "recentchats";
    private static final String TABLE_CHATS = "chats";
    private static final String TABLE_MESSAGES = "messages";
    private static final String KEY_NAME = "name";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_MSG_TIME = "time";
    private static final String KEY_STATUS = "status";
    private static final String KEY_UNREAD = "unread";
    private static final String KEY_IMAGEURL = "imageurl";

    private static final String KEY_PH_NO = "phone_number";
    Context context;

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        //3rd argument to be passed is CursorFactory instance
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String chatTable = "CREATE TABLE " + TABLE_CHATS + "(" + KEY_PH_NO + " TEXT PRIMARY KEY," + KEY_NAME + " TEXT," + KEY_IMAGEURL + " TEXT," + KEY_UNREAD + " INTEGER" + ")";
        String messageTable = "CREATE TABLE " + TABLE_MESSAGES + "(" + KEY_PH_NO + " TEXT," + KEY_MESSAGE + " TEXT," + KEY_MSG_TIME + " TEXT, " + KEY_STATUS + " TEXT" + ")";
        db.execSQL(chatTable);
        db.execSQL(messageTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    void addChat(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, contact.getName()); // Contact Name
        values.put(KEY_PH_NO, contact.getNumber()); // Contact Phone
        values.put(KEY_IMAGEURL, contact.getImage());
        values.put(KEY_UNREAD, contact.getUnread());
        db.insert(TABLE_CHATS, null, values);
        db.close();
    }

    void addMessage(Contact contact, Message message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PH_NO, contact.getNumber());
        values.put(KEY_MESSAGE, message.getMessage());
        values.put(KEY_MSG_TIME, message.getTime());
        values.put(KEY_STATUS, message.getStatus());
        db.insert(TABLE_MESSAGES, null, values);
        db.close();
    }

    ArrayList<Contact> getAllChats() {
        ArrayList<Contact> list = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_CHATS;
        SQLiteDatabase db = this.getReadableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact();
                contact.setNumber(cursor.getString(0));
                contact.setName(cursor.getString(1));
                contact.setImage(cursor.getString(2));
                contact.setUnread(cursor.getInt(3));
                list.add(0, contact);
            } while (cursor.moveToNext());
        }

        return list;
    }


    ArrayList<Message> getMessages(Contact contact) {
        ArrayList<Message> list = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + "( " + " SELECT * FROM " + TABLE_CHATS + " WHERE " + KEY_PH_NO + " = " + "\"" + contact.getNumber() + "\"" + " ) " + "NATURAL JOIN " + TABLE_MESSAGES;
        SQLiteDatabase db = this.getReadableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {  // M T S N SI
                /*
                    2 - image
                    3 -  unread
                    4 - MSG
                    0 - Number
                    5 - Time
                    1 - name
                    6 - Status
                */
                Message message = new Message(cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(0),cursor.getString(2));
                list.add(message);
            } while (cursor.moveToNext());
        }
        return list;
    }


    public void deleteChat(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CHATS, KEY_PH_NO + " = ?",
                new String[]{String.valueOf(contact.getNumber())});
        db.close();
    }

    public void removeUnread(Contact contact) {
        ContentValues data=new ContentValues();
        SQLiteDatabase db = this.getWritableDatabase();
        data.put(KEY_PH_NO,contact.getNumber());
        data.put(KEY_NAME,contact.getName());
        data.put(KEY_IMAGEURL,contact.getImage());
        data.put(KEY_UNREAD,0);
        db.update(TABLE_CHATS, data, KEY_PH_NO + " = ?",
                new String[]{String.valueOf(contact.getNumber())});
        db.close();
    }

    public void dropTables() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_CHATS);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_MESSAGES);
        db.close();
    }
}
