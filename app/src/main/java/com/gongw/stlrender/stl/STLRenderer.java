package com.gongw.stlrender.stl;

import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * STLObject渲染器
 * Created by gw on 2017/7/11.
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
	private static int bufferCounter = FRAME_BUFFER_COUNT;
	private STLObject stlObject;
	private float shotHeight = 260f;

	public STLObject getStlObject(){
		return stlObject;
	}

	/**
	 * 简单重绘（适用于旋转等）
	 */
	public void requestRedraw() {
		bufferCounter = FRAME_BUFFER_COUNT;
	}

	/**
	 * 停止渲染
	 */
	public void cancelRedraw(){
		bufferCounter = 0;
	}

	/**
	 * 更换STLObject并重新渲染
	 * @param stlObject
	 */
	public void requestRedraw(STLObject stlObject) {
		this.stlObject = stlObject;
		setPreviewParamters();
		bufferCounter = FRAME_BUFFER_COUNT;
	}

	/**
	 * 创建时调用
	 * @param gl
	 * @param config
	 */
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		//用指定颜色清空颜色缓存
		gl.glClearColor(0.93f, 0.93f, 0.93f, 1.0f);
		//启动色彩混合
		gl.glEnable(GL10.GL_BLEND);
		//设置源因子和目标因子（源颜色乘以的系数称为“源因子”，目标颜色乘以的系数称为“目标因子”）
		gl.glBlendFunc(GL10.GL_SRC_ALPHA,GL10.GL_ONE_MINUS_SRC_ALPHA);
		//开启更新深度缓冲区
		gl.glEnable(GL10.GL_DEPTH_TEST);
		//指定深度缓冲比较值，GL10.GL_LEQUAL：输入的深度值小于或等于参考值，则通过
		gl.glDepthFunc(GL10.GL_LEQUAL);
		gl.glHint(3152, 4354);
		//法线在转换后被标准化
		gl.glEnable(GL10.GL_NORMALIZE);
		//设置两点间其他点颜色的过渡模式
		gl.glShadeModel(GL10.GL_SMOOTH);

		//开始对投影矩阵操作
		gl.glMatrixMode(GL10.GL_PROJECTION);
		// 打开光源
		gl.glEnable(GL10.GL_LIGHTING);
		// 设置全局环境光
		gl.glLightModelfv(GL10.GL_LIGHT_MODEL_AMBIENT, getFloatBufferFromArray(new float[]{0.5f, 0.5f, 0.5f, 1.0f}));
		//使用GL_LIGHT0光源
		gl.glEnable(GL10.GL_LIGHT0);
		//设置材质的环境颜色和散射颜色
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT_AND_DIFFUSE, new float[]{0.3f, 0.3f, 0.3f, 1.0f}, 0);
		//设置光源位置
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, new float[]{0f, 0f, 1000f, 1.0f}, 0);

	}

	/**
	 * 尺寸发生变化时调用
	 * @param gl
	 * @param width
	 * @param height
	 */
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		float aspectRatio = (float) width / height;
		//设置视口矩形的位置，宽度和高度
		gl.glViewport(0, 0, width, height);
		//重置当前矩阵
		gl.glLoadIdentity();
		//清空颜色缓存
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		//指定观察的视景体在世界坐标系中的具体大小
		GLU.gluPerspective(gl, 45f, aspectRatio, 1f, 5000f);
		//开始对模型视景的操作
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		//定义视点矩阵（视点位置和参考点位置）
		GLU.gluLookAt(gl, 0, 0, shotHeight, 0, 0, 0, 0, 0, 1f);
	}

	/**
	 * 绘制每一帧的时候调用
	 * @param gl
	 */
	@Override
	public void onDrawFrame(GL10 gl) {
		if (bufferCounter < 1) {
			return;
		}
		bufferCounter--;
		gl.glLoadIdentity();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		// 设置旋转和偏移
		gl.glTranslatef(0, translation_y, 0);
		gl.glTranslatef(0, 0, translation_z);
		gl.glRotatef(angleX, 1, 0, 0);
		gl.glRotatef(angleY, 0, 1, 0);
		gl.glRotatef(angleZ, 0, 0, 1);
		//设置缩放
		scale_rember= scale_now * scale;
		gl.glScalef(scale_rember, scale_rember, scale_rember);

		//使能顶点数组功能
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		//开始对模型视景的操作
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		//允许写入深度缓冲区
		gl.glDepthMask(true);
		gl.glTranslatef(positionX, positionY, positionZ);
		gl.glRotatef(rotateZ, 0, 0, 1);
		scale_object_rember = scale_object_now * scale_object;
		gl.glScalef(scale_object_rember, scale_object_rember, scale_object_rember);
		//使用颜色材质
		gl.glEnable(GL10.GL_COLOR_MATERIAL);
		//设置材质环境颜色
		gl.glMaterialfv(GL10.GL_FRONT, GL10.GL_AMBIENT, new float[]{0.75f, 0.75f, 0.75f, 1f}, 0);
		//设置材质散射颜色
		gl.glMaterialfv(GL10.GL_FRONT, GL10.GL_DIFFUSE, new float[]{0.75f, 0.75f, 0.75f, 1f}, 0);
		//保存当前状态
		gl.glPushMatrix();
		gl.glColor4f(red, green, blue, alpha);
		if(stlObject!=null) {
			// 画Stl模型
			stlObject.draw(gl);
		}
		//恢复之前保存的状态
		gl.glPopMatrix();
		//禁用颜色材质
		gl.glDisable(GL10.GL_COLOR_MATERIAL);
	}

	/**
	 * 调整预览设置,使模型展示时大小位置适中
	 */
	private void setPreviewParamters (){
		float distance_y = stlObject.maxY - stlObject.minY;
		float distance_z = stlObject.maxZ - stlObject.minZ;

		translation_z = distance_z * -3f;
		translation_y = distance_y / -5f;
		angleX = -45f;

		//将模型置于中央位置
		positionX = -(stlObject.maxX + stlObject.minX)/2 * scale_object_rember;
		positionY = -(stlObject.maxY + stlObject.minY)/2 * scale_object_rember;
		positionZ = -stlObject.minZ * scale_object_rember;
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

	private FloatBuffer getFloatBufferFromArray(float[] vertexArray) {
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertexArray.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		FloatBuffer triangleBuffer = vbb.asFloatBuffer();
		triangleBuffer.put(vertexArray);
		triangleBuffer.position(0);
		return triangleBuffer;
	}

}
