package controller;

import controller.exceptions.*;
import entity.Caja;
import entity.ChequePropio;
import entity.ChequeTerceros;
import entity.ComprobanteRetencion;
import entity.CtacteProveedor;
import entity.CuentabancariaMovimientos;
import entity.DetalleCajaMovimientos;
import entity.Remesa;
import entity.Sucursal;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import javax.persistence.EntityManager;
import entity.DetalleRemesa;
import entity.FacturaCompra;
import entity.MovimientoConcepto;
import entity.NotaCreditoProveedor;
import entity.Proveedor;
import entity.RemesaPagos;
import entity.enums.ChequeEstado;
import utilities.general.UTIL;
import gui.JDBuscadorReRe;
import gui.JDReRe;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import jgestion.JGestionUtils;
import jpa.controller.ChequePropioJpaController;
import jpa.controller.ChequeTercerosJpaController;
import jpa.controller.ComprobanteRetencionJpaController;
import jpa.controller.CuentabancariaMovimientosJpaController;
import jpa.controller.FacturaCompraJpaController;
import jpa.controller.NotaCreditoProveedorJpaController;
import jpa.controller.RemesaJpaController;
import org.apache.log4j.Logger;
import utilities.gui.SwingUtil;
import utilities.swing.components.ComboBoxWrapper;
import utilities.swing.components.NumberRenderer;

/**
 *
 * @author Administrador
 */
public class RemesaController implements ActionListener, FocusListener {

    private final String CLASS_NAME = Remesa.class.getSimpleName();
    private static final String[] COLUMN_NAMES = {"facturaID", "Factura", "Observación", "Entrega"};
    private static final int[] COLUMN_WIDTH = {1, 50, 100, 50};
    private JDReRe jdReRe;
    private CtacteProveedor selectedCtaCte;
    private Date selectedFechaReRe = null;
    private JDBuscadorReRe buscador;
    private Remesa rereSelected;
    private static Logger LOG = Logger.getLogger(RemesaController.class);
    private RemesaJpaController jpaController;
    private boolean unlockedNumeracion = false;

    public RemesaController() {
        jpaController = new RemesaJpaController();
    }

    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }

    public void initRemesa(JFrame owner, boolean modal, boolean visible) throws MessageException {
        UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.COMPRA);
        jdReRe = new JDReRe(owner, modal);
        jdReRe.setUIForRemesas();
        UTIL.getDefaultTableModel(jdReRe.getTableAPagar(), COLUMN_NAMES, COLUMN_WIDTH);
        jdReRe.getTableAPagar().getColumnModel().getColumn(3).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        UTIL.hideColumnTable(jdReRe.getTableAPagar(), 0);
        UTIL.loadComboBox(jdReRe.getCbSucursal(), new UsuarioHelper().getWrappedSucursales(), false);
        UTIL.loadComboBox(jdReRe.getCbCaja(), new UsuarioHelper().getCajas(true), false);
        UTIL.loadComboBox(jdReRe.getCbClienteProveedor(), new ProveedorController().findEntities(), true);
        UTIL.loadComboBox(jdReRe.getCbCtaCtes(), null, false);
        jdReRe.getbAnular().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
//                    anular(rereSelected);
//                    jdReRe.showMessage(CLASS_NAME + " anulada..", CLASS_NAME, 1);
//                    resetPanel();
//                } catch (MessageException ex) {
//                    jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 2);
                }
            }
        });
        jdReRe.getbAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    checkConstraints();
                    Remesa re = getEntity();
                    persist(re);
                    jdReRe.showMessage(jpaController.getEntityClass().getSimpleName() + "Nº" + JGestionUtils.getNumeracion(re, true) + " registrada..", null, 1);
                    limpiarDetalle();
                    resetPanel();
                } catch (MessageException ex) {
                    jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    LOG.error(ex.getMessage(), ex);
                }
            }
        });
        jdReRe.getBtnADD().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    addEntregaToDetalle();
                } catch (MessageException ex) {
                    jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    LOG.error(ex.getMessage(), ex);
                }
            }
        });
        jdReRe.getBtnDEL().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                delEntragaFromDetalle();
            }
        });
        jdReRe.getCbClienteProveedor().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jdReRe.getCbClienteProveedor().getSelectedIndex() > 0) {
                    cargarFacturasCtaCtes((Proveedor) jdReRe.getCbClienteProveedor().getSelectedItem());
                } else {
                    //si no eligió nada.. vacia el combo de cta cte's
                    UTIL.loadComboBox(jdReRe.getCbCtaCtes(), null, false);
                    limpiarDetalle();
                }
            }
        });
        jdReRe.getCbCtaCtes().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    try {
                        selectedCtaCte = ((ComboBoxWrapper<CtacteProveedor>) jdReRe.getCbCtaCtes().getSelectedItem()).getEntity();
                        jdReRe.setTfImporte(UTIL.DECIMAL_FORMAT.format(selectedCtaCte.getImporte()));
                        jdReRe.setTfPagado(UTIL.DECIMAL_FORMAT.format(selectedCtaCte.getEntregado()));
                        jdReRe.setTfSaldo(UTIL.DECIMAL_FORMAT.format(selectedCtaCte.getImporte().subtract(selectedCtaCte.getEntregado())));
                        jdReRe.setTfEntrega(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getImporte().subtract(selectedCtaCte.getEntregado())));
                    } catch (ClassCastException ex) {
                        selectedCtaCte = null;
                        LOG.trace("No se pudo caster a CtaCteProveedor -> " + jdReRe.getCbCtaCtes().getSelectedItem());
                    }
                } catch (NullPointerException ex) {
                    //cuando no eligio una ctacte aún o el cliente/proveedor no tiene ninguna
                }
            }
        });
        jdReRe.getBtnAddPago().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jdReRe.getCbClienteProveedor().getSelectedIndex() > 0) {
                    try {
                        displayUIPagos(jdReRe.getCbFormasDePago().getSelectedIndex());
                        updateTotales();
                    } catch (MessageException ex) {
                        ex.displayMessage(jdReRe);
                    }
                } else {
                    JOptionPane.showMessageDialog(jdReRe, "Debe seleccionar un Proveedor para poder agregar pagos.", null, JOptionPane.WARNING_MESSAGE);
                }
            }

            /**
             * Efectivo, Cheque Propio, Cheque Tercero, Nota de Crédito,
             * Retención
             */
            private void displayUIPagos(int formaPago) throws MessageException {

                if (formaPago == 0) {
                    displayABMEfectivo();
                } else if (formaPago == 1) {
                    displayABMChequePropio();
                } else if (formaPago == 2) {
                    displayABMChequeTerceros();
                } else if (formaPago == 3) {
                    displayABMNotaCredito();
                } else if (formaPago == 4) {
                    displayABMRetencion();
                } else if (formaPago == 5) {
                    displayABMTransferencia();
                }
            }
        });
        jdReRe.getBtnDelPago().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UTIL.removeSelectedRows(jdReRe.getTablePagos());
                updateTotales();
            }
        });
        jdReRe.setLocationRelativeTo(owner);
        jdReRe.setVisible(visible);
    }

    private void persist(Remesa remesa) {
        jpaController.create(remesa);
        for (DetalleRemesa detalle : remesa.getDetalleRemesaList()) {
            //actuliza saldo pagado de cada ctacte
            actualizarMontoEntrega(detalle.getFacturaCompra(), detalle.getMontoEntrega());
        }

    }

    private void actualizarMontoEntrega(FacturaCompra factu, BigDecimal monto) {
        CtacteProveedor ctacte = new CtacteProveedorJpaController().findCtacteProveedorByFactura(factu.getId());
        Logger.getLogger(this.getClass()).trace("updatingMontoEntrega: CtaCte=" + ctacte.getId()
                + " -> Importe= $" + ctacte.getImporte() + " Entregado= $" + ctacte.getEntregado() + " + " + monto);

        ctacte.setEntregado(ctacte.getEntregado().add(monto));
        if (ctacte.getImporte().compareTo(ctacte.getEntregado()) == 0) {
            ctacte.setEstado(Valores.CtaCteEstado.PAGADA.getId());
            System.out.println("ctaCte PAGADA");
        }
        DAO.doMerge(ctacte);
    }

    private void displayABMEfectivo() {
        BigDecimal monto = jdReRe.displayABMEfectivo(null);
        if (monto != null) {
            DetalleCajaMovimientos d = new DetalleCajaMovimientos();
            d.setIngreso(false);
            d.setMonto(-monto.doubleValue());
            d.setTipo(DetalleCajaMovimientosJpaController.REMESA);
            d.setUsuario(UsuarioController.getCurrentUser());
            d.setMovimientoConcepto(MovimientoConceptoController.EFECTIVO);
            d.setDescripcion(null); // <--- setear con el N° del comprobante
            d.setCajaMovimientos(null); // no te olvides este tampoco!! 
            d.setNumero(0l); // Comprobante.id!!!!
            DefaultTableModel dtm = jdReRe.getDtmPagos();
            dtm.addRow(new Object[]{d, "EF", null, monto});
        }
    }

    private void displayABMChequePropio() {
        try {
            ChequePropio cheque = new ChequePropioController().initABM(jdReRe, false, (Proveedor) jdReRe.getCbClienteProveedor().getSelectedItem());
            if (cheque != null) {
                ChequesController.checkUniquenessOnTable(jdReRe.getDtmPagos(), cheque);
                DefaultTableModel dtm = jdReRe.getDtmPagos();
                dtm.addRow(new Object[]{cheque, "CH", cheque.getBanco().getNombre() + " N°" + cheque.getNumero(), cheque.getImporte()});
            }
        } catch (MessageException ex) {
            ex.displayMessage(jdReRe);
        }
    }

    private void displayABMChequeTerceros() throws MessageException {
        ChequeTerceros cheque = new ChequeTercerosController().initManagerBuscador(jdReRe);
        if (cheque != null) {
            try {
                ChequesController.checkUniquenessOnTable(jdReRe.getDtmPagos(), cheque);
                DefaultTableModel dtm = jdReRe.getDtmPagos();
                dtm.addRow(new Object[]{cheque, "CH", cheque.getBanco().getNombre() + " N°" + cheque.getNumero(), cheque.getImporte()});
            } catch (MessageException ex) {
                ex.displayMessage(jdReRe);
            }
        }
    }

    private void displayABMNotaCredito() {
        try {
            NotaCreditoProveedor notaCredito = new NotaCreditoProveedorController().initBuscador(jdReRe, false, true, false, (Proveedor) jdReRe.getCbClienteProveedor().getSelectedItem());
            if (notaCredito != null) {
                DefaultTableModel dtm = jdReRe.getDtmPagos();
                for (int row = 0; row < dtm.getRowCount(); row++) {
                    if (dtm.getValueAt(row, 0) instanceof NotaCreditoProveedor) {
                        NotaCreditoProveedor old = (NotaCreditoProveedor) dtm.getValueAt(row, 0);
                        if (notaCredito.equals(old)) {
                            throw new MessageException("La nota de crédito  N° " + JGestionUtils.getNumeracion(old, true) + " ya está agregada");
                        }
                    }
                }
                dtm.addRow(new Object[]{notaCredito, "NC", JGestionUtils.getNumeracion(notaCredito, true), notaCredito.getImporte()});
            }
        } catch (MessageException ex) {
            ex.displayMessage(jdReRe);
        }
    }

    private void displayABMRetencion() {
        ComprobanteRetencion comprobante = new ComprobanteRetencionController().displayComprobanteRetencion(jdReRe, null);
        if (comprobante != null) {
            comprobante.setPropio(true);
            DefaultTableModel dtm = jdReRe.getDtmPagos();
            try {
                for (int row = 0; row < dtm.getRowCount(); row++) {
                    if (dtm.getValueAt(row, 0) instanceof ComprobanteRetencion) {
                        ComprobanteRetencion old = (ComprobanteRetencion) dtm.getValueAt(row, 0);
                        if (comprobante.getNumero() == old.getNumero()) {
                            throw new MessageException("Ya existe un comprobante de retención con el N° " + old.getNumero());
                        }
                    }
                }
                dtm.addRow(new Object[]{comprobante, "RE", comprobante.getNumero(), comprobante.getImporte()});
            } catch (MessageException ex) {
                ex.displayMessage(jdReRe);
            }
        }
    }

    private void displayABMTransferencia() {
        String p = jdReRe.getCbClienteProveedor().getSelectedItem().toString();
        CuentabancariaMovimientos comprobante = new CuentabancariaMovimientosController().displayTransferenciaProveedor(jdReRe, p);
        if (comprobante != null) {
            DefaultTableModel dtm = jdReRe.getDtmPagos();
            dtm.addRow(new Object[]{comprobante, "TR", comprobante.getDescripcion(), comprobante.getDebito()});
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    private void checkConstraints() throws MessageException {
        if (jdReRe.getDtmAPagar().getRowCount() < 1) {
            throw new MessageException("No ha hecho ninguna entrega");
        }

        if (jdReRe.getDcFechaReRe() == null) {
            throw new MessageException("Fecha de " + jpaController.getEntityClass().getSimpleName() + " no válida");
        }
        if (0 != jdReRe.getTfTotalAPagar().getText().compareTo(jdReRe.getTfTotalPagado().getText())) {
            throw new MessageException("El importe a pagar no coincide el detalle de pagos.");
        }
        if (unlockedNumeracion) {
            try {
                Integer numero = Integer.valueOf(jdReRe.getTfOcteto());
                if (numero < 1 || numero > 99999999) {
                    throw new MessageException("Número de " + jpaController.getEntityClass().getSimpleName() + " no válido, debe ser mayor a 0 y menor o igual a 99999999");
                }
                @SuppressWarnings("unchecked")
                Remesa old = jpaController.find(((ComboBoxWrapper<Sucursal>) jdReRe.getCbSucursal().getSelectedItem()).getEntity(), numero);
                if (old != null) {
                    throw new MessageException("Ya existe un registro de " + jpaController.getEntityClass().getSimpleName() + " N° " + JGestionUtils.getNumeracion(old, true));
                }
            } catch (NumberFormatException numberFormatException) {
                throw new MessageException("Número de " + jpaController.getEntityClass().getSimpleName() + " no válido, ingrese solo dígitos");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Remesa getEntity() throws Exception {
        Remesa re = new Remesa();
        re.setCaja((Caja) jdReRe.getCbCaja().getSelectedItem());
        re.setSucursal(((ComboBoxWrapper<Sucursal>) jdReRe.getCbSucursal().getSelectedItem()).getEntity());
        if (unlockedNumeracion) {
            re.setNumero(Integer.valueOf(jdReRe.getTfOcteto()));
        } else {
            re.setNumero(jpaController.getNextNumero(re.getSucursal()));
        }
        re.setUsuario(UsuarioController.getCurrentUser());
        re.setEstado(true);
        re.setFechaRemesa(jdReRe.getDcFechaReRe());
        re.setPagos(new ArrayList<RemesaPagos>(jdReRe.getDtmPagos().getRowCount()));
        // 30% faster on ArrayList with initialCapacity
        re.setDetalleRemesaList(new ArrayList<DetalleRemesa>(jdReRe.getDtmAPagar().getRowCount()));
        DefaultTableModel dtm = jdReRe.getDtmAPagar();
        FacturaCompraJpaController fcc = new FacturaCompraJpaController();
        BigDecimal monto = BigDecimal.ZERO;
        for (int i = 0; i < dtm.getRowCount(); i++) {
            DetalleRemesa detalle = new DetalleRemesa();
            detalle.setFacturaCompra(fcc.find((Integer) (dtm.getValueAt(i, 0))));
            detalle.setObservacion(null);
            detalle.setMontoEntrega((BigDecimal) dtm.getValueAt(i, 3));
//            detalle.setAcreditado(false);
            detalle.setRemesa(re);
            re.getDetalleRemesaList().add(detalle);
            monto = monto.add(detalle.getMontoEntrega());
        }
        re.setMontoEntrega(monto.doubleValue());
        dtm = jdReRe.getDtmPagos();
        List<Object> pagos = new ArrayList<Object>(dtm.getRowCount());
        for (int row = 0; row < dtm.getRowCount(); row++) {
            pagos.add(dtm.getValueAt(row, 0));
        }
        re.setPagosEntities(pagos);
        return re;
    }

    private void limpiarDetalle() {
        jdReRe.limpiarDetalle();
        selectedFechaReRe = null;
    }

    private void addEntregaToDetalle() throws MessageException {
        if (jdReRe.getDcFechaReRe() == null) {
            throw new MessageException("Debe especificar una fecha de " + CLASS_NAME + " antes");
        }

        if (selectedCtaCte == null) {
            throw new MessageException("No hay Factura seleccionada");
        }

        if (jdReRe.getDcFechaReRe().before(selectedCtaCte.getFactura().getFechaCompra())) {
            throw new MessageException("La fecha de la " + CLASS_NAME + " no puede ser anterior"
                    + "\n a la fecha de la Factura del Proveedor ("
                    + UTIL.DATE_FORMAT.format(selectedCtaCte.getFechaCarga()) + ")");
        }

        // si ya se cargó un detalle de entrega
        // y sigue habiendo al menos UN detalle agregado (dtm no vacia)
        // ctrla que la fecha de ReRe siga siendo la misma
        if ((selectedFechaReRe != null) && (jdReRe.getDtmAPagar().getRowCount() > 0)
                && (!UTIL.DATE_FORMAT.format(selectedFechaReRe).equals(UTIL.DATE_FORMAT.format(jdReRe.getDcFechaReRe())))) {
            throw new MessageException("La fecha de " + CLASS_NAME + " a sido cambiada"
                    + "\nAnterior: " + UTIL.DATE_FORMAT.format(selectedFechaReRe)
                    + "\nActual: " + UTIL.DATE_FORMAT.format(jdReRe.getDcFechaReRe()));
        } else {
            selectedFechaReRe = jdReRe.getDcFechaReRe();
        }
        FacturaCompra facturaToAddToDetail = selectedCtaCte.getFactura();
        BigDecimal entrega$;
        try {
            entrega$ = new BigDecimal(jdReRe.getTfEntrega());
            if (entrega$.doubleValue() <= 0) {
                throw new MessageException("Monto de entrega no válido (Debe ser mayor a 0)");
            }

            if (entrega$.compareTo(selectedCtaCte.getImporte().subtract(selectedCtaCte.getEntregado())) == 1) {
                throw new MessageException("Monto de entrega no puede ser mayor al Saldo restante");
            }

        } catch (NumberFormatException e) {
            throw new MessageException("Monto de entrega no válido, ingrese solo números y utilice el punto como separador decimal.");
        }

        for (int i = 0; i < jdReRe.getDtmAPagar().getRowCount(); i++) {
            if (facturaToAddToDetail.getId() == (Integer) jdReRe.getDtmAPagar().getValueAt(i, 0)) {
                throw new MessageException("El detalle ya contiene una entrega "
                        + " de esta factura.");
            }
        }
        DefaultTableModel dtm = jdReRe.getDtmAPagar();
        dtm.addRow(new Object[]{
                    facturaToAddToDetail.getId(),
                    JGestionUtils.getNumeracion(facturaToAddToDetail),
                    null,
                    entrega$.setScale(2, RoundingMode.HALF_EVEN)
                });
        updateTotales();
    }

    private void updateTotales() {
        BigDecimal totalAPagar = BigDecimal.ZERO;
        BigDecimal totalPagado = BigDecimal.ZERO;
        DefaultTableModel dtm = (DefaultTableModel) jdReRe.getTableAPagar().getModel();
        for (int row = 0; row < dtm.getRowCount(); row++) {
            BigDecimal monto = (BigDecimal) dtm.getValueAt(row, 3);
            totalAPagar = totalAPagar.add(monto);
        }
        dtm = (DefaultTableModel) jdReRe.getTablePagos().getModel();
        for (int row = 0; row < dtm.getRowCount(); row++) {
            BigDecimal monto = (BigDecimal) dtm.getValueAt(row, 3);
            totalPagado = totalPagado.add(monto);
        }
        jdReRe.getTfTotalAPagar().setText(UTIL.DECIMAL_FORMAT.format(totalAPagar));
        jdReRe.getTfTotalPagado().setText(UTIL.DECIMAL_FORMAT.format(totalPagado));
    }

    private void delEntragaFromDetalle() {
        int selectedRow = jdReRe.getTableAPagar().getSelectedRow();
        if (selectedRow > -1) {
            jdReRe.getDtmAPagar().removeRow(selectedRow);
        }
        updateTotales();
    }

    public JDialog initBuscador(JDialog dialog, boolean modal) throws MessageException {
        UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.COMPRA);
        buscador = new JDBuscadorReRe(dialog, "Buscador - " + CLASS_NAME, modal, "Proveedor", "Nº " + CLASS_NAME);
        buscador.hideFormaPago();
        buscador.setLocationRelativeTo(dialog);
        buscador.setListeners(this);
        UTIL.loadComboBox(buscador.getCbClieProv(), new ProveedorController().findEntities(), true);
        UTIL.loadComboBox(buscador.getCbCaja(), new UsuarioHelper().getCajas(true), true);
        UTIL.loadComboBox(buscador.getCbSucursal(), new UsuarioHelper().getSucursales(), true);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"ID", "(Sucursal) Nº", "Proveedor", "Monto", "Fecha", "Caja", "Usuario", "Fecha/Hora (Sist)"},
                new int[]{1, 50, 150, 50, 50, 50, 50, 80});
        buscador.getjTable1().getColumnModel().getColumn(3).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        UTIL.hideColumnTable(buscador.getjTable1(), 0);
        buscador.getbBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    armarQuery();
                } catch (MessageException ex) {
                    buscador.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    buscador.showMessage(ex.getMessage(), CLASS_NAME, 2);
                }
            }
        });
        buscador.getjTable1().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    setSelectedRemesa();
                    try {
                        setComprobanteUI(rereSelected);
                        jdReRe.setLocationRelativeTo(buscador);
                        jdReRe.setVisible(true);
                    } catch (MessageException ex) {
                        ex.displayMessage(buscador);
                    }
                }
            }
        });
        return buscador;
    }

    private void cargarFacturasCtaCtes(Proveedor proveedor) {
        limpiarDetalle();
        List<CtacteProveedor> ctacteProveedorPendientesList = new CtacteProveedorJpaController().findCtacteProveedorByProveedor(proveedor.getId(), Valores.CtaCteEstado.PENDIENTE.getId());
        UTIL.loadComboBox(jdReRe.getCbCtaCtes(), JGestionUtils.getWrappedCtacteProveedor(ctacteProveedorPendientesList), false);
    }

    private void resetPanel() {
        jdReRe.setDcFechaReRe(null);
        jdReRe.getCbClienteProveedor().setSelectedIndex(0);
        SwingUtil.setComponentsEnabled(jdReRe.getComponents(), false, true);
    }

    private void armarQuery() throws MessageException {
        String query = "SELECT o.* "
                + "FROM remesa o JOIN detalle_remesa dr ON o.id = dr.remesa JOIN sucursal s ON o.sucursal = s.id"
                + " JOIN factura_compra f ON f.id = dr.factura_compra JOIN proveedor p ON p.id = f.proveedor"
                + " JOIN caja c ON o.caja = c.id JOIN usuario u ON o.usuario = u.id";

        long numero;
        //filtro por nº de ReRe
        if (buscador.getTfOcteto().length() > 0) {
            try {
                numero = Long.parseLong(buscador.getTfOcteto());
                query += " AND o.numero = " + numero;
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de " + CLASS_NAME + " no válido");
            }
        }

        //filtro por nº de factura
        if (buscador.getTfFactu4().length() > 0 && buscador.getTfFactu8().length() > 0) {
            try {
                numero = Long.parseLong(buscador.getTfFactu4() + buscador.getTfFactu8());
                query += " AND f.numero = " + numero;
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de " + CLASS_NAME + " no válido");
            }
        }
        if (buscador.getDcDesde() != null) {
            query += " AND o.fecha_remesa >= '" + buscador.getDcDesde() + "'";
        }
        if (buscador.getDcHasta() != null) {
            query += " AND o.fecha_remesa <= '" + buscador.getDcHasta() + "'";
        }
        if (buscador.getCbCaja().getSelectedIndex() > 0) {
            query += " AND o.caja = " + ((Caja) buscador.getCbCaja().getSelectedItem()).getId();
        }
        if (buscador.getCbSucursal().getSelectedIndex() > 0) {
            query += " AND o.sucursal = " + ((Sucursal) buscador.getCbSucursal().getSelectedItem()).getId();
        }
        if (buscador.isCheckAnuladaSelected()) {
            query += " AND o.estado = false";
        }
        if (buscador.getCbClieProv().getSelectedIndex() > 0) {
            query += " AND p.id = " + ((Proveedor) buscador.getCbClieProv().getSelectedItem()).getId();
        }
        query += " ORDER BY o.id";
        System.out.println("QUERY: " + query);
        cargarDtmBuscador(query);
    }

    private void cargarDtmBuscador(String query) {
        DefaultTableModel dtm = (DefaultTableModel) buscador.getjTable1().getModel();
        dtm.setRowCount(0);
        List<Remesa> l = jpaController.findByNativeQuery(query);
        if (l.isEmpty()) {
            JOptionPane.showMessageDialog(buscador, "La busqueda no produjo ningún resultado", null, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        for (Remesa remesa : l) {
            //new String[]{"ID", "Nº", "Monto", "Fecha", "Caja", "Usuario", "Fecha/Hora (Sist)"},
            dtm.addRow(new Object[]{
                        remesa.getId(),
                        JGestionUtils.getNumeracion(remesa, true),
                        remesa.getDetalleRemesaList().get(0).getFacturaCompra().getProveedor().getNombre(),
                        remesa.getMonto(),
                        UTIL.DATE_FORMAT.format(remesa.getFechaRemesa()),
                        remesa.getCaja().getNombre() + "(" + remesa.getCaja().getId() + ")",
                        remesa.getUsuario(),
                        UTIL.TIMESTAMP_FORMAT.format(remesa.getFechaCarga())
                    });
        }
    }

    private void setSelectedRemesa() {
        int rowIndex = buscador.getjTable1().getSelectedRow();
        Integer id = (Integer) buscador.getjTable1().getModel().getValueAt(rowIndex, 0);
        rereSelected = jpaController.find(id);
    }

    /**
     * Setea la ventana de JDReRe de forma q solo se puedan ver los datos y
     * detalles de la Remesa, imprimir y ANULAR, pero NO MODIFICAR
     *
     * @param remesa
     */
    private void setComprobanteUI(Remesa remesa) throws MessageException {
        if (jdReRe == null) {
            initRemesa(null, true, false);
        }
        //por no redundar en DATOOOOOOOOOSS...!!!
        Proveedor p = new FacturaCompraJpaController().find(remesa.getDetalleRemesaList().get(0).getFacturaCompra().getId()).getProveedor();
        //que compare por String's..
        //por si el combo está vacio <VACIO> o no eligió ninguno
        //van a tirar error de ClassCastException
        UTIL.setSelectedItem(jdReRe.getCbSucursal(), remesa.getSucursal().getNombre());
        UTIL.setSelectedItem(jdReRe.getCbCaja(), remesa.getCaja().getNombre());
        UTIL.setSelectedItem(jdReRe.getCbClienteProveedor(), p.getNombre());
        jdReRe.setTfCuarto(UTIL.AGREGAR_CEROS(remesa.getSucursal().getPuntoVenta(), 4));
        jdReRe.setTfOcteto(UTIL.AGREGAR_CEROS(String.valueOf(remesa.getNumero()), 8));
        jdReRe.setDcFechaReRe(remesa.getFechaRemesa());
        jdReRe.setDcFechaCarga(remesa.getFechaCarga());
        cargarDetalleReRe(remesa);
        updateTotales();
        jdReRe.setTfImporte("");
        jdReRe.setTfPagado("");
        jdReRe.setTfSaldo("");
        SwingUtil.setComponentsEnabled(jdReRe.getPanelDatos().getComponents(), false, true);
        SwingUtil.setComponentsEnabled(jdReRe.getPanelAPagar().getComponents(), false, true);
        SwingUtil.setComponentsEnabled(jdReRe.getPanelPagos().getComponents(), false, true);
        jdReRe.getbAceptar().setEnabled(false);
        jdReRe.getbAnular().setEnabled(false);
        jdReRe.getbCancelar().setEnabled(false);
        jdReRe.getbImprimir().setEnabled(true);
    }

    private void cargarDetalleReRe(Remesa remesa) {
        List<DetalleRemesa> detalle = remesa.getDetalleRemesaList();
        DefaultTableModel dtm = jdReRe.getDtmAPagar();
        dtm.setRowCount(0);
        for (DetalleRemesa r : detalle) {
            dtm.addRow(new Object[]{
                        r.getFacturaCompra().getId(),
                        UTIL.AGREGAR_CEROS(String.valueOf(r.getFacturaCompra().getNumero()), 12),
                        null,
                        r.getMontoEntrega()
                    });
        }
        getPagos(remesa);
        dtm = jdReRe.getDtmPagos();
        dtm.setRowCount(0);
        for (Object object : remesa.getPagosEntities()) {
            if (object instanceof ChequePropio) {
                ChequePropio pago = (ChequePropio) object;
                dtm.addRow(new Object[]{pago, "CH", pago.getBanco().getNombre() + " N°" + pago.getNumero(), pago.getImporte()});
            } else if (object instanceof ChequeTerceros) {
                ChequeTerceros pago = (ChequeTerceros) object;
                dtm.addRow(new Object[]{pago, "CH", pago.getBanco().getNombre() + " N°" + pago.getNumero(), pago.getImporte()});
            } else if (object instanceof NotaCreditoProveedor) {
                NotaCreditoProveedor pago = (NotaCreditoProveedor) object;
                dtm.addRow(new Object[]{pago, "NC", JGestionUtils.getNumeracion(pago, true), pago.getImporte()});
            } else if (object instanceof ComprobanteRetencion) {
                ComprobanteRetencion pago = (ComprobanteRetencion) object;
                dtm.addRow(new Object[]{pago, "RE", pago.getNumero(), pago.getImporte()});
            } else if (object instanceof DetalleCajaMovimientos) {
                DetalleCajaMovimientos pago = (DetalleCajaMovimientos) object;
                dtm.addRow(new Object[]{pago, "EF", null, BigDecimal.valueOf(-pago.getMonto())});
            } else if (object instanceof CuentabancariaMovimientos) {
                CuentabancariaMovimientos pago = (CuentabancariaMovimientos) object;
                dtm.addRow(new Object[]{pago, "TR", pago.getDescripcion(), pago.getDebito()});
            }
        }
    }

    public void focusGained(FocusEvent e) {
        //..........
    }

    public void focusLost(FocusEvent e) {
        if (buscador != null) {
            if (e.getSource().getClass().equals(javax.swing.JTextField.class)) {
                javax.swing.JTextField tf = (JTextField) e.getSource();
                if (tf.getName().equalsIgnoreCase("tfocteto")) {
                    if (buscador.getTfOcteto().length() > 0) {
                        buscador.setTfOcteto(UTIL.AGREGAR_CEROS(buscador.getTfOcteto(), 8));
                    }
                } else if (tf.getName().equalsIgnoreCase("tfFactu8")) {
                }
            }
        }

    }

    /**
     * La anulación de una Remesa, resta a
     * <code>CtaCteProveedor.entregado</code> los pagos/entregas
     * (parciales/totales) realizados de cada DetalleRemesa y cambia
     * <code>Remesa.estado = false<code>
     *
     * @throws MessageException
     * @throws IllegalOrphanException
     * @throws NonexistentEntityException
     */
    public void anular(Remesa remesa) throws MessageException, Exception {
        EntityManager em = getEntityManager();
        if (remesa == null) {
            throw new MessageException("Remesa is NULL");
        }
        if (!remesa.getEstado()) {
            throw new MessageException("Esta " + CLASS_NAME + " ya está anulada");
        }

        List<DetalleRemesa> detalleRemesaList = remesa.getDetalleRemesaList();
        CtacteProveedor ctaCteProveedor;
        try {
            em.getTransaction().begin();
            for (DetalleRemesa dr : detalleRemesaList) {
                //se resta la entrega ($) que implicaba este detalle con respecto a la factura
                ctaCteProveedor = new CtacteProveedorJpaController().findCtacteProveedorByFactura(dr.getFacturaCompra().getId());
                ctaCteProveedor.setEntregado(ctaCteProveedor.getEntregado().subtract(dr.getMontoEntrega()));
                // y si había sido pagada en su totalidad..
                if (ctaCteProveedor.getEstado() == Valores.CtaCteEstado.PAGADA.getId()) {
                    ctaCteProveedor.setEstado(Valores.CtaCteEstado.PENDIENTE.getId());
                }
                em.merge(ctaCteProveedor);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
        remesa.setEstado(false);
        jpaController.merge(remesa);
    }

    List<Remesa> findByFactura(FacturaCompra factura) {
        List<DetalleRemesa> detalleRemesaList = jpaController.findDetalleRemesaByFactura(factura);
        List<Remesa> remesas = new ArrayList<Remesa>(detalleRemesaList.size());
        for (DetalleRemesa detalleRecibo : detalleRemesaList) {
            remesas.add(detalleRecibo.getRemesa());
        }
        return remesas;
    }

    private List<Object> getPagos(Remesa remesa) {
        List<Object> pagos = new ArrayList<Object>(remesa.getPagos().size());
        for (RemesaPagos pago : remesa.getPagos()) {
            if (pago.getFormaPago() == 0) {
                DetalleCajaMovimientos o = new DetalleCajaMovimientosJpaController().findDetalleCajaMovimientos(pago.getComprobanteId());
                pagos.add(o);
            } else if (pago.getFormaPago() == 1) {
                ChequePropio o = new ChequePropioJpaController().find(pago.getComprobanteId());
                pagos.add(o);
            } else if (pago.getFormaPago() == 2) {
                ChequeTerceros o = new ChequeTercerosJpaController().find(pago.getComprobanteId());
                pagos.add(o);
            } else if (pago.getFormaPago() == 3) {
                NotaCreditoProveedor o = new NotaCreditoProveedorJpaController().find(pago.getComprobanteId());
                pagos.add(o);
            } else if (pago.getFormaPago() == 4) {
                ComprobanteRetencion o = new ComprobanteRetencionJpaController().find(pago.getComprobanteId());
                pagos.add(o);
            } else if (pago.getFormaPago() == 5) {
                CuentabancariaMovimientos o = new CuentabancariaMovimientosJpaController().find(pago.getComprobanteId());
                pagos.add(o);
            } else {
                throw new IllegalArgumentException("Forma Pago Remesa no válida:" + pago.getFormaPago());
            }
        }
        remesa.setPagosEntities(pagos);
        return pagos;
    }
}
