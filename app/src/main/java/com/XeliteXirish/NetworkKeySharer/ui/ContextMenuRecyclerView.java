package com.XeliteXirish.NetworkKeySharer.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.View;

public class ContextMenuRecyclerView extends RecyclerView {

    private ContextMenu.ContextMenuInfo contextMenuInfo;

    public ContextMenuRecyclerView(Context context) {
        super(context);
    }

    public ContextMenuRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ContextMenuRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return contextMenuInfo;
    }

    @Override
    public boolean showContextMenuForChild(View originalView) {
        final int longPressPosition = getChildLayoutPosition(originalView);
        if (longPressPosition >= 0) {
            final long longPressId = getAdapter().getItemId(longPressPosition);
            contextMenuInfo = new RecyclerContextMenuInfo(longPressPosition, longPressId);
            return super.showContextMenuForChild(originalView);
        }
        return false;

    }

    /**
     * Extra menu information provided to the
     * {@link android.view.View.OnCreateContextMenuListener#onCreateContextMenu(ContextMenu, View, ContextMenu.ContextMenuInfo)}
     * callback when a context menu is brought up for this AdapterView.
     */
    public static class RecyclerContextMenuInfo implements ContextMenu.ContextMenuInfo {

        /**
         * The position in the adapter for which the context menu is being
         * displayed.
         */
        public int position;
        /**
         * The row id of the item for which the context menu is being displayed.
         */
        public long id;

        public RecyclerContextMenuInfo(int position, long id) {
            this.position = position;
            this.id = id;
        }
    }
}
