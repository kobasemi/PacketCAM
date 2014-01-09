package jp.ac.kansai_u.kutc.firefly.packetcam;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;
import jp.ac.kansai_u.kutc.firefly.packetcam.opengl.DrawCamera;
import jp.ac.kansai_u.kutc.firefly.packetcam.opengl.GLView;
import jp.ac.kansai_u.kutc.firefly.packetcam.setting.SettingButtonClickListener;
import jp.ac.kansai_u.kutc.firefly.packetcam.setting.SettingsManager;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.*;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Enum.*;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends Activity
{
	private static final String TAG = MainActivity.class.getSimpleName();

	private GLView mGLView;
	private Switch mSwitch = Switch.getInstance();


	// INOUTフラグ
	private boolean inoutstatus = false;

	// アラートの飛び対策
	private boolean alert1_1 = false;

	private DrawCamera mDrawCamera;

	private Context context = this;

	/**
	 * アクティビティ起動時に呼び出される
	 */
	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate (savedInstanceState);

		// フルスクリーン化と，タイトルバーの非表示化
		getWindow ().addFlags (WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature (Window.FEATURE_NO_TITLE);

		setContentView (R.layout.activity_main);

		mGLView = new GLView(this);
		mDrawCamera = new DrawCamera();


        // 設定マネージャにアクティビティをセット + 初期処理
        SettingsManager.getInstance().setActivity(MainActivity.this);
        SettingsManager.getInstance().setSharedPreferences();

        Path.init();

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            // SDカードがマウントされているならば，ディレクトリを作成する
            if(CreateDirectory.createDirectory())
                Log.d(TAG, "Create Directory Success");
            else
                Log.d(TAG, "Create Directory Filed...");
        else
            Toast.makeText(this, "SDカードがマウントされていません", Toast.LENGTH_SHORT).show ();

        // res/rawにあるファイルをSDカードにコピーする
//        new CopyAllRawFieldToSd(getApplicationContext());
        // assets/cap ディレクトリ以下の構造をそのままSDカードにコピーする
        new CopyAllPcapFileToSd(getApplicationContext());


		// TODO ボタンイベントだけ拾っておいて，処理は別クラスで行うようにする（Switchを利用）
		// カメラ切り替えボタン
//		ImageButton INOUTBtn = (ImageButton) findViewById (R.id.inout);
//		INOUTBtn.setOnClickListener (new OnClickListener ()
//		{
//			@TargetApi (Build.VERSION_CODES.GINGERBREAD)
//			public void onClick (View v)
//			{
//				mDrawCamera.inoutChange(context);
//
				// カメラが複数あるかチェック
//				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
//				{
//					return;
//				}
//
//				int numberOfCameras = Camera.getNumberOfCameras ();
//				Toast.makeText (MainActivity.this, "NumberOfCameras :" + numberOfCameras, Toast.LENGTH_SHORT).show ();
//				if (numberOfCameras == 1)
//				{
//					Toast.makeText (MainActivity.this, "Cameraが一つです", Toast.LENGTH_SHORT).show ();
//					return;
//
//				}
//
//				// 現在利用しているカメラを解放
//				if (camera != null)
//				{
//					camera.release ();
//				}
//
//				// カメラを切り替え
//				if (!inoutstatus)
//				{
//					camera = Camera.open (1);
//					inoutstatus = true;
//				}
//				else
//				{
//					camera = Camera.open (0);
//					inoutstatus = false;
//
//				}
//				try
//				{
//					camera.setPreviewDisplay (holder);
//
//					Camera.Parameters param = camera.getParameters();
//					List <Size> previewSizes = camera.getParameters().getSupportedPreviewSizes();
//					Size size = previewSizes.get (0);
//
//					param.setPreviewSize (size.width, size.height);
//					camera.setParameters (param);
//					holder.setFixedSize (size.width, size.height);
//				}
//				catch (Exception e)
//				{
//					e.printStackTrace ();
//				}
//
//				// プレビュー再開
//				camera.startPreview ();
//			}
//		});
//
		// シャッターボタン
		ImageButton shutterBtn = (ImageButton) findViewById(R.id.shutter);
		shutterBtn.setOnClickListener (new OnClickListener()
		{
			@Override
			public void onClick (View v)
			{
//				if (camera != null)
//				{
//					if (!mIsTake)
//					{
//						Toast.makeText (MainActivity.this, "撮影", Toast.LENGTH_SHORT).show();
//						mIsTake = true;
//						camera.autoFocus (mAutoFocusListener);
//
//						// エフェクト画面の合成素材Bitmapを作成し、本クラスのeffectBitmapに格納
//						glView.setShutter();
//					}
//				}
				Toast.makeText(MainActivity.this, "パシャッ", Toast.LENGTH_SHORT).show();
				mGLView.setShutter();
			}
			
		});

		// 設定ボタン
		ImageButton settingBtn = (ImageButton) findViewById (R.id.setting);
        settingBtn.setOnClickListener(new SettingButtonClickListener(MainActivity.this));

        // エフェクトボタン
		final ImageButton effectBtn = (ImageButton) findViewById(R.id.effect);
		effectBtn.setOnClickListener (new OnClickListener()
		{
			@Override
			public void onClick (View v)
			{
				if (mSwitch.getVisibility() == VISIBILITY.INVISIBLE)
				{
					effectBtn.setImageResource(R.drawable.effect_on);

					// エフェクトを表示
					mGLView.setTransparent();
				}
				else if (mSwitch.getVisibility() == VISIBILITY.VISIBLE)
				{
					effectBtn.setImageResource(R.drawable.effect_off);

					// エフェクトを非表示
					mGLView.setTransparent();
				}
			}
		});
	}

//    public static Camera getCamera(){ return camera; }

//	private SurfaceHolder.Callback surfaceListener = new SurfaceHolder.Callback ()
//	{
//		/**
//		 * SurfaceViewが生成されたらカメラをオープンする
//		 */
//		public void surfaceCreated (SurfaceHolder holder)
//		{
//			camera = Camera.open ();
//			try
//			{
//				camera.setPreviewDisplay (holder);
//			}
//			catch (Exception e)
//			{
//				e.printStackTrace ();
//			}
//		}
//
//
//		/**
//		 * SurfaceViewが破棄されたらカメラを解放する
//		 */
//		public void surfaceDestroyed (SurfaceHolder holder)
//		{
//			camera.release ();
//			camera = null;
//		}
//
//
//		/**
//		 * SurfaceViewの大きさやフォーマットが変わったらプレビューの大きさを設定する
//		 */
//		public void surfaceChanged (SurfaceHolder holder, int format, int width, int height)
//		{
//			/**
//			 * 古い端末ではstartPreviewが正常に開始されない
//			 */
//			try
//			{
//				Camera.Parameters parameters = camera.getParameters ();
//
//				// Size size = getOptimalPreviewSize (parameters);
//
//				List <Size> previewSizes = camera.getParameters ().getSupportedPreviewSizes ();
//				Size size = previewSizes.get (0);
//
//				parameters.setPreviewSize (size.width, size.height);
//
//				camera.setParameters (parameters);
//
//				holder.setFixedSize (size.width, size.height);
//
//				// （端末によっては以下のコードを代わりに実行する必要があるかも）
//				// List <Size> supportedSizes =
//				// Reflect.getSuportedPreviewSizes(parameters);
//				//
//				// if (supportedSizes != null && supportedSizes.size() > 0)
//				// {
//				// Log.d(TAG, "supportedSizeIsNotNull");
//				// Size size = supportedSizes.get(0);
//				//
//				// Log.d(TAG, "sizeHeight = " + size.height + "sizeWidth = " +
//				// size.width);
//				//
//				//
//				// parameters.setPreviewSize(size.width, size.height);
//				// camera.setParameters(parameters);
//				// }
//				// ここまで
//
//				camera.startPreview ();
//			}
//			catch (Exception e)
//			{
//				e.printStackTrace ();
//				String a = e.getMessage ();
//				Log.d (TAG, a);
//			}
//		}
//	};


	/**
	 * 画面サイズに応じて最適なカメラプレビューのサイズを返すメソッド 参考URL：http://www.seeda.jp/modules/d3blog/details.php?bid=29&cid=7
	 * 
	 * @param params
	 *        CameraParameter
	 * @return カメラプレビューサイズ
	 */
	private Size getOptimalPreviewSize (Parameters params)
	{
		Size optimalSize = null;
		List <Size> sizes = params.getSupportedPreviewSizes ();
		float horizontalViewAngle = params.getHorizontalViewAngle ();
		float verticalViewAngle = params.getVerticalViewAngle ();
		double targetRatio = (double) horizontalViewAngle / verticalViewAngle;
		double minDiff = Double.MAX_VALUE;

		for (Size size : sizes)
		{
			double ratio = (double) size.width / size.height;
			double tempDiff = Math.abs (targetRatio - ratio);
			// 比率の差が少ない，より小さいプレビューサイズを選ぶ
			if (tempDiff <= minDiff)
			{
				minDiff = tempDiff;
				optimalSize = size;
			}
		}

		return optimalSize;
	}

//	/**
//	 * オートフォーカス完了のコールバック
//	 */
//	private Camera.AutoFocusCallback mAutoFocusListener = new Camera.AutoFocusCallback ()
//	{
//
//		@Override
//		public void onAutoFocus (boolean success, Camera camera)
//		{
//			camera.takePicture (null, null, pictureListener);
//		}
//	};
//
//	/**
//	 * シャッターが押された時に呼ばれるコールバック（画面タッチで撮影を行うため，コメントアウト）
//	 */
//	private Camera.ShutterCallback shutterListener = new Camera.ShutterCallback ()
//	{
//		public void onShutter ()
//		{
//			if (camera != null)
//			{
//				if (!mIsTake)
//				{
//					Toast.makeText (MainActivity.this, "撮影", Toast.LENGTH_SHORT).show ();
//					mIsTake = true;
//					// オートフォーカス
//					camera.autoFocus (mAutoFocusListener);
//				}
//			}
//
//		}
//	};
//
//	/**
//	 * イメージデータ生成後に呼ばれるコールバック
//	 */
//	private Camera.PictureCallback pictureListener = new Camera.PictureCallback ()
//	{
//		public void onPictureTaken (byte[] data, Camera camera)
//		{
//			if (data == null || effectBitmap == null)
//			{
//				effectBitmap = null;
//				return;
//			}
//
//
//			try
//			{
//				Log.d(TAG, "tryIn");
//				// createBitmapより，画像データを生成
//
//				// オーバーレイ表示された画像との合成処理を行う
//				// カメラのイメージ
//
//				BitmapFactory.Options options = new BitmapFactory.Options ();
//				options.inPurgeable = true;
//
//				Bitmap cameraBitmap = BitmapFactory.decodeByteArray (data, 0, data.length, options);
//
//				// 空のイメージを作成
//				Bitmap offBitmap = Bitmap.createBitmap (cameraBitmap.getWidth (), cameraBitmap.getHeight (), Bitmap.Config.ARGB_8888);
//
//				Canvas offScreen = new Canvas (offBitmap);
//
//				// 画像の合成処理
//				offScreen.drawBitmap (cameraBitmap, null, new Rect (0, 0, cameraBitmap.getWidth (), cameraBitmap.getHeight ()), null);
//				offScreen.drawBitmap (effectBitmap, null, new Rect (0, 0, cameraBitmap.getWidth (), cameraBitmap.getHeight ()), null);
//
//				// 合成した画像：offBitmap
//
//				// ファイル名を設定
//				Calendar cal = Calendar.getInstance ();
//				SimpleDateFormat sf = new SimpleDateFormat ("yyyyMMdd_HHmmss");
//				String imgPath = Path.PICFOLDER_PATH + File.separator + sf.format (cal.getTime ()) + ".jpg";
//
//				FileOutputStream fos;
//				fos = new FileOutputStream (imgPath, true);
//				// fos.write (data);
//
//				Log.d(TAG, "output");
//
//				offBitmap.compress (CompressFormat.JPEG, 100, fos);
//				fos.close ();
//
//				// Androidのデータベースへ登録
//				// 登録しないとギャラリーなどにすぐに反映されないらしい
//				registAndroidDB (imgPath);
//
//				Log.d(TAG, "tryEnd");
//			}
//			catch (Exception e)
//			{
//				Toast.makeText (MainActivity.this, e.getMessage (), Toast.LENGTH_SHORT).show ();
//			}
//			catch (OutOfMemoryError e)
//			{
//				Log.d (TAG, "OutOfMemory");
//				Toast.makeText (MainActivity.this, "このサイズでは撮影できません,", Toast.LENGTH_SHORT).show ();
//			}
//
//			camera.startPreview ();
//
//			mIsTake = false;
//		}
//	};
//
//	/**
//	 * Androidのギャラリーに画像を登録する
//	 *
//	 * @param path
//	 *        画像の保存パス
//	 */
//	private void registAndroidDB (String path)
//	{
//		ContentValues values = new ContentValues ();
//		ContentResolver contentResolver = MainActivity.this.getContentResolver ();
//		values.put (Images.Media.MIME_TYPE, "image/jpeg");
//		values.put ("_data", path);
//		contentResolver.insert (MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//
//	}

	@Override
	public boolean dispatchKeyEvent (KeyEvent event)
	{
//		int nowZoom;
//		Camera.Parameters parameter;

		switch (event.getAction ())
		{
			case KeyEvent.ACTION_DOWN:
				switch (event.getKeyCode ())
				{
					case KeyEvent.KEYCODE_VOLUME_UP:
						mDrawCamera.zoom(ZOOM.ZOOMUP);
//						// ズームイン機能
//						parameter = camera.getParameters ();
//						nowZoom = parameter.getZoom ();
//
//						if (nowZoom < parameter.getMaxZoom ())
//						{
//							parameter.setZoom (nowZoom + 1);
//						}
//						camera.setParameters (parameter);
						return true;
					case KeyEvent.KEYCODE_VOLUME_DOWN:
						// ズームダウン機能
						mDrawCamera.zoom(ZOOM.ZOOMDOWN);
//						parameter = camera.getParameters ();
//						nowZoom = parameter.getZoom ();
//
//						if (nowZoom > 0)
//						{
//							parameter.setZoom (nowZoom - 1);
//						}
//						camera.setParameters (parameter);
						return true;
					default:
						break;
				}
				break;
			case KeyEvent.ACTION_UP:
				switch (event.getKeyCode ())
				{
					case KeyEvent.KEYCODE_VOLUME_UP:
					case KeyEvent.KEYCODE_VOLUME_DOWN:
						// キーが離された場合にはイベントを捨てる
						return true;
					default:
						break;
				}
			default:
				break;
		}
		return super.dispatchKeyEvent (event);
	}

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater ().inflate (R.menu.main, menu);
        return true;
    }

	@Override
	protected void onPause()
	{
		synchronized (mDrawCamera)
		{
			// TODO これがうまいこと機能してないかも
			mDrawCamera.calledWhenOnPause();
			super.onPause();
		}
	}
}
