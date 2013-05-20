package controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import javax.swing.JTable;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 *
 * @author FiruzzZ
 */
public class TableExcelExporter {

    private final JTable table;
    private List<Integer> columnIdxToSkip;
    private File file;
    private boolean skipColumnHeaders = false;
    private HSSFSheet sheet;
    // the style for Date column
    private CellStyle DATE_CELL_STYLE;
    private CellStyle TIMESTAMP_CELL_STYLE;
    // the style for Money cells
    private CellStyle MONEY_CELL_STYLE;
    // the style for Money cells with negative value
    private CellStyle NEGATIVE_MONEY_STYLE;
    // evaluator to evaluate formula cell
    private FormulaEvaluator EVALUATOR_STYLE;
    private HashMap<Integer, CellStyle> cellStylePerColumn;
    private HSSFWorkbook workBook;

    public TableExcelExporter(File file, JTable table) {
        this.table = table;
        this.file = file;
        columnIdxToSkip = new ArrayList<Integer>();
        cellStylePerColumn = new HashMap<Integer, CellStyle>();
        workBook = new HSSFWorkbook();
        sheet = workBook.createSheet();
    }

    public void addMergedRegion(int firstRow, int lastRow, int firstCol, int lastCol) {
        sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
    }

    public void setCellStyle(int column, String format) {
        setCellStyle(workBook, column, format);
    }

    public void addColumnsToSkipFromExport(int... colums) {
        for (int i : colums) {
            columnIdxToSkip.add(i);
        }
    }

    public void export() throws FileNotFoundException, IOException {
        createCellStylesAndEvaluator(workBook);
        if (!skipColumnHeaders) {
            addTitleFromColumnHeaders();
        }
        addData();
        // adjust column width
//        autosizeColumns(sheet);
        // save workbook as .xls file
        saveFile(workBook);
    }

    public void setSkipColumnHeaders(boolean skipColumnHeaders) {
        this.skipColumnHeaders = skipColumnHeaders;
    }

    private void addTitleFromColumnHeaders() {
        HSSFRow titleRow = sheet.createRow(0);
        for (int columnIdx = 0; columnIdx < table.getColumnCount(); columnIdx++) {
            HSSFCell celda = titleRow.createCell(columnIdx);
            celda.setCellValue(new HSSFRichTextString(table.getColumnModel().getColumn(columnIdx).getHeaderValue().toString()));
        }
    }

    private void addData() {
        for (int rowIdx = 1; rowIdx < table.getRowCount() - 1; rowIdx++) {
            HSSFRow row = sheet.createRow(rowIdx);
            for (int columnIdx = 0; columnIdx < table.getColumnCount(); columnIdx++) {
                if (columnIdxToSkip.contains(columnIdx)) {
                    continue;
                }
                HSSFCell cell = row.createCell(columnIdx);
                if (table.getValueAt(rowIdx, columnIdx) != null) {
                    Object value = table.getValueAt(rowIdx, columnIdx);
                    if (value instanceof Date) {
                        cell.setCellValue((Date) value);
                        cell.setCellStyle(DATE_CELL_STYLE);
                    } else if (value instanceof BigDecimal) {
                        cell.setCellValue(((BigDecimal) value).doubleValue());
                    } else if (value instanceof Double) {
                        cell.setCellValue((Double) value);
                    } else if (value instanceof Integer) {
                        cell.setCellValue((Integer) value);
                    } else {
                        cell.setCellValue(new HSSFRichTextString(value.toString()));
                    }
                    if (cellStylePerColumn.containsKey(columnIdx)) {
                        cell.setCellStyle(cellStylePerColumn.get(columnIdx));

                    }
                }
            }
        }
    }

    private void createCellStylesAndEvaluator(Workbook wb) {
        // CreationHelper for create CellStyle
        CreationHelper createHelper = wb.getCreationHelper();
        DATE_CELL_STYLE = wb.createCellStyle();
        // add date format
        DATE_CELL_STYLE.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy"));
        TIMESTAMP_CELL_STYLE.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy HH:mm:ss"));
        // vertical align top
//        _dateCellStyle.setVerticalAlignment(CellStyle.VERTICAL_TOP);

        // money style ( >= 0)
        MONEY_CELL_STYLE = wb.createCellStyle();
        MONEY_CELL_STYLE.setDataFormat(createHelper.createDataFormat().getFormat("$__##,##0.##"));
        // money style ( < 0)
        Font font = wb.createFont();
        font.setColor(Font.COLOR_RED);
        NEGATIVE_MONEY_STYLE = wb.createCellStyle();
        NEGATIVE_MONEY_STYLE.setDataFormat(createHelper.createDataFormat().getFormat("$__##,##0.##"));
        NEGATIVE_MONEY_STYLE.setFont(font);

        EVALUATOR_STYLE = wb.getCreationHelper().createFormulaEvaluator();
    }

    /**
     * set style to numeric cell
     *
     * @param cell
     * @param isFormula
     */
    private void setNumericStyle(Cell cell, boolean isFormula) {
        double value = isFormula ? getFormulaCellValue(cell) : cell.getNumericCellValue();
        if (value >= 0) {
            cell.setCellStyle(MONEY_CELL_STYLE);
        } else {
            cell.setCellStyle(NEGATIVE_MONEY_STYLE);
        }
    }

    /**
     * Evaluate formula cell value
     *
     * @param cell
     * @return
     */
    private double getFormulaCellValue(Cell cell) {
        EVALUATOR_STYLE.evaluateFormulaCell(cell);
        return cell.getNumericCellValue();
    }

    /**
     * adjust column width
     *
     * @param sheet
     */
    private void autosizeColumns(Sheet sheet) {
        // auto size not work with date
        sheet.setColumnWidth(1, 3000);
        sheet.autoSizeColumn(2);
    }

    private void saveFile(Workbook wb) throws FileNotFoundException, IOException {
        // create a new file
        FileOutputStream out = new FileOutputStream(file);
        // Write out the workbook
        wb.write(out);
        out.close();
    }

    /**
     *
     * @param formulaRow
     * @param formula ejemplo: "SUM(desde:hasta)"
     * @param colum
     */
    private void setFormula(Row formulaRow, String formula, int colum) {
        Cell gtb = formulaRow.createCell(colum);
        gtb.setCellType(HSSFCell.CELL_TYPE_FORMULA);
        gtb.setCellFormula(formula);
        setNumericStyle(gtb, true);
    }

    private void setCellStyle(Workbook wb, int column, String format) {
        // CreationHelper for create CellStyle
        CreationHelper createHelper = wb.getCreationHelper();
        CellStyle cs = wb.createCellStyle();
        // add date format
        cs.setDataFormat(createHelper.createDataFormat().getFormat(format));
        cellStylePerColumn.put(column, cs);
    }
}
