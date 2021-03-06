package jgestion.controller;

import generics.ProjectUtils;
import jgestion.controller.exceptions.MissingReportException;
import generics.WaitingDialog;
import java.awt.Dialog;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.JasperViewer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author FiruzzZ
 */
public class Reportes implements Runnable {

    private Map<String, Object> parameters;
    private final String pathReport;
    private final String tituloReporte;
    /**
     * sería algo así "./reportes/"
     */
    public static final String FOLDER_REPORTES = "." + System.getProperty("file.separator") + "reportes" + System.getProperty("file.separator");
    private Boolean isViewerReport = null;
    /**
     * Para saber si la ventana de impresión ya sucedió. (No si aceptó o canceló)
     */
    private boolean reporteFinalizado;
    /**
     * Si aparece el PrintDialog Default = true
     */
    private boolean withPrintDialog;
    private final WaitingDialog waitingDialog;
    private JRBeanCollectionDataSource dataSource;
    private static final Logger LOG = LogManager.getLogger();
    private JasperPrint jPrint;
    private final Icon impresoraIcon = ProjectUtils.getIcon("impresora.png");
    private boolean dinamycReport;
    private Thread reportThread;

    /**
     *
     * @param pathReport ruta absoluta del archivo .JASPER o solo el nombre del archivo .JASPER si
     * se encuentra en {@link Reportes#FOLDER_REPORTES} +
     * @pathReport
     * @param title Título de la ventana del reporte
     * @throws MissingReportException si el archivo .jasper no se encuentra
     */
    public Reportes(String pathReport, String title) throws MissingReportException {
        if (pathReport == null) {
            throw new NullPointerException("pathReport CAN'T BE NULL....no es válida!");
        }
        if (!pathReport.toLowerCase().contains(".jasper")) {
            pathReport = pathReport + ".jasper";
        }
        if (!new File(pathReport).exists()) {
            if (!new File(FOLDER_REPORTES + pathReport).exists()) {
                LOG.error("No sé encontro reporte " + pathReport + " ni " + FOLDER_REPORTES + pathReport);
                throw new MissingReportException("No se encontró el archivo del reporte: " + pathReport
                        + "\n" + FOLDER_REPORTES + pathReport);
            }
            pathReport = FOLDER_REPORTES + pathReport;
        }
        waitingDialog = new WaitingDialog(null, "Imprimiendo", false, "Preparando reporte....", impresoraIcon);
        parameters = new HashMap<>();
        this.pathReport = pathReport;
        tituloReporte = title;
        reporteFinalizado = false;
        withPrintDialog = true;
    }

    public Reportes(JasperPrint jp, boolean dinamycReport) {
        waitingDialog = new WaitingDialog(null, "Imprimiendo", false, "Preparando reporte....", impresoraIcon);
        pathReport = null;
        tituloReporte = null;
        jPrint = jp;
        this.dinamycReport = dinamycReport;
    }

    public void showWaitingDialog() {
        isViewerReport = false;
        reportThread = new Thread(this);
        reportThread.start();
    }

    public void setWaitingDialogMessage(String string) {
        waitingDialog.getLabelMessage().setText(string);
    }

    public void setjPrint(JasperPrint jPrint) {
        this.jPrint = jPrint;
    }

    public void viewReport() throws JRException {
        isViewerReport = true;
        if (reportThread == null) {
            reportThread = new Thread(this);
        }
        if (!reportThread.isAlive()) {
            reportThread.start();
        }
    }

    /**
     * Bla bla...
     *
     * @param withPrintDialog si aparece el PrintDialog o imprime directamente.
     * @return
     * @throws JRException
     */
    public boolean printReport(boolean withPrintDialog) throws JRException {
        isViewerReport = false;
        this.withPrintDialog = withPrintDialog;
        new Thread(this).start();
        reporteFinalizado = true;
        return reporteFinalizado;
    }

    public void printReport() throws JRException {
        isViewerReport = false;
        reportThread = new Thread(this);
        reportThread.start();
    }

    public void exportToPDF(String filePathSafer) throws JRException {
        jPrint = JasperFillManager.fillReport(pathReport, parameters, getConnection());
        JasperExportManager.exportReportToPdfFile(jPrint, filePathSafer);
    }

    public void addParameter(String key, Object parametro) {
        parameters.put(key, parametro);
    }

    public void setParameterMap(HashMap<String, Object> map) {
        parameters = map;
    }

    /**
     * Add a parameter CURRENT_USER to the report
     */
    public void addCurrent_User() {
        addParameter("CURRENT_USER", UsuarioController.getCurrentUser().getNick());
    }

    @Override
    public void run() {
        LOG.trace("Initializing Thread Reportes:" + pathReport);
        waitingDialog.setVisible(true);
        try {
            if (dinamycReport) {
                LOG.trace("Waiting JasperPrint for DynamicJasper..");
                while (!isViewerReport) {
                    waitingDialog.getLabelMessage().setText(waitingDialog.getLabelMessage().getText() + ".");
                    Thread.sleep(500);
                }
            }
            doReport();
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error de impresora", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, pathReport + "\n" + ex.getLocalizedMessage(), "Error generando reporte", JOptionPane.WARNING_MESSAGE);
            LOG.error("Error en Report, trying to close JDBC Connection: " + ex.getLocalizedMessage(), ex);
            try {
                DAO.getJDBCConnection().close();
            } catch (SQLException ex1) {
                LOG.error(ex1, ex1);
            }
        }
        LOG.trace("Finished Thread Reportes..");
    }

    private synchronized void doReport() throws PrinterException, JRException {
        LOG.trace("Running doReport()..");
        try {
            //DynamicJasper already set JasperPrinter
            if (jPrint == null) {
                jPrint = getJasperPrinter();
            }
            if (isViewerReport) {
                JasperViewer jViewer = new JasperViewer(jPrint, false);
                waitingDialog.dispose();
                jViewer.setTitle(tituloReporte);
                jViewer.setExtendedState(JasperViewer.NORMAL);
                jViewer.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
                jViewer.setVisible(true);
            } else {
                waitingDialog.dispose();
                JasperPrintManager.printReport(jPrint, withPrintDialog);
            }
        } catch (JRException ex) {
            if (ex.getCause() != null && ex.getCause().getClass().equals(PrinterException.class)) {
                throw new PrinterException("Impresora no disponible\n" + ex.getMessage());
            } else {
                throw ex;
            }
        } finally {
            if (waitingDialog.isVisible()) {
                waitingDialog.dispose();
            }
        }
        LOG.trace("Finished doReport()");
    }

    public boolean isReporteFinalizado() {
        return reporteFinalizado;
    }

    void addMembreteParameter() {
        parameters.put("SUBREPORT_DIR", FOLDER_REPORTES);
    }

    public JRBeanCollectionDataSource getBeanCollectionDataSource(Collection<?> data) {
        return new JRBeanCollectionDataSource(data);
    }

    void setDataSource(Collection<?> data) {
        dataSource = new JRBeanCollectionDataSource(data);
    }

    void addConnection() {
        parameters.put("REPORT_CONNECTION", getConnection());
    }

    public File exportToXLS(String excelFilePath) throws JRException, FileNotFoundException, IOException {
        waitingDialog.setTitle("Exportando..");
        if (!excelFilePath.substring(excelFilePath.length() - 4).equals(".xls")) {
            excelFilePath += ".xls";
        }
        waitingDialog.getLabelMessage().setText("Guardando en: " + excelFilePath);
        addParameter(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
        if (dataSource == null) {
            jPrint = JasperFillManager.fillReport(pathReport, parameters, getConnection());
        } else {
            jPrint = JasperFillManager.fillReport(pathReport, parameters, dataSource);
        }
        File f = new File(excelFilePath);
        JRExporter exporter = new JRXlsExporter();
        exporter.setParameter(JRExporterParameter.OUTPUT_FILE, f);
        exporter.setParameter(JRXlsExporterParameter.JASPER_PRINT, jPrint);
        exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
        exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_COLUMNS, Boolean.TRUE);
        exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
        exporter.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);
        exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
        exporter.setParameter(JRXlsExporterParameter.IGNORE_PAGE_MARGINS, Boolean.TRUE);
        exporter.exportReport();
        if (waitingDialog.isVisible()) {
            waitingDialog.dispose();
        }
        return f;
    }

    /**
     * Busca el .jasper y recupera o el .jrxml, compila y retorna el objecto {@link JasperReport}
     *
     * @param pathReport el nombre del archivo debe especificar la extensión jasper/jrxml
     * @param parameters
     * @return an instance of {@link JasperReport}
     * @throws FileNotFoundException
     * @throws JRException
     */
    public static JasperReport getJasperReport(String pathReport, Map<String, Object> parameters) throws FileNotFoundException, JRException {
        String path = pathReport;
        if (!new File(pathReport).exists()) {
            if (!new File(FOLDER_REPORTES + pathReport).exists()) {
                throw new FileNotFoundException("No se encontró el archivo del reporte: " + pathReport
                        + "\n" + FOLDER_REPORTES + pathReport);
            }
            path = FOLDER_REPORTES + pathReport;
        }
        JasperReport jasperReport;
        String extension = path.substring(path.lastIndexOf('.') + 1, path.length());
        if (extension.equalsIgnoreCase("jrxml")) {
            JasperDesign jasDesign = JRXmlLoader.load(path);
            jasperReport = JasperCompileManager.compileReport(jasDesign);
        } else {
            jasperReport = (JasperReport) JRLoader.loadObjectFromFile(path);
        }
        return jasperReport;
    }

    public JasperPrint getJasperPrinter() throws JRException {
        JasperPrint jp;
        if (dataSource == null) {
            jp = JasperFillManager.fillReport(pathReport, parameters, getConnection());
        } else {
            jp = JasperFillManager.fillReport(pathReport, parameters, dataSource);
        }
        return jp;
    }

    private Connection getConnection() {
        return jgestion.controller.DAO.getJDBCConnection();
    }

    /**
     * Agrega/appenda/concatena al final de la instancia actual de JasperPrint {@link #jPrint} hoja
     * por hoja el JasperPrint
     *
     * @param toAdd
     * @return la instancia actual + {@code toAdd}
     */
    public JasperPrint append(JasperPrint toAdd) {
        List<JRPrintPage> pages = toAdd.getPages();
        for (JRPrintPage page : pages) {
            jPrint.addPage(page);
        }
        return jPrint;
    }
}
