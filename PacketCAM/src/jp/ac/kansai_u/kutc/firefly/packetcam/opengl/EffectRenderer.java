package jp.ac.kansai_u.kutc.firefly.packetcam.opengl;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.opengl.GLSurfaceView;
import android.util.Log;
import jp.ac.kansai_u.kutc.firefly.packetcam.readpcap.PacketAnalyser;
import jp.ac.kansai_u.kutc.firefly.packetcam.readpcap.PcapManager;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Enum;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Switch;
import org.jnetstream.capture.file.pcap.PcapPacket;
import org.jnetstream.protocol.lan.Ethernet2;
import org.jnetstream.protocol.tcpip.Ip4;
import org.jnetstream.protocol.tcpip.Tcp;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.io.IOException;
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

		int colorFlg = 0;

		// パケットファイルの初回読み込み時のみ，MACアドレスをセットする
		public static boolean macFlg = false;

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

		String dEthernetStrFlg = null;

		/**
		 * GLSurfaceViewのRendererが生成された際に呼ばれる
		 *
		 * @param gl
		 * @param config
		 */
		public void onSurfaceCreated (GL10 gl, EGLConfig config)
			{
				Log.i(TAG, "onSurfaceCreated()");

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
				Log.i(TAG, "onSurfaceChanged()");
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
								 * @see DrawBlendingRectangle
								 */
								drawBlendingRectangleList.add(new DrawBlendingRectangle(dIpPoint, sIpPoint, sizeX, sizeY, color));
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
								drawBlendingRectangleList.get(i).draw(gl);
							}
					}

				// OpenGLで描画したフレームバッファからbitmapを生成する
				if (mSwitch.getShutter() == true)
					{
						Log.d(TAG, "callCreateBitmap");
						createOpenGLBitmap(gl);
						mSwitch.switchShutter();
					}
			}

        /**
         * DrawBlendingRectangleクラス用のパラメータを作成する
         */
        public void buildDrawBlendingRectangleParametor(){

            if (pa.hasEthernet() && !macFlg)
            {
                // MACアドレス取得
                Ethernet2 ethernet2 = pa.getEthernet();

                try
                {
                    // MACアドレスを取得
                    dEthernetStrFlg = ethernet2.destination().toString();

                    // byte配列からStringに変換
                    Log.i(TAG, "dEthernetStrFlg = " + dEthernetStrFlg);
                    macFlg = true;
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

            // 解析機からヘッダを取り出す場合
            // 先にヘッダがあるかを確認してほしい
            if (pa.hasIp4() && pa.hasEthernet() && dEthernetStrFlg != null && !pa.hasIcmp())
            {
                Ethernet2 ethernet2 = pa.getEthernet();

                try
                {
                    // IPアドレスの各オクテットをXOR演算したものをオブジェクトのXY座標点として利用する
                    // MACアドレスを取得
                    String dEthernetStr = ethernet2.destination().toString();
//										Log.i(TAG, "dEthernetStr = " + dEthernetStr);

                    // IPヘッダのIPアドレスを取得する
                    Ip4 ip4 = pa.getIp4();

                    String dIPStr = ip4.destination().toString();
//										Log.i(TAG, "dIPStr = " + dIPStr);

                    String sIPStr = ip4.source().toString();
//										Log.i(TAG, "sIPStr = " + sIPStr);

                    dIpPoint = DrawBlendingRectangle.xorIP(dIPStr);
                    Log.i(TAG, "dIpPoint = " + dIpPoint);

                    sIpPoint = DrawBlendingRectangle.xorIP(sIPStr);
                    Log.i(TAG, "sIpPoint = " + sIpPoint);

                    if (!dEthernetStrFlg.equals(dEthernetStr))
                    {
                        dIpPoint = (short) (255 - dIpPoint);
                        Log.i(TAG, "revdIpPoint = " + dIpPoint);
//												sIpPoint = (short) (255 - sIpPoint);
                        Log.i(TAG, "revsIpPoint = " + sIpPoint);
                    }

                    // サイズ指定はTCP or UDPを利用する
                    if (pa.hasTcp())
                    {
                        // Tcpの場合，windowで求めたもので
                        Tcp tcp = pa.getTcp();

                        short window = tcp.window();
                        Log.i(TAG, "window = " + window);
                        if (0 < window)
                        {
                            short[] size = DrawBlendingRectangle.calcSize(window);

                            sizeX = size[0];
                            sizeY = size[1];
                        }
                        else
                        {
                            sizeX = 0;
                            sizeY = 0;
                        }
                        Log.i(TAG, "TcpSizeX = " + sizeX);
                        Log.i(TAG, "TcpSizeY = " + sizeY);
                    }
                    else if (pa.hasUdp())
                    {
                        sizeX = 30;
                        sizeY = 30;
                    }



					// TCPかUDPのポート番号から，カラーを指定する
					// PORT番号は，分割せずそのまま利用する
					if (pa.hasTcp())
						{
							if (dEthernetStrFlg.equals(dEthernetStr))
								{
									int sPort = pa.getTcpPortSource();

									color = DrawBlendingRectangle.choiceColorFromPort(sPort);
								}
							else
								{
									int dPort = pa.getTcpPortDestination();

									color = DrawBlendingRectangle.choiceColorFromPort(dPort);
								}
						}
					if (pa.hasUdp())
						{
							if (dEthernetStrFlg.equals(dEthernetStr))
								{
									int sPort = pa.getUdpPortSource();

									color = DrawBlendingRectangle.choiceColorFromPort(sPort);
								}
							else
								{
									int dPort = pa.getUdpPortDestination();

									color = DrawBlendingRectangle.choiceColorFromPort(dPort);
								}
						}
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                catch (IllegalArgumentException e)
                {
                    e.printStackTrace();
                }
                catch (IndexOutOfBoundsException e)
                {
                    e.printStackTrace();
                }
            }
        }

		/**
		 * 指定したオブジェクトを破棄する
		 *
		 * @param num 何番目のオブジェクトを破棄するか
		 */
		public void removeGraphic (int num)
			{
				Log.d(TAG, "removeGraphic()");
				drawBlendingRectangleList.remove(num);
			}


		/**
		 * OpenGLのフレームバッファからBitmapを作る
		 * http://stackoverflow.com/questions/3310990/taking-screenshot-of-android-opengl
		 *
		 * @param gl
		 */
		private void createOpenGLBitmap (GL10 gl)
			{
				Log.d(TAG, "createOpenglBitmap()");
				// Bitmap作ったあとに、透明化の処理を施す？
				if (mWidth == 0 || mHeight == 0)
					{
						Log.d(TAG, "Error1");
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