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
import org.jnetstream.protocol.tcpip.Tcp;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Queue;

/**
 * OpenGLの描画を行う
 * @author akasaka kousaka
 */
public class EffectRenderer implements GLSurfaceView.Renderer
{
    private static final String TAG = EffectRenderer.class.getSimpleName();

    // オブジェクトを格納するリスト
    ArrayList<Draw2D> Draw2DList = new ArrayList<Draw2D>();

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
     * GLSurfaceViewのRendererが生成された際に呼ばれる
     *
     * @param gl
     * @param config
     */
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        Log.i(TAG, "onSurfaceCreated()");

        mDrawCamera = new DrawCamera();
        mDrawCamera.generatedTexture(gl);
        newGraphic();
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
    public void onSurfaceChanged(GL10 gl, int width, int height)
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
    }


    /**
     * 描画処理のループ
     *
     * @param gl
     */
    public void onDrawFrame(GL10 gl)
    {
        Log.i(TAG, "onDrawFrame");

        // キューの先頭パケットを取得．到着していない場合でもぬるぽを出さない
        packet = packetsQueue.poll();
        if(packet != null){
            // パケットが到着した場合，描画オブジェクトを追加する

            // 解析機にパケットをセットする
            pa.setPacket(packet);

			// TODO ここでパケット情報を用いてエフェクトを追加していく

            // 解析機からヘッダを取り出す場合
            // 先にヘッダがあるかを確認してほしい
            if(pa.hasTcp()){
                Tcp tcp = pa.getTcp();

                short dport = tcp.destination();
                short sport = tcp.source();

                // 描画位置：TCPヘッダのdport
                short[] dportPoint = calcPort(dport);

                // 描画サイズ：TCPヘッダのsport
                short[] sportPoint = calcPort(sport);
            }

            // カラー：ICMPのchecksumとか，あるいはsequenceとか

            // Draw2DList.add(new Draw2D(dportPoint[0], dportPoint[1], sportPoint[0], sportPoint[1], Enum.COLOR.BLACK));

            Draw2DList.add(new Draw2D(0, 0, 50, 20, Enum.COLOR.BLACK));
            packet = null;
        }

        if (mSwitch.getDrawstate() == Enum.DRAWSTATE.PREPARATION)
        {
            return;
        }

        mDrawCamera.draw(gl);

        // カメラプレビュー描画後に，ブレンドを有効化する
        gl.glEnable(GL10.GL_BLEND);

        // ブレンドモードを指定
        gl.glBlendFunc(GL10.GL_ONE_MINUS_DST_COLOR, GL10.GL_ONE_MINUS_SRC_ALPHA);

        // GLViewクラスのvisibility変数をいじることで、描画のON・OFFが可能
        //SwitchクラスのswitchVisibilityメソッドをcallして描画のON・OFFを行う
        if (mSwitch.getVisibility() == Enum.VISIBILITY.VISIBLE)
        {
            for (int i = 0; i < this.Draw2DList.size(); i++)
            {
                Draw2DList.get(i).draw(gl);
            }
        }

        // OpenGLで描画したフレームバッファからbitmapを生成する
        if (mSwitch.getShutter() == true)
        {
            Log.d(TAG, "callCreateBitmap");
            createOpenGLBitmap(gl);
            mSwitch.switchShutter();
        }
        gl.glDisable(GL10.GL_BLEND);
    }


	/**
	 * ポート番号から，オブジェクト生成用の座標を求める
	 * @param port
	 * @return
	 */
	private short[] calcPort(short port)
	{
		// ポート番号をchar配列に
		char[] portChar = String.valueOf(port).toCharArray();

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

			j++;
			for (int i = 0; i < aboutCenter; i++)
			{
				secondChar[i] = portChar[j];
				j++;
			}

			// firstの方が桁数が多くなるはず
			short first = Short.valueOf(firstChar.toString());
			short second = Short.valueOf(secondChar.toString());

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

			j++;
			for (int i = 0; i < center; i++)
			{
				secondChar[i] = portChar[j];
				j++;
			}

			short first = Short.valueOf(firstChar.toString());
			short second = Short.valueOf(secondChar.toString());

			first = digitReducer(first);
			second = digitReducer(second);

			short[] data = new short[2];
			data[0] = first;
			data[1] = second;
			return data;
		}
	}


	// 3桁以上の値を10で割って2桁に抑える
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
     * 新たな描画オブジェクトを生成する
     * Draw2Dに渡す引数は，xy座標及びwidth, height, 色情報
     * なお，x, y, width, heightに関しては，
     * 描画したい位置や描画図形の大きさをスクリーンのパーセンテージで指定する
     * インスタンス化は次の2通りの方法がある
     *  - int型で0〜100[%]で渡す方法．
     *   ~ new Draw2D(0, 0, 100, 100, color);
     *  - float型で0.f〜1.fで渡す方法
     *   ~ new Draw2D(0.f, 0.f, 1.f, 1.f ,color);
     * 2つの例はどちらも，左上から右下までを描画するもの
     * 分かりやすい方法を使ったら良い
     *
     * @see jp.ac.kansai_u.kutc.firefly.packetcam.opengl.Draw2D
     */
    public void newGraphic()
    {
        Log.d(TAG, "newGraphic()");
        Draw2D drawBlue = new Draw2D(0.f, 0.f, .25f, .25f, Enum.COLOR.BLUE);
        Draw2DList.add(drawBlue);
        Draw2D drawGreen = new Draw2D(.25f, .25f, .75f, .75f, Enum.COLOR.GREEN);
        Draw2DList.add(drawGreen);
        Draw2D drawRed = new Draw2D(.75f, .75f, .25f, .25f, Enum.COLOR.RED);
        Draw2DList.add(drawRed);
    }


    /**
     * 指定したオブジェクトを破棄する
     *
     * @param num 何番目のオブジェクトを破棄するか
     */
    public void removeGraphic(int num)
    {
        Log.d(TAG, "removeGraphic()");
        Draw2DList.remove(num);
    }


    /**
     * OpenGLのフレームバッファからBitmapを作る
     * http://stackoverflow.com/questions/3310990/taking-screenshot-of-android-opengl
     *
     * @param gl
     */
    private void createOpenGLBitmap(GL10 gl)
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
}