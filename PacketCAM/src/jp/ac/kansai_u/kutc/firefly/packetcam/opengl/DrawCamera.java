package jp.ac.kansai_u.kutc.firefly.packetcam.opengl;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Enum.DRAWSTATE;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Enum.ZOOM;
import jp.ac.kansai_u.kutc.firefly.packetcam.utils.Switch;

import javax.microedition.khronos.opengles.GL10;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.List;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;

/**
 * カメラプレビューに関する処理を行うクラス
 * @author Kousaka akasaka
 */
public class DrawCamera
	{
		private FloatBuffer mVertexBuffer; // 描画先の頂点座標バッファ
		private FloatBuffer mTexCoordsBuffer; // 描画元の座標バッファ（どこを使うか的な）

		private int textureId; // 生成時にIDを格納し，描画時に利用する
		private SurfaceTexture mSurfaceTexture; // カメラプレビューの描画先テクスチャ

		private static Camera mCamera = null;
		private Switch mSwitch;

		private int nowZoom;

		// 描画先の座標
		private float vertices[] = {
				// 上下を反転させる必要がある
				-1.0f, 1.0f, 0, // 左下
				1.0f, 1.0f, 0, // 右下
				-1.0f, -1.0f, 0, // 左上
				1.0f, -1.0f, 0, // 右上
		};

		// 描画元の座標
		private float texcoords[] = {
				0.0f, 0.0f, // 左下
				1.0f, 0.0f, // 右下
				0.0f, 1.0f, // 左上
				1.0f, 1.0f, // 右上
		};


		/**
		 * Float配列からバッファを作成する
		 */
		public DrawCamera()
			{
				mSwitch = Switch.getInstance();

				mVertexBuffer = EffectRenderer.makeFloatBuffer(vertices);
				mTexCoordsBuffer = EffectRenderer.makeFloatBuffer(texcoords);
			}


		/**
		 * Textureを生成し，TextureIDを取得
		 *
		 * @param gl
		 */
		public void generatedTexture(GL10 gl)
			{
				int[] textureIds = new int[1];
				gl.glGenTextures(1, textureIds, 0);

				textureId = textureIds[0];
			}


		/**
		 * カメラをセットアップする
		 * カメラプレビューサイズ指定，SurfaceTextureにセットし，リスナをセット
		 */
		public void setUpCamera()
			{
				synchronized (this)
					{
						// 取得したTextureIDをもとに，SurfaceTextureを生成
						mSurfaceTexture = new SurfaceTexture(textureId);

						// SurfaceTextureに新規フレームが来た際に呼ばれるリスナを登録する
						mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener()
						{
							@Override
							public void onFrameAvailable(SurfaceTexture surfaceTexture)
								{
									// 新規フレームが来た際に呼ばれる
									if (surfaceTexture == null)
										{
											return;
										}
									else if (mSwitch.getDrawstate() == DRAWSTATE.PREPARATION)
										{
											// DRAWSTATEをReadyにする
											mSwitch.switchDrawState();
										}
								}
						});

                        cameraRelease();

						//region カメラの設定
						mCamera = Camera.open();

						Camera.Parameters parameters = mCamera.getParameters();
						List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
						Camera.Size size = previewSizes.get(0);
						parameters.setPreviewSize(size.width, size.height);
						mCamera.setParameters(parameters);

						try
							{
								mCamera.setPreviewTexture(mSurfaceTexture);
							}
						catch (IOException e)
							{
								e.printStackTrace();
							}

						mCamera.startPreview();
						//endregion
					}
			}

		/**
		 * カメラプレビュー描画処理
		 *
		 * @param gl
		 */
		public void draw(GL10 gl)
			{
				// SurfaceTextureはGL_DRAW_2Dではなく，GL_TEXTURE_EXTERNAL_OESを利用する
				gl.glEnable(GL_TEXTURE_EXTERNAL_OES);
				gl.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId);

				synchronized (this)
					{
						if (mSurfaceTexture != null)
							{
								// Image Create Success!!
								mSurfaceTexture.updateTexImage();
							}
					}

				gl.glMatrixMode(GL10.GL_PROJECTION);
				gl.glLoadIdentity();
				gl.glMatrixMode(GL10.GL_MODELVIEW);
				gl.glLoadIdentity();

				gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
				gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);

				gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
				gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexCoordsBuffer);

				gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

				gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
				gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
				gl.glDisable(GL_TEXTURE_EXTERNAL_OES);
				gl.glFlush();
			}

		/**
		 * カメラインスタンスを返す
		 *
		 * @return カメラインスタンス
		 */
		public static Camera getCamera()
			{
				return mCamera;
			}

		/**
		 * カメラを解放する
		 */
        public static void cameraRelease(){
            if(mCamera != null){
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        }

		/**
		 * カメラのズーム処理を行う
		 *
		 * @param zoom ZOOM型の値．ZOOMUPなのか，ZOOMDOWNなのか
		 */
		public void zoom(ZOOM zoom)
			{
				if (mCamera == null)
					{
						return;
					}
				Camera.Parameters parameters = mCamera.getParameters();
				nowZoom = parameters.getZoom();

				if (zoom == ZOOM.ZOOMUP)
					{
						if (nowZoom < parameters.getMaxZoom())
							{
								parameters.setZoom(nowZoom + 1);
							}
						mCamera.setParameters(parameters);
					}
				else if (zoom == ZOOM.ZOOMDOWN)
					{
						if (nowZoom > 0)
							{
								parameters.setZoom(nowZoom - 1);
							}
						mCamera.setParameters(parameters);
					}
			}
	}
