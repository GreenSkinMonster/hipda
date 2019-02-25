package com.vanniktech.emoji;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridView;

import com.vanniktech.emoji.emoji.Emoji;
import com.vanniktech.emoji.listeners.OnEmojiClickedListener;

import java.util.Collection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@SuppressLint("ViewConstructor")
final class RecentEmojiGridView extends FrameLayout {
    private final RecentEmoji recentEmojis;
    private EmojiArrayAdapter emojiArrayAdapter;

    RecentEmojiGridView(@NonNull final Context context, @NonNull final RecentEmoji recentEmoji) {
        super(context);

        View.inflate(context, R.layout.emoji_grid, this);

        this.recentEmojis = recentEmoji;
    }

    public RecentEmojiGridView init(@Nullable final OnEmojiClickedListener onEmojiClickedListener) {
        final Collection<Emoji> emojis = recentEmojis.getRecentEmojis();
        final GridView gridView = (GridView) findViewById(R.id.emoji_grid_view);
        emojiArrayAdapter = new EmojiArrayAdapter(getContext(), emojis.toArray(new Emoji[emojis.size()]));
        emojiArrayAdapter.setOnEmojiClickedListener(onEmojiClickedListener);
        gridView.setAdapter(emojiArrayAdapter);

        return this;
    }

    public void invalidateEmojis() {
        emojiArrayAdapter.updateEmojis(recentEmojis.getRecentEmojis());
    }

    public int numberOfRecentEmojis() {
        return emojiArrayAdapter.getCount();
    }
}
