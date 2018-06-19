package core;

import org.apache.poi.ss.formula.functions.Column;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellRange;
import org.apache.poi.ss.usermodel.Row;
import core.diff_match_patch.Diff;
import core.diff_match_patch.Operation;

import java.util.LinkedList;

public class CellDiff {

    private Cell range;
    private Operation state;
    private String txtOld;
    private String txtNew;
    private LinkedList<Diff> diffs;


    public CellDiff(Cell range, Operation state, String txtOld, String txtNew) {
        this.range = range;
        this.state = state;
        this.txtOld = txtOld;
        this.txtNew = txtNew;
    }

    public CellDiff(Cell cell, String stringa, String stringb, LinkedList<Diff> diffs) {
        this.range = cell;
        this.txtOld = stringa;
        this.txtNew = stringb;
        this.diffs = diffs;
        if(diffs.size() == 1) {
            if (diffs.get(0).operation.compareTo(Operation.EQUAL) == 0) {
                this.state = Operation.EQUAL;
            } else if (diffs.get(0).operation.compareTo(Operation.DELETE) == 0) {
                this.state = Operation.DELETE;
            } else if (diffs.get(0).operation.compareTo(Operation.INSERT) == 0) {
                this.state = Operation.INSERT;
            }
        } else {
            this.state = Operation.CHANGED;
        }
    }

    public int getRow(){
        return range.getRowIndex();
    }
    public int getCol(){
        return range.getColumnIndex();
    }

    public Cell getRange() {
        return range;
    }

    public void setRange(Cell range) {
        this.range = range;
    }

    public Operation getState() {
        return state;
    }

    public void setState(Operation state) {
        this.state = state;
    }

    public String getTxtOld() {
        return txtOld;
    }

    public void setTxtOld(String txtOld) {
        this.txtOld = txtOld;
    }

    public String getTxtNew() {
        return txtNew;
    }

    public void setTxtNew(String txtNew) {
        this.txtNew = txtNew;
    }

    public LinkedList<Diff> getDiffs() {
        return diffs;
    }

    public void setDiffs(LinkedList<Diff> diffs) {
        this.diffs = diffs;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (Diff diff :
                this.diffs) {
            switch (diff.operation) {
                case INSERT:
                    str.append("**");
                    str.append(diff.text);
                    str.append("**");
                    break;
                case DELETE:
                    str.append("~~");
                    str.append(diff.text);
                    str.append("~~");
                    break;
                case EQUAL:
                    str.append(diff.text);
            }
        }
        return str.toString();
    }
}
