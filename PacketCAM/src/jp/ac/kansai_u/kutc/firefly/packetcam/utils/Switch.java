package jp.ac.kansai_u.kutc.firefly.packetcam.utils;

import android.util.Log;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Enum.*;

/**
 * Created by kousaka on 2014/01/06.
 */
public class Switch
	{
		private static final String TAG = Switch.class.getSimpleName();

		private static Switch instance = new Switch();

		public static Switch getInstance()
			{
				return instance;
			}

		private VISIBILITY visibility = VISIBILITY.INVISIBLE;
		private STATUS status = STATUS.STOP;
		private DRAWSTATE drawstate = DRAWSTATE.PREPARATION;

		private boolean shutter = false;
		private boolean inoutStatus = false;

		public VISIBILITY getVisibility()
			{
				return visibility;
			}

		public void switchVisibility()
			{
				Log.d(TAG, "switchVisibility");
				if (visibility == VISIBILITY.VISIBLE)
					{
						visibility = VISIBILITY.INVISIBLE;
					}
				else
					{
						visibility = VISIBILITY.VISIBLE;
					}
			}

		public boolean getShutter()
			{
				return shutter;
			}

		public void switchShutter()
			{
				if (!shutter)
					{
						shutter = true;
					}
				else
					{
						shutter = false;
					}
			}

		public DRAWSTATE getDrawstate()
			{
				return drawstate;
			}

		public void switchDrawState()
			{
				if (drawstate == DRAWSTATE.PREPARATION)
					{
						drawstate = DRAWSTATE.READY;
					}
				else
					{
						drawstate = DRAWSTATE.PREPARATION;
					}
			}


		public STATUS getStatus()
			{
				return status;
			}


		public void switchStatus()
			{
				if (status == STATUS.STOP)
					{
						status = STATUS.RUNNING;
					}
				else
					{
						status = STATUS.STOP;
					}
			}

		public boolean getCameraInOut()
			{
				return inoutStatus;
			}

		public void switchCameraInOut()
			{
				if (!inoutStatus)
					{
						inoutStatus = true;
					}
				else
					{
						inoutStatus = false;
					}
			}
	}
