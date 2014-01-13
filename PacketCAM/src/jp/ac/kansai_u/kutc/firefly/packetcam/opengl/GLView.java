package jp.ac.kansai_u.kutc.firefly.packetcam.opengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.opengl.*;
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

		public Thread drawThread;

		/**
		 * エフェクトボタンを押した時に呼び出されるやつ
		 *
		 * @param context
		 */
		public GLView(Context context)
			{
				super(context);
				Init();
			}

		/**
		 * 起動してレイアウトを設定する際に呼び出されるやつ
		 *
		 * @param context
		 * @param attrs
		 */
		public GLView(Context context, AttributeSet attrs)
			{
				super(context, attrs);
				Init();
			}


		private void Init()
			{
				mRenderer = new ClearRenderer();

				this.getHolder().setFormat(PixelFormat.RGBA_8888);
				this.setEGLConfigChooser(8, 8, 8, 0, 0, 0);

				this.setRenderer(mRenderer);
				this.setRenderMode(RENDERMODE_WHEN_DIRTY);

				if (mSwitch.getStatus() == STATUS.STOP) mSwitch.switchStatus();

				drawThread = new Thread(new Runnable()
				{
					@Override
					public void run()
						{
							while (mSwitch.getStatus() == STATUS.RUNNING)
								{
									if (mSwitch.getDrawstate() == DRAWSTATE.READY)
										{
											requestRender();
										}
								}
						}
				});

				drawThread.start();
			}


		/**
		 * エフェクトの表示・非表示を切り替える
		 */
		public void setTransparent()
			{
				if (mSwitch.getVisibility() == VISIBILITY.VISIBLE)
					{
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


		public void calledWhenExit()
			{
				if (mSwitch.getDrawstate() == DRAWSTATE.READY) mSwitch.switchDrawState();
				if (mSwitch.getStatus() == STATUS.RUNNING) mSwitch.switchStatus();
			}
	}