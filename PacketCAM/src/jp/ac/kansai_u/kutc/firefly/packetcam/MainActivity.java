package jp.ac.kansai_u.kutc.firefly.packetcam;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;


public class MainActivity extends Activity
{
	private Camera camera;

	// 画面タッチの2度押し禁止用フラグ
	private boolean mIsTake = false;
	
	private static String FOLDER_PATH = null;
	
	/**
	 * アクティビティ起動時に呼び出される
	 */
	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate (savedInstanceState);
		
		getWindow().addFlags (WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView (R.layout.activity_main);
		
		
		SurfaceView surfaceView = (SurfaceView) findViewById (R.id.surfaceView1);
		SurfaceHolder holder = surfaceView.getHolder ();
		holder.addCallback (surfaceListener);

		
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
							camera.takePicture (shutterListener, null, pictureListener);
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
			Camera.Parameters parameters = camera.getParameters ();

			List <Size> previewSizes = camera.getParameters ().getSupportedPreviewSizes ();
			Size size = previewSizes.get (0);

			parameters.setPreviewSize (size.width, size.height);
			camera.setParameters (parameters);
			camera.startPreview ();
		}
	};
	
	
	/**
	 * シャッターが押された時に呼ばれるコールバック
	 */
	private Camera.ShutterCallback shutterListener = new Camera.ShutterCallback ()
	{
		public void onShutter ()
		{

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
				// ファイル名を設定
				Calendar cal = Calendar.getInstance ();
				SimpleDateFormat sf = new SimpleDateFormat ("yyyyMMdd_HHmmss");
				String imgPath = FOLDER_PATH + File.separator + sf.format (cal.getTime()) + ".jpg";
				
				FileOutputStream fos;
				fos = new FileOutputStream (imgPath, true);
				fos.write (data);
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



	private void registAndroidDB (String path)
	{
		ContentValues values = new ContentValues ();
		ContentResolver contentResolver = MainActivity.this.getContentResolver ();
		values.put (Images.Media.MIME_TYPE, "image/jpeg");
		values.put ("_data", path);
		contentResolver.insert (MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

		Toast.makeText (this, "registFinish", Toast.LENGTH_SHORT).show ();
	}


	// // 画面タッチのイベントリスナ
	// public boolean onTouthEvent(MotionEvent event)
	// {
	// Toast.makeText (this, "a", Toast.LENGTH_SHORT).show();
	// if (event.getAction() == MotionEvent.ACTION_DOWN)
	// {
	// if (camera!= null)
	// {
	// if (!mIsTake)
	// {
	// mIsTake = true;
	// camera.takePicture (shutterListener, null, pictureListener);
	// }
	// }
	// }
	// return true;
	// }

	@Override
	public boolean onCreateOptionsMenu (Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater ().inflate (R.menu.main, menu);
		return true;
	}

}
