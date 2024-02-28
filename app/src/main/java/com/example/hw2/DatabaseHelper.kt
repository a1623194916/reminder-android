package com.example.hw2

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.hw2.data.detail

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_VERSION = 10
        private const val DATABASE_NAME = "remind.db"
        val COLUMN_ID = "id"
        val TABLE_NAME = "details"
        val COLUMN_TITLE = "title"
        val COLUMN_ISCHECKED = "isChecked"
        val COLUMN_BEGINDATE = "begindate"
        val COLUMN_ENDDATE = "enddate"
        val COLUMN_BEGINTIME = "begintime"
        val COLUMN_ENDTIME = "endtime"
        val COLUMN_PRIORITY = "priority"
        val COLUMN_DESCRIPTION = "description"
        val COLUMN_IMAGEURL = "imageUrl"
        val COLUMN_FILEURL = "fileUrl"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_DETAILS_TABLE = ("CREATE TABLE " +
                TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_ISCHECKED + " INTEGER,"
                + COLUMN_BEGINDATE + " INTEGER,"
                + COLUMN_ENDDATE + " INTEGER,"
                + COLUMN_BEGINTIME + " INTEGER,"
                + COLUMN_ENDTIME + " INTEGER,"
                + COLUMN_PRIORITY + " INTEGER,"
                + COLUMN_IMAGEURL + " TEXT,"
                + COLUMN_FILEURL + " TEXT"
                + ")"
                )
        db.execSQL(CREATE_DETAILS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }


}


// 查询数据库
fun queryDataFromDatabase(context: Context): MutableList<detail> {
    val db = DatabaseHelper(context).writableDatabase
    val DetailList = mutableListOf<detail>()
    // 查询数据
    val cursor = db.query(DatabaseHelper.TABLE_NAME, null, null, null, null, null, null)
    if (cursor.moveToFirst()) {
        do {
            val idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID)//获取列名
            val titleIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TITLE)//获取列名
            val isCheckedIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ISCHECKED)//获取列名
            val priorityIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PRIORITY)//获取列名
            val begindateIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_BEGINDATE)//获取列名
            val enddateIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ENDDATE)//获取列名
            val begintimeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_BEGINTIME)//获取列名
            val endtimeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ENDTIME)//获取列名
            val descriptionIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DESCRIPTION)//获取列名
            val imageUrlIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGEURL)//获取列名
            val fileUrlIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_FILEURL)//获取列名
            if (titleIndex != -1 && isCheckedIndex != -1 && priorityIndex != -1 && begindateIndex != -1 && enddateIndex != -1 && begintimeIndex != -1 && endtimeIndex != -1 && descriptionIndex != -1) {
                val id = cursor.getInt(idIndex)
                val title = cursor.getString(titleIndex)
                val isChecked = cursor.getInt(isCheckedIndex) == 1
                val priority = cursor.getInt(priorityIndex)
                val begindate = cursor.getLong(begindateIndex)
                val enddate = cursor.getLong(enddateIndex)
                val begintime = cursor.getLong(begintimeIndex)
                val endtime = cursor.getLong(endtimeIndex)
                val description = cursor.getString(descriptionIndex)
                val imageUrl = cursor.getString(imageUrlIndex)
                val fileUrl = cursor.getString(fileUrlIndex)
                val detail = detail(
                    id = id,
                    title=title,
                    isChecked = isChecked,
                    priority = priority,
                    begindate = begindate,
                    enddate = enddate,
                    begintime = begintime,
                    endtime = endtime,
                    description = description,
                    imageUrl = imageUrl,
                    fileUrl = fileUrl
                )
                DetailList.add(detail)
            }


        } while (cursor.moveToNext())
    }
    cursor.close()

    return DetailList
}

// 更新数据库
fun updateDatabase(context: Context, detail: detail, id: Int) {
    val db = DatabaseHelper(context).writableDatabase
    val values = ContentValues().apply {
        put(DatabaseHelper.COLUMN_TITLE, detail.title)
        put(DatabaseHelper.COLUMN_ISCHECKED, if (detail.isChecked) 1 else 0)
        put(DatabaseHelper.COLUMN_PRIORITY, detail.priority)
        put(DatabaseHelper.COLUMN_BEGINDATE, detail.begindate)
        put(DatabaseHelper.COLUMN_ENDDATE, detail.enddate)
        put(DatabaseHelper.COLUMN_BEGINTIME, detail.begintime)
        put(DatabaseHelper.COLUMN_ENDTIME, detail.endtime)
        put(DatabaseHelper.COLUMN_DESCRIPTION, detail.description)
        put(DatabaseHelper.COLUMN_IMAGEURL, detail.imageUrl)
        put(DatabaseHelper.COLUMN_FILEURL, detail.fileUrl)
    }
    db.update(
        DatabaseHelper.TABLE_NAME,
        values,
        "${DatabaseHelper.COLUMN_ID} = ?",
        arrayOf(id.toString())
    )
}

// 添加数据到数据库
fun addDataToDatabase(context: Context, detail: detail) {
    val db = DatabaseHelper(context).writableDatabase
    val values = ContentValues().apply {
        put(DatabaseHelper.COLUMN_TITLE, detail.title)
        put(DatabaseHelper.COLUMN_ISCHECKED, if (detail.isChecked) 1 else 0)
        put(DatabaseHelper.COLUMN_PRIORITY, detail.priority)
        put(DatabaseHelper.COLUMN_BEGINDATE, detail.begindate)
        put(DatabaseHelper.COLUMN_ENDDATE, detail.enddate)
        put(DatabaseHelper.COLUMN_BEGINTIME, detail.begintime)
        put(DatabaseHelper.COLUMN_ENDTIME, detail.endtime)
        put(DatabaseHelper.COLUMN_DESCRIPTION, detail.description)
        put(DatabaseHelper.COLUMN_IMAGEURL, detail.imageUrl)
        put(DatabaseHelper.COLUMN_FILEURL, detail.fileUrl)
    }
    val id = db.insert(DatabaseHelper.TABLE_NAME, null, values)
    detail.id = id.toInt()
}

// 根据title等字段查询id
fun queryIdFromDatabase(context: Context, detail: detail): Int {
    val db = DatabaseHelper(context).writableDatabase
    val cursor = db.query(
        DatabaseHelper.TABLE_NAME,
        null,
        "${DatabaseHelper.COLUMN_TITLE} = ? AND ${DatabaseHelper.COLUMN_ISCHECKED} = ?",
        arrayOf(
            detail.title,
            if (detail.isChecked) 1.toString() else 0.toString(),
        ),
        null,
        null,
        null
    )
    if (cursor.moveToFirst()) {
        val idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID)
        if (idIndex != -1) {
            return cursor.getInt(idIndex)
        }
    }
    cursor.close()
    return -1
}

// 删除数据库中的数据
fun deleteDataFromDatabase(context: Context, position: Int) {
    val db = DatabaseHelper(context).writableDatabase
    val detail = queryDataFromDatabase(context)[position]
    val id = queryIdFromDatabase(context, detail)
    db.delete(
        DatabaseHelper.TABLE_NAME,
        "${DatabaseHelper.COLUMN_ID} = ?",
        arrayOf(id.toString())
    )
    Log.d("deleteDataFromDatabase", "deleteDataFromDatabase: $id")
}
