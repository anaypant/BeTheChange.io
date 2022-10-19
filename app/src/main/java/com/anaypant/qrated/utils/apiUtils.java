package com.anaypant.qrated.utils;

import androidx.annotation.NonNull;

import com.anaypant.qrated.Frames.ModelNews;
import com.anaypant.qrated.Frames.TrendingNews;
import com.anaypant.qrated.Interfaces.apiInterface;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class apiUtils {
    private static Retrofit retrofit=null;
    private static final int numPages = 32;

    public static String getApiKey(){
        String[] apiKeys = new String[]{"d9c1ce9082704e27bb1d4def64559eaa","2a2429ecaaa4496680cf6d23b9e8dc0a","62243ef8f86c46b8bb8e62c3b87088c6"};
        return apiKeys[com.anaypant.qrated.utils.baseUtils.getRandomNumber(0, apiKeys.length)];
        //24apant : d9c1ce9082704e27bb1d4def64559eaa
        //anaypant212 : 2a2429ecaaa4496680cf6d23b9e8dc0a
        //Throwaway : 62243ef8f86c46b8bb8e62c3b87088c6
    }


    public static void getNewsFromCategory(String category, String apiKey){
        if ("TrendingNews".equals(category)) {
            getApiInterface().getNews("us", numPages, apiKey).enqueue(new Callback<TrendingNews>() {
                @Override
                public void onResponse(@NonNull Call<TrendingNews> call, @NonNull Response<TrendingNews> response) {
                    ArrayList<ModelNews> news;
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            news = response.body().getArticles();
                        } else {
                            news = new ArrayList<>();
                        }
                        for(int z = 0; z < news.size(); z++){
                            ModelNews temp = news.get(z);
                            if(Objects.equals(temp.getTitle(), null) || Objects.equals(temp.getDescription(), null) || Objects.equals(temp.getUrlToImage(), null) || Objects.equals(temp.getTitle(), "") || Objects.equals(temp.getDescription(), "")){
                                news.remove(z);
                                z-=1;
                            }
                        }
                        for (int x = 0; x < news.size(); x++) {
                            news.get(x).setUpVoteCt("0");
                            news.get(x).setDownVoteCt("0");
                            news.get(x).setCommentCt("0");
                        }
//                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("articles");
//                        ref.child("TrendingNews").setValue(news);
                        for(int x = 0; x < 5;x++){
                            HashMap<String, Object> mainMap = new HashMap<>();
                            ModelNews n = news.get(x);
                            mainMap.put("author", n.getAuthor() + "   " + n.getPublishedAt());
                            mainMap.put("description", n.getDescription());
                            mainMap.put("title", n.getTitle());
                            mainMap.put("url", n.getUrl());
                            mainMap.put("imageUrl", n.getUrlToImage());
                            mainMap.put("publishedAt", n.getPublishedAt());
                            mainMap.put("upVoteCt", n.getUpVoteCt());
                            mainMap.put("downVoteCt", n.getDownVoteCt());
                            mainMap.put("commentCt", n.getCommentCt());
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            if(x < 10){
                                db.collection(category).document("0"+x).set(mainMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                    }
                                });

                            }
                            else{
                                db.collection(category).document(""+x).set(mainMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                    }
                                });
                            }
                        }

                    }
                }

                @Override
                public void onFailure(@NonNull Call<TrendingNews> call, @NonNull Throwable t) {

                }
            });
        }
        else{
            String[] keys = getKeyWords(category);
            if(keys == null){
                return;
            }
            for(String key: keys){
                getApiInterface().getKeywordNews(numPages, key, apiKey).enqueue(new Callback<TrendingNews>() {
                    @Override
                    public void onResponse(@NonNull Call<TrendingNews> call, @NonNull Response<TrendingNews> response) {
                        ArrayList<ModelNews> news;
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                news = response.body().getArticles();
                            } else {
                                news = new ArrayList<>();
                            }
                            for (int x = 0; x < news.size(); x++) {
                                news.get(x).setUpVoteCt("0");
                                news.get(x).setDownVoteCt("0");
                                news.get(x).setCommentCt("0");
                            }
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("articles");
                            ref.child(category).setValue(news);


                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<TrendingNews> call, @NonNull Throwable t) {

                    }
                });
            }
        }
    }


    private static String[] getKeyWords(String e){
        String[] econKeys = new String[]{"+Economy", "+bitcoin", "+crypto", "+wall street"};
        String[] socKeys = new String[]{"+sexism","+LGBTQ","+abortion","+Abortion","+racism","+affirmative action","+Antifa","Affordable Care Act","+covid","+filibuster","+gerrymandering","+voter fraud","+immigration"};
        String[] environmentKeys = new String[]{"+climate", "+climate change", "+health", "+pollution", "+ems", "+global warming", "+green", "+epa", "+sustainability", "+health"};
        switch (e) {
            case "EconomyNews":
                return econKeys;
            case "SocietyNews":
                return socKeys;
            case "EnvironmentNews":
                return environmentKeys;
            default:
                return null;
        }
    }

    private static apiInterface getApiInterface(){
        if(retrofit == null){
            retrofit = new Retrofit.Builder().baseUrl(apiInterface.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        }
        return retrofit.create(apiInterface.class);
    }
}
