package jp.ac.kansai_u.kutc.firefly.packetcam;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;

/**
 * カメラプレビューにオーバーレイするレイアウトクラス
 */
public class OverLayView extends View 
{
	private Bitmap bitmap;
	int width;
	int height;
	
	public OverLayView (Context context)
	{
		super(context);
		
		// getDrawingCacheで画面を画像で取得できるようにする
		setDrawingCacheEnabled(true);
		
		bitmap = BitmapFactory.decodeResource (context.getResources(), R.drawable.droidicon);
		setFocusable (true);
	}
	
	
	protected void onSizeChanged (int w, int h, int oldw, int oldh)
	{
		// ビューのサイズを取得
		width = w;
		height = h;
	}
	
	
	@Override
	protected void onDraw (Canvas canvas)
	{
		super.onDraw(canvas);
		canvas.drawColor (Color.TRANSPARENT);
		
		// 位置の調整
		canvas.drawBitmap (bitmap, width-400, 0, null);
	}
}
