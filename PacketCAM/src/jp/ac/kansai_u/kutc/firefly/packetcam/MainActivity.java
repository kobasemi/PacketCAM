package jp.ac.kansai_u.kutc.firefly.packetcam;

import android.app.Activity;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;
import jp.ac.kansai_u.kutc.firefly.packetcam.opengl.DrawCamera;
import jp.ac.kansai_u.kutc.firefly.packetcam.opengl.GLView;
import jp.ac.kansai_u.kutc.firefly.packetcam.readpcap.PcapManager;
import jp.ac.kansai_u.kutc.firefly.packetcam.setting.ReadPcapFileDialog;
import jp.ac.kansai_u.kutc.firefly.packetcam.setting.SettingDialog;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.SharedPreferencesManager;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.CopyAllPcapFileToSd;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.CreateDirectory;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Enum.VISIBILITY;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Enum.ZOOM;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Switch;

import java.util.List;

public class MainActivity extends Activity
	{
		private static final String TAG = MainActivity.class.getSimpleName();

		private GLView mGLView;
		private Switch mSwitch = Switch.getInstance();
		private DrawCamera mDrawCamera;

		/**
		 * アクティビティ起動時に呼び出される
		 */
		@Override
		protected void onCreate(Bundle savedInstanceState)
			{
				super.onCreate(savedInstanceState);

				// フルスクリーン化と，タイトルバーの非表示化
				getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
				requestWindowFeature(Window.FEATURE_NO_TITLE);

				setContentView(R.layout.activity_main);

                mGLView = (GLView)findViewById(R.id.glview);
				mDrawCamera = new DrawCamera();


				// 設定マネージャにアクティビティをセット + 初期処理
				SharedPreferencesManager.getInstance().init(MainActivity.this);

				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
					// SDカードがマウントされているならば，ディレクトリを作成する
					if (CreateDirectory.createDirectory())
						Log.d(TAG, "Create Directory Success");
					else
						Log.d(TAG, "Create Directory Filed...");
				else
					Toast.makeText(this, "SDカードがマウントされていません", Toast.LENGTH_SHORT).show();

				// assets/cap ディレクトリ以下の構造をそのままSDカードにコピーする
				new CopyAllPcapFileToSd(getApplicationContext());


				// シャッターボタン
				ImageButton shutterBtn = (ImageButton) findViewById(R.id.shutter);
				shutterBtn.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
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
				ImageButton settingBtn = (ImageButton) findViewById(R.id.setting);
                settingBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new SettingDialog().show(MainActivity.this);
                    }
                });

				// エフェクトボタン
				final ImageButton effectBtn = (ImageButton) findViewById(R.id.effect);
				effectBtn.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
						{
							if (!PcapManager.getInstance().isPcapFileOpened())
								{
									// PcapFileがオープンされていない場合
									// PcapFile読み込みダイアログを表示する
									new ReadPcapFileDialog().show(MainActivity.this);
									return;
								}
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
                            SharedPreferencesManager.getInstance().setEffectStatus(mSwitch.getVisibility());
						}
				});
			}

		/**
		 * 画面サイズに応じて最適なカメラプレビューのサイズを返すメソッド 参考URL：http://www.seeda.jp/modules/d3blog/details.php?bid=29&cid=7
		 *
		 * @param params CameraParameter
		 * @return カメラプレビューサイズ
		 */
		private Size getOptimalPreviewSize(Parameters params)
			{
				Size optimalSize = null;
				List<Size> sizes = params.getSupportedPreviewSizes();
				float horizontalViewAngle = params.getHorizontalViewAngle();
				float verticalViewAngle = params.getVerticalViewAngle();
				double targetRatio = (double) horizontalViewAngle / verticalViewAngle;
				double minDiff = Double.MAX_VALUE;

				for (Size size : sizes)
					{
						double ratio = (double) size.width / size.height;
						double tempDiff = Math.abs(targetRatio - ratio);
						// 比率の差が少ない，より小さいプレビューサイズを選ぶ
						if (tempDiff <= minDiff)
							{
								minDiff = tempDiff;
								optimalSize = size;
							}
					}

				return optimalSize;
			}

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
		public boolean dispatchKeyEvent(KeyEvent event)
			{
				switch (event.getAction())
					{
						case KeyEvent.ACTION_DOWN:
							switch (event.getKeyCode())
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
							switch (event.getKeyCode())
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
				return super.dispatchKeyEvent(event);
			}

		@Override
		public boolean onCreateOptionsMenu(Menu menu)
			{
				// Inflate the menu; this adds items to the action bar if it is present.
				getMenuInflater().inflate(R.menu.main, menu);
				return true;
			}

		@Override
		protected void onPause()
			{
				super.onPause();
				synchronized (mDrawCamera)
					{
						// TODO これがうまいこと機能してないかも
						mDrawCamera.calledWhenOnPause();
					}
			}
	}
