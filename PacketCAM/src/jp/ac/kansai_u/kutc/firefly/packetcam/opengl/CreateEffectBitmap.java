package jp.ac.kansai_u.kutc.firefly.packetcam.opengl;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;
import jp.ac.kansai_u.kutc.firefly.packetcam.MainActivity;

import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;

/**
 * Created by Kousaka on 13/12/22.
 * Effect画面のBitmapを作成する
 */
public class CreateEffectBitmap {
	private static final String TAG = "CreateEffectBitmap";

	public static void createOpenglBitmap(GL10 gl, int width, int height){
		Log.d(TAG, "createOpenglBitmap");

		if (width == 0 || height == 0 || MainActivity.effectBitmap != null)
		{
			Log.d(TAG, "Error");
			return;
		}

		int size = width * height;

		ByteBuffer bb = ByteBuffer.allocateDirect(size * 4);

		// OpenGLのフレームバッファからピクセル情報を読み込む
		gl.glReadPixels(0, 0, width, height, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, bb);

		Bitmap tmpBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		tmpBitmap.copyPixelsFromBuffer(bb);

		// 上のコードで作成したBitmapは上下反転しているので、正常な向きにする
		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);

		Bitmap bitmap = Bitmap.createBitmap(tmpBitmap, 0, 0, tmpBitmap.getWidth(), tmpBitmap.getHeight(), matrix, false);

		MainActivity.effectBitmap = bitmap;
	}
}
