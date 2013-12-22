package jp.ac.kansai_u.kutc.firefly.packetcam.opengl;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Enum;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.util.ArrayList;

/**
 * Created by Kousaka on 2013/12/10.
 * https://sites.google.com/a/gclue.jp/android-docs-2009/openglno-kiso
 */
public class GLView extends GLSurfaceView
{
	private static final String TAG = "GLVIEW";

	public static volatile Enum.VISIBILITY visibility = Enum.VISIBILITY.INVISIBLE;
	public static volatile boolean shutter = false;

	private ClearRenderer mRenderer;

	/**
	 * エフェクトボタンを押した時に呼び出されるやつ
	 * @param context
	 */
	public GLView (Context context)
	{
		super(context);
		Init();
	}

	/**
	 * 起動してレイアウトを設定する際に呼び出されるやつ
	 * @param context
	 * @param attrs
	 */
	public GLView (Context context, AttributeSet attrs)
	{
		super (context, attrs);
		Init();
	}


	private void Init()
	{
		mRenderer = new ClearRenderer();
		this.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		// 描画処理を設定（これをすることで，onDrawFrame()が定期的に呼ばれる
		this.setRenderer(mRenderer);
		this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
	}


	/**
	 * エフェクトの表示・非表示を切り替える
	 */
	public void setTransparent()
	{
		if (visibility == Enum.VISIBILITY.VISIBLE)
		{
			this.setVisibility(View.GONE);
			visibility = Enum.VISIBILITY.INVISIBLE;
		}
		else if (visibility == Enum.VISIBILITY.INVISIBLE)
		{
			this.setVisibility(View.VISIBLE);
			visibility = Enum.VISIBILITY.VISIBLE;
		}
	}


	/**
	 * シャッターボタンが押された際に呼び出す
	 * エフェクト画面のフレームバッファをBitmap形式にしてMainActivityに送るフラグをONにする
	 */
	public void setShutter()
	{
		Log.d(TAG, "setShutter()");
		if (shutter == false)
		{
			shutter = true;
			Log.d(TAG, "shutterStateChanged = " + shutter);
		}
	}

}


/**
 * OpenGLの描画を行う
 */
class ClearRenderer implements GLSurfaceView.Renderer
{
	private static final String TAG = "Renderer";

	float j = 0;

	ArrayList<Draw2D> Draw2DList = new ArrayList<Draw2D>();

	int mWidth = 0, mHeight = 0;


	/**
	 * GLSurfaceViewのRendererが生成された際に呼ばれる
	 * @param arg0
	 * @param arg1
	 */
	public void onSurfaceCreated (GL10 arg0, EGLConfig arg1)
	{
		Log.i(TAG, "onSurfaceCreated()");

		newGraphic();
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
		mWidth = width; mHeight = height;

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

		for (int i = 0; i < this.Draw2DList.size(); i++)
		{
			// GLViewクラスのvisibility変数をいじることで、描画のON・OFFが可能
			if (GLView.visibility == Enum.VISIBILITY.VISIBLE)
			{
				if (i == 0)
				{
					gl.glTranslatef(j, 0, 0);
				}
				if (i == 1)
				{
					gl.glTranslatef(0, j, 0);
				}

				Draw2DList.get(i).draw(gl);
			}
		}

		// OpenGLで描画したフレームバッファからbitmapを生成する
		if (GLView.shutter == true)
		{
			Log.d(TAG, "callCreateBitmap");
			CreateEffectBitmap.createOpenglBitmap(gl, mWidth, mHeight);
			GLView.shutter = false;
		}
		j++;
	}


	public void newGraphic()
	{
		Draw2D draw2D_1 = new Draw2D(Enum.POSITION.A);
		Draw2DList.add(draw2D_1);

		Draw2D draw2D_2 = new Draw2D(Enum.POSITION.B);
		Draw2DList.add(draw2D_2);
	}
}