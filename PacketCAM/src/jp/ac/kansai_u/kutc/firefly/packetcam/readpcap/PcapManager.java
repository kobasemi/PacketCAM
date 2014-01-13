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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * readpcapパッケージ内の全ての親となるクラス
 * @author akasaka
 */
public class PcapManager implements Runnable{

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

    private PcapManager(){
        pcapFile = null;
    }

    /**
     * PcapFileを開き，イテレータにパケットをセットする
     * @param file ファイルオブジェクト
     * @return b   オープン成功・失敗
     */
    public boolean openPcapFile(File file){
        if(pcapFile != null){
            // 既にPcapFileが開かれていた場合
            closePcapFile();
        }

        if(file.isDirectory())
            // ディレクトリ内のファイルを再帰的に読み込む
            for(File f: file.listFiles())
                openPcapFile(f);
        else
            try {
                pcapFile = Captures.openFile(PcapFile.class, file, FileMode.ReadOnly);
                setPacketsToPacketIterator();  // Set Packets from pcapFile
                Log.d(TAG, "FILE OPEN SUCCESS: " + file.getName());
            } catch (IOException e) {
                Log.d(TAG, "FAILED TO OPEN");
                return false;
            }
        return true;
    }

    /**
     * 絶対パスからファイルオブジェクトをインスタンス化し，openPcapFileを呼び出す
     * @param  path ファイルの絶対パス
     * @return b    オープン成功・失敗
     */
    public boolean openPcapFile(String path){
        return openPcapFile(new File(path));
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
     * PcapFileがオープンされているかどうか
     * @return オープン/アンオープン
     */
    public boolean isPcapFileOpened(){
        return (pcapFile != null);
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
     * ScheduledExecutorServiceを使用したスレッド処理
     * start()   ; スレッドの開始
     * stop()    ; スレッドの一時停止
     * shutdown(); スレッドの停止
     * @see java.util.concurrent.ScheduledExecutorService
     */
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    ScheduledFuture<?> future = null;
    /**
     * スレッド処理を開始する
     */
    public void start(){
        future = executor.scheduleAtFixedRate(this, 0, 1000, TimeUnit.MILLISECONDS);
    }
    /**
     * スレッド処理を一時停止する
     */
    public void stop(){
        if(future != null) future.cancel(true);
    }
    /**
     * スレッド処理を完全停止する
     */
    public void shutdown(){
        stop();  // 一時停止をしてから停止する
        if(executor != null) executor.shutdown();
    }

    /**
     * スレッド処理
     * パケットがある場合，1秒ごとにキューにパケットを装填
     * パケットがなくなった場合，スレッドを停止
     */
    @Override
    public void run() {
        if(hasPacket())
            try {
                if(packetsQueue.add(packetIterator.next()))  // キューに装填
                    Log.d(TAG + ": add", "Success");
            } catch(IOException e) {
                Log.d(TAG, "FAILED TO SET PACKET TO QUEUE");
            }
        else
            shutdown();
    }
}