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
        return createDirectory("Pictures", Path.APPROOT_PATH) && createDirectory("Packet", Path.APPROOT_PATH);
    }

    /**
     * ディレクトリの作成
     * @param fullpath 作成したいディレクトリのフルパス
     * @return 正常に作成できればtrue，できなければfalseを返す
     */
    //TODO: ディレクトリ作成時にPathクラスにパス名を追加とかしたほうがいいかも？
    public static boolean createDirectory (String fullpath)
    {
        File dirFile = new File(fullpath);

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

    /**
     * ディレクトリの作成
     * @param dirname ディレクトリ名
     * @param parentPath 作成したいディレクトリの親ディレクトリのパス
     * @return 正常に作成できればtrue，できなければfalseを返す
     */
    public static boolean createDirectory (String dirname, String parentPath)
    {
        return createDirectory(parentPath + File.separator + dirname);
    }
}
