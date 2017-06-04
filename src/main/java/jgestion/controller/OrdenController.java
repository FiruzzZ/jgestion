package jgestion.controller;

import jgestion.controller.exceptions.MessageException;
import jgestion.controller.exceptions.MissingReportException;
import jgestion.entity.Orden;
import java.awt.event.MouseEvent;
import jgestion.entity.DetalleOrden;
import jgestion.entity.Producto;
import jgestion.entity.Sucursal;
import generics.AutoCompleteComboBox;
import java.awt.Window;
import utilities.general.UTIL;
import jgestion.gui.JDBuscador;
import jgestion.gui.JDFacturaCompra;
import jgestion.gui.PanelBuscadorOrdenes;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import jgestion.JGestionUtils;
import jgestion.jpa.controller.JGestionJpaImpl;
import jgestion.jpa.controller.ProductoJpaController;
import net.sf.jasperreports.engine.JRException;
import org.apache.logging.log4j.LogManager;
import utilities.general.EntityWrapper;

/**
 *
 * @author FiruzzZ
 */
public class OrdenController {

    public static final String CLASS_NAME = Orden.class.getSimpleName();
    private JDFacturaCompra jdFactura;
    private Producto producto_selected;
    private JDBuscador buscador;
    private PanelBuscadorOrdenes panel;
    private Orden selectedOrden;
    private final JGestionJpaImpl<Orden, Integer> jpaController = new JGestionJpaImpl<Orden, Integer>() {
    };

    public OrdenController() {
    }

    private int getNextNumeroOrden() {
        return (int) jpaController.findAttribute("select COALESCE(MAX(o.numero)+1, 1) from Orden as o");
    }

    public void initOrden(JFrame owner, boolean visible) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.ORDENES_IO);
        jdFactura = new JDFacturaCompra(owner, true);
        jdFactura.getBtnAnular().setVisible(false);
        jdFactura.setUIToOrden();
        jdFactura.setTitle("ORDEN de Entrada/Salida");
        UTIL.getDefaultTableModel(jdFactura.getjTable1(),
                new String[]{"entity", "CÓDIGO", "PRODUCTO", "CANT."},
                new int[]{1, 80, 150, 20});
        UTIL.hideColumnTable(jdFactura.getjTable1(), 0);
        if (visible) {
            UTIL.loadComboBox(jdFactura.getCbSucursal(), JGestionUtils.getWrappedSucursales(new UsuarioHelper().getSucursales()), false);
            UTIL.loadComboBox(jdFactura.getCbProductos(), new ProductoController().findWrappedProductoToCombo(true), false);
            // <editor-fold defaultstate="collapsed" desc="ajuste de foco, problemas de GUI">
            jdFactura.getTfCantidad().addFocusListener(new FocusAdapter() {

                @Override
                public void focusLost(FocusEvent e) {
                    jdFactura.getBtnADD().requestFocus();
                }
            });// </editor-fold>
            jdFactura.getCbProductos().setEditable(true);
            JTextComponent editor = (JTextComponent) jdFactura.getCbProductos().getEditor().getEditorComponent();
            // change the editor's documenteishon
            editor.setDocument(new AutoCompleteComboBox(jdFactura.getCbProductos()));
            editor.addFocusListener(new FocusListener() {

                @Override
                public void focusGained(FocusEvent e) {
                }

                @Override
                public void focusLost(FocusEvent e) {
                    try {
                        @SuppressWarnings("unchecked")
                        EntityWrapper<Producto> wrapper = (EntityWrapper<Producto>) jdFactura.getCbProductos().getSelectedItem();
                        producto_selected = new ProductoJpaController().find((Integer) wrapper.getId());
                        setProducto(producto_selected);
                        //problemas de GUI design, sino el foco se va a la mierda..
                        if (producto_selected != null) {
                            jdFactura.getTfCantidad().requestFocus();
                        }
                    } catch (ClassCastException ex) {
                        //cuando no seleccionó ningún Producto del combo
                        jdFactura.setTfPrecioUnitario("");
                        jdFactura.setTfProductoIVA("");
                    }
                }
            });
            editor.addKeyListener(new KeyAdapter() {

                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == 10) {
                        try {
                            @SuppressWarnings("unchecked")
                            EntityWrapper<Producto> wrapper = (EntityWrapper<Producto>) jdFactura.getCbProductos().getSelectedItem();
                            producto_selected = new ProductoJpaController().find((Integer) wrapper.getId());
                            setProducto(producto_selected);
                            if (producto_selected != null) {
                                jdFactura.setTfProductoPrecioActual(new StockController().getStockGlobal(producto_selected.getId()).toString());
                            }
                        } catch (ClassCastException ex) {
                            jdFactura.setTfProductoPrecioActual("");
                            jdFactura.setTfPrecioUnitario("");
                            jdFactura.setTfProductoIVA("");
                        }
                    }
                }
            });
            jdFactura.getBtnADD().addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        int cantidad = Integer.valueOf(jdFactura.getTfCantidad().getText());
                        if (producto_selected != null) {
                            UTIL.getDtm(jdFactura.getjTable1()).addRow(new Object[]{
                                producto_selected,
                                producto_selected.getCodigo(),
                                producto_selected.getNombre(),
                                cantidad
                            });
                        }
                    } catch (NumberFormatException ex) {
                        jdFactura.showMessage("Cantidad no válida", null, 2);
                    }

                }
            });
            jdFactura.getBtnDEL().addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (jdFactura.getjTable1().getSelectedRow() > -1) {
                        jdFactura.getDtm().removeRow(jdFactura.getjTable1().getSelectedRow());
                    }
                }
            });
            jdFactura.getTfProductoCodigo().addKeyListener(new KeyAdapter() {

                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        producto_selected = new ProductoController().findProductoByCodigo(jdFactura.getTfProductoCodigo().getText().trim());
                        setProducto(producto_selected);
                    }
                }
            });
            jdFactura.getBtnAceptar().addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        Orden orden = setAndPersist();
                        jdFactura.showMessage("Orden Nº" + orden.getNumero() + " registrada", null, 1);
                        doReport(orden.getId());
                        UTIL.limpiarDtm(jdFactura.getjTable1());
                        producto_selected = null;
                    } catch (MessageException ex) {
                        jdFactura.showMessage(ex.getMessage(), "Error", 2);
                    } catch (Exception ex) {
                        jdFactura.showMessage(ex.getMessage(), "Error", 2);
                        LogManager.getLogger();//(OrdenController.class.getSimpleName()).error(ex, ex);
                    }
                }
            });
        }

        jdFactura.pack();
        jdFactura.setLocationRelativeTo(owner);
        jdFactura.setVisible(visible);
    }

    private void setProducto(Producto producto) {
        if (producto != null) {
            jdFactura.labelCodigoNoRegistrado(false);
            jdFactura.setTfProductoCodigo(producto.getCodigo());
            UTIL.setSelectedItem(jdFactura.getCbProductos(), producto.getNombre());
            jdFactura.setTfProductoPrecioActual(String.valueOf(producto.getStockactual()));
            jdFactura.getTfCantidad().requestFocus();
        } else {
            jdFactura.labelCodigoNoRegistrado(true);
            jdFactura.setTfProductoPrecioActual("");
        }
    }

    private Orden setAndPersist() throws MessageException, Exception {
        int rowCant = jdFactura.getDtm().getRowCount();
        if (rowCant < 1) {
            throw new MessageException("Y los productos a modificar?");
        }

        Orden orden = new Orden();
        try {
            @SuppressWarnings("unchecked")
            EntityWrapper<Sucursal> wrapper = (EntityWrapper<Sucursal>) jdFactura.getCbSucursal().getSelectedItem();
            orden.setSucursal(wrapper.getEntity());
        } catch (ClassCastException ex) {
            throw new MessageException("Sucursal no válida");
        }
        orden.setNumero(getNextNumeroOrden());
        orden.setUsuario(UsuarioController.getCurrentUser());
        orden.setDetalleOrdenList(new ArrayList<DetalleOrden>(rowCant));
        for (int i = 0; i < rowCant; i++) {
            DetalleOrden detalleOrden = new DetalleOrden();
            detalleOrden.setOrden(orden);
            detalleOrden.setProducto((Producto) jdFactura.getjTable1().getModel().getValueAt(i, 0));
            detalleOrden.setCantidad(Integer.valueOf(jdFactura.getjTable1().getModel().getValueAt(i, 3).toString()));
            orden.getDetalleOrdenList().add(detalleOrden);
        }
        jpaController.persist(orden);
        StockController stockController = new StockController();
        for (DetalleOrden item : orden.getDetalleOrdenList()) {
            stockController.modificarStockBySucursal(item.getProducto(), orden.getSucursal(), item.getCantidad());
        }
        return orden;
    }

    public void displayBuscador(Window owner) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.ORDENES_IO);
        panel = new PanelBuscadorOrdenes();
        UTIL.loadComboBox(panel.getCbSucursales(), JGestionUtils.getWrappedSucursales(new UsuarioHelper().getSucursales()), "<Todas>");
        buscador = new JDBuscador(owner, "Buscador de " + CLASS_NAME, true, panel);
        UTIL.getDefaultTableModel(buscador.getjTable1(),
                new String[]{"entity", "Nº", "Sucursal", "Usuario", "Fecha"},
                new int[]{1, 40, 60, 60, 80});
        UTIL.hideColumnTable(buscador.getjTable1(), 0);
        buscador.getBtnImprimir().setVisible(false);
        buscador.getBtnToExcel().setVisible(false);
        buscador.getjTable1().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    try {
                        selectedOrden = (Orden) UTIL.getSelectedValue(buscador.getjTable1(), 0);
                        setDatos(selectedOrden);
                    } catch (MessageException ex) {
                        //ignored...
                    }
                }
            }
        });
        buscador.getBtnBuscar().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                armarQuery();
            }
        });

        buscador.setVisible(true);
    }

    @SuppressWarnings("unchecked")
    private void armarQuery() {
        StringBuilder query = new StringBuilder(jpaController.getSelectFrom() + " WHERE o.id is not null ");

        if (panel.getCbSucursales().getSelectedIndex() > 0) {
            EntityWrapper<Sucursal> cbw = (EntityWrapper<Sucursal>) panel.getCbSucursales().getSelectedItem();
            query.append(" AND o.sucursal.id= ").append(cbw.getId());
        } else {
            query.append(" AND (");
            for (int i = 1; i < panel.getCbSucursales().getItemCount(); i++) {
                EntityWrapper<Sucursal> cbw = (EntityWrapper<Sucursal>) panel.getCbSucursales().getItemAt(i);
                query.append(" o.sucursal.id=").append(cbw.getId());
                if ((i + 1) < panel.getCbSucursales().getItemCount()) {
                    query.append(" OR ");
                }
            }
            query.append(")");
        }
        if (panel.getDcDesde().getDate() != null) {
            query.append(" AND o.fecha >= '").append(panel.getDcDesde().getDate()).append("'");
        }
        if (panel.getDcHasta().getDate() != null) {
            query.append(" AND o.fecha <= '").append(panel.getDcHasta().getDate()).append("'");
        }

        query.append(" ORDER BY o.id");
        cargarTablaBuscador(query.toString());
    }

    private void cargarTablaBuscador(String query) {
        buscador.getDtm().setRowCount(0);
        List<Orden> l = jpaController.findAll(query);
        for (Orden orden : l) {
            buscador.getDtm().addRow(new Object[]{
                orden,
                orden.getNumero(),
                orden.getSucursal().getNombre(),
                orden.getUsuario(),
                UTIL.TIMESTAMP_FORMAT.format(orden.getFecha())
            });
        }
    }

    private void setDatos(Orden orden) throws MessageException {
        if (jdFactura == null) {
            initOrden(null, false);
            jdFactura.getBtnADD().setEnabled(false);
            jdFactura.getBtnDEL().setEnabled(false);
            jdFactura.getBtnAceptar().setEnabled(false);
            jdFactura.getBtnCancelar().setEnabled(false);
            jdFactura.getBtnAnular().setVisible(false);
            jdFactura.getTfNumMovimiento().setVisible(true);
            jdFactura.getCbProductos().setEnabled(false);
            jdFactura.getTfProductoCodigo().setEnabled(false);
            jdFactura.getTfCantidad().setEnabled(false);
            jdFactura.pack();
        }
        jdFactura.getTfNumMovimiento().setText(orden.getNumero() + "");
        jdFactura.getCbSucursal().removeAllItems();
        jdFactura.getCbSucursal().addItem(new EntityWrapper<Sucursal>(orden.getSucursal(), orden.getId(), orden.getSucursal().getNombre()));
        DefaultTableModel dtm = (DefaultTableModel) jdFactura.getjTable1().getModel();
        dtm.setRowCount(0);
        for (DetalleOrden detalleOrden : orden.getDetalleOrdenList()) {
            dtm.addRow(new Object[]{
                detalleOrden.getProducto(),
                detalleOrden.getProducto().getCodigo(),
                detalleOrden.getProducto().getNombre() + " " + detalleOrden.getProducto().getMarca().getNombre(),
                detalleOrden.getCantidad()
            });
        }
        jdFactura.setVisible(true);
    }

    private void doReport(int numeroOrden) throws MissingReportException, JRException {
        Reportes r = new Reportes("JGestion_ordenio.jasper", "Orden IO N°" + numeroOrden);
        r.addMembreteParameter();
        r.addParameter("ORDEN_ID", numeroOrden);
        r.printReport(true);
    }
}
