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
 * Created by black_000 on 14/01/07.
 */
public class SaveSDCard {
	private static final String TAG = "SaveSDCard";

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
