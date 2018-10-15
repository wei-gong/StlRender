package com.gongw.stlrender.stl;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class STLView extends GLSurfaceView {

	private STLRenderer stlRenderer;
	//这里将偏移数值降低
	private final float TOUCH_SCALE_FACTOR = 180.0f / 320/2;
	private float previousX;
	private float previousY;
	private void changeDistance(float scale) {
		if(stlRenderer.measurePrintSize){
			stlRenderer.scale_object = scale;
			if(listener!=null){
				listener.onStlSizeChanged(getStlObjectSize());
			}
		}else{
			stlRenderer.scale = scale;
		}
	}
	// zoom rate (larger > 1.0f > smaller)
	private float pinchScale = 1.0f;
	private PointF pinchStartPoint = new PointF();
	private float pinchStartZ = 0.0f;
	private float pinchStartDistance = 0.0f;
	private float pinchMoveX = 0.0f;
	private float pinchMoveY = 0.0f;
	// for touch event handling
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

					if(stlRenderer.action == 0){
						stlRenderer.positionX += dx * TOUCH_SCALE_FACTOR /2;
						stlRenderer.positionY += -dy * TOUCH_SCALE_FACTOR /2;
					}else if(stlRenderer.action == 1){
						stlRenderer.positionZ += -dy * TOUCH_SCALE_FACTOR /2;
					}else if(stlRenderer.action == 2){
						stlRenderer.rotateZ += dx * TOUCH_SCALE_FACTOR;
					}else{
						stlRenderer.angleX +=  dy * TOUCH_SCALE_FACTOR;
//						stlRenderer.angleY +=  dx * TOUCH_SCALE_FACTOR;
						stlRenderer.angleZ +=  dx * TOUCH_SCALE_FACTOR;
					}

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
					if(stlRenderer.action == 0){
						stlRenderer.positionX += dx * TOUCH_SCALE_FACTOR /2;
						stlRenderer.positionY += -dy * TOUCH_SCALE_FACTOR /2;
					}else if(stlRenderer.action == 1){
						stlRenderer.positionZ += -dy * TOUCH_SCALE_FACTOR /2;
					}else if(stlRenderer.action == 2){
						stlRenderer.rotateZ += dx * TOUCH_SCALE_FACTOR;
					}else{
						stlRenderer.angleX +=  dy * TOUCH_SCALE_FACTOR;
//						stlRenderer.angleY +=  dx * TOUCH_SCALE_FACTOR;
						stlRenderer.angleZ +=  dx * TOUCH_SCALE_FACTOR;
					}

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
		        // TODO Auto-generated catch block
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

	/**
	 * 更新object 刷新界面
	 * @param stlObject
	 */
	public void requestRedraw(STLObject stlObject){
		stlRenderer.requestRedraw(stlObject);
	}
	/**
	 * 刷新界面
	 */
	public void requestRedraw(){
		stlRenderer.requestRedraw();
	}
	
	public void delete (){
		stlRenderer.delete();
	}
	/**
	 * 平移开关
	 */
	public boolean setAction(int i){
		if (stlRenderer.action == i) {
			stlRenderer.action = STLRenderer.ACTION_NONE;
			return false;
		} else {
			stlRenderer.action = i;
			return true;
		}
	}
	/**
	 * 将模型置于中央底部位置
	 */
	public void putCenterBottom(){
		stlRenderer.putCenterBottom();
		requestRedraw();
	}

	/**
	 * 调节打印尺寸开关
	 */
	public boolean toogleMeasureSize(){
		return stlRenderer.measurePrintSize = !stlRenderer.measurePrintSize;
	}

	/**
	 * 设置渲染颜色
	 * @param color
	 */
	public void setColor(int color){
		stlRenderer.red = Color.red(color) / 255f;
		stlRenderer.green = Color.green(color) / 255f;
		stlRenderer.blue = Color.blue(color) / 255f;
		requestRedraw();
	}
	/**
	 * 判断当前stl是否超出打印边界
	 */
	public boolean isStlOverLine(){
		return stlRenderer.isOverLine;
	}
	/**
	 * 获取stl的设置参数
	 */
	public String getStlSetting(){
		return "scale="+stlRenderer.scale_object_rember+"&offset_x="+stlRenderer.positionX+"&offset_y="+stlRenderer.positionY+"&offset_z="+stlRenderer.positionZ+"&rotate="+stlRenderer.rotateZ;
	}

	public float[] getStlObjectSize(){
		if(stlRenderer.getStlObject()!=null){
			float x = stlRenderer.scale_object_rember * (stlRenderer.getStlObject().maxX - stlRenderer.getStlObject().minX);
			float y = stlRenderer.scale_object_rember * (stlRenderer.getStlObject().maxY - stlRenderer.getStlObject().minY);
			float z = stlRenderer.scale_object_rember * (stlRenderer.getStlObject().maxZ - stlRenderer.getStlObject().minZ);
			return new float[]{x, y, z};
		}
		return new float[]{0, 0, 0};
	}

	public interface OnStlSizeChangedListener{
		void onStlSizeChanged(float[] newSize);
	}

	OnStlSizeChangedListener listener;

	public void setOnStlSizeChangedListener(OnStlSizeChangedListener listener){
		this.listener = listener;
	}
}
