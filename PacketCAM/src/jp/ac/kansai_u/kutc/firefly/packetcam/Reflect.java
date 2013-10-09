package jp.ac.kansai_u.kutc.firefly.packetcam;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.hardware.Camera;
import android.hardware.Camera.Size;

/**
 * Androidバージョン間の差異を吸収するクラス
 */
public class Reflect
{
	// バージョン（端末？）によって必要になるかも
	private static Method Parameters_getSupportedPreviewSizes;

	static
	{
		initCompatibility();
	};

	private static void initCompatibility()
	{
		try
		{
			Parameters_getSupportedPreviewSizes = Camera.Parameters.class.getMethod("getSupportedPreviewSizes", new Class[] {});
		}
		catch (NoSuchMethodException name)
		{
		}
	}


	@SuppressWarnings("unchecked")
	public static List<Size> getSuportedPreviewSizes (Camera.Parameters p)
	{
		try
		{
			if (Parameters_getSupportedPreviewSizes != null)
			{
				return (List<Size>) Parameters_getSupportedPreviewSizes.invoke(p);
			}
			else
			{
				return null;
			}
		}
		catch (InvocationTargetException ite)
		{
			Throwable cause = ite.getCause();

			if (cause instanceof RuntimeException)
			{
				throw (RuntimeException) cause;
			}
			else if (cause instanceof Error)
			{
				throw (Error) cause;
			}
			else
			{
				throw new RuntimeException(ite);
			}
		}
		catch (IllegalAccessException ie)
		{
			return null;
		}
	}
}
