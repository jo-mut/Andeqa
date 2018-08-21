package com.andeqa.andeqa.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.andeqa.andeqa.R;

public class AspectRatioImageView extends ImageView {
    private float aspectRatio = 0;
    private static final int MEASUREMENT_WIDTH = 0;
    private static final int MEASUREMENT_HEIGHT = 1;
    private static final float DEFAULT_ASPECT_RATIO = 1f;
    private static final boolean DEFAULT_ASPECT_RATIO_ENABLED = false;
    private static final  int DEFAULT_DOMINANT_MEASUREMENT = MEASUREMENT_WIDTH;
    private boolean aspectRatioEnabled;
    private int dominantMeasurement;

    public AspectRatioImageView(Context context) {
        super(context);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.AspectRatioImageView);
        aspectRatio = array.getFloat(R.styleable.AspectRatioImageView_aspectRatio, DEFAULT_ASPECT_RATIO);
        aspectRatioEnabled = array.getBoolean(R.styleable.AspectRatioImageView_aspectRatioEnabled, DEFAULT_ASPECT_RATIO_ENABLED);
        dominantMeasurement = array.getInt(R.styleable.AspectRatioImageView_dominantMeasurement, DEFAULT_DOMINANT_MEASUREMENT);
        array.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (!aspectRatioEnabled) return;

        int newWidth;
        int newHeight;
        switch (dominantMeasurement){
            case MEASUREMENT_WIDTH:
                newWidth = getMeasuredWidth();
                newHeight =(int) (newWidth * aspectRatio);
                break;
            case MEASUREMENT_HEIGHT:
                newHeight = getMeasuredHeight();
                newWidth = (int) (newHeight * aspectRatio);
                break;
            default:
                throw  new IllegalStateException("unknown measurement width" + dominantMeasurement);
        }

        setMeasuredDimension(newWidth, newHeight);

    }

    //get the aspect ratio for the current image view
    public float getAspectRatio(){
        return aspectRatio;
    }

    public void setAspectRatio(float aspectRatio){
        this.aspectRatio = aspectRatio;
        if (aspectRatioEnabled){
            requestLayout();
        }
    }

    //get whether or not forcing the aspect ration is enabled
    public void setAspectRatioEnabled(boolean aspectRatioEnabled){
        this.aspectRatioEnabled = aspectRatioEnabled;
        requestLayout();
    }

    //set the dominant measurement for the aspect ratio
    public void setDominantMeasurement(int  dominantMeasurement){
        if (dominantMeasurement != MEASUREMENT_WIDTH && dominantMeasurement != MEASUREMENT_HEIGHT){
            throw new IllegalArgumentException("Invalid measurement type");
        }

        this.dominantMeasurement = dominantMeasurement;
        requestLayout();
    }
}
