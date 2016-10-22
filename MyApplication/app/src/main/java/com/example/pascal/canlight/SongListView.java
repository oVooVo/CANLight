package com.example.pascal.canlight;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.ExpandableListView;

/**
 * Created by pascal on 22.10.16.
 */
public class SongListView extends ExpandableListView {
    private static final String TAG = "SongListView";
    public SongListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SongListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SongListView(Context context) {
        super(context);
        init();
    }

    void init() {
    }

    public void startDrag(View view, int groupPos, int childPos) {
        DragShadowBuilder shadowBuilder = new DragShadowBuilder(view);

        Intent data = new Intent();
        data.putExtra("group", groupPos);
        data.putExtra("child", childPos);

        ClipData.Item item = new ClipData.Item(data);
        ClipData dragData = new ClipData("songListItem", new String[]{ ClipDescription.MIMETYPE_TEXT_PLAIN }, item);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.d(TAG, "Start drag");
            view.startDragAndDrop(dragData, shadowBuilder, null, 0);
        } else {
            Log.d(TAG, "Start drag!");
            view.startDrag(dragData, shadowBuilder, null, 0);
        }
    }

    public boolean onDragEvent(DragEvent event) {
        Log.d(TAG, "onDragEvent"); //event.getAction() + ", " + event.getClipData());

        // Defines a variable to store the action type for the incoming event
        final int action = event.getAction();

        return true;
    }
}
