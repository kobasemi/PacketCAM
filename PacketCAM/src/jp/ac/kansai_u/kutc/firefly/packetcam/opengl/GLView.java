package jp.ac.kansai_u.kutc.firefly.packetcam.opengl;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Enum.STATUS;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Enum.VISIBILITY;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Switch;

/**
 * Created by Kousaka on 2013/12/10.
 * https://sites.google.com/a/gclue.jp/android-docs-2009/openglno-kiso
 */
public class GLView extends GLSurfaceView
	{
		private static final String TAG = GLView.class.getSimpleName();

		private EffectRenderer mRenderer;

		private Switch mSwitch = Switch.getInstance();

		/**
		 * エフェクトボタンを押した時に呼び出されるやつ
		 *
		 * @param context
		 */
		public GLView(Context context)
			{
				super(context);
				init();
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
				init();
			}


        /**
         * GLSurfaceViewに関する初期化
         * Rendererをセット
         */
		private void init()
			{
				mRenderer = new EffectRenderer();

				this.getHolder().setFormat(PixelFormat.RGBA_8888);
				this.setEGLConfigChooser(8, 8, 8, 0, 0, 0);

				this.setRenderer(mRenderer);

				if (mSwitch.getStatus() == STATUS.STOP) mSwitch.switchStatus();
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

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onResume() {
            super.onResume();
        }
    }