package net.friendsmap.ayrsa.friendsmap;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

public class BackView extends Drawable {
    private Paint paint;
    private Paint cPaint;
    private int backgroundColor = Color.parseColor("#4abdfe");
    private int circleColor = Color.parseColor("#fafafa");

    public BackView() {
        super();
        paint = new Paint();
        cPaint = new Paint();
        paint.setColor(backgroundColor);
    }

    @Override
    public void draw(Canvas canvas) {

        int x = getBounds().right;
        float y = (float) (getBounds().bottom * 0.35);

        canvas.drawRect(0, 0, x, y, paint);
        cPaint.setColor(circleColor);
        canvas.drawCircle(25, y + 125, (float) (x * 0.23), cPaint);
        canvas.drawCircle((float) (x * 0.28) + 25, y + 100, (float) (x * 0.28), cPaint);
        canvas.drawCircle((float) (x * 0.35) + (float) (x * 0.28) + 125, y + 75, (float) (x * 0.35), cPaint);
    }


    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
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