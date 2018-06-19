package core;


import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;

public class ExcelCompare {

    DataFormatter format = new DataFormatter();
    FormulaEvaluator formulaEvaluator;
    private diff_match_patch diff = new diff_match_patch();


    public ExcelCompare() {
    }

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

    public void compareWorkbooks(Workbook wba, Workbook wbb){

    }

    public void compareSheets(Sheet sha, Sheet shb){

    }

    public CellDiff compareCell(Cell ca, Cell cb){
        LinkedList<diff_match_patch.Diff> diffs = diff.diff_main(ca.getStringCellValue(), cb.getStringCellValue(), true);
        CellDiff cell = new CellDiff(ca, ca.getStringCellValue(), cb.getStringCellValue(), diffs);
        return cell;
    }

}
