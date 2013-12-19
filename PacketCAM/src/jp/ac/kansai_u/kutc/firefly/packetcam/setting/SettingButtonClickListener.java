package jp.ac.kansai_u.kutc.firefly.packetcam.setting;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * Created by akasaka on 2013/12/17.
 */
public class SettingButtonClickListener implements OnClickListener{
    Activity activity;
    public SettingButtonClickListener(Activity a){
        activity = a;
    }

    @Override
    public void onClick(View v) {
        new SettingDialog().show(activity);
    }
}
