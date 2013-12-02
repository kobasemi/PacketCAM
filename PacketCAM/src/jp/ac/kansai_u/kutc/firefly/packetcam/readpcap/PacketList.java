package jp.ac.kansai_u.kutc.firefly.packetcam.readpcap;

import org.jnetstream.capture.FileCapture;
import org.jnetstream.capture.FilePacket;

import java.util.ArrayList;
import java.util.List;

/**
 * ロードしたパケットを保持するリスト
 * ファイルからロード時はこちらを使用する
 * @author akasaka
 */
public class PacketList {
    /** パケットの集合 */
    List<FileCapture<? extends FilePacket>> captures = new ArrayList<FileCapture<? extends FilePacket>>();

}
