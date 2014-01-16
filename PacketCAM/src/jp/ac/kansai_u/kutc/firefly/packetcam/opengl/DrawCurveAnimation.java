package jp.ac.kansai_u.kutc.firefly.packetcam.opengl;

import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Enum;

import javax.microedition.khronos.opengles.GL10;
import java.nio.FloatBuffer;

/**
 * カーブアニメーション描画クラス
 * @author akasaka
 */
public class DrawCurveAnimation {

    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;

    float x, y;
    int pointSize = 10;

    public DrawCurveAnimation(int x, int y, Enum.COLOR color){
        this(x/100.f, y/100.f, color);
    }

    public DrawCurveAnimation(float x, float y, Enum.COLOR color){
        setDrawPoint(x, y);
        vertexBuffer = EffectRenderer.pointBuffer;
        colorBuffer  = GL_Color.getColorFloatBuffer(color);
    }

    /**
     * 描画図形の設定を行う
     * x, y座標は，パーセンテージで指定する
     *
     * @param x x座標[%]
     * @param y y座標[%]
     */
    private void setDrawPoint(float x, float y){
        // from OpenGL ES座標系 to 正規化されたデバイス座標系
        this.x = x * 2.f - 1.f;
        this.y = y * 2.f - 1.f;

        // 上下を反転させる（左下原点から左上原点へ）
        this.y = -this.y;
    }

    /**
     * オブジェクトの描画メソッド
     * @param gl10
     */
    public void draw(GL10 gl10){
        gl10.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl10.glEnableClientState(GL10.GL_COLOR_ARRAY);

        gl10.glPointSize(pointSize);

        gl10.glMatrixMode(GL10.GL_MODELVIEW);
        gl10.glLoadIdentity();
        gl10.glTranslatef(x, y, 0.f);

        gl10.glVertexPointer(2, GL10.GL_FLOAT, 0, vertexBuffer);
        gl10.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);

        // 描画モード（点とか線とかいろいろ）を設定
        gl10.glDrawArrays(GL10.GL_POINTS, 0, 1);
        gl10.glDisableClientState(GL10.GL_COLOR_ARRAY);
        gl10.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }
}
