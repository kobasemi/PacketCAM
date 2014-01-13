package jp.ac.kansai_u.kutc.firefly.packetcam.opengl;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.opengl.GLSurfaceView;
import android.util.Log;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Enum;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Switch;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * OpenGLの描画を行う
 */
public class EffectRenderer implements GLSurfaceView.Renderer
{
    private static final String TAG = EffectRenderer.class.getSimpleName();

    // オブジェクトを格納するリスト
    ArrayList<Draw2D> Draw2DList = new ArrayList<Draw2D>();

    int mWidth = 0, mHeight = 0;

    private Switch mSwitch = Switch.getInstance();

    private DrawCamera mDrawCamera;


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
        Draw2DList.get(0).setSize(width, height);
    }


    /**
     * 描画処理のループ
     *
     * @param gl
     */
    public void onDrawFrame(GL10 gl)
    {
        Log.i(TAG, "onDrawFrame");

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
     * 新たなオブジェクトを生成する
     */
    // TODO ここに座標などの情報を渡して新規オブジェクトの作成をオーダーする
    public void newGraphic()
    {
        Log.d(TAG, "newGraphic()");
        Draw2D drawBlue = new Draw2D(100, 100, 200, 200, Enum.COLOR.BLUE);
        Draw2DList.add(drawBlue);
        Draw2D drawGreen = new Draw2D(200, 200, 300, 300, Enum.COLOR.GREEN);
        Draw2DList.add(drawGreen);
        Draw2D drawRed = new Draw2D(500, 150, 100, 100, Enum.COLOR.RED);
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