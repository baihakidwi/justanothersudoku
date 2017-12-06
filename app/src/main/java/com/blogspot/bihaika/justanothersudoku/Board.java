package com.blogspot.bihaika.justanothersudoku;

import android.graphics.Point;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Created by Baihaki Dwi on 10/11/2017.
 */

public class Board implements Serializable {
    private class Cell implements Serializable {

        static final int DIGIT_OFFSET = 5;
        static final int VALUE = 0b11111;

        private int mContent;

        public Cell() {
            mContent = 0;
        }

        public Cell(int content) {
            mContent = content;
        }

        public void setContent(int content) {
            mContent = content;
        }

        public boolean setValue(int value) {
            if ((mContent & VALUE) == value) {
                mContent &= (~VALUE);
                return false;
            }
            mContent = (mContent & ~VALUE) | value;
            return true;
        }

        public int getValue() {
            return mContent & VALUE;
        }

        public boolean mark(int markValue) {
            if (checkMark(markValue)) {
                mContent = mContent & (~(int) Math.pow(2, markValue + DIGIT_OFFSET));
                return false;
            }
            mContent = mContent | (int) Math.pow(2, markValue + DIGIT_OFFSET);
            return true;
        }

        public boolean checkMark(int markValue) {
            return (mContent & (int) Math.pow(2, markValue + DIGIT_OFFSET)) != 0;
        }

        public void clear() {
            mContent = 0;
        }
    }

    public enum DIFFICULTY {
        EASY, MEDIUM, HARD, CUSTOM
    }

    public enum BOARD_STATUS {
        COMPLETE, INCOMPLETE, CONFLICT
    }

    public static final int BOARD_SIZE = 9;
    public static final int BLOCK_SIZE = 3;

    private long mBoardId;
    private Cell[][] mBoard;
    private boolean[][] mLock;
    private HashSet<Integer> mChecker;

    public Board() {
        mBoardId = System.currentTimeMillis();
        mBoard = new Cell[BOARD_SIZE][BOARD_SIZE];
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
                mBoard[row][column] = new Cell();
            }
        }
    }

    public Board(String boardSeed) {
        this();
        if (boardSeed != null && !boardSeed.equals("")) {
            String[] boardValues = boardSeed.split(";");
            for (int i = 0; i < boardValues.length; i++) {
                mBoard[i / BOARD_SIZE][i % BOARD_SIZE].setContent(Integer.parseInt(boardValues[i]));
            }
        }
        lock();
    }

    public long getBoardId() {
        return mBoardId;
    }

    public boolean setValue(int row, int column, int value) {
        if (mLock[row][column]) {
            return false;
        }
        return mBoard[row][column].setValue(value);
    }

    public int getValue(int row, int column) {
        return mBoard[row][column].getValue();
    }

    public boolean markCell(int row, int column, int markValue) {
        if (mLock[row][column]) {
            return false;
        }
        return mBoard[row][column].mark(markValue);
    }

    public boolean checkMark(int row, int column, int markValue) {
        return mBoard[row][column].checkMark(markValue);
    }

    public void clear(int row, int column) {
        if (mLock[row][column]) {
            return;
        }
        mBoard[row][column].clear();
    }

    public BOARD_STATUS validateBoard() {
        mChecker = new HashSet<>();

        BOARD_STATUS status;

        boolean incomplete = false;

        for (int diagonal = 0; diagonal < BOARD_SIZE; diagonal++) {
            status = validateRow(diagonal);
            if (status == BOARD_STATUS.CONFLICT) {
                return BOARD_STATUS.CONFLICT;
            }
            if (status == BOARD_STATUS.INCOMPLETE) {
                incomplete = true;
            }
            status = validateColumn(diagonal);
            if (status == BOARD_STATUS.CONFLICT) {
                return BOARD_STATUS.CONFLICT;
            }
            if (status == BOARD_STATUS.INCOMPLETE) {
                incomplete = true;
            }
        }

        for (int row = 0; row < BOARD_SIZE; row += BLOCK_SIZE) {
            for (int column = 0; column < BOARD_SIZE; column += BLOCK_SIZE) {
                status = validateBlock(row, column);
                if (status == BOARD_STATUS.CONFLICT) {
                    return BOARD_STATUS.CONFLICT;
                } else if (status == BOARD_STATUS.INCOMPLETE) {
                    incomplete = true;
                }
            }
        }

        if (incomplete) {
            return BOARD_STATUS.INCOMPLETE;
        }
        return BOARD_STATUS.COMPLETE;
    }

    public BOARD_STATUS validateRow(int row) {
        mChecker.clear();
        int value;
        for (int column = 0; column < BOARD_SIZE; column++) {
            value = mBoard[row][column].getValue();
            if (value != 0) {
                if (mChecker.contains(value)) {
                    return BOARD_STATUS.CONFLICT;
                }
                mChecker.add(value);
            }
        }
        if (mChecker.size() == BOARD_SIZE) {
            return BOARD_STATUS.COMPLETE;
        }
        return BOARD_STATUS.INCOMPLETE;
    }

    public BOARD_STATUS validateColumn(int column) {
        mChecker.clear();
        int value;
        for (int row = 0; row < BOARD_SIZE; row++) {
            value = mBoard[row][column].getValue();
            if (value != 0) {
                if (mChecker.contains(value)) {
                    return BOARD_STATUS.CONFLICT;
                }
                mChecker.add(value);
            }
        }
        if (mChecker.size() == BOARD_SIZE) {
            return BOARD_STATUS.COMPLETE;
        }
        return BOARD_STATUS.INCOMPLETE;
    }

    public BOARD_STATUS validateBlock(int blockRow, int blockColumn) {
        mChecker.clear();
        int value;
        for (int row = blockRow; row < blockRow + BLOCK_SIZE; row++) {
            for (int column = blockColumn; column < blockColumn + BLOCK_SIZE; column++) {
                value = mBoard[row][column].getValue();
                if (value != 0) {
                    if (mChecker.contains(value)) {
                        return BOARD_STATUS.CONFLICT;
                    }
                    mChecker.add(value);
                }
            }
        }
        if (mChecker.size() == BOARD_SIZE) {
            return BOARD_STATUS.COMPLETE;
        }
        return BOARD_STATUS.INCOMPLETE;
    }

    public void lock() {
        mLock = new boolean[BOARD_SIZE][BOARD_SIZE];
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
                if (mBoard[row][column].getValue() != 0) {
                    mLock[row][column] = true;
                } else {
                    mLock[row][column] = false;
                }
            }
        }
    }

    public boolean checkLock(int row, int column) {
        return mLock[row][column];
    }

    public boolean solveBoard() {
        if (validateBoard() == BOARD_STATUS.CONFLICT) {
            return false;
        }
        Point cell = new Point(-1, 0);

        return fillCell(next(cell));
    }

    private boolean fillCell(Point cell) {
        if (cell == null) {
            if (validateBoard() == BOARD_STATUS.COMPLETE) {
                return true;
            }
            return false;
        }
        for (int cellValue = 1; cellValue <= BOARD_SIZE; cellValue++) {
            if (!valueIsConflicted(cell, cellValue)) {
                mBoard[cell.y][cell.x].setValue(cellValue);
                cell = next(cell);
                if (fillCell(cell)) {
                    return true;
                }
                cell = prev(cell);
                mBoard[cell.y][cell.x].clear();
            }
        }
        return false;
    }

    private boolean valueIsConflicted(Point cell, int value) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (mBoard[i][cell.x].getValue() == value
                    || mBoard[cell.y][i].getValue() == value) {
                return true;
            }
        }
        int blockRow = (cell.y / BLOCK_SIZE) * BLOCK_SIZE;
        int blockColumn = (cell.x / BLOCK_SIZE) * BLOCK_SIZE;
        for (int row = blockRow; row < blockRow + BLOCK_SIZE; row++) {
            for (int column = blockColumn; column < blockColumn + BLOCK_SIZE; column++) {
                if (mBoard[row][column].getValue() == value) {
                    return true;
                }
            }
        }
        return false;
    }

    private Point next(Point cellPosition) {
        do {
            cellPosition.x++;
            if (cellPosition.x == BOARD_SIZE) {
                cellPosition.x = 0;
                cellPosition.y++;
            }
            if (cellPosition.y == BOARD_SIZE) {
                return null;
            }
        }
        while (mLock[cellPosition.y][cellPosition.x]);
        return cellPosition;
    }

    private Point prev(Point cellPosition) {
        do {
            cellPosition.x--;
            if (cellPosition.x == -1) {
                cellPosition.x = BOARD_SIZE - 1;
                cellPosition.y--;
            }
            if (cellPosition.y == -1) {
                return null;
            }
        } while (mLock[cellPosition.y][cellPosition.x]);
        return cellPosition;
    }

    public Board duplicate() {
        Board newBoard = new Board();
        newBoard.mBoardId = getBoardId();
        newBoard.mLock = new boolean[BOARD_SIZE][BOARD_SIZE];
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
                newBoard.mLock[row][column] = false;
                newBoard.setValue(row, column, mBoard[row][column].getValue());
                newBoard.mLock[row][column] = mLock[row][column];
            }
        }
        return newBoard;
    }

    @Override
    public String toString() {
        String s = "";
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
                s += mBoard[row][column].getValue() + ";";
            }
        }
        return s.substring(0, s.length() - 1);
    }

}


