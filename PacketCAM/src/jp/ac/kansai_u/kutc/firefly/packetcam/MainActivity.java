package jp.ac.kansai_u.kutc.firefly.packetcam;

import java.util.List;

import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends Activity
{
    private Camera camera;
    
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
    }
    
    // シャッターが押された時に呼ばれるコールバック
    private Camera.ShutterCallback shutterListener = new Camera.ShutterCallback()
    {
        public void onShutter ()
        {
            
        }
    };
    
    // JPEGイメージ生成後に呼ばれるコールバック
    private Camera.PictureCallback pictureListener = new Camera.PictureCallback()
    {
        public void onPictureTaken (byte[] data, Camera camera)
        {
            camera.startPreview ();
        }
    };
    
    // 画面タッチのイベントリスナ
    public boolean onTouthEvent(MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            if (camera!= null)
            {
                camera.takePicture (shutterListener, null, pictureListener);
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater ().inflate (R.menu.main, menu);
        return true;
    }

}
