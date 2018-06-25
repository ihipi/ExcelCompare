package core;


import core.diff_match_patch.Operation;
import model.CellDiff;
import model.OpenLFunction;
import model.RowDiff;
import model.SheetDiff;
import org.apache.poi.EncryptedDocumentException;
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
            return  WorkbookFactory.create(new File(path));
        } catch (InvalidFormatException | IOException | EncryptedDocumentException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void compareWorkbooks(Workbook wba, Workbook wbb, boolean isopenl){
        List<SheetDiff> sheetsA = getworkbookSheets(wba);
        List<SheetDiff> sheetsB = getworkbookSheets(wbb);

    }

    public List<SheetDiff> compareWorkbooks(String wbPathA, String wbPathB, boolean isopenl){
        Workbook wba = openFile(wbPathA);
        Workbook wbb = openFile(wbPathB);
        List<SheetDiff> sheetsA = getworkbookSheets(wba);
        List<SheetDiff> sheetsB = getworkbookSheets(wbb);
        List<SheetDiff> diffs= new ArrayList<SheetDiff>();

        if(!isopenl){
            for (SheetDiff sh : sheetsA) {
                SheetDiff shdiff = new SheetDiff();
                if (sheetsB.contains(sh)) {
                    shdiff.setSheet(sh.getSheet());
                    List<RowDiff> rowDiffs = compareSheets(sh, sheetsB.get(sheetsB.indexOf(sh)));
                    if (rowDiffs != null && !rowDiffs.isEmpty()) {
                        shdiff.setOperation(Operation.CHANGED);
                        shdiff.setDifferences(rowDiffs);
                        diffs.add(shdiff);
                    }
                    sheetsB.remove(sh);
                } else {
                    shdiff.setOperation(Operation.DELETE);
                    diffs.add(shdiff);
                }
            }
            for (SheetDiff shd :
                    sheetsB) {
                shd.setOperation(Operation.INSERT);
                diffs.add(shd);
            }
        } else {

            for (SheetDiff sh : sheetsA) {
                SheetDiff shdiff = new SheetDiff();
                if (sheetsB.contains(sh)) {
                    shdiff.setSheet(sh.getSheet());
                    List<OpenLFunction> rowDiffs = compareOpenLSheet(sh, sheetsB.get(sheetsB.indexOf(sh)));
                    if (rowDiffs != null && !rowDiffs.isEmpty()) {
                        shdiff.setOperation(Operation.CHANGED);
                        shdiff.setDifferences(rowDiffs);
                        diffs.add(shdiff);
                    }
                    sheetsB.remove(sh);
                } else {
                    shdiff.setOperation(Operation.DELETE);
                    diffs.add(shdiff);
                }
            }
            for (SheetDiff shd :
                    sheetsB) {
                shd.setOperation(Operation.INSERT);
                diffs.add(shd);
            }
        }
        return diffs;
    }


    public List<SheetDiff> getworkbookSheets(Workbook wb) {
        List<SheetDiff> sheets = new ArrayList<>();
        for(int i = 0; i< wb.getNumberOfSheets(); i++){
            SheetDiff shDiff = new SheetDiff();
            shDiff.setSheet(wb.getSheetAt(i));
            sheets.add(shDiff);
        }
        return sheets;
    }
    public List<Sheet> getworkbookSheets(String path) {
        Workbook wb = openFile(path);
        List<Sheet> sheets = new ArrayList<Sheet>();
        for(int i = 0; i< wb.getNumberOfSheets(); i++){
            sheets.add(wb.getSheetAt(i));
        }
        return sheets;
    }

    /**
     *
     * @param shda
     * @param shdb
     * @return
     */
    public List<RowDiff> compareSheets(SheetDiff shda, SheetDiff shdb){
        Sheet sha = shda.getSheet();
        Sheet shb = shdb.getSheet();
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
        System.out.println("\tsheet: " + sha.getSheetName() + " ini:" + ini + "end: " +end);
        List<RowDiff> rows = new ArrayList<RowDiff>();
        //compare first row of rank
        RowDiff row = compareRow(sha, shb, ini, ini+delta);
        //
        if(row != null && (row.getOperation() != Operation.EQUAL
                            && row.getOperation() != Operation.FULL_CHANGE)){
            rows.add(row);
            ini++;
            row = compareRow(sha, shb, ini, ini+delta);
        } else if(row != null && row.getOperation() == Operation.FULL_CHANGE){
            //comprova si s'han afegit o borrat files
            List<RowDiff> insertRows = checkDeletedAddRow(sha, shb, ini, end, delta);
            if(insertRows == null){
                //null = deleted Row decrease delta
                row.setOperation(Operation.DELETE);
                rows.add(row);
                delta--;
            } else if(insertRows.size()>0){
                //insertedRows increase delta
                //row.setOperation(Operation.INSERT);
                rows.addAll(insertRows);
                delta += insertRows.size();
            } else {
                // else is a fullChangeRow
                rows.add(row);
            }


            ini++;
            row = compareRow(sha, shb, ini, ini+delta);
        }
        //discard firsts equal rows
        while(ini<end && (row ==null ||row.getOperation().compareTo(Operation.EQUAL) == 0)){
            ini++;
            System.out.println("\tsheet: " + sha.getSheetName() + " ini:" + ini + "end: " +end);
            row = compareRow(sha, shb, ini, ini+delta);

        }
        //discard lasts equal rows

        while(end>ini &&  (row ==null || row.getOperation().compareTo(Operation.EQUAL) == 0)){
            end--;
            System.out.println("\tsheet: " + sha.getSheetName() + " ini:" + ini + "end: " +end);
            row = compareRow(sha, shb, ini, ini+delta);

        }
        //recurive condition
        if(ini<end){
            rows.addAll(compareSheets(sha, shb, ini, end, delta));
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
     * @return null         -> is a deleted row
     *         Empty list   -> FullChange row
     *         List.size > 0-> InsertedRows
      */
    private List<RowDiff> checkDeletedAddRow(Sheet sha, Sheet shb, int rowA, int end, int delta) {
        List<RowDiff> rows = new ArrayList<RowDiff>();
        /* iterate rows searching the same row
        ** while not apears save new rows
        ** if not apear is a deleted row
        */
        for(int rowB = rowA+delta; rowB< end;rowB++){
            RowDiff row = compareRow(sha, shb, rowA, rowB);
            if(row == null || row.getOperation() == Operation.EQUAL ){
                return rows;
            }
            row.setOperation(Operation.INSERT);
            rows.add(row);

        }
        rows = new ArrayList<RowDiff>();
        RowDiff row = compareRow(sha, shb, rowA+1, end+1);
        //while((rowA+1<sha.getLastRowNum() && end+1<shb.getLastRowNum()) &&
        if(row ==null || row.getOperation() == Operation.EQUAL){
            return rows;
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
     * @return if Equal == return null
     */
    public RowDiff compareRow(Sheet sha, Sheet shb, int rowA, int rowB) {
        if(sha.getRow(rowA)!=null && shb.getRow(rowB)!=null){
            //es calcula l'interval de cel.les mes ampli entre les 2 files
            int ini = sha.getRow(rowA).getFirstCellNum()<= shb.getRow(rowB).getFirstCellNum()? sha.getRow(rowA).getFirstCellNum():shb.getRow(rowB).getFirstCellNum();
            int end = sha.getRow(rowA).getLastCellNum() >= shb.getRow(rowB).getLastCellNum() ? sha.getRow(rowA).getLastCellNum(): shb.getRow(rowB).getLastCellNum();
            return compareRow(sha.getRow(rowA), shb.getRow(rowB), ini, end);
        } else {
            RowDiff row = new RowDiff();
            if(!(sha.getRow(rowA)==null && shb.getRow(rowB)==null)) {
                if (sha.getRow(rowA) == null) {
                    row.setOperation(Operation.INSERT);
                    row.setRowNew(shb.getRow(rowB));
                } else {
                    row.setOperation(Operation.DELETE);
                    row.setRowNew(sha.getRow(rowA));
                }
                System.out.println("\t\tRow: " + row.getRowindex() + "(" + row.getOperation() + ")");
                return row;
            }
            row.setOperation(Operation.EQUAL);
            System.out.println("\t\tRow: " + row.getRowindex() + "(" + row.getOperation() + ")");
            return null;
        }

    }

    /**
     *
     * @param rha
     * @param rhb
     * @param ini
     * @param end
     * @return if EQUAL => null
     */
    public RowDiff compareRow(Row rha, Row rhb, int ini, int end) {
        RowDiff row = new RowDiff();
        row.setRowindex(rhb.getRowNum()+1);
        //compara les cel.les de l' interval
        //Check changes affect all row
        boolean hasChanged = false;
        row.setOperation(Operation.FULL_CHANGE);
        for (int i = ini; i < end; i++) {
            CellDiff cdiff= compareCell(rha.getCell(i), rhb.getCell(i));
            row.getCells().add(cdiff);
            if(cdiff.getState().compareTo(Operation.EQUAL)== 0){// && !"".equals(cdiff.getTxtOld())) {
                row.setOperation(Operation.CHANGED);
            } else {
                hasChanged = true;
            }
        }

        System.out.println("\t\tRow: " + row.getRowindex() + "(" + row.getOperation() + ")");
        if(hasChanged){
            return row;
        }
        return null;
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
        CellDiff cell = new CellDiff();
        String a = getStringfromCell(ca);
        String b = getStringfromCell(cb);
        if (a != null && b != null) {
            LinkedList<diff_match_patch.Diff> diffs = diff.diff_main(a, b, true);
            cell = new CellDiff(ca, a, b, diffs);
            //System.out.println("\t\t\tcelda: " + cell.getCell().getAddress() + "(" + cell.getState() + ")");
        } else {
            if(!(ca == null && cb == null)){
                if(a == null){
                    cell.setState(Operation.INSERT);
                    cell.setTxtNew(getStringfromCell(cb));
                } else {
                    cell.setState(Operation.DELETE);
                    cell.setTxtOld(getStringfromCell(ca));
                }
            } else {
                cell.setState(Operation.EQUAL);
            }
        }
        return cell;
    }

    public static String getStringfromCell(Cell cell) {
        String value = "";
        if(cell !=null){
            switch (cell.getCellType()) {

            case Cell.CELL_TYPE_STRING:
                value = cell.getRichStringCellValue().getString();
                break;

            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    value = cell.getDateCellValue().toString();
                } else {
                    value = String.valueOf(cell.getNumericCellValue());
                    if (value.endsWith(".0")) {
                        value = String.valueOf((int) cell.getNumericCellValue());
                    }
                }
                break;

            case Cell.CELL_TYPE_BOOLEAN:
                value = String.valueOf(cell.getBooleanCellValue());
                break;

            case Cell.CELL_TYPE_FORMULA:
                value = cell.getCellFormula();
                break;
            case Cell.CELL_TYPE_BLANK:
                value = "";
                break;
            default:
                value = "";
            }
        }
        System.out.println(value);
        return value;
    }
    /*************************
    OPENL DIFF COMPARE FUNCS
     **************************/
    
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
                String head = getStringfromCell(cell);
                if(head.contains(types[i])){

                    int lastRow = mergCell.getFirstRow();
                    do {
                        lastRow += 1;
                    }while(!isRowEmpty(sh.getRow(lastRow)));
                    OpenLFunction funcio = new OpenLFunction(sh, cell.getRowIndex(), lastRow, cell.getColumnIndex(), mergCell.getLastColumn());
                    funcio.setName(head);
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

    public List<OpenLFunction> compareOpenLSheet(SheetDiff sha, SheetDiff shb){
        List<OpenLFunction> funcsA = getSheetFunctions(sha.getSheet());
        List<OpenLFunction> funcsB = getSheetFunctions(shb.getSheet());
        List<OpenLFunction> differences = new ArrayList<OpenLFunction>();
        for (OpenLFunction func
                : funcsA) {
            if(funcsB.contains(func)){
                OpenLFunction funcDiff = compareOpenLFunctions(func, funcsB.get(funcsB.indexOf(func)));
                if(funcDiff!=null) {
                    if (funcDiff.getOperation() != Operation.EQUAL) {
                        differences.add(funcDiff);
                    }
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
        int delta = funb.getFirstRow() - funa.getFirstRow();
        List<RowDiff> rowDiffs = compareSheets(funa.getSheet(), funb.getSheet(), ini, end, delta);
        funb.setDiferences(rowDiffs);
        if(rowDiffs.isEmpty()){
            return null;
        }
        funb.setOperation(Operation.CHANGED);
        return funb;
    }
    
}
