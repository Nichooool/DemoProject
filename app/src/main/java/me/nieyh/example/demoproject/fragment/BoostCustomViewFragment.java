package me.nieyh.example.demoproject.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import me.nieyh.example.anim.boost.view.CommonBoostAnimView;
import me.nieyh.example.demoproject.R;

/**
 * Created by nieyh on 17-8-22.
 */

public class BoostCustomViewFragment extends BaseFragment {

    @Override
    int retWindowsResId() {
        return R.layout.fragment_boost_custom_view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        CommonBoostAnimView commonBoostAnimView = (CommonBoostAnimView) view.findViewById(R.id.boost_anim_view);
        commonBoostAnimView.setAutoStart();
    }
}
