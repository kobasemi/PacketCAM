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
 * 吐かれる例外を全て回収するため，nullチェックなどをしっかりとすること
 * @author akasaka
 */
public class PacketAnalyser {
    PcapPacket packet;

    /**
     * デフォルトコンストラクタ
     * 後からパケットをセットすること
     */
    public PacketAnalyser(){}

    /**
     * パケットをセットする
     * @param p パケット
     */
    public void setPacket(PcapPacket p){ packet = p; }

    /**
     * パケットをゲットする
     * @return パケット
     * @deprecated ヘッダのゲッターを使ってね
     */
    public PcapPacket getPacket(){ return packet; }

    /**
     * パケットがセットされているか
     * @return パケットの有無
     */
    public boolean hasPacket(){ return packet != null; }

    /**
     * パケットに含まれる全てのヘッダを返す
     * @return ヘッダ配列
     * @throws IOException
     */
    public Header[] getAllHeader() throws IOException {
        return packet.getAllHeaders();
    }

    /*
    packetがnullの場合，
    packet.hasHeader(Class<T>); 及びpacket.getHeader(Class<T>);
    が例外を吐く．
    しかし，hasPacket()がfalseの場合（packet==null）は
    packet.hasHeader(Class<T>);
    の命令文を実行しない（Javaの作法）ため，大丈夫
     */

    /**
     * パケットにEthernetヘッダが含まれているか
     * @return Ethernetヘッダの有無
     */
    public boolean hasEthernet() {
        try{
            return hasPacket() && packet.hasHeader(Ethernet2.class);
        }catch(IllegalArgumentException e){
            return false;
        }catch(CodecCreateException e){
            return false;
        }catch(IOException e){
            return false;
        }
    }

    /**
     * パケットにIEEE802.3ヘッダが含まれているか
     * @return IEEE802.3ヘッダの有無
     */
    public boolean hasIeee802dot3() {
        try{
            return hasPacket() && packet.hasHeader(IEEE802dot3.class);
        }catch(IllegalArgumentException e){
            return false;
        }catch(CodecCreateException e){
            return false;
        }catch(IOException e){
            return false;
        }
    }

    /**
     * パケットにIPv4ヘッダが含まれているか
     * @return IPv4ヘッダの有無
     */
    public boolean hasIp4() {
        try{
            return hasPacket() && packet.hasHeader(Ip4.class);
        }catch(IllegalArgumentException e){
            return false;
        }catch(CodecCreateException e){
            return false;
        }catch(IOException e){
            return false;
        }
    }

    /**
     * パケットにTCPヘッダが含まれているか
     * @return TCPヘッダの有無
     */
    public boolean hasTcp() {
        try{
            return hasPacket() && packet.hasHeader(Tcp.class);
        }catch(IllegalArgumentException e){
            return false;
        }catch(CodecCreateException e){
            return false;
        }catch(IOException e){
            return false;
        }
    }

    /**
     * パケットにUDPヘッダが含まれているか
     * @return UDPヘッダの有無
     */
    public boolean hasUdp() {
        try{
            return hasPacket() && packet.hasHeader(Udp.class);
        }catch(IllegalArgumentException e){
            return false;
        }catch(CodecCreateException e){
            return false;
        }catch(IOException e){
            return false;
        }

    }

    /**
     * パケットにICMPヘッダが含まれているか
     * @return ICMPヘッダの有無
     */
    public boolean hasIcmp() {
        try{
            return hasPacket() && packet.hasHeader(Icmp.class);
        }catch(IllegalArgumentException e){
            return false;
        }catch(CodecCreateException e){
            return false;
        }catch(IOException e){
            return false;
        }
    }

    /**
     * パケットにARPヘッダが含まれているか
     * @return ARPヘッダの有無
     */
    public boolean hasArp() {
        try{
            return hasPacket() && packet.hasHeader(Arp.class);
        }catch(IllegalArgumentException e){
            return false;
        }catch(CodecCreateException e){
            return false;
        }catch(IOException e){
            return false;
        }
    }

    /**
     * Ethernetヘッダを取得する．ヘッダがない場合はnullを返す．
     * @return Ethernetヘッダ
     */
    public Ethernet2 getEthernet() {
        try {
            return hasEthernet()? packet.getHeader(Ethernet2.class): null;
        }catch(IOException e){
            return null;
        }
    }

    /**
     * IEEE802.3ヘッダを取得する．ヘッダがない場合はnullを返す．
     * @return IEEE802.3ヘッダ
     */
    public IEEE802dot3 getIeee802dot3() {
        try {
            return hasIeee802dot3()? packet.getHeader(IEEE802dot3.class): null;
        }catch(IOException e){
            return null;
        }
    }

    /**
     * IPv4ヘッダを取得する．ヘッダがない場合はnullを返す．
     * @return IPv4ヘッダ
     */
    public Ip4 getIp4() {
        try {
            return hasIp4()? packet.getHeader(Ip4.class): null;
        }catch(IOException e){
            return null;
        }
    }

    /**
     * TCPヘッダを取得する．ヘッダがない場合はnullを返す．
     * @return TCPヘッダ
     */
    public Tcp getTcp() {
        try {
            return hasTcp()? packet.getHeader(Tcp.class): null;
        }catch(IOException e){
            return null;
        }

    }

    /**
     * UDPヘッダを取得する．ヘッダがない場合はnullを返す．
     * @return UDPヘッダ
     */
    public Udp getUdp() {
        try {
            return hasUdp()? packet.getHeader(Udp.class): null;
        }catch(IOException e){
            return null;
        }
    }

    /**
     * ICMPヘッダを取得する．ヘッダがない場合はnullを返す．
     * @return ICMPヘッダ
     */
    public Icmp getIcmp() {
        try {
            return hasIcmp()? packet.getHeader(Icmp.class): null;
        }catch(IOException e){
            return null;
        }
    }

    /**
     * ARPヘッダを取得する．ヘッダがない場合はnullを返す．
     * @return ARPヘッダ
     */
    public Arp getArp() {
        try {
            return hasArp()? packet.getHeader(Arp.class): null;
        }catch(IOException e){
            return null;
        }
    }

    /**
     * パケットのタイムスタンプを返す．パケットがない場合は-1を返す．
     * @return 秒数，失敗した場合 -1
     */
    public long getSeconds() {
        try {
            return hasPacket()? packet.getTimestampSeconds(): -1;
        }catch(IOException e){
            return -1;
        }
    }

    /**
     * パケットのタイムスタンプを返す．パケットがない場合は-1を返す．
     * @return ナノ秒，失敗した場合 -1
     */
    public long getNanos() {
        try {
            return hasPacket()? packet.getTimestampNanos(): -1;
        }catch(IOException e){
            return -1;
        }

    }
}
