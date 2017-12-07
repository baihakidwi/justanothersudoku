package com.blogspot.bihaika.justanothersudoku;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Baihaki Dwi on 01/12/2017.
 */

class BoardViewTouchListener implements View.OnTouchListener, View.OnClickListener {

    private BoardActivity mBoardActivity;
    private int mx;
    private int my;

    public BoardViewTouchListener(BoardActivity boardActivity) {
        mBoardActivity = boardActivity;
    }

    @Override
    public void onClick(View v) {
        mBoardActivity.boardViewClicked(mx, my);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            mx = (int) event.getX();
            my = (int) event.getY();
        }
        return false;
    }
}
