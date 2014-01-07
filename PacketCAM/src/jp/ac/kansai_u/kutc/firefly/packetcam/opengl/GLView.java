package jp.ac.kansai_u.kutc.firefly.packetcam.opengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Enum.*;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Switch;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by Kousaka on 2013/12/10.
 * https://sites.google.com/a/gclue.jp/android-docs-2009/openglno-kiso
 */
public class GLView extends GLSurfaceView
{
	private static final String TAG = GLView.class.getSimpleName();

	private ClearRenderer mRenderer;

	private Switch mSwitch = Switch.getInstance();

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
		this.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
		this.setRenderer(mRenderer);
		this.setRenderMode(RENDERMODE_WHEN_DIRTY);

		if (mSwitch.getStatus() == STATUS.STOP) mSwitch.switchStatus();

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (mSwitch.getStatus() == STATUS.RUNNING)
				{
					if (mSwitch.getDrawstate() == DRAWSTATE.READY)
					{
						requestRender();
					}
				}
			}
		}).start();
	}


	/**
	 * エフェクトの表示・非表示を切り替える
	 */
	public void setTransparent()
	{
		if (mSwitch.getVisibility() == VISIBILITY.VISIBLE)
		{
			this.setVisibility(View.GONE);
			mSwitch.switchVisibility();
		}
		else if (mSwitch.getVisibility() == VISIBILITY.INVISIBLE)
		{
			this.setVisibility(View.VISIBLE);
			mSwitch.switchVisibility();
		}
	}


	/**
	 * シャッターボタンが押された際に呼び出す
	 * エフェクト画面のフレームバッファをBitmap形式にしてMainActivityに送るフラグをONにする
	 */
	public void setShutter()
	{
		Log.d(TAG, "setShutter()");
		if (!mSwitch.getShutter())
		{
			mSwitch.switchShutter();
		}
	}

}


/**
 * OpenGLの描画を行う
 */
class ClearRenderer implements GLSurfaceView.Renderer
{
	private static final String TAG = ClearRenderer.class.getSimpleName();

	// オブジェクトを格納するリスト
	ArrayList<Draw2D> Draw2DList = new ArrayList<Draw2D>();

	int mWidth = 0, mHeight = 0;

	private Switch mSwitch = Switch.getInstance();

	private DrawCamera mDrawCamera;


	/**
	 * GLSurfaceViewのRendererが生成された際に呼ばれる
	 * @param gl
	 * @param config
	 */
	public void onSurfaceCreated (GL10 gl, EGLConfig config)
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

		mDrawCamera.setUpCamera();
		Draw2DList.get(0).setSize(width, height);
	}


	/**
	 * 描画処理のループ
	 * @param gl
	 */
	public void onDrawFrame (GL10 gl)
	{
		mDrawCamera.draw(gl);

		// カメラプレビュー描画後に，ブレンドを有効化する
		gl.glEnable(GL10.GL_BLEND);

		// ブレンドモードを指定
		gl.glBlendFunc(GL10.GL_ONE_MINUS_DST_COLOR, GL10.GL_ONE_MINUS_SRC_ALPHA);


		// GLViewクラスのvisibility変数をいじることで、描画のON・OFFが可能
		//SwitchクラスのswitchVisibilityメソッドをcallして描画のON・OFFを行う
		if (mSwitch.getVisibility() == VISIBILITY.VISIBLE)
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
	public void newGraphic()
	{
		Log.d(TAG, "newGraphic()");
		Draw2D drawBlue = new Draw2D(100, 100, 200, 200, COLOR.BLUE);
		Draw2DList.add(drawBlue);
		Draw2D drawGreen = new Draw2D(200, 200, 300, 300, COLOR.GREEN);
		Draw2DList.add(drawGreen);
		Draw2D drawRed = new Draw2D(500, 150, 100, 100, COLOR.RED);
		Draw2DList.add(drawRed);
	}


	/**
	 * 指定したオブジェクトを破棄する
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
		ByteBuffer bb = ByteBuffer.allocateDirect(size * 4);

		// OpenGLのフレームバッファからピクセル情報を読み込む
		gl.glReadPixels(0, 0, mWidth, mHeight, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, bb);

		Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
		bitmap.copyPixelsFromBuffer(bb);

		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);

		Bitmap correctBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);

		SaveSDCard.save(correctBitmap);
	}
}