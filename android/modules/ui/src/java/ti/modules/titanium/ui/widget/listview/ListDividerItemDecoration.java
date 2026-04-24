/**
 * Titanium SDK
 * Copyright TiDev, Inc. 04/07/2022-Present. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package ti.modules.titanium.ui.widget.listview;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Custom ItemDecoration for ListView that avoids drawing a separator
 * above the first content row and skips separators after placeholder
 * items used for List/ListSection headers/footers.
 */
public class ListDividerItemDecoration extends RecyclerView.ItemDecoration
{
	private final TiListView listView;
	private Drawable dividerDrawable;

	public ListDividerItemDecoration(@NonNull TiListView listView)
	{
		this.listView = listView;
	}

	public void setDrawable(Drawable drawable)
	{
		this.dividerDrawable = drawable;
	}

	@Override
	public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state)
	{
		if (dividerDrawable == null) {
			return;
		}

		final int left = parent.getPaddingLeft();
		final int right = parent.getWidth() - parent.getPaddingRight();
		final int childCount = parent.getChildCount();
		final int adapterCount = listView.getAdapter() != null ? listView.getAdapter().getItemCount() : 0;

		for (int i = 0; i < childCount; i++) {
			final View child = parent.getChildAt(i);
			final int position = parent.getChildAdapterPosition(child);
			if (position == RecyclerView.NO_POSITION) {
				continue;
			}

			// Skip drawing for placeholder items (headers/footers)
			final ListItemProxy item = listView.getItemAtAdapterPosition(position);
			if (item != null && item.isPlaceholder()) {
				continue;
			}

			// Skip drawing after last adapter item
			if (position >= adapterCount - 1) {
				continue;
			}

			// Skip drawing if next adapter item is a placeholder (header/footer)
			final ListItemProxy nextItem = listView.getItemAtAdapterPosition(position + 1);
			if (nextItem != null && nextItem.isPlaceholder()) {
				continue;
			}

			// Draw the divider below this child.
			final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
			final int top = child.getBottom() + params.bottomMargin;
			final int bottom = top + dividerHeight();

			dividerDrawable.setBounds(left, top, right, bottom);
			dividerDrawable.draw(canvas);
		}
	}

	@Override
	public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent,
							 @NonNull RecyclerView.State state)
	{
		if (dividerDrawable == null) {
			outRect.set(0, 0, 0, 0);
			return;
		}

		final int position = parent.getChildAdapterPosition(view);
		if (position == RecyclerView.NO_POSITION) {
			outRect.set(0, 0, 0, 0);
			return;
		}

		final int adapterCount = listView.getAdapter() != null ? listView.getAdapter().getItemCount() : 0;

		// No offset after last item
		if (position >= adapterCount - 1) {
			outRect.set(0, 0, 0, 0);
			return;
		}

		// Do not add space around placeholders and not before a placeholder
		final ListItemProxy item = listView.getItemAtAdapterPosition(position);
		final ListItemProxy nextItem = listView.getItemAtAdapterPosition(position + 1);
		if ((item != null && item.isPlaceholder()) || (nextItem != null && nextItem.isPlaceholder())) {
			outRect.set(0, 0, 0, 0);
			return;
		}

		outRect.set(0, 0, 0, dividerHeight());
	}

	private int dividerHeight()
	{
		return dividerDrawable != null ? Math.max(1, dividerDrawable.getIntrinsicHeight()) : 0;
	}
}
