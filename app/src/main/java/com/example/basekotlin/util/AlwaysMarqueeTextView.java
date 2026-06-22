package com.example.basekotlin.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class AlwaysMarqueeTextView extends AppCompatTextView {

        public AlwaysMarqueeTextView(Context contextVpn) {
        super(contextVpn);
        init(contextVpn, null);
    }

    public AlwaysMarqueeTextView(Context contextVpn, AttributeSet attrs) {
        super(contextVpn, attrs);
        init(contextVpn, attrs);
    }

    public AlwaysMarqueeTextView(Context contextVpn, AttributeSet attrs, int defStyleAttr) {
        super(contextVpn, attrs, defStyleAttr);
        init(contextVpn, attrs);
    }

    private void init(Context contextVpn, AttributeSet attrs) {
        setSingleLine(true);
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        setMarqueeRepeatLimit(-1);
        setSelected(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);
        setHorizontallyScrolling(true);

        if (attrs != null) {
            int[] attrArray = new int[]{android.R.attr.textAllCaps};
            TypedArray typedArray = contextVpn.obtainStyledAttributes(attrs, attrArray);
            boolean allCaps = typedArray.getBoolean(0, false);
            typedArray.recycle();

            if (allCaps && getText() != null) {
                setText(getText().toString().toUpperCase());
            }
        }
    }

    @Override
    public boolean isFocused() {
        return true;
    }

    /**
     * Prevent marquee from restarting when text is set programmatically
     * This is called during RecyclerView rebind
     */
    @Override
    public void setText(CharSequence text, BufferType type) {
        if (text != null && text.equals(getText())) {
            return;
        }
        super.setText(text, type);
    }
}