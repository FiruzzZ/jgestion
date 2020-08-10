package jgestion.controller;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.text.JTextComponent;
import jgestion.JGestion;
import jgestion.JGestionUtils;
import jgestion.controller.exceptions.MessageException;
import jgestion.entity.Producto;
import jgestion.entity.Proveedor;
import jgestion.entity.RemitoCompra;
import jgestion.entity.RemitoCompraDetalle;
import jgestion.entity.Sucursal;
import jgestion.entity.UsuarioAcciones;
import jgestion.gui.JDBuscadorReRe;
import jgestion.gui.JDRemitoCompra;
import jgestion.jpa.controller.ProductoAcuentaProveedorJpaController;
import jgestion.jpa.controller.ProductoJpaController;
import jgestion.jpa.controller.ProveedorJpaController;
import jgestion.jpa.controller.RemitoCompraJpaController;
import jgestion.jpa.controller.UsuarioAccionesJpaController;
import org.apache.logging.log4j.Logger;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import utilities.general.EntityWrapper;
import utilities.general.UTIL;
import utilities.gui.SwingUtil;
import utilities.swing.components.NumberRenderer;

/**
 *
 * @author FiruzzZ
 */
public class RemitoCompraController {

    private final RemitoCompraJpaController jpaController = new RemitoCompraJpaController();
    private JDBuscadorReRe buscador;

    public RemitoCompraController() {
    }

    public void displayABM(Window owner, RemitoCompra rc) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.COMPRA);
        UsuarioHelper uh = new UsuarioHelper();
        List<Sucursal> sucus = uh.getSucursales();
        if (sucus.isEmpty()) {
            throw new MessageException(JGestion.resourceBundle.getString("unassigned.sucursal"));
        }
        final JDRemitoCompra jd = new JDRemitoCompra(owner, true);
        UTIL.loadComboBox(jd.getCbSucursal(), JGestionUtils.getWrappedSucursales(sucus), false);
        UTIL.loadComboBox(jd.getCbProveedor(), JGestionUtils.getWrappedProveedores(new ProveedorJpaController().findAllLite()), false);
        UTIL.loadComboBox(jd.getCbProductos(), new ProductoController().findWrappedProductoToCombo(true), false);
        AutoCompleteDecorator.decorate(jd.getCbSucursal());
        AutoCompleteDecorator.decorate(jd.getCbProveedor());
        AutoCompleteDecorator.decorate(jd.getCbProductos());
        if (rc != null) {
            SwingUtil.resetJComponets(jd.getComponents());
            SwingUtil.setComponentsEnabled(jd.getComponents(), false, true);
            UTIL.setSelectedItem(jd.getCbProveedor(), rc.getProveedor());
            String numFactura = UTIL.AGREGAR_CEROS(rc.getNumero(), 12);
            jd.getTfFacturaCuarto().setText(numFactura.substring(0, 4));
            jd.getTfFacturaOcteto().setText(numFactura.substring(4));
            jd.getDcFechaRemito().setDate(rc.getFechaRemito());
            jd.getCheckAcuenta().setSelected(rc.isAcuenta());
            jd.getCheckAnulada().setSelected(rc.isAnulada());
            jd.getCheckActualizaStock().setSelected(rc.isActualizaStock());
            UTIL.setSelectedItem(jd.getCbSucursal(), rc.getSucursal());
            for (RemitoCompraDetalle item : rc.getDetalle()) {
                jd.addItem(item);
            }
            jd.getTfObservacion().setText(rc.getObservacion());
        }
        JTextComponent editor = (JTextComponent) jd.getCbProductos().getEditor().getEditorComponent();
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        Producto p = (Producto) UTIL.getEntityWrapped(jd.getCbProductos()).getEntity();
                        jd.setProducto(p);
                    } catch (ClassCastException ex) {
                    }
                }
            }
        });
        editor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                try {
                    Producto p = (Producto) UTIL.getEntityWrapped(jd.getCbProductos()).getEntity();
                    jd.setProducto(p);
                } catch (ClassCastException ex) {
                }
            }
        });
        jd.getTfProductoCodigo().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    Producto selectedProducto = new ProductoJpaController().findByCodigo(jd.getTfProductoCodigo().getText());
                    jd.setProducto(selectedProducto);
                }
            }
        });
        //<editor-fold defaultstate="collapsed" desc="uniqueness control (Proveedor y numero">
        jd.getTfFacturaCuarto().addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                Proveedor p = (Proveedor) UTIL.getEntityWrapped(jd.getCbProveedor()).getEntity();
                Long numero = jd.getNumero();
                if (numero == null) {
                    return;
                }
                try {
                    checkRemitoCompraUniqueness(p, numero, rc);
                } catch (MessageException ex) {
                    ex.displayMessage(jd);
                }
            }

        });
        jd.getTfFacturaCuarto().addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                Proveedor p = (Proveedor) UTIL.getEntityWrapped(jd.getCbProveedor()).getEntity();
                Long numero = jd.getNumero();
                if (numero == null) {
                    return;
                }
                try {
                    checkRemitoCompraUniqueness(p, numero, rc);
                } catch (MessageException ex) {
                    ex.displayMessage(jd);
                }
            }

        });
        //</editor-fold>
        jd.getBtnAceptar().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Proveedor p = (Proveedor) UTIL.getEntityWrapped(jd.getCbProveedor()).getEntity();
                    Sucursal sucursal = (Sucursal) UTIL.getEntityWrapped(jd.getCbSucursal()).getEntity();
                    Long numero = jd.getNumero();
                    if (numero == null) {
                        throw new MessageException("Número de remito no válido");
                    }
                    checkRemitoCompraUniqueness(p, numero, rc);
                    Date fecha = jd.getDcFechaRemito().getDate();
                    if (fecha == null) {
                        throw new MessageException("Fecha no válida");
                    }
                    List<RemitoCompraDetalle> items = jd.getItems();
                    if (items.isEmpty()) {
                        throw new MessageException("Debe contener al menos un item");
                    }
                    String observ = jd.getTfObservacion().getText().trim();
                    if (observ.isEmpty()) {
                        observ = null;
                    }
                    RemitoCompra r;
                    if (rc == null) {
                        r = new RemitoCompra();
                    } else {
                        r = rc;
                    }
                    for (RemitoCompraDetalle item : items) {
                        item.setRemitoCompra(r);
                    }
                    r.setProveedor(p);
                    r.setSucursal(sucursal);
                    r.setNumero(numero);
                    r.setObservacion(observ);
                    r.setAcuenta(jd.getCheckAcuenta().isSelected());
                    r.setActualizaStock(jd.getCheckActualizaStock().isSelected());
                    r.setFechaRemito(fecha);
                    r.getDetalle().addAll(items);
                    if (r.getId() == null) {
                        jpaController.persist(r);
                        if (r.isAcuenta()) {
                            new ProductoAcuentaProveedorJpaController().updateCuenta(r);
                        }
                        if (r.isActualizaStock()) {
                            //y también la variable Producto.stockActual
                            new StockController().updateStock(r);
                        }
                        String det
                                = "numero=" + numero + ", fechaRemito=" + UTIL.DATE_FORMAT.format(r.getFechaRemito()) + ", acuenta=" + r.isAcuenta()
                                + ", actualizaStock=" + r.isActualizaStock()
                                + ", proveedor=" + r.getProveedor().getNombre() + ", sucursal=" + r.getSucursal().getNombre() + ", detalle=" + parseDetalleToString(r.getDetalle());
                        UsuarioAcciones ua = UsuarioAccionesController.build(r, r.getId(), "Remito Compra N° " + JGestionUtils.getNumeracion(r, true), det, 'c');
                        new UsuarioAccionesController().create(ua);
                    } else {
                        jpaController.merge(r);
                        String det
                                = "numero=" + numero + ", fechaRemito=" + UTIL.DATE_FORMAT.format(r.getFechaRemito()) + ", acuenta=" + r.isAcuenta()
                                + ", actualizaStock=" + r.isActualizaStock()
                                + ", proveedor=" + r.getProveedor().getNombre() + ", sucursal=" + r.getSucursal().getNombre() + ", detalle=" + parseDetalleToString(r.getDetalle());
                        UsuarioAcciones ua = UsuarioAccionesController.build(r, r.getId(), "Remito Compra N° " + JGestionUtils.getNumeracion(r, true), det, 'u');
                        new UsuarioAccionesController().create(ua);
                    }
                    JOptionPane.showMessageDialog(jd, "Remito de Compra guardado");
                    jd.getBtnCancelar().doClick();
                } catch (MessageException ex) {
                    ex.displayMessage(jd);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(jd, "Error: " + ex.getMessage());
                    JGestion.LOG.error(ex, ex);
                }
            }

        });
        jd.getBtnCancelar().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (rc != null) {
                    jd.dispose();
                } else {
                    SwingUtil.resetJComponets(jd.getComponents());
                }
            }
        });
        jd.setVisible(true);
    }

    private void checkRemitoCompraUniqueness(Proveedor p, Long numero, RemitoCompra editing) throws MessageException {
        RemitoCompra old = jpaController.findBy(p, numero);
        if (old != null && (editing == null || !old.equals(editing))) {
            throw new MessageException("Ya existe el Remito de Compra N°: " + JGestionUtils.getNumeracion(old, true)
                    + " del proveedor " + p.getNombre());
        }
    }

    public static final String parseDetalleToString(List<RemitoCompraDetalle> detalle) {
        String s = "Items:";
        for (RemitoCompraDetalle item : detalle) {
            s += " Producto=" + item.getProducto().getNombre() + "; cantidad=" + item.getCantidad() + ";";
        }
        return s;
    }

    public void displayBuscador(Window frame, final boolean modal, final boolean toAnular) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.COMPRA);
        if (toAnular) {
            UsuarioController.checkPermiso(PermisosController.PermisoDe.ANULAR_COMPROBANTES);
        }
        buscador = new JDBuscadorReRe(frame, "Buscador - Remitos de compra", modal, "Proveedor", "Nº Remito");
        buscador.hideFactura();
        buscador.hideVendedor();
        buscador.getbImprimir().setVisible(true);
        UTIL.loadComboBox(buscador.getCbClieProv(), JGestionUtils.getWrappedProveedores(new ProveedorJpaController().findAllLite()), true);
        UTIL.loadComboBox(buscador.getCbSucursal(), JGestionUtils.getWrappedSucursales(new UsuarioHelper().getSucursales()), true);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"remitoID", "Nº Remito", "Proveedor", "Fecha", "Sucursal", "A Cuenta", "Usuario", "Fecha (Sistema)"},
                new int[]{1, 80, 150, 80, 100, 30, 80, 80},
                new Class<?>[]{null, null, null, null, null, Boolean.class, null, null}
        );
        //escondiendo facturaID
        TableColumnModel tc = buscador.getjTable1().getColumnModel();
        tc.getColumn(4).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        UTIL.hideColumnTable(buscador.getjTable1(), 0);
        UTIL.setHorizonalAlignment(buscador.getjTable1(), String.class, SwingConstants.RIGHT);
        buscador.getbBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    cargarTablaBuscador(armarQuery());
                } catch (MessageException ex) {
                    ex.displayMessage(buscador);
                }
            }
        });
        buscador.getjTable1().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    if (buscador.getjTable1().getSelectedRow() > -1) {
                        RemitoCompra remito = jpaController.find((Integer) buscador.getDtm().getValueAt(buscador.getjTable1().getSelectedRow(), 0));
                        try {
                            displayABM(buscador, remito);
                        } catch (MessageException ex) {
                            ex.displayMessage(buscador);
                        }
                    }
                }
            }
        });
        if (toAnular) {
            //solo buscar facturas NO anuladas
            buscador.getCheckAnulada().setEnabled(false);
        }
        buscador.getbBuscar().addActionListener((ActionEvent e) -> {
            try {
                cargarTablaBuscador(armarQuery());
            } catch (MessageException ex) {
                ex.displayMessage(buscador);
            }
        });
        buscador.setLocationRelativeTo(frame);
        buscador.setVisible(true);
    }

    private void cargarTablaBuscador(String nativeQuery) {
        DefaultTableModel dtm = buscador.getDtm();
        dtm.setRowCount(0);
        List<RemitoCompra> l = jpaController.findAll(nativeQuery);
        l.stream().forEach((remito) -> {
            UsuarioAcciones ua = new UsuarioAccionesJpaController().findCreate(remito, remito.getId());
            dtm.addRow(new Object[]{
                remito.getId(),
                JGestionUtils.getNumeracion(remito, true),
                remito.getProveedor().getNombre(),
                UTIL.DATE_FORMAT.format(remito.getFechaRemito()),
                remito.getSucursal().getNombre(),
                remito.isAcuenta(),
                (ua != null ? ua.getUsuario().getNick() : null),
                (ua != null ? UTIL.TIMESTAMP_FORMAT.format(ua.getFechasistema()) : null),});
        });
    }

    @SuppressWarnings("unchecked")
    private String armarQuery() throws MessageException {
        StringBuilder query = new StringBuilder(jpaController.getSelectFrom()
                + " WHERE o.anulada = " + buscador.isCheckAnuladaSelected());

        long numero;
        //filtro por nº de factura
        if (buscador.getTfCuarto().length() > 0 && buscador.getTfOcteto().length() > 0) {
            try {
                numero = Long.parseLong(buscador.getTfCuarto() + buscador.getTfOcteto());
                query.append(" AND o.numero = ").append(numero);
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de " + jpaController.getEntityClass().getSimpleName() + " no válido");
            }
        }
        if (buscador.getDcDesde() != null) {
            query.append(" AND o.fechaRemito >= '").append(UTIL.yyyy_MM_dd.format(buscador.getDcDesde())).append("'");
        }
        if (buscador.getDcHasta() != null) {
            query.append(" AND o.fechaRemito <= '").append(UTIL.yyyy_MM_dd.format(buscador.getDcHasta())).append("'");
        }
//        if (buscador.getDcDesdeSistema() != null) {
//            query.append(" AND o.fechaalta >= '").append(yyyyMMdd.format(UTIL.clearTimeFields(buscador.getDcDesdeSistema()))).append("'");
//        }
//        if (buscador.getDcHastaSistema() != null) {
//            query.append(" AND o.fechaalta <= '").append(yyyyMMdd.format(UTIL.clearTimeFields(buscador.getDcHastaSistema()))).append("'");
//        }
        if (buscador.getCbSucursal().getSelectedIndex() > 0) {
            query.append(" AND o.sucursal.id = ").append(((EntityWrapper<Sucursal>) buscador.getCbSucursal().getSelectedItem()).getId());
        } else {
            List<Sucursal> sucursales = new UsuarioHelper().getSucursales();
            query.append(" AND (");
            for (int i = 0; i < sucursales.size(); i++) {
                query.append(" o.sucursal.id=").append(sucursales.get(i).getId());
                if ((i + 1) < sucursales.size()) {
                    query.append(" OR ");
                }
            }
            query.append(")");
        }

        if (buscador.getCbClieProv().getSelectedIndex() > 0) {
            query.append(" AND o.proveedor.id = ").append(((EntityWrapper<Proveedor>) buscador.getCbClieProv().getSelectedItem()).getId());
        }

        query.append(" ORDER BY o.id");
        return query.toString();
    }
}
