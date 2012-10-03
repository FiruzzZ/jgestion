package controller;

import controller.exceptions.MissingReportException;
import entity.DatosEmpresa;
import generics.WaitingDialog;
import java.awt.Dialog;
import java.awt.print.PrinterException;
import utilities.general.UTIL;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

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
    public static String FOLDER_REPORTES = "." + System.getProperty("file.separator") + "reportes" + System.getProperty("file.separator");
    private final DatosEmpresa datosEmpresaController;
    private Boolean isViewerReport = null;
    /**
     * Para saber si la ventana de impresión ya sucedió. (No si aceptó o
     * canceló)
     */
    private boolean reporteFinalizado;
    /**
     * Si aparece el PrintDialog Default = true
     */
    private boolean withPrintDialog;
    private JDialog jd;
    private JRBeanCollectionDataSource beanCollectionDataSource;
    private static final Logger LOG = Logger.getLogger(Reportes.class.getName());

    /**
     *
     * @param pathReport ruta absoluta del archivo .JASPER o solo el nombre del
     * archivo .JASPER si se encuentra en {@link Reportes#FOLDER_REPORTES} +
     * @pathReport
     * @param title Título de la ventana del reporte
     * @throws MissingReportException
     * @throws Exception Si el archivo .jasper no se encuentra
     */
    public Reportes(String pathReport, String title) throws MissingReportException {

        if (pathReport == null) {
            throw new NullPointerException("pathReport CAN'T BE NULL....no es válida!");
        }

        if (!new File(pathReport).exists()) {
            if (!new File(FOLDER_REPORTES + pathReport).exists()) {
                throw new MissingReportException("No se encontró el archivo del reporte: " + pathReport
                        + "\n" + FOLDER_REPORTES + pathReport);
            }
            pathReport = FOLDER_REPORTES + pathReport;
        }
        jd = new WaitingDialog((JDialog) null, "Imprimiendo", false, "Preparando reporte....");
        jd.setVisible(true);
        parameters = new HashMap<String, Object>();
        this.pathReport = pathReport;
        tituloReporte = title;
        reporteFinalizado = false;
        withPrintDialog = true;
        datosEmpresaController = new DatosEmpresaJpaController().findDatosEmpresa(1);
    }

    public void viewReport() throws JRException {
        isViewerReport = true;
        new Thread(this).start();
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
        new Thread(this).start();
    }

    public void exportPDF(String filePathSafer) throws JRException {
        JasperPrint jprint;
        jprint = JasperFillManager.fillReport(pathReport, parameters, controller.DAO.getJDBCConnection());
        JasperExportManager.exportReportToPdfFile(jprint, filePathSafer);
    }

    public void addParameter(String key, Object parametro) {
        parameters.put(key, parametro);
    }

    public void setParameterMap(HashMap<String, Object> map) {
        parameters = map;
    }

    /**
     * Add a parameter ENTIDAD al reporte. Es decir el Nombre de la empresa, si
     * existe
     *
     * @throws IOException
     */
    public void addEntidad() throws IOException {
        addParameter("ENTIDAD", datosEmpresaController.getNombre());
    }

    /**
     * Add a parameter LOGO at the report! if a Logo has been setted
     *
     * @throws IOException
     */
    public void addLogo() throws IOException {
        if (datosEmpresaController.getLogo() != null) {
            addParameter("LOGO", UTIL.imageToFile(datosEmpresaController.getLogo(), "png"));
        } else {
            addParameter("LOGO", null);
        }
    }

    /**
     * Add a parameter CURRENT_USER to the report
     */
    public void addCurrent_User() {
        addParameter("CURRENT_USER", UsuarioController.getCurrentUser().getNick());
    }

    @Override
    public void run() {
        LOG.trace("Initializing Thread Reportes..");
        try {
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

    private synchronized void doReport() throws PrinterException {
        LOG.trace("Running doReport()..");
        JasperPrint jPrint;
        try {
            if (beanCollectionDataSource == null) {
                jPrint = JasperFillManager.fillReport(pathReport, parameters, controller.DAO.getJDBCConnection());
            } else {
                jPrint = JasperFillManager.fillReport(pathReport, parameters, beanCollectionDataSource);
            }
            if (isViewerReport) {
                JasperViewer jViewer = new JasperViewer(jPrint, false);
                jd.dispose();
                jViewer.setTitle(tituloReporte);
                jViewer.setExtendedState(JasperViewer.NORMAL);
                jViewer.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
                jViewer.setVisible(true);
            } else {
                jd.dispose();
                JasperPrintManager.printReport(jPrint, withPrintDialog);
            }
        } catch (JRException ex) {
            if (ex.getCause().getClass().equals(PrinterException.class)) {
                throw new PrinterException("Impresora no disponible\n" + ex.getMessage());
            } else {
                LOG.error("Se pudrió todo con el reporte", ex);
            }
        } finally {
            if (jd.isVisible()) {
                jd.dispose();
            }
        }
        LOG.trace("Finished doReport()");
    }

    public boolean isReporteFinalizado() {
        return reporteFinalizado;
    }

    void addEmpresaReport() {
        parameters.put("SUBREPORT_DIR", FOLDER_REPORTES);
    }

    void setDataSource(Collection<?> data) {
        beanCollectionDataSource = new JRBeanCollectionDataSource(data);
    }

    void addConnection() {
        parameters.put("REPORT_CONNECTION", controller.DAO.getJDBCConnection());
    }
}
