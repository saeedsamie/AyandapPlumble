package com.morlunk.ayandap.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class VoiceDatabase extends SQLiteOpenHelper {
  private final static String DB_NAME = "voices.db";
  private final static int DB_VERSION = 1;

  public static final String VOICE_TABLE = "voices";
  public static final String VOICE_ID = "id";
  public static final String USER_ID = "userid";
  public static final String CHAT_ID = "chatid";
  public static final String LENGTH = "length";
  public static final String CONTENT = "content";
  public static final String DATE = "date";

  public VoiceDatabase(Context context) {
    super(context, DB_NAME, null, DB_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase mydb) {
    mydb.execSQL("CREATE TABLE IF NOT EXISTS "+ VOICE_TABLE + " (" +
      VOICE_ID + " INTEGER PRIMARY KEY , " +
      USER_ID + " INTEGER , " +
      CHAT_ID + " INTEGER, " +
      LENGTH + " INTEGER, " +
      DATE + " TEXT, " +
      CONTENT + " TEXT);");
  }

  @Override
  public void onUpgrade(SQLiteDatabase mydb,
                        int oldVersion, int newVersion) {

  }
}