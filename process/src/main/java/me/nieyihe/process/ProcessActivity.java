package me.nieyihe.process;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import me.nieyihe.process.ashmem.TalkAdapter;
import me.nieyihe.process.ashmem.Server;

public class ProcessActivity extends AppCompatActivity {
    private final String CLIENT_AUTHOR = "client";
    private final String CLIENT_WORD = "干嘛呢?";
    private IDemoInterface mRemote;
    private TextView mTalk;
    private ListView mWordList;
    private TalkAdapter mTalkAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);
        mTalk = (TextView) findViewById(R.id.activity_process_talk);
        mWordList = (ListView) findViewById(R.id.activity_process_list);
        mTalkAdapter = new TalkAdapter();
        mWordList.setAdapter(mTalkAdapter);
        Intent intent = new Intent();
        intent.setAction("me.nieyihe.talkserver");
        intent.setPackage("me.nieyh.example.demoproject");
        this.bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mRemote = IDemoInterface.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, Context.BIND_AUTO_CREATE);

        mTalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRemote == null) {
                    Toast.makeText(ProcessActivity.this, "绑定没有完成!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    Word call = new Word();
                    call.author = CLIENT_AUTHOR;
                    call.word = CLIENT_WORD;
                    call.id = 1;
                    Word reply = mRemote.talk(call);
                    mTalkAdapter.addWord(call);
                    mTalkAdapter.addWord(reply);
                    mTalkAdapter.notifyDataSetChanged();
                } catch (RemoteException e) {
                    Toast.makeText(ProcessActivity.this, "失败!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }
}
