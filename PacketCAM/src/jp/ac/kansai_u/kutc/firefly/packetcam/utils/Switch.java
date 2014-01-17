package jp.ac.kansai_u.kutc.firefly.packetcam.utils;

import android.util.Log;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Enum.*;

/**
 * スイッチクラス．まさにスイッチ
 * @author Kousaka
 */
public class Switch
	{
		private static final String TAG = Switch.class.getSimpleName();

		private static Switch instance = new Switch();

		/**
		 * スイッチのインスタンスを返す
		 * @return Switchインスタンス
		 */
		public static Switch getInstance()
			{
				return instance;
			}

		private VISIBILITY visibility = VISIBILITY.INVISIBLE;
		private STATUS status = STATUS.STOP;
		private DRAWSTATE drawstate = DRAWSTATE.PREPARATION;

		private boolean shutter = false;

		/**
		 * 現在のVISIBILITYを返す
		 * @return VISIBILITY
		 */
		public VISIBILITY getVisibility()
			{
				return visibility;
			}

		/**
		 * VISIBILITYをスイッチする（VISIBLE or INVISIBLE）
		 */
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

		/**
		 * シャッターステータスを返す
		 * @return boolean型のシャッターステータス
		 */
		public boolean getShutter()
			{
				return shutter;
			}

		/**
		 * シャッターステータスをスイッチする（true or false）
		 */
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

		/**
		 * 現在の描画ステータスを返す
		 * @return DRAWSTATE
		 */
		public DRAWSTATE getDrawstate()
			{
				return drawstate;
			}

		/**
		 * 描画ステータスをスイッチする（PREPARATION or READY）
		 */
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


		/**
		 * 動作ステータスを返す
		 * @return STATUS
		 */
		public STATUS getStatus()
			{
				return status;
			}


		/**
		 * 動作ステータスをスイッチする（STOP or RUNNING）
		 */
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
	}
