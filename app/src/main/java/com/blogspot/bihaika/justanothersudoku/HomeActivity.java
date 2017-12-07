package com.blogspot.bihaika.justanothersudoku;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;

import com.bumptech.glide.Glide;

public class HomeActivity extends Activity {

    private FrameLayout mHomeFrame;
    private Switch mSwcNightMode;
    private ImageView mImgAppLogo;
    private Button mBtnEasy;
    private Button mBtnMedium;
    private Button mBtnHard;
    private Button mBtnCustom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mHomeFrame = findViewById(R.id.homeFrame);
        mSwcNightMode = findViewById(R.id.homeSwcNightMode);
        mImgAppLogo = findViewById(R.id.homeImgAppLogo);
        mBtnEasy = findViewById(R.id.homeBtnEasy);
        mBtnMedium = findViewById(R.id.homeBtnMedium);
        mBtnHard = findViewById(R.id.homeBtnHard);
        mBtnCustom = findViewById(R.id.homeBtnCustom);

        final View rootView = mHomeFrame.getRootView();
        final ViewTreeObserver observer = rootView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int h = rootView.getHeight();
                int w = rootView.getWidth();
                int margins = getResources().getDimensionPixelSize(R.dimen.board_view_margin) * 2;
                int boardSize = (w < h) ? w : h;
                int cellSize = (boardSize - (2 * margins)) / 9;
                if (cellSize != DataManager.getInstance(getApplicationContext()).getBitmapSize()) {
                    DataManager.getInstance(getApplicationContext()).setBitmapSize(cellSize);
                }
                rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        DataManager.getInstance(getApplicationContext()).readImage();

        mSwcNightMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DataManager.getInstance(getApplicationContext()).setIsNightMode(isChecked);
                setAppearance();
            }
        });

        setButtonAction(mBtnEasy, Board.DIFFICULTY.EASY);
        setButtonAction(mBtnMedium, Board.DIFFICULTY.MEDIUM);
        setButtonAction(mBtnHard, Board.DIFFICULTY.HARD);
        setButtonAction(mBtnCustom, Board.DIFFICULTY.CUSTOM);

        setAppearance();
    }

    private void setAppearance() {
        int textColor;
        int backgroundColor;
        int imgLogo;

        mSwcNightMode.setChecked(DataManager.getInstance(this).isNightMode());
        if (mSwcNightMode.isChecked()) {
            textColor = R.color.night_text_color;
            backgroundColor = R.color.night_background;
            imgLogo = R.drawable.wtitle;
        } else {
            textColor = R.color.day_text_color;
            backgroundColor = R.color.day_background;
            imgLogo = R.drawable.btitle;
        }

        Glide.with(this)
                .asBitmap()
                .load(imgLogo)
                .into(mImgAppLogo);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mHomeFrame.setBackgroundColor(getResources().getColor(backgroundColor, null));
            mSwcNightMode.setTextColor(getResources().getColor(textColor, null));
            mBtnEasy.setTextColor(getResources().getColor(textColor, null));
            mBtnMedium.setTextColor(getResources().getColor(textColor, null));
            mBtnHard.setTextColor(getResources().getColor(textColor, null));
            mBtnCustom.setTextColor(getResources().getColor(textColor, null));
        } else {
            mHomeFrame.setBackgroundColor(getResources().getColor(backgroundColor));
            mSwcNightMode.setTextColor(getResources().getColor(textColor));
            mBtnEasy.setTextColor(getResources().getColor(textColor));
            mBtnMedium.setTextColor(getResources().getColor(textColor));
            mBtnHard.setTextColor(getResources().getColor(textColor));
            mBtnCustom.setTextColor(getResources().getColor(textColor));
        }

    }

    private void setButtonAction(Button button, final Board.DIFFICULTY difficulty) {
        final Context activity = this;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (int i = 0; i < 10 && !DataManager.getInstance(getApplicationContext()).isReady(); i++) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {

                    }
                }

                String boardSeed = DataManager.getInstance(getApplicationContext()).getBoardSeed(difficulty);
                Intent intent = new Intent(activity, BoardActivity.class);
                intent.putExtra(getString(R.string.tag_board_difficulty), difficulty);
                intent.putExtra(getString(R.string.tag_board_seed), boardSeed);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setAppearance();
    }
}
