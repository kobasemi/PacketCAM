package jp.ac.kansai_u.kutc.firefly.packetcam.readpcap;

import java.io.File;
import java.io.FilenameFilter;

/**
 * PcapFileのみをフィルターするクラス
 * @author akasaka
 */
public class PcapFileFilter implements FilenameFilter {
    @Override
    public boolean accept(File dir, String filename) {
        // .pcapで終わるファイル名 -> 真
        return filename.endsWith(".pcap");
    }
}
