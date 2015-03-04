package jgestion.test;

import jgestion.controller.DAO;
import jgestion.controller.exceptions.MissingReportException;
import jgestion.entity.DetalleVenta;
import jgestion.entity.FacturaVenta;
import generics.PropsUtils;
import java.awt.Dialog;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgestion.jpa.controller.FacturaVentaJpaController;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author Administrador
 */
public class Reporteando {

    public static void main(String[] args) {
        try {
            PropertyConfigurator.configure("log4j.properties");
            Properties properties = PropsUtils.load(new File("cfg.ini"));
            DAO.setProperties(properties);
//            new Reporteando();
        } catch (Exception ex) {
            Logger.getLogger(JPATesting.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Reporteando() throws MissingReportException, JRException {
//        Reportes reportes = new Reportes("D:\\Mis Documentos\\Reportes\\JGestion\\empty_detail.jasper", "asdfasdf");
        FacturaVenta fv = new FacturaVentaJpaController().find(620);
        List<DetalleVenta> list = new ArrayList<DetalleVenta>(10);
        for (DetalleVenta detalleVenta : fv.getDetallesVentaList()) {
            list.add(detalleVenta);
        }
        int completar = list.size();
        for (int i = completar; i < 10; i++) {
            new DetalleVenta(-1);
//            list.add();

        }
        JRDataSource datos = new JRBeanCollectionDataSource(list);
        JasperPrint jprint = JasperFillManager.fillReport("D:\\Mis Documentos\\Reportes\\JGestion\\empty_detail.jasper",
                new HashMap<String, Object>(), datos);
        JasperViewer jViewer = new JasperViewer(jprint, false);
        jViewer.setExtendedState(JasperViewer.NORMAL);
        jViewer.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        jViewer.setVisible(true);
    }
}
