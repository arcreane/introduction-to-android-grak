package com.example.headsup.managers;

import android.util.Log;

import com.example.headsup.models.CardResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import api.CardService;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class APIManager {
    private final String BASE_URL = "https://api.jsonbin.io/";


    // had to change func to be async since i was getting a NetworkOnMainThreadException
    public void setupRetrofitAsync(String binId, Callback callback) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            ArrayList<String> words = setupRetrofit(binId);
            callback.onResult(words);
        });
    }

    // callback for above async function
    public interface Callback {
        void onResult(ArrayList<String> words);
    }


    // retrofit api call using binid to json bin online
    public ArrayList<String> setupRetrofit(String binId) {
        ArrayList<String> words = new ArrayList<>();

        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            // using service mentioned in api folder
            CardService service = retrofit.create(CardService.class);
            Response<CardResponse> response = service.getCards(binId).execute();

            // error handling for api call and using default words in case of failure
            if (response.isSuccessful() && response.body() != null) {
                List<String> cards = response.body().getRecord().getCards();
                words = new ArrayList<>(cards);
                Collections.shuffle(words);
                return words;
            }
        } catch (Exception e) {
            Log.e("Fetch error", "API call failed", e);
        }

        return initializeWords();
    }

    // default word list
    private ArrayList<String> initializeWords() {
        ArrayList<String> words = new ArrayList<>();
        words.add("ELEPHANT");
        words.add("DANCE");
        words.add("YOGA");
        words.add("DANCING");
        Collections.shuffle(words);
        return words;
    }
}