package jp.ac.kansai_u.kutc.firefly.packetcam.opengl;

import android.opengl.GLU;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Enum.COLOR;

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
public class Draw2D {
	// Java NIOに転送した頂点バッファや色バッファを格納する変数を定義
	// 頂点バッファ
	private FloatBuffer mVertexBuffer;

	// 色バッファ
	private FloatBuffer mColorBuffer;

	private float colors[] = {};

	private static int mWidth = 0, mHeight = 0;


	/**
	 * 図形オブジェクトの描画位置と描画カラーを設定する
	 * @param x x座標
	 * @param y y座標
	 * @param w 幅
	 * @param h 高さ
	 * @param color EnumクラスのCOLORで指定できる色
	 */
	public Draw2D (int x, int y, int w, int h, COLOR color)
	{
		float left = (float)x;
		float right = (float)x + (float)w;
		float bottom = (float)y;
		float top = (float)y + (float)h;


		// 最初の描画で上３つ，次の描画で下の３つが使われ，２個の三角形で四角形を描画する
		float positions[] = {
				//x, y
				left, bottom, // 左下
				right, bottom, // 右下
				left, top, // 左上
				right, top, // 右上
		};

        this.colors = GL_Color.getColorArray(color);

		mVertexBuffer = makeFloatBuffer(positions);
		mColorBuffer = makeFloatBuffer(colors);
	}


	public void setSize(int width, int height)
	{
		mWidth = width; mHeight = height;
	}


	/**
	 * オブジェクトの描画メソッド
	 * @param gl
	 */
	public void draw (GL10 gl)
	{
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();

		GLU.gluOrtho2D(gl, 0.0f, mWidth, 0.0f, mHeight);

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

		gl.glMatrixMode(GL10.GL_MODELVIEW);

		gl.glLoadIdentity();
		// 点のサイズ
		gl.glPointSize(10);

		// 頂点バッファのポインタの場所を設定
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, mVertexBuffer);

		// 色バッファのポインタの場所を設定
		gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);

		// 描画モード（点とか線とかいろいろ）を設定
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
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
