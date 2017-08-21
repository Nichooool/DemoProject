package me.nieyh.example.demoproject.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import me.nieyh.example.anim.boost.view.CommonBoostAnimLayout;
import me.nieyh.example.demoproject.R;

/**
 * Created by nieyh on 17-8-22.
 */

public class BoostCustomViewGroupFragment extends BaseFragment {

    @Override
    int retWindowsResId() {
        return R.layout.fragment_boost_custom_viewgroup;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        CommonBoostAnimLayout commonBoostAnimLayout = (CommonBoostAnimLayout) view.findViewById(R.id.activity_main_b_main);
        commonBoostAnimLayout.startCommonBoostAnim();
    }
}
