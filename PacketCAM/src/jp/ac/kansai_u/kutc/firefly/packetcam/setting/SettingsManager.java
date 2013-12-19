package jp.ac.kansai_u.kutc.firefly.packetcam.setting;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Name;

/**
 * 設定項目の保存，編集，読出しを行うクラス
 * @author akasaka
 */
public class SettingsManager {
    private static SettingsManager settingsManager = new SettingsManager();
    private SettingsManager(){}

    Activity activity;
    SharedPreferences sp;

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setSharedPreferences(){
        sp = activity.getSharedPreferences(Name.PRE_FILENAME, Context.MODE_PRIVATE);
    }


    public SharedPreferences.Editor getEditor(){
        return sp.edit();
    }

}
