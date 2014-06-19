package controller;

import controller.exceptions.MessageException;
import entity.CreditoProveedor;
import entity.DetalleNotaCreditoProveedor;
import entity.Iva;
import entity.NotaCreditoProveedor;
import entity.Producto;
import entity.Proveedor;
import generics.AutoCompleteComboBox;
import gui.JDBuscadorReRe;
import gui.JDFacturaCompra;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.text.JTextComponent;
import jgestion.JGestionUtils;
import jpa.controller.CreditoProveedorJpaController;
import jpa.controller.NotaCreditoProveedorJpaController;
import jpa.controller.ProveedorJpaController;
import org.apache.log4j.Logger;
import utilities.general.UTIL;
import utilities.swing.components.ComboBoxWrapper;
import utilities.swing.components.NumberRenderer;

/**
 *
 * @author FiruzzZ
 */
public class NotaCreditoProveedorController implements ActionListener {

    private static final Logger LOG = Logger.getLogger(NotaCreditoProveedorController.class.getName());
    private final NotaCreditoProveedorJpaController jpaController;
    private JDFacturaCompra jdFactura;
    private final String[] colsName = {"IVA", "Cód. Producto", "Producto", "Cantidad", "Precio U.", "Sub total", "Mod", "Producto.instance"};
    private final int[] colsWidth = {10, 70, 180, 10, 30, 30, 1, 1};
    private FacturaCompraController facturaCompraController;
    private NotaCreditoProveedor EL_OBJECT;
    private JDBuscadorReRe buscador;

    public NotaCreditoProveedorController() {
        jpaController = new NotaCreditoProveedorJpaController();
    }

    public void initABM(Window owner, boolean modal) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.COMPRA);
        initComprobanteUI(owner, modal);

        jdFactura.addTotalesRefreshListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                facturaCompraController.refreshResumen();
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
                    facturaCompraController.buscarProducto(wrap.getEntity().getCodigo());
                } catch (ClassCastException ex) {
                    //cuando no seleccionó ningún Producto del combo
                    jdFactura.setTfProductoPrecioActual("");
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
                        facturaCompraController.buscarProducto(wrap.getEntity().getCodigo());
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
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    facturaCompraController.buscarProducto(jdFactura.getTfProductoCodigo().getText());
                }
            }
        });
        jdFactura.addListener(this);
        jdFactura.setLocationRelativeTo(owner);
        jdFactura.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try { //global catch!
            // <editor-fold defaultstate="collapsed" desc="JButton">
            if (e.getSource().getClass().equals(JButton.class)) {
                JButton boton = (JButton) e.getSource();
                if (jdFactura != null && jdFactura.isActive()) {
                    //<editor-fold defaultstate="collapsed" desc="JDFactura">
                    if (boton.getName().equalsIgnoreCase("aceptar")) {
                        try {
                            checkConstraints();
                            setEntityAndPersist();
                            jdFactura.showMessage("Nota Credito cargada..", jpaController.getEntityClass().getSimpleName(), 1);
                            jdFactura.limpiarPanel();
                        } catch (MessageException ex) {
                            jdFactura.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                        }
                    } else if (boton.getName().equalsIgnoreCase("add")) {
                        facturaCompraController.actionPerformed(e);
                    } else if (boton.getName().equalsIgnoreCase("del")) {
                        facturaCompraController.actionPerformed(e);
                    } else if (boton.getName().equalsIgnoreCase("cancelar")) {
                        jdFactura = null;
                        EL_OBJECT = null;
                    } else if (boton.getName().equalsIgnoreCase("buscarProducto")) {
                        facturaCompraController.actionPerformed(e);
                    }
                    //</editor-fold>
                } else if (buscador != null && buscador.isActive()) {
                    //<editor-fold defaultstate="collapsed" desc="Buscador">
                    if (boton.equals(buscador.getbBuscar())) {
                        try {
                            String query = armarQuery(false);
                            cargarDtmBuscador(query);
                        } catch (MessageException ex) {
                            ex.displayMessage(buscador);
                        }
                    } else if (boton.equals(buscador.getbImprimir())) {
                        try {
                            String query = armarQuery(false);
                            cargarDtmBuscador(query);
                        } catch (MessageException ex) {
                            ex.displayMessage(buscador);
                        }
                    }
                    //</editor-fold>
                }
            }// </editor-fold>
        } catch (Exception ex) {
            LOG.error(UsuarioController.getCurrentUser().getNick() + ">>" + ex.getLocalizedMessage(), ex);
            JOptionPane.showMessageDialog(null, "Algo salió mal.\n" + ex.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings({"unchecked", "unchecked"})
    private String armarQuery(boolean selecting) throws MessageException {
        StringBuilder query = new StringBuilder("SELECT o FROM  " + jpaController.getEntityClass().getSimpleName() + " o"
                + " WHERE o.anulada = " + buscador.isCheckAnuladaSelected());
        if (selecting) {
            query.append(" AND o.desacreditado=0");
        }
        long numero;
        if (buscador.getTfCuarto().length() > 0 && buscador.getTfOcteto().length() > 0) {
            try {
                numero = Long.parseLong(buscador.getTfCuarto() + UTIL.AGREGAR_CEROS(buscador.getTfOcteto(), 8));
                query.append(" AND o.numero=").append(numero);
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de " + jpaController.getEntityClass().getSimpleName() + " no válido");
            }
        }
        if (buscador.getDcDesde() != null) {
            query.append(" AND o.fechaNotaCredito >= '").append(buscador.getDcDesde()).append("'");
        }
        if (buscador.getDcHasta() != null) {
            query.append(" AND o.fechaNotaCredito <= '").append(buscador.getDcHasta()).append("'");
        }
        if (buscador.getDcDesdeSistema() != null) {
            query.append(" AND o.fechacarga >= '").append(buscador.getDcDesdeSistema()).append("'");
        }
        if (buscador.getDcHastaSistema() != null) {
            query.append(" AND o.fechacarga <= '").append(buscador.getDcHastaSistema()).append("'");
        }

        if (buscador.getCbClieProv().getSelectedIndex() > 0) {
            query.append(" AND o.proveedor.id = ").append(((ComboBoxWrapper<Proveedor>) buscador.getCbClieProv().getSelectedItem()).getId());
        }

        //acreditada?... 
        if (buscador.getCbFormasDePago().getSelectedIndex() > 0) {
            query.append(" AND o.remesa ").append(buscador.getCbFormasDePago().getSelectedItem().toString().equalsIgnoreCase("si") ? " is not null" : " is null");
        }

        query.append( //" GROUP BY o.id, o.fecha_carga, o.hora_carga, o.monto_entrega, o.usuario, o.caja, o.sucursal, o.fecha_remesa, o.estado"
                " ORDER BY o.fechaCarga");
        LOG.debug("Buscador.query=" + query.toString());
        return query.toString();
    }

    private void checkConstraints() throws MessageException {
        try {
            ((ComboBoxWrapper<Proveedor>) jdFactura.getCbProveedor().getSelectedItem()).getId();
        } catch (ClassCastException ex) {
            throw new MessageException("Proveedor no válido");
        }
        if (jdFactura.getDcFechaFactura() == null) {
            throw new MessageException("Fecha de comprobante no válida");
        }

        if (jdFactura.getTfFacturaCuarto().length() > 4) {
            throw new MessageException("Número de comprobante no válido: " + jdFactura.getTfFacturaCuarto());
        }

        if (jdFactura.getTfFacturaCuarto().length() > 8) {
            throw new MessageException("Número de comprobante no válido (máximo 8 dígitos)");
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
            Double.valueOf(jdFactura.getTfPercIIBB());
        } catch (NumberFormatException e) {
            throw new MessageException("Monto de Percepción de Ingresos Brutos no válido (Solo números y utilizar el PUNTO como separador decimal");
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
        if (jdFactura.getjTable1().getRowCount() < 1) {
            throw new MessageException("El comprobante debe tener al menos un detalle");
        }
        BigDecimal total = new BigDecimal(jdFactura.getTfTotalText());
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new MessageException("Monto del comprobante no válido, debe ser mayor a cero.");
        }

        long numeroFactura = Long.valueOf(jdFactura.getTfFacturaCuarto() + jdFactura.getTfFacturaOcteto());
        NotaCreditoProveedor old = jpaController.findBy(numeroFactura, ((ComboBoxWrapper<Proveedor>) jdFactura.getCbProveedor().getSelectedItem()).getEntity());
        if (old != null) {
            throw new MessageException("Ya existe la Nota de Credito Nº: " + numeroFactura
                    + " del Proveedor " + old.getProveedor().getNombre());
        }
    }

    private void setEntityAndPersist() {
        NotaCreditoProveedor o = new NotaCreditoProveedor();
        o.setNumero(Long.valueOf(jdFactura.getTfFacturaCuarto() + jdFactura.getTfFacturaOcteto()));
        o.setFechaNotaCredito(jdFactura.getDcFechaFactura());
        o.setAnulada(false);
        //set entities
        o.setProveedor(new ProveedorJpaController().find(((ComboBoxWrapper<Proveedor>) jdFactura.getCbProveedor().getSelectedItem()).getId()));
        o.setUsuario(UsuarioController.getCurrentUser());
        o.setDesacreditado(BigDecimal.ZERO);
        o.setImporte(new BigDecimal(jdFactura.getTfTotalText()));
        o.setGravado(new BigDecimal(jdFactura.getTfGravado()));
        o.setNoGravado(new BigDecimal(jdFactura.getTfTotalNoGravado()));
        o.setIva10(new BigDecimal(jdFactura.getTfTotalIVA105()));
        o.setIva21(new BigDecimal(jdFactura.getTfTotalIVA21()));
        BigDecimal impRecuperables = new BigDecimal(jdFactura.getTfOtrosImpuestosRecuperables().getText().trim());
        BigDecimal otrosIvas = new BigDecimal(jdFactura.getTfTotalOtrosImpuestos().getText().trim());
        o.setImpuestosRecuperables(impRecuperables.add(otrosIvas));
        //carga detalleCompra
        DefaultTableModel dtm = jdFactura.getDtm();
        o.setDetalleNotaCreditoProveedorList(new ArrayList<DetalleNotaCreditoProveedor>(dtm.getRowCount()));
        for (int i = 0; i < dtm.getRowCount(); i++) {
            DetalleNotaCreditoProveedor detalle = new DetalleNotaCreditoProveedor();
            detalle.setProducto(new ProductoController().findProductoByCodigo(dtm.getValueAt(i, 1).toString()));
            detalle.setCantidad(Integer.valueOf(dtm.getValueAt(i, 3).toString()));
            detalle.setPrecioUnitario(new BigDecimal(dtm.getValueAt(i, 4).toString()));
            detalle.setNotaCreditoProveedor(o);
            o.getDetalleNotaCreditoProveedorList().add(detalle);
        }
        jpaController.create(o);
        if (jdFactura.getCheckActualizaStock().isSelected()) {
            CreditoProveedor cp = new CreditoProveedor(null, true, o.getImporte(), "NC N°" + JGestionUtils.getNumeracion(o, true), o.getProveedor());
            new CreditoProveedorJpaController().create(cp);
        }
    }

    public void initBuscador(Window frame, final boolean modal, final boolean paraAnular) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.COMPRA);

        buscador = new JDBuscadorReRe(frame, null, modal, "Proveedor", null);
        buscador.setParaNotaCreditoProveedor();
        buscador.getbImprimir().setVisible(false);
        UTIL.loadComboBox(buscador.getCbClieProv(), JGestionUtils.getWrappedProveedores(new ProveedorJpaController().findAll()), true);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"ID", "Nº NotaCredito", "Proveedor", "Importe", "Fecha", "Remesa", "Usuario", "Fecha (Sistema)"},
                new int[]{1, 80, 100, 50, 50, 80, 70},
                new Class<?>[]{Integer.class, null, null, null, String.class, null, null, String.class});
        TableColumnModel tc = buscador.getjTable1().getColumnModel();
        tc.getColumn(3).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        UTIL.hideColumnTable(buscador.getjTable1(), 0);
        UTIL.setHorizonalAlignment(buscador.getjTable1(), String.class, SwingConstants.RIGHT);

        buscador.getjTable1().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    if (buscador.getjTable1().getSelectedRow() > -1) {
                        EL_OBJECT = jpaController.find((Integer) buscador.getDtm().getValueAt(buscador.getjTable1().getSelectedRow(), 0));
                        try {
                            setComprobanteUI(EL_OBJECT, paraAnular);
                        } catch (MessageException ex) {
                            ex.displayMessage(buscador);
                        } catch (Exception ex) {
                            LOG.error(UsuarioController.getCurrentUser().getNick() + ">>" + ex, ex);
                            JOptionPane.showMessageDialog(buscador, "Algo salió mal.\n" + ex.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
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

    private void setComprobanteUI(NotaCreditoProveedor notaCredito, boolean paraAnular) throws MessageException {
        initComprobanteUI(buscador, true);
        jdFactura.modoVista(false);
        jdFactura.setLocationRelativeTo(buscador);
        if (paraAnular) {
            jdFactura.getBtnAnular().setEnabled(paraAnular);
            jdFactura.getBtnAnular().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
//                    try {
//                        String msg_extra_para_ctacte = factura.getFormaPago() == Valores.FormaPago.CTA_CTE.getId() ? "\n- Remesas de pago de Cta.Cte." : "";
//                        if (0 == JOptionPane.showOptionDialog(jdFactura, "- Factura Nº:" + UTIL.AGREGAR_CEROS(factura.getNumero() + "\n- Movimiento de Caja\n- Movimiento de Stock" + msg_extra_para_ctacte, 12), "Confirmación de anulación", JOptionPane.YES_OPTION, 2, null, null, null)) {
//                            anular(factura);
//                        }
//                        jdFactura.showMessage("Anulada", jpaController.getEntityClass().getSimpleName(), 1);
//                        jdFactura.dispose();
//                    } catch (MessageException ex) {
//                        jdFactura.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
//                    }
                }
            });
        }
        // seteando datos de FacturaCompra
        jdFactura.getCbProveedor().addItem(new ComboBoxWrapper<>(notaCredito.getProveedor(), notaCredito.getProveedor().getId(), notaCredito.getProveedor().getNombre()));
        jdFactura.setDcFechaFactura(notaCredito.getFechaNotaCredito());
        String numFactura = UTIL.AGREGAR_CEROS(notaCredito.getNumero(), 12);
        jdFactura.setTfFacturaCuarto(numFactura.substring(0, 4));
        jdFactura.setTfFacturaOcteto(numFactura.substring(4));
        jdFactura.getTfOtrosImpuestosRecuperables().setText(UTIL.PRECIO_CON_PUNTO.format(notaCredito.getImpuestosRecuperables()));
        DefaultTableModel dtm = jdFactura.getDtm();
        dtm.setRowCount(0);
        jpaController.find(notaCredito.getId());
        jpaController.refresh(notaCredito);
        for (DetalleNotaCreditoProveedor detalle : notaCredito.getDetalleNotaCreditoProveedorList()) {
            Iva iva = detalle.getProducto().getIva();
            while (iva == null || iva.getIva() == null) {
                LOG.trace("Producto.id=" + detalle.getProducto().getId() + ", IVA null");
                Producto findProducto = (Producto) DAO.findEntity(Producto.class, detalle.getProducto().getId());
                iva = findProducto.getIva();
                if (iva == null || iva.getIva() == null) {
                    System.out.print(".");
                    iva = new IvaController().findByProducto(detalle.getProducto().getId());
                }
            }
            try {
                //"IVA", "Cód. Producto", "Producto", "Cantidad", "Precio U.", "Sub total", "Mod", Producto.instance
                dtm.addRow(new Object[]{
                    iva.getIva(),
                    detalle.getProducto().getCodigo(),
                    detalle.getProducto().getNombre() + " " + detalle.getProducto().getMarca().getNombre(),
                    detalle.getCantidad(),
                    detalle.getPrecioUnitario(),
                    detalle.getPrecioUnitario().multiply(BigDecimal.valueOf(detalle.getCantidad())).setScale(2, RoundingMode.HALF_EVEN),
                    null,
                    detalle.getProducto()
                });
            } catch (NullPointerException e) {
                LOG.debug(e, e);
                throw new MessageException("Ocurrió un error recuperando el detalle y los datos del Producto:"
                        + "\nCódigo:" + detalle.getProducto().getNombre()
                        + "\nMarca:" + detalle.getProducto().getMarca()
                        + "\nNombre:" + detalle.getProducto().getCodigo()
                        + "\nIVA:" + iva.getIva()
                        + "\n\n   Intente nuevamente.");
            }
        }
        facturaCompraController.refreshResumen();
        jdFactura.addListener(this);
        jdFactura.setVisible(true);
    }

    private void initComprobanteUI(Window owner, boolean modal) {
        jdFactura = new JDFacturaCompra(owner, modal);
        jdFactura.setUIToNotaCredito();
        facturaCompraController = new FacturaCompraController();
        facturaCompraController.setContenedor(jdFactura);
        UTIL.getDefaultTableModel(jdFactura.getjTable1(), colsName, colsWidth);
        jdFactura.getjTable1().getColumnModel().getColumn(3).setCellRenderer(NumberRenderer.getIntegerRenderer());
        jdFactura.getjTable1().getColumnModel().getColumn(4).setCellRenderer(NumberRenderer.getCurrencyRenderer(4));
        jdFactura.getjTable1().getColumnModel().getColumn(5).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        UTIL.hideColumnsTable(jdFactura.getjTable1(), new int[]{0, 6, 7});
        UTIL.loadComboBox(jdFactura.getCbProveedor(), JGestionUtils.getWrappedProveedores(new ProveedorJpaController().findAll()), false);
        UTIL.loadComboBox(jdFactura.getCbFacturaTipo(), FacturaCompraController.TIPOS_FACTURA, false);
        UTIL.loadComboBox(jdFactura.getCbProductos(), new ProductoController().findWrappedProductoToCombo(true), false);
    }

    private void cargarDtmBuscador(String query) {
        buscador.dtmRemoveAll();
        DefaultTableModel dtm = buscador.getDtm();
        @SuppressWarnings("unchecked")
        List<NotaCreditoProveedor> l = jpaController.findByQuery(query);
        for (NotaCreditoProveedor facturaCompra : l) {
            dtm.addRow(new Object[]{
                facturaCompra.getId(),
                JGestionUtils.getNumeracion(facturaCompra, true),
                facturaCompra.getProveedor().getNombre(),
                facturaCompra.getImporte(),
                UTIL.DATE_FORMAT.format(facturaCompra.getFechaNotaCredito()),
                (facturaCompra.getRemesa() != null ? JGestionUtils.getNumeracion(facturaCompra.getRemesa(), true) : null),
                facturaCompra.getUsuario(),
                UTIL.TIMESTAMP_FORMAT.format(facturaCompra.getFechaCarga())
            });
        }
    }

    NotaCreditoProveedor initBuscador(Window owner, final boolean paraAnular, Proveedor proveedor, final boolean selectingMode) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.VENTA);
        buscador = new JDBuscadorReRe(owner, "Buscador - Notas de crédito Proveedor", true, "Proveedor", "Nº");
        buscador.hideCaja();
        buscador.hideFactura();
        buscador.hideFormaPago();
        buscador.getbBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    cargarDtmBuscador(armarQuery(selectingMode));
                } catch (MessageException ex) {
                    ex.displayMessage(buscador);
                }
            }
        });
        UTIL.loadComboBox(buscador.getCbClieProv(), JGestionUtils.getWrappedProveedores(new ProveedorJpaController().findAll()), true);
        UTIL.loadComboBox(buscador.getCbSucursal(), new UsuarioHelper().getWrappedSucursales(), true);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"NotaCreditoID", "Nº Nota de Crédito", "Proveedor", "Importe", "Acreditado", "Fecha", "Usuario", "Fecha (Sistema)"},
                new int[]{1, 60, 150, 50, 50, 50, 50, 70},
                new Class<?>[]{null, null, null, Double.class, Double.class, null, null, null});
        //escondiendo facturaID
        buscador.getjTable1().getColumnModel().getColumn(3).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        buscador.getjTable1().getColumnModel().getColumn(4).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        UTIL.hideColumnTable(buscador.getjTable1(), 0);
        buscador.getjTable1().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2 && buscador.getjTable1().getSelectedRow() > -1) {
                    EL_OBJECT = jpaController.find(Integer.valueOf(buscador.getDtm().getValueAt(buscador.getjTable1().getSelectedRow(), 0).toString()));
                    if (selectingMode) {
                        buscador.dispose();
                    } else {
                        try {
                            setComprobanteUI(EL_OBJECT, paraAnular);
                            //refresh post anulación...
                            if (EL_OBJECT == null) {
                                cargarDtmBuscador(armarQuery(selectingMode));
                            }
                        } catch (MessageException ex) {
                            ex.displayMessage(buscador);
                        }
                    }
                }
            }
        });
        if (paraAnular) {
            buscador.getCheckAnulada().setEnabled(false);
            buscador.getCheckAnulada().setToolTipText("Buscador para ANULAR");
        }
        if (proveedor != null) {
            UTIL.setSelectedItem(buscador.getCbClieProv(), proveedor.getNombre());
            buscador.getCbClieProv().setEnabled(false);
            buscador.getCheckAnulada().setEnabled(false);
        }
        buscador.setLocationRelativeTo(owner);
        buscador.setVisible(true);
        return EL_OBJECT;
    }
}
