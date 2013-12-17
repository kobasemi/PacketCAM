package jp.ac.kansai_u.kutc.firefly.packetcam;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import jp.ac.kansai_u.kutc.firefly.packetcam.readpcap.PcapManager;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Path;

import java.io.File;
import java.util.List;

/**
 * 設定ダイアログに関するクラス
 * TODO: とりあえず，これでいい
 * @author akasaka
 */
public class SettingDialog{
    Activity activity;
    private boolean alert1_1 = false;
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
        builder.setTitle("Setting");
        builder.setItems(conf, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0) {
                    // パケットの読み込み
                    CharSequence[] conf1 = {"ファイルの選択", "リアルタイム読み込み"};
                    final AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
                    builder1.setTitle("設定");
                    builder1.setItems(conf1, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if(which == 0) {
                                // ファイルの選択ダイアログの表示
                                // リストにするディレクトリの指定
                                File dir = new File(Path.PACKETFOLDER_PATH);

                                // 指定されたディレクトリ内のファイル名をすべて取得
                                final File[] files = dir.listFiles();
                                final String[] str_items;
                                str_items = new String[files.length];

                                for(int i = 0; i < files.length; i++) {
                                    File file = files[i];
                                    str_items[i] = file.getName();
                                }
                                // ファイルリストダイアログの表示
                                final AlertDialog.Builder builder1_1 = new AlertDialog.Builder(activity);
                                builder1_1.setTitle("パケットファイルを選択してください");
                                builder1_1.setItems(str_items, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // 選択されたパケットファイルのパス
                                        String filePath = Path.PACKETFOLDER_PATH + File.separator + str_items[which];
                                        Log.d("SettingDialogClickListener.java", "filePath = " + filePath);

                                        // PcapManagerのopenPcapFileにファイルパスを渡す
                                        PcapManager pcap = PcapManager.getInstance();
                                        pcap.openPcapFile(filePath);
                                    }
                                });
                                builder1_1.setOnKeyListener(new DialogInterface.OnKeyListener() {
                                    @Override
                                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent keyEvent) {
                                        if(keyCode == KeyEvent.KEYCODE_BACK) {
                                            // アラートの飛び対策
                                            alert1_1 = true;
                                            dialog.dismiss();
                                            builder1.show();
                                            return true;
                                        }
                                        return false;
                                    }
                                });
                                builder1_1.show();
                            }
                            if (which == 1)
                            {
                                // リアルタイム読み込み処理
                            }
                        }
                    });
                    builder1.setOnKeyListener (new DialogInterface.OnKeyListener()
                    {
                        @Override
                        public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event)
                        {
                            // alert1_1を噛ませているのは，ファイル選択画面からBackキーを押すと，
                            // builder1を飛ばして一気にbuilder（最初のアラート）に戻ってしまうため
                            if (keyCode == KeyEvent.KEYCODE_BACK && alert1_1 == false)
                            {
                                dialog.dismiss ();
                                builder.show ();
                                return true;
                            }
                            alert1_1 = false;
                            return false;
                        }

                    });
                    builder1.show();
                }
                if (which == 1)
                {
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
                        int gcd = GetGCD.getGCD (picSize.width, picSize.height);

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
                        }
                    });
                    builder2.setOnKeyListener (new DialogInterface.OnKeyListener()
                    {

                        @Override
                        public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event)
                        {
                            // TODO 自動生成されたメソッド・スタブ
                            if (keyCode == KeyEvent.KEYCODE_BACK)
                            {
                                dialog.dismiss ();
                                builder.show();
                                return true;
                            }
                            return false;
                        }
                    });
                    builder2.show();
                }
                if (which == 2)
                {
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
                            }
                            if (which == 1)
                            {
                                // 自動発光処理
                                Camera.Parameters parameters = MainActivity.getCamera().getParameters ();
                                parameters.setFlashMode (Camera.Parameters.FLASH_MODE_AUTO);
                                MainActivity.getCamera().setParameters (parameters);
                                Toast.makeText (activity, "Flashを自動モードにしました", Toast.LENGTH_SHORT).show();
                            }
                            if (which == 2)
                            {
                                // フラッシュOFF処理
                                Camera.Parameters parameters = MainActivity.getCamera().getParameters ();
                                parameters.setFlashMode (Camera.Parameters.FLASH_MODE_OFF);
                                MainActivity.getCamera().setParameters (parameters);
                                Toast.makeText (activity, "FlashをOFFにしました", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    builder3.setOnKeyListener (new DialogInterface.OnKeyListener(){
                        @Override
                        public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event)
                        {
                            // TODO 自動生成されたメソッド・スタブ
                            if (keyCode == KeyEvent.KEYCODE_BACK)
                            {
                                dialog.dismiss();
                                builder.show();
                                return true;
                            }
                            return false;
                        }

                    });
                    builder3.show ();
                }
            }
        });

        builder.show();
    }
}
