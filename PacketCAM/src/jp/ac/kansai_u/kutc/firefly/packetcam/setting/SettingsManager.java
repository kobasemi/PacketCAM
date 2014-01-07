package jp.ac.kansai_u.kutc.firefly.packetcam.setting;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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

    /**
     * アクティビティをセットする
     * SettingManagerを使う場合，最初に呼び出す必要がある
     * @param activity メインアクティビティ
     */
    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    /**
     * SharedPreferencesを初期化する
     * setActivity 後に必ず呼び出す必要がある
     */
    public void setSharedPreferences(){
        sp = activity.getSharedPreferences(Name.PRE_FILENAME, Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    /**
     * カメラのフラッシュの設定を保存する
     * @param mode FLASH_ON/OFF/AUTO
     */
    public void setFlash(String mode){
        editor.putString("FLASH", mode);
        editor.commit();
    }

//    /**
//     * カメラの解像度を保存する
//     * @param w 解像度の幅
//     * @param h 解像度の高さ
//     */
//    public void setResolution(int w, int h){
//        editor.putInt("ResW", w);
//        editor.putInt("ResH", h);
//        editor.commit();
//    }

    /**
     * フラッシュの設定を取得する
     * @return FLASH_ON/OFF/AUTO，失敗したらぬるぽ
     */
    public String getFlash(){
        return sp.getString("FLASH", "NULPO");
    }

    /**
     * 解像度の幅を取得する
     * @return 幅，失敗したら-1
     */
    public int getResolutionWidth(){
        return sp.getInt("ResW", -1);
    }

    /**
     * 解像度の高さを取得する
     * @return 高さ，失敗したら-1
     */
    public int getResolutionHeight(){
        return sp.getInt("ResH", -1);
    }

    /**
     * フラッシュの設定を削除する
     */
    public void removeFlash(){
        editor.remove("FLASH");
        editor.commit();
    }

    /**
     * 解像度の設定を削除する
     */
    public void removeResolution(){
        editor.remove("ResW");
        editor.remove("ResH");
        editor.commit();
    }

    /**
     * 全ての設定項目を削除する
     * 恐らく，使う必要はない
     * @deprecated
     */
    public void removeAllPreferences(){
        editor.clear();
        editor.commit();
    }
}
