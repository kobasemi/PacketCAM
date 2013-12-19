package jp.ac.kansai_u.kutc.firefly.packetcam.setting;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * 設定ボタンのリスナー
 * TODO: 簡単なコードで済みそうなら，クラスを抹殺する
 * TODO: ボタンにタグ付けして，全てのボタンのリスナーをこれに纏めるのもあり
 * @author akasaka
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
