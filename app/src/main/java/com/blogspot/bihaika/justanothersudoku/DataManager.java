package com.blogspot.bihaika.justanothersudoku;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Baihaki Dwi on 30/11/2017.
 */

public class DataManager {

    private static DataManager mDataManager;

    private Context mAppContext;

    private int mBitmapSize;
    private boolean mIsNightMode;
    private AdRequest mAdRequest;

    private HashMap<Boolean, HashMap<Integer, Bitmap>> mImgSets;

    private DataManager(Context context) {
        mAppContext = context.getApplicationContext();
        load();
        mImgSets = new HashMap<>();
        mImgSets.put(true, new HashMap<Integer, Bitmap>());
        mImgSets.put(false, new HashMap<Integer, Bitmap>());
        MobileAds.initialize(mAppContext, mAppContext.getString(R.string.ads_app_id));
        mAdRequest = new AdRequest.Builder().build();
    }

    public static DataManager getInstance(Context context) {
        if (mDataManager == null) {
            mDataManager = new DataManager(context);
        }
        return mDataManager;
    }

    public boolean isReady() {
        return (mImgSets.get(true).size() == Board.BOARD_SIZE && mImgSets.get(false).size() == Board.BOARD_SIZE);
    }

    public int getBitmapSize() {
        return mBitmapSize;
    }

    public void setBitmapSize(int bitmapSize) {
        mBitmapSize = bitmapSize;
        save();
    }

    public boolean isNightMode() {
        return mIsNightMode;
    }

    public void setIsNightMode(boolean isNightMode) {
        mIsNightMode = isNightMode;
        save();
    }

    public void readImage() {

        Bitmap bitmap = mImgSets.get(true).get(Board.BOARD_SIZE - 1);
        if (bitmap != null && isReady()) {
            if (bitmap.getHeight() == mBitmapSize) {
                return;
            }
        }

        int[][] imgIds = {
                {R.drawable.b1, R.drawable.b2, R.drawable.b3
                        , R.drawable.b4, R.drawable.b5, R.drawable.b6
                        , R.drawable.b7, R.drawable.b8, R.drawable.b9}
                ,
                {R.drawable.w1, R.drawable.w2, R.drawable.w3
                        , R.drawable.w4, R.drawable.w5, R.drawable.w6
                        , R.drawable.w7, R.drawable.w8, R.drawable.w9}};

        for (int nightMode = 0; nightMode < imgIds.length; nightMode++) {
            final boolean isNightMode = (nightMode == 1);
            for (int id = 0; id < imgIds[nightMode].length; id++) {
                final int finalId = id;
                Glide.with(mAppContext)
                        .asBitmap()
                        .load(imgIds[nightMode][id])
                        .into(new SimpleTarget<Bitmap>(mBitmapSize, mBitmapSize) {
                            @Override
                            public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                                mImgSets.get(isNightMode).put(finalId, bitmap);
                            }
                        });
            }
        }
    }

    public Bitmap getBitmap(int key) {
        if (mImgSets.get(mIsNightMode).isEmpty()) {
            readImage();
        }
        return (Bitmap) mImgSets.get(mIsNightMode).get(key);
    }

    public void save() {
        SharedPreferences sharedPreferences = mAppContext.getSharedPreferences(
                mAppContext.getString(R.string.app_shared_preferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(mAppContext.getString(R.string.app_preferences_bitmap_size), mBitmapSize);
        editor.putBoolean(mAppContext.getString(R.string.app_preferences_is_night_mode), mIsNightMode);

        editor.apply();
    }

    public void load() {
        SharedPreferences sharedPreferences = mAppContext.getSharedPreferences(
                mAppContext.getString(R.string.app_shared_preferences), Context.MODE_PRIVATE);
        mBitmapSize = sharedPreferences.getInt(
                mAppContext.getString(R.string.app_preferences_bitmap_size), 64);
        mIsNightMode = sharedPreferences.getBoolean(
                mAppContext.getString(R.string.app_preferences_is_night_mode), false);
    }

    public String getBoardSeed(Board.DIFFICULTY difficulty) {
        String seed = "";
        Random random = new Random(System.currentTimeMillis());
        int id = random.nextInt(mAppContext.getResources().getInteger(R.integer.number_of_seeds));
        BufferedReader reader = null;
        switch (difficulty) {
            case EASY:
                reader = new BufferedReader(new InputStreamReader(
                        mAppContext.getResources().openRawResource(R.raw.easy)));
                break;
            case MEDIUM:
                reader = new BufferedReader(new InputStreamReader(
                        mAppContext.getResources().openRawResource(R.raw.medium)));
                break;
            case HARD:
                reader = new BufferedReader(new InputStreamReader(
                        mAppContext.getResources().openRawResource(R.raw.hard)));
                break;
            case CUSTOM:
                return "";
        }
        do {
            try {
                seed = reader.readLine();
            } catch (IOException e) {
                return "2;1;8;7;5;6;3;4;9;" +
                        "9;6;3;4;2;8;1;7;5;" +
                        "4;5;7;1;3;9;8;2;6;" +
                        "6;3;5;8;7;2;4;9;1;" +
                        "1;8;4;6;9;5;2;3;7;" +
                        "7;9;2;3;4;1;5;6;8;" +
                        "8;7;1;2;6;4;9;5;3;" +
                        "5;4;6;9;1;3;7;8;2;" +
                        "3;2;9;5;8;7;6;1;4;";
            }
            id--;
        } while (id > 0);
        return seed;
    }

    public AdRequest getAdRequest() {
        return mAdRequest;
    }
}
