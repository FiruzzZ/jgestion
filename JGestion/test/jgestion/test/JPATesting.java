package jgestion.test;

import controller.DAO;
import controller.IvaController;
import entity.*;
import generics.PropsUtils;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import jpa.controller.FacturaVentaJpaController;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author Administrador
 */
public class JPATesting {

    public static void main(String[] args) {
        try {
            PropertyConfigurator.configure("log4j.properties");
            Properties properties = PropsUtils.load(new File("cfg.ini"));
            DAO.setProperties(properties);
            new JPATesting();
        } catch (IOException ex) {
            Logger.getLogger(JPATesting.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public JPATesting() {
        List<Iva> ivas = new IvaController().findIvaEntities();
        for (Iva iva : ivas) {
            System.out.println(iva.getId() + ", " + iva.getIva());
        }
//        List<Sucursal> sucursales = new SucursalJpaController().findAll();
//        for (Sucursal sucursal : sucursales) {
//            System.out.println(sucursal.toString());
//        }
        FacturaVentaJpaController fvjpa = new FacturaVentaJpaController();
        List<FacturaVenta> findByQuery = fvjpa.findByQuery("SELECT o FROM " + fvjpa.getEntityClass().getSimpleName() + " o WHERE o.id >608 and o.id < 611");
        for (FacturaVenta fv : findByQuery) {
            System.out.println(fv.toString());
            for (DetalleVenta detalleVenta : fv.getDetallesVentaList()) {
                System.out.println(detalleVenta.toString());
                Producto producto = detalleVenta.getProducto();
                System.out.println(producto.toString());
                System.out.println(producto.getIva());
            }
        }

    }
}
