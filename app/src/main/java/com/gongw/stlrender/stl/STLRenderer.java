package com.gongw.stlrender.stl;

import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import static javax.microedition.khronos.opengles.GL10.GL_DEPTH_TEST;

/**
 * 自定义渲染器
 */
public class STLRenderer implements Renderer {
	public static final int FRAME_BUFFER_COUNT = 10;
	public float angleX = 0f;
	public float angleY = 0f;
	public float angleZ = 0f;
	public float rotateZ = 0f;
	public float positionX = 0f;
	public float positionY = 0f;
	public float positionZ = 0f;
	//外部控制
	public float scale = 1.0f;
	public float scale_object = 1.0f;
	//当前展示
	private float scale_rember=1.0f;
	public float scale_object_rember = 1.0f;
	//当前固定
	private float scale_now=1.0f;
	private float scale_object_now=1.0f;
	public float translation_z;
	public float translation_y;
	public float red = 0.027f;
	public float green = 0.38f;
	public float blue = 0.79f;
	public float alpha = 1f;
	public boolean measurePrintSize =false;
	public int action = ACTION_NONE;
	private static int bufferCounter = FRAME_BUFFER_COUNT;
	private STLObject stlObject;
	public boolean isOverLine = false;
	public static final int ACTION_NONE = -1;
	public static final int ACTION_HORIZONTAL = 0;
	public static final int ACTION_VERTICAL = 1;
	public static final int ACTION_ROTATE = 2;

	private float[] borderColor = new float[]{0.62f, 0.62f, 0.62f, 0.3f};

	public STLObject getStlObject(){
		return stlObject;
	}

	/**
	 * 简单重绘（适用于旋转等）
	 */
	public void requestRedraw() {
		bufferCounter = FRAME_BUFFER_COUNT;
	}

	public void cancelRedraw(){
		bufferCounter = 0;
	}

	/**
	 * 复杂重绘 （适用于更换文件）
	 * @param stlObject
	 */
	public void requestRedraw(STLObject stlObject) {
		this.stlObject = stlObject;
		setPreviewParamters();
		bufferCounter = FRAME_BUFFER_COUNT;
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		if (bufferCounter < 1) {
			return;
		}
		bufferCounter--;
		gl.glLoadIdentity();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		// rotation and offset
		gl.glTranslatef(0, translation_y, 0);
		gl.glTranslatef(0, 0, translation_z);
		gl.glRotatef(angleX, 1, 0, 0);
		gl.glRotatef(angleY, 0, 1, 0);
		gl.glRotatef(angleZ, 0, 0, 1);
		scale_rember = scale_now * scale;
		gl.glScalef(scale_rember, scale_rember, scale_rember);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glMatrixMode(GL10.GL_MODELVIEW);

		gl.glDepthMask(true);
		// draw object
		if (stlObject != null) {
			drawStlObject(gl);
		}
	}

	private FloatBuffer getFloatBufferFromArray(float[] vertexArray) {
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertexArray.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		FloatBuffer triangleBuffer = vbb.asFloatBuffer();
		triangleBuffer.put(vertexArray);
		triangleBuffer.position(0);
		return triangleBuffer;
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		float aspectRatio = (float) width / height;

		gl.glViewport(0, 0, width, height);

		gl.glLoadIdentity();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		GLU.gluPerspective(gl, 45f, aspectRatio, 1f, 5000f);// (stlObject.maxZ - stlObject.minZ) * 10f + 100f);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		GLU.gluLookAt(gl, 0, 0, (stlObject.maxZ - stlObject.minZ)*2, 0, 0, 0, 0, 0, 1f);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glClearColor(0.93f, 0.93f, 0.93f, 1.0f);


//		 gl.glEnable(GL10.GL_TEXTURE_2D);
//		gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);
		gl.glHint(3152, 4354);
		gl.glEnable(GL10.GL_NORMALIZE);
		gl.glShadeModel(GL10.GL_SMOOTH);

		gl.glMatrixMode(GL10.GL_PROJECTION);

		// Lighting
		gl.glEnable(GL10.GL_LIGHTING);
		gl.glLightModelfv(GL10.GL_LIGHT_MODEL_AMBIENT, getFloatBufferFromArray(new float[]{0.5f, 0.5f, 0.5f, 1.0f}));// 全局环境光
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT_AND_DIFFUSE, new float[]{0.3f, 0.3f, 0.3f, 1.0f}, 0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, new float[]{0f, 0f, 1000f, 1.0f}, 0);
		gl.glEnable(GL10.GL_LIGHT0);

	}

	/**
	 * 画stl模型
	 * @param gl
	 */
	private void drawStlObject(GL10 gl) {
		gl.glTranslatef(positionX, positionY, positionZ);
		gl.glRotatef(rotateZ, 0, 0, 1);
		scale_object_rember = scale_object_now * scale_object;
		gl.glScalef(scale_object_rember, scale_object_rember, scale_object_rember);

		gl.glMaterialfv(GL10.GL_FRONT, GL10.GL_AMBIENT, new float[]{0.75f, 0.75f, 0.75f, 1f}, 0);
		gl.glMaterialfv(GL10.GL_FRONT, GL10.GL_DIFFUSE, new float[]{0.75f, 0.75f, 0.75f, 1f}, 0);
		gl.glEnable(GL10.GL_COLOR_MATERIAL);
		gl.glPushMatrix();
		gl.glColor4f(red, green, blue, alpha);
		stlObject.draw(gl);
		gl.glPopMatrix();
		gl.glDisable(GL10.GL_COLOR_MATERIAL);
	}

	/**
	 * 调整预览设置,目的是为了模型展示大小位置适中
	 */
	private void setPreviewParamters (){
		float distance_x = stlObject.maxX - stlObject.minX;
		float distance_y = stlObject.maxY - stlObject.minY;
		float distance_z = stlObject.maxZ - stlObject.minZ;

		translation_z = distance_z * -3f;
		translation_y = distance_y / -5f;
		angleX = -45f;
	}
	public void delete(){
		stlObject=null;
	}
	/**
	 * 固定缩放比例
	 */
	public void setsclae(){
		scale_object_now = scale_object_rember;
		scale_object_rember = 1.0f;
		scale_object = 1.0f;

		scale_now=scale_rember;
		scale_rember=1.0f;
		scale=1.0f;
	}

	/**
	 * 将模型置于中央位置
	 */
	public void putCenterBottom(){
		positionX = -(stlObject.maxX + stlObject.minX)/2 * scale_object_rember;
		positionY = -(stlObject.maxY + stlObject.minY)/2 * scale_object_rember;
		positionZ = -stlObject.minZ * scale_object_rember;
	}

}
