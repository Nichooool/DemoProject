package me.nieyh.example.demoproject;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import me.nieyh.example.view.CommonBoostAnimLayout;
import me.nieyh.example.view.CommonBoostAnimView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_b);
        CommonBoostAnimLayout commonBoostAnimLayout = (CommonBoostAnimLayout) findViewById(R.id.activity_main_b_main);
        commonBoostAnimLayout.startCommonBoostAnim();
//        setContentView(R.layout.activity_main);
//        CommonBoostAnimView commonBoostAnimView = (CommonBoostAnimView) findViewById(R.id.boost_anim_view);
//        commonBoostAnimView.setAutoStart();
    }
}
