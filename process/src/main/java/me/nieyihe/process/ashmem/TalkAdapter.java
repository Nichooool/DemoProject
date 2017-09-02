package me.nieyihe.process.ashmem;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.nieyihe.process.Word;

/**
 * 作者：nichool on 2017/9/2 18:15
 * 邮箱：813825509@qq.com
 */

public class TalkAdapter extends BaseAdapter {

    List<Word> mWordList = new ArrayList<>();

    public void addWord(Word word) {
        mWordList.add(word);
    }

    @Override
    public int getCount() {
        return mWordList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), android.R.layout.simple_list_item_1, null);
        }

        Word word = mWordList.get(position);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(word.author);
        stringBuilder.append(" : ");
        stringBuilder.append(word.word);
        TextView content = (TextView) convertView;
        content.setText(stringBuilder.toString());

        return convertView;
    }
}
