package com.vanniktech.emoji;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.DisplayCutout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupWindow;

import com.vanniktech.emoji.emoji.Emoji;
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener;
import com.vanniktech.emoji.listeners.OnEmojiClickedListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class EmojiPopup {
    private static final int MIN_KEYBOARD_HEIGHT = 100;
    private static final String PREFERENCE_NAME = "emoji-recent-manager";
    private static final String PREF_KEYBOARD_HEIGHT = "PREF_KEYBOARD_HEIGHT";

    private final EmojiEditText emojiEditText;
    private final View rootView;
    private final Context context;

    private int keyBoardHeight;
    private boolean isKeyboardOpen;
    private boolean isListenerAttached;

    @Nullable
    private OnEmojiPopupShownListener onEmojiPopupShownListener;
    @Nullable
    private OnSoftKeyboardCloseListener onSoftKeyboardCloseListener;
    @Nullable
    private OnSoftKeyboardOpenListener onSoftKeyboardOpenListener;
    @Nullable
    private OnEmojiBackspaceClickListener onEmojiBackspaceClickListener;
    @Nullable
    private OnEmojiClickedListener onEmojiClickedListener;
    @Nullable
    private OnEmojiPopupDismissListener onEmojiPopupDismissListener;

    @NonNull
    private final RecentEmoji recentEmoji;

    private final PopupWindow popupWindow;
    private final ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            final Rect rect = new Rect();
            rootView.getWindowVisibleDisplayFrame(rect);

            int heightDifference = getUsableScreenHeight() + getCutoutHeight() - rect.bottom;

            if (heightDifference > MIN_KEYBOARD_HEIGHT) {
                if (keyBoardHeight != heightDifference) {
                    keyBoardHeight = heightDifference;
                    getPreferences().edit().putInt(PREF_KEYBOARD_HEIGHT, keyBoardHeight).apply();
                    if (popupWindow.isShowing())
                        popupWindow.setHeight(keyBoardHeight);
                }

                if (!isKeyboardOpen && onSoftKeyboardOpenListener != null) {
                    onSoftKeyboardOpenListener.onKeyboardOpen(keyBoardHeight);
                }
                isKeyboardOpen = true;
            } else {
                if (isKeyboardOpen) {
                    isKeyboardOpen = false;

                    if (onSoftKeyboardCloseListener != null) {
                        onSoftKeyboardCloseListener.onKeyboardClose();
                    }
                }
            }
        }
    };

    private EmojiPopup(final View rootView, final EmojiEditText emojiEditText, @Nullable final RecentEmoji recent) {
        this.context = rootView.getContext();
        this.rootView = rootView;
        this.emojiEditText = emojiEditText;
        this.recentEmoji = recent != null ? recent : new RecentEmojiManager(context);
        this.keyBoardHeight = getPreferences().getInt(PREF_KEYBOARD_HEIGHT, 0);

        popupWindow = new PopupWindow(context);
        popupWindow.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null)); // To avoid borders & overdraw

        final EmojiView emojiView = new EmojiView(context, new OnEmojiClickedListener() {
            @Override
            public void onEmojiClicked(final Emoji emoji) {
                emojiEditText.input(emoji);
                recentEmoji.addEmoji(emoji);

                if (onEmojiClickedListener != null) {
                    onEmojiClickedListener.onEmojiClicked(emoji);
                }
            }
        }, this.recentEmoji);

        emojiView.setOnEmojiBackspaceClickListener(new OnEmojiBackspaceClickListener() {
            @Override
            public void onEmojiBackspaceClicked(final View v) {
                emojiEditText.backspace();

                if (onEmojiBackspaceClickListener != null) {
                    onEmojiBackspaceClickListener.onEmojiBackspaceClicked(v);
                }
            }
        });

        popupWindow.setContentView(emojiView);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        popupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight((int) context.getResources().getDimension(R.dimen.emoji_keyboard_height));
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (onEmojiPopupDismissListener != null) {
                    onEmojiPopupDismissListener.onEmojiPopupDismiss();
                }
            }
        });

        setupListener();
    }

    private void showAtBottom() {
        if (keyBoardHeight > MIN_KEYBOARD_HEIGHT) {
            popupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
            popupWindow.setHeight(keyBoardHeight);
        }
        popupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
        isKeyboardOpen = true;
    }

    public void toggle() {
        if (!popupWindow.isShowing()) {
            setupListener();
            if (isKeyboardOpen) {
                // If keyboard is visible, simply show the emoji popup
                this.showAtBottom();
            } else {
                // Open the text keyboard first and immediately after that show the emoji popup
                emojiEditText.setFocusableInTouchMode(true);
                emojiEditText.requestFocus();

                this.showAtBottom();

                final InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(emojiEditText, InputMethodManager.SHOW_IMPLICIT);
            }

            if (onEmojiPopupShownListener != null) {
                onEmojiPopupShownListener.onEmojiPopupShown();
            }
        } else {
            dismiss();
        }
    }

    public boolean isShowing() {
        return popupWindow.isShowing();
    }

    public void dismiss() {
        removeListener();
        popupWindow.dismiss();
        recentEmoji.persist();
    }

    private int getCutoutHeight() {
        int cutoutHeight = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowInsets windowInsets = rootView.getRootWindowInsets();
            if (windowInsets != null) {
                DisplayCutout displayCutout = windowInsets.getDisplayCutout();
                if (displayCutout != null) {
                    List<Rect> rects = displayCutout.getBoundingRects();
                    for (Rect rect : rects) {
                        if (rect.top == 0) {
                            cutoutHeight += rect.bottom - rect.top;
                        }
                    }
                }
            }
        }
        return cutoutHeight;
    }

    private int getUsableScreenHeight() {
        final DisplayMetrics metrics = new DisplayMetrics();

        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);

        return metrics.heightPixels;
    }

    private int getStatusBarHeight() {
        int statusBarHeight = 0;
        final Resources resources = context.getResources();
        final int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    public static final class Builder {
        /**
         * @param rootView the rootView of your layout.xml which will be used for calculating the height of the keyboard
         * @return builder for building {@link EmojiPopup}
         */
        public static Builder fromRootView(final View rootView) {
            return new Builder(rootView);
        }

        private final View rootView;

        @Nullable
        private OnEmojiPopupShownListener onEmojiPopupShownListener;
        @Nullable
        private OnSoftKeyboardCloseListener onSoftKeyboardCloseListener;
        @Nullable
        private OnSoftKeyboardOpenListener onSoftKeyboardOpenListener;
        @Nullable
        private OnEmojiBackspaceClickListener onEmojiBackspaceClickListener;
        @Nullable
        private OnEmojiClickedListener onEmojiClickedListener;
        @Nullable
        private OnEmojiPopupDismissListener onEmojiPopupDismissListener;

        @Nullable
        private RecentEmoji recentEmoji;

        private Builder(final View rootView) {
            this.rootView = rootView;
        }

        public Builder setOnSoftKeyboardCloseListener(@Nullable final OnSoftKeyboardCloseListener listener) {
            this.onSoftKeyboardCloseListener = listener;
            return this;
        }

        public Builder setOnEmojiClickedListener(@Nullable final OnEmojiClickedListener listener) {
            this.onEmojiClickedListener = listener;
            return this;
        }

        public Builder setOnSoftKeyboardOpenListener(@Nullable final OnSoftKeyboardOpenListener listener) {
            this.onSoftKeyboardOpenListener = listener;
            return this;
        }

        public Builder setOnEmojiPopupShownListener(@Nullable final OnEmojiPopupShownListener listener) {
            this.onEmojiPopupShownListener = listener;
            return this;
        }

        public Builder setOnEmojiPopupDismissListener(@Nullable final OnEmojiPopupDismissListener listener) {
            this.onEmojiPopupDismissListener = listener;
            return this;
        }

        public Builder setOnEmojiBackspaceClickListener(@Nullable final OnEmojiBackspaceClickListener listener) {
            this.onEmojiBackspaceClickListener = listener;
            return this;
        }

        /**
         * allows you to pass your own implementation of recent emojis. If not provided the default one ({@link RecentEmojiManager} will be used
         *
         * @since 0.2.0
         */
        public Builder setRecentEmoji(@Nullable final RecentEmoji recent) {
            this.recentEmoji = recent;
            return this;
        }

        public EmojiPopup build(final EmojiEditText emojiEditText) {
            final EmojiPopup emojiPopup = new EmojiPopup(rootView, emojiEditText, recentEmoji);
            emojiPopup.onSoftKeyboardCloseListener = onSoftKeyboardCloseListener;
            emojiPopup.onEmojiClickedListener = onEmojiClickedListener;
            emojiPopup.onSoftKeyboardOpenListener = onSoftKeyboardOpenListener;
            emojiPopup.onEmojiPopupShownListener = onEmojiPopupShownListener;
            emojiPopup.onEmojiPopupDismissListener = onEmojiPopupDismissListener;
            emojiPopup.onEmojiBackspaceClickListener = onEmojiBackspaceClickListener;
            return emojiPopup;
        }
    }

    private SharedPreferences getPreferences() {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    private void setupListener() {
        if (!isListenerAttached) {
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
            isListenerAttached = true;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void removeListener() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            //noinspection deprecation
            rootView.getViewTreeObserver().removeGlobalOnLayoutListener(onGlobalLayoutListener);
        } else {
            rootView.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
        }
        isListenerAttached = false;
    }

    public void addImage(String imgId, Bitmap bitmap) {
        EmojiHandler.addImage(imgId, bitmap);
    }

    public void cleanup() {
        dismiss();
        EmojiHandler.cleanup();
    }

}