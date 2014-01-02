package jp.ac.kansai_u.kutc.firefly.packetcam.readpcap;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * ファイルからロードしたパケットを保持するサイズ制限有りキュー
 * Ref: http://stackoverflow.com/questions/5498865/size-limited-queue-that-holds-last-n-elements-in-java
 * @author akasaka
 */
public class ConcurrentPacketsQueue<T> extends ConcurrentLinkedQueue<T> {

    private int limit = 100;
    private final Object lock = new Object();

    public ConcurrentPacketsQueue(){
        setLimit(limit);
    }

    public ConcurrentPacketsQueue(int lim){
        setLimit(lim);
    }

    /**
     * 動的にキューの長さを変更できます。
     *
     * @param lim
     * @return 無効なキューの長さが与えられた場合はfalseが返ります。
     */
    public boolean setLimit(int lim) {
        if (lim <= 0) {
            return false;
        }
        synchronized(lock) {
            limit = lim;
        }
        return true;
    }

    /**
     * LinkedListの関数をオーバーライドしたものです。
     *
     * @return 基本的にtrueですが、nullが渡された場合はキューに入れず、falseを返します。
     */
    @Override
    public synchronized boolean add(final T o) {
        if (o == null) {
            return false;
        }
        super.add(o);
        synchronized(lock) {
            while (size() > limit) {
                super.remove();
            }
        }
        return true;
    }

    /**
     * キューの許容サイズを返す
     * @return キューの許容サイズ
     */
    public int getLimit(){
        return limit;
    }
}