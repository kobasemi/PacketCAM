package jp.ac.kansai_u.kutc.firefly.packetcam.utils;

/**
 * Created by Kousaka on 13/12/22.
 */
public class Enum
	{
		public static enum STATUS
			{
				RUNNING, STOP
			}

		// 描画の初期位置
		public static enum POSITION
			{
				A, B, C
			}

		// 移動速度
		public static enum SPEED
			{
				A, B, C, D, E
			}

		// 描画時間
		public static enum DURATION
			{
				A, B, C, D, E
			}

		// 描画するかどうか
		public static enum VISIBILITY
			{
				VISIBLE, INVISIBLE
			}

		// 描画方法 http://atelier-yoka.com/dev_android/p_main.php?file=apigl10gldrawarrays
		public static enum SHAPE
			{
				POINT, LINE, LINE_STRIP, LINE_LOOP, TRIANGLE, TRIANGLE_STRIP, TRIANGLE_FAN
			}

		public static enum COLOR
			{
				RED, GREEN, BLUE, CYAN, MAGENTA, YELLOW, BLACK, WHITE
			}

		public static enum DRAWSTATE
			{
				PREPARATION, READY
			}

		public static enum ZOOM
			{
				ZOOMUP, ZOOMDOWN
			}
	}
