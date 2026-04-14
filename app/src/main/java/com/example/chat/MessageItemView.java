package com.example.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;

import io.zhuozhuo.remotetestlib.Message;
import io.zhuozhuo.remotetestlib.Size;

public class MessageItemView extends View {
    private Message message;
    private StaticLayout staticLayout;
    private TextPaint textPaint;
    private Bitmap currentBitmap;
    private Paint bitmapPaint;

    private int displayW;
    private int displayH;
    private boolean isScrolling;
    private int textWidth;

    public MessageItemView(Context context) {
        super(context);
        init();
    }

    private void init() {
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public void setMessage(Message message, boolean isScrolling) {
        boolean isSameMessage = (this.message != null && this.message.getMsgId() == message.getMsgId());
        if (!isSameMessage) {
            this.currentBitmap = null;
        }
        this.message = message;
        this.isScrolling = isScrolling;
        this.staticLayout = null;
        this.textWidth = 0;

        if (message.getMsgType() == Message.MessageTypeText) {
            textPaint.setTextSize(Size.message_text_size);
            
            // Measure first to see if it's smaller than max width
            float measuredWidth = textPaint.measureText(message.getContent());
            int layoutWidth = Math.min((int) Math.ceil(measuredWidth), Size.message_text_max_width);
            
            staticLayout = new StaticLayout(
                    message.getContent(),
                    textPaint,
                    layoutWidth,
                    Layout.Alignment.ALIGN_NORMAL,
                    1.0f,
                    0.0f,
                    false
            );
            
            for (int i = 0; i < staticLayout.getLineCount(); i++) {
                textWidth = Math.max(textWidth, (int) staticLayout.getLineWidth(i));
            }
            
        } else if (message.getMsgType() == Message.MessageTypeImage) {
            float maxWidth = Size.message_image_max_width;
            float maxHeight = Size.message_image_max_height;
            int origW = message.getImageWidth();
            int origH = message.getImageHeight();

            displayW = origW;
            displayH = origH;
            if (origW > maxWidth || origH > maxHeight) {
                float ratio = Math.min(maxWidth / (float) origW, maxHeight / (float) origH);
                displayW = Math.max(1, (int) (origW * ratio));
                displayH = Math.max(1, (int) (origH * ratio));
            }

            Bitmap memBitmap = ImageCache.getInstance().getBitmapFromMemCache(message.getContent());
            if (memBitmap != null) {
                this.currentBitmap = memBitmap;
            } else if (!isScrolling) {
                loadImage();
            }
        }
        requestLayout();
        invalidate();
    }

    private void loadImage() {
        ImageLoader.getInstance().loadImage(message, (bitmap, url) -> {
            if (message != null && url.equals(message.getContent())) {
                currentBitmap = bitmap;
                invalidate();
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = 0;

        if (message == null) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY));
            return;
        }

        if (message.getMsgType() == Message.MessageTypeText && staticLayout != null) {
            height = staticLayout.getHeight() + Size.message_vertical_margin * 2;
        } else if (message.getMsgType() == Message.MessageTypeImage) {
            height = displayH + Size.message_vertical_margin * 2;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (message == null) return;

        int width = getWidth();
        int rightMargin = Size.message_horizontal_margin;
        int topMargin = Size.message_vertical_margin;

        if (message.getMsgType() == Message.MessageTypeText && staticLayout != null) {
            int left = width - rightMargin - textWidth;
            canvas.save();
            canvas.translate(left, topMargin);
            staticLayout.draw(canvas);
            canvas.restore();
        } else if (message.getMsgType() == Message.MessageTypeImage) {
            int left = width - rightMargin - displayW;
            if (currentBitmap != null && !currentBitmap.isRecycled()) {
                Rect destRect = new Rect(left, topMargin, left + displayW, topMargin + displayH);
                canvas.drawBitmap(currentBitmap, null, destRect, bitmapPaint);
            } else {
                Paint placeholderPaint = new Paint();
                placeholderPaint.setColor(Color.LTGRAY);
                canvas.drawRect(left, topMargin, left + displayW, topMargin + displayH, placeholderPaint);
            }
        }
    }
}
