package me.nieyh.example.anim;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import me.nieyh.example.anim.fragment.BaseFragment;
import me.nieyh.example.anim.fragment.BoostCustomViewFragment;
import me.nieyh.example.anim.fragment.BoostCustomViewGroupFragment;
import me.nieyh.example.anim.fragment.CleanTrashFragment;

public class AnimActivity extends FragmentActivity {

    private static final String ARG_ANIM_TYPE = "arg_anim_type";
    public static final int ANIM_TYPE_CLEAN_TRASH_CUSTOM_VIEW_GROUP = 1;
    public static final int ANIM_TYPE_BOOST_CUSTOM_VIEW_GROUP = 2;
    public static final int ANIM_TYPE_BOOST_CUSTOM_VIEW = 3;

    @IntDef({ANIM_TYPE_BOOST_CUSTOM_VIEW, ANIM_TYPE_BOOST_CUSTOM_VIEW_GROUP, ANIM_TYPE_CLEAN_TRASH_CUSTOM_VIEW_GROUP})
    @Retention(value = RetentionPolicy.SOURCE )
    @interface ANIM_TYPE {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent();
    }

    private void handleIntent() {
        BaseFragment showFragment = null;
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            finish();
            return;
        }
        int type = bundle.getInt(ARG_ANIM_TYPE);
        switch (type) {
            case ANIM_TYPE_CLEAN_TRASH_CUSTOM_VIEW_GROUP:
                showFragment = new CleanTrashFragment();
                break;
            case ANIM_TYPE_BOOST_CUSTOM_VIEW:
                showFragment = new BoostCustomViewFragment();
                break;
            case ANIM_TYPE_BOOST_CUSTOM_VIEW_GROUP:
                showFragment = new BoostCustomViewGroupFragment();
                break;
        }
        if (showFragment != null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(android.R.id.content, showFragment);
            fragmentTransaction.commit();
        }
    }

    public static void startView(@ANIM_TYPE int animType, Context context) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, AnimActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(ARG_ANIM_TYPE, animType);
        context.startActivity(intent);
    }
}
