package jp.ac.kansai_u.kutc.firefly.packetcam.readpcap;

import java.io.File;
import java.io.IOException;

import android.util.Log;
import org.jnetstream.capture.Captures;
import org.jnetstream.capture.FileMode;
import org.jnetstream.capture.file.pcap.PcapFile;

/**
 * readpcapパッケージ内の全ての親となるクラス
 * @author akasaka
 */
public class PcapManager {

    /** シングルトン♪ シングルトン♪ 鈴が鳴る〜♪ */
    private static PcapManager instance = new PcapManager();

    private PcapFile pcapFile;
    /**
     * シングルトンのインスタンスを返す
     * @return インスタンス
     */
    public static PcapManager getInstance(){
        return instance;
    }

    private PcapManager(){
        pcapFile = null;
    }

    /**
     * PcapFileを開く
     * @param  path ファイルの完全パス
     * @return b    オープン成功・失敗
     */
    public boolean openPcapFile(String path){
        if(pcapFile != null){
            // 既にPcapFileが開かれていた場合
            closePcapFile();
            pcapFile = null;
        }

        try {
            pcapFile = Captures.openFile(PcapFile.class, new File(path), FileMode.ReadOnly);
        } catch (IOException e) {
            Log.d("PcapManager.java", "FAILED TO OPEN");
            return false;
        }
        return true;
    }

    /**
     * PcapFileを閉じる
     * プログラム終了時，必ず閉じなければならない
     */
    public void closePcapFile(){
        try {
            pcapFile.close();
        } catch (IOException e) {
            Log.d("PcapManager.java", "FAILED TO CLOSE");
        }
    }

    /**
     * 開いているPcapFileを返す．開いていない場合は，nullを返す
     * @return PcapFile またはnull
     */
    public PcapFile getPcapFile(){
        return pcapFile;
    }
}