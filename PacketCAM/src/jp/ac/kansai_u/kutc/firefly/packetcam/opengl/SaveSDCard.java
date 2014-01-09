package jp.ac.kansai_u.kutc.firefly.packetcam.opengl;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Path;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Kousaka on 14/01/07.
 * 作成した画像をSDカードに保存する
 */
public class SaveSDCard {
	private static final String TAG = "SaveSDCard";


	/**
	 * 作成した画像を受け取って，SDカードに保存する
	 * @param bmp 作成されたBitmap画像
	 */
	public static void save(Bitmap bmp)
	{
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String imgPath = Path.PICFOLDER_PATH + File.separator + simpleDateFormat.format(calendar.getTime()) + ".png";

		try
		{
			FileOutputStream fos;
			fos = new FileOutputStream(imgPath, true);

			bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.close();
		}
		catch (FileNotFoundException e)
		{
			Log.d(TAG, "FileNotFoundException");
			Log.d(TAG, e.getMessage());
		}
		catch (Exception e)
		{
			Log.d(TAG, "Exception");
			Log.d(TAG, e.getMessage());
		}
	}

}