package com.mohammadsuhail.letschatencrypted;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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

    private static final String KEY_PH_NO = "phone_number";
    Context context;
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        //3rd argument to be passed is CursorFactory instance
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String chatTable = "CREATE TABLE " + TABLE_CHATS + "(" + KEY_PH_NO + " TEXT PRIMARY KEY," + KEY_NAME + " TEXT" + ")";
        String messageTable = "CREATE TABLE " + TABLE_MESSAGES + "(" + KEY_PH_NO + " TEXT," + KEY_MESSAGE + " TEXT," + KEY_MSG_TIME +" TEXT, " + KEY_STATUS +" TEXT" + ")";
        db.execSQL(chatTable);
        db.execSQL(messageTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    void addChat(Chat chat) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, chat.getName()); // Contact Name
        values.put(KEY_PH_NO, chat.getNumber()); // Contact Phone
        db.insert(TABLE_CHATS, null, values);
        db.close();
    }

    void addMessage(Chat chat,Message message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PH_NO,chat.getNumber());
        values.put(KEY_MESSAGE,message.getMessage());
        values.put(KEY_MSG_TIME,message.getTime());
        values.put(KEY_STATUS,message.getStatus());
        db.insert(TABLE_MESSAGES,null,values);
        db.close();
    }

    ArrayList<Chat> getAllChats() {
        ArrayList<Chat> list = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_CHATS;
        SQLiteDatabase db = this.getReadableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Chat chat = new Chat();
                chat.setNumber(cursor.getString(0));
                chat.setName(cursor.getString(1));

                list.add(0,chat);
            } while (cursor.moveToNext());
        }

        return list;
    }


    ArrayList<Message> getMessages(Chat chat) {
        ArrayList<Message> list = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + "( " + " SELECT * FROM "+ TABLE_CHATS + " WHERE "+ KEY_PH_NO + " = " + "\""+ chat.getNumber() + "\"" + " ) " + "NATURAL JOIN " + TABLE_MESSAGES;
        SQLiteDatabase db = this.getReadableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Message message = new Message(cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getString(0));
                list.add(message);
            } while (cursor.moveToNext());
        }
        return list;
    }


    public void deleteChat(Chat chat) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CHATS, KEY_PH_NO + " = ?",
                new String[] { String.valueOf(chat.getNumber()) });
        db.close();
    }

}
