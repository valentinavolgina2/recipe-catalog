//CSD 230 Final Project - Valentina Volgina

package edu.lwtech.finalp;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "recipes",
        foreignKeys = @ForeignKey(entity = Type.class,
                parentColumns = "text",
                childColumns = "type"))
public class Recipe {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long mId;

    @ColumnInfo(name = "text")
    private String mText;

    @ColumnInfo(name = "description")
    private String mDescription;

    @ForeignKey(entity = Type.class, parentColumns = "text", childColumns = "type")
    @ColumnInfo(name = "type")
    private String mType;

    public String getText() {
        return mText;
    }

    public void setId(long id) {
        mId = id;
    }

    public long getId() {
        return mId;
    }

    public void setText(String text) {
        mText = text;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

}
