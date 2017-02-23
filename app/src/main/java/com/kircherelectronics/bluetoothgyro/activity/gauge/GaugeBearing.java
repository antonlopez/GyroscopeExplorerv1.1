package com.kircherelectronics.bluetoothgyro.activity.gauge;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff.Mode;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/*
 * Gyroscope Explorer
 * Copyright (C) 2013-2015, Kaleb Kircher - Kircher Engineering, LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Draws an analog gauge (a compass) for displaying bearing measurements from
 * device sensors.
 * 
 * Note that before TextureView in Android 4.0, SurfaceView existed as an
 * alternative to the UI hogging View class. We want to render outside of the UI
 * thread, which is what SurfaceView is for. However, SurfaceView has
 * significant drawbacks. TextView is essentially the same as the SurfaceView,
 * but it behaves as a normal view and supports normal view operations. TextView
 * requires hardware acceleration and, because it is more flexible than
 * SurfaceView, incurs a performance hit. You would not want to use it for
 * running a full-screen game.
 * 
 * @author Kaleb
 * @version %I%, %G%
 * @see http://developer.android.com/reference/android/view/SurfaceView.html
 */
public final class GaugeBearing extends View
{

	/*
	 * Developer Note: In the interest of keeping everything as fast as
	 * possible, only the measurements are redrawn, the gauge background and
	 * display information are drawn once per device orientation and then cached
	 * so they can be reused. All allocation and reclaiming of memory should
	 * occur before and after the handler is posted to the thread, but never
	 * while the thread is running. Allocation and reclamation of memory while
	 * the handler is posted to the thread will cause the GC to run, resulting
	 * in long delays (up to 600ms) while the GC cleans up memory. The frame
	 * rate to drop dramatically if the GC is running often, so try to keep it
	 * happy and out of the way.
	 * 
	 * Avoid iterators, Set or Map collections (use SparseArray), + to
	 * concatenate Strings (use StringBuffers) and above all else boxed
	 * primitives (Integer, Double, Float, etc).
	 */

	/*
	 * Developer Note: There are some things to keep in mind when it comes to
	 * Android and hardware acceleration. What we see in Android 4.0 is �full�
	 * hardware acceleration. All UI elements in windows, and third-party apps
	 * will have access to the GPU for rendering. Android 3.0 had the same
	 * system, but now developers will be able to specifically target Android
	 * 4.0 with hardware acceleration. Google is encouraging developers to
	 * update apps to be fully-compatible with this system by adding the
	 * hardware acceleration tag in an app�s manifest. Android has always used
	 * some hardware accelerated drawing.
	 * 
	 * Since before 1.0 all window compositing to the display has been done with
	 * hardware. "Full" hardware accelerated drawing within a window was added
	 * in Android 3.0. The implementation in Android 4.0 is not any more full
	 * than in 3.0. Starting with 3.0, if you set the flag in your app saying
	 * that hardware accelerated drawing is allowed, then all drawing to the
	 * application�s windows will be done with the GPU. The main change in this
	 * regard in Android 4.0 is that now apps that are explicitly targeting 4.0
	 * or higher will have acceleration enabled by default rather than having to
	 * put android:handwareAccelerated="true" in their manifest. (And the reason
	 * this isn�t just turned on for all existing applications is that some
	 * types of drawing operations can�t be supported well in hardware and it
	 * also impacts the behavior when an application asks to have a part of its
	 * UI updated. Forcing hardware accelerated drawing upon existing apps will
	 * break a significant number of them, from subtly to significantly.)
	 */

	private static final String tag = GaugeBearing.class.getSimpleName();

	private static final int DEGREE_CENTER = 0;
	private static final int DEGREE_MIN = 0;
	private static final int DEGREE_MAX = 360;

	private boolean handInitialized = false;

	private float handPosition = DEGREE_CENTER;
	private float handTarget = DEGREE_CENTER;
	private float handVelocity = 0.0f;
	private float handAcceleration = 0.0f;

	private long lastHandMoveTime = -1L;

	// Static bitmaps
	private Bitmap background;
	private Bitmap hand;

	private Canvas handCanvas;

	private Paint backgroundPaint;
	private Paint facePaint;
	private Paint handPaint;
	private Paint rimPaint;
	private Paint rimOuterPaint;

	private Path handPath;

	private RectF faceRect;
	private RectF rimRect;
	private RectF rimOuterRect;
	private RectF rimTopRect;
	private RectF rimBottomRect;
	private RectF rimLeftRect;
	private RectF rimRightRect;

	/**
	 * Create a new instance.
	 * 
	 * @param context
	 */
	public GaugeBearing(Context context)
	{
		super(context);
		init();
	}

	/**
	 * Create a new instance.
	 * 
	 * @param context
	 * @param attrs
	 */
	public GaugeBearing(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	/**
	 * Create a new instance.
	 * 
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public GaugeBearing(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init();
	}

	/**
	 * Update the bearing of the device.
	 * 
	 * @param azimuth
	 */
	public void updateBearing(float azimuth)
	{
		// Adjust the range: 0 < range <= 360 (from: -180 < range <=
		// 180)
		azimuth = (float) (Math.toDegrees(azimuth) + 360) % 360;

		setHandTarget(azimuth);
	}

	/**
	 * Run the instance. This can be thought of as onDraw().
	 */
	protected void onDraw(Canvas canvas)
	{
		drawBackground(canvas);

		drawHand(canvas);

		canvas.restore();

		moveHand();
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state)
	{
		Bundle bundle = (Bundle) state;
		Parcelable superState = bundle.getParcelable("superState");
		super.onRestoreInstanceState(superState);

		handInitialized = bundle.getBoolean("handInitialized");
		handPosition = bundle.getFloat("handPosition");
		handTarget = bundle.getFloat("handTarget");
		handVelocity = bundle.getFloat("handVelocity");
		handAcceleration = bundle.getFloat("handAcceleration");
		lastHandMoveTime = bundle.getLong("lastHandMoveTime");
	}

	@Override
	protected Parcelable onSaveInstanceState()
	{
		Parcelable superState = super.onSaveInstanceState();

		Bundle state = new Bundle();
		state.putParcelable("superState", superState);
		state.putBoolean("handInitialized", handInitialized);
		state.putFloat("handPosition", handPosition);
		state.putFloat("handTarget", handTarget);
		state.putFloat("handVelocity", handVelocity);
		state.putFloat("handAcceleration", handAcceleration);
		state.putLong("lastHandMoveTime", lastHandMoveTime);
		return state;
	}

	/**
	 * Initialize the instance.
	 */
	private void init()
	{
		initDrawingTools();
	}

	/**
	 * Initialize the drawing tools.
	 */
	private void initDrawingTools()
	{

		// Rectangle for the rim of the gauge bezel
		rimRect = new RectF(0.12f, 0.12f, 0.88f, 0.88f);

		// Paint for the rim of the gauge bezel
		rimPaint = new Paint();
		rimPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		// The linear gradient is a bit skewed for realism
		rimPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));

		float rimOuterSize = -0.04f;
		rimOuterRect = new RectF();
		rimOuterRect.set(rimRect.left + rimOuterSize, rimRect.top
				+ rimOuterSize, rimRect.right - rimOuterSize, rimRect.bottom
				- rimOuterSize);

		rimTopRect = new RectF(0.5f, 0.106f, 0.5f, 0.06f);
		rimTopRect.set(rimTopRect.left + rimOuterSize, rimTopRect.top
				+ rimOuterSize, rimTopRect.right - rimOuterSize,
				rimTopRect.bottom - rimOuterSize);

		rimBottomRect = new RectF(0.5f, 0.94f, 0.5f, 0.894f);
		rimBottomRect.set(rimBottomRect.left + rimOuterSize, rimBottomRect.top
				+ rimOuterSize, rimBottomRect.right - rimOuterSize,
				rimBottomRect.bottom - rimOuterSize);

		rimLeftRect = new RectF(0.106f, 0.5f, 0.06f, 0.5f);
		rimLeftRect.set(rimLeftRect.left + rimOuterSize, rimLeftRect.top
				+ rimOuterSize, rimLeftRect.right - rimOuterSize,
				rimLeftRect.bottom - rimOuterSize);

		rimRightRect = new RectF(0.94f, 0.5f, 0.894f, 0.5f);
		rimRightRect.set(rimRightRect.left + rimOuterSize, rimRightRect.top
				+ rimOuterSize, rimRightRect.right - rimOuterSize,
				rimRightRect.bottom - rimOuterSize);

		rimOuterPaint = new Paint();
		rimOuterPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		rimOuterPaint.setColor(Color.rgb(255, 255, 255));

		float rimSize = 0.03f;
		faceRect = new RectF();
		faceRect.set(rimRect.left + rimSize, rimRect.top + rimSize,
				rimRect.right - rimSize, rimRect.bottom - rimSize);

		facePaint = new Paint();
		facePaint.setStyle(Paint.Style.FILL);
		facePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		facePaint.setAntiAlias(true);
		facePaint.setColor(Color.TRANSPARENT);

		handPaint = new Paint();
		handPaint.setAntiAlias(true);
		handPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		handPaint.setColor(Color.WHITE);
		handPaint.setStyle(Paint.Style.FILL);

		handPath = new Path();
		handPath.moveTo(0.5f, 0.5f + 0.32f);
		handPath.lineTo(0.5f - 0.02f, 0.5f + 0.32f - 0.32f);

		handPath.lineTo(0.5f, 0.5f - 0.32f);
		handPath.lineTo(0.5f + 0.02f, 0.5f + 0.32f - 0.32f);
		handPath.lineTo(0.5f, 0.5f + 0.32f);
		handPath.addCircle(0.5f, 0.5f, 0.025f, Path.Direction.CW);

		backgroundPaint = new Paint();
		backgroundPaint.setFilterBitmap(true);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);

		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int chosenWidth = chooseDimension(widthMode, widthSize);
		int chosenHeight = chooseDimension(heightMode, heightSize);

		int chosenDimension = Math.min(chosenWidth, chosenHeight);

		setMeasuredDimension(chosenDimension, chosenDimension);
	}

	/**
	 * Chose the dimension of the view.
	 * 
	 * @param mode
	 * @param size
	 * @return
	 */
	private int chooseDimension(int mode, int size)
	{
		if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY)
		{
			return size;
		}
		else
		{ // (mode == MeasureSpec.UNSPECIFIED)
			return getPreferredSize();
		}
	}

	/**
	 * In case there is no size specified
	 * 
	 * @return
	 */
	private int getPreferredSize()
	{
		return 300;
	}

	/**
	 * Draw the rim of the gauge.
	 * 
	 * @param canvas
	 */
	private void drawRim(Canvas canvas)
	{
		// First draw the most back rim
		canvas.drawOval(rimOuterRect, rimOuterPaint);
		// Then draw the small black line
		canvas.drawOval(rimRect, rimPaint);

		// top rect
		canvas.drawRect(rimTopRect, rimOuterPaint);
		// bottom rect
		canvas.drawRect(rimBottomRect, rimOuterPaint);
		// left rect
		canvas.drawRect(rimLeftRect, rimOuterPaint);
		// right rect
		canvas.drawRect(rimRightRect, rimOuterPaint);
	}

	/**
	 * Draw the face of the gauge.
	 * 
	 * @param canvas
	 */
	private void drawFace(Canvas canvas)
	{
		canvas.drawOval(faceRect, facePaint);
	}

	/**
	 * Convert degrees to an angle.
	 * 
	 * @param degree
	 * @return
	 */
	private float degreeToAngle(float degree)
	{
		return degree;
	}

	/**
	 * Draw the gauge hand.
	 * 
	 * @param canvas
	 */
	/**
	 * Draw the gauge hand.
	 * 
	 * @param canvas
	 */
	private void drawHand(Canvas canvas)
	{
		// *Bug Notice* We draw the hand with a bitmap and a new canvas because
		// canvas.drawPath() doesn't work. This seems to be related to devices
		// with hardware acceleration enabled.

		// free the old bitmap
		if (hand != null)
		{
			hand.recycle();
		}

		hand = Bitmap.createBitmap(getWidth(), getHeight(),
				Bitmap.Config.ARGB_8888);
		handCanvas = new Canvas(hand);
		float scale = (float) getWidth();
		handCanvas.scale(scale, scale);

		if (handInitialized)
		{
			float handAngle = degreeToAngle(handPosition);
			handCanvas.save(Canvas.MATRIX_SAVE_FLAG);
			handCanvas.rotate(handAngle, 0.5f, 0.5f);
			handCanvas.drawPath(handPath, handPaint);
		}
		else
		{
			float handAngle = degreeToAngle(0);
			handCanvas.save(Canvas.MATRIX_SAVE_FLAG);
			handCanvas.rotate(handAngle, 0.5f, 0.5f);
			handCanvas.drawPath(handPath, handPaint);
		}

		canvas.drawBitmap(hand, 0, 0, backgroundPaint);
	}

	/**
	 * Draw the background of the gauge.
	 * 
	 * @param canvas
	 */
	private void drawBackground(Canvas canvas)
	{
		if (background == null)
		{
			Log.w(tag, "Background not created");
		}
		else
		{
			canvas.drawBitmap(background, 0, 0, backgroundPaint);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		Log.d(tag, "Size changed to " + w + "x" + h);

		regenerateBackground();
	}

	/**
	 * Regenerate the background image. This should only be called when the size
	 * of the screen has changed. The background will be cached and can be
	 * reused without needing to redraw it.
	 */
	private void regenerateBackground()
	{
		// free the old bitmap
		if (background != null)
		{
			background.recycle();
		}

		background = Bitmap.createBitmap(getWidth(), getHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas backgroundCanvas = new Canvas(background);
		float scale = (float) getWidth();
		backgroundCanvas.scale(scale, scale);

		drawRim(backgroundCanvas);
		drawFace(backgroundCanvas);
	}

	/**
	 * Move the hand.
	 */
	private void moveHand()
	{
		handPosition = handTarget;
	}

	/**
	 * Indicate where the hand should be moved to.
	 * 
	 * @param bearing
	 */
	private void setHandTarget(float bearing)
	{
		if (bearing < DEGREE_MIN)
		{
			bearing = DEGREE_MIN;
		}
		else if (bearing > DEGREE_MAX)
		{
			bearing = DEGREE_MAX;
		}

		handTarget = bearing;
		handInitialized = true;

		invalidate();
	}

}
