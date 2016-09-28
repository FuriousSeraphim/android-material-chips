package com.seraphim.chips;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.target.ImageViewTarget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ChipsView extends ScrollView implements ChipsEditText.InputConnectionWrapperInterface, ChipsEditText.ItemClickListener, TextView.OnEditorActionListener {

    //<editor-fold desc="Static Fields">
    private static final String TAG = "ChipsView";
    private static final int CHIP_HEIGHT = 32; // dp
    private static final int SPACING_TOP = 4; // dp
    private static final int SPACING_BOTTOM = 4; // dp
    public static final int DEFAULT_VERTICAL_SPACING = 1; // dp
    private static final int DEFAULT_MAX_HEIGHT = -1;
    //</editor-fold>

    //<editor-fold desc="Resources">
    private int chipsBgRes = R.drawable.chip_background;
    //</editor-fold>

    //<editor-fold desc="Attributes">
    private int maxHeight; // px
    private int verticalSpacing;

    private int chipsColor;
    private int chipsColorClicked;
    private int chipsColorErrorClicked;
    private int chipsBgColor;
    private int chipsBgColorClicked;
    private int chipsBgColorErrorClicked;
    private int chipsTextColor;
    private int chipsTextColorClicked;
    private int chipsTextColorErrorClicked;
    private int chipsPlaceholderResId;
    private int chipsDeleteResId;
    //</editor-fold>

    //<editor-fold desc="Private Fields">
    private float density;
    private RelativeLayout chipsContainer;
    private ChipsListener chipsListener;
    private ChipsEditText editText;
    private ChipsVerticalLinearLayout rootChipsLayout;
    private EditTextListener editTextListener;
    private List<Chip> chipList = new ArrayList<>();
    private Object currentEditTextSpan;
    private ChipValidator chipsValidator;
    private Mode mode = Mode.ALL;
    private ChipsFactory factory;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    public ChipsView(Context context) {
        super(context);
        init();
    }

    public ChipsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttr(context, attrs);
        init();
    }

    public ChipsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(context, attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ChipsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttr(context, attrs);
        init();
    }
    //</editor-fold>

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (maxHeight != DEFAULT_MAX_HEIGHT) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        if (editText == null) return super.requestFocus(direction, previouslyFocusedRect);
        return editText.requestFocus();
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        return true;
    }

    //<editor-fold desc="Initialization">
    private void initAttr(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ChipsView,
                0, 0);
        try {
            maxHeight = a.getDimensionPixelSize(R.styleable.ChipsView_cv_max_height, DEFAULT_MAX_HEIGHT);
            verticalSpacing = a.getDimensionPixelSize(R.styleable.ChipsView_cv_vertical_spacing, (int) (DEFAULT_VERTICAL_SPACING * density));
            chipsColor = a.getColor(R.styleable.ChipsView_cv_color,
                    ContextCompat.getColor(context, R.color.base30));
            chipsColorClicked = a.getColor(R.styleable.ChipsView_cv_color_clicked,
                    ContextCompat.getColor(context, R.color.colorPrimaryDark));
            chipsColorErrorClicked = a.getColor(R.styleable.ChipsView_cv_color_error_clicked,
                    ContextCompat.getColor(context, R.color.color_error));

            chipsBgColor = a.getColor(R.styleable.ChipsView_cv_bg_color,
                    ContextCompat.getColor(context, R.color.base10));
            chipsBgColorClicked = a.getColor(R.styleable.ChipsView_cv_bg_color_clicked,
                    ContextCompat.getColor(context, R.color.blue));

            chipsBgColorErrorClicked = a.getColor(R.styleable.ChipsView_cv_bg_color_clicked,
                    ContextCompat.getColor(context, R.color.color_error));

            chipsTextColor = a.getColor(R.styleable.ChipsView_cv_text_color,
                    Color.BLACK);
            chipsTextColorClicked = a.getColor(R.styleable.ChipsView_cv_text_color_clicked,
                    Color.WHITE);
            chipsTextColorErrorClicked = a.getColor(R.styleable.ChipsView_cv_text_color_clicked,
                    Color.WHITE);

            chipsPlaceholderResId = a.getResourceId(R.styleable.ChipsView_cv_icon_placeholder,
                    R.drawable.ic_person_24dp);
            chipsDeleteResId = a.getResourceId(R.styleable.ChipsView_cv_icon_delete,
                    R.drawable.ic_close_24dp);
        } finally {
            a.recycle();
        }
    }

    private void init() {
        density = getResources().getDisplayMetrics().density;

        chipsContainer = new RelativeLayout(getContext());
        addView(chipsContainer);

        // Dummy item to prevent AutoCompleteTextView from receiving focus
        LinearLayout linearLayout = new LinearLayout(getContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(0, 0);
        linearLayout.setLayoutParams(params);
        linearLayout.setFocusable(true);
        linearLayout.setFocusableInTouchMode(true);

        chipsContainer.addView(linearLayout);

        editText = new ChipsEditText(getContext(), this, this);
        RelativeLayout.LayoutParams editTextParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editTextParams.topMargin = (int) (SPACING_TOP * density);
        editTextParams.bottomMargin = (int) (SPACING_BOTTOM * density) + verticalSpacing;
        editText.setLayoutParams(editTextParams);
        editText.setMinHeight((int) (CHIP_HEIGHT * density));
        editText.setPadding(0, 0, 0, 0);
        editText.setLineSpacing(verticalSpacing, (CHIP_HEIGHT * density) / editText.getLineHeight());
        editText.setBackgroundColor(Color.argb(0, 0, 0, 0));
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editText.setOnEditorActionListener(this);

        chipsContainer.addView(editText);

        rootChipsLayout = new ChipsVerticalLinearLayout(getContext(), verticalSpacing);
        rootChipsLayout.setOrientation(LinearLayout.VERTICAL);
        rootChipsLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        rootChipsLayout.setPadding(0, (int) (SPACING_TOP * density), 0, 0);
        chipsContainer.addView(rootChipsLayout);

        factory = new DefaultFactory();

        initListener();
    }

    private void initListener() {
        chipsContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.requestFocus();
                unselectAllChips();
            }
        });

        editTextListener = new EditTextListener();
        editText.addTextChangedListener(editTextListener);
        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    unselectAllChips();
                }
            }
        });
    }
    //</editor-fold>

    //<editor-fold desc="Public Methods">
    public void addChip(ChipEntry entry) {
        addChip(entry, false);
    }

    public void addChip(ChipEntry entry, boolean isIndelible) {
        Chip chip = new Chip(entry, isIndelible);
        chipList.add(chip);
        if (chipsListener != null) {
            chipsListener.onChipAdded(chip);
        }

        onChipsChanged(true);
        post(new Runnable() {
            @Override
            public void run() {
                fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    public void addChips(Collection<ChipEntry> entries) {
        addChips(entries, false);
    }

    public void addChips(Collection<ChipEntry> entries, boolean isIndelible) {
        for (ChipEntry entry : entries) {
            Chip chip = new Chip(entry, isIndelible);
            chipList.add(chip);
            if (chipsListener != null) {
                chipsListener.onChipAdded(chip);
            }
        }

        onChipsChanged(true);
        post(new Runnable() {
            @Override
            public void run() {
                fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    @NonNull
    public List<Chip> getChips() {
        return Collections.unmodifiableList(chipList);
    }

    public boolean removeChipBy(ChipEntry entry) {
        for (int i = 0; i < chipList.size(); i++) {
            if (chipList.get(i).entry != null && chipList.get(i).entry.equals(entry)) {
                chipList.remove(i);
                onChipsChanged(true);
                return true;
            }
        }
        return false;
    }

    public void removeAllChips() {
        chipList.clear();
        onChipsChanged(true);
    }

    public void setChipsListener(ChipsListener chipsListener) {
        this.chipsListener = chipsListener;
    }

    public void setChipsValidator(ChipValidator chipsValidator) {
        this.chipsValidator = chipsValidator;
    }

    public void addSuggestions(List<ChipEntry> entries) {
        editText.addSuggestions(entries);
    }

    public void setSuggestions(List<ChipEntry> entries) {
        editText.setSuggestions(entries);
    }

    public void setChipsFilter(ChipsEntriesFilter chipsEntriesFilter) {
        editText.setChipsFilter(chipsEntriesFilter);
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        if (mode == Mode.NONE) {
            editText.setInputType(InputType.TYPE_NULL);
            editText.setFocusable(false);
            editText.setFocusableInTouchMode(false);
        } else {
            editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            editText.setFocusable(true);
            editText.setFocusableInTouchMode(true);
        }
    }

    public void setFactory(ChipsFactory factory) {
        this.factory = factory;
    }
    //</editor-fold>

    //<editor-fold desc="Private Methods">

    /**
     * rebuild all chips and place them right
     */
    private void onChipsChanged(final boolean moveCursor) {
        ChipsVerticalLinearLayout.TextLineParams textLineParams = rootChipsLayout.onChipsChanged(chipList);

        // if null then run another layout pass
        if (textLineParams == null) {
            post(new Runnable() {
                @Override
                public void run() {
                    onChipsChanged(moveCursor);
                }
            });
            return;
        }

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) editText.getLayoutParams();
        params.topMargin = (int) ((SPACING_TOP + textLineParams.row * CHIP_HEIGHT) * density) + textLineParams.row * verticalSpacing;
        editText.setLayoutParams(params);
        addLeadingMarginSpan(textLineParams.lineMargin);
        if (moveCursor) {
            editText.setSelection(editText.length());
        }
    }

    private void addLeadingMarginSpan(int margin) {
        Editable text = editText.getText();
        if (currentEditTextSpan != null) text.removeSpan(currentEditTextSpan);
        currentEditTextSpan = new android.text.style.LeadingMarginSpan.LeadingMarginSpan2.Standard(margin + Math.round(4 * density), 0);
        text.setSpan(currentEditTextSpan, 0, 0, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    private void addLeadingMarginSpan() {
        Editable text = editText.getText();
        if (currentEditTextSpan != null) text.removeSpan(currentEditTextSpan);
        text.setSpan(currentEditTextSpan, 0, 0, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    private void selectOrDeleteLastChip() {
        if (chipList.size() > 0) {
            onChipInteraction(chipList.size() - 1);
        }
    }

    private void onChipInteraction(int position) {
        try {
            Chip chip = chipList.get(position);
            if (chip != null) {
                onChipInteraction(chip);
            }
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "Out of bounds", e);
        }
    }

    private void onChipInteraction(final Chip chip) {
        unselectChipsExcept(chip);
        if (chip.isSelected()) {
            chipList.remove(chip);
            if (chipsListener != null) {
                chipsListener.onChipDeleted(chip);
            }
            onChipsChanged(true);
        } else {
            chip.setSelected(true);
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (chipList.contains(chip)) {
                        chip.setSelected(false);
                        onChipsChanged(false);
                    }
                }
            }, 2000);
            onChipsChanged(false);
        }
    }

    private void unselectChipsExcept(Chip rootChip) {
        for (Chip chip : chipList) {
            if (chip != rootChip) {
                chip.setSelected(false);
            }
        }
        onChipsChanged(false);
    }

    private void unselectAllChips() {
        unselectChipsExcept(null);
    }
    //</editor-fold>

    //<editor-fold desc="Listeners Implementations">
    @Override
    public InputConnection getInputConnection(InputConnection target) {
        return new KeyInterceptingInputConnection(target);
    }

    @Override
    public void clicked(ChipEntry entry) {
        addChip(entry);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (mode == Mode.ALL && !editText.getText().toString().isEmpty() && actionId == EditorInfo.IME_ACTION_DONE) {
            ChipEntry entry = factory.createChip(editText.getText().toString());
            editText.setText("");
            addChip(entry);
        }
        return true;
    }
    //</editor-fold>

    //<editor-fold desc="Inner Classes / Interfaces">
    private class EditTextListener implements TextWatcher {
        private boolean mIsPasteTextChange = false;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (count > 1) {
                mIsPasteTextChange = true;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mIsPasteTextChange) {
                mIsPasteTextChange = false;
                // todo handle copy/paste text here

            } else {
                // no paste text change
                if (s.toString().contains("\n")) {
                    String text = s.toString();
                    text = text.replace("\n", "");
                    while (text.contains("  ")) {
                        text = text.replace("  ", " ");
                    }
                    s.clear();
                    if (text.length() > 1) {
                    } else {
                        s.append(text);
                    }
                }
            }
            if (chipsListener != null) {
                chipsListener.onTextChanged(s);
            }
        }
    }

    private class KeyInterceptingInputConnection extends InputConnectionWrapper {

        public KeyInterceptingInputConnection(InputConnection target) {
            super(target, true);
        }

        @Override
        public boolean commitText(CharSequence text, int newCursorPosition) {
            return super.commitText(text, newCursorPosition);
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            Log.d("Key", "action: " + event.getAction() + ", code: " + event.getKeyCode());
            if (editText.length() == 0) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                    selectOrDeleteLastChip();
                    return true;
                }
            }
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                Log.d(TAG, "Enter: " + editText.getText().toString());
                editText.append("\n");
                return true;
            }

            return super.sendKeyEvent(event);
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            // magic: in latest Android, deleteSurroundingText(1, 0) will be called for backspace
            if (editText.length() == 0 && beforeLength == 1 && afterLength == 0) {
                // backspace
                return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                        && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            }

            return super.deleteSurroundingText(beforeLength, afterLength);
        }
    }

    public class Chip implements OnClickListener {

        private static final int MAX_LABEL_LENGTH = 30;

        private String label;
        private final Uri photoUri;
        private final ChipEntry entry;
        private final boolean isIndelible;

        private RelativeLayout view;
        private View iconWrapper;
        private TextView textView;

        private ImageView avatarView;
        private ImageView personIcon;
        private ImageView closeIcon;

        private ImageView errorIcon;

        private boolean isSelected = false;

        public Chip(ChipEntry entry) {
            this(entry, false);
        }

        public Chip(ChipEntry entry, boolean isIndelible) {
            this.label = entry.displayedName();
            this.photoUri = entry.avatarUri();
            this.entry = entry;
            this.isIndelible = isIndelible;

            if (this.label == null) {
                this.label = entry.displayedName();
            }

            if (this.label.length() > MAX_LABEL_LENGTH) {
                this.label = this.label.substring(0, MAX_LABEL_LENGTH) + "...";
            }
        }

        public View getView() {
            if (view == null) {
                view = (RelativeLayout) inflate(getContext(), R.layout.chips_view, null);
                view.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) (CHIP_HEIGHT * density)));
                avatarView = (ImageView) view.findViewById(R.id.ri_ch_avatar);
                iconWrapper = view.findViewById(R.id.rl_ch_avatar);
                textView = (TextView) view.findViewById(R.id.tv_ch_name);
                personIcon = (ImageView) view.findViewById(R.id.iv_ch_person);
                closeIcon = (ImageView) view.findViewById(R.id.iv_ch_close);

                errorIcon = (ImageView) view.findViewById(R.id.iv_ch_error);

                // set inital res & attrs
                view.setBackgroundResource(chipsBgRes);
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        view.getBackground().setColorFilter(chipsBgColor, PorterDuff.Mode.SRC_ATOP);
                    }
                });
                iconWrapper.setBackgroundResource(R.drawable.circle);
                textView.setTextColor(chipsTextColor);

                // set icon resources
                personIcon.setBackgroundResource(chipsPlaceholderResId);
                closeIcon.setBackgroundResource(chipsDeleteResId);


                view.setOnClickListener(this);
                iconWrapper.setOnClickListener(this);
            }
            updateViews();
            return view;
        }

        private void updateViews() {
            textView.setText(label);
            if (photoUri != null) {
                Glide.with(getContext())
                        .load(photoUri)
                        .asBitmap()
                        .transform(new CenterCrop(getContext()))
                        .into(new ImageViewTarget<Bitmap>(avatarView) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                avatarView.setImageBitmap(resource);
                                personIcon.setVisibility(INVISIBLE);
                            }
                        });
            }
            if (isSelected()) {
                if (chipsValidator != null && !chipsValidator.isValid(entry)) {
                    // not valid & show error
                    view.getBackground().setColorFilter(chipsBgColorErrorClicked, PorterDuff.Mode.SRC_ATOP);
                    textView.setTextColor(chipsTextColorErrorClicked);
                    iconWrapper.getBackground().setColorFilter(chipsColorErrorClicked, PorterDuff.Mode.SRC_ATOP);
                    errorIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                } else {
                    view.getBackground().setColorFilter(chipsBgColorClicked, PorterDuff.Mode.SRC_ATOP);
                    textView.setTextColor(chipsTextColorClicked);
                    iconWrapper.getBackground().setColorFilter(chipsColorClicked, PorterDuff.Mode.SRC_ATOP);
                }
                personIcon.animate().alpha(0.0f).setDuration(200).start();
                avatarView.animate().alpha(0.0f).setDuration(200).start();
                closeIcon.animate().alpha(1f).setDuration(200).setStartDelay(100).start();

            } else {
                if (chipsValidator != null && !chipsValidator.isValid(entry)) {
                    // not valid & show error
                    errorIcon.setVisibility(View.VISIBLE);
                    errorIcon.setColorFilter(null);
                } else {
                    errorIcon.setVisibility(View.GONE);
                }
                view.getBackground().setColorFilter(chipsBgColor, PorterDuff.Mode.SRC_ATOP);
                textView.setTextColor(chipsTextColor);
                iconWrapper.getBackground().setColorFilter(chipsColor, PorterDuff.Mode.SRC_ATOP);

                personIcon.animate().alpha(0.3f).setDuration(200).setStartDelay(100).start();
                avatarView.animate().alpha(1f).setDuration(200).setStartDelay(100).start();
                closeIcon.animate().alpha(0.0f).setDuration(200).start();
            }
        }

        @Override
        public void onClick(View v) {
            editText.clearFocus();
            onChipInteraction(this);
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean isSelected) {
            if (isIndelible) {
                return;
            }
            this.isSelected = isSelected;
        }

        public ChipEntry getEntry() {
            return entry;
        }

        @Override
        public boolean equals(Object o) {
            if (entry != null && o instanceof ChipEntry) {
                return entry.equals(o);
            }
            return super.equals(o);
        }

        @Override
        public String toString() {
            return "{"
                    + "[Entry: " + entry + "]"
                    + "}"
                    ;
        }
    }

    public enum Mode {NONE, ONLY_SUGGESTIONS, ALL}

    private class DefaultFactory implements ChipsFactory {
        @Override
        public ChipEntry createChip(String text) {
            return new SimpleChipEntry(text, null);
        }
    }
    //</editor-fold>
}
