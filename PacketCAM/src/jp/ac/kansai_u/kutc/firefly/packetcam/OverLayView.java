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
		
		// bitmapの透過処理
		
		if (!bitmap.isMutable())
		{
			bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
		}
		
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		
		// Bitmapのピクセルデータを取得
		int[] pixels = new int[width * height];
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
		
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				// 透過率をintとして取得
				int a = pixels [x + y * width];
				
				// 色情報の入っている右から24ビットを破棄するため，ビットを右に24個移動させる
				a = a >>> 24;
				
				// 透過率の変更
				if (a != 0)
				{
					// 0が完全透過，255が
					a -= 100; // 加える透過率の値（0〜255）
					if (a < 0)
					{
						a = 0;
					}
				}
				
				// 左に24個ビットをずらすことで色情報を持たない透過情報のみのintを得る
				a = a << 24;
				
				// ピクセルの色情報から透過率の削除
				int b = pixels [x + y * width];
				
				// 左の8ビットずらした後元に戻すことで透過情報を持たない色情報のみのintを作成
				b = b << 8;
				b = b >>> 8;
				
				// 透過情報と色情報の合成
				pixels[x + y * width] = a ^ b;
			}
		}
		
		//透過度が変わったBitmapデータの作成
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		
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
