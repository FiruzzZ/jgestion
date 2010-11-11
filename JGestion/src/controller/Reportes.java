package controller;

import controller.exceptions.MissingReportException;
import entity.DatosEmpresa;
import generics.UTIL;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.view.JasperViewer;

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
    * Para saber si la ventana de impresión ya sucedió.
    * (No si aceptó o canceló)
    */
   private boolean reporteFinalizado;
   /**
    * Si aparece el PrintDialog
    * Default = true
    */
   private boolean withPrintDialog;

   /**
    *
    * @param pathReport ruta absoluta del archivo .JASPER o
    * solo el nombre del archivo .JASPER  si se encuentra en {@link Reportes#FOLDER_REPORTES} + @pathReport
    * @param title Título de la ventana del reporte
    * @throws Exception Si el archivo .jasper no se encuentra
    */
   public Reportes(String pathReport, String title) throws MissingReportException, Exception {
      if (title == null || title.trim().length() < 1) {
         throw new Exception("El título del reporte can't be NULL");
      }

      if (pathReport == null) {
         throw new Exception("La ruta/URL/pathname no es válida!");
      }

      if (!new File(pathReport).exists()) {
         System.out.println("FileNotFound = " + pathReport);
         if (!new File(FOLDER_REPORTES + pathReport).exists()) {
            throw new MissingReportException("No se encontró el archivo del reporte: " + pathReport
                    + "\n" + FOLDER_REPORTES + pathReport);
         }
         pathReport = FOLDER_REPORTES + pathReport;
      }
      parameters = new java.util.HashMap();
      this.pathReport = pathReport;
      this.tituloReporte = title;
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
    * @param withPrintDialog si aparece el PrintDialog o imprime directamente.
    * @throws JRException
    */
   public boolean printReport(boolean withPrintDialog) throws JRException {
      isViewerReport = false;
      this.withPrintDialog = withPrintDialog;
      doReport();
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
    * Add a parameter ENTIDAD al reporte.
    * Es decir el Nombre de la empresa, si existe
    * @throws IOException
    */
   public void addEntidad() throws IOException {
      addParameter("ENTIDAD", datosEmpresaController.getNombre());
   }

   /**
    * Add a parameter LOGO at the report!
    * if a Logo has been setted
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
      System.out.println("Initializing Thread Reportes..");
      try {
         doReport();
      } catch (Exception ex) {
         System.out.println("Error en Report, reset JDBC Connection..");
         try {
            DAO.getJDBCConnection().close();
         } catch (SQLException ex1) {
            Logger.getLogger(Reportes.class.getName()).log(Level.SEVERE, null, ex1);
         }
      }
      System.out.println("Finished Thread Reportes..");
   }

   private synchronized void doReport() {
      System.out.println("Running doReport()..");
      JasperPrint jPrint;
      try {
         jPrint = JasperFillManager.fillReport(pathReport, parameters, controller.DAO.getJDBCConnection());
         if (isViewerReport) {
            JasperViewer jViewer = new JasperViewer(jPrint, false);
            jViewer.setTitle(tituloReporte);
            jViewer.setExtendedState(JasperViewer.NORMAL);
            jViewer.setAlwaysOnTop(true);
            jViewer.setVisible(true);
         } else {
            JasperPrintManager.printReport(jPrint, withPrintDialog);
         }
      } catch (JRException ex) {
         Logger.getLogger(Reportes.class.getName()).log(Level.SEVERE, null, ex);
      }
      System.out.println("Finished doReport()");
   }

   public boolean isReporteFinalizado() {
      return reporteFinalizado;
   }
}
