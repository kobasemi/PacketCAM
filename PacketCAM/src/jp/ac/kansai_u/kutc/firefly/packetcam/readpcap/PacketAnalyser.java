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
 * 必要なのかは不明
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
     * パケットをセットする
     * @param p パケット
     */
    public void setPacket(PcapPacket p){
        packet = p;
    }

    /**
     * パケットをゲットする
     * @return パケット
     * @deprecated ヘッダのゲッターを使ってね
     */
    public PcapPacket getPacket(){
        return packet;
    }

    /**
     * パケットがセットされているか
     * @return パケットの有無
     */
    public boolean hasPacket(){
        return packet != null;
    }

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
     * @throws IOException
     * @throws CodecCreateException
     */
    public Ethernet2 getEthernet() throws IOException, CodecCreateException {
        return hasEthernet()? packet.getHeader(Ethernet2.class): null;
    }

    /**
     * IEEE802.3ヘッダを取得する．ヘッダがない場合はnullを返す．
     * @return IEEE802.3ヘッダ
     * @throws IOException
     * @throws CodecCreateException
     */
    public IEEE802dot3 getIeee802dot3() throws IOException, CodecCreateException {
        return hasIeee802dot3()? packet.getHeader(IEEE802dot3.class): null;
    }

    /**
     * IPv4ヘッダを取得する．ヘッダがない場合はnullを返す．
     * @return IPv4ヘッダ
     * @throws IOException
     * @throws CodecCreateException
     */
    public Ip4 getIp4() throws IOException, CodecCreateException {
        return hasIp4()? packet.getHeader(Ip4.class): null;
    }

    /**
     * TCPヘッダを取得する．ヘッダがない場合はnullを返す．
     * @return TCPヘッダ
     * @throws IOException
     * @throws CodecCreateException
     */
    public Tcp getTcp() throws IOException, CodecCreateException {
        return hasTcp()? packet.getHeader(Tcp.class): null;
    }

    /**
     * UDPヘッダを取得する．ヘッダがない場合はnullを返す．
     * @return UDPヘッダ
     * @throws IOException
     * @throws CodecCreateException
     */
    public Udp getUdp() throws IOException, CodecCreateException {
        return hasUdp()? packet.getHeader(Udp.class): null;
    }

    /**
     * ICMPヘッダを取得する．ヘッダがない場合はnullを返す．
     * @return ICMPヘッダ
     * @throws IOException
     * @throws CodecCreateException
     */
    public Icmp getIcmp() throws IOException, CodecCreateException {
        return hasIcmp()? packet.getHeader(Icmp.class): null;
    }

    /**
     * ARPヘッダを取得する．ヘッダがない場合はnullを返す．
     * @return ARPヘッダ
     * @throws IOException
     * @throws CodecCreateException
     */
    public Arp getArp() throws IOException, CodecCreateException {
        return hasArp()? packet.getHeader(Arp.class): null;
    }

    /**
     * パケットのタイムスタンプを返す．パケットがない場合は-1を返す．
     * @return 秒数
     * @throws IOException
     */
    public long getSeconds() throws IOException {
        return hasPacket()? packet.getTimestampSeconds(): -1;
    }

    /**
     * パケットのタイムスタンプを返す．パケットがない場合は-1を返す．
     * @return ナノ秒
     * @throws IOException
     */
    public long getNanos() throws IOException {
        return hasPacket()? packet.getTimestampNanos(): -1;
    }
}
