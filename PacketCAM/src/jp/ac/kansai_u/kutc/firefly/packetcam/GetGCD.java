package jp.ac.kansai_u.kutc.firefly.packetcam;

/**
 * 与えられた2数からユークリッド互除法を用いて最大公約数を求める
 */
public class GetGCD
{
	/**
	 * 与えられた2数から最大公約数を求め，結果を返す
	 * @param x 1つ目の数
	 * @param y 2つ目の数
	 * @return 最大公約数
	 */
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
