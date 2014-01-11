package jp.ac.kansai_u.kutc.firefly.packetcam.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * 設定項目の保存，編集，読出しを行うシングルトンデザインパターンクラス
 * はじめにActivityをセットすること
 * @author akasaka
 */
public class SharedPreferencesManager {
    /** シングルトン♪ シングルトン♪ 鈴が鳴る〜♪ */
    private static SharedPreferencesManager instance = new SharedPreferencesManager();
    public static SharedPreferencesManager getInstance() { return instance; }

    final static String KEY_EFFECT = "EFFECT";
    final static String KEY_FLASH  = "FLASH";

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    private SharedPreferencesManager(){
        sp = null;
        editor = null;
    }

    /**
     * SharedPreferencesの初期化を行う
     * @param activity MainActivity
     */
    public void init(Activity activity){
        sp = activity.getSharedPreferences(Name.PRE_FILENAME, Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    /**
     * エフェクトの状態を保存する
     * @param status [VISIBLE|INVISIBLE]
     */
    public void setEffectStatus(Enum.VISIBILITY status){
        editor.putString(KEY_EFFECT, status.name());
        editor.commit();
    }

    /**
     * エフェクトの状態を取得する
     * @return Enum.VISIBILITY.[VISIBLE|INVISIBLE]
     */
    public Enum.VISIBILITY getEffectStatus(){
        if(sp.getString(KEY_EFFECT, null).equals(Enum.VISIBILITY.VISIBLE.name()))
            return Enum.VISIBILITY.VISIBLE;
        else
            return Enum.VISIBILITY.INVISIBLE;
    }

    /**
     * エフェクトの状態を削除する
     * @deprecated 使う必要ないよね？
     */
    public void removeEffectStatus(){
        editor.remove(KEY_EFFECT);
        editor.commit();
    }

    /**
     * カメラのフラッシュの設定を保存する
     * @param mode [ON|OFF]
     */
    public void setFlashStatus(String mode){
        editor.putString(KEY_FLASH, mode);
        editor.commit();
    }

    /**
     * フラッシュの設定を取得する
     * @return [ON|OFF|null]
     */
    public String getFlashStatus(){
        return sp.getString(KEY_FLASH, null);
    }

    /**
     * フラッシュの設定を削除する
     * @deprecated 使わないよね？
     */
    public void removeFlashStatus(){
        editor.remove(KEY_FLASH);
        editor.commit();
    }

    /**
     * 全ての設定項目を削除する
     * @deprecated 使う必要ある？
     */
    public void removeAllPreferences(){
        editor.clear();
        editor.commit();
    }
}
