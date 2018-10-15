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
}
