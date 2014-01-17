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

		CharSequence[] conf = { "パケットの読み込み", "フラッシュ"};

		final AlertDialog.Builder rootBuilder = new AlertDialog.Builder(activity);
        rootBuilder.setTitle("設定");
        rootBuilder.setItems(conf, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0) {
                    new ReadPcapFileDialog().show(activity);
                }
                if (which == 1) {
                    // フラッシュ
                    CharSequence[] flashStatus = { "強制発光", "OFF" };

                    AlertDialog.Builder flashBuilder = new AlertDialog.Builder (activity);
                    flashBuilder.setTitle ("フラッシュ");
                    flashBuilder.setItems (flashStatus, new DialogInterface.OnClickListener ()
                    {
                        public void onClick (DialogInterface dialog, int which)
                        {
                            Camera.Parameters parameters = DrawCamera.getCamera().getParameters();
                            if (which == 0){
                                // 強制発光処理
                                if(parameters.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_TORCH)){
                                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                                    Toast.makeText (activity, "Flashを強制発光モードにしました", Toast.LENGTH_SHORT).show ();
                                    // 設定ファイルに保存
                                    SharedPreferencesManager.getInstance().setFlashStatus(Camera.Parameters.FLASH_MODE_TORCH);
                                }else{
                                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                                    Toast.makeText(activity, "強制発光に対応していません", Toast.LENGTH_SHORT).show();
                                    // 設定ファイルに保存
                                    SharedPreferencesManager.getInstance().setFlashStatus(Camera.Parameters.FLASH_MODE_ON);
                                }
                            }if (which == 1){
                                // フラッシュOFF処理
                                parameters.setFlashMode (Camera.Parameters.FLASH_MODE_OFF);
                                DrawCamera.getCamera().setParameters (parameters);
                                Toast.makeText (activity, "FlashをOFFにしました", Toast.LENGTH_SHORT).show();
                                // 設定ファイルに保存
                                SharedPreferencesManager.getInstance().setFlashStatus(Camera.Parameters.FLASH_MODE_OFF);
                            }
                            DrawCamera.getCamera().setParameters(parameters);
                        }
                    });
                    flashBuilder.show ();
                }
            }
        });
        rootBuilder.show();
    }
}
