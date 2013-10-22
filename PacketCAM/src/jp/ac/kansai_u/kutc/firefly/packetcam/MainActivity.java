package jp.ac.kansai_u.kutc.firefly.packetcam;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends Activity
{
	private Camera camera;

	private OverLayView overlay;
	// 画面タッチの2度押し禁止用フラグ
	private boolean mIsTake = false;
	
	// カメラFlashON・OFFフラグ
	private boolean status = false;
	
	//INOUTフラグ
	private boolean inoutstatus = false;
	// 画像サイズ（height，width）
	Size picSize = null;

	// プレビューサイズ
//	Size preSize = null;
	

	// 画像保存フォルダのパス
	private static String FOLDER_PATH = null;

	private static final String TAG = "MainActivity";
   
	/**
	 * アクティビティ起動時に呼び出される
	 */
	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate (savedInstanceState);

		// フルスクリーン化と，タイトルバーの非表示化
		getWindow().addFlags (WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView (R.layout.activity_main);


		SurfaceView surfaceView = (SurfaceView) findViewById (R.id.surfaceView1);
		final SurfaceHolder holder = surfaceView.getHolder ();
		holder.addCallback (surfaceListener);


		// 非推奨だが，3.0以前のAndroidバージョンでは必要らしい
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
		{
			holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		// オーバーレイ
		overlay = new OverLayView(this);
		addContentView(overlay, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		
		Button flashBtn = (Button) findViewById(R.id.button1);
		flashBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0) {
				// TODO 自動生成されたメソッド・スタブ
				if(status==false)
				{
					Camera.Parameters parameters = camera.getParameters();
					parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
					camera.setParameters(parameters);
					status = true;
				}
				else
				{
					Camera.Parameters parameters = camera.getParameters();
					parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
					camera.setParameters(parameters);
					status = false;
				}
			}
			
		});
		
		/************************************************************************/
		/*******   取得した最大の画像サイズで撮影した際，アプリが強制終了するバグ発生   *******/
		/************************************************************************/
		// 撮影した画像のサイズが大きすぎて，メモリエラーが発生しているのかもしれない．
		Button resolutionBtn = (Button) findViewById(R.id.button3);
		resolutionBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick (View v)
			{
				final List <Size> supportedPictureSize = camera.getParameters().getSupportedPictureSizes();
				List <Size> supportedPreviewSize = camera.getParameters().getSupportedPreviewSizes();
				
				// リストサイズの取得
				// 5
				int numPicItem = supportedPictureSize.size();
				// 9
//				int numPreItem = supportedPreviewSize.size();
				
				
				String[] picHeight = new String[numPicItem];
				String[] picWidth = new String[numPicItem];
				final String[] pic = new String[numPicItem];
//				
////				String[] preHeight = new String[numPreItem];
////				String[] preWidth = new String[numPreItem];
////				final String[] pre = new String[numPreItem];
				
				
				for (int i = 0; i < supportedPictureSize.size(); i++)
				{
					picSize = supportedPictureSize.get(i);
					picHeight[i] = String.valueOf(picSize.height);
					picWidth[i] = String.valueOf(picSize.width);
					
//					preSize = supportedPreviewSize.get(i);
//					preHeight[i] = String.valueOf(preSize.height);
//					preWidth[i] = String.valueOf(preSize.width);
					
					pic[i] = "Height: " + picHeight[i] + "Width: " + picWidth[i];
					
//					pic[i] = "PicHeight: " + picHeight[i] + "PicWidth: " + picWidth[i] + "\n"
//							 + "PreHeight: " + preHeight[i] + "PreWidth: " + preWidth[i];
				}
				
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle("画像サイズを選択してください");
				builder.setItems(pic, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						Camera.Parameters parameter = camera.getParameters();
						
						picSize = supportedPictureSize.get(which);
						parameter.setPictureSize(picSize.width, picSize.height);
						
//						parameter.setPreviewSize(preSize.width, preSize.height);
						
						camera.setParameters(parameter);
					}
				});
				builder.show();
				
			}
		});
		
	  Button INOUTBtn = (Button) findViewById(R.id.button3);
	  INOUTBtn.setOnClickListener(new OnClickListener()
		{
		 public void onClick(View v)
		 {
        // カメラが複数あるかチェック
		  int numberOfCameras = camera.getNumberOfCameras();
		  Toast.makeText(MainActivity.this, "NumberOfCameras :" + numberOfCameras, Toast.LENGTH_SHORT).show();
        if (numberOfCameras == 1) {
          Toast.makeText(MainActivity.this, "Cameraが一つです", Toast.LENGTH_SHORT).show();
          return;
          		
        }
 
        // 現在利用しているカメラを解放
        if (camera != null) {
            camera.release();
            }
 
        // カメラを切り替え
        if(inoutstatus==false){
        camera = Camera.open(1);
        inoutstatus=true;
        }
        else
        {
        	camera=Camera.open(0);
        	inoutstatus=false;
        	
        }
        try
        {
        	camera.setPreviewDisplay(holder);
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
 
        // プレビュー再開
        camera.startPreview();    
		 }
		});
			
		
		surfaceView.setOnTouchListener (new OnTouchListener ()
		{
			@Override
			public boolean onTouch (View v, MotionEvent event)
			{
				if (event.getAction () == MotionEvent.ACTION_DOWN)
				{
					if (camera != null)
					{
						if (!mIsTake)
						{

							Toast.makeText (MainActivity.this, "撮影", Toast.LENGTH_SHORT).show ();
							mIsTake = true;
							// オートフォーカス
							camera.autoFocus(mAutoFocusListener);
						}
					}
				}
				return true;
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
			
			Size size = getOptimalPreviewSize (parameters);

//			List <Size> previewSizes = camera.getParameters ().getSupportedPreviewSizes ();
//			Size size = previewSizes.get (0);

			parameters.setPreviewSize (size.width, size.height);

			camera.setParameters (parameters);
			
			holder.setFixedSize(size.width, size.height);

			// （端末によっては以下のコードを代わりに実行する必要があるかも）
//			List <Size> supportedSizes = Reflect.getSuportedPreviewSizes(parameters);
//
//			if (supportedSizes != null && supportedSizes.size() > 0)
//			{
//				Log.d(TAG, "supportedSizeIsNotNull");
//				Size size = supportedSizes.get(0);
//
//				Log.d(TAG, "sizeHeight = " + size.height + "sizeWidth = " + size.width);
//
//
//				parameters.setPreviewSize(size.width, size.height);
//				camera.setParameters(parameters);
//			}
			// ここまで

			camera.startPreview ();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				String a = e.getMessage();
				Log.d(TAG, a);
			}
		}
	};
	
	
	/**
	 * 画面サイズに応じて最適なカメラプレビューのサイズを返すメソッド
	 * 参考URL：http://www.seeda.jp/modules/d3blog/details.php?bid=29&cid=7
	 * @param params CameraParameter
	 * @return カメラプレビューサイズ
	 */
	private Size getOptimalPreviewSize (Parameters params)
	{
		Size optimalSize = null;
		List <Size> sizes = params.getSupportedPreviewSizes();
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


	/**
	 * オートフォーカス完了のコールバック
	 */
	private Camera.AutoFocusCallback mAutoFocusListener = new Camera.AutoFocusCallback()
	{

		@Override
		public void onAutoFocus(boolean success, Camera camera)
		{
			camera.takePicture(null, null, pictureListener);
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
					camera.autoFocus(mAutoFocusListener);
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

			// フォルダの作成を行う
			if (!createFolder())
			{
				Toast.makeText(MainActivity.this, "failure", Toast.LENGTH_SHORT).show();
			}
			else
			{
				Toast.makeText (MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
			}


			try
			{
				// createBitmapより，画像データを生成

				// オーバーレイ表示された画像との合成処理を行う
				// カメラのイメージ
				Bitmap cameraBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, null);

				// オーバーレイイメージ viewから画像を取得
				Bitmap overlayBitmap = overlay.getDrawingCache();

				// 空のイメージを作成
				Bitmap offBitmap = Bitmap.createBitmap (cameraBitmap.getWidth(), cameraBitmap.getHeight(), Bitmap.Config.ARGB_8888);

				Canvas offScreen = new Canvas (offBitmap);

				// 画像の合成処理
				offScreen.drawBitmap (cameraBitmap, null, new Rect (0, 0, cameraBitmap.getWidth(), cameraBitmap.getHeight()), null);
				offScreen.drawBitmap(overlayBitmap, null, new Rect(0, 0, cameraBitmap.getWidth(), cameraBitmap.getHeight()), null);

				// 合成した画像：offBitmap

				// ファイル名を設定
				Calendar cal = Calendar.getInstance ();
				SimpleDateFormat sf = new SimpleDateFormat ("yyyyMMdd_HHmmss");
				String imgPath = FOLDER_PATH + File.separator + sf.format (cal.getTime()) + ".jpg";

				FileOutputStream fos;
				fos = new FileOutputStream (imgPath, true);
//				fos.write (data);

				offBitmap.compress(CompressFormat.JPEG, 100, fos);
				fos.close ();

				// Androidのデータベースへ登録
				// 登録しないとギャラリーなどにすぐに反映されないらしい
				registAndroidDB (imgPath);
			}
			catch (Exception e)
			{
				Toast.makeText (MainActivity.this, e.getMessage (), Toast.LENGTH_SHORT).show();
			}

			camera.startPreview ();

			mIsTake = false;
		}
	};


	/**
	 * 画像保存フォルダの作成
	 * @return 正常に作成できればtrue，できなければfalseを返す
	 */
	private boolean createFolder()
	{
		String status = Environment.getExternalStorageState ();

		if (!isSdCardMounted(status))
		{
			Toast.makeText (this, "SDカードがマウントされていません", Toast.LENGTH_SHORT).show();
			return false;
		}

		// SDカードのフォルダパスの取得
		String SD_PATH = Environment.getExternalStorageDirectory ().getPath ();

		// SDカードにアプリ名でフォルダを新規作成
		FOLDER_PATH = SD_PATH + File.separator + getString(R.string.app_name);

		Toast.makeText (this,  "FolderPath = " + FOLDER_PATH, Toast.LENGTH_SHORT).show ();

		File file = new File (FOLDER_PATH);

		try
		{
			if (!file.exists())
			{
				Toast.makeText (this, "fileNotExists", Toast.LENGTH_SHORT).show();
				file.mkdirs ();
			}
		}
		catch (Exception e)
		{
			Toast.makeText (this,  "mkFileException", Toast.LENGTH_SHORT).show();
			Toast.makeText (this, e.getMessage (), Toast.LENGTH_SHORT).show ();
			e.printStackTrace ();
			return false;
		}
		return true;
	}


	/**
	 * SDカードが端末にマウントされているか確認するメソッド
	 * @param status Environment.getExternalStorageStateメソッドで取得したString型の値
	 * @return マウントされていればtrue, マウントされていなければfalseが返される
	 */
	private boolean isSdCardMounted(String status)
	{
		if (status.equals (Environment.MEDIA_MOUNTED))
		{
			return true;
		}
		return false;
	}


	/**
	 * Androidのギャラリーに画像を登録する
	 * @param path 画像の保存パス
	 */
	private void registAndroidDB (String path)
	{
		ContentValues values = new ContentValues ();
		ContentResolver contentResolver = MainActivity.this.getContentResolver ();
		values.put (Images.Media.MIME_TYPE, "image/jpeg");
		values.put ("_data", path);
		contentResolver.insert (MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

		Toast.makeText (this, "registFinish", Toast.LENGTH_SHORT).show ();
	}

	
	@Override
	public boolean dispatchKeyEvent (KeyEvent event)
	{
		int nowZoom;
		Camera.Parameters parameter;
		List <Size> previewSizes = camera.getParameters ().getSupportedPreviewSizes ();

		switch (event.getAction())
		{
		case KeyEvent.ACTION_DOWN:
			switch (event.getKeyCode())
			{
			case KeyEvent.KEYCODE_VOLUME_UP:
				// ズームイン機能
				parameter = camera.getParameters();
				nowZoom = parameter.getZoom();
				
				if (nowZoom < parameter.getMaxZoom())
				{
					parameter.setZoom(nowZoom + 1);
				}
				camera.setParameters(parameter);
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				// ズームアウト機能
				parameter = camera.getParameters();
				nowZoom = parameter.getZoom();
				
				if (nowZoom > 0)
				{
					parameter.setZoom(nowZoom - 1);
				}
				camera.setParameters(parameter);
				return true;
				
			case KeyEvent.KEYCODE_MENU:
				// メニューボタンで解像度選択
				
				
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
	public boolean onCreateOptionsMenu (Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater ().inflate (R.menu.main, menu);
		return true;
	}

	{
		
	}

	}
