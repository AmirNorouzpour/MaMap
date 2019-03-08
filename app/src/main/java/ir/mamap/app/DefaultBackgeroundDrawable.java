package ir.mamap.app;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class DefaultBackgeroundDrawable extends Drawable {

    private Paint paint;
    private Paint circlePaint;
    private int backgroundColor = Color.parseColor("#2196f3");
    private int circleColor = Color.parseColor("#28FFFFFF");

    public DefaultBackgeroundDrawable() {
        super();
        paint = new Paint();
        paint.setColor(backgroundColor);
        circlePaint = new Paint();
        circlePaint.setColor(circleColor);
        circlePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void draw(Canvas canvas) {

        int x = getBounds().right;
        int y = getBounds().bottom;

        canvas.drawRect(getBounds(), paint);
        RectF rectF_left1 = new RectF(-2 * x, y / 4, x, y + 5 * y / 6);
        RectF rectF_right1 = new RectF(x / 4, y / 2, 5 * x / 2, 2 * y);
        canvas.drawArc(rectF_left1, 0, 360, false, circlePaint);
        canvas.drawArc(rectF_right1, 0, 360, false, circlePaint);
    }

    public void setBackgroundColor(int color) {
        backgroundColor = color;
        paint.setColor(color);
        invalidateSelf();
    }

    public void setCircleColor(int color) {
        circleColor = color;
        circlePaint.setColor(color);
        invalidateSelf();
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

    @Override
    public int getIntrinsicWidth() {
        return 0;
    }

    @Override
    public int getIntrinsicHeight() {
        return 0;
    }
}
