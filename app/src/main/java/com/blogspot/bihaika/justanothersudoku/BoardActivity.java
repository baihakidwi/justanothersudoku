package com.blogspot.bihaika.justanothersudoku;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;

import java.util.HashMap;

public class BoardActivity extends Activity {

    FrameLayout mActivityFrame;
    LinearLayout mPlayFrame;
    LinearLayout mPauseFrame;
    BoardView mBoardView;
    HashMap<Integer, Button> mInputButtons;
    Button mBtnMark;
    Button mBtnErase;
    Chronometer mChronometer;
    TableRow mRowInput;
    TableRow mRowFinishEdit;
    Button mBtnFinishEdit;
    Switch mSwcNightMode;
    TextView mTextView;
    Button mBtnResume;
    Button mBtnReset;
    Button mBtnQuit;
    Button mBtnSolve;
    AdView mAdView;
    boolean mIsPaused;
    MODE mMode;
    long mChronometerBase;
    boolean mIsComplete;
    boolean mIsCustomMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        findView();

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            Board.DIFFICULTY difficulty = (Board.DIFFICULTY) intent.getSerializableExtra(getString(R.string.tag_board_difficulty));
            String boardSeed = intent.getStringExtra(getString(R.string.tag_board_seed));
            Board board = new Board(boardSeed);
            mBoardView.setBoard(board);

            mIsPaused = false;
            mMode = MODE.PEN;
            mChronometerBase = 0;
            mIsComplete = false;

            if (difficulty == Board.DIFFICULTY.CUSTOM) {
                mIsCustomMode = true;
            }

        } else {
            Board board = (Board) savedInstanceState.getSerializable(getString(R.string.board_state_board));
            mBoardView.setBoard(board);
            mIsPaused = savedInstanceState.getBoolean(getString(R.string.board_state_is_paused), false);
            mMode = (MODE) savedInstanceState.getSerializable(getString(R.string.board_state_input_mode));
            mChronometerBase = savedInstanceState.getLong(getString(R.string.board_state_chronometer_time_base), 0);
            mIsComplete = savedInstanceState.getBoolean(getString(R.string.board_state_is_complete), false);
            mIsCustomMode = savedInstanceState.getBoolean(getString(R.string.board_state_is_custom_mode), false);
            int selectedCell = savedInstanceState.getInt(getString(R.string.board_state_selected_cell));
            if (selectedCell != -1) {
                mBoardView.selectCell(selectedCell / Board.BOARD_SIZE, selectedCell % Board.BOARD_SIZE);
            }
        }

        setActions();

        setAppearance();

        if (mIsPaused) {
            pause();
        } else {
            resume();
        }
    }

    private void findView() {
        mActivityFrame = findViewById(R.id.boardActivityFrame);
        mPlayFrame = findViewById(R.id.playFrame);
        mPauseFrame = findViewById(R.id.pauseFrame);

        mBoardView = findViewById(R.id.boardView);

        if (mInputButtons == null) {
            mInputButtons = new HashMap<>();
        }
        mInputButtons.clear();
        int[] btnIds = {R.id.playBtn1, R.id.playBtn2, R.id.playBtn3, R.id.playBtn4, R.id.playBtn5
                , R.id.playBtn6, R.id.playBtn7, R.id.playBtn8, R.id.playBtn9};
        for (int i = 0; i < btnIds.length; i++) {
            Button inputButton = findViewById(btnIds[i]);
            mInputButtons.put(i, inputButton);
        }
        mBtnMark = findViewById(R.id.playBtnMark);
        mBtnErase = findViewById(R.id.playBtnErase);
        mChronometer = findViewById(R.id.chronometer);

        mRowInput = findViewById(R.id.rowBtnInput);
        mRowFinishEdit = findViewById(R.id.rowBtnFinish);
        mBtnFinishEdit = findViewById(R.id.playBtnFinish);

        mSwcNightMode = findViewById(R.id.pauseSwcNightMode);
        mTextView = findViewById(R.id.pauseTxtView);
        mBtnResume = findViewById(R.id.pauseBtnResume);
        mBtnReset = findViewById(R.id.pauseBtnReset);
        mBtnQuit = findViewById(R.id.pauseBtnQuit);
        mBtnSolve = findViewById(R.id.pauseBtnSolve);

        mAdView = findViewById(R.id.adView);
    }

    private void setActions() {
        BoardViewTouchListener listener = new BoardViewTouchListener(this);
        mBoardView.setOnTouchListener(listener);
        mBoardView.setOnClickListener(listener);

        for (int id = 0; id < mInputButtons.size(); id++) {
            Button button = mInputButtons.get(id);
            final int finalId = id;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (mMode) {
                        case PENCIL:
                            mBoardView.markValue(finalId + 1);
                            break;
                        case ERASER:
                            mMode = MODE.PEN;
                            mBtnErase.getBackground().clearColorFilter();
                        case PEN:
                            mBoardView.setValue(finalId + 1);
                            if (mBoardView.validateBoard() == Board.BOARD_STATUS.COMPLETE && !mIsComplete) {
                                mIsComplete = true;
                                mChronometerBase = mChronometer.getBase() - SystemClock.elapsedRealtime();
                                pause();
                            }
                            break;
                    }
                }
            });
        }

        mBtnMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int highlightColorId;
                if (DataManager.getInstance(getApplicationContext()).isNightMode()) {
                    highlightColorId = R.color.night_highlight;
                } else {
                    highlightColorId = R.color.day_highlight;
                }
                int highlightColor;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    highlightColor = getResources().getColor(highlightColorId, null);
                } else {
                    highlightColor = getResources().getColor(highlightColorId);
                }
                switch (mMode) {
                    case PEN:
                    case ERASER:
                        mMode = MODE.PENCIL;
                        mBtnMark.getBackground().setColorFilter(highlightColor, PorterDuff.Mode.ADD);
                        mBtnErase.getBackground().clearColorFilter();
                        break;
                    case PENCIL:
                        mMode = MODE.PEN;
                        mBtnMark.getBackground().clearColorFilter();
                }
            }
        });

        mBtnErase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int highlightColorId;
                if (DataManager.getInstance(getApplicationContext()).isNightMode()) {
                    highlightColorId = R.color.night_highlight;
                } else {
                    highlightColorId = R.color.day_highlight;
                }
                int highlightColor;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    highlightColor = getResources().getColor(highlightColorId, null);
                } else {
                    highlightColor = getResources().getColor(highlightColorId);
                }
                switch (mMode) {
                    case PEN:
                    case PENCIL:
                        mMode = MODE.ERASER;
                        mBtnMark.getBackground().clearColorFilter();
                        mBtnErase.getBackground().setColorFilter(highlightColor, PorterDuff.Mode.ADD);
                        mBoardView.clearCell();
                        break;
                    case ERASER:
                        mMode = MODE.PEN;
                        mBtnErase.getBackground().clearColorFilter();
                }
            }
        });

        mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                long time = SystemClock.elapsedRealtime() - chronometer.getBase();
                int h = (int) (time / 3600000) % 99;
                int m = (int) (time - h * 3600000) / 60000;
                int s = (int) (time - h * 3600000 - m * 60000) / 1000;
                String hh = h == 0 ? "" : (h < 10 ? "0" + h + ":" : h + ":");
                String mm = m < 10 ? "0" + m + ":" : m + ":";
                String ss = s < 10 ? "0" + s : s + "";
                chronometer.setText(hh + mm + ss);
            }
        });

        mBtnFinishEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsCustomMode = false;
                mBoardView.lockBoard();
                resume();
            }
        });

        mSwcNightMode.setChecked(DataManager.getInstance(this).isNightMode());
        mSwcNightMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DataManager.getInstance(getApplicationContext()).setIsNightMode(isChecked);
                mBoardView.invalidate();
                setAppearance();
            }
        });

        mBtnResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resume();
            }
        });

        mBtnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBoardView.resetBoard();
                mIsComplete = false;
                resume();
            }
        });

        mBtnQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mBtnSolve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Board duplicate = mBoardView.getBoard().duplicate();
                new AsyncTaskBoardSolver().execute(duplicate);
            }
        });

        mAdView.loadAd(DataManager.getInstance(this).getAdRequest());
    }

    private void setAppearance() {
        int textColorId;
        int backgroundColorId;
        int buttonBackgroundId;
        int highlightColorId;
        int textColor;
        int backgroundColor;
        int highlightColor;

        if (DataManager.getInstance(this).isNightMode()) {
            textColorId = R.color.night_text_color;
            backgroundColorId = R.color.night_background;
            buttonBackgroundId = R.drawable.button_background_night;
            highlightColorId = R.color.night_highlight;
        } else {
            textColorId = R.color.day_text_color;
            backgroundColorId = R.color.day_background;
            buttonBackgroundId = R.drawable.button_background_day;
            highlightColorId = R.color.day_highlight;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textColor = getResources().getColor(textColorId, null);
            backgroundColor = getResources().getColor(backgroundColorId, null);
            highlightColor = getResources().getColor(highlightColorId, null);

            for (int i = 0; i < Board.BOARD_SIZE; i++) {
                Button button = mInputButtons.get(i);
                button.setTextColor(textColor);
                button.setBackground(getResources().getDrawable(buttonBackgroundId, null));
            }
            mBtnMark.setBackground(getResources().getDrawable(buttonBackgroundId, null));
            mBtnErase.setBackground(getResources().getDrawable(buttonBackgroundId, null));
        } else {
            textColor = getResources().getColor(textColorId);
            backgroundColor = getResources().getColor(backgroundColorId);
            highlightColor = getResources().getColor(highlightColorId);

            for (int i = 0; i < Board.BOARD_SIZE; i++) {
                Button button = mInputButtons.get(i);
                button.setTextColor(textColor);
                button.setBackground(getResources().getDrawable(buttonBackgroundId));
            }
            mBtnMark.setBackground(getResources().getDrawable(buttonBackgroundId));
            mBtnErase.setBackground(getResources().getDrawable(buttonBackgroundId));
        }

        mActivityFrame.setBackgroundColor(backgroundColor);

        mBtnMark.setTextColor(textColor);

        mBtnErase.setTextColor(textColor);

        mChronometer.setTextColor(textColor);

        mSwcNightMode.setTextColor(textColor);

        mTextView.setTextColor(textColor);

        mBtnResume.setTextColor(textColor);

        mBtnReset.setTextColor(textColor);

        mBtnQuit.setTextColor(textColor);

        mBtnSolve.setTextColor(textColor);

        mBtnFinishEdit.setTextColor(textColor);

        switch (mMode) {
            case PENCIL:
                mBtnMark.getBackground().setColorFilter(highlightColor, PorterDuff.Mode.ADD);
                break;
            case ERASER:
                mBtnErase.getBackground().setColorFilter(highlightColor, PorterDuff.Mode.ADD);
                break;
        }
    }

    private void pause() {
        mIsPaused = true;

        mChronometer.stop();

        mPlayFrame.setVisibility(View.GONE);
        mPauseFrame.setVisibility(View.VISIBLE);
        {
            for (int id = 0; id < Board.BOARD_SIZE; id++) {
                Button button = mInputButtons.get(id);
                button.setVisibility(View.GONE);
            }
            mBtnMark.setVisibility(View.GONE);
            mBtnErase.setVisibility(View.GONE);
        }

        mChronometer.stop();
        if (mIsComplete) {
            mTextView.setText(getString(R.string.pause_txv_complete) + " " + mChronometer.getText());
        } else {
            mTextView.setText(getString(R.string.pause_txv_paused));
            mChronometerBase = mChronometer.getBase() - SystemClock.elapsedRealtime();
        }

        {
            for (int id = 0; id < Board.BOARD_SIZE; id++) {
                Button button = mInputButtons.get(id);
                button.setVisibility(View.VISIBLE);
            }
            mBtnMark.setVisibility(View.VISIBLE);
            mBtnErase.setVisibility(View.VISIBLE);
        }

        mBtnSolve.setEnabled(!mIsComplete && !mIsCustomMode);

        mAdView.setVisibility(View.VISIBLE);

        if (mAdView.getTag() == null) {
            mAdView.loadAd(DataManager.getInstance(this).getAdRequest());
        }
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    private void resume() {
        mIsPaused = false;

        mPlayFrame.setVisibility(View.VISIBLE);

        mChronometer.setBase(SystemClock.elapsedRealtime() + mChronometerBase);

        if (mIsCustomMode) {
            mRowInput.setVisibility(View.GONE);
            mRowFinishEdit.setVisibility(View.VISIBLE);
        } else {
            mRowInput.setVisibility(View.VISIBLE);
            mRowFinishEdit.setVisibility(View.GONE);
            if (!mIsComplete) {
                mChronometer.start();
            }
        }
        mPauseFrame.setVisibility(View.GONE);
        mAdView.setVisibility(View.GONE);
        if (mAdView != null) {
            mAdView.pause();
        }

    }

    public void boardViewClicked(int x, int y) {
        int cellSize = DataManager.getInstance(this).getBitmapSize();
        int row = y / cellSize;
        int column = x / cellSize;
        mBoardView.selectCell(row, column);
        if (mMode == MODE.ERASER) {
            mBoardView.clearCell();
        }
    }

    public void submitSolvedBoard(Board board) {
        if (mIsPaused) {
            if (board != null && board.getBoardId() == mBoardView.getBoard().getBoardId()) {
                mBoardView.setBoard(board);
                mBoardView.invalidate();
                mIsComplete = true;
                pause();
                Toast.makeText(this, "Board is solved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Sorry, unable to solve the board.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mIsPaused) {
            resume();
        } else {
            pause();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(getString(R.string.board_state_is_paused), mIsPaused);
        outState.putSerializable(getString(R.string.board_state_board), mBoardView.getBoard());
        outState.putSerializable(getString(R.string.board_state_input_mode), mMode);
        outState.putLong(getString(R.string.board_state_chronometer_time_base), mChronometerBase);
        outState.putBoolean(getString(R.string.board_state_is_complete), mIsComplete);
        outState.putBoolean(getString(R.string.board_state_is_custom_mode), mIsCustomMode);
        outState.putInt(getString(R.string.board_state_selected_cell), mBoardView.getSelectedCell());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            pause();
            if (mAdView != null) {
                mAdView.pause();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    public enum MODE {PEN, PENCIL, ERASER}

    private class AsyncTaskBoardSolver extends AsyncTask<Board, Void, Board> {

        @Override
        protected Board doInBackground(Board... boards) {
            Board origin = boards[0];
            Board editedBoard = origin.duplicate();
            editedBoard.lock();
            if (editedBoard.solveBoard()) {
                for (int row = 0; row < Board.BOARD_SIZE; row++) {
                    for (int column = 0; column < Board.BOARD_SIZE; column++) {
                        if (origin.getValue(row, column) == 0) {
                            origin.setValue(row, column, editedBoard.getValue(row, column));
                        }
                    }
                }
                return origin;
            }
            for (int row = 0; row < Board.BOARD_SIZE; row++) {
                for (int column = 0; column < Board.BOARD_SIZE; column++) {
                    if (!origin.checkLock(row, column)) {
                        origin.clear(row, column);
                    }
                }
            }
            if (origin.solveBoard()) {
                return origin;
            }
            return new Board();
        }

        @Override
        protected void onPostExecute(Board board) {
            submitSolvedBoard(board);
        }
    }

}
