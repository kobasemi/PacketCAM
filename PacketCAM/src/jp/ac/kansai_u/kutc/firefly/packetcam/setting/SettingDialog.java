package jp.ac.kansai_u.kutc.firefly.packetcam.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.widget.Toast;
import jp.ac.kansai_u.kutc.firefly.packetcam.opengl.DrawCamera;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.SharedPreferencesManager;

/**
 * 設定ダイアログに関するクラス
 * @author akasaka
 */
public class SettingDialog{
    final String TAG = "SettingDialog.java";
    Activity activity;

    /**
     * ダイアログを表示する
     * @param a アクティビティ
     */
    public void show(Activity a){
        activity = a;

		CharSequence[] conf = { "パケットの読み込み"};

		final AlertDialog.Builder rootBuilder = new AlertDialog.Builder(activity);
        rootBuilder.setTitle("設定");
        rootBuilder.setItems(conf, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0) {
                    new ReadPcapFileDialog().show(activity);
                }
            }
        });
        rootBuilder.show();
    }
}
