package jp.ac.kansai_u.kutc.firefly.packetcam.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by kousaka on 2013/12/10.
 * https://sites.google.com/a/gclue.jp/android-docs-2009/openglno-kiso
 */
public class GLView extends GLSurfaceView implements GLSurfaceView.Renderer
{
	private static final String TAG = "GLVIEW";

	// Draw2D
	private Draw2D mDraw2D;
	float i = 0;

	public GLView (Context context)
	{
		super(context);

		// 描画処理を設定（これをすることで，onDrawFrame()が定期的に呼ばれる
		setRenderer(this);
	}


	/**
	 * 描画処理のループ
	 * @param gl
	 */
	public void onDrawFrame (GL10 gl)
	{
		Log.i(TAG, "onDrawFrame()");

		// 背景色を設定
		// GL10.glClearColor (Red, Green, Blue, Alpha）
//		gl.glClearColor(0, 0, 1, 1.0f);

		// 背景色を描画
		// 背景の初期化を行う．初期化された際に，上で設定された値が反映される．
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		// モデルビューモードに設定
		gl.glMatrixMode(GL10.GL_MODELVIEW);

		// モデル座標の初期化
		gl.glLoadIdentity();

		// 図形の移動
		gl.glTranslatef(i, i, 0);

		mDraw2D.draw(gl);

		i = i + 1;
	}


	/**
	 * 画面が変更された時に呼び出されるメソッド
	 * １：画面が生成された時（onSurfaceCreatedの後）
	 * ２：画面サイズが変わった時（縦と横で端末が切り替わった時）
	 * @param gl
	 * @param width
	 * @param height
	 */
	public void onSurfaceChanged (GL10 gl, int width, int height)
	{
		Log.i(TAG, "onSurfaceChanged()");

		// ビューモードの設定
		// GL上で扱うスクリーンをどこにどれくらいの大きさで表示するのかを設定する
		// 左上が0,0
		// 以下のコードで全画面になる
		gl.glViewport(0, 0, width, height);

		// プロジェクションモードに設定
		gl.glMatrixMode(GL10.GL_PROJECTION);

		// スクリーン座標を初期化
		gl.glLoadIdentity();

		// 2Dの投影
		GLU.gluOrtho2D(gl, 0.0f, width, 0.0f, height);

		// 頂点の配列の利用を有効にする
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

		// 色の配列の利用を有効にする
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
	}


	/**
	 * GLSurfaceViewのRendererが生成された際に呼ばれる
	 * @param arg0
	 * @param arg1
	 */
	public void onSurfaceCreated (GL10 arg0, EGLConfig arg1)
	{
		Log.i(TAG, "onSurfaceCreated()");

		// Draw2Dのインスタンスを生成
		mDraw2D = new Draw2D();
	}


	/**
	 * GLSurfaceViewのRendererが破棄された際に呼ばれる
	 */
	protected void onDetachedFromWindow()
	{
		super.onDetachedFromWindow();
		Log.i(TAG, "onDetachedFromWindow()");
	}
}
