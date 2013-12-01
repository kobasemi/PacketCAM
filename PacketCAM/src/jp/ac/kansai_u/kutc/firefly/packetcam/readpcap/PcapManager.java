package jp.ac.kansai_u.kutc.firefly.packetcam.readpcap;

import java.io.File;
import java.io.IOException;

import android.util.Log;
import org.jnetstream.capture.Captures;
import org.jnetstream.capture.FileCapture;
import org.jnetstream.capture.FilePacket;

/**
 * readpcapパッケージ内の全ての親となるクラス
 * @author akasaka
 */
public class PcapManager {

    // シングルトン♪ シングルトン♪ 鈴が鳴る〜♪
    private static PcapManager instance = new PcapManager();
    File pcapFile = null;
    /**
     * シングルトンのインスタンスを返す
     * @return インスタンス
     */
    public static PcapManager getInstance(){
        return instance;
    }

    FileCapture<? extends FilePacket> capture;
    private PcapManager(){
        capture = null;
    }

    /**
     * PcapFileを開く
     * @param  path ファイルの完全パス
     * @return b    オープン成功・失敗
     */
    public boolean openPcapFile(String path){
        if(capture != null){
            // 既にPcapFileが開かれていた場合
            closePcapFile();
            capture = null;
        }

        try {
            capture = Captures.openFile(new File(path));
        } catch (IOException e) {
            Log.d("OPEN FAILED", "PcapManager.java: FAILED TO OPEN");
        }
        return true;
    }

    /**
     * PcapFileを閉じる
     * プログラム終了時，必ず閉じなければならない
     */
    public void closePcapFile(){
        try {
            capture.close();
        } catch (IOException e) {
            Log.d("CLOSE FAILED", "PcapManager.java: FAILED TO CLOSE");
        }
    }
}