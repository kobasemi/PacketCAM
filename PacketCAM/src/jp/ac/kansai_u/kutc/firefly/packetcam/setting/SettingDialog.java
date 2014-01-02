package jp.ac.kansai_u.kutc.firefly.packetcam.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.util.Log;
import android.widget.Toast;
import jp.ac.kansai_u.kutc.firefly.packetcam.GetGCD;
import jp.ac.kansai_u.kutc.firefly.packetcam.MainActivity;
import jp.ac.kansai_u.kutc.firefly.packetcam.readpcap.PcapManager;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Path;

import java.io.File;
import java.util.List;

/**
 * 設定ダイアログに関するクラス
 * TODO: とりあえず，これでいい
 * TODO: トーストを削除して，選択されてるものを分かりやすいようにする
 * @author akasaka
 */
public class SettingDialog{
    final String TAG = "SettingDialog.java";
    Activity activity;
    // 画像サイズ（height，width）
    Camera.Size picSize = null;

    /**
     * ダイアログを表示する
     * @param a アクティビティ
     */
    public void show(Activity a){
        activity = a;

        CharSequence[] conf = { "パケットの読み込み", "解像度", "フラッシュ" };

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("設定");
        builder.setItems(conf, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0) {
                    // パケットの読み込みダイアログ
                    final AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
                    builder1.setTitle("ファイルの選択");

                    // リストにするディレクトリの指定
                    File dir = new File(Path.PACKETFOLDER_PATH);

                    // 指定されたディレクトリ内のファイル名をすべて取得
                    final File[] files = dir.listFiles();
                    final String[] str_items = new String[files.length];
                    for(int i = 0; i < files.length; i++) {
                        File file = files[i];
                        str_items[i] = file.getName();
                    }

                    // ファイルリストダイアログの表示
                    builder1.setItems(str_items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // 選択されたパケットファイルのパス
                            String filePath = Path.PACKETFOLDER_PATH + File.separator + str_items[which];
                            // ファイルを開く
                            File file = new File(filePath);
                            Log.d(TAG, "filePath = " + filePath);
                            // PcapManagerインスタンスを取得
                            PcapManager pcap = PcapManager.getInstance();

                            if(file.isDirectory()){
                                // ディレクトリの場合
                                // ディレクトリ内のファイルを読み込む
                                for(File f: file.listFiles()){
                                    if(pcap.openPcapFile(f))
                                        Log.d(TAG, "FILE OPEN SUCCESS: " + f.getName());
                                }
                            }else{
                                // PcapManagerのopenPcapFileにファイルパスを渡す
                                pcap.openPcapFile(file);
                                Log.d(TAG, "FILE OPEN SUCCESS: " + file.getName());
                            }
                        }
                    });
                    builder1.show();
                }
                if (which == 1) {
                    // 解像度
                    // 対応する画像サイズのリストを取得
                    final List<Camera.Size> supportedPictureSize = MainActivity.getCamera().getParameters().getSupportedPictureSizes ();
                    // 画像サイズの個数を取得
                    int numPicItem = supportedPictureSize.size ();

                    // さきほど取得した個数をもとに配列を定義
                    String[] picHeight = new String[numPicItem];
                    String[] picWidth = new String[numPicItem];
                    final String[] pic = new String[numPicItem];

                    for (int i = 0; i < supportedPictureSize.size (); i++)
                    {
                        picSize = supportedPictureSize.get (i);
                        picHeight[i] = String.valueOf (picSize.height);
                        picWidth[i] = String.valueOf (picSize.width);

                        // 縦サイズと横サイズの最大公約数を求める
                        int gcd = GetGCD.getGCD(picSize.width, picSize.height);

                        String aspWidth = String.valueOf (picSize.width / gcd);
                        String aspHeight = String.valueOf (picSize.height / gcd);

                        pic[i] = picWidth[i] + " : " + picHeight[i] + "（" + aspWidth + " ： " + aspHeight + "）";
                    }


                    final AlertDialog.Builder builder2 = new AlertDialog.Builder (activity);
                    builder2.setTitle ("解像度");
                    builder2.setItems (pic, new DialogInterface.OnClickListener ()
                    {
                        public void onClick (DialogInterface dialog, int which)
                        {
                            Camera.Parameters parameter = MainActivity.getCamera().getParameters();

                            picSize = supportedPictureSize.get (which);

                            Toast.makeText(activity, "画像サイズを" + pic[which] + "に設定しました．", Toast.LENGTH_SHORT).show ();

                            parameter.setPictureSize (picSize.width, picSize.height);
                            MainActivity.getCamera().setParameters(parameter);
                            // 設定ファイルに保存
                            SettingsManager.getInstance().setResolution(picSize.width, picSize.height);
                        }
                    });
                    builder2.show();
                }
                if (which == 2) {
                    // フラッシュ
                    CharSequence[] conf3 = { "強制発光", "自動", "OFF" };

                    AlertDialog.Builder builder3 = new AlertDialog.Builder (activity);
                    builder3.setTitle ("フラッシュ");
                    builder3.setItems (conf3, new DialogInterface.OnClickListener ()
                    {
                        public void onClick (DialogInterface dialog, int which)
                        {
                            if (which == 0)
                            {
                                // 強制発光処理
                                Camera.Parameters parameters = MainActivity.getCamera().getParameters ();
                                parameters.setFlashMode (Camera.Parameters.FLASH_MODE_ON);
                                MainActivity.getCamera().setParameters (parameters);
                                Toast.makeText (activity, "Flashを強制発光モードにしました", Toast.LENGTH_SHORT).show ();
                                // 設定ファイルに保存
                                SettingsManager.getInstance().setFlash(Camera.Parameters.FLASH_MODE_ON);
                            }
                            if (which == 1)
                            {
                                // 自動発光処理
                                Camera.Parameters parameters = MainActivity.getCamera().getParameters ();
                                parameters.setFlashMode (Camera.Parameters.FLASH_MODE_AUTO);
                                MainActivity.getCamera().setParameters (parameters);
                                Toast.makeText (activity, "Flashを自動モードにしました", Toast.LENGTH_SHORT).show();
                                // 設定ファイルに保存
                                SettingsManager.getInstance().setFlash(Camera.Parameters.FLASH_MODE_AUTO);
                            }
                            if (which == 2)
                            {
                                // フラッシュOFF処理
                                Camera.Parameters parameters = MainActivity.getCamera().getParameters ();
                                parameters.setFlashMode (Camera.Parameters.FLASH_MODE_OFF);
                                MainActivity.getCamera().setParameters (parameters);
                                Toast.makeText (activity, "FlashをOFFにしました", Toast.LENGTH_SHORT).show();
                                // 設定ファイルに保存
                                SettingsManager.getInstance().setFlash(Camera.Parameters.FLASH_MODE_OFF);
                            }
                        }
                    });
                    builder3.show ();
                }
            }
        });
        builder.show();
    }
}
