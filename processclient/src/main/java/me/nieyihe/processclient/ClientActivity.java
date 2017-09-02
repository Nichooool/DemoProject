package me.nieyihe.processclient;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import me.nieyihe.process.IDemoInterface;
import me.nieyihe.process.Word;

public class ClientActivity extends AppCompatActivity {

    private final String CLIENT_AUTHOR = "nichool";
    private final String CLIENT_WORD = "干嘛呢?";
    private IDemoInterface mRemote;
    private TextView mCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        mCall = (TextView) findViewById(R.id.activitiy_client_call);
        Intent intent = new Intent();
        intent.setAction("me.nieyihe.talkserver");
        intent.setPackage("me.nieyh.example.demoproject");
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mRemote = IDemoInterface.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mRemote = null;
            }
        }, BIND_AUTO_CREATE);

        mCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRemote == null) {
                    Toast.makeText(ClientActivity.this, "服务器没准备好", Toast.LENGTH_SHORT).show();
                    return;
                }
                Word word = new Word();
                word.author = CLIENT_AUTHOR;
                word.word = CLIENT_WORD;
                word.id = 2;
                try {
                    Word reply = mRemote.talk(word);
                    Toast.makeText(ClientActivity.this, reply.author + ": " + reply.word, Toast.LENGTH_SHORT).show();
                } catch (RemoteException e) {
                    Toast.makeText(ClientActivity.this, "服务器没准备好", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
