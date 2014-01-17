package jp.ac.kansai_u.kutc.firefly.packetcam;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
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
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.*;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Enum.VISIBILITY;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Enum.ZOOM;


/**
 * 起動時に呼ばれる，アクティビティクラス
 * @auther Kousaka akasaka Funada Hibino
 */
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
					// SDカードがマウントされているならば，画像保存用及びパケット保存用ディレクトリを作成する
					if (CreateDirectory.createDirectory("Pictures", Path.APPROOT_PATH) &&
                            CreateDirectory.createDirectory("Packet", Path.APPROOT_PATH))
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
                if(mSwitch.getVisibility() == VISIBILITY.VISIBLE)
                    effectBtn.setImageResource(R.drawable.effect_on);
				effectBtn.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
						{
							if (!PcapManager.getInstance().isReady())
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
                                    PcapManager.getInstance().start();  // Thread Start
								}
							else if (mSwitch.getVisibility() == VISIBILITY.VISIBLE)
								{
									effectBtn.setImageResource(R.drawable.effect_off);

									// エフェクトを非表示
									mGLView.setTransparent();
                                    PcapManager.getInstance().stop();  // Thread Stop
								}
                            SharedPreferencesManager.getInstance().setEffectStatus(mSwitch.getVisibility());
						}
				});

                final ImageButton flashBtn = (ImageButton)findViewById(R.id.flash);
                flashBtn.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v){
                        Camera.Parameters parameters = DrawCamera.getCamera().getParameters ();
                        if(parameters.getFlashMode().equals(Parameters.FLASH_MODE_TORCH)){
                            // フラッシュがオンの場合
                            flashBtn.setImageResource(R.drawable.flash_off);
                            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                            // 設定ファイルに保存
                            SharedPreferencesManager.getInstance().setFlashStatus(Camera.Parameters.FLASH_MODE_OFF);
                        }else{
                            flashBtn.setImageResource(R.drawable.flash_on);
                            if(parameters.getSupportedFlashModes().contains(Parameters.FLASH_MODE_TORCH))
                                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                            else{
                                Toast.makeText(MainActivity.this, "CAMERAがTORCHに対応していません", Toast.LENGTH_SHORT).show();
                                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                            }
                            // 設定ファイルに保存
                            SharedPreferencesManager.getInstance().setFlashStatus(Camera.Parameters.FLASH_MODE_TORCH);
                        }
                        DrawCamera.getCamera().setParameters(parameters);
                    }
                });
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


		/**
		 * キーイベントを取得し，イベントに応じた処理を行う
		 * @param event 取得したキーイベント
		 * @return
		 */
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
										return true;
									case KeyEvent.KEYCODE_VOLUME_DOWN:
										// ズームダウン機能
										mDrawCamera.zoom(ZOOM.ZOOMDOWN);
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
        protected void onResume() {
            super.onResume();
            mGLView.onResume();
            if(mSwitch.getVisibility() == VISIBILITY.VISIBLE)
                // エフェクトオン状態で復帰した場合
                PcapManager.getInstance().start();
        }

        @Override
        protected void onPause() {
            super.onPause();
            mGLView.onPause();
            DrawCamera.cameraRelease();
            PcapManager.getInstance().stop();
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            PcapManager.getInstance().shutdown();
        }
    }
