package jp.ac.kansai_u.kutc.firefly.packetcam.readpcap;

import android.util.Log;
import org.jnetstream.capture.Captures;
import org.jnetstream.capture.FileMode;
import org.jnetstream.capture.PacketIterator;
import org.jnetstream.capture.file.pcap.PcapFile;
import org.jnetstream.capture.file.pcap.PcapPacket;

import java.io.File;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
    private PcapManager(){}
    private static PcapManager instance = new PcapManager();
    /**
     * シングルトンのインスタンスを返す
     * @return インスタンス
     */
    public static PcapManager getInstance(){
        return instance;
    }

    final String TAG = "PcapManager.java";
    private PcapFile pcapFile = null; // 常に一つのファイルのみを保持する

    /**
     * PacketIterator<T>のhasNext()及びnext()が訳分からん例外を吐くため，
     * 例外を吐かないIterator<T>を使用する
     * コードは多少解りづらくなるが，仕方がない
     * 詳しくは，setPacketsToPacketList()を参照
     */
    // PacketIterator<T>とIterator<T>の中間で橋渡しをするリスト
    private List<PcapPacket> packetList = null;
    // キューへの装填を仲介するイテレータ
    Iterator<PcapPacket> packetIterator = null;
    // スレッドセーフなキュー @see ConcurrentPacketsQueue
    private Queue<PcapPacket> packetsQueue = new ConcurrentPacketsQueue<PcapPacket>();

    // PcapManagerの準備状態
    private boolean isReady = false;

    /**
     * 絶対パスからファイルオブジェクトをインスタンス化し，open(File file)を呼び出す
     * @param  path ファイルの絶対パス
     */
    public void open(String path){
        open(new File(path));
    }

    /**
     * 選択されたファイル（ディレクトリの場合，中の全てのファイル）を開き，
     * 選択されたファイルから得られる全てのパケットを格納したリストをイテレータに変換する
     * @param file 選択されたファイルオブジェクト
     */
    public void open(File file){
        // パケットリストの参照を削除
        packetList = null;
        if(file.isDirectory())
            // ディレクトリ内の全てのファイルを読み込む
            for(File f: file.listFiles())
                openPcapFile(f);
        else
            openPcapFile(file);

        packetIterator = packetList.iterator();
        isReady = true;
    }

    /**
     * PcapFileをオープンし，パケットをリストに追加する
     * @param file ロードするファイル
     */
    private void openPcapFile(File file){
        if(pcapFile != null){
            // 既にPcapFileが開かれていた場合
            close();
        }

        try {
            pcapFile = Captures.openFile(PcapFile.class, file, FileMode.ReadOnly);
            setPacketsToPacketList();
            Log.d(TAG, "FILE OPEN SUCCESS: " + file.getName());
        } catch (IOException e) {
            Log.d(TAG, "FAILED TO OPEN");
        }
    }


    /**
     * PcapFileからロードしたパケットをイテレータに変換した後，リストに追加する
     *
     * PacketIteratorの不具合なのか原因は不明だが
     * pi.next()でBufferUnderflowExceptionの例外を吐く
     * しかし，リストへの追加は正常に行われているため，無理やりキャッチする
     * ちなみに，このメソッドは絶対に例外を吐くメソッド，どうしようもないねｗ
     *
     * 調査したところ，hasNext()がどうも上手いこと動いていないように見える
     * 例えば，
         * while(pi.hasNext())
         *     pi.next();
     * というコードを実行したとき，イテレータが持っている範囲を超えてnext()をしているのではないか
     * また下記デバッグコードを実行したとき
         * // イテレータの中身は10個のみ
         * static int cnt = 0;
         * while(pi.hasNext()){
         *     Log.d(TAG, String.valueOf(++cnt));
         *     pi.next();
         * }
     * ログの結果を見ると，11までカウントが進んでいる
     * イテレータの中身は10個のため，これは可笑しいと思われる
     * hasNext()が上手いこと動かないことにより，next()で例外を吐いている？
     * ??? 原因不明 ???
     */
    private void setPacketsToPacketList(){
        if(pcapFile.isOpen()) {
            try {
                PacketIterator<PcapPacket> tmpPacketIterator = pcapFile.getPacketIterator();
                packetList = new ArrayList<PcapPacket>();
                while(tmpPacketIterator.hasNext()){
                    packetList.add(tmpPacketIterator.next());
                }
            } catch(IOException e) {
                Log.d(TAG, "FAILED TO SET PACKETS");
            } catch(BufferUnderflowException e){
                // イテレータのnext()が吐く例外
                Log.d(TAG, "F**K Exception!!! why happen?");
            }
        }
    }

    /**
     * PcapFileを閉じる
     * プログラム終了時，必ず閉じなければならない
     */
    public void close(){
        try {
            pcapFile.close();
            pcapFile = null;
        } catch (IOException e) {
            Log.d(TAG, "FAILED TO CLOSE");
        }
    }

    /**
     * PcapManagerの準備が完了しているかどうか
     * @return 完了/未完了
     */
    public boolean isReady(){ return isReady; }

    /**
     * 開いているPcapFileを返す．開いていない場合は，nullを返す
     * @return PcapFile またはnull
     */
    public PcapFile getPcapFile(){
        return pcapFile;
    }

    /**
     * ConcurrentPacketsQueueオブジェクトを返す
     * @return packetsQueue インスタンス
     */
    public Queue<PcapPacket> getConcurrentPacketsQueue(){ return packetsQueue; }

    /**
     * 使わないで．
     * パケットが欲しい場合は，キューから貰って．
     * これ使ったら，イテレータのポジションが進む
     * @return パケット
     * @throws IOException
     * @deprecated
     */
    public PcapPacket getPacket() throws IOException { return packetIterator.next(); }

    /**
     * ファイルからロードしたパケットの数を返す
     * @return パケット数，マイナスの場合はエラー
     * @deprecated BufferUnderflowExceptionが投げられる
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
     * ref: http://www.02.246.ne.jp/~torutk/javahow2/timer.html#doc1_id147
     */
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    ScheduledFuture<?> future = null;
    /**
     * スレッド処理を開始する
     */
    public void start(){
        if(executor.isShutdown())
            // 復帰時にシャットダウンしていた場合
            executor = Executors.newSingleThreadScheduledExecutor();
        // this: スレッド，1000: 周期，TimeUnit.MILLSECONDS: ミリ秒単位
        future = executor.scheduleAtFixedRate(this, 0, 100, TimeUnit.MILLISECONDS);
    }
    /**
     * スレッド処理を一時停止する
     * called when onPause()
     */
    public void stop(){
        if(future != null) future.cancel(true);
    }
    /**
     * スレッド処理を完全停止する
     * called when onDestroy()
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
        if(packetIterator.hasNext())
            if(packetsQueue.add(packetIterator.next()))  // キューに装填
                Log.d(TAG + ": add", "Success");
        else
            stop();
    }
}