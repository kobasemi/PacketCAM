package jp.ac.kansai_u.kutc.firefly.packetcam.readpcap;

import org.jnetstream.capture.FilePacket;

import java.util.ArrayList;
import java.util.List;

/**
 * ロードしたパケットを保持するリスト
 * ファイルからロード時はこちらを使用する
 * @author akasaka
 */
public class PacketsList {
    /**
     * パケット格納リスト
     * イテレータなどを使うことも考えたが
     * どうせならループさせたいのでリストを使用し，
     * インデックスで取得できるようにする．再考の余地はあり
     */
    List<FilePacket> packets = new ArrayList<FilePacket>();

    /** リスト中の要素番号 */
    int idx = 0;
    /** リストの最大要素数 */
    int size = 0;

    /**
     * 初期化メソッド
     * ファイルからロードしたパケットを格納する
     */
    public void init(){
        for(FilePacket p: PcapManager.getInstance().capture){
            packets.add(p);
        }
        size = packets.size();
    }

    /**
     * パケットがあれば返す
     * @return 次のパケット
     */
    public FilePacket next(){
        if(!hasPacket()){
            // パケットがない場合

        }
        idx++; //TODO: 0番目にアクセスできねえｗｗｗ
        return packets.get(idx);
    }

    /**
     * パケットが残っているか
     * @return 有無
     */
    public boolean hasPacket(){
        if (packets.get(idx) instanceof FilePacket) return true;
        else return false;
    }

    /**
     * クラス破棄
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();    //To change body of overridden methods use File | Settings | File Templates.
    }
}