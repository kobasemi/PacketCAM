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
    public PacketAnalyser(){}
    /**
     * 初期化時にパケットをセット
     * @param p セットするパケット
     */
    public PacketAnalyser(PcapPacket p){
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

    /**
     * @return Ethernetヘッダの有無
     * @throws IOException
     * @throws CodecCreateException
     */
    public boolean hasEthernet() throws IOException, CodecCreateException {
        return packet != null & packet.hasHeader(Ethernet2.class);
    }

    /**
     * @return IEEE802.3ヘッダの有無
     * @throws IOException
     * @throws CodecCreateException
     */
    public boolean hasIeee802dot3() throws IOException, CodecCreateException {
        return packet != null & packet.hasHeader(IEEE802dot3.class);
    }

    /**
     * @return IPv4ヘッダの有無
     * @throws IOException
     * @throws CodecCreateException
     */
    public boolean hasIp4() throws IOException, CodecCreateException{
        return packet != null & packet.hasHeader(Ip4.class);
    }

    /**
     * @return TCPヘッダの有無
     * @throws IOException
     * @throws CodecCreateException
     */
    public boolean hasTcp() throws IOException, CodecCreateException{
        return packet != null & packet.hasHeader(Tcp.class);
    }

    /**
     * @return UDPヘッダの有無
     * @throws IOException
     * @throws CodecCreateException
     */
    public boolean hasUdp() throws IOException, CodecCreateException{
        return packet != null & packet.hasHeader(Udp.class);
    }

    /**
     * @return ICMPヘッダの有無
     * @throws IOException
     * @throws CodecCreateException
     */
    public boolean hasIcmp() throws IOException, CodecCreateException{
        return packet != null & packet.hasHeader(Icmp.class);
    }

    /**
     * @return ARPヘッダの有無
     * @throws IOException
     * @throws CodecCreateException
     */
    public boolean hasArp() throws IOException, CodecCreateException {
        return packet != null & packet.hasHeader(Arp.class);
    }

    /**
     * @return Ethernetヘッダ
     * @throws IOException
     * @throws CodecCreateException
     */
    public Ethernet2 getEthernet() throws IOException, CodecCreateException {
        if(packet != null && packet.hasHeader(Ethernet2.class)){
            return packet.getHeader(Ethernet2.class);
        }
        return null;
    }

    /**
     * @return IEEE802.3ヘッダ
     * @throws IOException
     * @throws CodecCreateException
     */
    public IEEE802dot3 getIeee802dot3() throws IOException, CodecCreateException {
        if(packet != null && packet.hasHeader(IEEE802dot3.class)){
            return packet.getHeader(IEEE802dot3.class);
        }
        return null;
    }

    /**
     * @return IPv4ヘッダ
     * @throws IOException
     * @throws CodecCreateException
     */
    public Ip4 getIp4() throws IOException, CodecCreateException{
        if(packet != null && packet.hasHeader(Ip4.class)){
            return packet.getHeader(Ip4.class);
        }
        return null;
    }

    /**
     * @return TCPヘッダ
     * @throws IOException
     * @throws CodecCreateException
     */
    public Tcp getTcp() throws IOException, CodecCreateException{
        if(packet != null && packet.hasHeader(Tcp.class)){
            return packet.getHeader(Tcp.class);
        }
        return null;
    }

    /**
     * @return UDPヘッダ
     * @throws IOException
     * @throws CodecCreateException
     */
    public Udp getUdp() throws IOException, CodecCreateException{
        if(packet != null && packet.hasHeader(Udp.class)){
            return packet.getHeader(Udp.class);
        }
        return null;
    }

    /**¥
     * @return ICMPヘッダ
     * @throws IOException
     * @throws CodecCreateException
     */
    public Icmp getIcmp() throws IOException, CodecCreateException{
        if(packet != null && packet.hasHeader(Icmp.class)){
            return packet.getHeader(Icmp.class);
        }
        return null;
    }

    /**
     * @return ARPヘッダ
     * @throws IOException
     * @throws CodecCreateException
     */
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
