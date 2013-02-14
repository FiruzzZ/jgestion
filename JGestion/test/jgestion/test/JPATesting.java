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
import jpa.controller.FacturaCompraJpaController;
import jpa.controller.FacturaVentaJpaController;
import jpa.controller.ProductoJpaController;
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

    }

    private void updateCostoCompraYPrecioVentaSegunDetalleCompra() {
        List<FacturaCompra> fc = new FacturaCompraJpaController().findAll();
        ProductoJpaController pc = new ProductoJpaController();
        for (FacturaCompra facturaCompra : fc) {
            for (DetalleCompra d : facturaCompra.getDetalleCompraList()) {
                Producto p = d.getProducto();
                p.setCostoCompra(d.getPrecioUnitario());
                if (p.getUpdatePrecioVenta()) {
                    p.setPrecioVenta(d.getPrecioUnitario());
                }
                pc.merge(p);
            }
        }
    }
}
