package jgestion.controller;

import jgestion.controller.exceptions.DatabaseErrorException;
import jgestion.controller.exceptions.MissingReportException;
import jgestion.controller.exceptions.MessageException;
import jgestion.entity.Caja;
import jgestion.entity.CajaMovimientos;
import jgestion.entity.Cuenta;
import jgestion.entity.DetalleCompra;
import jgestion.entity.Dominio;
import jgestion.entity.FacturaCompra;
import jgestion.entity.Iva;
import jgestion.entity.Proveedor;
import jgestion.entity.Sucursal;
import java.util.Arrays;
import jgestion.entity.Producto;
import jgestion.entity.SubCuenta;
import jgestion.entity.UnidadDeNegocio;
import generics.AutoCompleteComboBox;
import generics.GenericBeanCollection;
import utilities.general.UTIL;
import jgestion.gui.JDBuscadorReRe;
import jgestion.gui.JDFacturaCompra;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.persistence.NoResultException;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;
import jgestion.ActionListenerManager;
import jgestion.JGestionUtils;
import jgestion.JGestion;
import jgestion.Wrapper;
import jgestion.entity.FacturaCompra_;
import jgestion.jpa.controller.CajaMovimientosJpaController;
import jgestion.jpa.controller.DominioJpaController;
import jgestion.jpa.controller.FacturaCompraJpaController;
import jgestion.jpa.controller.ProductoJpaController;
import jgestion.jpa.controller.ProveedorJpaController;
import jgestion.jpa.controller.SubCuentaJpaController;
import jgestion.jpa.controller.UnidadDeNegocioJpaController;
import net.sf.jasperreports.engine.JRException;
import org.apache.log4j.Logger;
import utilities.gui.SwingUtil;
import utilities.swing.components.ComboBoxWrapper;
import utilities.swing.components.FormatRenderer;
import utilities.swing.components.NumberRenderer;

/**
 *
 * @author FiruzzZ
 */
public class FacturaCompraController implements ActionListener, KeyListener {

    public static final List<String> TIPOS_FACTURA;
    public static final List<String> FORMAS_PAGO;
    public static final String CLASS_NAME = FacturaCompra.class.getSimpleName();
    private final String[] colsName = {"IVA", "Cód. Producto", "Producto", "Cantidad", "Precio U.", "Sub total", "Mod", "Producto.instance"};
    private final int[] colsWidth = {1, 70, 180, 10, 30, 30, 1, 1};
    private JDFacturaCompra jdFactura;
    private Producto selectedProducto;
    private FacturaCompra EL_OBJECT;
    private JDBuscadorReRe buscador;
    private static final Logger LOG = Logger.getLogger(FacturaCompraController.class.getName());
    private final FacturaCompraJpaController jpaController = new FacturaCompraJpaController();

    static {
        String[] tipos = {"A", "B", "C", "M", "X", "O"};
        TIPOS_FACTURA = Arrays.asList(tipos);
        String[] formas = {"Contado", "Cta. Cte."};
        FORMAS_PAGO = Arrays.asList(formas);
    }

    public FacturaCompraController() {
    }

    public void initABMFacturaCompra(JFrame owner, boolean modal) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.COMPRA);
        if (new UnidadDeNegocioJpaController().findAll().isEmpty()) {
            throw new MessageException(JGestion.resourceBundle.getString("info.unidaddenegociosempty"));
        }
        UsuarioHelper uh = new UsuarioHelper();
        if (uh.getSucursales().isEmpty()) {
            throw new MessageException(JGestion.resourceBundle.getString("unassigned.sucursal"));
        }
        if (uh.getCajas(true).isEmpty()) {
            throw new MessageException(JGestion.resourceBundle.getString("unassigned.caja"));
        }
        initComprobanteUI(owner, modal);
        //set next nº movimiento
        jdFactura.setTfNumMovimiento("");
        UTIL.loadComboBox(jdFactura.getCbProveedor(), JGestionUtils.getWrappedProveedores(new ProveedorJpaController().findAllLite()), false);
        ActionListenerManager.setUnidadDeNegocioSucursalActionListener(jdFactura.getCbUnidadDeNegocio(), false, jdFactura.getCbSucursal(), false, true);
        ActionListenerManager.setCuentasEgresosSubcuentaActionListener(jdFactura.getCbCuenta(), false, jdFactura.getCbSubCuenta(), true, true);
        UTIL.loadComboBox(jdFactura.getCbCaja(), uh.getCajas(true), false);
        UTIL.loadComboBox(jdFactura.getCbFacturaTipo(), TIPOS_FACTURA, false);
        UTIL.loadComboBox(jdFactura.getCbFormaPago(), Valores.FormaPago.getFormasDePago(), false);
        UTIL.loadComboBox(jdFactura.getCbProductos(), new ProductoController().findWrappedProductoToCombo(true), false);
        UTIL.loadComboBox(jdFactura.getCbDominio(), JGestionUtils.getWrappedDominios(new DominioJpaController().findAll()), true);

        jdFactura.getCbFacturaTipo().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jdFactura.getCbFacturaTipo().getSelectedItem().toString().equalsIgnoreCase("x")
                        || jdFactura.getCbFacturaTipo().getSelectedItem().toString().equalsIgnoreCase("o")) {
                    jdFactura.setFacturaNumeroEnable(false);
                } else {
                    jdFactura.setFacturaNumeroEnable(true);
                    jdFactura.setTfFacturaCuarto(null);
                    jdFactura.setTfFacturaOcteto(null);
                }
            }
        });
        jdFactura.addTotalesRefreshListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                refreshResumen();
            }
        });
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
                    ComboBoxWrapper<Producto> wrap = (ComboBoxWrapper<Producto>) jdFactura.getCbProductos().getSelectedItem();
                    buscarProducto(wrap.getEntity().getCodigo());
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
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        @SuppressWarnings("unchecked")
                        ComboBoxWrapper<Producto> wrap = (ComboBoxWrapper<Producto>) jdFactura.getCbProductos().getSelectedItem();
                        buscarProducto(wrap.getEntity().getCodigo());
                    } catch (ClassCastException ex) {
                        jdFactura.setTfProductoPrecioActual("");
                        jdFactura.setTfPrecioUnitario("");
                        jdFactura.setTfProductoIVA("");
                    }
                }
            }
        });
        jdFactura.getTfProductoCodigo().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == 10) {
                    selectedProducto = new ProductoController().findProductoByCodigo(jdFactura.getTfProductoCodigo().getText());
                    setProducto(selectedProducto);
                }
            }
        });
        jdFactura.addListener(this);
        jdFactura.setLocationRelativeTo(owner);
        jdFactura.setVisible(true);
    }

    void buscarProducto(String codigoProducto) {
        selectedProducto = new ProductoController().findProductoByCodigo(codigoProducto);
        setProducto(selectedProducto);
    }

    private void addProductoToDetalle() throws MessageException {
        if (selectedProducto == null) {
            throw new MessageException("Seleccione un producto");
        }
        int cantidad;
        BigDecimal precioUnitario;
        BigDecimal descuento = BigDecimal.ZERO;
        //se le suma uno para q coincidan con los valores en la DB de cambio precio
        int tipoValorizacionStock = jdFactura.getCbCambioPrecio().getSelectedIndex() + 1;
        if (tipoValorizacionStock == ProductoController.PPP && selectedProducto.getStockactual() < 0) {
            throw new MessageException("No se puede hacer un cálculo de PPP"
                    + " siendo el stock actual del producto menor a 0"
                    + "\nStock actual: " + selectedProducto.getStockactual());
        }
        try {
            cantidad = Integer.valueOf(jdFactura.getTfCantidad().getText());
            if (cantidad < 1) {
                throw new MessageException("La cantidad no puede ser menor a 1");
            }
        } catch (NumberFormatException ex) {
            throw new MessageException("Cantidad no válida (solo números enteros)");
        }
        try {
            precioUnitario = new BigDecimal(jdFactura.getTfPrecioUnitario()).setScale(4, RoundingMode.HALF_EVEN);
            if (precioUnitario.signum() == -1) {
                throw new MessageException("El precio unitario no puede ser menor a 0");
            }

        } catch (NumberFormatException ex) {
            throw new MessageException("Precio Unitario no válido");
        }
        //<editor-fold defaultstate="collapsed" desc="descuento">
        int tipoDescuento = jdFactura.getCbDesc().getSelectedIndex();
        try {
            if (!jdFactura.getTfProductoDesc().getText().trim().isEmpty()) {
                descuento = new BigDecimal(jdFactura.getTfProductoDesc().getText().trim());
                if (descuento.signum() == -1) {
                    throw new MessageException("Descuento no puede ser menor a 0");
                }
                //cuando el descuento es por porcentaje (%)
                if ((tipoDescuento == 0) && (descuento.doubleValue() > 100)) {
                    throw new MessageException("El descuento no puede ser superior al 100%, ¿no te parece?");
                } else if ((tipoDescuento == 1) && (descuento.compareTo(precioUnitario) == -1)) {
                    // cuando es por un monto fijo ($)
                    throw new MessageException("El descuento (" + descuento + ") no puede ser superior al precio unitario (" + precioUnitario + ")");
                }
            }
        } catch (NumberFormatException ex) {
            throw new MessageException("Descuento no válido (solo números y utilice el PUNTO como separador decimal)");
        }
        if (descuento.signum() == 1) {
            if (tipoDescuento == 0) {
                precioUnitario = precioUnitario.subtract(precioUnitario.multiply((descuento.divide(new BigDecimal("100")))));
            } else {
                precioUnitario = precioUnitario.subtract(descuento);
            }

        }
        //</editor-fold>
        // agregando a la tabla el producto
        precioUnitario = precioUnitario.setScale(4, RoundingMode.HALF_EVEN);
        jdFactura.getDtm().addRow(new Object[]{
            selectedProducto.getIva().getIva(),
            selectedProducto.getCodigo(),
            selectedProducto.getNombre() + " " + selectedProducto.getMarca().getNombre(),
            cantidad,
            precioUnitario,
            precioUnitario.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_EVEN),
            tipoValorizacionStock
        });

        refreshResumen();
    }

    private void deleteProductoFromDetalle() {
        int cant = UTIL.removeSelectedRows(jdFactura.getjTable1());
        if (cant > 0) {
            refreshResumen();
        }
    }

    /**
     * share to {@link NotaCreditoProveedorController}
     *
     * @param producto
     */
    void setProducto(Producto producto) {
        if (producto != null) {
            jdFactura.labelCodigoNoRegistrado(false);
            jdFactura.setTfProductoCodigo(producto.getCodigo());
            UTIL.setSelectedItem(jdFactura.getCbProductos(), producto.getNombre());
            jdFactura.setTfProductoIVA(producto.getIva().getIva().toString());
            jdFactura.setTfProductoPrecioActual(UTIL.PRECIO_CON_PUNTO.format(producto.getCostoCompra()));
            jdFactura.getTfCantidad().requestFocus();
        } else {
            jdFactura.labelCodigoNoRegistrado(true);
            jdFactura.setTfProductoPrecioActual("");
            jdFactura.setTfProductoIVA("");
        }
    }

    /**
     * share to {@link NotaCreditoProveedorController}
     *
     * @param producto
     */
    void refreshResumen() {
        if (jdFactura.getTfPercIIBB().trim().isEmpty()) {
            jdFactura.setTfPercIIBB("0");
        }
        if (jdFactura.getTfPercIVA().getText().trim().isEmpty()) {
            jdFactura.getTfPercIVA().setText("0");
        }
        if (jdFactura.getTfOtrosImpuestosRecuperables().getText().trim().isEmpty()) {
            jdFactura.getTfOtrosImpuestosRecuperables().setText("0");
        }
        if (jdFactura.getTfOtrosImpuestosNoRecuperables().getText().trim().isEmpty()) {
            jdFactura.getTfOtrosImpuestosNoRecuperables().setText("0");
        }
        if (jdFactura.getTfDescuento().getText().trim().isEmpty()) {
            jdFactura.getTfDescuento().setText("0");
        }
        BigDecimal gravado21 = BigDecimal.ZERO;
        BigDecimal gravado105 = BigDecimal.ZERO;
        BigDecimal noGravado = BigDecimal.ZERO;
        BigDecimal percIIBB = new BigDecimal(jdFactura.getTfPercIIBB());
        BigDecimal percIVA = new BigDecimal(jdFactura.getTfPercIVA().getText());
        BigDecimal recuperables = new BigDecimal(jdFactura.getTfOtrosImpuestosRecuperables().getText().trim());
        BigDecimal noRecuperables = new BigDecimal(jdFactura.getTfOtrosImpuestosNoRecuperables().getText().trim());
        BigDecimal desc = new BigDecimal(jdFactura.getTfDescuento().getText().trim());
        BigDecimal gravado27 = BigDecimal.ZERO;
        DefaultTableModel dtm = jdFactura.getDtm();
        if (jdFactura.getCbFacturaTipo().getSelectedItem().toString().equalsIgnoreCase("A")
                || jdFactura.getCbFacturaTipo().getSelectedItem().toString().equalsIgnoreCase("M")) {
            for (int i = (dtm.getRowCount() - 1); i > -1; i--) {
                String iva = dtm.getValueAt(i, 0).toString();
                BigDecimal subTotalSinIVA = (BigDecimal) dtm.getValueAt(i, 5);
                if (new BigDecimal(iva).compareTo(BigDecimal.ZERO) == 0) {
                    noGravado = noGravado.add(subTotalSinIVA);
                } else if (iva.equalsIgnoreCase("10.5")) {
                    gravado105 = gravado105.add(subTotalSinIVA);
                } else if (iva.equalsIgnoreCase("21.0")) {
                    gravado21 = gravado21.add(subTotalSinIVA);
                } else if (iva.equalsIgnoreCase("27.0")) {
                    gravado27 = gravado27.add(subTotalSinIVA);
                }
            }
            if (gravado21.compareTo(desc) == 1) {
                gravado21 = gravado21.subtract(desc);
            } else if (gravado105.compareTo(desc) == 1) {
                gravado105 = gravado105.subtract(desc);
            } else {
                // y ahora??...
            }
        } else {
            for (int i = (dtm.getRowCount() - 1); i > -1; i--) {
                BigDecimal subTotalSinIVA = (BigDecimal) dtm.getValueAt(i, 5);
                noGravado = noGravado.add(subTotalSinIVA);
            }
        }
        jdFactura.setTfGravado(UTIL.PRECIO_CON_PUNTO.format(gravado21.add(gravado105).add(gravado27)));
        jdFactura.setTfTotalIVA105(UTIL.PRECIO_CON_PUNTO.format(UTIL.getPorcentaje(gravado105, new BigDecimal("10.5"))));
        jdFactura.setTfTotalIVA21(UTIL.PRECIO_CON_PUNTO.format(UTIL.getPorcentaje(gravado21, new BigDecimal("21.0"))));
        jdFactura.getTfTotalPercIIBB().setText(UTIL.PRECIO_CON_PUNTO.format(percIIBB));
        jdFactura.getTfTotalPercIVA().setText(UTIL.PRECIO_CON_PUNTO.format(percIVA));
        jdFactura.getTfTotalOtrosImpuestos().setText(UTIL.PRECIO_CON_PUNTO.format(UTIL.getPorcentaje(gravado27, new BigDecimal("27.0"))));
        jdFactura.setTfTotalNoGravado(UTIL.PRECIO_CON_PUNTO.format(noGravado));
//        jdFactura.getTfTotalImpuestosNoRecuperables().setText(UTIL.PRECIO_CON_PUNTO.format(noRecuperables));
        jdFactura.setTfTotal(UTIL.PRECIO_CON_PUNTO.format(gravado21.add(gravado105).add(gravado27).
                add(UTIL.getPorcentaje(gravado105, new BigDecimal("10.5"))).
                add(UTIL.getPorcentaje(gravado21, new BigDecimal("21.0"))).
                add(UTIL.getPorcentaje(gravado27, new BigDecimal("27.0"))).
                add(percIIBB).add(percIVA).add(recuperables).add(noRecuperables).add(noGravado)));
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (e.getComponent().getClass().equals(javax.swing.JTextField.class)) {
            javax.swing.JTextField tf = (javax.swing.JTextField) e.getComponent();
            if (tf.getName().equalsIgnoreCase("cuarteto")) {
                if (tf.getText().length() < 4) {
                    UTIL.soloNumeros(e);
                } else if (tf.getSelectedText() != null && tf.getSelectedText().length() == tf.getText().length()) {
                    UTIL.soloNumeros(e);
                } else {
                    e.setKeyChar((char) KeyEvent.VK_CLEAR);
                }
            } else if (tf.getName().equalsIgnoreCase("octeto")) {
                if (tf.getText().length() < 8) {
                    UTIL.soloNumeros(e);
                } else if (tf.getSelectedText() != null && tf.getSelectedText().length() == tf.getText().length()) {
                    UTIL.soloNumeros(e);
                } else {
                    e.setKeyChar((char) KeyEvent.VK_CLEAR);
                }
            } else if (tf.getName().equalsIgnoreCase("dias")) {
                if (tf.getText().length() >= 3) {
                    e.setKeyChar((char) KeyEvent.VK_CLEAR);
                }
            }
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
    }

    @SuppressWarnings("unchecked")
    private FacturaCompra setAndPersist() throws Exception {
        FacturaCompra newFacturaCompra = new FacturaCompra();
        newFacturaCompra.setFechaCompra(jdFactura.getDcFechaFactura());
        newFacturaCompra.setAnulada(false);
        String ob = jdFactura.getTfObservacion().getText().trim();
        newFacturaCompra.setObservacion(ob.isEmpty() ? null : ob);
        //set entities
        newFacturaCompra.setProveedor(new ProveedorJpaController().find(getSelectedProveedorFromABM().getId()));
        newFacturaCompra.setUsuario(UsuarioController.getCurrentUser());
        newFacturaCompra.setCaja((Caja) jdFactura.getCbCaja().getSelectedItem());
        try {
            newFacturaCompra.setUnidadDeNegocio(((ComboBoxWrapper<UnidadDeNegocio>) jdFactura.getCbUnidadDeNegocio().getSelectedItem()).getEntity());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(jdFactura, "Unidad de Negocio no válida");
        }
        newFacturaCompra.setSucursal(getSelectedSucursalFromJD());
        try {
            newFacturaCompra.setCuenta(((ComboBoxWrapper<Cuenta>) jdFactura.getCbCuenta().getSelectedItem()).getEntity());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(jdFactura, "Cuenta no válida");
        }
        if (jdFactura.getCbSubCuenta().getSelectedIndex() > 0) {
            try {
                newFacturaCompra.setSubCuenta(((ComboBoxWrapper<SubCuenta>) jdFactura.getCbSubCuenta().getSelectedItem()).getEntity());
            } catch (Exception e) {
                newFacturaCompra.setSubCuenta(null);
            }
        }
        if (jdFactura.getCbDominio().getSelectedIndex() > 0) {
            try {
                newFacturaCompra.setDominio(((ComboBoxWrapper<Dominio>) jdFactura.getCbDominio().getSelectedItem()).getEntity());
            } catch (Exception e) {
                newFacturaCompra.setDominio(null);
            }
        }
        newFacturaCompra.setTipo(jdFactura.getCbFacturaTipo().getSelectedItem().toString().charAt(0));
        newFacturaCompra.setImporte(Double.valueOf(jdFactura.getTfTotalText()));
        if (newFacturaCompra.getTipo() == 'X' || newFacturaCompra.getTipo() == 'O') {
            long x = jpaController.getMaxNumeroComprobante(newFacturaCompra.getSucursal(), newFacturaCompra.getTipo()) + 1;
            newFacturaCompra.setNumero(Long.valueOf(newFacturaCompra.getSucursal().getPuntoVenta().toString() + UTIL.AGREGAR_CEROS(x, 8)));
        } else {
            newFacturaCompra.setNumero(Long.valueOf(jdFactura.getTfFacturaCuarto() + jdFactura.getTfFacturaOcteto()));
        }
        if (newFacturaCompra.getTipo() == 'A' || newFacturaCompra.getTipo() == 'M') {
            newFacturaCompra.setGravado(new BigDecimal(jdFactura.getTfGravado()));
            newFacturaCompra.setNoGravado(new BigDecimal(jdFactura.getTfTotalNoGravado()));
            newFacturaCompra.setIva10(Double.valueOf(jdFactura.getTfTotalIVA105()));
            newFacturaCompra.setIva21(Double.valueOf(jdFactura.getTfTotalIVA21()));
            newFacturaCompra.setPercIva(new BigDecimal(jdFactura.getTfPercIVA().getText()));
            newFacturaCompra.setPercDgr(Double.valueOf(jdFactura.getTfPercIIBB()));
            newFacturaCompra.setOtrosIvas(new BigDecimal(jdFactura.getTfTotalOtrosImpuestos().getText().trim()));
            newFacturaCompra.setImpuestosRecuperables(new BigDecimal(jdFactura.getTfOtrosImpuestosRecuperables().getText().trim()));
            newFacturaCompra.setImpuestosNoRecuperables(new BigDecimal(jdFactura.getTfOtrosImpuestosNoRecuperables().getText().trim()));
        } else {
            newFacturaCompra.setGravado(BigDecimal.ZERO);
            newFacturaCompra.setNoGravado(BigDecimal.ZERO);
            newFacturaCompra.setIva10(BigDecimal.ZERO.doubleValue());
            newFacturaCompra.setIva21(BigDecimal.ZERO.doubleValue());
            newFacturaCompra.setPercIva(BigDecimal.ZERO);
            newFacturaCompra.setPercDgr(BigDecimal.ZERO.doubleValue());
            newFacturaCompra.setOtrosIvas(BigDecimal.ZERO);
            newFacturaCompra.setImpuestosRecuperables(BigDecimal.ZERO);
            newFacturaCompra.setImpuestosNoRecuperables(BigDecimal.ZERO);
        }
        newFacturaCompra.setDescuento(new BigDecimal(jdFactura.getTfDescuento().getText().trim()));
        newFacturaCompra.setRemito(0L);
        newFacturaCompra.setActualizaStock(jdFactura.getCheckActualizaStock().isSelected());
        newFacturaCompra.setFormaPago((short) ((Valores.FormaPago) jdFactura.getCbFormaPago().getSelectedItem()).getId());
        if (newFacturaCompra.getFormaPago() == Valores.FormaPago.CTA_CTE.getId()) {
            newFacturaCompra.setDiasCtaCte(Short.valueOf(jdFactura.getTfDias()));
        }
        newFacturaCompra.setMovimientoInterno(jpaController.count() + 1);
        newFacturaCompra.setDetalleCompraList(new ArrayList<DetalleCompra>());

        //carga detalleCompra
        DefaultTableModel dtm = jdFactura.getDtm();
        for (int i = 0; i < dtm.getRowCount(); i++) {
            DetalleCompra detalleCompra = new DetalleCompra();
            detalleCompra.setProducto(new ProductoController().findProductoByCodigo(dtm.getValueAt(i, 1).toString()));
            detalleCompra.setCantidad(Integer.valueOf(dtm.getValueAt(i, 3).toString()));
            detalleCompra.setPrecioUnitario(BigDecimal.valueOf(Double.valueOf(dtm.getValueAt(i, 4).toString())));
            newFacturaCompra.getDetalleCompraList().add(detalleCompra);
        }

        // 1- PERSIST, 2- UPDATE STOCK, 3- UPDATE CAJA, 4- update costo 
        try {
            //persistiendo
            jpaController.persist(newFacturaCompra);
            ProductoController productoCtrl = new ProductoController();
            for (DetalleCompra detalleCompra : newFacturaCompra.getDetalleCompraList()) {
                for (int row = 0; row < dtm.getRowCount(); row++) {
                    Producto p = detalleCompra.getProducto();
                    if (p.getCodigo().equals((String) dtm.getValueAt(row, 1))) {
                        productoCtrl.valorizarStock(p, detalleCompra.getPrecioUnitario(), detalleCompra.getCantidad(), Integer.parseInt(dtm.getValueAt(row, 6).toString()));
                    }
                }
            }
            newFacturaCompra = (FacturaCompra) DAO.findEntity(FacturaCompra.class, newFacturaCompra.getId());
            if (newFacturaCompra.getActualizaStock()) {
                //y también la variable Producto.stockActual
                new StockController().updateStock(newFacturaCompra);
            }
            asentarSegunFormaDePago(newFacturaCompra);
        } catch (Exception ex) {
            if (newFacturaCompra.getId() != null) {
                jpaController.remove(newFacturaCompra);
            }
            throw ex;
        }
        return newFacturaCompra;
    }

    /**
     * Asienta en Caja o no .. según la forma de pago ({@link FacturaCompra#formaPago})
     *
     * @param facturaCompra
     * @throws Exception
     */
    private void asentarSegunFormaDePago(FacturaCompra facturaCompra) throws Exception {
        int formaPago = facturaCompra.getFormaPago();
        switch (formaPago) {
            case 1: { // CONTADO
                new CajaMovimientosJpaController().asentarMovimiento(facturaCompra);
                break;
            }
            case 2: { // CTA CTE Proveedor (NO HAY NINGÚN MOVIMIENTO DE CAJA)
                new CtacteProveedorController().nuevaCtaCte(facturaCompra);
                break;
            }
            case 3: { // CHEQUE
                //UNIMPLEMENTED !!!....yet
                throw new IllegalArgumentException("forma de pago CHEQUE no está implementada");
            }
            default: {
                throw new IllegalArgumentException("FORMA PAGO NO VáliDA!");
                // acá se pudre todo....
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void checkConstraints() throws MessageException {
        try {
            getSelectedProveedorFromABM();
        } catch (ClassCastException ex) {
            throw new MessageException("Proveedor no válido");
        }
        try {
            ((ComboBoxWrapper<UnidadDeNegocio>) jdFactura.getCbUnidadDeNegocio().getSelectedItem()).getEntity();
        } catch (Exception e) {
            throw new MessageException("Unidad de Negocio no válida");
        }
        try {
            getSelectedSucursalFromJD();
        } catch (ClassCastException ex) {
            throw new MessageException("Sucursal no válido");
        }
        try {
            ((ComboBoxWrapper<Cuenta>) jdFactura.getCbCuenta().getSelectedItem()).getEntity();
        } catch (Exception e) {
            throw new MessageException("Cuenta no válida");
        }
        if (jdFactura.getDcFechaFactura() == null) {
            throw new MessageException("Fecha de factura no válida");
        }

        if (!jdFactura.getCbFacturaTipo().getSelectedItem().toString().equalsIgnoreCase("X")
                && !jdFactura.getCbFacturaTipo().getSelectedItem().toString().equalsIgnoreCase("O")) {
            if (jdFactura.getTfFacturaCuarto().length() > 4) {
                throw new MessageException("Número de factura no válido: " + jdFactura.getTfFacturaCuarto() + jdFactura.getTfFacturaOcteto());
            }
            if (jdFactura.getTfFacturaCuarto().length() > 8) {
                throw new MessageException("Número de factura no válido");
            }

            try {
                if (Long.valueOf(jdFactura.getTfFacturaCuarto() + jdFactura.getTfFacturaOcteto()) < 100000001) {
                    throw new MessageException("Número de factura no válido (no puede ser menor a 0001-00000001");
                }

            } catch (NumberFormatException e) {
                throw new MessageException("Los primeros 4 números de la factura no son válido");
            }
            try {
                if (Integer.valueOf(jdFactura.getTfFacturaOcteto()) < 1) {
                    throw new MessageException("Número de factura no válido");
                }

            } catch (NumberFormatException e) {
                throw new MessageException("Los primeros 4 números de la factura no válido");
            }
            long numeroFactura = Long.valueOf(jdFactura.getTfFacturaCuarto() + jdFactura.getTfFacturaOcteto());
            FacturaCompra old = findFacturaCompra(numeroFactura, jdFactura.getCbFacturaTipo().getSelectedItem().toString(),
                    getSelectedProveedorFromABM(), false);
            if (old != null) {
                throw new MessageException("Ya existe la factura " + JGestionUtils.getNumeracion(old)
                        + " del proveedor " + jdFactura.getCbProveedor().getSelectedItem());
            }
        }
        try {
            if (jdFactura.getCbFormaPago().getSelectedItem().toString().equalsIgnoreCase(Valores.FormaPago.CTA_CTE.getNombre())) {
                if (Short.valueOf(jdFactura.getTfDias()) < 1) {
                    throw new MessageException("Días no válidos para Forma de pago: " + FORMAS_PAGO.get(1));
                }
            }

        } catch (NumberFormatException e) {
            throw new MessageException("Días no válidos para Forma de pago: " + FORMAS_PAGO.get(1));
        }

        if (jdFactura.getTfObservacion().getText().trim().length() > 100) {
            throw new MessageException("Observación no válida, no debe superar los 100 caracteres (no es una novela)");
        }
        try {
            Double.valueOf(jdFactura.getTfPercIIBB());
        } catch (NumberFormatException e) {
            throw new MessageException("Monto de Percepción de Ingresos Brutos no válido");
        }
        try {
            Double.valueOf(jdFactura.getTfOtrosImpuestosRecuperables().getText().trim());
        } catch (NumberFormatException e) {
            throw new MessageException("Monto de Impuestos Recuperables no válido (Solo números y utilizar el PUNTO como separador decimal");
        }
        try {
            Double.valueOf(jdFactura.getTfOtrosImpuestosNoRecuperables().getText().trim());
        } catch (NumberFormatException e) {
            throw new MessageException("Monto de Impuestos No Recuperables no válido (Solo números y utilizar el PUNTO como separador decimal");
        }
        try {
            Double.valueOf(jdFactura.getTfDescuento().getText().trim());
        } catch (NumberFormatException e) {
            throw new MessageException("Monto de Descuento no válido (Solo números y utilizar el PUNTO como separador decimal");
        }

        //si hay productos cargados en la lista de compra!!
        if (jdFactura.getDtm().getRowCount() < 1) {
            throw new MessageException("No hay productos cargados");
        }

        BigDecimal total = new BigDecimal(jdFactura.getTfTotalText());
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            throw new MessageException("El total no puede ser negativo");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // <editor-fold defaultstate="collapsed" desc="JButton">
        if (e.getSource().getClass().equals(JButton.class)) {
            JButton boton = (JButton) e.getSource();
            //<editor-fold defaultstate="collapsed" desc="JDFacturaCompra actions">
            if (jdFactura != null && jdFactura.isActive()) {
                if (boton.equals(jdFactura.getBtnAceptar())) {
                    try {
                        checkConstraints();
                        FacturaCompra f = setAndPersist();
                        String extra = "";
                        if (f.getTipo() == 'X') {
                            extra = JGestionUtils.getNumeracion(f);
                        }
                        JOptionPane.showMessageDialog(jdFactura, "Factura " + extra + " cargada", CLASS_NAME, 1);
                        jdFactura.limpiarPanel();
                    } catch (MessageException ex) {
                        JOptionPane.showMessageDialog(jdFactura, ex.getMessage(), CLASS_NAME, 2);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(jdFactura, ex.getMessage(), CLASS_NAME, 0);
                        LOG.error(ex, ex);
                    }
                } else if (boton.equals(jdFactura.getBtnADD())) {
                    try {
                        addProductoToDetalle();
                    } catch (MessageException ex) {
                        JOptionPane.showMessageDialog(jdFactura, ex.getMessage(), CLASS_NAME, 2);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(jdFactura, ex.getMessage(), CLASS_NAME, 0);
                        LOG.error(ex, ex);
                    }
                } else if (boton.equals(jdFactura.getBtnDEL())) {
                    deleteProductoFromDetalle();

                } else if (boton.equals(jdFactura.getBtnCancelar())) {
                    jdFactura = null;
                    EL_OBJECT = null;
                } else if (boton.equals(jdFactura.getbBuscarProducto())) {
                    try {
                        initBuscadorProducto();
                    } catch (DatabaseErrorException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage());
                    }
                }
            }
            //</editor-fold>
            return;
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="JTextField">
        if (e.getSource().equals(javax.swing.JTextField.class)) {
            javax.swing.JTextField tf = (javax.swing.JTextField) e.getSource();
        }// </editor-fold>

    }

    public FacturaCompra findFacturaCompra(long numero, String tipoFactura, Proveedor p, boolean anulada) {
        try {
            return (FacturaCompra) DAO.getEntityManager().createNativeQuery(
                    "SELECT o.* from factura_compra o where o.proveedor =" + p.getId()
                    + " AND o.numero = " + numero
                    + " AND o." + FacturaCompra_.tipo.getName() + " = '" + tipoFactura + "'"
                    + " AND o.anulada = " + anulada, FacturaCompra.class).getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    public void initBuscador(Window frame, final boolean modal, final boolean toAnular) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.COMPRA);
        if (toAnular) {
            UsuarioController.checkPermiso(PermisosController.PermisoDe.ANULAR_COMPROBANTES);
        }
        buscador = new JDBuscadorReRe(frame, "Buscador - Factura compra", modal, "Proveedor", "Nº Factura");
        buscador.hideFactura();
        buscador.hideVendedor();
        buscador.getbImprimir().setVisible(true);
        ActionListenerManager.setUnidadDeNegocioSucursalActionListener(buscador.getCbUnidadDeNegocio(), true, buscador.getCbSucursal(), true, true);
        ActionListenerManager.setCuentasEgresosSubcuentaActionListener(buscador.getCbCuenta(), true, buscador.getCbSubCuenta(), true, true);
        UTIL.loadComboBox(buscador.getCbClieProv(),  JGestionUtils.getWrappedProveedores(new ProveedorJpaController().findAllLite()), true);
        UTIL.loadComboBox(buscador.getCbCaja(), new UsuarioHelper().getCajas(true), true);
//        UTIL.loadComboBox(buscador.getCbSucursal(), new UsuarioHelper().getWrappedSucursales(), true);
        UTIL.loadComboBox(buscador.getCbFormasDePago(), Valores.FormaPago.getFormasDePago(), true);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"facturaID", "Nº factura", "Mov.", "Proveedor", "Importe", "Fecha", "Sucursal", "Caja", "Usuario", "Fecha (Sistema)"},
                new int[]{1, 80, 10, 50, 50, 50, 80, 80, 40, 70},
                new Class<?>[]{null, null, Integer.class, null, null, String.class, null, null, null, null});
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
        buscador.getbImprimir().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (buscador.getjTable1().getRowCount() < 1) {
                        throw new MessageException("El buscador no contiene filas para exportar");
                    }
                    doReportFacturas();
                } catch (JRException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                } catch (MissingReportException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
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
                        EL_OBJECT = jpaController.find(Integer.valueOf(buscador.getDtm().getValueAt(buscador.getjTable1().getSelectedRow(), 0).toString()));
                        try {
//                            if (jdFactura == null) {
                            initComprobanteUI(buscador, true);
//                            }
                            setData(EL_OBJECT, toAnular);
                            SwingUtil.setComponentsEnabled(jdFactura.getPanelDatosCompra().getComponents(), false, true, (Class<? extends Component>[]) null);
                            SwingUtil.setComponentsEnabled(jdFactura.getPanelOpcionesCompra().getComponents(), false, true, (Class<? extends Component>[]) null);
                            SwingUtil.setComponentsEnabled(jdFactura.getPanelProducto().getComponents(), false, true, (Class<? extends Component>[]) null);
                            jdFactura.setVisible(true);
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
        buscador.setListeners(this);
        buscador.setLocationRelativeTo(frame);
        buscador.setVisible(true);
    }

    void initComprobanteUI(Window owner, boolean modal) {
        jdFactura = new JDFacturaCompra(owner, modal);
        UTIL.getDefaultTableModel(jdFactura.getjTable1(), colsName, colsWidth);
        jdFactura.getjTable1().getColumnModel().getColumn(3).setCellRenderer(NumberRenderer.getIntegerRenderer());
        jdFactura.getjTable1().getColumnModel().getColumn(4).setCellRenderer(NumberRenderer.getCurrencyRenderer(4));
        jdFactura.getjTable1().getColumnModel().getColumn(5).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        UTIL.hideColumnsTable(jdFactura.getjTable1(), new int[]{0, 6, 7});
        jdFactura.setLocationRelativeTo(owner);
    }

    private void cargarTablaBuscador(String nativeQuery) {
        buscador.dtmRemoveAll();
        DefaultTableModel dtm = buscador.getDtm();
        @SuppressWarnings("unchecked")
        List<FacturaCompra> l = DAO.getEntityManager().createNativeQuery(nativeQuery, FacturaCompra.class).getResultList();
        for (FacturaCompra facturaCompra : l) {
            dtm.addRow(new Object[]{
                facturaCompra.getId(),
                JGestionUtils.getNumeracion(facturaCompra),
                facturaCompra.getMovimientoInterno(),
                facturaCompra.getProveedor().getNombre(),
                BigDecimal.valueOf(facturaCompra.getImporte()),
                UTIL.DATE_FORMAT.format(facturaCompra.getFechaCompra()),
                facturaCompra.getSucursal().getNombre(),
                facturaCompra.getCaja(),
                facturaCompra.getUsuario(),
                UTIL.DATE_FORMAT.format(facturaCompra.getFechaalta()) + " (" + UTIL.TIME_FORMAT.format(facturaCompra.getFechaalta()) + ")"
            });
        }
    }

    @SuppressWarnings("unchecked")
    private String armarQuery() throws MessageException {
        StringBuilder query = new StringBuilder("SELECT o.* FROM factura_compra o"
                + " WHERE o.anulada = " + buscador.isCheckAnuladaSelected());

        long numero;
        //filtro por nº de factura
        if (buscador.getTfCuarto().length() > 0 && buscador.getTfOcteto().length() > 0) {
            try {
                numero = Long.parseLong(buscador.getTfCuarto() + buscador.getTfOcteto());
                query.append(" AND o.numero = ").append(numero);
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de " + CLASS_NAME + " no válido");
            }
        }
        SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyy/MM/dd");
        if (buscador.getDcDesde() != null) {
            query.append(" AND o.fecha_compra >= '").append(yyyyMMdd.format(buscador.getDcDesde())).append("'");
        }
        if (buscador.getDcHasta() != null) {
            query.append(" AND o.fecha_compra <= '").append(yyyyMMdd.format(buscador.getDcHasta())).append("'");
        }
        if (buscador.getDcDesdeSistema() != null) {
            query.append(" AND o.fechaalta >= '").append(yyyyMMdd.format(UTIL.clearTimeFields(buscador.getDcDesdeSistema()))).append("'");
        }
        if (buscador.getDcHastaSistema() != null) {
            query.append(" AND o.fechaalta <= '").append(yyyyMMdd.format(UTIL.clearTimeFields(buscador.getDcHastaSistema()))).append("'");
        }
        UsuarioHelper usuarioHelper = new UsuarioHelper();
        if (buscador.getCbCaja().getSelectedIndex() > 0) {
            query.append(" AND o.caja = ").append(((Caja) buscador.getCbCaja().getSelectedItem()).getId());
        } else {
            query.append(" AND (");
            Iterator<Caja> iterator = usuarioHelper.getCajas(Boolean.TRUE).iterator();
            while (iterator.hasNext()) {
                Caja caja = iterator.next();
                query.append("o.caja=").append(caja.getId());
                if (iterator.hasNext()) {
                    query.append(" OR ");
                }
            }
            query.append(")");
        }
        if (buscador.getCbUnidadDeNegocio().getSelectedIndex() > 0) {
            query.append(" AND o.unidad_de_negocio_id = ").append(((ComboBoxWrapper<UnidadDeNegocio>) buscador.getCbUnidadDeNegocio().getSelectedItem()).getId());
        }
        if (buscador.getCbCuenta().getSelectedIndex() > 0) {
            query.append(" AND o.cuenta_id = ").append(((ComboBoxWrapper<Cuenta>) buscador.getCbCuenta().getSelectedItem()).getId());
        }
        if (buscador.getCbSubCuenta().getSelectedIndex() > 0) {
            query.append(" AND o.subcuenta_id = ").append(((ComboBoxWrapper<SubCuenta>) buscador.getCbSubCuenta().getSelectedItem()).getId());
        }
        if (buscador.getCbSucursal().getSelectedIndex() > 0) {
            query.append(" AND o.sucursal = ").append(((ComboBoxWrapper<Sucursal>) buscador.getCbSucursal().getSelectedItem()).getId());
        } else {
            if (buscador.getCbUnidadDeNegocio().getSelectedIndex() > 0) {
                query.append(" AND (");
                for (int i = 1; i < buscador.getCbSucursal().getItemCount(); i++) {
                    ComboBoxWrapper<Sucursal> cbw = (ComboBoxWrapper<Sucursal>) buscador.getCbSucursal().getItemAt(i);
                    query.append(" o.sucursal=").append(cbw.getId());
                    if ((i + 1) < buscador.getCbSucursal().getItemCount()) {
                        query.append(" OR ");
                    }
                }
                query.append(")");
            } else {
                List<Sucursal> sucursales = new UsuarioHelper().getSucursales();
                query.append(" AND (");
                for (int i = 0; i < sucursales.size(); i++) {
                    query.append(" o.sucursal=").append(sucursales.get(i).getId());
                    if ((i + 1) < sucursales.size()) {
                        query.append(" OR ");
                    }
                }
                query.append(")");
            }
        }

        if (buscador.getCbClieProv().getSelectedIndex() > 0) {
            query.append(" AND o.proveedor = ").append(((ComboBoxWrapper<Proveedor>) buscador.getCbClieProv().getSelectedItem()).getId());
        }

        if (buscador.getCbFormasDePago().getSelectedIndex() > 0) {
            query.append(" AND o.forma_pago = ").append(((Valores.FormaPago) buscador.getCbFormasDePago().getSelectedItem()).getId());
        }

        if (buscador.getTfFactu4().trim().length() > 0) {
            try {
                query.append(" AND o.movimiento = ").append(Integer.valueOf(buscador.getTfFactu4()));
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de movimiento no válido");
            }
        }
        query.append(" ORDER BY o.id");
        return query.toString();
    }

    /**
     * before call this method, must call {@link #initComprobanteUI(java.awt.Window, boolean)}
     *
     * @param fc
     * @param paraAnular
     * @throws MessageException
     */
    @SuppressWarnings("unchecked")
    void setData(FacturaCompra fc, boolean paraAnular) throws MessageException {
        EL_OBJECT = fc;
        LOG.trace(EL_OBJECT.toString());
        if (paraAnular) {
            jdFactura.getBtnAnular().setEnabled(paraAnular);
            jdFactura.getBtnAnular().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        String msg_extra_para_ctacte = EL_OBJECT.getFormaPago() == Valores.FormaPago.CTA_CTE.getId() ? "\n- Remesas de pago de Cta.Cte." : "";
                        if (JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(jdFactura,
                                "- " + JGestionUtils.getNumeracion(EL_OBJECT)
                                + "\n- Movimiento de Caja"
                                + "\n- Movimiento de Stock" + msg_extra_para_ctacte, "Confirmación de anulación", JOptionPane.YES_OPTION, 2, null, null, null)) {
                            anular(EL_OBJECT);
                            EL_OBJECT = null;
                            jdFactura.showMessage("Anulada", CLASS_NAME, 2);
                            jdFactura.dispose();
                            cargarTablaBuscador(armarQuery());
                        }
                    } catch (MessageException ex) {
                        jdFactura.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    }
                }
            });
        }
        // seteando datos de FacturaCompra
//        jdFactura.getCbProveedor().removeAllItems();
        jdFactura.getCbProveedor().addItem(new ComboBoxWrapper<>(EL_OBJECT.getProveedor(), EL_OBJECT.getProveedor().getId(), EL_OBJECT.getProveedor().getNombre()));
        UnidadDeNegocio udn = EL_OBJECT.getUnidadDeNegocio();
        Sucursal s = EL_OBJECT.getSucursal();
        Cuenta cuenta = EL_OBJECT.getCuenta();
        SubCuenta subCuenta = EL_OBJECT.getSubCuenta();
        jdFactura.getCbSucursal().addItem(new ComboBoxWrapper<>(s, s.getId(), s.getNombre()));
        try {
            jdFactura.getCbUnidadDeNegocio().addItem(new ComboBoxWrapper<>(udn, udn.getId(), udn.getNombre()));
        } catch (Exception e) {
            jdFactura.getCbUnidadDeNegocio().addItem("<Unidad de Negocios no especificada>");
        }
        try {
            jdFactura.getCbCuenta().addItem(new ComboBoxWrapper<>(cuenta, cuenta.getId(), cuenta.getNombre()));
        } catch (Exception e) {
            jdFactura.getCbCuenta().addItem("<Cuenta no especificada>");
        }
        try {
            if (EL_OBJECT.getSubCuenta() != null) {
                jdFactura.getCbSubCuenta().addItem(new ComboBoxWrapper<>(subCuenta, subCuenta.getId(), subCuenta.getNombre()));
            }
        } catch (Exception e) {
            jdFactura.getCbCuenta().addItem("<SubCuenta no especificada>");
        }
        if (EL_OBJECT.getDominio() != null) {
            jdFactura.getCbDominio().addItem(new ComboBoxWrapper<>(EL_OBJECT.getDominio(), EL_OBJECT.getDominio().getId(), EL_OBJECT.getDominio().getNombre()));
        }

        jdFactura.setDcFechaFactura(EL_OBJECT.getFechaCompra());
        jdFactura.getCbFacturaTipo().removeAllItems();
        jdFactura.getCbFacturaTipo().addItem(EL_OBJECT.getTipo());
        String numFactura = UTIL.AGREGAR_CEROS(EL_OBJECT.getNumero(), 12);
        jdFactura.setTfFacturaCuarto(numFactura.substring(0, 4));
        jdFactura.setTfFacturaOcteto(numFactura.substring(4));
        jdFactura.setTfNumMovimiento(String.valueOf(EL_OBJECT.getMovimientoInterno()));
        jdFactura.getCbCaja().addItem(EL_OBJECT.getCaja());
        UTIL.loadComboBox(jdFactura.getCbFormaPago(), Valores.FormaPago.getFormasDePago(), false);
        jdFactura.setTfPercIIBB(UTIL.PRECIO_CON_PUNTO.format(EL_OBJECT.getPercDgr()));
        jdFactura.getTfPercIVA().setText(UTIL.PRECIO_CON_PUNTO.format(EL_OBJECT.getPercIva()));
        jdFactura.getTfOtrosImpuestosRecuperables().setText(UTIL.PRECIO_CON_PUNTO.format(EL_OBJECT.getImpuestosRecuperables()));
        jdFactura.getTfOtrosImpuestosNoRecuperables().setText(UTIL.PRECIO_CON_PUNTO.format(EL_OBJECT.getImpuestosNoRecuperables()));
        jdFactura.getTfDescuento().setText(UTIL.PRECIO_CON_PUNTO.format(EL_OBJECT.getDescuento()));
        jdFactura.getCbFormaPago().setSelectedIndex(EL_OBJECT.getFormaPago() - 1);
        jdFactura.setTfDias(EL_OBJECT.getDiasCtaCte() != null ? EL_OBJECT.getDiasCtaCte().toString() : null);
        jdFactura.getTfObservacion().setText(EL_OBJECT.getObservacion());
        setDetalleData(EL_OBJECT);
        refreshResumen();
        jpaController.closeEntityManager();
        jdFactura.addListener(this);
    }

    public void anular(FacturaCompra factura) throws MessageException {
        if (factura.getAnulada()) {
            throw new MessageException("Ya está anulada esta factura");
        }

        Caja oldCaja = factura.getCaja();
        List<Caja> cajasPermitidasList = new CajaController().findCajasPermitidasByUsuario(UsuarioController.getCurrentUser(), true);
        if (cajasPermitidasList.isEmpty()) {
            throw new MessageException("No tiene acceso a ninguna Caja del sistema");
        }
        if (!cajasPermitidasList.contains(oldCaja)) {
            throw new MessageException("No tiene permiso para modificar la Caja " + oldCaja + ", con la que fue realizada la venta.");
        }
        CajaMovimientosJpaController cmController = new CajaMovimientosJpaController();
        CajaMovimientos cajaMovimientoActiva;
        try {
            cajaMovimientoActiva = cmController.findCajaMovimientoAbierta(oldCaja);
        } catch (NoResultException e) {
            throw new MessageException("ERROR DE SISTEMA\nHay un problema con la Caja " + oldCaja.getNombre());
        }
        if (cajaMovimientoActiva.getCaja().isBaja() || (!cajaMovimientoActiva.getCaja().getEstado())) {
            cajaMovimientoActiva = new FacturaVentaController().initReAsignacionCajaMovimiento(cajasPermitidasList);
            if (cajaMovimientoActiva == null) {
                throw new MessageException("Operación cancelada, no ha seleccionado una caja.");
            }
        }
        try {
            cmController.anular(factura, cajaMovimientoActiva);
        } catch (MessageException ex) {
            throw ex;
        } catch (Exception ex) {
            LOG.error("Anulando " + factura.getClass() + ", id=" + factura.getId(), ex);
        }
    }

    private void initBuscadorProducto() throws DatabaseErrorException {
        ProductoController p = new ProductoController();
        p.initContenedor(null, true, true);
        UTIL.loadComboBox(jdFactura.getCbProductos(), new ProductoController().findWrappedProductoToCombo(true), false);
    }

    JDFacturaCompra getContenedor() {
        return jdFactura;
    }

    private Sucursal getSelectedSucursalFromJD() {
        @SuppressWarnings("unchecked")
        ComboBoxWrapper<Sucursal> cbw = (ComboBoxWrapper<Sucursal>) jdFactura.getCbSucursal().getSelectedItem();
        return cbw.getEntity();
    }

    private void doReportFacturas() throws MissingReportException, JRException {
        List<GenericBeanCollection> data = new ArrayList<>(buscador.getjTable1().getRowCount());
        DefaultTableModel dtm = buscador.getDtm();
        for (int row = 0; row < dtm.getRowCount(); row++) {

            data.add(new GenericBeanCollection(
                    dtm.getValueAt(row, 1),
                    dtm.getValueAt(row, 2),
                    dtm.getValueAt(row, 3),
                    dtm.getValueAt(row, 4),
                    dtm.getValueAt(row, 5),
                    dtm.getValueAt(row, 6),
                    dtm.getValueAt(row, 7),
                    dtm.getValueAt(row, 8),
                    dtm.getValueAt(row, 9),
                    null, null, null));
        }
        Reportes r = new Reportes("JGestion_ListadoFacturasCompra.jasper", "Listado Facturas Compra");
        r.setDataSource(data);
        r.addParameter("TITLE_PAGE_HEADER", "Compras");
        r.addConnection();
        r.viewReport();
    }

    void setContenedor(JDFacturaCompra jDFacturaCompra) {
        jdFactura = jDFacturaCompra;
    }

    public void asignador() {
        buscador = new JDBuscadorReRe(null, "Asignación de Unid. de Negocio/Cuenta/SubCuenta - Facturas Venta", false, "Proveedor", "Nº Factura");
        buscador.setToFacturaVenta();
        UTIL.loadComboBox(buscador.getCbClieProv(),  JGestionUtils.getWrappedProveedores(new ProveedorJpaController().findAll()), true);
        UTIL.loadComboBox(buscador.getCbCaja(), new CajaController().findCajasPermitidasByUsuario(UsuarioController.getCurrentUser(), true), true);
        UTIL.loadComboBox(buscador.getCbSucursal(), new UsuarioHelper().getWrappedSucursales(), true);
        UTIL.loadComboBox(buscador.getCbFormasDePago(), Valores.FormaPago.getFormasDePago(), true);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"facturaID", "Nº factura", "Mov.", "Proveedor", "Importe", "Fecha", "Caja", "Sucursal", "Unid. Neg.", "Cuenta", "Sub Cuenta"},
                new int[]{1, 90, 10, 50, 40, 50, 50, 80, 50, 70, 70},
                new Class<?>[]{Integer.class, null, Integer.class, null, null, String.class, null, null, null, null, null},
                new int[]{8, 9, 10});
        JComboBox unidades = new JComboBox();
        JComboBox cuentas = new JComboBox();
        JComboBox subCuentas = new JComboBox();
        UTIL.loadComboBox(unidades, new Wrapper<UnidadDeNegocio>().getWrapped(new UnidadDeNegocioJpaController().findAll()), false);
        UTIL.loadComboBox(cuentas, new Wrapper<Cuenta>().getWrapped(new CuentaController().findAll()), false);
        UTIL.loadComboBox(subCuentas, JGestionUtils.getWrappedSubCuentas(new SubCuentaJpaController().findAll()), false);
//        ActionListenerManager.setCuentaSubcuentaActionListener(cuentas, false, subCuentas, false, true);
        buscador.getjTable1().getColumnModel().getColumn(4).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        buscador.getjTable1().getColumnModel().getColumn(5).setCellRenderer(FormatRenderer.getDateRenderer());
        buscador.getjTable1().getColumnModel().getColumn(8).setCellEditor(new DefaultCellEditor(unidades));
        buscador.getjTable1().getColumnModel().getColumn(9).setCellEditor(new DefaultCellEditor(cuentas));
        buscador.getjTable1().getColumnModel().getColumn(10).setCellEditor(new DefaultCellEditor(subCuentas));
        UTIL.hideColumnTable(buscador.getjTable1(), 0);
        buscador.getbBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    cargarTablaBuscadorAsignacion(armarQuery());
                } catch (MessageException ex) {
                    buscador.showMessage(ex.getMessage(), "Buscador - " + jpaController.getEntityClass().getSimpleName(), 0);
                }
            }
        });
        buscador.getjTable1().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    if (buscador.getjTable1().getSelectedRow() > -1) {
                        EL_OBJECT = jpaController.find(Integer.valueOf(buscador.getDtm().getValueAt(buscador.getjTable1().getSelectedRow(), 0).toString()));
                        try {
                            if (jdFactura == null) {
                                initComprobanteUI(buscador, true);
                            }
                            jdFactura.setLocationRelativeTo(buscador);
                            setData(EL_OBJECT, false);
                            jdFactura.modoVista(false);
                            jdFactura.setVisible(true);
                        } catch (MessageException ex) {
                            ex.displayMessage(buscador);
                        }
                    }
                }
            }
        });
        buscador.getjTable1().getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    int column = e.getColumn();
                    TableModel model = (TableModel) e.getSource();
                    Object data = model.getValueAt(row, column);
                    System.out.println(row + "/" + column + " =" + data);
                    FacturaCompra selected = jpaController.find((Integer) model.getValueAt(row, 0));
                    if (data != null) {
                        if (column == 8) {
                            @SuppressWarnings("unchecked")
                            UnidadDeNegocio unidad = ((ComboBoxWrapper<UnidadDeNegocio>) data).getEntity();
                            selected.setUnidadDeNegocio(unidad);
                        } else if (column == 9) {
                            @SuppressWarnings("unchecked")
                            Cuenta cuenta = ((ComboBoxWrapper<Cuenta>) data).getEntity();
                            selected.setCuenta(cuenta);

                            //carga las subCuentas
                            DefaultCellEditor dce = (DefaultCellEditor) buscador.getjTable1().getColumnModel()
                                    //columnIndex is one lesser than its original position because one column was removed from table
                                    .getColumn(9).getCellEditor();
                            JComboBox cbSubCuentas = (JComboBox) dce.getComponent();
                            if (cuenta.getSubCuentas().isEmpty()) {
                                cbSubCuentas.removeAllItems();
                            } else {
                                UTIL.loadComboBox(cbSubCuentas, JGestionUtils.getWrappedSubCuentas(cuenta.getSubCuentas()), false);
                            }
                        } else if (column == 10) {
                            @SuppressWarnings("unchecked")
                            SubCuenta subCuenta = ((ComboBoxWrapper<SubCuenta>) data).getEntity();
                            selected.setSubCuenta(subCuenta);
                        }
                        jpaController.merge(selected);
                    }
                }
            }
        });
        buscador.getbExtra().setVisible(false);
        buscador.setLocationRelativeTo(null);
        buscador.setVisible(true);
    }

    private void cargarTablaBuscadorAsignacion(String query) {
        DefaultTableModel dtm = (DefaultTableModel) buscador.getjTable1().getModel();
        dtm.setRowCount(0);
        List<FacturaCompra> l = jpaController.findByNativeQuery(query);
        for (FacturaCompra factura : l) {
            dtm.addRow(new Object[]{
                factura.getId(), // <--- no es visible
                JGestionUtils.getNumeracion(factura),
                factura.getMovimientoInterno(),
                factura.getProveedor().getNombre(),
                BigDecimal.valueOf(factura.getImporte()),
                factura.getFechaCompra(),
                factura.getSucursal().getNombre(),
                factura.getCaja().getNombre(),
                (factura.getUnidadDeNegocio() != null ? new ComboBoxWrapper<>(factura.getUnidadDeNegocio(), factura.getUnidadDeNegocio().getId(), factura.getUnidadDeNegocio().getNombre()) : null),
                (factura.getCuenta() != null ? new ComboBoxWrapper<>(factura.getCuenta(), factura.getCuenta().getId(), factura.getCuenta().getNombre()) : null),
                (factura.getSubCuenta() != null ? new ComboBoxWrapper<>(factura.getSubCuenta(), factura.getSubCuenta().getId(), factura.getSubCuenta().getNombre()) : null)
            });
        }

    }

    private void setDetalleData(FacturaCompra factura) throws MessageException {
        DefaultTableModel dtm = jdFactura.getDtm();
        dtm.setRowCount(0);
        IvaController ivaController = new IvaController();
        ProductoJpaController productoJpaController = new ProductoJpaController();
        Producto producto;
        for (DetalleCompra detalle : factura.getDetalleCompraList()) {
            producto = productoJpaController.find(detalle.getProducto().getId());
            Iva iva = producto.getIva();
            while (iva == null || iva.getIva() == null) {
                System.out.print(".");
                iva = ivaController.findByProducto(producto.getId());
            }
            try {
                //"IVA", "Cód. Producto", "Producto", "Cantidad", "Precio U.", "Sub total", "Mod
                dtm.addRow(new Object[]{
                    iva.getIva(),
                    producto.getCodigo(),
                    producto.getNombre() + " " + producto.getMarca().getNombre(),
                    detalle.getCantidad(),
                    detalle.getPrecioUnitario(),
                    detalle.getPrecioUnitario().multiply(BigDecimal.valueOf(detalle.getCantidad())),
                    null});
            } catch (NullPointerException e) {
                jpaController.closeEntityManager();
                throw new MessageException("Ocurrió un error recuperando el detalle y los datos del Producto:"
                        + "\nCódigo:" + detalle.getProducto().getNombre()
                        + "\nMarca:" + detalle.getProducto().getMarca()
                        + "\nNombre:" + detalle.getProducto().getCodigo()
                        + "\nIVA:" + detalle.getProducto().getIva()
                        + "\n\n   Intente nuevamente.");
            }
        }
    }

    private Proveedor getSelectedProveedorFromABM() {
        return ((ComboBoxWrapper<Proveedor>) jdFactura.getCbProveedor().getSelectedItem()).getEntity();
    }
}
