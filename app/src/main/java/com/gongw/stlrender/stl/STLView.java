package com.gongw.stlrender.stl;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 使用STLRender的GLSurfaceView
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
