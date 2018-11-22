package gui;

import core.ExcelCompare;
import core.diff_match_patch;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.OpenLFunction;
import model.RowDiff;
import model.SheetDiff;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Controller {

    private diff_match_patch comparador = new diff_match_patch();
    private ExcelCompare eCompare = new ExcelCompare();

    @FXML
    private ToggleGroup toggleGroup;
    @FXML
    private RadioButton openl;
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnCompare;


    @FXML
    private TextArea txtA;
    @FXML
    private TextArea txtB;

    @FXML
    private WebView webView = new WebView();
    //@FXML
    //private final WebEngine webEngine = webView.getEngine();

    @FXML
    public void compareFiles(ActionEvent actionEvent) {
        if(!txtA.getText().isEmpty() && !txtB.getText().isEmpty()) {
            //Workbook wbA = eCompare.openFile(txtA.getText());
            //Workbook wbB = eCompare.openFile(txtB.getText());

            //List<Sheet> sha = eCompare.getworkbookSheets(txtA.getText());
            //List<Sheet> shb = eCompare.getworkbookSheets(txtB.getText());

            boolean isOpenL = false;
            if(openl.equals(toggleGroup.getSelectedToggle())){
                isOpenL = true;
            }
            List<SheetDiff> diffs = eCompare.compareWorkbooks(txtA.getText(), txtB.getText(), isOpenL);
            populateTree(diffs, isOpenL);


        }
    }

    private void populateTree(List<SheetDiff> diffs, boolean isOpenL) {
        String[] pathSplited = txtA.getText().split("/");
        String rootIcon = isOpenL? "/resources/icons/openl.png" : "/resources/icons/excel.png";
        Node icon = new ImageView(new Image(rootIcon));
        TreeItem<Object> root= new TreeItem<Object>(pathSplited[pathSplited.length-1], icon );
        for (SheetDiff shd :
                diffs) {

            Node shIcon = new ImageView(new Image("/resources/icons/row_mod.png"));
            System.out.println(shd.getSheet().getSheetName());
            TreeItem<Object> shTree = new TreeItem<Object>(shd.getSheet().getSheetName(), shIcon);
            for (Object  diff:
                    shd.getDifferences()) {
                if(diff instanceof RowDiff){
                    Node rowIcon = new ImageView(new Image("/resources/icons/row_add.png"));
                    RowDiff row = (RowDiff) diff;
                    TreeItem<Object> rowTree = new TreeItem<Object>(row, rowIcon);
                    shTree.getChildren().add(rowTree);

                } else if(diff instanceof OpenLFunction){
                    Node rowIcon = null;
                    OpenLFunction fun = (OpenLFunction) diff;
                    if(fun.getOperation().equals(fun.getOperation().CHANGED)){
                    	rowIcon = new ImageView(new Image("/resources/icons/rule_mod.png"));
                    } else if (fun.getOperation().equals(fun.getOperation().INSERT)){
                    	rowIcon = new ImageView(new Image("/resources/icons/rule_add.png"));                    	
                    }else if (fun.getOperation().equals(fun.getOperation().DELETE)){
                    	rowIcon = new ImageView(new Image("/resources/icons/rule_del.png"));                    	
                    }
                    if(fun.getDiferences().size()>0) {
                        TreeItem<Object> funTree = new TreeItem<Object>(fun, rowIcon);

                        for (RowDiff row :
                                fun.getDiferences()) {
                            TreeItem<Object> rowTree = new TreeItem<Object>(row, rowIcon);
                            funTree.getChildren().add(rowTree);
                        }
                        shTree.getChildren().add(funTree);
                    }
                }
            }

            root.getChildren().add(shTree);
        }
        tree.setRoot(root);
    }

    @FXML
    void initialize() {
        assert btnCompare != null : "fx:id=\"btnCompare\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert txtB != null : "fx:id=\"txtB\" was not injected: check your FXML file 'MainWindow.fxml'.";
    }

    @FXML
    public void leftDragOver(DragEvent dragEvent) {
        fileDragOver(dragEvent, txtA);
    }

    @FXML
    public void rigthDragOver(DragEvent dragEvent) {
        fileDragOver(dragEvent, txtB);
    }

    @FXML
    void fileDragOver(DragEvent event, TextArea textArea) {
        Dragboard board = event.getDragboard();
        if (board.hasFiles()) {

            List<File> phil = board.getFiles();
            String path = phil.get(0).toPath().toString();

            if (path.endsWith(".xls") ||path.endsWith(".xlsx")) {
                event.acceptTransferModes(TransferMode.ANY);
                textArea.setText(path);
            }
        }
    }

    @FXML
    public void fileChooserA(ActionEvent actionEvent) {
        fileChooser(actionEvent, txtA);
    }

    @FXML
    public void fileChooserB(ActionEvent actionEvent) {
        fileChooser(actionEvent, txtB);
    }

    @FXML
    public void fileChooser(ActionEvent event, TextArea txtArea){
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel '97 files (*.xls)", "*.xls");
        FileChooser.ExtensionFilter extFilterx = new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx");
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Excel File");
        chooser.getExtensionFilters().add(extFilter);
        chooser.getExtensionFilters().add(extFilterx);
        File file = chooser.showOpenDialog(new Stage());
        if (file != null) {
            txtArea.setText(file.getPath());
        }
    }

    @FXML
    TreeView<Object> tree;

    @FXML
    public void updateWebView(MouseEvent mouseEvent) {
        TreeItem<Object> selectionItem = tree.getSelectionModel().getSelectedItem();
        if(selectionItem != null){
            Object obj = selectionItem.getValue();
            String html = "";
            if(obj instanceof RowDiff) {
                html = "<table  border=\"1px solid black\">";
                html += ((RowDiff)obj).toHtmlString();
                html += "</table>";
            }else if(obj instanceof OpenLFunction){
                html = ((OpenLFunction)obj).toHtmlString();
            }
            System.out.println(html);
            webView.getEngine().loadContent(html);

            webView.getEngine().reload();
        }

    }
}
