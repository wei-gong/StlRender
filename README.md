# Android下实现STL模型3D渲染

## STL文件

STL文件是在计算机图形应用系统中，用于表示三角形网格的一种文件格式，是最多快速原型系统所应用的标准文件类型。

 STL文件有两种：一种是ASCII明码格式，另一种是二进制格式。 

- **ASCII格式**

ASCII码格式的STL文件逐行给出三角面片的几何信息，每一行以1个或2个关键字开头。

在STL文件中的三角面片的信息单元 facet 是一个带矢量方向的三角面片，STL三维模型就是由一系列这样的三角面片构成。

整个STL文件的首行给出了文件路径及文件名。

在一个 STL文件中，每一个facet由7 行数据组成，

facet normal 是三角面片指向实体外部的法矢量坐标，

outer loop 说明随后的3行数据分别是三角面片的3个顶点坐标，3顶点沿指向实体外部的法矢量方向逆时针排列。

ASCII格式的STL 文件结构如下： 

```c++
//字符段意义
solidfilenamestl//文件路径及文件名
facetnormalxyz//三角面片法向量的3个分量值
outerloop
vertexxyz//三角面片第一个顶点坐标
vertexxyz//三角面片第二个顶点坐标
vertexxyz//三角面片第三个顶点坐标
endloop
endfacet//完成一个三角面片定义
 
......//其他facet
 
endsolidfilenamestl//整个STL文件定义结束
```



- **二进制格式**

二进制STL文件用固定的字节数来给出三角面片的几何信息。

文件起始的80个字节是文件头，用于存贮文件名；

紧接着用 4 个字节的整数来描述模型的三角面片个数，

后面逐个给出每个三角面片的几何信息。每个三角面片占用固定的50个字节，依次是:

3个4字节浮点数(角面片的法矢量)

3个4字节浮点数(1个顶点的坐标)

3个4字节浮点数(2个顶点的坐标)

3个4字节浮点数(3个顶点的坐标)个

三角面片的最后2个字节用来描述三角面片的属性信息。

一个完整二进制STL文件的大小为三角形面片数乘以 50再加上84个字节。

二进制格式的STL 文件结构如下： 

```c++
UINT8//Header//文件头
UINT32//Numberoftriangles//三角面片数量
//foreachtriangle（每个三角面片中）
REAL32[3]//Normalvector//法线矢量
REAL32[3]//Vertex1//顶点1坐标
REAL32[3]//Vertex2//顶点2坐标
REAL32[3]//Vertex3//顶点3坐标
UINT16//Attributebytecountend//文件属性统计
```



### Stl文件解析

- **定义STLObject对象**

```java
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
```



- **判断STL文件是否ASCII格式，如果不是则说明是二进制格式**

```java
/**
     * 解析ASCII格式的STL文件
     * @param stlFile
     * @return
     */
    private static boolean isTextFile(File stlFile){
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(stlFile));
            br.skip(80);
            int line = 0;
            String buffer;
            while((buffer = br.readLine()) != null && line < 5){
                line ++;
                if(buffer.contains("facet")||buffer.contains("outer")||buffer.contains("vertex")||buffer.contains("end")){
                    return true;
                }
            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(br != null){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
```



- **解析ASCII格式的STL文件**

```java
/**
     * 解析ASCII格式的STL文件
     * @param stlFile
     * @param handler
     * @param callback
     */
    private static void fetchTextFile(final File stlFile, final Handler handler, final StlFetchCallback callback){
        final STLObject stlObject = new STLObject();
        List<Float> normalList = new ArrayList<>();
        List<Float> vertexList = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(stlFile));
            String str;
            long size = stlFile.length();
            long current = 0;
            long lastTime = System.currentTimeMillis();
            while ((str = br.readLine()) != null){
                current += str.length();
                str = str.trim();
                //获取三角面片法向量数据
                if(str.startsWith("facet normal ")){
                    str = str.replaceFirst("(^facet normal)([ \\f\\r\\t\\n]+)", "");
                    for(int i=0;i<3;i++){
                        normalList.add(Float.parseFloat(str.substring(0, str.indexOf(" "))));
                        normalList.add(Float.parseFloat(str.substring(str.indexOf(" ")+1, str.lastIndexOf(" "))));
                        normalList.add(Float.parseFloat(str.substring(str.lastIndexOf(" ")+1)));
                    }
                    stlObject.triangleCount ++;
                }
                //获取三角面片顶点数据
                if(str.startsWith("vertex ")){
                    str = str.replaceFirst("(^vertex)([ \\f\\r\\t\\n]+)", "");
                    float x = Float.parseFloat(str.substring(0, str.indexOf(" ")));
                    float y = Float.parseFloat(str.substring(str.indexOf(" ")+1, str.lastIndexOf(" ")));
                    float z = Float.parseFloat(str.substring(str.lastIndexOf(" ")+1));
                    vertexList.add(x);
                    vertexList.add(y);
                    vertexList.add(z);
                    adjustMaxMin(stlObject, x, y, z);
                }

                if(callback != null){
                    final int progress = (int) ((1.0f * current/size) * 100);
                    long time = System.currentTimeMillis();
                    //每1秒回调一次onProgress方法
                    if(time - lastTime > 1000){
                        lastTime = time;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onProgress(progress);
                            }
                        });
                    }
                }
            }

            ByteBuffer normalBuffer = ByteBuffer.allocateDirect(normalList.size() * 4);
            normalBuffer.order(ByteOrder.nativeOrder());
            stlObject.normalBuffer = normalBuffer.asFloatBuffer();
            int i = 0;
            for(float f : normalList){
                stlObject.normalBuffer.position(i++);
                stlObject.normalBuffer.put(f);
            }
            stlObject.normalBuffer.position(0);

            ByteBuffer vertexBuffer = ByteBuffer.allocateDirect(vertexList.size() * 4);
            vertexBuffer.order(ByteOrder.nativeOrder());
            stlObject.vertexBuffer = vertexBuffer.asFloatBuffer();
            i = 0;
            for(float f : vertexList){
                stlObject.vertexBuffer.position(i++);
                stlObject.vertexBuffer.put(f);
            }
            stlObject.vertexBuffer.position(0);

            if(callback != null){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFinish(stlObject);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
            if(callback != null){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError();
                    }
                });
            }
        }finally {
            if(br != null){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
```



- **解析二进制格式的STL文件**

```java
/**
     * 解析二进制格式的STL文件
     * @param stlFile
     * @param handler
     * @param callback
     */
    private static void fetchBinaryFile(File stlFile, Handler handler, final StlFetchCallback callback){
        final STLObject stlObject = new STLObject();
        FileInputStream fis = null;
        float[] normalArray;
        float[] vertexArray;
        try {
            fis = new FileInputStream(stlFile);
            fis.skip(80);
            byte[] vertex_size = new byte[4];
            fis.read(vertex_size);
            //大小端转换，java平台使用大端模式，OpenGL使用小端模式
            stlObject.triangleCount = getIntByLittleEndian(vertex_size, 0);
            normalArray = new float[stlObject.triangleCount * 3 * 3];
            vertexArray = new float[stlObject.triangleCount * 3 * 3];
            byte[] facet = new byte[50];
            int num = 0;
            long lastTime = System.currentTimeMillis();
            while(fis.read(facet) != -1){
                //获取三角面片法向量数据
                for(int i=0;i<3;i++){
                    normalArray[num ++] = Float.intBitsToFloat(getIntByLittleEndian(facet, 0));
                    normalArray[num ++] = Float.intBitsToFloat(getIntByLittleEndian(facet, 4));
                    normalArray[num ++] = Float.intBitsToFloat(getIntByLittleEndian(facet, 8));
                }

                //获取三角面片顶点数据
                for(int i=0;i<3;i++){
                    float x = Float.intBitsToFloat(getIntByLittleEndian(facet, (i*12)+12));
                    float y = Float.intBitsToFloat(getIntByLittleEndian(facet, (i*12)+16));
                    float z = Float.intBitsToFloat(getIntByLittleEndian(facet, (i*12)+20));
                    vertexArray[(num-9) + (i*3)] = x;
                    vertexArray[(num-9)  + (i*3) + 1] = y;
                    vertexArray[(num-9)  + (i*3) + 2] = z;
                    adjustMaxMin(stlObject, x, y, z);
                }
                if(callback != null){
                    final int progress = (int) (1.0f * num/(stlObject.triangleCount*3*3) * 100);
                    long time = System.currentTimeMillis();
                    //每1秒回调一次onProgress方法
                    if(time - lastTime > 1000){
                        lastTime = time;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onProgress(progress);
                            }
                        });
                    }
                }
            }
            ByteBuffer normal = ByteBuffer.allocateDirect(normalArray.length * 4);
            normal.order(ByteOrder.nativeOrder());
            stlObject.normalBuffer = normal.asFloatBuffer();
            stlObject.normalBuffer.put(normalArray);
            stlObject.normalBuffer.position(0);

            ByteBuffer vertex = ByteBuffer.allocateDirect(vertexArray.length * 4);
            vertex.order(ByteOrder.nativeOrder());
            stlObject.vertexBuffer = vertex.asFloatBuffer();
            stlObject.vertexBuffer.put(vertexArray);
            stlObject.vertexBuffer.position(0);

            if(callback != null){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFinish(stlObject);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
            if(callback != null){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError();
                    }
                });
            }
        }finally {
            if(fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
```



- **大小端模式处理**

```java
    /**
     * 大端转小端
     * @param bytes
     * @param offset
     * @return
     */
    private static int getIntByLittleEndian(byte[] bytes, int offset){
        return (0xff & bytes[offset]) | ((0xff & bytes[offset + 1]) << 8) | ((0xff & bytes[offset + 2]) << 16) | ((0xff & bytes[offset + 3]) << 24);
    }
```



### OpenGL 3D渲染

- **编写STLObject Render**

```java
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
		// 画Stl模型
		drawSTLObject(stlObject, gl);
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

	/**
	 * 绘制STLObject
	 * @param stlObject
	 * @param gl
	 */
	public void drawSTLObject(STLObject stlObject, GL10 gl) {
		if (stlObject.normalBuffer == null || stlObject.vertexBuffer == null) {
			return;
		}
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, stlObject.vertexBuffer);
		gl.glNormalPointer(GL10.GL_FLOAT,0, stlObject.normalBuffer);
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, stlObject.triangleCount*3);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
	}

}
```



- **编写使用STLRender的GLSurfaceView**


```java
/**
 * 使用STLRender的GLSurfaceView，用于3D展示STLObject
 * Created by gw on 2017/7/11.
 */
public class STLView extends GLSurfaceView {

	private STLRenderer stlRenderer;
	//这里将偏移数值降低
	private final float TOUCH_SCALE_FACTOR = 180.0f / 320/2;
	private float previousX;
	private float previousY;
	private void changeDistance(float scale) {
		stlRenderer.scale = scale;
	}
	// 缩放比例
	private float pinchScale = 1.0f;
	private PointF pinchStartPoint = new PointF();
	private float pinchStartZ = 0.0f;
	private float pinchStartDistance = 0.0f;
	private float pinchMoveX = 0.0f;
	private float pinchMoveY = 0.0f;
	private static final int TOUCH_NONE = 0;
	private static final int TOUCH_DRAG = 1;
	private static final int TOUCH_ZOOM = 2;
	private int touchMode = TOUCH_NONE;

	public STLView(Context context, AttributeSet attrs) {
		super(context, attrs);
		stlRenderer = new STLRenderer();
		setRenderer(stlRenderer);
	}

	public STLRenderer getStlRenderer(){
		return stlRenderer;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			// starts pinch
			case MotionEvent.ACTION_POINTER_DOWN:
				if (event.getPointerCount() >= 2) {
					pinchStartDistance = getPinchDistance(event);
					//pinchStartZ = pinchStartDistance;
					if (pinchStartDistance > 50f) {
						getPinchCenterPoint(event, pinchStartPoint);
						previousX = pinchStartPoint.x;
						previousY = pinchStartPoint.y;
						touchMode = TOUCH_ZOOM;
					}
				}
				break;

			case MotionEvent.ACTION_MOVE:
				if (touchMode == TOUCH_ZOOM && pinchStartDistance > 0) {
					// on pinch
					PointF pt = new PointF();

					getPinchCenterPoint(event, pt);
					pinchMoveX = pt.x - previousX;
					pinchMoveY = pt.y - previousY;
					float dx = pinchMoveX;
					float dy = pinchMoveY;
					previousX = pt.x;
					previousY = pt.y;

					stlRenderer.angleX +=  dy * TOUCH_SCALE_FACTOR;
//					stlRenderer.angleY +=  dx * TOUCH_SCALE_FACTOR;
					stlRenderer.angleZ +=  dx * TOUCH_SCALE_FACTOR;

					pinchScale = getPinchDistance(event) / pinchStartDistance;
					changeDistance(pinchScale);
					stlRenderer.requestRedraw();
					invalidate();
				}
				break;

			// end pinch
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				pinchScale=0;
				pinchStartZ=0;
				if (touchMode == TOUCH_ZOOM) {
					touchMode = TOUCH_NONE;

					pinchMoveX = 0.0f;
					pinchMoveY = 0.0f;
					pinchScale = 1.0f;
					pinchStartPoint.x = 0.0f;
					pinchStartPoint.y = 0.0f;
					invalidate();
				}
				break;
		}

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			// start drag
			case MotionEvent.ACTION_DOWN:
				if (touchMode == TOUCH_NONE && event.getPointerCount() == 1) {
					touchMode = TOUCH_DRAG;
					previousX = event.getX();
					previousY = event.getY();
				}
				break;

			case MotionEvent.ACTION_MOVE:
				if (touchMode == TOUCH_DRAG) {
					float x = event.getX();
					float y = event.getY();

					float dx = x - previousX;
					float dy = y - previousY;
					previousX = x;
					previousY = y;

					// change view point
					stlRenderer.angleX +=  dy * TOUCH_SCALE_FACTOR;
//					stlRenderer.angleY +=  dx * TOUCH_SCALE_FACTOR;
					stlRenderer.angleZ +=  dx * TOUCH_SCALE_FACTOR;

					stlRenderer.requestRedraw();
					requestRender();
				}
				break;

			// end drag
			case MotionEvent.ACTION_UP:
				if (touchMode == TOUCH_DRAG) {
					touchMode = TOUCH_NONE;
					break;
				}
				stlRenderer.setsclae();
		}

		return true;
	}

	/**
	 *
	 * @param event
	 * @return pinched distance
	 */
	private float getPinchDistance(MotionEvent event) {
		float x=0;
		float y=0;
		  try {
			  x = event.getX(0) - event.getX(1);
			  y = event.getY(0) - event.getY(1);
		    } catch (IllegalArgumentException e) {
		        e.printStackTrace();
		    }
		return (float) Math.sqrt(x * x + y * y);
	}

	/**
	 *
	 * @param event
	 * @param pt pinched point
	 */
	private void getPinchCenterPoint(MotionEvent event, PointF pt) {
		pt.x = (event.getX(0) + event.getX(1)) * 0.5f;
		pt.y = (event.getY(0) + event.getY(1)) * 0.5f;
	}

}
```



## 渲染效果

源码下载地址：https://github.com/wei-gong/StlRender

![](images/device-2018-10-15-210743.png)