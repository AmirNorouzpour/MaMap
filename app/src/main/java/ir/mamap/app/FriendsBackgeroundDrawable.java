package ir.mamap.app;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

public class FriendsBackgeroundDrawable extends Drawable {

    private Paint paint;
    private Paint circlePaint;
    private int backgroundColor = Color.parseColor("#fafafa");
    private int circleColor = Color.parseColor("#4abdfe");

    public FriendsBackgeroundDrawable() {
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
        float y = (float) (getBounds().bottom * 0.3);

//        canvas.drawRect(getBounds(), paint);
//        RectF rectF_left1 = new RectF(x*-3, 0, x*3, y );
//
//        RectF rectF_right1 = new RectF(getBounds());
//        canvas.drawArc(rectF_left1, 0, 180, true, circlePaint);
        canvas.drawRect(0, 0, x, y, circlePaint);
        // canvas.drawCircle(x / 2, -y * 3f, y * 4f, circlePaint);
//        canvas.drawArc (rectF_right1, 0, 360, false, circlePaint);
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
