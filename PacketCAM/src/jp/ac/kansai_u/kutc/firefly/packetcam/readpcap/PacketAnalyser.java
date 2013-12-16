package jp.ac.kansai_u.kutc.firefly.packetcam.readpcap;

import org.jnetstream.capture.file.pcap.PcapPacket;
import org.jnetstream.packet.Header;
import org.jnetstream.protocol.codec.CodecCreateException;
import org.jnetstream.protocol.lan.Arp;
import org.jnetstream.protocol.lan.Ethernet2;
import org.jnetstream.protocol.lan.IEEE802dot3;
import org.jnetstream.protocol.tcpip.Icmp;
import org.jnetstream.protocol.tcpip.Ip4;
import org.jnetstream.protocol.tcpip.Tcp;
import org.jnetstream.protocol.tcpip.Udp;

import java.io.IOException;

/**
 * パケット解析を行うクラス
 * 一つのパケットのみを保持し，そのパケットの様々な結果を返す
 * いるかは不明
 * @author akasaka
 */
public class PacketAnalyser {
    PcapPacket packet;

    /**
     * 空のコンストラクタ
     * 後からパケットをセットすること
     */
    PacketAnalyser(){}
    /**
     * 初期化時にパケットをセット
     * @param p セットするパケット
     */
    PacketAnalyser(PcapPacket p){
        setPacket(p);
    }

    /**
     * パケットをセットする
     * @param p
     */
    public void setPacket(PcapPacket p){
        packet = p;
    }

    /**
     * パケットに含まれる全てのヘッダを返す
     * @return ヘッダ配列
     * @throws IOException
     */
    public Header[] getAllHeader() throws IOException {
        return packet.getAllHeaders();
    }

    public boolean hasEthernet() throws IOException, CodecCreateException {
        return packet != null & packet.hasHeader(Ethernet2.class);
    }

    public boolean hasIeee802dot3() throws IOException, CodecCreateException {
        return packet != null & packet.hasHeader(IEEE802dot3.class);
    }

    public boolean hasIp4() throws IOException, CodecCreateException{
        return packet != null & packet.hasHeader(Ip4.class);
    }

    public boolean hasTcp() throws IOException, CodecCreateException{
        return packet != null & packet.hasHeader(Tcp.class);
    }

    public boolean hasUdp() throws IOException, CodecCreateException{
        return packet != null & packet.hasHeader(Udp.class);
    }

    public boolean hasIcmp() throws IOException, CodecCreateException{
        return packet != null & packet.hasHeader(Icmp.class);
    }

    public boolean hasArp() throws IOException, CodecCreateException {
        return packet != null & packet.hasHeader(Arp.class);
    }

    public Ethernet2 getEthernet() throws IOException, CodecCreateException {
        if(packet != null && packet.hasHeader(Ethernet2.class)){
            return packet.getHeader(Ethernet2.class);
        }
        return null;
    }

    public IEEE802dot3 getIeee802dot3() throws IOException, CodecCreateException {
        if(packet != null && packet.hasHeader(IEEE802dot3.class)){
            return packet.getHeader(IEEE802dot3.class);
        }
        return null;
    }

    public Ip4 getIp4() throws IOException, CodecCreateException{
        if(packet != null && packet.hasHeader(Ip4.class)){
            return packet.getHeader(Ip4.class);
        }
        return null;
    }

    public Tcp getTcp() throws IOException, CodecCreateException{
        if(packet != null && packet.hasHeader(Tcp.class)){
            return packet.getHeader(Tcp.class);
        }
        return null;
    }

    public Udp getUdp() throws IOException, CodecCreateException{
        if(packet != null && packet.hasHeader(Udp.class)){
            return packet.getHeader(Udp.class);
        }
        return null;
    }

    public Icmp getIcmp() throws IOException, CodecCreateException{
        if(packet != null && packet.hasHeader(Icmp.class)){
            return packet.getHeader(Icmp.class);
        }
        return null;
    }

    public Arp getArp() throws IOException, CodecCreateException {
        if(packet != null && packet.hasHeader(Arp.class)){
            return packet.getHeader(Arp.class);
        }
        return null;
    }

    /**
     * パケットのタイムスタンプを返す
     * @return 秒数
     * @throws IOException
     */
    public long getSeconds() throws IOException {
        if(packet != null)
            return packet.getTimestampSeconds();
        return -1;
    }

    /**
     * パケットのタイムスタンプを返す
     * @return ナノ秒
     * @throws IOException
     */
    public long getNanos() throws IOException {
        if(packet != null)
            return packet.getTimestampNanos();
        return -1;
    }
}
