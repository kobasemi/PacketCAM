package jp.ac.kansai_u.kutc.firefly.packetcam;

public class GetGCD
{
	public static int getGCD(int x, int y)
	{
		int tmp = 0;
		
		// XよりもYの方が値が大きければ，tmp変数を使って入れ替える
		if (x < y)
		{
			tmp = x;
			x = y;
			y = tmp;
		}
		
		if (x % y == 0)
		{
			return y;
		}
		
		// 再帰関数（上のif文に引っかかるまでgetGCDを繰り返す）
		return getGCD (y, x % y);
	}
}
