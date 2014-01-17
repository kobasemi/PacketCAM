package jp.ac.kansai_u.kutc.firefly.packetcam.opengl;

import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Enum.COLOR;

import javax.microedition.khronos.opengles.GL10;
import java.nio.FloatBuffer;

/**
 * このクラスの中で様々な図形の描画メソッドを定義し，GLViewにおいて適宜
 * 該当メソッドを指定して描画させればいい感じかもしれない
 * @author akasaka Kousaka
 */
public class DrawBlendingRectangle
	{
		private static final String TAG = DrawBlendingRectangle.class.getSimpleName();
		// Java NIOに転送した頂点バッファや色バッファを格納する変数を定義
		// 頂点バッファ
		private FloatBuffer mVertexBuffer;
		// 色バッファ
		private FloatBuffer mColorBuffer;

        float x, y, width, height;

		private int objectCountDown = 0;
		private boolean deadFlag = false;

        public DrawBlendingRectangle(int x, int y, int w, int h, COLOR color, short ttl)
            {
                // 座標を0 ~ 255の範囲に正規化
                this(x/255.f, y/255.f, w/100.f, h/100.f, color, ttl);
            }

        private DrawBlendingRectangle(float x, float y, float width, float height, COLOR color, short ttl){
			// IDとttlを設定
			this.objectCountDown = ttl;

            mVertexBuffer = EffectRenderer.rectangleBuffer;
            mColorBuffer  = GL_Color.getColorFloatBuffer(color);
            setDrawObject(x, y, width, height);
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
         */
        private void setDrawObject(float x, float y, float width, float height){
            this.width  = width;
            this.height = height;

            // 座標位置を正規化したのち，サイズ分移動する
            this.x = (x * 2.f - 1.f) + width;
            this.y = (y * 2.f - 1.f) + height;

            // 上下を反転させる（左下原点から左上原点へ）
            this.y = -this.y;
        }

		/**
		 * オブジェクトの描画メソッド
		 *
		 * @param gl
		 */
		public void draw(GL10 gl)
			{
				// カメラプレビュー描画後に，ブレンドを有効化する
				gl.glEnable(GL10.GL_BLEND);

				// ブレンドモードを指定
				// src, dst
				gl.glBlendFunc(GL10.GL_ONE_MINUS_DST_COLOR, GL10.GL_ONE_MINUS_SRC_ALPHA);

				gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
				gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

				gl.glMatrixMode(GL10.GL_MODELVIEW);
				gl.glLoadIdentity();
				gl.glTranslatef(x, y, 0.f);
				gl.glScalef(width, height, 0.f);

				// 頂点バッファのポインタの場所を設定
				gl.glVertexPointer(2, GL10.GL_FLOAT, 0, mVertexBuffer);
				// 色バッファのポインタの場所を設定
				gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);

				// 描画モード（点とか線とかいろいろ）を設定
				gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
				gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
				gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
				gl.glDisable(GL10.GL_BLEND);

				this.objectCountDown--;

				if (this.objectCountDown <= 0)
					{
						deadFlag = true;
					}
			}


		protected boolean getDeadFlag()
			{
				return deadFlag;
			}
	}
