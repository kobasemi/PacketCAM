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
    ArrayList<DrawBlendingRectangle> drawBlendingRectangleList = new ArrayList<DrawBlendingRectangle>();

    int mWidth = 0, mHeight = 0;

    private Switch mSwitch = Switch.getInstance();

    private DrawCamera mDrawCamera;

    PcapPacket packet = null;

	int colorFlg = 0;
	boolean alphaFlg = false;

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
        // キューの先頭パケットを取得．到着していない場合でもぬるぽを出さない
		// パケットは1秒ごとに装填される
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
                short[] dportPoint = DrawBlendingRectangle.calcPort(dport);

                // 描画サイズ：TCPヘッダのsport
                short[] sportPoint = DrawBlendingRectangle.calcPort(sport);

				Enum.COLOR color = null;
				if (colorFlg == 0) color = Enum.COLOR.RED;
				if (colorFlg == 1) color = Enum.COLOR.GREEN;
				if (colorFlg == 2) color = Enum.COLOR.BLUE;
				if (colorFlg == 3) color = Enum.COLOR.CYAN;
				if (colorFlg == 4) color = Enum.COLOR.MAGENTA;
				if (colorFlg == 5) color = Enum.COLOR.YELLOW;
				if (colorFlg == 6) color = Enum.COLOR.WHITE;
				if (colorFlg == 7) color = Enum.COLOR.BLACK;

				drawBlendingRectangleList.add(new DrawBlendingRectangle(dportPoint[0], dportPoint[1], sportPoint[0], sportPoint[1], color));

				colorFlg++;
				if (colorFlg == 8) colorFlg = 0;
			}
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
		// src, dst
        gl.glBlendFunc(GL10.GL_ONE_MINUS_DST_COLOR, GL10.GL_ONE_MINUS_SRC_ALPHA);

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
        gl.glDisable(GL10.GL_BLEND);
    }


    /**
     * 新たな描画オブジェクトを生成する
     * Draw2Dに渡す引数は，xy座標及びwidth, height, 色情報
     * なお，x, y, width, heightに関しては，
     * 描画したい位置や描画図形の大きさをスクリーンのパーセンテージで指定する
     * インスタンス化は次の2通りの方法がある
     *  - int型で0〜100[%]で渡す方法．
     *   ~ new DrawBlendingRectangle(0, 0, 100, 100, color);
     *  - float型で0.f〜1.fで渡す方法
     *   ~ new DrawBlendingRectangle(0.f, 0.f, 1.f, 1.f ,color);
     * 2つの例はどちらも，左上から右下までを描画するもの
     * 分かりやすい方法を使ったら良い
     *
     * @see DrawBlendingRectangle
     */
    public void newGraphic()
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
    public void removeGraphic(int num)
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