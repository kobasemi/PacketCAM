package jp.ac.kansai_u.kutc.firefly.packetcam.opengl;

import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Enum.COLOR;
import org.apache.commons.collections.primitives.ArrayFloatList;

import java.nio.FloatBuffer;

/**
 * OpenGL ESで使用する色情報に関するクラス
 *
 * @author akasaka
 */
public class GL_Color
	{
		// 透明度
		private static float transparency = 0.75f;
		// 1頂点ごとの色情報
        private static final float[] redVertex = {
                1.f, 0.f, 0.f, transparency,
        };
        private static final float[] greenVertex = {
                0.f, 1.f, 0.f, transparency,
        };
        private static final float[] blueVertex = {
                0.f, 0.f, 1.f, transparency,
        };
        private static final float[] cyanVertex = {
                0.f, 1.f, 1.f, transparency,
        };
        private static final float[] magentaVertex = {
                1.f, 0.f, 1.f, transparency,
        };
        private static final float[] yellowVertex = {
                1.f, 1.f, 0.f, transparency,
        };
        private static final float[] whiteVertex = {
                1.f, 1.f, 1.f, transparency,
        };
        private static final float[] blackVertex = {
                0.f, 0.f, 0.f, transparency,
        };

        // 4頂点ごとの色情報
        private static final float[] RED = {
				1.f, 0.f, 0.f, transparency,
				1.f, 0.f, 0.f, transparency,
				1.f, 0.f, 0.f, transparency,
				1.f, 0.f, 0.f, transparency,
		};
		private static final float[] GREEN = {
				0.f, 1.f, 0.f, transparency,
				0.f, 1.f, 0.f, transparency,
				0.f, 1.f, 0.f, transparency,
				0.f, 1.f, 0.f, transparency,
		};
		private static final float[] BLUE = {
				0.f, 0.f, 1.f, transparency,
				0.f, 0.f, 1.f, transparency,
				0.f, 0.f, 1.f, transparency,
				0.f, 0.f, 1.f, transparency,
		};
		private static final float[] CYAN = {
				0.f, 1.f, 1.f, transparency,
				0.f, 1.f, 1.f, transparency,
				0.f, 1.f, 1.f, transparency,
				0.f, 1.f, 1.f, transparency,
		};
		private static final float[] MAGENTA = {
				1.f, 0.f, 1.f, transparency,
				1.f, 0.f, 1.f, transparency,
				1.f, 0.f, 1.f, transparency,
				1.f, 0.f, 1.f, transparency,
		};
		private static final float[] YELLOW = {
				1.f, 1.f, 0.f, transparency,
				1.f, 1.f, 0.f, transparency,
				1.f, 1.f, 0.f, transparency,
				1.f, 1.f, 0.f, transparency,
		};
		private static final float[] WHITE = {
				1.f, 1.f, 1.f, transparency,
				1.f, 1.f, 1.f, transparency,
				1.f, 1.f, 1.f, transparency,
				1.f, 1.f, 1.f, transparency,
		};
		private static final float[] BLACK = {
				0.f, 0.f, 0.f, transparency,
				0.f, 0.f, 0.f, transparency,
				0.f, 0.f, 0.f, transparency,
				0.f, 0.f, 0.f, transparency,
		};

		/**
		 * OpenGLで使用する色情報の配列を返す
		 *
		 * @param color 取得したい色
		 * @return 色配列
		 */
		static float[] getColorArray(COLOR color)
			{
				switch (color)
					{
						case RED:
							return RED;
						case GREEN:
							return GREEN;
						case BLUE:
							return BLUE;
						case CYAN:
							return CYAN;
						case MAGENTA:
							return MAGENTA;
						case YELLOW:
							return YELLOW;
						case WHITE:
							return WHITE;
						case BLACK:
							return BLACK;
						default:
							return null;
					}
			}

        // 各色のFloatBuffer
        private static FloatBuffer red     = null;
        private static FloatBuffer green   = null;
        private static FloatBuffer blue    = null;
        private static FloatBuffer cyan    = null;
        private static FloatBuffer magenta = null;
        private static FloatBuffer yellow  = null;
        private static FloatBuffer white   = null;
        private static FloatBuffer black   = null;

        /**
         * OpenGL ESで使用する色情報のFloatBufferを返す
         * 各色のバッファは一度のみ，ネイティブのメモリ領域に確保される
         * 以後は，各色の参照を返す
         * @param color 取得したい色
         * @return 各色のバッファ
         */
        static FloatBuffer getColorFloatBuffer(COLOR color){
            switch(color){
                case RED:
                    return red     == null? red     = EffectRenderer.makeFloatBuffer(RED)    : red;
                case GREEN:
                    return green   == null? green   = EffectRenderer.makeFloatBuffer(GREEN)  : green;
                case BLUE:
                    return blue    == null? blue    = EffectRenderer.makeFloatBuffer(BLUE)   : blue;
                case CYAN:
                    return cyan    == null? cyan    = EffectRenderer.makeFloatBuffer(CYAN)   : cyan;
                case MAGENTA:
                    return magenta == null? magenta = EffectRenderer.makeFloatBuffer(MAGENTA): magenta;
                case YELLOW:
                    return yellow  == null? yellow  = EffectRenderer.makeFloatBuffer(YELLOW) : yellow;
                case WHITE:
                    return white   == null? white   = EffectRenderer.makeFloatBuffer(WHITE)  : white;
                case BLACK:
                    return black   == null? black   = EffectRenderer.makeFloatBuffer(BLACK)  : black;
                default:
                    return null;
            }
        }

        /**
         * 任意の数の頂点の色情報配列を作成する
         * @param color 作成する色
         * @param vertexes 頂点の数
         * @return 色情報配列
         */
        static float[] makeColorVertex(COLOR color, int vertexes){
            ArrayFloatList floatList = new ArrayFloatList();
            switch(color){
                case RED:
                    for(int i=0; i<vertexes; i++)
                        for(float red: redVertex)
                            floatList.add(red);
                    break;
                case GREEN:
                    for(int i=0; i<vertexes; i++)
                        for(float green: greenVertex)
                            floatList.add(green);
                    break;
                case BLUE:
                    for(int i=0; i<vertexes; i++)
                        for(float blue: blueVertex)
                            floatList.add(blue);
                    break;
                case CYAN:
                    for(int i=0; i<vertexes; i++)
                        for(float cyan: cyanVertex)
                            floatList.add(cyan);
                    break;
                case MAGENTA:
                    for(int i=0; i<vertexes; i++)
                        for(float magenta: magentaVertex)
                            floatList.add(magenta);
                    break;
                case YELLOW:
                    for(int i=0; i<vertexes; i++)
                        for(float yellow: yellowVertex)
                            floatList.add(yellow);
                    break;
                case WHITE:
                    for(int i=0; i<vertexes; i++)
                        for(float white: whiteVertex)
                            floatList.add(white);
                    break;
                case BLACK:
                    for(int i=0; i<vertexes; i++)
                        for(float black: blackVertex)
                            floatList.add(black);
                    break;
                default:
                    return null;
            }

            return floatList.toArray();
        }
	}
