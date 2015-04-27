package jgestion.controller;

import java.awt.Dialog;
import java.awt.Window;
import java.util.List;
import javax.persistence.NoResultException;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import jgestion.entity.DetalleCompra;
import jgestion.entity.FacturaCompra;
import jgestion.entity.ProductoAcuentaProveedor;
import jgestion.entity.Proveedor;
import jgestion.jpa.controller.ProductoAcuentaProveedorJpaController;
import utilities.general.UTIL;

/**
 *
 * @author FiruzzZ
 */
public class ProductoAcuentaProveedorController {

    private final ProductoAcuentaProveedorJpaController jpaController = new ProductoAcuentaProveedorJpaController();

    public ProductoAcuentaProveedorController() {
    }

    void displayCuenta(Window owner, Proveedor proveedor) {
        JTable table = new JTable();
        UTIL.getDefaultTableModel(table,
                new String[]{"Producto", "Cuenta"},
                new int[]{200, 50},
                new Class<?>[]{null, Integer.class});
        List<ProductoAcuentaProveedor> l = new ProductoAcuentaProveedorJpaController().findAcuenta(proveedor);
        DefaultTableModel dtm = (DefaultTableModel) table.getModel();
        for (ProductoAcuentaProveedor pap : l) {
            dtm.addRow(new Object[]{pap.getProducto().getNombre(), pap.getCantidad()});
        }
        JScrollPane scroll = new JScrollPane(table);
        JDialog jd = new JDialog(owner, "Productos a cuenta de: " + proveedor.getNombre(), Dialog.ModalityType.MODELESS);
        jd.setAlwaysOnTop(true);
        jd.getContentPane().add(scroll);
        jd.pack();
        jd.setLocationRelativeTo(null);
        jd.setVisible(true);
    }

    void updateCuenta(FacturaCompra facturaCompra) {
        for (DetalleCompra item : facturaCompra.getDetalleCompraList()) {
            try {
                ProductoAcuentaProveedor pap = jpaController.findAcuenta(facturaCompra.getProveedor(), item.getProducto());
                if (item.getCantidad() > pap.getCantidad()) {
                    pap.setCantidad(0);
                } else {
                    pap.setCantidad(pap.getCantidad() - item.getCantidad());
                }
                jpaController.merge(pap);
            } catch (NoResultException ex) {
                //no todo lo que se paga en una factura puede estar "Acuenta"
            }
        }
    }

}
