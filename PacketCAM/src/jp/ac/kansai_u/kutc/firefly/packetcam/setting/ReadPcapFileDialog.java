package jp.ac.kansai_u.kutc.firefly.packetcam.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import jp.ac.kansai_u.kutc.firefly.packetcam.readpcap.PcapManager;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Path;

import java.io.File;

/**
 * PcapFileの読み込み用ダイアログの作成
 * 汎用性を持たせるために別クラスに分けた
 * @author akasaka
 */
public class ReadPcapFileDialog implements DialogInterface.OnClickListener {
    final String TAG = "ReadPcapFileDialog.java";
    String[] str_items;

    public void show(Activity activity){
        // パケット読み込みダイアログの作成
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("ファイルの選択");

        // リストにするディレクトリの指定
        File dir = new File(Path.PACKETFOLDER_PATH);

        // 指定されたディレクトリ内のファイル名をすべて取得
        // 但し，深さ1のディレクトリ構造のみを考慮している
        // /hoge/hogehoge/fooの場合，hogeまでしか表示されない
        final File[] files = dir.listFiles();
        str_items = new String[files.length];
        for(int i = 0; i < files.length; i++) {
            File file = files[i];
            str_items[i] = file.getName();
        }

        // ファイルリストダイアログの表示
        builder.setItems(str_items, this);
        builder.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        // 選択されたパケットファイルのパス
        String filePath = Path.PACKETFOLDER_PATH + File.separator + str_items[which];
        PcapManager.getInstance().open(new File(filePath));
    }
}
