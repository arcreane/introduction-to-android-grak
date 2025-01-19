package com.example.headsup.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.headsup.R;
import com.example.headsup.models.WordResult;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class WordHistoryAdapter extends RecyclerView.Adapter<WordHistoryAdapter.WordViewHolder> {
    private final List<WordResult> wordResults;
    private int lastAnimatedPosition = -1;

    public WordHistoryAdapter(List<WordResult> wordResults) {
        this.wordResults = wordResults;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_word_history, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, @SuppressLint("RecyclerView") int position) {
        WordResult result = wordResults.get(position);
        holder.bind(result);
        
        // Animate item if it hasn't been animated before
        if (position > lastAnimatedPosition) {
            holder.itemView.setTranslationY(100f);
            holder.itemView.setAlpha(0f);
            holder.itemView.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(300)
                    .setStartDelay(position * 100L)
                    .start();
            lastAnimatedPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return wordResults.size();
    }

    static class WordViewHolder extends RecyclerView.ViewHolder {
        private final ImageView statusIcon;
        private final TextView wordText;
        private final TextView timeText;

        WordViewHolder(@NonNull View itemView) {
            super(itemView);
            statusIcon = itemView.findViewById(R.id.statusIcon);
            wordText = itemView.findViewById(R.id.wordText);
            timeText = itemView.findViewById(R.id.timeText);

            // Add ripple effect
            itemView.setClickable(true);
            itemView.setFocusable(true);
        }

        void bind(WordResult result) {
            // Set word text and color
            wordText.setText(result.getWord());
            int textColor = result.isCorrect() ? 
                    ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark) :
                    ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark);
            wordText.setTextColor(textColor);

            // Set status icon
            statusIcon.setImageResource(result.isCorrect() ? 
                    R.drawable.ic_check_circle : R.drawable.ic_close_circle);
            statusIcon.setColorFilter(textColor);

            // Format and set time
            long seconds = TimeUnit.MILLISECONDS.toSeconds(result.getTimeTaken());
            timeText.setText(String.format(Locale.getDefault(), "%ds", seconds));

            // Add press animation
            itemView.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        v.animate()
                                .scaleX(0.95f)
                                .scaleY(0.95f)
                                .setDuration(100)
                                .start();
                        break;
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();
                        break;
                }
                return false;
            });
        }
    }
} 