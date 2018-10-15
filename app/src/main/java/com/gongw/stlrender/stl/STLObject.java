package com.gongw.stlrender.stl;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class STLObject {
	FloatBuffer normalBuffer;
	FloatBuffer vertexBuffer;
	int triangleCount;
	float maxX;
	float maxY;
	float maxZ;
	float minX;
	float minY;
	float minZ;

	public void draw(GL10 gl) {
		if (normalBuffer == null || vertexBuffer == null) {
			return;
		}
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		//gl.glFrontFace(GL10.GL_CCW);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glNormalPointer(GL10.GL_FLOAT,0, normalBuffer);
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, triangleCount*3);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
	}


}
