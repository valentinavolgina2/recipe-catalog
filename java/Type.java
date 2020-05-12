//CSD 230 Final Project - Valentina Volgina

package edu.lwtech.finalp;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "types")
public class Type {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "text")
    private String mText = "";

    @ColumnInfo(name = "updated")
    private long mUpdateTime;

    public Type() {
        mUpdateTime = System.currentTimeMillis();
    }

    public Type(String text) {
        mText = text;
        mUpdateTime = System.currentTimeMillis();
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public long getUpdateTime() {
        return mUpdateTime;
    }

    public void setUpdateTime(long updateTime) {
        mUpdateTime = updateTime;
    }


}
