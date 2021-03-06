/*
 * Copyright (C) 2016 Doodle AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seraphim.chips;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

class ChipsVerticalLinearLayout extends LinearLayout {
    private List<LinearLayout> lineLayouts = new ArrayList<>();
    private float density;
    private int rowSpacing;

    public ChipsVerticalLinearLayout(Context context, int rowSpacing) {
        super(context);

        density = getResources().getDisplayMetrics().density;
        this.rowSpacing = rowSpacing;

        init();
    }

    private void init() {
        setOrientation(VERTICAL);
    }

    public TextLineParams onChipsChanged(List<ChipsView.Chip> chips) {
        clearChipsViews();

        int width = getWidth();
        if (width == 0) {
            return null;
        }
        int widthSum = 0;
        int rowCounter = 0;

        LinearLayout ll = createHorizontalView();

        for (ChipsView.Chip chip : chips) {
            View view = chip.getView();
            view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

            // if width exceed current width. create a new LinearLayout
            if (widthSum + view.getMeasuredWidth() > width) {
                rowCounter++;
                widthSum = 0;
                ll = createHorizontalView();
            }

            widthSum += view.getMeasuredWidth();
            ll.addView(view);
        }

        // check if there is enough space left
        if (width - widthSum < width * 0.1f) {
            widthSum = 0;
            rowCounter++;
        }
        if (width == 0) {
            rowCounter = 0;
        }
        widthSum += Math.round(ll.getChildCount() * (float) 8 * density);
        return new TextLineParams(rowCounter, widthSum);
    }

    private LinearLayout createHorizontalView() {
        LinearLayout ll = new LinearLayout(getContext());
        ll.setPadding(0, 0, 0, rowSpacing);
        ll.setOrientation(HORIZONTAL);
        ll.setDividerDrawable(ContextCompat.getDrawable(getContext(), R.drawable.empty_vertical_divider));
        ll.setShowDividers(SHOW_DIVIDER_MIDDLE);
        addView(ll);
        lineLayouts.add(ll);
        return ll;
    }

    private void clearChipsViews() {
        for (LinearLayout linearLayout : lineLayouts) {
            linearLayout.removeAllViews();
        }
        lineLayouts.clear();
        removeAllViews();
    }

    public static class TextLineParams {
        public int row;
        public int lineMargin;

        public TextLineParams(int row, int lineMargin) {
            this.row = row;
            this.lineMargin = lineMargin;
        }
    }
}
