package me.nieyihe.process;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 作者：nichool on 2017/9/2 17:32
 * 邮箱：813825509@qq.com
 */

public class Word implements Parcelable {
    public String word;
    public String author;
    public long time;
    public int id = 0;

    public Word() {}

    protected Word(Parcel in) {
        word = in.readString();
        author = in.readString();
        time = in.readLong();
        id = in.readInt();
    }

    public static final Creator<Word> CREATOR = new Creator<Word>() {
        @Override
        public Word createFromParcel(Parcel in) {
            return new Word(in);
        }

        @Override
        public Word[] newArray(int size) {
            return new Word[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(word);
        dest.writeString(author);
        dest.writeLong(time);
        dest.writeInt(id);
    }
}
