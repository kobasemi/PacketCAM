package jp.ac.kansai_u.kutc.firefly.packetcam.opengl;

import android.graphics.Bitmap;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Path;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Bitmap画像をpng形式でSDカードに保存する
 * @author Kousaka
 */
public class SaveSDCard
	{
		/**
		 * 作成した画像を受け取って，SDカードに保存する
		 *
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
						e.printStackTrace();
					}
				catch (Exception e)
					{
						e.printStackTrace();
					}
			}

	}
