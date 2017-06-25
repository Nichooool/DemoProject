package me.nieyh.example.demoproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import me.nieyh.example.view.CommonBoostAnimView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CommonBoostAnimView commonBoostAnimView = (CommonBoostAnimView) findViewById(R.id.boost_anim_view);
        commonBoostAnimView.setAutoStart();
    }
}
