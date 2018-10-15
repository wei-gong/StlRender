package com.gongw.stlrender.stl;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class STLObject {
	//三角面片法向量数据
	FloatBuffer normalBuffer;
	//三角面片法顶点数据
	FloatBuffer vertexBuffer;
	//三角面片数
	int triangleCount;
	float maxX;
	float maxY;
	float maxZ;
	float minX;
	float minY;
	float minZ;

	/**
	 * 使用OpenGL绘制STLObject
	 * @param gl
	 */
	public void draw(GL10 gl) {
		if (normalBuffer == null || vertexBuffer == null) {
			return;
		}
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glNormalPointer(GL10.GL_FLOAT,0, normalBuffer);
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, triangleCount*3);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
	}


}
