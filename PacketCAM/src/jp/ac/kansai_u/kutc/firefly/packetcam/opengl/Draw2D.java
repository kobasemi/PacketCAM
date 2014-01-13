package jp.ac.kansai_u.kutc.firefly.packetcam.opengl;

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
public class Draw2D
	{
		// Java NIOに転送した頂点バッファや色バッファを格納する変数を定義
		// 頂点バッファ
		private FloatBuffer mVertexBuffer;
		// 色バッファ
		private FloatBuffer mColorBuffer;

        public Draw2D(int x, int y, int w, int h, COLOR color)
            {
                setDrawObject(x/100.f, y/100.f, w/100.f, h/100.f, color);
            }

        public Draw2D(float x, float y, float width, float height, COLOR color){
            setDrawObject(x, y, width, height, color);
        }

        /**
         * 描画図形の設定を行う
         * x, y座標及びwidth, heightは，パーセンテージで指定する
         * 例
         * x: .25f
         * y: .25f
         * width: .5f
         * height: .5f
         * を指定した場合，
         * 画面プレビューのXY25%の位置に画面プレビューの50％の大きさの図形が描画される
         *
         * @param x      x座標[%]
         * @param y      y座標[%]
         * @param width  幅[%]
         * @param height 高さ[%]
         * @param color  EnumクラスのCOLORで指定できる色
         */
        private void setDrawObject(float x, float y, float width, float height, COLOR color){
            float left   = x * 2.0f - 1.0f;
            float top    = y * 2.0f - 1.0f;
            float right  = left + width  * 2.0f;
            float bottom = top  + height * 2.0f;

            // 上下を反転させる（左下原点から左上原点へ）
            top = -top;
            bottom = -bottom;

            // 位置情報(x, y)
            float positions[] = {
                left , top   , // 左上
                left , bottom, // 左下
                right, top   , // 右上
                right, bottom, // 右下
            };

            mVertexBuffer = makeFloatBuffer(positions);
            mColorBuffer = makeFloatBuffer(GL_Color.getColorArray(color));
        }

		/**
		 * オブジェクトの描画メソッド
		 *
		 * @param gl
		 */
		public void draw(GL10 gl)
			{
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
		 *
		 * @param arr 配列
		 * @return 生成されたFloatBuffer
		 */
		protected static FloatBuffer makeFloatBuffer(float[] arr)
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
		 *
		 * @param arr 配列
		 * @return 生成されたIntBuffer
		 */
		protected static IntBuffer makeIntBuffer(int[] arr)
			{
				ByteBuffer bb = ByteBuffer.allocateDirect(arr.length * 4);
				bb.order(ByteOrder.nativeOrder());
				IntBuffer ib = bb.asIntBuffer();
				ib.put(arr);
				ib.position(0);
				return ib;
			}
	}
