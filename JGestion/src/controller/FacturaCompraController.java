package controller;

import controller.exceptions.*;
import entity.Caja;
import entity.CajaMovimientos;
import entity.DetalleCompra;
import entity.FacturaCompra;
import entity.Proveedor;
import entity.Sucursal;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import entity.Producto;
import generics.AutoCompleteComboBox;
import utilities.general.UTIL;
import gui.JDBuscadorReRe;
import gui.JDFacturaCompra;
import java.awt.Color;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.persistence.NoResultException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import jgestion.JGestionUtils;
import jgestion.Main;
import jpa.controller.CajaMovimientosJpaController;
import utilities.swing.components.ComboBoxWrapper;

/**
 *
 * @author FiruzzZ
 */
public class FacturaCompraController implements ActionListener, KeyListener {

    public static final List<String> TIPOS_FACTURA;
    public static final List<String> FORMAS_PAGO;
    public static final String CLASS_NAME = FacturaCompra.class.getSimpleName();
    private final String[] colsName = {"IVA", "Cód. Producto", "Producto", "Cantidad", "Precio U.", "Sub total", "Mod", "Producto.instance"};
    private final int[] colsWidth = {10, 70, 180, 10, 30, 30, 1, 1};
    private JDFacturaCompra jdFactura;
    private Producto producto_selected;
    private FacturaCompra EL_OBJECT;
    private JDBuscadorReRe buscador;

    static {
        String[] tipos = {"A", "B", "C", "M"};
        TIPOS_FACTURA = new ArrayList<String>();
        TIPOS_FACTURA.addAll(Arrays.asList(tipos));
        String[] formas = {"Contado", "Cta. Cte."};
        FORMAS_PAGO = new ArrayList<String>();
        FORMAS_PAGO.addAll(Arrays.asList(formas));
    }

    // <editor-fold defaultstate="collapsed" desc="CRUD, List's">
    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }

    public void create(FacturaCompra facturaCompra) throws Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<DetalleCompra> detallesCompraListToPersist = facturaCompra.getDetalleCompraList();
            facturaCompra.setDetalleCompraList(new ArrayList<DetalleCompra>());
            em.persist(facturaCompra);
            em.getTransaction().commit();
            DetalleCompraJpaController dcController = new DetalleCompraJpaController();
            for (DetalleCompra detallesCompra : detallesCompraListToPersist) {
                detallesCompra.setFactura(facturaCompra);
                dcController.create(detallesCompra);
            }
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            if (em != null) {
                if (em.isOpen()) {
                    em.close();
                }
            }
        }
    }

    public void edit(FacturaCompra facturaCompra) throws IllegalOrphanException, NonexistentEntityException, Exception {
        //no se edita
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        // ni se borra
    }

    public List<FacturaCompra> findFacturaCompraEntities() {
        return findFacturaCompraEntities(true, -1, -1);
    }

    public List<FacturaCompra> findFacturaCompraEntities(int maxResults, int firstResult) {
        return findFacturaCompraEntities(false, maxResults, firstResult);
    }

    private List<FacturaCompra> findFacturaCompraEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from FacturaCompra as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public FacturaCompra findFacturaCompra(Integer id) {
        EntityManager em = getEntityManager();
        try {
            FacturaCompra o = em.find(FacturaCompra.class, id);
            em.refresh(o);
            return o;
        } finally {
            em.close();
        }
    }

    public Integer getFacturaCompraCount() {
        EntityManager em = getEntityManager();
        try {
            return ((Long) em.createQuery("select count(o) from FacturaCompra as o").getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }// </editor-fold>

    public void initABMFacturaCompra(JFrame owner, boolean modal) throws MessageException {
        UsuarioJpaController.checkPermiso(PermisosJpaController.PermisoDe.COMPRA);
        UsuarioHelper uh = new UsuarioHelper();
        if (uh.getSucursales().isEmpty()) {
            throw new MessageException(Main.resourceBundle.getString("unassigned.sucursal"));
        }
        if (uh.getCajas(true).isEmpty()) {
            throw new MessageException(Main.resourceBundle.getString("unassigned.caja"));
        }
        jdFactura = new JDFacturaCompra(owner, modal, 1);
        UTIL.getDefaultTableModel(jdFactura.getjTable1(), colsName, colsWidth);
        UTIL.hideColumnsTable(jdFactura.getjTable1(), new int[]{0, 6, 7});
        //set next nº movimiento
        jdFactura.setTfNumMovimiento(String.valueOf(getFacturaCompraCount() + 1));
        UTIL.loadComboBox(jdFactura.getCbProveedor(), new ProveedorController().findEntities(), false);
        UTIL.loadComboBox(jdFactura.getCbSucursal(), uh.getWrappedSucursales(), false);
        UTIL.loadComboBox(jdFactura.getCbCaja(), uh.getCajas(true), false);
        UTIL.loadComboBox(jdFactura.getCbFacturaTipo(), TIPOS_FACTURA, false);
        UTIL.loadComboBox(jdFactura.getCbFormaPago(), Valores.FormaPago.getFormasDePago(), false);
        UTIL.loadComboBox(jdFactura.getCbProductos(), new ProductoController().findWrappedProductoToCombo(), false);

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
                    producto_selected = new ProductoController().findProductoByCodigo(jdFactura.getTfProductoCodigo().getText());
                    setProducto(producto_selected);
                }
            }
        });
        jdFactura.addListener(this);
        jdFactura.setLocationRelativeTo(owner);
        jdFactura.setVisible(true);
    }

    private void buscarProducto(String codigoProducto) {
        producto_selected = new ProductoController().findProductoByCodigo(codigoProducto);
        setProducto(producto_selected);
    }

    private void addProductoToDetalle() throws MessageException {
        if (producto_selected == null) {
            throw new MessageException("Seleccione un producto");
        }
        int cantidad;
        BigDecimal precioUnitario;
        BigDecimal descuento = BigDecimal.ZERO;
        try {
            cantidad = Integer.valueOf(jdFactura.getTfCantidad().getText());
            if (cantidad < 1) {
                throw new MessageException("La cantidad no puede ser menor a 1");
            }
        } catch (NumberFormatException ex) {
            throw new MessageException("Cantidad no válida (solo números enteros)");
        }
        try {
            precioUnitario = new BigDecimal(jdFactura.getTfPrecioUnitario());
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
        jdFactura.getDtm().addRow(new Object[]{
                    producto_selected.getIva().getIva(),
                    producto_selected.getCodigo(),
                    producto_selected.getNombre() + " " + producto_selected.getMarca().getNombre(),
                    cantidad,
                    UTIL.PRECIO_CON_PUNTO.format(precioUnitario),
                    UTIL.PRECIO_CON_PUNTO.format((precioUnitario.multiply(BigDecimal.valueOf(cantidad)))),
                    //se le suma uno para q coincidan con los valores en la DB de cambio precio
                    jdFactura.getCbCambioPrecio().getSelectedIndex() + 1
                });

        refreshResumen();
    }

    private void deleteProductoFromDetalle() {
        int cant = UTIL.removeSelectedRows(jdFactura.getjTable1());
        if (cant > 0) {
            refreshResumen();
        }
    }

    private void setProducto(Producto producto) {
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

    private void refreshResumen() {
        if (jdFactura.getTfPercIIBB().trim().isEmpty()) {
            jdFactura.setTfPercIIBB("0");
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
        BigDecimal gravado = BigDecimal.ZERO;
        BigDecimal iva10 = BigDecimal.ZERO;
        BigDecimal iva21 = BigDecimal.ZERO;
        BigDecimal percepcion = new BigDecimal(jdFactura.getTfPercIIBB());
        BigDecimal recuperables = new BigDecimal(jdFactura.getTfOtrosImpuestosRecuperables().getText().trim());
        BigDecimal noRecuperables = new BigDecimal(jdFactura.getTfOtrosImpuestosNoRecuperables().getText().trim());
        BigDecimal descuento = new BigDecimal(jdFactura.getTfDescuento().getText().trim());
        BigDecimal otrosIvas = BigDecimal.ZERO;
        DefaultTableModel dtm = jdFactura.getDtm();
        for (int i = (dtm.getRowCount() - 1); i > -1; i--) {
            gravado = gravado.add(new BigDecimal(dtm.getValueAt(i, 5).toString()));
            String iva = dtm.getValueAt(i, 0).toString();
            if (iva.equalsIgnoreCase("10.5")) {
                iva10 = iva10.add(BigDecimal.valueOf(UTIL.getPorcentaje(Double.valueOf(dtm.getValueAt(i, 5).toString()), 10.5)));
            } else if (iva.equalsIgnoreCase("21.0")) {
                iva21 = iva21.add(BigDecimal.valueOf(UTIL.getPorcentaje(Double.valueOf(dtm.getValueAt(i, 5).toString()), 21)));
            } else {
                otrosIvas = otrosIvas.add(BigDecimal.valueOf(UTIL.getPorcentaje(Double.valueOf(dtm.getValueAt(i, 5).toString()), Float.valueOf(iva))));
            }
        }
        gravado = gravado.subtract(descuento);
        jdFactura.setTfGravado(UTIL.PRECIO_CON_PUNTO.format(gravado));
        jdFactura.setTfTotalIVA105(UTIL.PRECIO_CON_PUNTO.format(iva10));
        jdFactura.setTfTotalIVA21(UTIL.PRECIO_CON_PUNTO.format(iva21));
        jdFactura.getTfTotalPercepcion().setText(UTIL.PRECIO_CON_PUNTO.format(percepcion));
        jdFactura.getTfTotalOtrosImpuestos().setText(UTIL.PRECIO_CON_PUNTO.format(recuperables.add(otrosIvas)));
        jdFactura.getTfTotalImpuestosNoRecuperables().setText(UTIL.PRECIO_CON_PUNTO.format(noRecuperables));
        jdFactura.setTfTotal(UTIL.PRECIO_CON_PUNTO.format(gravado.add(iva10).add(iva21).add(percepcion).add(otrosIvas).add(recuperables).add(noRecuperables)));
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

    private void setEntityAndPersist() throws Exception {
        FacturaCompra newFacturaCompra = new FacturaCompra();
        newFacturaCompra.setFacturaCuarto(Short.valueOf(jdFactura.getTfFacturaCuarto()));
        newFacturaCompra.setFacturaOcteto(Integer.valueOf(jdFactura.getTfFacturaOcteto()));
        newFacturaCompra.setFechaCompra(jdFactura.getDcFechaFactura());
        newFacturaCompra.setAnulada(false);
        //set entities
        newFacturaCompra.setProveedor((Proveedor) jdFactura.getCbProveedor().getSelectedItem());
        newFacturaCompra.setSucursal(getSelectedSucursalFromJD());
        newFacturaCompra.setUsuario(UsuarioJpaController.getCurrentUser());
        newFacturaCompra.setCaja((Caja) jdFactura.getCbCaja().getSelectedItem());

        newFacturaCompra.setImporte(Double.valueOf(jdFactura.getTfTotalText()));
        newFacturaCompra.setIva10(Double.valueOf(jdFactura.getTfTotalIVA105()));
        newFacturaCompra.setIva21(Double.valueOf(jdFactura.getTfTotalIVA21()));
        newFacturaCompra.setPercDgr(0.0); // <--- no corresponde en la COMPRA ..BURRO!!
        newFacturaCompra.setPercIva(Double.valueOf(jdFactura.getTfPercIIBB()));
        BigDecimal impRecuperables = new BigDecimal(jdFactura.getTfOtrosImpuestosRecuperables().getText().trim());
        BigDecimal otrosIvas = new BigDecimal(jdFactura.getTfTotalOtrosImpuestos().getText().trim());
        newFacturaCompra.setImpuestosRecuperables(impRecuperables.add(otrosIvas));
        newFacturaCompra.setImpuestosNoRecuperables(new BigDecimal(jdFactura.getTfTotalImpuestosNoRecuperables().getText().trim()));
        newFacturaCompra.setDescuento(new BigDecimal(jdFactura.getTfDescuento().getText().trim()));
        newFacturaCompra.setRemito(0L);
        newFacturaCompra.setTipo(jdFactura.getCbFacturaTipo().getSelectedItem().toString().charAt(0));
        newFacturaCompra.setNumero(Long.valueOf(jdFactura.getTfFacturaCuarto() + jdFactura.getTfFacturaOcteto()));
        newFacturaCompra.setActualizaStock(jdFactura.getCheckActualizaStock().isSelected());
        newFacturaCompra.setFormaPago((short) ((Valores.FormaPago) jdFactura.getCbFormaPago().getSelectedItem()).getId());
        if (newFacturaCompra.getFormaPago() == Valores.FormaPago.CTA_CTE.getId()) {
            newFacturaCompra.setDiasCtaCte(Short.valueOf(jdFactura.getTfDias()));
        }
        newFacturaCompra.setMovimientoInterno(Integer.valueOf(jdFactura.getTfNumMovimiento().getText()));
        newFacturaCompra.setDetalleCompraList(new ArrayList<DetalleCompra>());

        //carga detalleCompra
        DetalleCompra detalleCompra;
        DefaultTableModel dtm = jdFactura.getDtm();
        ProductoController productoCtrl = new ProductoController();
        for (int i = 0; i < dtm.getRowCount(); i++) {
            detalleCompra = new DetalleCompra();
            detalleCompra.setProducto(new ProductoController().findProductoByCodigo(dtm.getValueAt(i, 1).toString()));
            detalleCompra.setCantidad(Integer.valueOf(dtm.getValueAt(i, 3).toString()));
            detalleCompra.setPrecioUnitario(Double.valueOf(dtm.getValueAt(i, 4).toString()));
            newFacturaCompra.getDetalleCompraList().add(detalleCompra);
            productoCtrl.valorizarStock(detalleCompra.getProducto(),
                    detalleCompra.getPrecioUnitario(),
                    detalleCompra.getCantidad(),
                    Integer.parseInt(dtm.getValueAt(i, 6).toString()));
        }

        // 1- PERSIST, 2- UPDATE STOCK, 3- UPDATE CAJA
        try {
            //persistiendo
            create(newFacturaCompra);
            newFacturaCompra = (FacturaCompra) DAO.findEntity(FacturaCompra.class, newFacturaCompra.getId());
            if (newFacturaCompra.getActualizaStock()) {
                //y también la variable Producto.stockActual
                new StockJpaController().updateStock(newFacturaCompra);
            }
            asentarSegunFormaDePago(newFacturaCompra);
        } catch (Exception ex) {
            throw ex;
        }
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
                new CtacteProveedorJpaController().nuevaCtaCte(facturaCompra);
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

    private void checkConstraints() throws MessageException {
        try {
            ((Proveedor) jdFactura.getCbProveedor().getSelectedItem()).getId();
        } catch (ClassCastException ex) {
            throw new MessageException("Proveedor no válido");
        }
        try {
            getSelectedSucursalFromJD();
        } catch (ClassCastException ex) {
            throw new MessageException("Sucursal no válido");
        }
        if (jdFactura.getDcFechaFactura() == null) {
            throw new MessageException("Fecha de factura no válida");
        }

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
        try {
            if (jdFactura.getCbFormaPago().getSelectedItem().toString().equalsIgnoreCase(Valores.FormaPago.CTA_CTE.getNombre())) {
                if (Short.valueOf(jdFactura.getTfDias()) < 1) {
                    throw new MessageException("Días no válidos para Forma de pago: " + FORMAS_PAGO.get(1));
                }
            }

        } catch (NumberFormatException e) {
            throw new MessageException("Días no válidos para Forma de pago: " + FORMAS_PAGO.get(1));
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

        long numeroFactura = Long.valueOf(jdFactura.getTfFacturaCuarto() + jdFactura.getTfFacturaOcteto());
        FacturaCompra old = findFacturaCompra(numeroFactura, ((Proveedor) jdFactura.getCbProveedor().getSelectedItem()), false);
        if (old != null) {
            throw new MessageException("Ya existe la factura Nº: " + numeroFactura
                    + " con el Proveedor " + (Proveedor) jdFactura.getCbProveedor().getSelectedItem());
        }

        BigDecimal total = new BigDecimal(jdFactura.getTfTotalText());
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            throw new MessageException("El total no puede ser negativo");
        }
    }

    private void limpiarPanel() {
        jdFactura.limpiarPanelDatos();
        jdFactura.limpiarPanelProducto();
        jdFactura.getDtm().setRowCount(0);
        jdFactura.limpiarResumen();
        jdFactura.setTfNumMovimiento(String.valueOf(getFacturaCompraCount() + 1));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // <editor-fold defaultstate="collapsed" desc="JButton">
        if (e.getSource().getClass().equals(javax.swing.JButton.class)) {
            javax.swing.JButton boton = (javax.swing.JButton) e.getSource();
            if (boton.getName().equalsIgnoreCase("aceptar")) {
                try {
                    checkConstraints();
                    setEntityAndPersist();
                    jdFactura.showMessage("Factura cargada..", CLASS_NAME, 1);
                    limpiarPanel();
                } catch (MessageException ex) {
                    jdFactura.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    jdFactura.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    Logger.getLogger(FacturaCompraController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (boton.getName().equalsIgnoreCase("add")) {
                try {
                    addProductoToDetalle();
                } catch (MessageException ex) {
                    jdFactura.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    jdFactura.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    Logger.getLogger(FacturaCompraController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (boton.getName().equalsIgnoreCase("del")) {
                deleteProductoFromDetalle();

            } else if (boton.getName().equalsIgnoreCase("cancelar")) {
                jdFactura = null;
                EL_OBJECT = null;
            } else if (boton.getName().equalsIgnoreCase("buscarProducto")) {
                try {
                    initBuscadorProducto();
                } catch (DatabaseErrorException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            } else if (boton.getName().equalsIgnoreCase("filtrarReRe")) {
                try {
                    armarQuery();
                } catch (MessageException ex) {
                    Logger.getLogger(FacturaCompraController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            return;
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="JTextField">
        if (e.getSource().equals(javax.swing.JTextField.class)) {
            javax.swing.JTextField tf = (javax.swing.JTextField) e.getSource();
        }// </editor-fold>

    }

    public FacturaCompra findFacturaCompra(long numero, Proveedor p, boolean anulada) {
        try {
            return (FacturaCompra) DAO.getEntityManager().createNativeQuery(
                    "SELECT o.* from factura_compra o where o.proveedor =" + p.getId()
                    + " AND o.numero = " + numero
                    + " AND o.anulada = " + anulada, FacturaCompra.class).getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }

    }

    public void initBuscador(JFrame frame, final boolean modal, final boolean paraAnular) {
        // <editor-fold defaultstate="collapsed" desc="checking Permiso">
        try {
            UsuarioJpaController.checkPermiso(PermisosJpaController.PermisoDe.COMPRA);
        } catch (MessageException ex) {
            javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage());
            return;
        }// </editor-fold>

        buscador = new JDBuscadorReRe(frame, "Buscador - Factura compra", modal, "Proveedor", "Nº Factura");
        UTIL.loadComboBox(buscador.getCbClieProv(), new ProveedorController().findEntities(), true);
        UTIL.loadComboBox(buscador.getCbCaja(), new UsuarioHelper().getCajas(true), true);
        UTIL.loadComboBox(buscador.getCbSucursal(), new UsuarioHelper().getWrappedSucursales(), true);
        UTIL.loadComboBox(buscador.getCbFormasDePago(), Valores.FormaPago.getFormasDePago(), true);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"facturaID", "Nº factura", "Mov.", "Proveedor", "Importe", "Fecha", "Sucursal", "Caja", "Usuario", "Fecha (Sistema)"},
                new int[]{1, 80, 10, 50, 50, 50, 80, 80, 40, 70},
                new Class<?>[]{null, null, Integer.class, null, String.class, String.class, null, null, null, null});
        //escondiendo facturaID
        UTIL.hideColumnTable(buscador.getjTable1(), 0);
        UTIL.setHorizonalAlignment(buscador.getjTable1(), String.class, SwingConstants.RIGHT);
        buscador.getjTable1().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    if (buscador.getjTable1().getSelectedRow() > -1) {
                        EL_OBJECT = findFacturaCompra(Integer.valueOf(buscador.getDtm().getValueAt(buscador.getjTable1().getSelectedRow(), 0).toString()));
                        setDatosFactura(EL_OBJECT, paraAnular);
                    }
                }
            }
        });
        if (paraAnular) {
            //solo buscar facturas NO anuladas
            buscador.getCheckAnulada().setEnabled(false);
        }
        buscador.setListeners(this);
        buscador.setLocationRelativeTo(frame);
        buscador.setVisible(true);
    }

    private void cargarDtmBuscador(String query) {
        buscador.dtmRemoveAll();
        DefaultTableModel dtm = buscador.getDtm();
        @SuppressWarnings("unchecked")
        List<FacturaCompra> l = DAO.getEntityManager().createNativeQuery(query, FacturaCompra.class).getResultList();
        for (FacturaCompra facturaCompra : l) {
            dtm.addRow(new Object[]{
                        facturaCompra.getId(),
                        JGestionUtils.getNumeracion(facturaCompra),
                        facturaCompra.getMovimientoInterno(),
                        facturaCompra.getProveedor().getNombre(),
                        facturaCompra.getImporte(),
                        UTIL.DATE_FORMAT.format(facturaCompra.getFechaCompra()),
                        facturaCompra.getSucursal().getNombre(),
                        facturaCompra.getCaja(),
                        facturaCompra.getUsuario(),
                        UTIL.DATE_FORMAT.format(facturaCompra.getFechaalta()) + " (" + UTIL.TIME_FORMAT.format(facturaCompra.getFechaalta()) + ")"
                    });
        }
    }

    @SuppressWarnings("unchecked")
    private void armarQuery() throws MessageException {
        StringBuilder query = new StringBuilder("SELECT o.* FROM factura_compra o"
                + " WHERE o.anulada = " + buscador.isCheckAnuladaSelected());

        long numero;
        //filtro por nº de ReRe
        if (buscador.getTfOcteto().length() > 0) {
            try {
                numero = Long.parseLong(buscador.getTfOcteto());
                query.append(" AND o.numero = ").append(numero);
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de " + CLASS_NAME + " no válido");
            }
        }

        //filtro por nº de factura
        if (buscador.getTfFactu4().length() > 0 && buscador.getTfFactu8().length() > 0) {
            try {
                numero = Long.parseLong(buscador.getTfFactu4() + buscador.getTfFactu8());
                query.append(" AND o.numero = ").append(numero);
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de " + CLASS_NAME + " no válido");
            }
        }
        if (buscador.getDcDesde() != null) {
            query.append(" AND o.fecha_compra >= '").append(buscador.getDcDesde()).append("'");
        }
        if (buscador.getDcHasta() != null) {
            query.append(" AND o.fecha_compra <= '").append(buscador.getDcHasta()).append("'");
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
        if (buscador.getCbSucursal().getSelectedIndex() > 0) {
            query.append(" AND o.sucursal = ").append(((ComboBoxWrapper<Sucursal>) buscador.getCbSucursal().getSelectedItem()).getId());
        } else {
            query.append(" AND (");
            for (int i = 1; i < buscador.getCbSucursal().getItemCount(); i++) {
                ComboBoxWrapper<Sucursal> cbw = (ComboBoxWrapper<Sucursal>) buscador.getCbSucursal().getItemAt(i);
                query.append(" o.sucursal=").append(cbw.getId());
                if ((i + 1) < buscador.getCbSucursal().getItemCount()) {
                    query.append(" OR ");
                }
            }
            query.append(")");
        }

        if (buscador.getCbClieProv().getSelectedIndex() > 0) {
            query.append(" AND o.proveedor = ").append(((Proveedor) buscador.getCbClieProv().getSelectedItem()).getId());
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
        query.append( //" GROUP BY o.id, o.fecha_carga, o.hora_carga, o.monto_entrega, o.usuario, o.caja, o.sucursal, o.fecha_remesa, o.estado"
                " ORDER BY o.id");
        System.out.println("QUERY: " + query);
        cargarDtmBuscador(query.toString());
    }

    private void setDatosFactura(final FacturaCompra factura, boolean paraAnular) {
        jdFactura = new JDFacturaCompra(null, true, 1);
        jdFactura.modoVista(false);
        jdFactura.setLocationRelativeTo(buscador);
        UTIL.getDefaultTableModel(jdFactura.getjTable1(), colsName, colsWidth);
        UTIL.hideColumnsTable(jdFactura.getjTable1(), new int[]{0, 6, 7});
        if (paraAnular) {
            jdFactura.getBtnAnular().setEnabled(paraAnular);
            jdFactura.getBtnAnular().addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        String msg_extra_para_ctacte = factura.getFormaPago() == Valores.FormaPago.CTA_CTE.getId() ? "\n- Remesas de pago de Cta.Cte." : "";
                        if (0 == JOptionPane.showOptionDialog(jdFactura, "- Factura Nº:" + UTIL.AGREGAR_CEROS(factura.getNumero() + "\n- Movimiento de Caja\n- Movimiento de Stock" + msg_extra_para_ctacte, 12), "Confirmación de anulación", JOptionPane.YES_OPTION, 2, null, null, null)) {
                            anular(factura);
                        }
                        jdFactura.showMessage("Anulada", CLASS_NAME, 2);
                        jdFactura.dispose();
                    } catch (MessageException ex) {
                        jdFactura.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    }
                }
            });
        }
        // seteando datos de FacturaCompra
        jdFactura.getCbProveedor().addItem(factura.getProveedor());
        jdFactura.getCbSucursal().addItem(new ComboBoxWrapper<Sucursal>(factura.getSucursal(), factura.getSucursal().getId(), factura.getSucursal().getNombre()));
        jdFactura.setDcFechaFactura(factura.getFechaCompra());
        String numFactura = UTIL.AGREGAR_CEROS(factura.getNumero(), 12);
        jdFactura.setTfFacturaCuarto(numFactura.substring(0, 4));
        jdFactura.setTfFacturaOcteto(numFactura.substring(4));
        jdFactura.setTfNumMovimiento(String.valueOf(factura.getMovimientoInterno()));
        jdFactura.getCbFacturaTipo().addItem(factura.getTipo());
        jdFactura.getCbCaja().addItem(factura.getCaja());
        UTIL.loadComboBox(jdFactura.getCbFormaPago(), Valores.FormaPago.getFormasDePago(), false);
        jdFactura.setTfPercIIBB(UTIL.PRECIO_CON_PUNTO.format(factura.getPercIva()));
        jdFactura.getTfOtrosImpuestosRecuperables().setText(UTIL.PRECIO_CON_PUNTO.format(factura.getImpuestosRecuperables()));
        jdFactura.getTfOtrosImpuestosNoRecuperables().setText(UTIL.PRECIO_CON_PUNTO.format(factura.getImpuestosNoRecuperables()));
        jdFactura.getTfDescuento().setText(UTIL.PRECIO_CON_PUNTO.format(factura.getDescuento()));
        jdFactura.getCbFormaPago().setSelectedIndex(factura.getFormaPago() - 1);
        List<DetalleCompra> lista = new DetalleCompraJpaController().findByFactura(factura);
        DefaultTableModel dtm = jdFactura.getDtm();
        for (DetalleCompra detallesCompra : lista) {
            //"IVA", "Cód. Producto", "Producto", "Cantidad", "Precio U.", "Sub total", "Mod
            dtm.addRow(new Object[]{
                        detallesCompra.getProducto().getIva().getIva(),
                        detallesCompra.getProducto().getCodigo(),
                        detallesCompra.getProducto().getNombre() + " " + detallesCompra.getProducto().getMarca().getNombre(),
                        detallesCompra.getCantidad(),
                        detallesCompra.getPrecioUnitario(),
                        UTIL.PRECIO_CON_PUNTO.format((detallesCompra.getCantidad() * detallesCompra.getPrecioUnitario())),
                        null});
        }
        refreshResumen();
        jdFactura.addListener(this);
        jdFactura.setVisible(true);

    }

    public void anular(FacturaCompra factura) throws MessageException {
        if (factura.getAnulada()) {
            throw new MessageException("Ya está anulada esta factura");
        }

        Caja oldCaja = factura.getCaja();
        List<Caja> cajasPermitidasList = new CajaController().findCajasPermitidasByUsuario(UsuarioJpaController.getCurrentUser(), true);
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
                return;
            }
        }
        try {
            cmController.anular(factura, cajaMovimientoActiva);
        } catch (Exception ex) {
            Logger.getLogger(FacturaVentaController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void imprimirFactura() {
        if (EL_OBJECT == null) {
            return;
        }
        try {
            Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_FacturaCompra.jasper", "Factura compra");
            r.addParameter("FACTURA_ID", EL_OBJECT.getId());
            r.addParameter("FORMA_PAGO", Valores.FormaPago.getFormaPago(EL_OBJECT.getFormaPago()).getNombre());
            r.viewReport();
        } catch (Exception ex) {
            Logger.getLogger(FacturaCompraController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initBuscadorProducto() throws DatabaseErrorException {
        ProductoController p = new ProductoController();
        p.initContenedor(null, true, true);
        UTIL.loadComboBox(jdFactura.getCbProductos(), new ProductoController().findWrappedProductoToCombo(), false);
    }

    public JDFacturaCompra getContenedor() {
        return jdFactura;
    }

    private Sucursal getSelectedSucursalFromJD() {
        @SuppressWarnings("unchecked")
        ComboBoxWrapper<Sucursal> cbw = (ComboBoxWrapper<Sucursal>) jdFactura.getCbSucursal().getSelectedItem();
        return cbw.getEntity();
    }
}
