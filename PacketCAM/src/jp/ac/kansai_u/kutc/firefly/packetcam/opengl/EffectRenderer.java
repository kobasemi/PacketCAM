package jp.ac.kansai_u.kutc.firefly.packetcam.opengl;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.opengl.GLSurfaceView;
import android.util.Log;
import com.slytechs.utils.net.EUI48;
import jp.ac.kansai_u.kutc.firefly.packetcam.readpcap.PacketAnalyser;
import jp.ac.kansai_u.kutc.firefly.packetcam.readpcap.PcapManager;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Enum;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Switch;
import org.jnetstream.capture.file.pcap.PcapPacket;
import org.jnetstream.protocol.lan.Ethernet2;
import org.jnetstream.protocol.tcpip.Ip4;
import org.jnetstream.protocol.tcpip.Tcp;
import org.jnetstream.protocol.tcpip.Udp;

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
		 *
		 * @see jp.ac.kansai_u.kutc.firefly.packetcam.readpcap.ConcurrentPacketsQueue
		 */
		Queue<PcapPacket> packetsQueue;
		/**
		 * パケット解析インスタンス
		 *
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
					{
						// パケットが到着した場合，描画オブジェクトを追加する
						short sizeX = 0;
						short sizeY = 0;
						Enum.COLOR color = null;

						// 解析機にパケットをセットする
						pa.setPacket(packet);

						// TODO ここでパケット情報を用いてエフェクトを追加していく
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
						if (pa.hasIp4() && pa.hasEthernet() && dEthernetStrFlg != null)
							{
								Ethernet2 ethernet2 = pa.getEthernet();

								try
									{
										// IPアドレスの末尾アドレスをオブジェクトの座標情報として用いる
										// TODO 末端のみ利用しているのを，01からXORを利用してすべての情報を使うように
										// 1オクテット XOR 2オクテット
										// 上記結果 XOR 3オクテット
										// 上記結果 XOR 4オクテット

										// MACアドレスを取得
//										String dEthernetStr = new String(ethernet2.getDestinationRaw(), "UTF-8");
										String dEthernetStr = ethernet2.destination().toString();
										Log.i(TAG, "dEthernetStr = " + dEthernetStr);

										// IPヘッダのIPアドレスを取得する
										Ip4 ip4 = pa.getIp4();

										String dIPStr = ip4.destination().toString();
										Log.i(TAG, "dIPStr = " + dIPStr);

										String sIPStr = ip4.source().toString();
										Log.i(TAG, "sIPStr = " + sIPStr);

										short dIpPoint = DrawBlendingRectangle.calcIP(dIPStr);
										Log.i(TAG, "dIpPoint = " + dIpPoint);

										short sIpPoint = DrawBlendingRectangle.calcIP(sIPStr);
										Log.i(TAG, "sIpPoint = " + sIpPoint);


										if (!dEthernetStrFlg.equals(dEthernetStr))
											{
												dIpPoint = (short) (100 - dIpPoint);
												Log.i(TAG, "revdIpPoint = " + dIpPoint);
												sIpPoint = (short) (100 - sIpPoint);
												Log.i(TAG, "revsIpPoint = " + sIpPoint);
											}


										// サイズ指定はTCP or UDPを利用する
										if (pa.hasTcp())
											{
												// Tcpの場合，window/flagで求めたもので
												Tcp tcp = pa.getTcp();



												short window = tcp.window();
												Log.i(TAG, "window = " + window);
												if (0 < window)
													{
														short[] size = DrawBlendingRectangle.calcPort(window);

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

//												// 描画位置：TCPヘッダのdport
//												short[] dportPoint = DrawBlendingRectangle.calcPort(dport);
//
//												// 描画サイズ：TCPヘッダのsport
//												short[] sportPoint = DrawBlendingRectangle.calcPort(sport);
											}
										else if (pa.hasUdp())
											{
												// TODO サイズ指定を，正方形固定にする（サイズは適当に決める）
												Udp udp = pa.getUdp();

												if (dEthernetStrFlg.equals(dEthernetStr))
													{
														short dport = udp.destination();
														short[] size = DrawBlendingRectangle.calcPort(dport);

														sizeX = size[0];
														sizeY = size[1];

														Log.i(TAG, "dUdpSizeX = " + sizeX);
														Log.i(TAG, "dUdpSizeY = " + sizeY);
													}
												else
													{
														short sport = udp.source();
														short[] size = DrawBlendingRectangle.calcPort(sport);

														sizeX = size[0];
														sizeY = size[1];

														Log.i(TAG, "sUdpSizeX = " + sizeX);
														Log.i(TAG, "sUdpSizeY = " + sizeY);
													}
											}
										else
											{
												sizeX = 25;
												sizeY = 25;

												Log.i(TAG, "sizeX = " + sizeX);
												Log.i(TAG, "sizeY = " + sizeY);
											}


										//TODO カラー指定を，ポート番号を使うようにする（MACアドレスで，dかsかを切り替え）
										// IPヘッダのlengthで分岐

										short length = ip4.length();

										short id = ip4.id();


										Log.i(TAG, "length = " + length);

										if (length < 40)
											{
												color = Enum.COLOR.RED;
											}
										else if (40 <= length && length < 50)
											{
												color = Enum.COLOR.GREEN;
											}
										else if (50 <= length && length < 60)
											{
												color = Enum.COLOR.BLUE;
											}
										else if (60 <= length && length < 70)
											{
												color = Enum.COLOR.CYAN;
											}
										else if (70 <= length && length < 80)
											{
												color = Enum.COLOR.MAGENTA;
											}
										else if (80 <= length && length < 500)
											{
												color = Enum.COLOR.YELLOW;
											}
										else if (500 <= length && length < 700)
											{
												color = Enum.COLOR.WHITE;
											}
										else
											{
												color = Enum.COLOR.BLACK;
											}

										Log.i(TAG, "color = " + color);


										drawBlendingRectangleList.add(new DrawBlendingRectangle(dIpPoint, sIpPoint, sizeX, sizeY, color));
									}
								catch (IOException e)
									{
										e.printStackTrace();
									}
								catch (IllegalArgumentException e)
									{
										e.printStackTrace();
									}

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
		 * 新たな描画オブジェクトを生成する
		 * Draw2Dに渡す引数は，xy座標及びwidth, height, 色情報
		 * なお，x, y, width, heightに関しては，
		 * 描画したい位置や描画図形の大きさをスクリーンのパーセンテージで指定する
		 * インスタンス化は次の2通りの方法がある
		 * - int型で0〜100[%]で渡す方法．
		 * ~ new DrawBlendingRectangle(0, 0, 100, 100, color);
		 * - float型で0.f〜1.fで渡す方法
		 * ~ new DrawBlendingRectangle(0.f, 0.f, 1.f, 1.f ,color);
		 * 2つの例はどちらも，左上から右下までを描画するもの
		 * 分かりやすい方法を使ったら良い
		 *
		 * @see DrawBlendingRectangle
		 */
		public void newGraphic ()
			{
				Log.d(TAG, "newGraphic()");
				DrawBlendingRectangle drawBlue = new DrawBlendingRectangle(0.f, 0.f, .25f, .25f, Enum.COLOR.BLUE);
				drawBlendingRectangleList.add(drawBlue);
				DrawBlendingRectangle drawGreen = new DrawBlendingRectangle(.25f, .25f, .75f, .75f, Enum.COLOR.GREEN);
				drawBlendingRectangleList.add(drawGreen);
				DrawBlendingRectangle drawRed = new DrawBlendingRectangle(.75f, .75f, .25f, .25f, Enum.COLOR.RED);
				drawBlendingRectangleList.add(drawRed);
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