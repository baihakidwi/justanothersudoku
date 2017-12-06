package com.blogspot.bihaika.justanothersudoku;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Baihaki Dwi on 30/11/2017.
 */

public class BoardView extends View {

    private Context mContext;

    private Board mBoard;
    private int mCellSize;

    private int mSelectedRow;
    private int mSelectedColumn;

    private Paint mLinePainter;
    private Paint mBoldLinePainter;
    private Paint mLockBackgroundPainter;
    private Paint mHighlightPainter;
    private Paint mValuePainter;

    public BoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        mCellSize = DataManager.getInstance(context).getBitmapSize();
        mSelectedRow = -1;
        mSelectedColumn = -1;

    }

    public void setBoard(Board board) {
        mBoard = board;
        invalidate();
    }

    public Board getBoard() {
        return mBoard;
    }

    private void initPainter() {
        int lineColor;
        int lockBackgroundColor;
        int highlightColor;

        if (DataManager.getInstance(mContext).isNightMode()) {
            lineColor = R.color.night_line;
            lockBackgroundColor = R.color.night_lock_background;
            highlightColor = R.color.night_highlight;
        } else {
            lineColor = R.color.day_line;
            lockBackgroundColor = R.color.day_lock_background;
            highlightColor = R.color.day_highlight;
        }

        int lineSize = (mCellSize / 24 > 2) ? 2 : mCellSize / 24;
        int boldLineSize = (mCellSize / 16 > 4) ? 4 : mCellSize / 16;

        mLinePainter = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePainter.setStyle(Paint.Style.STROKE);
        mLinePainter.setStrokeWidth(lineSize);

        mBoldLinePainter = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBoldLinePainter.setStyle(Paint.Style.STROKE);
        mBoldLinePainter.setStrokeWidth(boldLineSize);

        mLockBackgroundPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLockBackgroundPainter.setStyle(Paint.Style.FILL);

        mHighlightPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHighlightPainter.setStyle(Paint.Style.FILL);

        mValuePainter = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mValuePainter.setFilterBitmap(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mLinePainter.setColor(getResources().getColor(lineColor, null));
            mBoldLinePainter.setColor(getResources().getColor(lineColor, null));
            mLockBackgroundPainter.setColor(getResources().getColor(lockBackgroundColor, null));
            mHighlightPainter.setColor(getResources().getColor(highlightColor, null));
        } else {
            mLinePainter.setColor(getResources().getColor(lineColor));
            mBoldLinePainter.setColor(getResources().getColor(lineColor));
            mLockBackgroundPainter.setColor(getResources().getColor(lockBackgroundColor));
            mHighlightPainter.setColor(getResources().getColor(highlightColor));
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mCellSize = DataManager.getInstance(mContext).getBitmapSize();
        int boardSize = 9 * mCellSize;
        setMeasuredDimension(boardSize, boardSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        initPainter();

        if (mBoard != null) {
            for (int row = 0; row < Board.BOARD_SIZE; row++) {
                for (int column = 0; column < Board.BOARD_SIZE; column++) {
                    drawCell(canvas, row, column);
                }
            }
        }

        int boardSize = mCellSize * 9;

        for (int i = 0; i <= Board.BOARD_SIZE; i++) {
            canvas.drawLine(0
                    , i * mCellSize
                    , boardSize
                    , i * mCellSize
                    , mLinePainter);

            canvas.drawLine(i * mCellSize
                    , 0
                    , i * mCellSize
                    , boardSize
                    , mLinePainter);

            if (i % Board.BLOCK_SIZE == 0) {
                canvas.drawLine(0
                        , i * mCellSize
                        , boardSize
                        , i * mCellSize
                        , mBoldLinePainter);

                canvas.drawLine(i * mCellSize
                        , 0
                        , i * mCellSize
                        , boardSize
                        , mBoldLinePainter);
            }
        }
    }

    private void drawCell(Canvas canvas, int row, int column) {
        int top = row * mCellSize;
        int left = column * mCellSize;

        if (mBoard.checkLock(row, column)) {
            canvas.drawRect(left, top, left + mCellSize, top + mCellSize, mLockBackgroundPainter);
        }

        if (row == mSelectedRow && column == mSelectedColumn) {
            canvas.drawRect(left, top, left + mCellSize, top + mCellSize, mHighlightPainter);
        }

        Rect targetSize;
        int cellValue = mBoard.getValue(row, column);
        if (cellValue != 0) {
            Bitmap bmpValue = DataManager.getInstance(mContext).getBitmap(cellValue - 1);
            if (bmpValue != null) {
                targetSize = new Rect(left, top, left + mCellSize, top + mCellSize);
                canvas.drawBitmap(DataManager.getInstance(mContext).getBitmap(cellValue - 1),
                        null, targetSize, mValuePainter);
            }
        } else {
            int markSize = mCellSize / Board.BLOCK_SIZE;
            for (int mark = 1; mark <= Board.BOARD_SIZE; mark++) {
                if (mBoard.checkMark(row, column, mark)) {
                    Bitmap bmpValue = DataManager.getInstance(mContext).getBitmap(mark - 1);
                    if (bmpValue != null) {
                        int markLeft = left + ((mark - 1) % Board.BLOCK_SIZE) * markSize;
                        int markTop = top + ((mark - 1) / Board.BLOCK_SIZE) * markSize;
                        targetSize = new Rect(markLeft, markTop, markLeft + markSize, markTop + markSize);

                        canvas.drawBitmap(DataManager.getInstance(mContext).getBitmap(mark - 1)
                                , null, targetSize, mValuePainter);
                    }
                }
            }
        }
    }

    public void selectCell(int row, int column) {
        if (row == mSelectedRow && column == mSelectedColumn) {
            mSelectedRow = -1;
            mSelectedColumn = -1;
        } else {
            if (row >= 0 && row < Board.BOARD_SIZE) {
                mSelectedRow = row;
            } else {
                mSelectedRow = -1;
            }

            if (column >= 0 && column < Board.BOARD_SIZE) {
                mSelectedColumn = column;
            } else {
                mSelectedColumn = -1;
            }
        }
        invalidate();
    }

    public int getSelectedCell() {
        if (mSelectedColumn == -1 || mSelectedRow == -1) {
            return -1;
        }
        return mSelectedRow * Board.BOARD_SIZE + mSelectedColumn;
    }

    public void setValue(int value) {
        if (mSelectedRow >= 0 && mSelectedRow < Board.BOARD_SIZE
                && mSelectedColumn >= 0 && mSelectedColumn < Board.BOARD_SIZE) {
            mBoard.setValue(mSelectedRow, mSelectedColumn, value);
            invalidate();
        }
    }

    public void markValue(int value) {
        if (mSelectedRow >= 0 && mSelectedRow < Board.BOARD_SIZE
                && mSelectedColumn >= 0 && mSelectedColumn < Board.BOARD_SIZE) {
            mBoard.markCell(mSelectedRow, mSelectedColumn, value);
            invalidate();
        }
    }

    public void clearCell() {
        if (mSelectedRow >= 0 && mSelectedRow < Board.BOARD_SIZE
                && mSelectedColumn >= 0 && mSelectedColumn < Board.BOARD_SIZE) {
            mBoard.clear(mSelectedRow, mSelectedColumn);
            invalidate();
        }
    }

    public void resetBoard() {
        for (int row = 0; row < Board.BOARD_SIZE; row++) {
            for (int column = 0; column < Board.BOARD_SIZE; column++) {
                mBoard.clear(row, column);
            }
        }
        invalidate();
    }

    public void lockBoard() {
        mBoard.lock();
    }

    public Board.BOARD_STATUS validateBoard() {
        for (int row = 0; row < Board.BOARD_SIZE; row++) {
            for (int column = 0; column < Board.BOARD_SIZE; column++) {
                if (mBoard.getValue(row, column) == 0) {
                    return Board.BOARD_STATUS.INCOMPLETE;
                }
            }
        }
        return mBoard.validateBoard();
    }
}
