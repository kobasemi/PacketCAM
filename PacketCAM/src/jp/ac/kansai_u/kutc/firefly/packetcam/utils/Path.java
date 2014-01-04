package jp.ac.kansai_u.kutc.firefly.packetcam.utils;

import android.os.Environment;

import java.io.File;

/**
 * アプリ内で使用するPATHの一覧
 * TODO: とりあえず書いておいて，後々考える
 * TODO: CreateDirectoryクラスとの連携が何か嫌だなあ
 * @author akasaka
 */
public class Path {
    // SDカードのパス
    public static String SD_PATH = null;
    // SDカード/PacketCAM のルートパス
    public static String APPROOT_PATH = null;
    // 画像保存フォルダのパス
    public static String PICFOLDER_PATH = null;
    // Packetデータ保存フォルダのパス
    public static String PACKETFOLDER_PATH = null;

    /**
     * アプリケーション内で使用するPATHを初期化する
     */
    public static void init(){
        SD_PATH           = Environment.getExternalStorageDirectory().getPath ();
        APPROOT_PATH      = SD_PATH + File.separator + Name.APP_NAME;
        PICFOLDER_PATH    = APPROOT_PATH + File.separator + "Pictures";
        PACKETFOLDER_PATH = APPROOT_PATH + File.separator + "Packet";
    }
}
