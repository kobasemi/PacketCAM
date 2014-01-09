package jp.ac.kansai_u.kutc.firefly.packetcam.opengl;

import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Enum.COLOR;

/**
 * OpenGL ESで使用する色情報に関するクラス
 * @author akasaka
 */
public class GL_Color {
    // 透明度
    static float transparency = 0.75f;
    // 色情報
    static float red[] = {
            1.f, 0.f, 0.f, transparency,
            1.f, 0.f, 0.f, transparency,
            1.f, 0.f, 0.f, transparency,
            1.f, 0.f, 0.f, transparency,
    };
    static float green[] = {
            0.f, 1.f, 0.f, transparency,
            0.f, 1.f, 0.f, transparency,
            0.f, 1.f, 0.f, transparency,
            0.f, 1.f, 0.f, transparency,
    };
    static float blue[] = {
            0.f, 0.f, 1.f, transparency,
            0.f, 0.f, 1.f, transparency,
            0.f, 0.f, 1.f, transparency,
            0.f, 0.f, 1.f, transparency,
    };
    static float cyan[] = {
            0.f, 1.f, 1.f, transparency,
            0.f, 1.f, 1.f, transparency,
            0.f, 1.f, 1.f, transparency,
            0.f, 1.f, 1.f, transparency,
    };
    static float magenta[] = {
            1.f, 0.f, 1.f, transparency,
            1.f, 0.f, 1.f, transparency,
            1.f, 0.f, 1.f, transparency,
            1.f, 0.f, 1.f, transparency,
    };
    static float yellow[] = {
            1.f, 1.f, 0.f, transparency,
            1.f, 1.f, 0.f, transparency,
            1.f, 1.f, 0.f, transparency,
            1.f, 1.f, 0.f, transparency,
    };
    static float white[] = {
            1.f, 1.f, 1.f, transparency,
            1.f, 1.f, 1.f, transparency,
            1.f, 1.f, 1.f, transparency,
            1.f, 1.f, 1.f, transparency,
    };
    static float black[] = {
            0.f, 0.f, 0.f, transparency,
            0.f, 0.f, 0.f, transparency,
            0.f, 0.f, 0.f, transparency,
            0.f, 0.f, 0.f, transparency,
    };

    /**
     * OpenGLで使用する色情報の配列を返す
     * @param color 取得したい色
     * @return 色配列
     */
    static float[] getColorArray(COLOR color){
        switch(color){
            case RED:
                return red;
            case GREEN:
                return green;
            case BLUE:
                return blue;
            case CYAN:
                return cyan;
            case MAGENTA:
                return magenta;
            case YELLOW:
                return yellow;
            case WHITE:
                return white;
            case BLACK:
                return black;
            default:
                return null;
        }
    }
}
