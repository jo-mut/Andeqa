package com.cinggl.cinggl.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.cinggl.cinggl.R;

/**
 * Created by J.EL on 9/16/2017.
 */

public class ExpandableTextView extends TextView implements View.OnClickListener{

    private static final int MAX_LINES = 2;
    private int currentMaxLines = Integer.MAX_VALUE;
    private static final String TAG = "ExpandableTextView";
    private static final String ELLIPSIZE = "... ";
    private static final String MORE = "more";
    private static final String LESS = "less";
    private String mFullText;
    private int mMaxLines;


    public ExpandableTextView(Context context)
    {
        super(context);
        setOnClickListener(this);
    }
    public ExpandableTextView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        setOnClickListener(this);
    }

    public ExpandableTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setOnClickListener(this);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ExpandableTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setOnClickListener(this);
    }


    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
    {

        /* If text longer than MAX_LINES set DrawableBottom - I'm using '...' icon */
        post(new Runnable()
        {
            public void run() {
                if (getLineCount()>MAX_LINES)
                    setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_expand_more_black_24dp);
                else{
                    setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                    setMaxLines(MAX_LINES);
                }


            }
        });
    }


    @Override
    public void setMaxLines(int maxLines)
    {
        currentMaxLines = maxLines;
        super.setMaxLines(maxLines);
    }

    /* Custom method because standard getMaxLines() requires API > 16 */
    public int getMyMaxLines()
    {
        return currentMaxLines;
    }

    @Override
    public void onClick(View v)
    {
        /* Toggle between expanded collapsed states */
        if (getMyMaxLines() == Integer.MAX_VALUE) {
            setMaxLines(MAX_LINES);
        } else{
            setMaxLines(Integer.MAX_VALUE);

        }
    }

}
