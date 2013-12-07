package jp.ac.kansai_u.kutc.firefly.packetcam;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;
import jp.ac.kansai_u.kutc.firefly.packetcam.readpcap.PcapManager;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.CopyAllRawFieldToSd;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.CreateDirectory;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Path;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends Activity
{
	private Camera camera;

	private OverLayView overlay;
	// 画面タッチの2度押し禁止用フラグ
	private boolean mIsTake = false;

	// カメラFlashON・OFFフラグ
	private boolean status = false;

	// INOUTフラグ
	private boolean inoutstatus = false;
	// 画像サイズ（height，width）
	Size picSize = null;

	// プレビューサイズ
	// Size preSize = null;

	private static final String TAG = "MainActivity";

	// エフェクトボタンアイコンの切り替え用
	private boolean switchEffect=false;

	// アラートの飛び対策
	private boolean alert1_1 = false;

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

		// カメラプレビュー用のViewを準備
		SurfaceView surfaceView = (SurfaceView) findViewById (R.id.surfaceView1);
		final SurfaceHolder holder = surfaceView.getHolder ();
		holder.addCallback (surfaceListener);

		// 非推奨だが，3.0以前のAndroidバージョンでは必要らしい
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
		{
			holder.setType (SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		// オーバーレイ画面をFrameLayoutに追加
		overlay = new OverLayView (this);
		FrameLayout frame = (FrameLayout) findViewById (R.id.frameLayout1);
		frame.addView (overlay);

        // 各ディレクトリの作成
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            // SDカードがマウントされているならば
            new CreateDirectory(getResources());
        }else{
            Toast.makeText(this, "SDカードがマウントされていません", Toast.LENGTH_SHORT).show ();
        }

        // res/rawにあるファイルをSDカードにコピーする
        new CopyAllRawFieldToSd(this.getResources(), this.getPackageName());

		// カメラ切り替えボタン
		ImageButton INOUTBtn = (ImageButton) findViewById (R.id.inout);
		INOUTBtn.setOnClickListener (new OnClickListener ()
		{
			@TargetApi (Build.VERSION_CODES.GINGERBREAD)
			public void onClick (View v)
			{
				// カメラが複数あるかチェック
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
				{
					return;
				}

				int numberOfCameras = Camera.getNumberOfCameras ();
				Toast.makeText (MainActivity.this, "NumberOfCameras :" + numberOfCameras, Toast.LENGTH_SHORT).show ();
				if (numberOfCameras == 1)
				{
					Toast.makeText (MainActivity.this, "Cameraが一つです", Toast.LENGTH_SHORT).show ();
					return;

				}

				// 現在利用しているカメラを解放
				if (camera != null)
				{
					camera.release ();
				}

				// カメラを切り替え
				if (!inoutstatus)
				{
					camera = Camera.open (1);
					inoutstatus = true;
				}
				else
				{
					camera = Camera.open (0);
					inoutstatus = false;

				}
				try
				{
					camera.setPreviewDisplay (holder);
					
					Camera.Parameters param = camera.getParameters();
					List <Size> previewSizes = camera.getParameters().getSupportedPreviewSizes();
					Size size = previewSizes.get (0);
					
					param.setPreviewSize (size.width, size.height);
					camera.setParameters (param);
					holder.setFixedSize (size.width, size.height);
				}
				catch (Exception e)
				{
					e.printStackTrace ();
				}

				// プレビュー再開
				camera.startPreview ();
			}
		});
		
		// シャッターボタン
		ImageButton shutterBtn = (ImageButton) findViewById(R.id.shutter);
		shutterBtn.setOnClickListener (new OnClickListener()
		{
			@Override
			public void onClick (View v)
			{
				if (camera != null)
				{
					if (!mIsTake)
					{
						Toast.makeText (MainActivity.this, "撮影", Toast.LENGTH_SHORT).show();
						mIsTake = true;
						camera.autoFocus (mAutoFocusListener);
					}
				}
			}
			
		});

		// 設定ボタン
        //TODO: いつぞクラスに分ける
		ImageButton settingBtn = (ImageButton) findViewById (R.id.setting);
		settingBtn.setOnClickListener (new OnClickListener()
		{
			@Override
			public void onClick (View v)
			{
				CharSequence[] conf = { "パケットの読み込み", "解像度", "フラッシュ" };

				final AlertDialog.Builder builder = new AlertDialog.Builder (MainActivity.this);
				builder.setTitle ("設定");
				builder.setItems (conf, new DialogInterface.OnClickListener ()
				{
					public void onClick (DialogInterface dialog, int which)
					{
						if (which == 0)
						{
							// パケットの読み込み
							CharSequence[] conf1 = { "ファイルの選択", "リアルタイム読み込み" };

							final AlertDialog.Builder builder1 = new AlertDialog.Builder (MainActivity.this);
							builder1.setTitle ("パケットの読み込み");
							builder1.setItems (conf1, new DialogInterface.OnClickListener ()
							{
								public void onClick (DialogInterface dialog, int which)
								{
									if (which == 0)
									{
										// ファイルの選択ダイアログの表示
										// リストにするディレクトリの指定
										File dir = new File (Path.PACKETFOLDER_PATH);

										// 指定されたディレクトリ内のファイル名をすべて取得
										final File[] files = dir.listFiles();
										final String[] str_items;
										str_items = new String[files.length];

										for (int i = 0; i < files.length; i++)
										{
											File file = files[i];
											str_items[i] = file.getName();
										}

										// ファイルリストダイアログの表示
										AlertDialog.Builder builder1_1 = new AlertDialog.Builder(MainActivity.this);
										builder1_1.setTitle("パケットファイルを選択してください");
										builder1_1.setItems(str_items, new DialogInterface.OnClickListener()
										{
											@Override
											public void onClick(DialogInterface dialog, int which)
											{
												// 選択されたパケットファイルのパス
												String filePath = Path.PACKETFOLDER_PATH + File.separator + str_items[which];
												Log.d(TAG, "filePath = " + filePath);

												// PcapManagerのopenPcapFileにファイルパスを渡す
												PcapManager pcap = PcapManager.getInstance();
												pcap.openPcapFile(filePath);
                                            }
										});
										builder1_1.setOnKeyListener(new OnKeyListener()
										{
											@Override
											public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent keyEvent)
											{
												if (keyCode == KeyEvent.KEYCODE_BACK)
												{
													// アラートの飛び対策
													alert1_1 = true;
													dialog.dismiss();
													builder1.show();
													return true;
												}
												return false;
											}
										});
										builder1_1.show();
									}
									if (which == 1)
									{
										// リアルタイム読み込み処理
									}
								}
							});
							// Backキーを押した際に前のダイアログに戻る
							builder1.setOnKeyListener (new OnKeyListener()
							{
								@Override
								public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event)
								{
									// TODO 自動生成されたメソッド・スタブ
									// alert1_1を噛ませているのは，ファイル選択画面からBackキーを押すと，
									// builder1を飛ばして一気にbuilder（最初のアラート）に戻ってしまうため
									if (keyCode == KeyEvent.KEYCODE_BACK && alert1_1 == false)
									{
										dialog.dismiss ();
										builder.show ();
										return true;
									}
									alert1_1 = false;
									return false;
								}

							});
							builder1.show ();
						}
						if (which == 1)
						{
							// 解像度
							// 対応する画像サイズのリストを取得
							final List <Size> supportedPictureSize = camera.getParameters ().getSupportedPictureSizes ();
							// 画像サイズの個数を取得
							int numPicItem = supportedPictureSize.size ();
							
							// さきほど取得した個数をもとに配列を定義
							String[] picHeight = new String[numPicItem];
							String[] picWidth = new String[numPicItem];
							final String[] pic = new String[numPicItem];

							for (int i = 0; i < supportedPictureSize.size (); i++)
							{
								picSize = supportedPictureSize.get (i);
								picHeight[i] = String.valueOf (picSize.height);
								picWidth[i] = String.valueOf (picSize.width);
								
								// 縦サイズと横サイズの最大公約数を求める
								int gcd = GetGCD.getGCD (picSize.width, picSize.height);
								
								String aspWidth = String.valueOf (picSize.width / gcd);
								String aspHeight = String.valueOf (picSize.height / gcd);
								
								pic[i] = picWidth[i] + " : " + picHeight[i] + "（" + aspWidth + " ： " + aspHeight + "）";
							}


							AlertDialog.Builder builder2 = new AlertDialog.Builder (MainActivity.this);
							builder2.setTitle ("解像度");
							builder2.setItems (pic, new DialogInterface.OnClickListener ()
							{
								public void onClick (DialogInterface dialog, int which)
								{
									Camera.Parameters parameter = camera.getParameters ();

									picSize = supportedPictureSize.get (which);
									
									Toast.makeText (MainActivity.this, "画像サイズを" + pic[which] + "に設定しました．", Toast.LENGTH_SHORT).show ();
									
									parameter.setPictureSize (picSize.width, picSize.height);
									camera.setParameters (parameter);
								}
							});
							builder2.setOnKeyListener (new OnKeyListener()
							{

								@Override
								public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event)
								{
									// TODO 自動生成されたメソッド・スタブ
									if (keyCode == KeyEvent.KEYCODE_BACK)
									{
										dialog.dismiss ();
										builder.show();
										return true;
									}
									return false;
								}
							});
							builder2.show();
						}
						if (which == 2)
						{
							// フラッシュ
							CharSequence[] conf3 = { "強制発光", "自動", "OFF" };

							AlertDialog.Builder builder3 = new AlertDialog.Builder (MainActivity.this);
							builder3.setTitle ("フラッシュ");
							builder3.setItems (conf3, new DialogInterface.OnClickListener ()
							{
								public void onClick (DialogInterface dialog, int which)
								{
									if (which == 0)
									{
										// 強制発光処理
										Camera.Parameters parameters = camera.getParameters ();
										parameters.setFlashMode (Camera.Parameters.FLASH_MODE_ON);
										camera.setParameters (parameters);
										Toast.makeText (MainActivity.this, "Flashを強制発光モードにしました", Toast.LENGTH_SHORT).show ();
									}
									if (which == 1)
									{
										// 自動発光処理
										Camera.Parameters parameters = camera.getParameters ();
										parameters.setFlashMode (Camera.Parameters.FLASH_MODE_AUTO);
										camera.setParameters (parameters);
										Toast.makeText (MainActivity.this, "Flashを自動モードにしました", Toast.LENGTH_SHORT).show();
									}
									if (which == 2)
									{
										// フラッシュOFF処理
										Camera.Parameters parameters = camera.getParameters ();
										parameters.setFlashMode (Camera.Parameters.FLASH_MODE_OFF);
										camera.setParameters (parameters);
										Toast.makeText (MainActivity.this, "FlashをOFFにしました", Toast.LENGTH_SHORT).show();
									}
								}
							});
							builder3.setOnKeyListener (new OnKeyListener()
							{

								@Override
								public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event)
								{
									// TODO 自動生成されたメソッド・スタブ
									if (keyCode == KeyEvent.KEYCODE_BACK)
									{
										dialog.dismiss();
										builder.show();
										return true;
									}
									return false;
								}

							});
							builder3.show ();
						}
					}
				});
				builder.show ();

			}
		});

		// エフェクトボタン
		final ImageButton effectBtn = (ImageButton) findViewById(R.id.effect);
		effectBtn.setOnClickListener (new OnClickListener()
		{
			@Override
			public void onClick (View v)
			{
				if (!switchEffect){
					effectBtn.setImageResource(R.drawable.effect_on);
					switchEffect=true;
				}else{
					effectBtn.setImageResource(R.drawable.effect_off);
					switchEffect=false;
					
				}
			}
		});
	}

	private SurfaceHolder.Callback surfaceListener = new SurfaceHolder.Callback ()
	{
		/**
		 * SurfaceViewが生成されたらカメラをオープンする
		 */
		public void surfaceCreated (SurfaceHolder holder)
		{
			camera = Camera.open ();
			try
			{
				camera.setPreviewDisplay (holder);
			}
			catch (Exception e)
			{
				e.printStackTrace ();
			}
		}


		/**
		 * SurfaceViewが破棄されたらカメラを解放する
		 */
		public void surfaceDestroyed (SurfaceHolder holder)
		{
			camera.release ();
			camera = null;
		}


		/**
		 * SurfaceViewの大きさやフォーマットが変わったらプレビューの大きさを設定する
		 */
		public void surfaceChanged (SurfaceHolder holder, int format, int width, int height)
		{
			/**
			 * 古い端末ではstartPreviewが正常に開始されない
			 */
			try
			{
				Camera.Parameters parameters = camera.getParameters ();

				// Size size = getOptimalPreviewSize (parameters);

				List <Size> previewSizes = camera.getParameters ().getSupportedPreviewSizes ();
				Size size = previewSizes.get (0);

				parameters.setPreviewSize (size.width, size.height);

				camera.setParameters (parameters);

				holder.setFixedSize (size.width, size.height);

				// （端末によっては以下のコードを代わりに実行する必要があるかも）
				// List <Size> supportedSizes =
				// Reflect.getSuportedPreviewSizes(parameters);
				//
				// if (supportedSizes != null && supportedSizes.size() > 0)
				// {
				// Log.d(TAG, "supportedSizeIsNotNull");
				// Size size = supportedSizes.get(0);
				//
				// Log.d(TAG, "sizeHeight = " + size.height + "sizeWidth = " +
				// size.width);
				//
				//
				// parameters.setPreviewSize(size.width, size.height);
				// camera.setParameters(parameters);
				// }
				// ここまで

				camera.startPreview ();
			}
			catch (Exception e)
			{
				e.printStackTrace ();
				String a = e.getMessage ();
				Log.d (TAG, a);
			}
		}
	};


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

	/**
	 * オートフォーカス完了のコールバック
	 */
	private Camera.AutoFocusCallback mAutoFocusListener = new Camera.AutoFocusCallback ()
	{

		@Override
		public void onAutoFocus (boolean success, Camera camera)
		{
			camera.takePicture (null, null, pictureListener);
		}
	};

	/**
	 * シャッターが押された時に呼ばれるコールバック（画面タッチで撮影を行うため，コメントアウト）
	 */
	private Camera.ShutterCallback shutterListener = new Camera.ShutterCallback ()
	{
		public void onShutter ()
		{
			if (camera != null)
			{
				if (!mIsTake)
				{
					Toast.makeText (MainActivity.this, "撮影", Toast.LENGTH_SHORT).show ();
					mIsTake = true;
					// オートフォーカス
					camera.autoFocus (mAutoFocusListener);
				}
			}

		}
	};

	/**
	 * イメージデータ生成後に呼ばれるコールバック
	 */
	private Camera.PictureCallback pictureListener = new Camera.PictureCallback ()
	{
		public void onPictureTaken (byte[] data, Camera camera)
		{
			if (data == null)
			{
				return;
			}


			Toast.makeText (MainActivity.this, "totalMemory" + String.valueOf (Runtime.getRuntime ().totalMemory ()), Toast.LENGTH_LONG).show ();
			Toast.makeText (MainActivity.this, "maxMemory" + String.valueOf (Runtime.getRuntime ().maxMemory ()), Toast.LENGTH_LONG).show ();
			Toast.makeText (MainActivity.this, "freeMemory" + String.valueOf (Runtime.getRuntime ().freeMemory ()), Toast.LENGTH_LONG).show ();

			Log.d (TAG, "totalMemory" + String.valueOf (Runtime.getRuntime ().totalMemory ()));
			Log.d (TAG, "maxMemory" + String.valueOf (Runtime.getRuntime ().maxMemory ()));
			Log.d (TAG, "freeMemory" + String.valueOf (Runtime.getRuntime ().freeMemory ()));

			try
			{
				// createBitmapより，画像データを生成

				// オーバーレイ表示された画像との合成処理を行う
				// カメラのイメージ

				BitmapFactory.Options options = new BitmapFactory.Options ();
				options.inPurgeable = true;

				// Bitmap cameraBitmap = BitmapFactory.decodeByteArray(data, 0,
				// data.length, null);
				Bitmap cameraBitmap = BitmapFactory.decodeByteArray (data, 0, data.length, options);

				// オーバーレイイメージ viewから画像を取得
				Bitmap overlayBitmap = overlay.getDrawingCache ();

				// 空のイメージを作成
				Bitmap offBitmap = Bitmap.createBitmap (cameraBitmap.getWidth (), cameraBitmap.getHeight (), Bitmap.Config.ARGB_8888);

				Canvas offScreen = new Canvas (offBitmap);

				// 画像の合成処理
				offScreen.drawBitmap (cameraBitmap, null, new Rect (0, 0, cameraBitmap.getWidth (), cameraBitmap.getHeight ()), null);
				offScreen.drawBitmap (overlayBitmap, null, new Rect (0, 0, cameraBitmap.getWidth (), cameraBitmap.getHeight ()), null);

				// 合成した画像：offBitmap

				// ファイル名を設定
				Calendar cal = Calendar.getInstance ();
				SimpleDateFormat sf = new SimpleDateFormat ("yyyyMMdd_HHmmss");
				String imgPath = Path.PICFOLDER_PATH + File.separator + sf.format (cal.getTime ()) + ".jpg";

				FileOutputStream fos;
				fos = new FileOutputStream (imgPath, true);
				// fos.write (data);

				offBitmap.compress (CompressFormat.JPEG, 100, fos);
				fos.close ();

				// Androidのデータベースへ登録
				// 登録しないとギャラリーなどにすぐに反映されないらしい
				registAndroidDB (imgPath);
			}
			catch (Exception e)
			{
				Toast.makeText (MainActivity.this, e.getMessage (), Toast.LENGTH_SHORT).show ();
			}
			catch (OutOfMemoryError e)
			{
				Log.d (TAG, "OutOfMemory");
				Toast.makeText (MainActivity.this, "このサイズでは撮影できません,", Toast.LENGTH_SHORT).show ();
			}

			camera.startPreview ();

			mIsTake = false;
		}
	};

	/**
	 * Androidのギャラリーに画像を登録する
	 * 
	 * @param path
	 *        画像の保存パス
	 */
	private void registAndroidDB (String path)
	{
		ContentValues values = new ContentValues ();
		ContentResolver contentResolver = MainActivity.this.getContentResolver ();
		values.put (Images.Media.MIME_TYPE, "image/jpeg");
		values.put ("_data", path);
		contentResolver.insert (MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

	}

	@Override
	public boolean dispatchKeyEvent (KeyEvent event)
	{
		int nowZoom;
		Camera.Parameters parameter;
		List <Size> previewSizes = camera.getParameters ().getSupportedPreviewSizes ();

		switch (event.getAction ())
		{
			case KeyEvent.ACTION_DOWN:
				switch (event.getKeyCode ())
				{
					case KeyEvent.KEYCODE_VOLUME_UP:
						// ズームイン機能
						parameter = camera.getParameters ();
						nowZoom = parameter.getZoom ();

						if (nowZoom < parameter.getMaxZoom ())
						{
							parameter.setZoom (nowZoom + 1);
						}
						camera.setParameters (parameter);
						return true;
					case KeyEvent.KEYCODE_VOLUME_DOWN:
						// ズームアウト機能
						parameter = camera.getParameters ();
						nowZoom = parameter.getZoom ();

						if (nowZoom > 0)
						{
							parameter.setZoom (nowZoom - 1);
						}
						camera.setParameters (parameter);
						return true;

					case KeyEvent.KEYCODE_MENU:
                        // TODO: 物理メニューボタンにおけるコンテンツを変える（クレジットなどに）
                        // TODO: いつぞクラスに分ける
						// メニューボタンで解像度選択
						// メニューボタンで解像度選択
						CharSequence[] conf = { "パケットの読み込み", "解像度", "フラッシュ" };

						AlertDialog.Builder builder = new AlertDialog.Builder (MainActivity.this);
						builder.setTitle ("画像サイズを選択してください");
						builder.setItems (conf, new DialogInterface.OnClickListener ()
						{
							public void onClick (DialogInterface dialog, int which)
							{
								if (which == 0)
								{
									// パケットの読み込み
									CharSequence[] conf1 = { "ファイルの選択", "リアルタイム読み込み" };

									AlertDialog.Builder builder1 = new AlertDialog.Builder (MainActivity.this);
									builder1.setTitle ("パケットの読み込み");
									builder1.setItems (conf1, new DialogInterface.OnClickListener ()
									{
										public void onClick (DialogInterface dialog, int which)
										{
											if (which == 0)
											{
												// ファイルの選択ダイアログの表示
											}
											if (which == 1)
											{
												// リアルタイム読み込み処理
											}
										}
									});
									builder1.show ();
								}
								if (which == 1)
								{
									// 解像度
									// 対応する画像サイズのリストを取得
									final List <Size> supportedPictureSize = camera.getParameters ().getSupportedPictureSizes ();
									// 画像サイズの個数を取得
									int numPicItem = supportedPictureSize.size ();
									
									// さきほど取得した個数をもとに配列を定義
									String[] picHeight = new String[numPicItem];
									String[] picWidth = new String[numPicItem];
									final String[] pic = new String[numPicItem];

									for (int i = 0; i < supportedPictureSize.size (); i++)
									{
										picSize = supportedPictureSize.get (i);
										picHeight[i] = String.valueOf (picSize.height);
										picWidth[i] = String.valueOf (picSize.width);
										
										// 縦サイズと横サイズの最大公約数を求める
										int gcd = GetGCD.getGCD (picSize.width, picSize.height);
										
										String aspWidth = String.valueOf (picSize.width / gcd);
										String aspHeight = String.valueOf (picSize.height / gcd);
										
										pic[i] = picWidth[i] + " : " + picHeight[i] + "（" + aspWidth + " ： " + aspHeight + "）";
									}


									AlertDialog.Builder builder2 = new AlertDialog.Builder (MainActivity.this);
									builder2.setTitle ("解像度");
									builder2.setItems (pic, new DialogInterface.OnClickListener ()
									{
										public void onClick (DialogInterface dialog, int which)
										{
											Camera.Parameters parameter = camera.getParameters ();

											picSize = supportedPictureSize.get (which);
											
											Toast.makeText (MainActivity.this, "画像サイズを" + pic[which] + "に設定しました．", Toast.LENGTH_SHORT).show ();
											
											parameter.setPictureSize (picSize.width, picSize.height);
											camera.setParameters (parameter);
										}
									});
									builder2.show ();
								}
								if (which == 2)
								{
									// フラッシュ
									CharSequence[] conf3 = { "強制発光", "自動", "OFF" };

									AlertDialog.Builder builder3 = new AlertDialog.Builder (MainActivity.this);
									builder3.setTitle ("フラッシュ");
									builder3.setItems (conf3, new DialogInterface.OnClickListener ()
									{
										public void onClick (DialogInterface dialog, int which)
										{
											if (which == 0)
											{
												// 強制発光処理
												Camera.Parameters parameters = camera.getParameters ();
												parameters.setFlashMode (Camera.Parameters.FLASH_MODE_ON);
												camera.setParameters (parameters);
												Toast.makeText (MainActivity.this, "Flashを強制発光モードにしました", Toast.LENGTH_SHORT).show ();
											}
											if (which == 1)
											{
												// 自動発光処理
												Camera.Parameters parameters = camera.getParameters ();
												parameters.setFlashMode (Camera.Parameters.FLASH_MODE_AUTO);
												camera.setParameters (parameters);
												Toast.makeText (MainActivity.this, "Flashを自動モードにしました", Toast.LENGTH_SHORT).show();
											}
											if (which == 2)
											{
												// フラッシュOFF処理
												Camera.Parameters parameters = camera.getParameters ();
												parameters.setFlashMode (Camera.Parameters.FLASH_MODE_OFF);
												camera.setParameters (parameters);
												Toast.makeText (MainActivity.this, "FlashをOFFにしました", Toast.LENGTH_SHORT).show();
											}
										}
									});
									builder3.show ();
								}
							}
						});
						builder.show ();
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

}
