package me.nieyh.example.demoproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import me.nieyihe.process.ProcessActivity;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this, ProcessActivity.class));
    }
}
