package com.AgaKakje.mindnote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final int version = 4;
    public  static String dbName="Company.db";
    public static final String TABLE_NAME ="images";
    public static final String TABLE_NAME2 ="current";


    public static final String COL1 = "i_id";
    public static final String COL2 = "s_name";
    public static final String COL3 = "i_parent";
    public static final String COL4 = "i_color";
    public static final String COL5 = "i_leftMargin";
    public static final String COL6 = "i_topMargin";
    public static final String COL7 = "i_rightMargin";
    public static final String COL8 = "i_bottomMargin";
    public static final String COL9 = "f_scalediff";
    public static final String COL10 = "f_rotation";
    public static final String COL11 = "i_order";

    public static final String COL21 = "i_id";
    public static final String COL22 = "i_value";
    private static final String CREATE_TABLE="create table if not exists "
            + TABLE_NAME +
            "(" + COL1 + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            +COL2+" TEXT NOT NULL,"
            + COL3 + " INTEGER, "
            + COL4 + " INTEGER, "
            + COL5 + " INTEGER, "
            + COL6 + " INTEGER, "
            + COL7 + " INTEGER, "
            + COL8 + " INTEGER, "
            + COL9 + " REAL, "
            + COL10 + " REAL, "
            + COL11  + " INTEGER );";

    private static final String CREATE_TABLE2="create table if not exists "
            + TABLE_NAME2 +
            "(" + COL21 + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COL22 + " INTEGER);";
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS "+ TABLE_NAME;
    private static final String DROP_TABLE2 = "DROP TABLE IF EXISTS "+ TABLE_NAME2;

    private Context context;

    public DatabaseHelper(Context context) {
        super(context,dbName,null,version);
        context=this.context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        try {
            db.execSQL(CREATE_TABLE);
        } catch (Exception e) {
        }

        try {
            db.execSQL(CREATE_TABLE2);
        } catch (Exception e) {
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE);
        db.execSQL(DROP_TABLE2);
        onCreate(db);
    }

    public void delete(int id){
        SQLiteDatabase db=this.getWritableDatabase();
        db.delete(TABLE_NAME, "i_id=?", new String[]{ String.valueOf(id)});
    }

    public void  truncate_table(){
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
        db.execSQL("VACUUM");
    }

    public String GetNewName(){
        SQLiteDatabase db=this.getWritableDatabase();

        Cursor cursor = db.query(TABLE_NAME2,
                new String[] { COL22 },
                "i_id = 2",
                new String[] {},
                null, null, null);

        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            int name = cursor.getInt( 0);
            name = name + 1;

            ContentValues contentValues= new ContentValues();
            contentValues.put(COL22,name);

            db.update(TABLE_NAME2, contentValues,"i_id=?",new String[] {"2"});

            StringBuilder sb = new StringBuilder();
            String name_str = String.valueOf(name);
            while (sb.length() < 10 - name_str.length()) {
                sb.append('0');
            }
            sb.append(name_str);
            sb.append(".png");
            return sb.toString();

        }

        return "inalid.png";

    }

    public void initdb(){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL21,1); //current
        cv.put(COL22,0);

        long result = db.insert(TABLE_NAME2,null,cv);

        cv = new ContentValues();
        cv.put(COL21,2); //current
        cv.put(COL22,1);

        result = db.insert(TABLE_NAME2,null,cv);

    }

    public void  setCurrent(int current){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues= new ContentValues();
        contentValues.put(COL22,current);

        db.update(TABLE_NAME2, contentValues,"i_id=?",new String[] {"1"});
    }


    public Cursor getCurrentItem(){
        int current = getCurent();

        SQLiteDatabase db=this.getWritableDatabase();

        Cursor cursor = db.query(TABLE_NAME,
                new String[] { COL1, COL11 },
                "i_parent = ?",
                new String[] { String.valueOf(current)},
                null, null, COL11);

        return cursor;

    }

    public int getParrent(){
        int current = getCurent();
        SQLiteDatabase db=this.getWritableDatabase();

        Cursor cursor = db.query(TABLE_NAME,
                new String[] { COL3 },
                "i_id = ?",
                new String[] { String.valueOf( current )},
                null, null, null);

        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            int result = cursor.getInt( 0);

            return result;

        }

        return 0;

    }

    public int getCurent(){
        SQLiteDatabase db=this.getWritableDatabase();

        Cursor cursor = db.query(TABLE_NAME2,
                new String[] { COL22 },
                "i_id = 1",
                new String[] {},
                null, null, null);

        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            int current = cursor.getInt( 0);

            return current;

        }

        return 0;
    }

    public Entity get_entity(int id){
        SQLiteDatabase db=this.getWritableDatabase();

        Cursor cursor = db.query(TABLE_NAME,
                new String[] { COL1,
                        COL2,
                        COL3,
                        COL4,
                        COL5,
                        COL6,
                        COL7,
                        COL8,
                        COL9,
                        COL10,
                        COL11
                },
                "i_id = ? ",
                new String[] {String.valueOf(id)},
                null, null, null);

        if(cursor.getCount() > 0){
            cursor.moveToFirst();

            Entity entity = new Entity( cursor.getInt( 0),
                                        cursor.getString(1),
                                        cursor.getInt(2),
                                        cursor.getInt(3),
                                        cursor.getInt(4),
                                        cursor.getInt(5),
                                        cursor.getInt(6),
                                        cursor.getInt(7),
                                        cursor.getFloat(8),
                                        cursor.getFloat(9),
                                        cursor.getInt(10));

            return entity;

        }

        return new Entity(0,"123",0,0,0,0,0,0,1,0,0);
    }

    public  void modify_Entity(Entity objEnt){

        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL2,objEnt.s_name);
        cv.put(COL3,objEnt.i_parent);
        cv.put(COL4,objEnt.i_color);
        cv.put(COL5,objEnt.i_leftMargin);
        cv.put(COL6,objEnt.i_topMargin);
        cv.put(COL7,objEnt.i_rightMargin);
        cv.put(COL8,objEnt.i_bottomMargin);
        cv.put(COL9,objEnt.f_scalediff);
        cv.put(COL10,objEnt.f_rotation);
        cv.put(COL11,objEnt.i_order);

        db.update(TABLE_NAME, cv,"i_id=?",new String[] { String.valueOf(objEnt.i_id)});
    }

    public long InsertEmployee_existing(Entity objEnt)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL1,objEnt.i_id);
        cv.put(COL2,objEnt.s_name);
        cv.put(COL3,objEnt.i_parent);
        cv.put(COL4,objEnt.i_color);
        cv.put(COL5,objEnt.i_leftMargin);
        cv.put(COL6,objEnt.i_topMargin);
        cv.put(COL7,objEnt.i_rightMargin);
        cv.put(COL8,objEnt.i_bottomMargin);
        cv.put(COL9,objEnt.f_scalediff);
        cv.put(COL10,objEnt.f_rotation);
        cv.put(COL11,objEnt.i_order);


        long result = db.insert(TABLE_NAME,null,cv);

        return result;
        // db.close();


    }

    public long InsertEmployee(Entity objEnt)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL2,objEnt.s_name);
        cv.put(COL3,objEnt.i_parent);
        cv.put(COL4,objEnt.i_color);
        cv.put(COL5,objEnt.i_leftMargin);
        cv.put(COL6,objEnt.i_topMargin);
        cv.put(COL7,objEnt.i_rightMargin);
        cv.put(COL8,objEnt.i_bottomMargin);
        cv.put(COL9,objEnt.f_scalediff);
        cv.put(COL10,objEnt.f_rotation);
        cv.put(COL11,objEnt.i_order);


        long result = db.insert(TABLE_NAME,null,cv);

        return result;
       // db.close();


    }
}
