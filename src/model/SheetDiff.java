package model;

import org.apache.poi.ss.usermodel.Sheet;

import java.util.Collection;

public abstract class  SheetDiff {
    private Sheet sheet;
    private  Collection differences;

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

    public abstract Collection getDifferences();

    public abstract void setDifferences(Collection differences);
}
