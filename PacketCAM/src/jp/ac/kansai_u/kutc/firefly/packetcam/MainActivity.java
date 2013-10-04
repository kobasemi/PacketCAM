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
import android.opengl.Matrix;
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
        	try
        	{
        		Bitmap tmp_bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        		
        		
        		Bitmap bitmap = Bitmap.createBitmap (tmp_bitmap);
        		
        		String name = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", java.util.Locale.JAPAN).format (new Date()) + ".jpg";
        		MediaStore.Images.Media.insertImage (getContentResolver(), bitmap, name, null);
        		// ファイル保存
//        		FileOutputStream fos;
        	
//        		fos = new FileOutputStream (folderPath, false);
//        		fos = new FileOutputStream ("/sdcard/camera_test.jpg");
//        		fos.write (data);
//        		fos.flush ();
//        		fos.close();
//        		
        		// Androidのデータベースへ登録
        		// （登録しないとギャラリーなどにすぐに反映されないらしい）
//        		registAndroidDB("/sdcard/camera_test.jpg");
        	}
        	catch (Exception e)
        	{
        		Log.e("Debug", e.getMessage ());
        	}
        	
        	
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
    	
    	Toast.makeText (this, "registFinish", Toast.LENGTH_SHORT).show();
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
