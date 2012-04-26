package controller;

import controller.exceptions.MissingReportException;
import entity.DatosEmpresa;
import generics.WaitingDialog;
import utilities.general.UTIL;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JDialog;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.view.JasperViewer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author FiruzzZ
 */
public class Reportes implements Runnable {

    private Map parameters;
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
        parameters = new HashMap();
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

    public void addParameter(Object key, Object parametro) {
        parameters.put(key, parametro);
    }

    public void setParameterMap(java.util.HashMap map) {
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
        addParameter("CURRENT_USER", UsuarioJpaController.getCurrentUser().getNick());
    }

    @Override
    public void run() {
        Logger.getLogger(Reportes.class).trace("Initializing Thread Reportes..");
        try {
            doReport();
        } catch (Exception ex) {
            Logger.getLogger(Reportes.class).trace("Error en Report, trying to close JDBC Connection..");
            try {
                DAO.getJDBCConnection().close();
            } catch (SQLException ex1) {
                Logger.getLogger(Reportes.class).log(Level.ERROR, null, ex1);
            }
            Logger.getLogger(Reportes.class).log(Level.ERROR, null, ex);
        }
        Logger.getLogger(Reportes.class).trace("Finished Thread Reportes..");
    }

    private synchronized void doReport() {
        Logger.getLogger(Reportes.class).trace("Running doReport()..");
        JasperPrint jPrint;
        try {
            jPrint = JasperFillManager.fillReport(pathReport, parameters, controller.DAO.getJDBCConnection());
            if (isViewerReport) {
                JasperViewer jViewer = new JasperViewer(jPrint, false);
                jd.dispose();
                jViewer.setTitle(tituloReporte);
                jViewer.setExtendedState(JasperViewer.NORMAL);
                jViewer.setAlwaysOnTop(true);
                jViewer.setVisible(true);
            } else {
                jd.dispose();
                JasperPrintManager.printReport(jPrint, withPrintDialog);
            }
        } catch (JRException ex) {
            Logger.getLogger(Reportes.class).log(Level.ERROR, "Se pudrió todo con el reporte", ex);
        }
        Logger.getLogger(Reportes.class).trace("Finished doReport()");
    }

    public boolean isReporteFinalizado() {
        return reporteFinalizado;
    }

    void addEmpresaReport() {
        parameters.put("SUBREPORT_DIR", FOLDER_REPORTES);
    }
}
