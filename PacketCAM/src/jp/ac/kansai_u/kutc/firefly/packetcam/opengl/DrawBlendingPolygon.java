package jp.ac.kansai_u.kutc.firefly.packetcam.opengl;

import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Enum.COLOR;
import org.apache.commons.collections.primitives.ArrayFloatList;

import javax.microedition.khronos.opengles.GL10;
import java.nio.FloatBuffer;

/**
 * 多角形オブジェクトを生成する
 * @author akasaka
 */
public class DrawBlendingPolygon {
    private static final String TAG = DrawBlendingPolygon.class.getSimpleName();
    // Java NIOに転送した頂点バッファや色バッファを格納する変数を定義
    // 頂点バッファ
    private FloatBuffer mVertexBuffer;
    // 色バッファ
    private FloatBuffer mColorBuffer;

    float x, y;
    float size = .3f;  // 30%
    int divide;

    private int objectCountDown = 0;
    private boolean deadFlag = false;

    public DrawBlendingPolygon(int x, int y, COLOR color, short ttl, short d){
        // 座標を0 ~ 255の範囲に正規化
        // d: 分割数
        this(x/255.f, y/255.f, color, ttl, d);
    }

    private DrawBlendingPolygon(float x, float y, COLOR color, short ttl, short d){
        // IDとttlを設定
        this.objectCountDown = ttl;

        if(d == 0)
            // 分割数が0の場合，円を表示する
            divide = 36;
        else
            divide = d;

        setDrawObject(x, y, color, divide);
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
     * @param x     x座標[%]
     * @param y     y座標[%]
     * @param color 色情報
     * @param d     円の分割数
     */
    private void setDrawObject(float x, float y, COLOR color, int d){
        // 座標位置を正規化したのち，サイズ分移動する
        this.x = x * 2.f - 1.f;
        this.y = y * 2.f - 1.f;

        // 上下を反転させる（左下原点から左上原点へ）
        this.y = -this.y;

        double theta;  // 角度
        float positionX;  // 描画頂点のx座標
        float positionY;  // 描画頂点のy座標

        ArrayFloatList floatList = new ArrayFloatList();
        // 原点の頂点情報を格納する
        // GL_TRIANGLE_FANモードで，原点を中心に三角形を書いていく
        floatList.add(0.f);
        floatList.add(0.f);

        for(int i=0; i<=d; i++){
            // 分割数+1までループする
            // 最後の三角形を繋げるため，ループの最初の値と同じ値を最後に追加する
            theta = Math.toRadians(360/d * i);
            positionX = (float)Math.cos(theta);
            positionY = (float)Math.sin(theta);
            floatList.add(positionX);
            floatList.add(positionY);
        }
        // 頂点情報を作成する
        mVertexBuffer = EffectRenderer.makeFloatBuffer(floatList.toArray());
        // divide+2: 分割数 + 原点の情報 + 三角形を繋げるために最後に追加した頂点情報
        mColorBuffer  = EffectRenderer.makeFloatBuffer(GL_Color.makeColorVertex(color, divide + 2));
    }

    /**
     * オブジェクトの描画メソッド
     *
     * @param gl
     */
    public void draw(GL10 gl){
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
        gl.glScalef(size, size, 0.f);

        // 頂点バッファのポインタの場所を設定
        gl.glVertexPointer(2, GL10.GL_FLOAT, 0, mVertexBuffer);
        // 色バッファのポインタの場所を設定
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);

        // 描画モード（点とか線とかいろいろ）を設定
        // GL_TRIANGLE_FAN: 原点を中心に扇型に描画する
        gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, divide+2);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisable(GL10.GL_BLEND);

        this.objectCountDown--;

        if (this.objectCountDown <= 0){
            deadFlag = true;
        }
    }

    /**
     * オブジェクトが寿命を迎えたかを取得する
     * @return 生死
     */
    protected boolean getDeadFlag(){ return deadFlag; }
}
