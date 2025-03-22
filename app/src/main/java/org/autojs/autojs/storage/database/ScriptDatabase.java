package org.autojs.autojs.storage.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.autojs.autojs.model.script.ScriptModel;

public class ScriptDatabase extends Database<ScriptModel> {
    private static final int VERSION = 1;
    private static final String NAME = "ASScriptDatabase";

    public ScriptDatabase(Context context) {
        super(new ScriptDatabase.SQLHelper(context), ScriptModel.TABLE);
    }

    @Override
    protected ContentValues asContentValues(ScriptModel model) {
        ContentValues values = new ContentValues();
        values.put("id", model.getId());
        values.put("desc", model.getDesc());
        values.put("name", model.getName());
        values.put("version", model.getVersion());
        values.put("logoUrl", model.getLogoUrl());
        values.put("updateTimestamp", model.getUpdateTimestamp());
        values.put("buildNum", model.getBuildNum());
        return values;
    }

    @Override
    protected ScriptModel createModelFromCursor(Cursor cursor) {
        ScriptModel task = new ScriptModel();
        task.setId(cursor.getInt(0));
        task.setDesc(cursor.getString(1));
        task.setName(cursor.getString(2));
        task.setLogoUrl(cursor.getString(3));
        task.setUpdateTimestamp(cursor.getLong(4));
        task.setVersion(cursor.getString(5));
        task.setBuildNum(cursor.getLong(6));
        return task;
    }


    private static class SQLHelper extends SQLiteOpenHelper {

        public SQLHelper(Context context) {
            super(context, NAME + ".db", null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE `" + ScriptModel.TABLE + "`(" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "`desc` TEXT, " +
                    "`name` TEXT, " +
                    "`logoUrl` TEXT, " +
                    "`updateTimestamp` INT," +
                    "`version` TEXT," +
                    "`buildNum` INT);");
        }


        // 根据数据库版本 更新数据库
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion == 1 && newVersion == 2) {
                db.execSQL("ALTER TABLE " + ScriptModel.TABLE + "\n" +
                        "ADD buildNum INT");
            }
        }
    }
}
