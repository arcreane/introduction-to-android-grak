package com.example.headsup.activities;

//Import statements
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.headsup.models.Deck;
import com.example.headsup.adapters.DeckAdapter;
import com.example.headsup.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
//    private variables

    private RecyclerView recyclerView; // recyclerview for the deck cards
    private DeckAdapter adapter; // adapter for managing deck view, onclick etc
    private FrameLayout animationContainer; // container for zoom in flip animation
    private View dimBackground; // background view for the above animation
    private androidx.cardview.widget.CardView animatedCard; // card used for the onclick animation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize deck recyclerView
        recyclerView = findViewById(R.id.deckRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // initialize animation views
        animationContainer = findViewById(R.id.animationContainer);
        dimBackground = findViewById(R.id.dimBackground);
        animatedCard = findViewById(R.id.animatedCard);

        // register card decks using id, title, desc, image and binId
        List<Deck> decks = new ArrayList<>();
        decks.add(new Deck(1, getString(R.string.deck1_title),getString(R.string.deck1_desc), R.drawable.pop_image, "67827a64e41b4d34e475c1e9"));
        decks.add(new Deck(2, getString(R.string.deck2_title), getString(R.string.deck2_desc),R.drawable.mime, "67827a6aacd3cb34a8c9fc36"));
        decks.add(new Deck(3, getString(R.string.deck3_title), getString(R.string.deck3_desc), R.drawable.animals, "67850066acd3cb34a8cae7be"));
        decks.add(new Deck(4, getString(R.string.deck4_title), getString(R.string.deck4_desc), R.drawable.celebrities, "67850043e41b4d34e476ad2e"));

        // initialize adapter with onclick animation for each deck
        adapter = new DeckAdapter(decks, new DeckAdapter.OnDeckClickListener() {
            @Override
            public void onDeckClick(Deck deck, View cardView) {
                showZoomInAnimation(deck, cardView);
            }
        });

        recyclerView.setAdapter(adapter);

        // Video background setup
        setupVideoBackground();
    }

    private void setupVideoBackground() {
        VideoView videoView = findViewById(R.id.videoView);

        // Try to get the last saved video
        File headsUpDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES), "HeadsUp");

        Uri videoUri = null;
        if (headsUpDir.exists() && headsUpDir.isDirectory()) {
            File[] files = headsUpDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp4"));
            if (files != null && files.length > 0) {
                // Sort files by last modified time to get the most recent one
                Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                videoUri = Uri.fromFile(files[0]);
            }
        }

        // If no saved video found, use the default one
        if (videoUri == null) {
            videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.memories2);
        }

        videoView.setVideoURI(videoUri);
        videoView.start();

        // here just looping video and muting
        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            mp.setVolume(0, 0);  // This line mutes the video (left and right channels)
        });

        // Handle video errors
        videoView.setOnErrorListener((mp, what, extra) -> {
            // If there's an error with the saved video, fall back to the default one
            Uri defaultUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.memories2);
            videoView.setVideoURI(defaultUri);
            videoView.start();
            return true;
        });
    }

    // important to resume video again when user reenters activity
    @Override
    protected void onResume() {
        super.onResume();
        VideoView videoView = findViewById(R.id.videoView);
        if (!videoView.isPlaying()) {
            setupVideoBackground();
        }
    }

    // important to pause video when activity has been left (but still in background)
    @Override
    protected void onPause() {
        super.onPause();
        VideoView videoView = findViewById(R.id.videoView);
        if (videoView.isPlaying()) {
            videoView.pause();
        }
    }

    // zoom in animation for onclick in deck
    private void showZoomInAnimation(final Deck deck, View cardView) {
        try {
            // make animation container visible
            animationContainer.setVisibility(View.VISIBLE);
            dimBackground.setVisibility(View.VISIBLE);
            animatedCard.setVisibility(View.VISIBLE);

            // then get screen dimensions
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int screenHeight = getResources().getDisplayMetrics().heightPixels;

            // get clicked card location to start zoom in
            int[] cardLocation = new int[2];
            cardView.getLocationInWindow(cardLocation);

            // calculate center position
            int targetX = (screenWidth - cardView.getWidth()) / 2;
            int targetY = (screenHeight - cardView.getHeight()) / 2;

            // set initial position of animated card
            animatedCard.setX(cardLocation[0]);
            animatedCard.setY(cardLocation[1]);

            // Set card size
            int cardWidth = cardView.getWidth();
            int cardHeight = cardView.getHeight();
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(cardWidth, cardHeight);
            animatedCard.setLayoutParams(params);

            // set initial front card content to that of the card clicked
            animatedCard.removeAllViews();
            View frontContent = getLayoutInflater().inflate(R.layout.item_card, animatedCard, false);
            ImageView imageView = frontContent.findViewById(R.id.deckImage);
            TextView titleView = frontContent.findViewById(R.id.deckTitle);
            imageView.setImageResource(deck.getImageResId());
            titleView.setText(deck.getTitle());
            animatedCard.addView(frontContent);

            // animate the card to the center of the screen
            animatedCard.animate()
                    .x(targetX)
                    .y(targetY)
                    .scaleX(2f)
                    .scaleY(2.5f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        // then initiate the flip animation
                        startFlipAnimation(deck);

                        // clicking on the background closes the card
                        dimBackground.setOnClickListener(v -> {
                            animatedCard.animate()
                                    .x(cardLocation[0])
                                    .y(cardLocation[1])
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(200)
                                    .withEndAction(() -> {
                                        animationContainer.setVisibility(View.INVISIBLE);
                                        dimBackground.setVisibility(View.INVISIBLE);
                                        animatedCard.setVisibility(View.INVISIBLE);
                                        dimBackground.setAlpha(0f);
                                        // Reset rotation
                                        animatedCard.setRotationY(0f);
                                    })
                                    .start();
                        });
                    })
                    .start();

            // Animate background dim
            dimBackground.animate()
                    .alpha(0.8f)
                    .setDuration(300)
                    .start();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Animation error", Toast.LENGTH_SHORT).show();
        }
    }

    private void startFlipAnimation(Deck deck) {
        AnimatorSet flipAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(MainActivity.this, R.animator.flip_card);

        // Set camera distance
        float scale = getResources().getDisplayMetrics().density * 8000;
        animatedCard.setCameraDistance(scale);

        final boolean[] isFlipped = {false};

        ((ObjectAnimator) flipAnimation.getChildAnimations().get(0)).addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                if (value >= 90f && !isFlipped[0]) {
                    isFlipped[0] = true;
                    // When card is perpendicular to screen (at 90 degrees), switch the content
                    animatedCard.removeAllViews();
                    View backContent = getLayoutInflater().inflate(R.layout.item_card_back, animatedCard, false);

                    backContent.setRotationY(180f);

                    TextView backTitle = backContent.findViewById(R.id.backCardTitle);
                    TextView backDesc = backContent.findViewById(R.id.backCardDescription);
                    Button actionButton = backContent.findViewById(R.id.backCardActionButton);

                    backTitle.setText(deck.getTitle());
                    backDesc.setText(deck.getDesc());

                    actionButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Navigate to the target activity
                            Intent intent = new Intent(MainActivity.this, GameActivity.class);
                            intent.putExtra("binId", deck.getBinId());
                            startActivity(intent);
                        }
                    });

                    animatedCard.addView(backContent);
                }
            }
        });

        flipAnimation.setTarget(animatedCard);
        flipAnimation.start();
    }

}