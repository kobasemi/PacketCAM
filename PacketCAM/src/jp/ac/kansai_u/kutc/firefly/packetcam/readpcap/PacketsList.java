package jp.ac.kansai_u.kutc.firefly.packetcam.readpcap;

import org.jnetstream.capture.FilePacket;
import org.jnetstream.capture.PacketIterator;
import org.jnetstream.capture.file.pcap.PcapPacket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ロードしたパケットを保持するリスト
 * ファイルからロード時はこちらを使用する
 * パケットをリストに入れただけの単純なもの
 * ！！！恐らく必要ない！！！
 * @author akasaka
 */
public class PacketsList {
    /**
     * パケット格納リスト
     * ループさせるのを考慮して，リストを使用
     * 再考の余地はあり
     */
    public List<PcapPacket> packets = new ArrayList<PcapPacket>();
    /** リスト中の要素番号 */
    int idx = 0;

    public PacketsList(){
        init();
    }

    /**
     * 初期化メソッド
     * ファイルからロードしたパケットを格納する
     */
    public void init(){
        try {
            while(PcapManager.getInstance().getPcapFile().getPacketIterator().hasNext()){
                packets.add(PcapManager.getInstance().getPcapFile().getPacketIterator().next());
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * パケットがあれば返す
     * @return パケット，またはnull
     */
    public PcapPacket next(){
        return packets.get(idx++);
    }
}