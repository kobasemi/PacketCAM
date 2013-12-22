package jp.ac.kansai_u.kutc.firefly.packetcam.opengl;

import android.util.Log;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Enum.POSITION;

import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by Kousaka on 2013/12/10.
 * このクラスの中で様々な図形の描画メソッドを定義し，GLViewにおいて適宜
 * 該当メソッドを指定して描画させればいい感じかもしれない
 */
public class Draw2D
{
	// Java NIOに転送した頂点バッファや色バッファを格納する変数を定義
	// 頂点バッファ
	private FloatBuffer mVertexBuffer;

	// 色バッファ
	private FloatBuffer mColorBuffer;


	/**
	 * コンストラクタ
	 */
	public Draw2D(POSITION position)
	{
		if (position == POSITION.A)
		{
			Log.d("Draw2D", "makeFloatBufferA");

			// 図形の頂点の配列，色の配列を定義
			// 頂点情報の配列（x,y）＝（200f, 200f）
			// OpenGLの座標系は，左下が（0, 0）になっている
			float vertices[] = {
					200f, 200f,
					250f, 250f,
					100f, 300f
			};

			// 色情報の配列（Red, Green, Blue, Alpha）
			float colors[] = {
					1f, 1f, 1f, 1f,
					1f, 1f, 1f, 1f,
					1f, 1f, 1f, 1f,
			};

			mVertexBuffer = makeFloatBuffer(vertices);
			mColorBuffer = makeFloatBuffer(colors);
		}
		else if (position == POSITION.B)
		{
			Log.d("Draw2D", "makeFloatBufferB");
			float vertices[] = {
					500f, 500f,
					550f, 550f,
					400f, 600f,
			};
			float colors[] = {
					1f, 1f, 1f, 1f,
					1f, 1f, 1f, 1f,
					1f, 1f, 1f, 1f,
			};

			mVertexBuffer = makeFloatBuffer(vertices);
			mColorBuffer = makeFloatBuffer(colors);
		}
	}


	/**
	 * オブジェクトの描画設定メソッド
	 * @param gl
	 */
	public void draw (GL10 gl)
	{
		// 点のサイズ
		gl.glPointSize(10);

		// 頂点バッファのポインタの場所を設定
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, mVertexBuffer);

		// 色バッファのポインタの場所を設定
		gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);

		// 描画モード（点とか線とかいろいろ）を設定
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);
	}


	/**
	 * float型の配列からダイレクトなNew I/OのFloatBufferを生成します．
	 * @param arr 配列
	 * @return 生成されたFloatBuffer
	 */
	protected static FloatBuffer makeFloatBuffer (float[] arr)
	{
		ByteBuffer bb = ByteBuffer.allocateDirect(arr.length * 4);
		bb.order(ByteOrder.nativeOrder());
		FloatBuffer fb = bb.asFloatBuffer();
		fb.put(arr);
		fb.position(0);
		return fb;
	}


	/**
	 * int型の配列からダイレクトなNew I/OのIntBufferを生成します．
	 * @param arr 配列
	 * @return 生成されたIntBuffer
	 */
	protected static IntBuffer makeIntBuffer (int[] arr)
	{
		ByteBuffer bb = ByteBuffer.allocateDirect(arr.length * 4);
		bb.order(ByteOrder.nativeOrder());
		IntBuffer ib = bb.asIntBuffer();
		ib.put(arr);
		ib.position(0);
		return ib;
	}
}
