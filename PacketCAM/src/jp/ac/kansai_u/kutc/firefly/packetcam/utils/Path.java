package jp.ac.kansai_u.kutc.firefly.packetcam.utils;

import android.os.Environment;

import java.io.File;

/**
 * アプリ内で使用するPATHの一覧
 * @author akasaka
 */
public class Path {
    // SDカードのパス
    public static String SD_PATH = Environment.getExternalStorageDirectory().getPath();
    // SDカード/PacketCAM のルートパス
    public static String APPROOT_PATH = SD_PATH + File.separator + Name.APP_NAME;
    // 画像保存フォルダのパス
    public static String PICFOLDER_PATH = APPROOT_PATH + File.separator + "Pictures";
    // Packetデータ保存フォルダのパス
    public static String PACKETFOLDER_PATH = APPROOT_PATH + File.separator + "Packet";
}
