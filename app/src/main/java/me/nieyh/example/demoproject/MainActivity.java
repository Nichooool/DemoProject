package me.nieyh.example.demoproject;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.nieyh.example.anim.AnimActivity;

public class MainActivity extends FragmentActivity {

    @BindView(R.id.activity_main_list) ListView mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mList.setAdapter(new ArrayAdapter<>(MainActivity.this,android.R.layout.simple_list_item_1, new String[] {
                "加速动画（自定义View）",
                "加速动画（自定义ViewGroup）",
                "清理动画（自定义ViewGroup）"
        }));
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        AnimActivity.startView(AnimActivity.ANIM_TYPE_BOOST_CUSTOM_VIEW, MainActivity.this);
                        break;
                    case 1:
                        AnimActivity.startView(AnimActivity.ANIM_TYPE_BOOST_CUSTOM_VIEW_GROUP, MainActivity.this);
                        break;
                    case 2:
                        AnimActivity.startView(AnimActivity.ANIM_TYPE_CLEAN_TRASH_CUSTOM_VIEW_GROUP, MainActivity.this);
                        break;
                }
            }
        });
    }
}
