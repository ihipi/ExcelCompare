package model;

import core.diff_match_patch.Operation;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.Collection;

public class  SheetDiff {
    private Sheet sheet;
    private Collection differences;
    private Operation operation;

    public SheetDiff() {
    }

    public SheetDiff(Sheet sheet, Collection differences) {
        this.sheet = sheet;
        this.differences = differences;
    }

    public Sheet getSheet() {
        return sheet;
    }

    public void setSheet(Sheet sheet) {
        this.sheet = sheet;
    }

    public  Collection getDifferences(){
        return this.differences;
    };

    public  void setDifferences(Collection diffs){
        this.differences = diffs;

    };

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SheetDiff)) return false;
        SheetDiff sheetDiff = (SheetDiff) o;
        return getSheet().getSheetName().equals(sheetDiff.getSheet().getSheetName());
    }



}
