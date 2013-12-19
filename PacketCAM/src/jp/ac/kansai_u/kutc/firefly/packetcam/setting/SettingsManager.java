package jp.ac.kansai_u.kutc.firefly.packetcam.setting;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Name;

/**
 * 設定項目の保存，編集，読出しを行うシングルトンデザインパターンクラス
 * はじめにActivityをセットすること
 * @author akasaka
 */
public class SettingsManager {
    /** シングルトン♪ シングルトン♪ 鈴が鳴る〜♪ */
    private static SettingsManager instance = new SettingsManager();
    public static SettingsManager getInstance() { return instance; }

    private Activity activity;
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    private SettingsManager(){
        sp = null;
        editor = null;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setSharedPreferences(){
        sp = activity.getSharedPreferences(Name.PRE_FILENAME, Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    public void setFlash(String mode){
        editor.putString("FLASH", mode);
        editor.commit();
    }

    public void setResolution(int w, int h){
        editor.putInt("ResW", w);
        editor.putInt("ResH", h);
        editor.commit();
    }

    public String getFlash(){
        return sp.getString("FLASH", "NULPO");
    }

    public int getResolutionWidth(){
        return sp.getInt("ResW", -1);
    }

    public int getResolutionHeight(){
        return sp.getInt("ResH", -1);
    }

    public int[] getResolution(){
        return new int[]{sp.getInt("ResW", -1), sp.getInt("ResH", -1)};
    }
}
