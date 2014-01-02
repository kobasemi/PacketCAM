package jp.ac.kansai_u.kutc.firefly.packetcam.readpcap;

import android.util.Log;
import org.jnetstream.capture.Captures;
import org.jnetstream.capture.FileMode;
import org.jnetstream.capture.PacketIterator;
import org.jnetstream.capture.file.pcap.PcapFile;
import org.jnetstream.capture.file.pcap.PcapPacket;

import java.io.File;
import java.io.IOException;
import java.util.Queue;

/**
 * readpcapパッケージ内の全ての親となるクラス
 * @author akasaka
 */
public class PcapManager extends Thread{

    /** シングルトン♪ シングルトン♪ 鈴が鳴る〜♪ */
    private static PcapManager instance = new PcapManager();
    /**
     * シングルトンのインスタンスを返す
     * @return インスタンス
     */
    public static PcapManager getInstance(){
        return instance;
    }

    final String TAG = "PcapManager.java";
    private PcapFile pcapFile; // 常に一つのファイルのみを保持する
    // pcapFileとキューの中間で橋渡しをするイテレータ
    private PacketIterator<PcapPacket> packetIterator = null;
    // スレッドセーフなキュー @see ConcurrentPacketsQueue
    private Queue<PcapPacket> packetsQueue = new ConcurrentPacketsQueue<PcapPacket>();

    // スレッド抹殺用
    private boolean kill  = false;
    // sleep time[milliseconds]
    private long ms = 1000;

    private PcapManager(){
        pcapFile = null;
    }

    /**
     * PcapFileを開き，イテレータにパケットをセットする
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
            setPacketsToPacketIterator();  // Set Packets from pcapFile
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
            pcapFile = null;
        } catch (IOException e) {
            Log.d(TAG, "FAILED TO CLOSE");
        }
    }

    /**
     * 開いているPcapFileを返す．開いていない場合は，nullを返す
     * @return PcapFile またはnull
     */
    public PcapFile getPcapFile(){
        return pcapFile;
    }

    /**
     * ConcurrentPacketsQueueオブジェクトを返す
     * TODO: インスタンス化は違う場所でやった方がいいと思う
     * @return packetsQueue インスタンス
     */
    public Queue getConcurrentPacketsQueue(){
        return packetsQueue;
    }

    /**
     * ファイルからロードしたパケットをイテレータにセットする
     */
    private void setPacketsToPacketIterator(){
        if(pcapFile.isOpen()) {
            try {
                packetIterator = pcapFile.getPacketIterator();
            } catch(IOException e) {
                Log.d(TAG, "FAILED TO SET PACKETS");
            }
        }
    }

    /**
     * イテレータにパケットが残っているか
     * @return パケットの有無
     */
    public boolean hasPacket() {
        try {
            return packetIterator.hasNext();
        } catch(IOException e) {
            Log.d(TAG, "FAILED TO CHECK HASNEXT()");
        }
        return false;
    }

    /**
     * 使わないで．
     * パケットが欲しい場合は，キューから貰って．
     * これ使ったら，イテレータのポジションが進む
     * @return パケット
     * @throws IOException
     * @deprecated
     */
    public PcapPacket getPacket() throws IOException {
        return packetIterator.next();
    }


    /**
     * ファイルからロードしたパケットの数を返す
     * @return パケット数，マイナスの場合はエラー
     */
    public long getPacketCount(){
        try {
            return pcapFile.getPacketCount();
        } catch(IOException e) {
            Log.d(TAG, "FAILED TO GET PACKET COUNT");
        }
        return -1;
    }

    /**
     * スレッドを停止する
     */
    public void kill(){
        kill = true;
    }

    /**
     * スリーブ時間を動的に変更する
     * @param ms ミリ秒
     */
    public synchronized void setMs(long ms){
        this.ms = ms;
    }

    /**
     * スレッド処理
     * 絶対，pm.start()で呼び出すこと，基本中の基本
     */
    @Override
    public void run() {
        kill = false;

        while(!kill){
            if(hasPacket()){
                // イテレータにパケットが残っている場合
                try {
                    // キューにパケットをセットする
                    packetsQueue.add(packetIterator.next());
                } catch(IOException e) {
                    Log.d(TAG, "FAILED TO SET PACKET TO QUEUE");
                }
                try {
                    // 単位はミリ秒，動的に変えられる
                    sleep(ms);
                } catch(InterruptedException e) {
                    Log.d(TAG, "INTERRUPTED SLEEP");
                }
            }else
                kill();  // スレッドを殺す
        }
    }
}