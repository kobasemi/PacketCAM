package jp.ac.kansai_u.kutc.firefly.packetcam;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

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
import android.widget.Toast;

public class MainActivity extends Activity
{
    private Camera camera;
    
    // 画面タッチの2度押し禁止用フラグ
    private boolean mIsTake = false;
    
    private SurfaceHolder.Callback surfaceListener = new SurfaceHolder.Callback()
    {
        // SurfaceViewが生成されたらカメラをオープンする
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
        
        // SurfaceViewが破棄されたらカメラを解放する
        public void surfaceDestroyed (SurfaceHolder holder)
        {
            camera.release ();
            camera = null;
            
        }
        
        // SurfaceViewの大きさやフォーマットが変わったらプレビューの大きさを設定する
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
        {
            Camera.Parameters parameters = camera.getParameters ();
            
            List<Size> previewSizes = camera.getParameters().getSupportedPreviewSizes();
            Size size = previewSizes.get(0);
            
            parameters.setPreviewSize (size.width, size.height);
            camera.setParameters (parameters);
            camera.startPreview ();
        }
    };

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);
        
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback (surfaceListener);
//        holder.setType (SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        surfaceView.setOnTouchListener (new OnTouchListener()
        {
			@Override
			public boolean onTouch (View v, MotionEvent event)
			{
		        if (event.getAction() == MotionEvent.ACTION_DOWN)
		        {
		            if (camera!= null)
		            {
		            	if (!mIsTake)
		            	{
		            		Toast.makeText (MainActivity.this, "撮影", Toast.LENGTH_SHORT).show();
		            		mIsTake = true;
		            		camera.takePicture (shutterListener, null, pictureListener);
		            	}
		            }
		        }
		        return true;
			}
        	
        });
    }
    
    // シャッターが押された時に呼ばれるコールバック
    private Camera.ShutterCallback shutterListener = new Camera.ShutterCallback()
    {
        public void onShutter ()
        {
            
        }
    };
    
    // JPEGイメージ生成後に呼ばれるコールバック
    
    /*********************************************************/
    /*****************画像保存のコードを書く*******************/
    /*********************************************************/
    private Camera.PictureCallback pictureListener = new Camera.PictureCallback()
    {
        public void onPictureTaken (byte[] data, Camera camera)
        {
        	if (data == null)
        	{
        		return;
        	}
        	
        	String SD_PATH = Environment.getExternalStorageDirectory ().getAbsolutePath ();
        	String folderPath = SD_PATH + File.separator + getString(R.string.app_name);
        	File file = new File (folderPath);
        	try
        	{
        		if (!file.exists ())
        		{
        			Log.d("a", "a");
        			file.mkdir ();
        		}
        	}
        	catch (Exception e)
        	{
        		e.printStackTrace ();
        	}
        	
        	// 画像保存パス
        	String saveDir = Environment.getExternalStorageDirectory ().getAbsolutePath () + File.separator + getString(R.string.app_name);
        	Calendar cal = Calendar.getInstance ();
        	SimpleDateFormat sf = new SimpleDateFormat ("yyyyMMdd_HHmmss");
        	String imgPath = saveDir + "/" + sf.format (cal.getTime()) + ".jpg";
        	
        	// ファイル保存
        	FileOutputStream fos;
        	
        	try
        	{
        		fos = new FileOutputStream (imgPath, true);
        		fos.write (data);
        		fos.close();
        		
        		// Androidのデータベースへ登録
        		// （登録しないとギャラリーなどにすぐに反映されないらしい）
        		registAndroidDB(imgPath);
        	}
        	catch (Exception e)
        	{
        		Log.e("Debug", e.getMessage ());
        	}
        	
        	fos = null;
        	
        	camera.startPreview ();
        	
        	mIsTake = false;
        }
    };
    
    
    private void registAndroidDB (String path)
    {
    	ContentValues values = new ContentValues ();
    	ContentResolver contentResolver = MainActivity.this.getContentResolver();
    	values.put (Images.Media.MIME_TYPE, "image/jpeg");
    	values.put ("_data", path);
    	contentResolver.insert (MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
    
//    // 画面タッチのイベントリスナ
//    public boolean onTouthEvent(MotionEvent event)
//    {
//    	Toast.makeText (this, "a", Toast.LENGTH_SHORT).show();
//        if (event.getAction() == MotionEvent.ACTION_DOWN)
//        {
//            if (camera!= null)
//            {
//            	if (!mIsTake)
//            	{
//            		mIsTake = true;
//            		camera.takePicture (shutterListener, null, pictureListener);
//            	}
//            }
//        }
//        return true;
//    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater ().inflate (R.menu.main, menu);
        return true;
    }

}
