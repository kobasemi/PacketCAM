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
		// Java NIOに転送した頂点バッファや色バッファを格納する変数を定義
		// 頂点バッファ
		private FloatBuffer mVertexBuffer;
		// 色バッファ
		private FloatBuffer mColorBuffer;

        float x, y, width, height;

        public DrawBlendingRectangle(int x, int y, int w, int h, COLOR color)
            {
                this(x/100.f, y/100.f, w/100.f, h/100.f, color);
            }

        public DrawBlendingRectangle(float x, float y, float width, float height, COLOR color){
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
			}


		/**
		 * ポート番号から，オブジェクト生成用の座標を求める
		 * @param port
		 * @return
		 */
		protected static short[] calcPort(short port)
			{
				// ポート番号にマイナス？が混じっている場合があるので，取り除く
				port = minusReducer(port);

				// ポート番号をchar配列に
				char[] portChar = String.valueOf(port).toCharArray();

				if ((portChar.length % 2) != 0)
					{
						// 桁数が奇数の場合の処理
						// だいたい真ん中を求める
						int aboutCenter = portChar.length / 2;

						char[] firstChar = new char[aboutCenter + 1];

						int j = 0;
						for (int i = 0; i < aboutCenter + 1; i++)
							{
								firstChar[i] = portChar[i];
								j++;
							}

						char[] secondChar = new char[aboutCenter];

						for (int i = 0; i < aboutCenter; i++)
							{
								secondChar[i] = portChar[j];
								j++;
							}

						// firstの方が桁数が多くなるはず
						short first = Short.valueOf(String.valueOf(firstChar));
						short second = Short.valueOf(String.valueOf(secondChar));

						// PORT番号の幅は，0~65535
						// firstが3桁以上の場合，ひたすら2で割って2桁に抑える
						first = digitReducer(first);
						second = digitReducer(second);

						short[] data = new short[2];
						data[0] = first;
						data[1] = second;
						return data;
					}
				else
					{
						// 桁数が偶数の場合
						// 4桁か，2桁
						int center = portChar.length / 2;
						char[] firstChar = new char[center];

						int j = 0;
						for (int i = 0; i < center; i++)
							{
								firstChar[i] = portChar[i];
								j++;
							}

						char[] secondChar = new char[center];

						for (int i = 0; i < center; i++)
							{
								secondChar[i] = portChar[j];
								j++;
							}

						short first = Short.valueOf(String.valueOf(firstChar));
						short second = Short.valueOf(String.valueOf(secondChar));

						first = digitReducer(first);
						second = digitReducer(second);

						short[] data = new short[2];
						data[0] = first;
						data[1] = second;
						return data;
					}
			}


		protected static short minusReducer(short num)
			{
				if (num >= 0) return num;

				char[] oldNumCharArray = String.valueOf(num).toCharArray();
				char[] newNumCharArray = new char[oldNumCharArray.length - 1];

				for (int i = 1; i < oldNumCharArray.length; i++)
					{
						newNumCharArray[i - 1] = oldNumCharArray[i];
					}

				short newNum = Short.valueOf(String.valueOf(newNumCharArray));
				return newNum;
			}


		// 3桁以上の値を10で割って2桁に抑える
		protected static short digitReducer(short source)
			{
				if (String.valueOf(source).length() > 2)
					{
						source = (short)(source / 10);
						digitReducer(source);
					}

				return source;
			}
	}
