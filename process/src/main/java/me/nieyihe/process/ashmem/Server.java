package me.nieyihe.process.ashmem;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import me.nieyihe.process.IDemoInterface;
import me.nieyihe.process.Word;

/**
 * 作者：nichool on 2017/9/2 12:02
 * 邮箱：813825509@qq.com
 */

public class Server extends Service {

    private final String SERVER_AUTHOR = "server";
    private final String SERVER_WORD = "写代码";
    private Word mLastWord ;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new IDemoInterface.Stub() {
            @Override
            public Word talk(Word word) throws RemoteException {
                if (word == null) {
                    return null;
                }
                StringBuilder stringBuilder = new StringBuilder();
                if (mLastWord != null && mLastWord.id != word.id) {
                    stringBuilder.append("我刚才在和" + mLastWord.author + "聊天");
                } else {
                    stringBuilder.append(SERVER_WORD);
                }
                mLastWord = word;
                Word reply = new Word();
                reply.author = SERVER_AUTHOR;
                reply.word = stringBuilder.toString();
                return reply;
            }
        };
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
