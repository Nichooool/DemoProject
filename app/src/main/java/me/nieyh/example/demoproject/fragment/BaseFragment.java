package me.nieyh.example.demoproject.fragment;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by nieyh on 17-8-22.
 */

public abstract class BaseFragment extends Fragment {

    abstract @LayoutRes int retWindowsResId();

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(retWindowsResId(), container, false);
    }
}
