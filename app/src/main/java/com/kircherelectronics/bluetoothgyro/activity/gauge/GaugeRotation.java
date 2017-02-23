package com.kircherelectronics.bluetoothgyro.activity.gauge;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.PorterDuff.Mode;
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
 * Draws an analog gauge for displaying rotation measurements in three-space
 * from device sensors.
 * 
 * Note that after Android 4.0 TextureView exists, as does SurfaceView for
 * Android 3.0 which won't hog the UI thread like View will. This should only be
 * used with devices or certain libraries that require View.
 * 
 * @author Kaleb
 * @version %I%, %G%
 * @see http://developer.android.com/reference/android/view/View.html
 */
public final class GaugeRotation extends View
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

	private static final String tag = GaugeRotation.class.getSimpleName();

	// Keep track of the rotation of the device
	private float[] rotation = new float[3];

	// Keep static bitmaps of the gauge so we only have to redraw if we have to
	// Static bitmap for the bezel of the gauge
	private Bitmap bezelBitmap;
	// Static bitmap for the face of the gauge
	private Bitmap faceBitmap;

	private Paint backgroundPaint;
	private Paint rimOuterPaint;
	private Paint rimPaint;
	private Paint skyPaint;

	private RectF rimRect;
	private RectF rimOuterRect;
	private RectF rimTopRect;
	private RectF rimBottomRect;
	private RectF rimLeftRect;
	private RectF rimRightRect;
	private RectF skyBackgroundRect;

	/**
	 * Create a new instance.
	 * 
	 * @param context
	 */
	public GaugeRotation(Context context)
	{
		super(context);

		initDrawingTools();
	}

	/**
	 * Create a new instance.
	 * 
	 * @param context
	 * @param attrs
	 */
	public GaugeRotation(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		initDrawingTools();
	}

	/**
	 * Create a new instance.
	 * 
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public GaugeRotation(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);

		initDrawingTools();
	}

	/**
	 * Update the rotation of the device.
	 * 
	 * @param rotation
	 */
	public void updateRotation(float[] rotation)
	{
		System.arraycopy(rotation, 0, this.rotation, 0, rotation.length);

		this.invalidate();
	}

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

		float rimSize = 0.02f;

		skyBackgroundRect = new RectF();
		skyBackgroundRect.set(rimRect.left + rimSize, rimRect.top + rimSize,
				rimRect.right - rimSize, rimRect.bottom - rimSize);

		skyPaint = new Paint();
		skyPaint.setAntiAlias(true);
		skyPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		skyPaint.setColor(Color.WHITE);

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

	// in case there is no size specified
	private int getPreferredSize()
	{
		return 300;
	}

	/**
	 * Draw the gauge rim.
	 * 
	 * @param canvas
	 */
	private void drawRim(Canvas canvas)
	{
		// First draw the most back rim
		canvas.drawOval(rimOuterRect, rimOuterPaint);
		// Then draw the small black line
		canvas.drawOval(rimRect, rimPaint);

		canvas.drawRect(rimTopRect, rimOuterPaint);
		// bottom rect
		canvas.drawRect(rimBottomRect, rimOuterPaint);
		// left rect
		canvas.drawRect(rimLeftRect, rimOuterPaint);
		// right rect
		canvas.drawRect(rimRightRect, rimOuterPaint);
	}

	/**
	 * Draw the gauge face.
	 * 
	 * @param canvas
	 */
	private void drawFace(Canvas canvas)
	{
		// free the old bitmap
		if (faceBitmap != null)
		{
			faceBitmap.recycle();
		}

		faceBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
				Bitmap.Config.ARGB_8888);

		Canvas faceCanvas = new Canvas(faceBitmap);
		float scale = (float) getWidth();
		faceCanvas.scale(scale, scale);

		skyBackgroundRect.set(rimRect.left, rimRect.top, rimRect.right,
				rimRect.bottom);

		faceCanvas.drawArc(skyBackgroundRect, 0, 360, true, skyPaint);

		int[] allpixels = new int[faceBitmap.getHeight()
				* faceBitmap.getWidth()];

		faceBitmap.getPixels(allpixels, 0, faceBitmap.getWidth(), 0, 0,
				faceBitmap.getWidth(), faceBitmap.getHeight());

		for (int i = 0; i < faceBitmap.getHeight() * faceBitmap.getWidth(); i++)
		{
			allpixels[i] = Color.TRANSPARENT;
		}

		int height = (int) ((faceBitmap.getHeight() / 2) - ((faceBitmap
				.getHeight() / 2.5) * rotation[1]));

		if (height > faceBitmap.getHeight())
		{
			height = faceBitmap.getHeight();
		}
		
		// Check for 0 case
		if(height == 0)
		{
			height = 1;
		}

		faceBitmap.setPixels(allpixels, 0, faceBitmap.getWidth(), 0, 0,
				faceBitmap.getWidth(), height);

		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.rotate((float) Math.toDegrees(rotation[2]), faceBitmap.getWidth() / 2f,
				faceBitmap.getHeight() / 2f);

		canvas.drawBitmap(faceBitmap, 0, 0, backgroundPaint);
		canvas.restore();
	}

	/**
	 * Draw the gauge bezel.
	 * 
	 * @param canvas
	 */
	private void drawBezel(Canvas canvas)
	{
		if (bezelBitmap == null)
		{
			Log.w(tag, "Bezel not created");
		}
		else
		{
			canvas.drawBitmap(bezelBitmap, 0, 0, backgroundPaint);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		Log.d(tag, "Size changed to " + w + "x" + h);

		regenerateBezel();
	}

	/**
	 * Regenerate the background image. This should only be called when the size
	 * of the screen has changed. The background will be cached and can be
	 * reused without needing to redraw it.
	 */
	private void regenerateBezel()
	{
		// free the old bitmap
		if (bezelBitmap != null)
		{
			bezelBitmap.recycle();
		}

		bezelBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas bezelCanvas = new Canvas(bezelBitmap);
		float scale = (float) getWidth();
		bezelCanvas.scale(scale, scale);

		drawRim(bezelCanvas);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		drawBezel(canvas);
		drawFace(canvas);

		float scale = (float) getWidth();
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.scale(scale, scale);

		canvas.restore();
	}

}
