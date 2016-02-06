package com.innopolis.greenavatar;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import java.text.SimpleDateFormat;

/**
 * Created by Николай Юшкевич on 2/6/16.
 */
public class Utils {
    protected static SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    protected static SimpleDateFormat hm = new SimpleDateFormat("HH:mm:ss");

    protected static void clearBackStack(FragmentManager manager) {
        if (manager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = manager.getBackStackEntryAt(0);
            manager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }
}
