package jp.ac.kansai_u.kutc.firefly.packetcam.readpcap;

import com.slytechs.utils.net.EUI48;
import com.slytechs.utils.net.Ip4Address;
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

    // 参照先を用意する
    Ethernet2 eth;
    Ip4 ip4;
    Tcp tcp;
    Udp udp;
    Icmp icmp;

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

    // それぞれのパケットの情報を取得するメソッド群
    // 本アプリケーション内で使用するもののみ用意しているため
    // 下記メソッド以外からも取得できる情報がある

    /**
     * 送信元MACアドレスを返す
     * @return source/ null（失敗）
     */
    public EUI48 getMacAddressSource(){
        if((eth = getEthernet()) != null)
            try {
                return eth.source();
            } catch(IOException e) {
                e.printStackTrace();
            }
        return null;
    }

    /**
     * 送信先MACアドレスを返す
     * @return destination/ null（失敗）
     */
    public EUI48 getMacAddressDestination(){
        if((eth = getEthernet()) != null)
            try {
                return eth.destination();
            } catch(IOException e) {
                e.printStackTrace();
            }
        return null;
    }

    /**
     * 送信元IPアドレスを返す
     * @return source/ null（失敗）
     */
    public Ip4Address getIpAddressSource(){
        if((ip4 = getIp4()) != null)
            return ip4.source();
        return null;
    }

    /**
     * 送信先IPアドレスを返す
     * @return destination/ null（失敗）
     */
    public Ip4Address getIpAddressDestination(){
        if((ip4 = getIp4()) != null)
            return ip4.destination();
        return null;
    }

    /**
     * IPヘッダのTTL(Time to Live)を返す
     * @return ttl/ -1（失敗）
     */
    public short getIpTtl(){
		try
			{
				if((ip4 = getIp4()) != null)
					return (short)(ip4.ttl() & 0xFF);
				return -1;
			}
		catch (IndexOutOfBoundsException e)
			{
				e.printStackTrace();
				return -1;
			}
	}

    /**
     * IPヘッダに含まれるプロトコル情報を返す
     * 例 1: ICMP, 6: TCP, 17: UDP
     * @return protocol/ -1（失敗）
     */
    public byte getIpProtocol(){
        if((ip4 = getIp4()) != null)
            return ip4.protocol();
        return -1;
    }

    /**
     * 送信元ポートを返す
     * @return source/ -1（失敗）
     */
    public int getTcpPortSource(){
        if((tcp = getTcp()) != null)
            return (int)(char)tcp.source();
        return -1;
    }

    /**
     * 送信先ポートを返す
     * @return destination/ -1（失敗）
     */
    public int getTcpPortDestination(){
        if((tcp = getTcp()) != null)
            return (int)(char)tcp.destination();
        return -1;
    }

    /**
     * TCPヘッダに含まれるフラグ情報を返す
     * 例 URG|ACK|PSH|RST|SYN|FIN
     * @return flags/ -1（失敗）
     */
    public int getTcpFlags(){
        if((tcp = getTcp()) != null)
            return tcp.flags();
        return -1;
    }

    /**
     * TCPヘッダに含まれるWindow（サイズ）情報を返す
     * @return window/ -1（失敗）
     */
    public int getTcpWindow(){
        if((tcp = getTcp()) != null)
            return (int)(char)tcp.window();
        return -1;
    }

    /**
     * 送信元ポートを返す
     * @return source/ -1（失敗）
     */
    public int getUdpPortSource(){
        if((udp = getUdp()) != null)
            return (int)(char)udp.source();
        return -1;
    }

    /**
     * 送信先ポートを返す
     * @return destination/ -1（失敗）
     */
    public int getUdpPortDestination(){
        if((udp = getUdp()) != null)
            return (int)(char)udp.destination();
        return -1;
    }

    /**
     * ICMPに含まれるタイプ情報を返す
     * 例 0: エコー応答, 8: エコー要求, 11: 時間超過
     * @return type/ -1（失敗）
     */
    public byte getIcmpType(){
        if((icmp = getIcmp()) != null)
            return icmp.type();
        return -1;
    }

    /**
     * ICMPに含まれるタイプに関するコードを返す
     * 例 0: ネットワークが到達できない
     * @return code/ -1（失敗）
     */
    public byte getIcmpCode(){
        if((icmp = getIcmp()) != null)
            return icmp.code();
        return -1;
    }

    /**
     * パケットのタイムスタンプを返す
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
     * パケットのタイムスタンプを返す
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
