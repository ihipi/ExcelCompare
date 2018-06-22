package model;

import core.diff_match_patch.Operation;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellRange;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OpenLFunction implements CellRange {

    public static enum types {SPREADSHEET, RULES, METHOD};


    private String name;
    private types type;
    private Sheet sheet;
    private Operation operation;
    private int firstRow;
    private int firstCol;
    private int lastRow;
    private int lastCol;
    private List<RowDiff> diferences = new ArrayList<RowDiff>();


    public OpenLFunction(Sheet sheet, int firstRow, int firstCol, int lastRow, int lastCol) {
        this.name = this.getTopLeftCell().getRichStringCellValue().toString();
        this.sheet = sheet;
        this.firstRow = firstRow;
        this.firstCol = firstCol;
        this.lastRow = lastRow;
        this.lastCol = lastCol;
    }

    public OpenLFunction( Sheet sheet, int firstRow, int firstCol, int lastRow, int lastCol, types type) {
        this.name = this.getTopLeftCell().getRichStringCellValue().toString();
        this.type = type;
        this.sheet = sheet;
        this.firstRow = firstRow;
        this.firstCol = firstCol;
        this.lastRow = lastRow;
        this.lastCol = lastCol;
    }

    public OpenLFunction( Sheet sheet, int firstRow, int firstCol, int lastRow, int lastCol, Operation op) {
        this.name = this.getTopLeftCell().getRichStringCellValue().toString();
        this.type = type;
        this.sheet = sheet;
        this.firstRow = firstRow;
        this.firstCol = firstCol;
        this.lastRow = lastRow;
        this.lastCol = lastCol;
        this.operation = op;
    }


    @Override
    public int getWidth() {
        return this.lastCol -this.firstCol;
    }

    @Override
    public int getHeight() {
        return this.lastRow - this.firstRow;
    }

    @Override
    public int size() {
        return getWidth()*getHeight();
    }

    @Override
    public String getReferenceText() {
        return this.name;
    }

    @Override
    public Cell getTopLeftCell() {
        return sheet.getRow(this.firstRow).getCell(this.firstCol);
    }

    @Override
    public Cell getCell(int relativeRowIndex, int relativeColumnIndex) {
        return sheet.getRow(this.firstRow+relativeRowIndex).getCell( this.firstCol+relativeColumnIndex);
    }

    @Override
    public Cell[] getFlattenedCells() {
        Cell[] cells = new Cell[getHeight()*getHeight()];
        for(int i = 0; i < getHeight(); i++){
            for(int j = 0; j < getWidth(); j++){
                cells[i*getWidth()+j] = this.getCell(i,j);
            }
        }
        return new Cell[0];
    }

    @Override
    public Cell[][] getCells() {
        Cell[][] cells = new Cell[getHeight()][getWidth()];
        for(int i = 0; i < getHeight(); i++){
            for(int j = 0; j < getWidth(); j++){
                cells[i][j] = this.getCell(i,j);
            }
        }
        return new Cell[0][];
    }

    @Override
    public Iterator iterator() {
        return null;
    }

    public types getType() {
        return type;
    }

    public void setType(types type) {
        this.type = type;
    }

    public int getFirstRow() {
        return firstRow;
    }

    public void setFirstRow(int firstRow) {
        this.firstRow = firstRow;
    }

    public int getFirstCol() {
        return firstCol;
    }

    public void setFirstCol(int firstCol) {
        this.firstCol = firstCol;
    }

    public int getLastRow() {
        return lastRow;
    }

    public void setLastRow(int lastRow) {
        this.lastRow = lastRow;
    }

    public int getLastCol() {
        return lastCol;
    }

    public void setLastCol(int lastCol) {
        this.lastCol = lastCol;
    }

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    public Sheet getSheet() {
        return sheet;
    }


    public void setSheet(Sheet sheet) {
        this.sheet = sheet;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public List<RowDiff> getDiferences() {
        return diferences;
    }


    public void setDiferences(List<RowDiff> diferences) {
        this.diferences = diferences;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OpenLFunction)) return false;
        OpenLFunction that = (OpenLFunction) o;
        return getTopLeftCell().getRichStringCellValue().toString().equals(
                 that.getTopLeftCell().getRichStringCellValue().toString());
    }

}
