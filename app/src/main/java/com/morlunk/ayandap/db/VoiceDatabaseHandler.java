package com.morlunk.ayandap.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class VoiceDatabaseHandler {
  private SQLiteDatabase mydb;

  public VoiceDatabaseHandler(Context context){
    mydb = new VoiceDatabase(context).getWritableDatabase();
  }

  public void insertToTable(int userid, int chatid,String content,int length,String date){
    ContentValues cv = new ContentValues();
    cv.put(VoiceDatabase.USER_ID, userid);
    cv.put(VoiceDatabase.CHAT_ID, chatid);
    cv.put(VoiceDatabase.LENGTH, length);
    cv.put(VoiceDatabase.CONTENT, content);
    cv.put(VoiceDatabase.DATE, date);

    mydb.insert(VoiceDatabase.VOICE_TABLE, null, cv);
    mydb.close();
  }

//  public void updateAge(int id, int newAge){
//    ContentValues cv = new ContentValues();
//    cv.put(VoiceDatabase.AGE, newAge);
//
//    mydb.update(VoiceDatabase.MYTB, cv,
//      VoiceDatabase.ID + " = ?",
//      new String[] { String.valueOf(id) });
//    mydb.close();
//  }

  public TableData[] getAllTable(){
    Cursor cr = mydb.rawQuery("select * from " + VoiceDatabase.VOICE_TABLE, null);

    TableData[] data = new TableData[cr.getCount()];
    int i = 0;
    while (cr.moveToNext()){
      data[i] = new TableData();
      data[i].userid = cr.getInt(cr.getColumnIndex(VoiceDatabase.USER_ID));
      data[i].chatid = cr.getInt(cr.getColumnIndex(VoiceDatabase.CHAT_ID));
      data[i].length = cr.getInt(cr.getColumnIndex(VoiceDatabase.LENGTH));
      data[i].content = cr.getString(cr.getColumnIndex(VoiceDatabase.CONTENT));
      data[i].date = cr.getString(cr.getColumnIndex(VoiceDatabase.DATE));
    }

    cr.close();
    mydb.close();
    return data;
  }

  public ChatVoices[] getChatVoices(int chatId){
    Cursor cr = mydb.rawQuery("select * from " + VoiceDatabase.VOICE_TABLE + "where chatid = '"+chatId+"'", null);

    ChatVoices[] voices = new ChatVoices[cr.getCount()];
    int i = 0;
    while (cr.moveToNext()){
      voices[i] = new ChatVoices();
      voices[i].userid = cr.getInt(cr.getColumnIndex(VoiceDatabase.USER_ID));
      voices[i].length = cr.getInt(cr.getColumnIndex(VoiceDatabase.LENGTH));
      voices[i].content = cr.getString(cr.getColumnIndex(VoiceDatabase.CONTENT));
      voices[i].date = cr.getString(cr.getColumnIndex(VoiceDatabase.DATE));
    }

    cr.close();
    mydb.close();
    return voices;
  }


  public class TableData{
    public int userid;
    public int chatid;
    public String content;
    public int length;
    public String date;
  }

  public class ChatVoices{
    public int userid;
    public String content;
    public int length;
    public String date;
  }

}
