package core;


import core.diff_match_patch.Operation;
import model.CellDiff;
import model.OpenLFunction;
import model.RowDiff;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ExcelCompare {

    private static String[] types = {"Spreadsheet", "Rules", "Method"};
    DataFormatter format = new DataFormatter();
    FormulaEvaluator formulaEvaluator;
    private diff_match_patch diff = new diff_match_patch();


    public ExcelCompare() {
    }

    /**
     *
     * @param path
     * @return
     */
    public Workbook openFile(String path){

        try {
            FileInputStream excelFile = new FileInputStream(new File(path));
            Workbook wb = WorkbookFactory.create(new File(path));
            return wb;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void compareWorkbooks(Workbook wba, Workbook wbb, boolean isopenl){
        List<Sheet> sheetsA = getworkbookSheets(wba);
        List<Sheet> sheetsB = getworkbookSheets(wbb);

    }

    private List<Sheet> getworkbookSheets(Workbook wb) {
        List<Sheet> sheets = new ArrayList<Sheet>();
        for(int i = 0; i< wb.getNumberOfSheets(); i++){
            sheets.add(wb.getSheetAt(i));
        }
        return sheets;
    }

    /**
     *
     * @param sha
     * @param shb
     * @return
     */
    public List<RowDiff> compareSheets(Sheet sha, Sheet shb){
        int ini = sha.getFirstRowNum()<shb.getFirstRowNum() ? sha.getFirstRowNum() : shb.getFirstRowNum();
        int end = sha.getLastRowNum()>shb.getLastRowNum() ? sha.getLastRowNum() : shb.getLastRowNum();
        return compareSheets(sha, shb, ini, end, 0);
    }

    /**
     * comparae two sheets
     * @param sha left sheet
     * @param shb rigth sheet
     * @param ini first row to analize
     * @param end last row to analize
     * @param delta delta row distance between left<->rigth</->
     * @return  List of rows with some changes
     */
    public List<RowDiff> compareSheets(Sheet sha, Sheet shb, int ini, int end, int delta) {
        List<RowDiff> rows = new ArrayList<RowDiff>();
        //compare first row of rank
        RowDiff row = compareRow(sha, shb, ini, ini+delta);
        //
        if(row.getOperation() == Operation.CHANGED){
            rows.add(row);
        } else if(row.getOperation() == Operation.FULL_CHANGE){
            List<RowDiff> insertRows = checkDeletedAddRow(sha, shb, ini, end, delta);
            if(insertRows == null){
                row.setOperation(Operation.DELETE);
                delta--;
            } else {
                rows.addAll(insertRows);
                delta += insertRows.size();
            }
        }
        //discard firsts equal rows
        while(ini<end && row.getOperation().compareTo(Operation.EQUAL) == 0){
            ini++;
            row = compareRow(sha, shb, ini, ini+delta);
            rows.add(row);
        }
        //discard lasts equal rows
        while(end>ini && row.getOperation().compareTo(Operation.EQUAL) == 0){
            end--;
            row = compareRow(sha, shb, ini, ini+delta);
            rows.add(row);
        }
        //recurive condition
        if(ini<end){
            compareSheets(sha, shb, ini, end, delta);
        }
        return rows;

    }


    /**
     *
     * @param sha
     * @param shb
     * @param rowA
     * @param end
     * @param delta
     * @return
     */
    private List<RowDiff> checkDeletedAddRow(Sheet sha, Sheet shb, int rowA, int end, int delta) {
        List<RowDiff> rows = new ArrayList<RowDiff>();
        /* iterate rows searching the same row
        ** while not apears save new rows
        ** if not apear is a deleted row
        */
        for(int rowB = rowA+delta; rowB< end;rowB++){
            RowDiff row = compareRow(sha, shb, rowA, rowB);
            row.setOperation(Operation.INSERT);
            rows.add(row);
            if(row.getOperation() == Operation.EQUAL){
                return rows;
            }

        }
        return null;
    }

    /*private RowDiff compareRow(Sheet sha, Sheet shb, int rowA, int rowB) {
        return null;
    }*/

    /**
     *
     * @param sha
     * @param shb
     * @param rowA
     * @param rowB
     * @return
     */
    public RowDiff compareRow(Sheet sha, Sheet shb, int rowA, int rowB) {
        if(sha.getRow(rowA)!=null && shb.getRow(rowB)!=null){
            //es calcula l'interval de cel.les mes ampli entre les 2 files
            int ini = sha.getRow(rowA).getFirstCellNum()<= shb.getRow(rowB).getFirstCellNum()? sha.getRow(rowA).getFirstCellNum():shb.getRow(rowB).getFirstCellNum();
            int end = sha.getRow(rowA).getLastCellNum() >= shb.getRow(rowB).getLastCellNum() ? sha.getRow(rowA).getLastCellNum(): shb.getRow(rowB).getLastCellNum();
            return compareRow(sha.getRow(rowA), shb.getRow(rowB), ini, end);
        } else {
            RowDiff row = new RowDiff();
            if(sha.getRow(rowA)==null){
                row.setOperation(Operation.INSERT);
                row.setRowNew(shb.getRow(rowB));
            } else {
                row.setOperation(Operation.DELETE);
                row.setRowNew(sha.getRow(rowA));
            }
            return row;
        }
    }

    /**
     *
     * @param rha
     * @param rhb
     * @param ini
     * @param end
     * @return
     */
    public RowDiff compareRow(Row rha, Row rhb, int ini, int end) {
        RowDiff row = new RowDiff();
        //compara les cel.les de l' interval
        for (int i = ini; i < end; i++) {
            row.getCells().add(compareCell(rha.getCell(i), rhb.getCell(i)));
        }
        //Check changes affect all row
        row.setOperation(Operation.FULL_CHANGE);
        for(CellDiff cell: row.getCells()) {
            if(cell.getState().compareTo(Operation.EQUAL)== 0) {
                row.setOperation(Operation.CHANGED);
            }
        }
        return row;
    }

    /**
     * Checks if is row empty.
     *
     * @param row the row
     * @return true, if is row empty
     */
    public static boolean isRowEmpty(Row row) {

        if (row != null) {
            for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
                Cell cell = row.getCell(c);
                if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK)
                    return false;
            }
        }
        return true;
    }

    /**
     *
     * @param ca
     * @param cb
     * @return
     */
    public CellDiff compareCell(Cell ca, Cell cb){
        LinkedList<diff_match_patch.Diff> diffs = diff.diff_main(ca.getStringCellValue(), cb.getStringCellValue(), true);
        CellDiff cell = new CellDiff(ca, ca.getStringCellValue(), cb.getStringCellValue(), diffs);
        return cell;
    }

    /*
    OPENL DIFF COMPARE FUNCS
     */
    
    /**
     * Gets the sheet functions.
     *
     * @param sh the sh
     * @return the sheet functions
     */
    public List<OpenLFunction> getSheetFunctions(Sheet sh) {
        System.out.println(sh.getSheetName());
        List<OpenLFunction> functions = new ArrayList<OpenLFunction>();
        List<CellRangeAddress> merged = sh.getMergedRegions();
        for (CellRangeAddress mergCell : merged) {
            Cell cell =sh.getRow(mergCell.getFirstRow()).getCell(mergCell.getFirstColumn());

            for (int i = 0; i < types.length; i++) {
                if(cell.getRichStringCellValue().toString().contains(types[i])){

                    int lastRow = mergCell.getFirstRow();
                    do {
                        lastRow += 1;
                    }while(!isRowEmpty(sh.getRow(lastRow)));
                    OpenLFunction funcio = new OpenLFunction(sh, cell.getRowIndex(), lastRow, cell.getColumnIndex(), mergCell.getLastColumn());
                    if(types[i]=="Spreadsheet"){
                        funcio.setType(OpenLFunction.types.SPREADSHEET);
                    } else if(types[i]=="Rules"){
                        funcio.setType(OpenLFunction.types.RULES);
                    } else if(types[i]=="Method"){
                        funcio.setType(OpenLFunction.types.METHOD);
                    }
                    functions.add(funcio);
                }
            }
        }
        return functions;
    }

    public List<OpenLFunction> compareOpenLSheet(Sheet sha, Sheet shb){
        List<OpenLFunction> funcsA = getSheetFunctions(sha);
        List<OpenLFunction> funcsB = getSheetFunctions(shb);
        List<OpenLFunction> differences = new ArrayList<OpenLFunction>();
        for (OpenLFunction func
                : funcsA) {
            if(funcsB.contains(func)){
                OpenLFunction funcDiff = compareOpenLFunctions(func, funcsB.get(funcsB.indexOf(func)));
                if (func.getOperation() != Operation.EQUAL){
                    differences.add(funcDiff);
                }
                funcsB.remove(func);
            } else {
                func.setOperation(Operation.DELETE);
                differences.add(func);
            }
        }
        if(!funcsB.isEmpty()){
            for (OpenLFunction func:
                 funcsB) {
                func.setOperation(Operation.INSERT);
                differences.add(func);
            }
        }
        return differences;
    }

    public OpenLFunction compareOpenLFunctions(OpenLFunction funa, OpenLFunction funb){
        int ini = funa.getFirstRow();
        int end = funa.getLastRow() >= funb.getLastRow() ?  funa.getLastRow() : funb.getLastRow();
        int delta = funb.getFirstRow() - funa.getFirstCol();
        List<RowDiff> rowDiffs = compareSheets(funa.getSheet(), funb.getSheet(), ini, end, delta);
        funb.setDiferences(rowDiffs);
        if(rowDiffs.isEmpty()){
            return null;
        }
        return null;
    }
    
}
