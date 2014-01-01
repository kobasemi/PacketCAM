package jp.ac.kansai_u.kutc.firefly.packetcam.utils;

import java.io.File;

/**
 * SDカードにディレクトリを作成する
 * @author akasaka
 */
public class CreateDirectory {

    /**
     * 画像保存用及びパケットファイル用ディレクトリの作成
     * @return 正常に作成できればtrue，できなければfalseを返す
     * TODO: 今はこのまま放置しておくけど，こんな訳わからんメソッドはいつか消す，またはメソッド名変える
     */
    public static boolean createDirectory ()
    {
        return createDirectory("Pictures") && createDirectory("Packet");
    }

    /**
     * ディレクトリの作成
     * @param dirname 作成したいディレクトリ名
     * @return 正常に作成できればtrue，できなければfalseを返す
     */
    public static boolean createDirectory (String dirname)
    {
        // SDカードにアプリ名でディレクトリを新規作成
        // TODO: メソッドをstaticにするため，アプリケーション名を直接書いた，アプリケーション名なんて変えないよね？
//        String dirpath = Path.SD_PATH + File.separator + res.getString(R.string.app_name) + File.separator + dirname;
        String dirpath = Path.APPROOT_PATH + File.separator + dirname;

        File dirFile = new File(dirpath);

        try{
            if (!dirFile.exists ()){
                dirFile.mkdirs ();
            }
        }catch (Exception e){
            e.printStackTrace ();
            return false;
        }
        return true;
    }
}
