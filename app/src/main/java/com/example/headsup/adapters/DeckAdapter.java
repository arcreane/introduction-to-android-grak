package com.example.headsup.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.headsup.models.Deck;
import com.example.headsup.R;

import java.util.List;


// this adapter is used to manage the multiple decks on the home page
public class DeckAdapter extends RecyclerView.Adapter<DeckAdapter.DeckViewHolder> {
    private final List<Deck> decks; // refers to the list of decks
    private final OnDeckClickListener listener; // listener for onclicking the deck
    private View lastClickedView;

    public interface OnDeckClickListener {
        void onDeckClick(Deck deck, View cardView);
    }

    public DeckAdapter(List<Deck> decks, OnDeckClickListener listener) {
        this.decks = decks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);
        return new DeckViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeckViewHolder holder, int position) {
        Deck deck = decks.get(position);
        holder.deckImage.setImageResource(deck.getImageResId());
        holder.deckTitle.setText(deck.getTitle());

        holder.cardView.setOnClickListener(v -> {
            lastClickedView = holder.cardView;
            listener.onDeckClick(deck, holder.cardView);
        });
    }

    @Override
    public int getItemCount() {
        return decks.size();
    }

    public View getLastClickedView() {
        return lastClickedView;
    }

    public static class DeckViewHolder extends RecyclerView.ViewHolder {
        final CardView cardView;
        final ImageView deckImage;
        final TextView deckTitle;

        DeckViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            deckImage = itemView.findViewById(R.id.deckImage);
            deckTitle = itemView.findViewById(R.id.deckTitle);
        }
    }
}
