package jgestion.controller;

import jgestion.controller.exceptions.MessageException;
import jgestion.entity.CreditoProveedor;
import jgestion.entity.DetalleNotaCreditoProveedor;
import jgestion.entity.Iva;
import jgestion.entity.NotaCreditoProveedor;
import jgestion.entity.Producto;
import jgestion.entity.Proveedor;
import generics.AutoCompleteComboBox;
import java.awt.Desktop;
import java.awt.TextComponent;
import java.awt.TextField;
import jgestion.gui.JDBuscadorReRe;
import jgestion.gui.JDFacturaCompra;
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
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.text.JTextComponent;
import jgestion.JGestionUtils;
import jgestion.jpa.controller.CreditoProveedorJpaController;
import jgestion.jpa.controller.NotaCreditoProveedorJpaController;
import jgestion.jpa.controller.ProveedorJpaController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utilities.general.UTIL;
import utilities.general.EntityWrapper;
import utilities.general.TableExcelExporter;
import utilities.swing.components.NumberRenderer;

/**
 *
 * @author FiruzzZ
 */
public class NotaCreditoProveedorController implements ActionListener {

    private static final Logger LOG = LogManager.getLogger();
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

    public void displayABM(Window owner, boolean modal) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.COMPRA);
        initComprobanteUI(owner, modal);
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
                    EntityWrapper<Producto> wrap = (EntityWrapper<Producto>) jdFactura.getCbProductos().getSelectedItem();
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
                        EntityWrapper<Producto> wrap = (EntityWrapper<Producto>) jdFactura.getCbProductos().getSelectedItem();
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
                            cargarTablaBuscador(query);
                        } catch (MessageException ex) {
                            ex.displayMessage(buscador);
                        }
                    } else if (boton.equals(buscador.getbImprimir())) {
                        try {
                            String query = armarQuery(false);
                            cargarTablaBuscador(query);
                        } catch (MessageException ex) {
                            ex.displayMessage(buscador);
                        }
                    } else if (boton.equals(buscador.getbExtra())) {
                        if (buscador.getjTable1().getSelectedRow() > -1) {
                            try {
                                EL_OBJECT = jpaController.find((Integer) buscador.getDtm().getValueAt(buscador.getjTable1().getSelectedRow(), 0));
                                if (EL_OBJECT.isAnulada()) {
                                    throw new MessageException("No se puede editar un comprobante ANULADO");
                                }
                                if (EL_OBJECT.getDesacreditado().compareTo(BigDecimal.ZERO) > 0) {
                                    throw new MessageException("No se puede editar porque ya fue acreditada");
                                }
                                setComprobanteUI(EL_OBJECT, false, true);
                                cargarTablaBuscador(armarQuery(false));
                            } catch (MessageException ex) {
                                buscador.showMessage(ex.getMessage(), null, 0);
                            }
                        } else {
                            buscador.showMessage("Seleccione la fila del comprobante que desea editar", null, JOptionPane.WARNING_MESSAGE);
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
            query.append(" AND o.fechaNotaCredito >= '").append(UTIL.yyyy_MM_dd.format(buscador.getDcDesde())).append("'");
        }
        if (buscador.getDcHasta() != null) {
            query.append(" AND o.fechaNotaCredito <= '").append(UTIL.yyyy_MM_dd.format(buscador.getDcHasta())).append("'");
        }
        if (buscador.getDcDesdeSistema() != null) {
            query.append(" AND o.fechaCarga >= '").append(UTIL.yyyy_MM_dd.format(buscador.getDcDesdeSistema())).append("'");
        }
        if (buscador.getDcHastaSistema() != null) {
            Date hastaSist = buscador.getDcHastaSistema();
            query.append(" AND o.fechaCarga < '").append(UTIL.yyyy_MM_dd.format(UTIL.customDateByDays(hastaSist, 1))).append("'");
        }

        if (buscador.getCbClieProv().getSelectedIndex() > 0) {
            query.append(" AND o.proveedor.id = ").append(((EntityWrapper<Proveedor>) buscador.getCbClieProv().getSelectedItem()).getId());
        }

        //acreditada?... 
        if (buscador.getCbFormasDePago().getSelectedIndex() > 0) {
            query.append(" AND o.remesa ").append(buscador.getCbFormasDePago().getSelectedItem().toString().equalsIgnoreCase("si") ? " is not null" : " is null");
        }

        query.append(
                " ORDER BY o.fechaCarga");
        LOG.debug("Buscador.query=" + query.toString());
        return query.toString();
    }

    private void checkConstraints() throws MessageException {
        try {
            ((EntityWrapper<Proveedor>) jdFactura.getCbProveedor().getSelectedItem()).getId();
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
        NotaCreditoProveedor old = jpaController.findBy(numeroFactura, ((EntityWrapper<Proveedor>) jdFactura.getCbProveedor().getSelectedItem()).getEntity());
        if (old != null && (EL_OBJECT == null || !old.getId().equals(EL_OBJECT.getId()))) {
            throw new MessageException("Ya existe la Nota de Credito Nº: " + JGestionUtils.getNumeracion(old, true)
                    + " del Proveedor " + old.getProveedor().getNombre());
        }
    }

    private void setEntityAndPersist() {
        NotaCreditoProveedor o = new NotaCreditoProveedor();
        o.setNumero(Long.valueOf(jdFactura.getTfFacturaCuarto() + jdFactura.getTfFacturaOcteto()));
        o.setFechaNotaCredito(jdFactura.getDcFechaFactura());
        o.setAnulada(false);
        //set entities
        o.setProveedor(new ProveedorJpaController().find((Integer) ((EntityWrapper<Proveedor>) jdFactura.getCbProveedor().getSelectedItem()).getId()));
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
        o.setDetalleNotaCreditoProveedorList(new ArrayList<>(dtm.getRowCount()));
        for (int i = 0; i < dtm.getRowCount(); i++) {
            DetalleNotaCreditoProveedor detalle = new DetalleNotaCreditoProveedor();
            detalle.setProducto(new ProductoController().findProductoByCodigo(dtm.getValueAt(i, 1).toString()));
            detalle.setCantidad(Integer.valueOf(dtm.getValueAt(i, 3).toString()));
            detalle.setPrecioUnitario(new BigDecimal(dtm.getValueAt(i, 4).toString()));
            detalle.setNotaCreditoProveedor(o);
            o.getDetalleNotaCreditoProveedorList().add(detalle);
        }
        if (EL_OBJECT == null) {
            jpaController.persist(o);
            if (jdFactura.getCheckActualizaStock().isSelected()) {
                CreditoProveedor cp = new CreditoProveedor(null, true, o.getImporte(), "NC N°" + JGestionUtils.getNumeracion(o, true), o.getProveedor());
                new CreditoProveedorJpaController().persist(cp);
            }
        } else {
            o.setId(EL_OBJECT.getId());
            jpaController.merge(o);
        }
    }

    public void displayBuscador(Window owner, final boolean modal, final boolean paraAnular) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.COMPRA);

        buscador = new JDBuscadorReRe(owner, null, modal, "Proveedor", null);
        buscador.getbExtra().setVisible(true);
        buscador.setParaNotaCreditoProveedor();
        UTIL.loadComboBox(buscador.getCbClieProv(), JGestionUtils.getWrappedProveedores(new ProveedorJpaController().findAll()), true);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"ID", "Nº NotaCredito", "Proveedor", "Importe", "Fecha", "Remesa", "Usuario", "Fecha (Sistema)"},
                new int[]{1, 80, 150, 60, 50, 50, 80, 70},
                new Class<?>[]{Integer.class, null, null, BigDecimal.class, null, null, null, null});
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
                            setComprobanteUI(EL_OBJECT, paraAnular, false);
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
        buscador.getBtnToExcel().addActionListener(event -> {
            try {
                if (buscador.getjTable1().getRowCount() < 1) {
                    throw new MessageException("No hay info para exportar.");
                }
                File file = JGestionUtils.showSaveDialogFileChooser(buscador, "Archivo Excel (.xls)", null, "xls");
                TableExcelExporter tee = new TableExcelExporter(file, buscador.getjTable1());
                tee.export();
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(buscador, "¿Abrir archivo generado?", null, JOptionPane.YES_NO_OPTION)) {
                    Desktop.getDesktop().open(file);
                }
            } catch (MessageException ex) {
                JOptionPane.showMessageDialog(buscador, ex.getMessage());
            } catch (Exception ex) {
                LOG.error(ex, ex);
                JOptionPane.showMessageDialog(buscador, "Algo salió mal: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        if (paraAnular) {
            //solo buscar facturas NO anuladas
            buscador.getCheckAnulada().setEnabled(false);
        }
        buscador.setListeners(this);
        buscador.setLocationRelativeTo(owner);
        buscador.setVisible(true);
    }

    private void setComprobanteUI(NotaCreditoProveedor notaCredito, boolean paraAnular, boolean toEdit) throws MessageException {
        initComprobanteUI(buscador, true);
        if (paraAnular && toEdit) {
            throw new MessageException("Para anular o para editar? decidite..!");
        }
        jdFactura.modoVista(toEdit);
        if (toEdit) {
            jdFactura.enableDetalle(false);
        }
        if (paraAnular) {
            jdFactura.getBtnAnular().setEnabled(paraAnular);
        }
        // seteando datos de FacturaCompra
        if (jdFactura.getCbProveedor().getItemCount() == 0) {
            jdFactura.getCbProveedor().addItem(new EntityWrapper<>(notaCredito.getProveedor(), notaCredito.getProveedor().getId(), notaCredito.getProveedor().getNombre()));
        } else {
            UTIL.setSelectedItem(jdFactura.getCbProveedor(), notaCredito.getProveedor());
        }
        jdFactura.setDcFechaFactura(notaCredito.getFechaNotaCredito());
        String numFactura = UTIL.AGREGAR_CEROS(notaCredito.getNumero(), 12);
        jdFactura.setTfFacturaCuarto(numFactura.substring(0, 4));
        jdFactura.setTfFacturaOcteto(numFactura.substring(4));
        jdFactura.getTfOtrosImpuestosRecuperables().setText(UTIL.PRECIO_CON_PUNTO.format(notaCredito.getImpuestosRecuperables()));
        DefaultTableModel dtm = jdFactura.getDtm();
        dtm.setRowCount(0);
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
        jdFactura.setLocationRelativeTo(owner);
        jdFactura.addTotalesRefreshListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (((TextComponent) e.getComponent()).isEditable()) {
                    facturaCompraController.refreshResumen();
                }
            }
        });
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

    private void cargarTablaBuscador(String query) {
        DefaultTableModel dtm = buscador.getDtm();
        dtm.setRowCount(0);
        @SuppressWarnings("unchecked")
        List<NotaCreditoProveedor> l = jpaController.findAll(query);
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

    NotaCreditoProveedor displayBuscador(Window owner, final boolean paraAnular, Proveedor proveedor, final boolean selectingMode) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.VENTA);
        buscador = new JDBuscadorReRe(owner, "Buscador - Notas de crédito Proveedor", true, "Proveedor", "Nº");
        buscador.hideCaja();
        buscador.hideFactura();
        buscador.hideFormaPago();
        buscador.getbBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    cargarTablaBuscador(armarQuery(selectingMode));
                } catch (MessageException ex) {
                    ex.displayMessage(buscador);
                }
            }
        });
        UTIL.loadComboBox(buscador.getCbClieProv(), JGestionUtils.getWrappedProveedores(new ProveedorJpaController().findAll()), true);
        UTIL.loadComboBox(buscador.getCbSucursal(), JGestionUtils.getWrappedSucursales(new UsuarioHelper().getSucursales()), true);
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
                            setComprobanteUI(EL_OBJECT, paraAnular, false);
                            //refresh post anulación...
                            if (EL_OBJECT == null) {
                                cargarTablaBuscador(armarQuery(selectingMode));
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
