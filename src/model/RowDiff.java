package model;
import core.diff_match_patch.Operation;
import org.apache.poi.ss.usermodel.Row;

import java.util.ArrayList;
import java.util.List;

public class RowDiff {
    private Operation operation;
    private List<CellDiff> cells = new ArrayList<CellDiff>();
    private int rowindex;
    private Boolean isHeader;
    private Row rowOld;
    private Row rowNew;



    public RowDiff(boolean isheader){
        this.isHeader = isheader;
    }

    public RowDiff(int rowindex, List<CellDiff> cells, Boolean isHeader){
        this.cells = cells;
        this.rowindex = rowindex;
        this.isHeader = isHeader != null? isHeader: null;
    }

    public RowDiff() {

    }

    public Row getRowOld() {
        return rowOld;
    }

    public void setRowOld(Row rowOld) {
        this.rowOld = rowOld;
    }

    public Row getRowNew() {
        return rowNew;
    }

    public void setRowNew(Row rowNew) {
        this.rowNew = rowNew;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public List<CellDiff> getCells() {
        return cells;
    }

    public void setCells(List<CellDiff> cells) {
        this.cells = cells;
    }

    public int getRowindex() {
        return rowindex;
    }

    public void setRowindex(int rowindex) {
        this.rowindex = rowindex;
    }

    public Boolean getHeader() {
        return isHeader;
    }

    public void setHeader(Boolean header) {
        isHeader = header;
    }

    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append("<tr>");
        for (CellDiff cell:
             this.cells) {
            str.append("<td>").append(cell.toString()).append("</td>");
        }
        str.append("</tr>");
        return str.toString();
    }
}
