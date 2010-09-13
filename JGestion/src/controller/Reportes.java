package controller;

import entity.DatosEmpresa;
import entity.UTIL;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
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

   private java.util.Map parameters;
   private final String pathReport;
   private final String tituloReporte;
   /**
    * sería algo así "./reportes/"
    */
   public static String FOLDER_REPORTES = "." + System.getProperty("file.separator") + "reportes" + System.getProperty("file.separator");
   private final DatosEmpresa d;
   private Boolean isViewerReport = null;

   /**
    *
    * @param pathReport archivoReporte.jasper. <b>FOLDER_REPORTES</b> + @pathReport si el reporte se encuentra en la carpeta "reportes"
    * @param title Título de la ventana del reporte
    * @throws Exception Si el archivo .jasper no se encuentra
    */
   public Reportes(String pathReport, String title) throws Exception {
      if (title == null || title.trim().length() < 1) {
         throw new Exception("El título del reporte can't be NULL");
      }

      if (pathReport == null || pathReport.trim().length() < 1) {
         throw new Exception("La ruta/URL/pathname no es válida!");
      }

      if (!new java.io.File(pathReport).exists()) {
         throw new Exception("No se encontró el archivo del reporte:\n" + pathReport);
      }
      parameters = new java.util.HashMap();
      this.pathReport = pathReport;
      this.tituloReporte = title;
      d = new DatosEmpresaJpaController().findDatosEmpresa(1);
   }

   public void viewReport() throws JRException {
      isViewerReport = true;
      new Thread(this).start();
//      net.sf.jasperreports.engine.JasperPrint jPrint;
//      jPrint = net.sf.jasperreports.engine.JasperFillManager.fillReport(pathReport, parameters, controller.DAO.getJDBCConnection());
//      net.sf.jasperreports.view.JasperViewer jViewer = new net.sf.jasperreports.view.JasperViewer(jPrint, false);
//      jViewer.setTitle(tituloReporte);
//      jViewer.setExtendedState(net.sf.jasperreports.view.JasperViewer.NORMAL);
//      jViewer.setAlwaysOnTop(true);
//      jViewer.setVisible(true);
   }

   public void printReport() throws JRException {
      isViewerReport = false;
      new Thread(this).start();
//      net.sf.jasperreports.engine.JasperPrint jPrint;
//      jPrint = net.sf.jasperreports.engine.JasperFillManager.fillReport(pathReport, parameters, controller.DAO.getJDBCConnection());
//      JasperPrintManager.printReport(jPrint, true);
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
      addParameter("ENTIDAD", d.getNombre());
   }

   /**
    * Add a parameter LOGO at the report!
    * if a Logo has been setted
    * @throws IOException
    */
   public void addLogo() throws IOException {
      if (d.getLogo() != null) {
         addParameter("LOGO", UTIL.imageToFile(d.getLogo(), "png"));
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

   public void run() {
      doReport();
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
            JasperPrintManager.printReport(jPrint, true);
         }
      } catch (JRException ex) {
         Logger.getLogger(Reportes.class.getName()).log(Level.SEVERE, null, ex);
      }
      System.out.println("Finished synchronized doReport()");
   }
}
