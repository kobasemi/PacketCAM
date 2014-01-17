package jp.ac.kansai_u.kutc.firefly.packetcam.opengl;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.opengl.GLSurfaceView;
import jp.ac.kansai_u.kutc.firefly.packetcam.readpcap.PacketAnalyser;
import jp.ac.kansai_u.kutc.firefly.packetcam.readpcap.PcapManager;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Enum;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Switch;
import org.jnetstream.capture.file.pcap.PcapPacket;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * OpenGLの描画を行う
 *
 * @author Kousaka akasaka
 */
public class EffectRenderer implements GLSurfaceView.Renderer
	{
		private static final String TAG = EffectRenderer.class.getSimpleName();

		// オブジェクトを格納するリスト
		List<DrawBlendingRectangle> drawBlendingRectangleList = new ArrayList<DrawBlendingRectangle>();

		int mWidth = 0, mHeight = 0;

		private Switch mSwitch = Switch.getInstance();

		private DrawCamera mDrawCamera;

		PcapPacket packet = null;
		/**
		 * スレッドセーフなキュー，インスタンスはPcapManagerから取得する
		 * @see jp.ac.kansai_u.kutc.firefly.packetcam.readpcap.ConcurrentPacketsQueue
		 */
		Queue<PcapPacket> packetsQueue;
		/**
		 * パケット解析インスタンス
		 * @see jp.ac.kansai_u.kutc.firefly.packetcam.readpcap.PacketAnalyser
		 */
		PacketAnalyser pa = new PacketAnalyser();

		/**
		 * オブジェクト描画用の頂点座標に関するバッファ
		 */
		static FloatBuffer rectangleBuffer = null;
		static FloatBuffer pointBuffer = null;

        // MACアドレスを保持する
        // 大会用パケットは2種類しかないため，この方法で判別する
        String typeMacAddress = null;

		/**
		 * GLSurfaceViewのRendererが生成された際に呼ばれる
		 *
		 * @param gl
		 * @param config
		 */
		public void onSurfaceCreated (GL10 gl, EGLConfig config)
			{
				mDrawCamera = new DrawCamera();
				mDrawCamera.generatedTexture(gl);
				// キューの取得
				packetsQueue = PcapManager.getInstance().getConcurrentPacketsQueue();
			}


		/**
		 * 画面が変更された時に呼び出されるメソッド
		 * １：画面が生成された時（onSurfaceCreatedの後）
		 * ２：画面サイズが変わった時（縦と横で端末が切り替わった時）
		 *
		 * @param gl
		 * @param width
		 * @param height
		 */
		public void onSurfaceChanged (GL10 gl, int width, int height)
			{
				mWidth = width;
				mHeight = height;

				// ビューモードの設定
				// GL上で扱うスクリーンをどこにどれくらいの大きさで表示するのかを設定する
				// 左上が0,0
				// 以下のコードで全画面になる
				gl.glViewport(0, 0, width, height);

				mDrawCamera.setUpCamera();

				// ネイティブのメモリ領域にバッファを作成する
				float point[] = {0.f, 0.f,};  // x, y
				float rectangle[] = {
						-1.0f, 1.0f,  // 左上
						-1.0f, -1.0f,  // 左下
						1.0f, 1.0f,  // 右上
						1.0f, -1.0f,  // 右下
				};
				pointBuffer = makeFloatBuffer(point);
				rectangleBuffer = makeFloatBuffer(rectangle);
			}


        short dIpPoint;
        short sIpPoint;
        short sizeX = 0;
        short sizeY = 0;
        short ttl;
        Enum.COLOR color = null;

		/**
		 * 描画処理のループ
		 *
		 * @param gl
		 */
		public void onDrawFrame (GL10 gl)
			{
				// キューの先頭パケットを取得．到着していない場合でもぬるぽを出さない
				// パケットは1秒ごとに装填される
				packet = packetsQueue.poll();
				if (packet != null)
					// パケットが到着した場合，描画オブジェクトを追加する
					{
                        // 解析機にパケットをセットする
                        pa.setPacket(packet);

						// ICMPヘッダ以外のパケットを受信した際に，オブジェクトを作成する
						if (!pa.hasIcmp())
							{
								// 描画オブジェクト用のパラメータを作成する
								buildDrawBlendingRectangleParametor();

                                /**
                                 * @see jp.ac.kansai_u.kutc.firefly.packetcam.opengl.DrawBlendingRectangle
                                 */
                                drawBlendingRectangleList.add(new DrawBlendingRectangle(dIpPoint, sIpPoint, sizeX, sizeY, color, ttl));
							}
						packet = null;
					}

				if (mSwitch.getDrawstate() == Enum.DRAWSTATE.PREPARATION)
					{
						return;
					}

				mDrawCamera.draw(gl);

				// GLViewクラスのvisibility変数をいじることで、描画のON・OFFが可能
				//SwitchクラスのswitchVisibilityメソッドをcallして描画のON・OFFを行う
				if (mSwitch.getVisibility() == Enum.VISIBILITY.VISIBLE)
					{
						for (int i = 0; i < this.drawBlendingRectangleList.size(); i++)
							{
								if (drawBlendingRectangleList.get(i).getDeadFlag())
									{
										drawBlendingRectangleList.remove(i);
									}
								else
									{
										drawBlendingRectangleList.get(i).draw(gl);
									}
							}
					}

				// OpenGLで描画したフレームバッファからbitmapを生成する
				if (mSwitch.getShutter())
					{
						createOpenGLBitmap(gl);
						mSwitch.switchShutter();
					}
			}

        /**
         * DrawBlendingRectangleクラス用のパラメータを作成する
         */
        public void buildDrawBlendingRectangleParametor(){

            // 初期化
            sizeX = 0;
            sizeY = 0;
            dIpPoint = 0;
            sIpPoint = 0;
            ttl = 0;
            color = Enum.COLOR.MAGENTA;

            // Ethernetヘッダから，スイッチ用MACアドレスを取得する
            if (pa.hasEthernet()){
                // MACアドレスを取得
                if(typeMacAddress == null)
                    // 初期化
                    typeMacAddress = pa.getMacAddressDestination().toString();
                else
                    // MACアドレスのタイプスイッチ
                    if(typeMacAddress.equals(pa.getMacAddressSource().toString()))
                        typeMacAddress = pa.getMacAddressDestination().toString();
                    else
                        typeMacAddress = pa.getMacAddressSource().toString();
            }

            // IP4ヘッダから座標位置を算出，及びオブジェクトの寿命を設定する
            if (pa.hasIp4()){
                try{
                    // IP4.source/destination から座標位置を算出する
                    dIpPoint = xorIP(pa.getIpAddressDestination().toString());
                    sIpPoint = xorIP(pa.getIpAddressSource().toString());

                    if (!typeMacAddress.equals(pa.getMacAddressDestination().toString())){
                        dIpPoint = (short) (255 - dIpPoint);
						sIpPoint = (short) (255 - sIpPoint);
                    }

                    // IP4.ttlから寿命を設定する
                    ttl = pa.getIpTtl();
                }catch (IllegalArgumentException e){
                    e.printStackTrace();
                }catch (IndexOutOfBoundsException e){
                    e.printStackTrace();
                }
            }

            // TCPヘッダからサイズとカラーを算出する
            if (pa.hasTcp()){
                // TCP.windowからサイズを算出する
                if(pa.getTcpWindow() > 0){
                    short[] size = calcSize(pa.getTcpWindow());
                    sizeX = size[0];
                    sizeY = size[1];
                }

                // TCP.portからカラーを指定する
                // PORT番号は，分割せずそのまま利用する
                if (typeMacAddress.equals(pa.getMacAddressDestination().toString()))
                    color = choiceColorFromPort(pa.getTcpPortSource());
                else
                    color = choiceColorFromPort(pa.getTcpPortDestination());
            }

            // UDPヘッダからサイズとカラーを算出する
            if (pa.hasUdp()){
                // サイズは固定
                sizeX = 30;
                sizeY = 30;

                try {
                    // UDP.portからカラーを指定する
                    // PORT番号は，分割せずそのまま利用する
                    if (typeMacAddress.equals(pa.getMacAddressDestination().toString()))
                        color = choiceColorFromPort(pa.getUdpPortSource());
                    else
                        color = choiceColorFromPort(pa.getUdpPortDestination());
                }catch(IllegalArgumentException e){
                    e.printStackTrace();
                }catch(IndexOutOfBoundsException e){
                    e.printStackTrace();
                }
            }
        }


        // パラメータ作成用メソッド群
        /**
         * TCPヘッダ又はUDPヘッダのポート番号を元に条件判定を行い，カラーを返す
         * @param num ポート番号
         * @return Enum.COLOR
         */
        private Enum.COLOR choiceColorFromPort(int num)
            {
                if ((0 <= num && num <= 79) || (81 <= num && num <= 442) || (444 <= num && num <= 1023))
                    {
                        return Enum.COLOR.RED;
                    }
                    else if (num == 80)
                    {
                        return Enum.COLOR.GREEN;
                    }
                    else if (num == 443)
                    {
                        return Enum.COLOR.BLUE;
                    }
                    else if (1024 <= num && num <= 30000)
                    {
                        return Enum.COLOR.BLACK;
                    }
                    else if (30001 <= num && num <= 50000)
                    {
                        return Enum.COLOR.WHITE;
                    }
                    else if (50001 <= num && num <= 55000)
                    {
                        return Enum.COLOR.CYAN;
                    }
                    else if (55001 <= num && num <= 60000)
                    {
                        return Enum.COLOR.YELLOW;
                    }
                    else
                    {
                        return Enum.COLOR.MAGENTA;
                    }

            }

        /**
         * IPアドレスの各オクテットをXOR演算し，結果を返す
         * @param ipaddr String型のIPアドレス
         * @return short型で，10で割って2桁に処理したXOR計算結果
         */
        private short xorIP(String ipaddr)
            {
                String[] point = ipaddr.split("\\.");

                short[] ipaddrPoint = new short[point.length];
                for (int i = 0; i < point.length; i++)
                {
                    ipaddrPoint[i] = Short.valueOf(point[i]);
                }

                short xorValue = (short) (ipaddrPoint[0] ^ ipaddrPoint[1]);
                xorValue = (short)(xorValue ^ ipaddrPoint[2]);
                xorValue = (short)(xorValue ^ ipaddrPoint[3]);

                return xorValue;
            }

        /**
         * windowサイズから，オブジェクト生成用の座標を求める
         * @param value int型の数値
         * @return だいたい真ん中で分けられ，10で割って2桁に処理されたshort配列
         */
        private short[] calcSize(int value)
            {
                // ポート番号をchar配列に
                char[] portChar = String.valueOf(value).toCharArray();

                if ((portChar.length % 2) != 0)
                    {
                        // 桁数が奇数の場合の処理
                        // だいたい真ん中を求める
                        int aboutCenter = portChar.length / 2;

                        char[] firstChar = new char[aboutCenter + 1];

                        int j = 0;
                        for (int i = 0; i < aboutCenter + 1; i++)
                        {
                            firstChar[i] = portChar[i];
                            j++;
                        }

                        char[] secondChar = new char[aboutCenter];

                        for (int i = 0; i < aboutCenter; i++)
                        {
                            secondChar[i] = portChar[j];
                            j++;
                        }

                        // firstの方が桁数が多くなるはず
                        short first = Short.valueOf(String.valueOf(firstChar));
                        short second = Short.valueOf(String.valueOf(secondChar));

                        // PORT番号の幅は，0~65535
                        // firstが3桁以上の場合，ひたすら2で割って2桁に抑える
                        first = digitReducer(first);
                        second = digitReducer(second);

                        short[] data = new short[2];
                        data[0] = first;
                        data[1] = second;
                        return data;
                    }
                else
                    {
                        // 桁数が偶数の場合
                        // 4桁か，2桁
                        int center = portChar.length / 2;
                        char[] firstChar = new char[center];

                        int j = 0;
                        for (int i = 0; i < center; i++)
                        {
                            firstChar[i] = portChar[i];
                            j++;
                        }

                        char[] secondChar = new char[center];

                        for (int i = 0; i < center; i++)
                        {
                            secondChar[i] = portChar[j];
                            j++;
                        }

                        short first = Short.valueOf(String.valueOf(firstChar));
                        short second = Short.valueOf(String.valueOf(secondChar));

                        first = digitReducer(first);
                        second = digitReducer(second);

                        short[] data = new short[2];
                        data[0] = first;
                        data[1] = second;
                        return data;
                    }
            }

        /**
         * 数値内にマイナスが含まれている場合，マイナスを取り除く
         * @param num 処理するshort型の数値
         * @return マイナスが取り除かれたshort型の数値
         */
        private short minusReducer(short num)
            {
                if (num >= 0) return num;

                char[] oldNumCharArray = String.valueOf(num).toCharArray();
                char[] newNumCharArray = new char[oldNumCharArray.length - 1];

                System.arraycopy(oldNumCharArray, 1, newNumCharArray, 0, oldNumCharArray.length - 1);

                return Short.valueOf(String.valueOf(newNumCharArray));
            }


        // 3桁以上の値を10で割って2桁に抑える

        /**
         * 3桁以上の値を10で割って2桁に抑える
         * @param source 処理するshort型の数値
         * @return 2桁のshort型の数値
         */
        private short digitReducer(short source)
            {
                if (String.valueOf(source).length() > 2)
                    {
                        source = (short)(source / 10);
                        digitReducer(source);
                    }

                return source;
            }

		/**
		 * OpenGLのフレームバッファからBitmapを作る
		 * http://stackoverflow.com/questions/3310990/taking-screenshot-of-android-opengl
		 *
		 * @param gl
		 */
		private void createOpenGLBitmap (GL10 gl)
			{
				// Bitmap作ったあとに、透明化の処理を施す？
				if (mWidth == 0 || mHeight == 0)
					{
						return;
					}
				int size = mWidth * mHeight;

				//region createBitmapFromFrameBuffer
				ByteBuffer bb = ByteBuffer.allocateDirect(size * 4);

				// OpenGLのフレームバッファからピクセル情報を読み込む
				gl.glReadPixels(0, 0, mWidth, mHeight, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, bb);

				Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
				bitmap.copyPixelsFromBuffer(bb);
				//endregion

				// 生成されたBitmap画像は上下が反転しているため，修正する
				Matrix matrix = new Matrix();
				matrix.preScale(1, -1);

				Bitmap correctBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);

				SaveSDCard.save(correctBitmap);
			}

		/**
		 * OpenGL ESはVMのヒープ領域は使用できないため，ネイティブのメモリ領域に書き込む
		 *
		 * @param buf float型の配列
		 * @return ネイティブ領域に書き込まれたバッファの参照
		 */
		public static FloatBuffer makeFloatBuffer (float[] buf)
			{
				ByteBuffer bb = ByteBuffer.allocateDirect(buf.length * 4);
				bb.order(ByteOrder.nativeOrder());
				FloatBuffer fb = bb.asFloatBuffer();
				fb.put(buf);
				fb.position(0);
				return fb;
			}

		/**
		 * OpenGL ESはVMのヒープ領域は使用できないため，ネイティブのメモリ領域に書き込む
		 *
		 * @param buf int型の配列
		 * @return ネイティブ領域に書き込まれたバッファの参照
		 */
		public static IntBuffer makeIntBuffer (int[] buf)
			{
				ByteBuffer bb = ByteBuffer.allocateDirect(buf.length * 4);
				bb.order(ByteOrder.nativeOrder());
				IntBuffer ib = bb.asIntBuffer();
				ib.put(buf);
				ib.position(0);
				return ib;
			}

	}