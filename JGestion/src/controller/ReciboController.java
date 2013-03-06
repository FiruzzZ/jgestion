package controller;

import controller.exceptions.MessageException;
import entity.*;
import generics.GenericBeanCollection;
import gui.JDBuscadorReRe;
import gui.JDReRe;
import gui.generics.JDialogTable;
import java.awt.Window;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import jgestion.JGestionUtils;
import jgestion.Main;
import jpa.controller.CajaMovimientosJpaController;
import jpa.controller.ChequePropioJpaController;
import jpa.controller.ChequeTercerosJpaController;
import jpa.controller.ComprobanteRetencionJpaController;
import jpa.controller.CuentabancariaMovimientosJpaController;
import jpa.controller.NotaCreditoJpaController;
import jpa.controller.NotaDebitoJpaController;
import jpa.controller.ReciboJpaController;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.log4j.Logger;
import utilities.general.NumberToLetterConverter;
import utilities.general.UTIL;
import utilities.gui.SwingUtil;
import utilities.swing.components.ComboBoxWrapper;
import utilities.swing.components.FormatRenderer;
import utilities.swing.components.NumberRenderer;

/**
 *
 * @author FiruzzZ
 */
public class ReciboController implements ActionListener, FocusListener {

    private static final Logger LOG = Logger.getLogger(ReciboController.class.getName());
    private static final String CLASS_NAME = Recibo.class.getSimpleName();
    private JDReRe jdReRe;
    private CtacteCliente selectedCtaCte;
    private NotaDebito selectedNotaDebito;
    private Date selectedFechaReRe = null;
    private JDBuscadorReRe buscador;
    private Recibo selectedRecibo;
    private ReciboJpaController jpaController;
    private boolean unlockedNumeracion = false;
    private boolean viewMode;

    public ReciboController() {
        jpaController = new ReciboJpaController();
    }

    /**
     * Crea la ventana para realizar Recibo's
     *
     * @param owner owner/parent
     * @param modal debería ser <code>true</code> siempre, no está implementado
     * para false
     * @param setVisible
     * @throws MessageException
     */
    public void initRecibos(Window owner, boolean modal, boolean setVisible) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.VENTA);
        UsuarioHelper uh = new UsuarioHelper();
        if (uh.getSucursales().isEmpty()) {
            throw new MessageException(Main.resourceBundle.getString("unassigned.sucursal"));
        }
        if (uh.getCajas(true).isEmpty()) {
            throw new MessageException(Main.resourceBundle.getString("unassigned.caja"));
        }
        jdReRe = new JDReRe(owner, modal);
        jdReRe.setUIForRecibos();
        UTIL.getDefaultTableModel(jdReRe.getTableAPagar(),
                new String[]{"facturaID", "Factura", "Observación", "Entrega"},
                new int[]{1, 50, 150, 30});
        jdReRe.getTableAPagar().getColumnModel().getColumn(3).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        UTIL.hideColumnTable(jdReRe.getTableAPagar(), 0);
        UTIL.loadComboBox(jdReRe.getCbSucursal(), uh.getWrappedSucursales(), false);
        UTIL.loadComboBox(jdReRe.getCbCaja(), uh.getCajas(true), false);
        UTIL.loadComboBox(jdReRe.getCbClienteProveedor(), new ClienteController().findAll(), true);
        UTIL.loadComboBox(jdReRe.getCbCtaCtes(), null, false);
        setNextNumeroReRe();
        jdReRe.getbAnular().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(jdReRe, "La anulación de un implica:"
                            + "\n- Deshacer el pago realizado a cada Factura."
                            + "\n- La eliminación completa de las Formas de Pago de Tipo: Cheques Terceros, Retenciones, Transferencias."
                            + "\n- La desvinculación de las Notas de Crédito con el Recibo."
                            + "\n- El cambio de estado de Cheques Propios a \"Entregados\" y dejando en blanco el concepto \"Comprobante de Ingreso\"."
                            + "\n- La generación de un movimiento de Egreso de la entrega en Efectivo, en la misma Caja en la cual se originó el Recibo."
                            + "\n¿Confirmar anulación?")) {
                        jpaController.anular(selectedRecibo);
                        jdReRe.showMessage(CLASS_NAME + " anulada..", CLASS_NAME, 1);
                        resetPanel();
                    }
                } catch (MessageException ex) {
                    jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 2);
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
                    Logger.getLogger(ReciboController.class).log(org.apache.log4j.Level.ERROR, null, ex);
                }
            }
        });
        jdReRe.getBtnDEL().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (UTIL.removeSelectedRows(jdReRe.getTableAPagar()) > 0) {
                    updateTotales();
                }
            }
        });
        jdReRe.getbAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (!viewMode) {
                        Recibo re = setAndPersist();
                        selectedRecibo = re;
                        jdReRe.showMessage(jpaController.getEntityClass().getSimpleName() + "Nº" + JGestionUtils.getNumeracion(re, true) + " registrada..", null, 1);
                        limpiarDetalle();
                        resetPanel();
                    }
                } catch (MessageException ex) {
                    jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    LOG.error(ex, ex);
                }
            }
        });
        jdReRe.getbImprimir().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (selectedRecibo != null) {
                        // cuando se re-imprime un recibo elegido desde el buscador
                        doReportRecibo(selectedRecibo);
                    } else {
                        //cuando se está creando un recibo y se va imprimir al tokesaun!
                        Recibo recibo = setAndPersist();
                        selectedRecibo = recibo;
                        doReportRecibo(selectedRecibo);
                        limpiarDetalle();
                        resetPanel();
                    }
                } catch (MessageException ex) {
                    jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    LOG.error(ex, ex);
                }
            }
        });
        jdReRe.getCbClienteProveedor().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jdReRe.getCbClienteProveedor().getSelectedIndex() > 0) {
                    Cliente cliente = (Cliente) jdReRe.getCbClienteProveedor().getSelectedItem();
                    cargarFacturasCtaCtesYNotasDebito(cliente);
                    double credito = new NotaCreditoController().getCreditoDisponible(cliente);
                    jdReRe.getTfCreditoDebitoDisponible().setText(UTIL.PRECIO_CON_PUNTO.format(credito));
                    SwingUtil.setComponentsEnabled(jdReRe.getPanelPagos().getComponents(), true, true);
                } else {
                    //si no eligió nada.. vacia el combo de cta cte's
                    UTIL.loadComboBox(jdReRe.getCbCtaCtes(), null, false);
                    limpiarDetalle();
                    SwingUtil.setComponentsEnabled(jdReRe.getPanelPagos().getComponents(), false, true);
                }
            }
        });
        jdReRe.getCbCtaCtes().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    try {
                        @SuppressWarnings("unchecked")
                        ComboBoxWrapper<?> cbw = (ComboBoxWrapper<?>) jdReRe.getCbCtaCtes().getSelectedItem();
                        if (cbw.getEntity() instanceof CtacteCliente) {
                            selectedCtaCte = (CtacteCliente) cbw.getEntity();
                            selectedNotaDebito = null;
                            jdReRe.setTfImporte(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getImporte()));
                            jdReRe.setTfPagado(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getEntregado()));
                            jdReRe.setTfSaldo(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getImporte() - selectedCtaCte.getEntregado()));
                            jdReRe.setTfEntrega(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getImporte() - selectedCtaCte.getEntregado()));
                            jdReRe.getTfEntrega().setEditable(true);
                        } else {
                            selectedNotaDebito = (NotaDebito) cbw.getEntity();
                            selectedCtaCte = null;
                            jdReRe.setTfImporte(UTIL.PRECIO_CON_PUNTO.format(selectedNotaDebito.getImporte()));
                            jdReRe.setTfPagado(null);
                            jdReRe.setTfSaldo(null);
                            jdReRe.setTfEntrega(UTIL.PRECIO_CON_PUNTO.format(selectedNotaDebito.getImporte()));
                            jdReRe.getTfEntrega().setEditable(false);
                        }
                    } catch (ClassCastException ex) {
                        selectedCtaCte = null;
                        selectedNotaDebito = null;
                        System.out.println("No se pudo caster a CombBoxWrapper<CtaCteProveedor / NotaDebito> -> " + jdReRe.getCbCtaCtes().getSelectedItem());
                    }
                } catch (NullPointerException ex) {
                    //cuando no eligio una ctacte aún o el cliente/proveedor no tiene ninguna
                }
            }
        });
        jdReRe.getCbSucursal().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jdReRe.getCbSucursal().isEnabled()) {
                    setNextNumeroReRe();
                }
            }
        });
        jdReRe.getbCancelar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetPanel();
                limpiarDetalle();
            }
        });
        jdReRe.getBtnAddPago().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    displayUIPagos(jdReRe.getCbFormasDePago().getSelectedIndex());
                    updateTotales();
                } catch (MessageException ex) {
                    ex.displayMessage(jdReRe);
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
        jdReRe.getTablePagos().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (selectedRecibo == null) {
                    int selectedRow = jdReRe.getTablePagos().getSelectedRow();
                    if (e.getClickCount() > 1 && selectedRow > -1) {
                        Object o = jdReRe.getTablePagos().getModel().getValueAt(selectedRow, 0);
                        if (o instanceof DetalleCajaMovimientos) {
                            displayABMEfectivo((DetalleCajaMovimientos) o);
                        } else if (o instanceof ComprobanteRetencion) {
                            displayABMRetencion((ComprobanteRetencion) o);
                        } else if (o instanceof ChequePropio) {
                            JOptionPane.showMessageDialog(jdReRe, "Los cheques propios no puede ser editados", null, JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
            }
        });
        jdReRe.getBtnDetalleCreditoDebito().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    displayDetalleCredito((Cliente) jdReRe.getCbClienteProveedor().getSelectedItem());
                } catch (ClassCastException ex) {
                    JOptionPane.showMessageDialog(jdReRe, "Debe elegir un Cliente", null, JOptionPane.WARNING_MESSAGE);

                }
            }
        });
        jdReRe.setLocationRelativeTo(owner);
        jdReRe.setVisible(setVisible);
    }

    /**
     * Efectivo, Cheque Propio, Cheque Tercero, Nota de Crédito, Retención
     */
    private void displayUIPagos(int formaPago) throws MessageException {

        if (formaPago == 0) {
            displayABMEfectivo(null);
        } else if (formaPago == 1) {
            displayABMChequePropio();
        } else if (formaPago == 2) {
            displayABMChequeTerceros();
        } else if (formaPago == 3) {
            displayABMNotaCredito();
        } else if (formaPago == 4) {
            displayABMRetencion(null);
        } else if (formaPago == 5) {
            displayABMTransferencia();
        }
    }

    private void displayABMEfectivo(DetalleCajaMovimientos toEdit) {
        BigDecimal monto = jdReRe.displayABMEfectivo(toEdit == null ? null : BigDecimal.valueOf(toEdit.getMonto()));
        if (monto != null) {
            DefaultTableModel dtm = jdReRe.getDtmPagos();
            if (toEdit == null) {
                DetalleCajaMovimientos d = new DetalleCajaMovimientos();
                d.setIngreso(true);
                d.setTipo(DetalleCajaMovimientosController.RECIBO);
                d.setUsuario(UsuarioController.getCurrentUser());
                d.setCuenta(CuentaController.SIN_CLASIFICAR);
                d.setDescripcion(null); // <--- setear con el N° del comprobante
                d.setCajaMovimientos(null); // no te olvides este tampoco!! 
                d.setNumero(0l); // Comprobante.id!!!!
                d.setMonto(monto.doubleValue());
                dtm.addRow(new Object[]{d, "EF", null, monto});
            } else {
                toEdit.setMonto(monto.doubleValue());
                int selectedRow = jdReRe.getTablePagos().getSelectedRow();
                dtm.setValueAt(toEdit, selectedRow, 0);
                dtm.setValueAt(toEdit.getNumero(), selectedRow, 2);
                dtm.setValueAt(monto, selectedRow, 3);
            }
        }
    }

    private void displayABMChequePropio() throws MessageException {
        ChequePropio cheque = new ChequePropioController().initManagerBuscador(jdReRe);
        if (cheque != null) {
            ChequesController.checkUniquenessOnTable(jdReRe.getDtmPagos(), cheque);
            DefaultTableModel dtm = jdReRe.getDtmPagos();
            dtm.addRow(new Object[]{cheque, "CH", cheque.getNumero(), cheque.getImporte()});
        }
    }

    private void displayABMChequeTerceros() {
        try {
            ChequeTerceros cheque = new ChequeTercerosController().initABM(jdReRe, false, (Cliente) jdReRe.getCbClienteProveedor().getSelectedItem());
            if (cheque != null) {
                ChequesController.checkUniquenessOnTable(jdReRe.getDtmPagos(), cheque);
                DefaultTableModel dtm = jdReRe.getDtmPagos();
                dtm.addRow(new Object[]{cheque, "CH", cheque.getBanco().getNombre() + " " + cheque.getNumero(), cheque.getImporte()});
            }
        } catch (MessageException ex) {
            ex.displayMessage(jdReRe);
        }
    }

    private void displayABMNotaCredito() {
        try {
            NotaCredito notaCredito = new NotaCreditoController().initBuscador(jdReRe, false, (Cliente) jdReRe.getCbClienteProveedor().getSelectedItem(), true);
            if (notaCredito != null) {
                DefaultTableModel dtm = jdReRe.getDtmPagos();
                for (int row = 0; row < dtm.getRowCount(); row++) {
                    if (dtm.getValueAt(row, 0) instanceof NotaCredito) {
                        NotaCredito old = (NotaCredito) dtm.getValueAt(row, 0);
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

    private void displayABMRetencion(ComprobanteRetencion toEdit) {
        ComprobanteRetencion comprobante = new ComprobanteRetencionController().displayComprobanteRetencion(jdReRe, toEdit);
        if (comprobante != null) {
            DefaultTableModel dtm = jdReRe.getDtmPagos();
            comprobante.setPropio(false);
            try {
                for (int row = 0; row < dtm.getRowCount(); row++) {
                    if (dtm.getValueAt(row, 0) instanceof ComprobanteRetencion) {
                        ComprobanteRetencion old = (ComprobanteRetencion) dtm.getValueAt(row, 0);
                        if (comprobante.getNumero() == old.getNumero()) {
                            throw new MessageException("Ya existe un comprobante de retención con el N° " + old.getNumero());
                        }
                    }
                }
                if (toEdit == null) {
                    dtm.addRow(new Object[]{comprobante, "RE", comprobante.getNumero(), comprobante.getImporte()});
                } else {
                    int selectedRow = jdReRe.getTablePagos().getSelectedRow();
                    dtm.setValueAt(comprobante, selectedRow, 0);
                    dtm.setValueAt(comprobante.getNumero(), selectedRow, 2);
                    dtm.setValueAt(comprobante.getImporte(), selectedRow, 3);
                }
            } catch (MessageException ex) {
                ex.displayMessage(jdReRe);
            }
        }
    }

    private void displayABMTransferencia() {
        Cliente c = (Cliente) jdReRe.getCbClienteProveedor().getSelectedItem();
        CuentabancariaMovimientos comprobante = new CuentabancariaMovimientosController().displayTransferenciaCliente(jdReRe, c.getNombre());
        if (comprobante != null) {
            DefaultTableModel dtm = jdReRe.getDtmPagos();
            dtm.addRow(new Object[]{comprobante, "TR", comprobante.getDescripcion(), comprobante.getCredito()});
        }
    }

    private void displayDetalleCredito(Cliente cliente) {
        JTable tabla = UTIL.getDefaultTableModel(null,
                new String[]{"Nº Nota crédito", "Fecha", "Importe", "Recibo", "Total Acum."},
                new int[]{50, 50, 50, 50, 100},
                new Class<?>[]{null, null, Double.class, null, Double.class});
        TableColumnModel tcm = tabla.getColumnModel();
        tcm.getColumn(2).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        tcm.getColumn(4).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        List<NotaCredito> lista = new NotaCreditoController().findNotaCreditoFrom(cliente, false);
        DefaultTableModel dtm = (DefaultTableModel) tabla.getModel();
        BigDecimal acumulativo = BigDecimal.ZERO;
        for (NotaCredito notaCredito : lista) {
            if (notaCredito.getRecibo() == null) {
                acumulativo = acumulativo.add(notaCredito.getImporte());
            }
            dtm.addRow(new Object[]{
                        JGestionUtils.getNumeracion(notaCredito, true),
                        UTIL.DATE_FORMAT.format(notaCredito.getFechaNotaCredito()),
                        notaCredito.getImporte(),
                        notaCredito.getRecibo() == null ? "" : JGestionUtils.getNumeracion(notaCredito.getRecibo(), true),
                        acumulativo});
        }
        JDialogTable jd = new JDialogTable(jdReRe, "Detalle de crédito: " + cliente.getNombre(), true, tabla);
        jd.setSize(600, 400);
        jd.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    private void checkConstraints() throws MessageException {
        if (jdReRe.getDcFechaReRe() == null) {
            throw new MessageException("Fecha de " + jpaController.getEntityClass().getSimpleName() + " no válida");
        }
        if (jdReRe.getDtmAPagar().getRowCount() < 1) {
            throw new MessageException("No ha hecho ninguna entrega");
        }
        if (unlockedNumeracion) {
            try {
                Integer numero = Integer.valueOf(jdReRe.getTfOcteto());
                if (numero < 1 || numero > 99999999) {
                    throw new MessageException("Número de " + jpaController.getEntityClass().getSimpleName() + " no válido, debe ser mayor a 0 y menor o igual a 99999999");
                }
                Recibo old = jpaController.find(getSelectedSucursalFromJD(), numero);
                if (old != null) {
                    throw new MessageException("Ya existe un registro de " + jpaController.getEntityClass().getSimpleName() + " N° " + JGestionUtils.getNumeracion(old, true));
                }
            } catch (NumberFormatException numberFormatException) {
                throw new MessageException("Número de " + jpaController.getEntityClass().getSimpleName() + " no válido, ingrese solo dígitos");
            }
        }
    }

    private Sucursal getSelectedSucursalFromJD() {
        @SuppressWarnings("unchecked")
        ComboBoxWrapper<Sucursal> cbw = (ComboBoxWrapper<Sucursal>) jdReRe.getCbSucursal().getSelectedItem();
        return cbw.getEntity();
    }

    private Recibo setAndPersist() throws MessageException, Exception {
        checkConstraints();
        Recibo recibo = new Recibo();
        try {
            recibo.setCaja((Caja) jdReRe.getCbCaja().getSelectedItem());
        } catch (ClassCastException e) {
            recibo.setCaja(null);
        }
        recibo.setSucursal(getSelectedSucursalFromJD());
        if (unlockedNumeracion) {
            recibo.setNumero(Integer.valueOf(jdReRe.getTfOcteto()));
        } else {
            recibo.setNumero(jpaController.getNextNumero(recibo.getSucursal()));
        }
        recibo.setUsuario(UsuarioController.getCurrentUser());
        recibo.setEstado(true);
        recibo.setFechaRecibo(jdReRe.getDcFechaReRe());
        recibo.setDetalleReciboList(new ArrayList<DetalleRecibo>(jdReRe.getDtmAPagar().getRowCount()));
        recibo.setPagos(new ArrayList<ReciboPagos>(jdReRe.getDtmPagos().getRowCount()));
        DefaultTableModel dtm = jdReRe.getDtmAPagar();
        FacturaVentaController fcc = new FacturaVentaController();
        BigDecimal monto = BigDecimal.ZERO;
        for (int rowIndex = 0; rowIndex < dtm.getRowCount(); rowIndex++) {
            Object o = dtm.getValueAt(rowIndex, 0);
            DetalleRecibo detalle = new DetalleRecibo();
            if (o instanceof FacturaVenta) {
                FacturaVenta fv = fcc.findFacturaVenta(((FacturaVenta) o).getId());
                detalle.setFacturaVenta(fv);
            } else if (o instanceof NotaDebito) {
                NotaDebito nota = new NotaDebitoJpaController().find(((NotaDebito) o).getId());
                nota.setRecibo(recibo);
                detalle.setNotaDebito(nota);
            } else {
                throw new IllegalArgumentException();
            }
            detalle.setMontoEntrega((BigDecimal) dtm.getValueAt(rowIndex, 3));
            detalle.setRecibo(recibo);
            recibo.getDetalleReciboList().add(detalle);
            monto = monto.add(detalle.getMontoEntrega());
        }
        recibo.setMonto(monto.doubleValue());
        dtm = jdReRe.getDtmPagos();
        List<Object> pagos = new ArrayList<Object>(dtm.getRowCount());
        BigDecimal importePagado = BigDecimal.ZERO;
        for (int row = 0; row < dtm.getRowCount(); row++) {
            pagos.add(dtm.getValueAt(row, 0));
            importePagado = importePagado.add((BigDecimal) dtm.getValueAt(row, 3));
        }
        boolean asentarDiferenciaEnCaja = false;
        if (0 != jdReRe.getTfTotalAPagar().getText().compareTo(jdReRe.getTfTotalPagado().getText())) {
            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(jdReRe, "El importe a pagar no coincide el detalle de pagos."
                    + "¿Desea que la diferencia ($" + UTIL.DECIMAL_FORMAT.format(recibo.getMonto() - importePagado.doubleValue()) + ") sea reflejada en la Caja?", "Arqueo de valores", JOptionPane.YES_NO_OPTION)) {
                asentarDiferenciaEnCaja = true;
            } else {
                throw new MessageException("Operación cancelada");
            }
        }
        recibo.setPagosEntities(pagos);
        recibo.setRetencion(BigDecimal.ZERO);

        //persisting.....
        jpaController.create(recibo);
        Iterator<DetalleRecibo> iterator = recibo.getDetalleReciboList().iterator();
        while (iterator.hasNext()) {
            DetalleRecibo detalle = iterator.next();
            if (detalle.getFacturaVenta() != null) {
                //actuliza saldo pagado de cada ctacte
                actualizarMontoEntrega(detalle.getFacturaVenta(), detalle.getMontoEntrega());
            }
        }
        if (asentarDiferenciaEnCaja) {
            double diferencia = importePagado.doubleValue() - recibo.getMonto();
            DetalleCajaMovimientos d = new DetalleCajaMovimientos();
            d.setIngreso(diferencia < 0);
            d.setTipo(DetalleCajaMovimientosController.RECIBO);
            d.setUsuario(UsuarioController.getCurrentUser());
            d.setCuenta(CuentaController.SIN_CLASIFICAR);
            d.setDescripcion(jpaController.getEntityClass().getSimpleName() + " " + JGestionUtils.getNumeracion(recibo, true) + " (DIFERENCIA)");
            CajaMovimientos cm = new CajaMovimientosJpaController().findCajaMovimientoAbierta(recibo.getCaja());
            d.setCajaMovimientos(cm); // no te olvides este tampoco!! 
            d.setNumero(recibo.getId());
            d.setMonto(d.getIngreso() ? diferencia : -diferencia);
            new DetalleCajaMovimientosController().create(d);
        }
        return recibo;
    }

    private void actualizarMontoEntrega(FacturaVenta factu, BigDecimal monto) {
        CtacteCliente ctacte = new CtacteClienteController().findByFactura(factu.getId());
        ctacte.setEntregado(ctacte.getEntregado() + monto.doubleValue());
        if (ctacte.getImporte() == ctacte.getEntregado()) {
            ctacte.setEstado(Valores.CtaCteEstado.PAGADA.getId());
        }
        DAO.doMerge(ctacte);
    }

    private void addEntregaToDetalle() throws MessageException {
        Date comprobanteFecha;
        if (jdReRe.getDcFechaReRe() == null) {
            throw new MessageException("Debe especificar una fecha de " + CLASS_NAME + " antes de empezar a cargar Facturas.");
        }
        if (selectedCtaCte == null && selectedNotaDebito == null) {
            throw new MessageException("No hay Factura o Nota de Débito seleccionada.");
        }
        if (selectedCtaCte != null) {
            comprobanteFecha = selectedCtaCte.getFactura().getFechaVenta();
        } else {
            comprobanteFecha = selectedNotaDebito.getFechaNotaDebito();
        }
        //hay que ignorar las HH:MM:ss:mmmm de las fechas para hacer las comparaciones
        if (UTIL.getDateYYYYMMDD(jdReRe.getDcFechaReRe()).before(UTIL.getDateYYYYMMDD(comprobanteFecha))) {
            throw new MessageException("La fecha de " + CLASS_NAME + " no puede ser anterior"
                    + "\n a la fecha del comprobante ("
                    + UTIL.DATE_FORMAT.format(comprobanteFecha) + ")");
        }

        // si hay cargado al menos un detalle de entrega
        // ctrla que la fecha de ReRe siga siendo la misma
        if ((selectedFechaReRe != null) && (jdReRe.getDtmAPagar().getRowCount() > 0)
                && (!UTIL.DATE_FORMAT.format(selectedFechaReRe).equals(UTIL.DATE_FORMAT.format(jdReRe.getDcFechaReRe())))) {
            throw new MessageException("La fecha de " + CLASS_NAME + " a sido cambiada"
                    + "\nAnterior: " + UTIL.DATE_FORMAT.format(selectedFechaReRe)
                    + "\nActual: " + UTIL.DATE_FORMAT.format(jdReRe.getDcFechaReRe()));
        } else {
            selectedFechaReRe = jdReRe.getDcFechaReRe();
        }
        double entrega;
        try {
            entrega = Double.parseDouble(jdReRe.getTfEntrega().getText());
        } catch (NumberFormatException ex) {
            throw new MessageException("Monto de entrega no válido");
        }
        if (entrega <= 0) {
            throw new MessageException("Monto de entrega no válido (Debe ser mayor a 0)");
        }
        FacturaVenta facturaToAddToDetail = null;
        if (selectedCtaCte != null) {
            if (entrega > (selectedCtaCte.getImporte() - selectedCtaCte.getEntregado())) {
                throw new MessageException("Monto de entrega no puede ser mayor al Saldo restante (" + (selectedCtaCte.getImporte() - selectedCtaCte.getEntregado()) + ")");
            }
            facturaToAddToDetail = selectedCtaCte.getFactura();
            //check que no se inserte + de una entrega de la misma factura
            //        double entregaParcial = 0;
            for (int i = 0; i < jdReRe.getDtmAPagar().getRowCount(); i++) {
                if (facturaToAddToDetail.equals(jdReRe.getDtmAPagar().getValueAt(i, 0))) {
                    throw new MessageException("La factura " + JGestionUtils.getNumeracion(facturaToAddToDetail) + " ya se ha agregada al detalle");
                }
            }
        } else {
            for (int i = 0; i < jdReRe.getDtmAPagar().getRowCount(); i++) {
                if (selectedNotaDebito.equals(jdReRe.getDtmAPagar().getValueAt(i, 0))) {
                    throw new MessageException("La Nota de Débito " + JGestionUtils.getNumeracion(selectedNotaDebito, true) + " ya se ha agregada al detalle");
                }
            }

        }
        jdReRe.getDtmAPagar().addRow(new Object[]{
                    selectedCtaCte != null ? facturaToAddToDetail : selectedNotaDebito,
                    selectedCtaCte != null ? JGestionUtils.getNumeracion(facturaToAddToDetail) : JGestionUtils.getNumeracion(selectedNotaDebito, true),
                    null,
                    BigDecimal.valueOf(entrega)
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

    /**
     *
     * @param owner
     * @param modal
     * @param toAnular if true, se chequea
     * {@link PermisosJpaController.PermisoDe#ANULAR_COMPROBANTES}
     */
    public void showBuscador(Window owner, boolean modal, boolean toAnular) {
        // <editor-fold defaultstate="collapsed" desc="checking Permiso">
        try {
            UsuarioController.checkPermiso(PermisosController.PermisoDe.VENTA);
            if (toAnular) {
                UsuarioController.checkPermiso(PermisosController.PermisoDe.ANULAR_COMPROBANTES);
            }
        } catch (MessageException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            return;
        }// </editor-fold>
        buscador = new JDBuscadorReRe(owner, "Buscador - " + CLASS_NAME, modal, "Cliente", "Nº " + CLASS_NAME);
        initBuscador(toAnular);

    }

    private void showReciboViewerMode(boolean toAnular) {
        try {
            viewMode = true;
            setComprobanteUI(selectedRecibo);
            jdReRe.getbAceptar().setVisible(false);
            jdReRe.getbCancelar().setVisible(false);
            jdReRe.getbAnular().setVisible(toAnular);
            jdReRe.setLocationRelativeTo(buscador);
            jdReRe.setVisible(true);
        } catch (MessageException ex) {
            jdReRe.showMessage(ex.getMessage(), null, JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Setea la ventana de JDReRe de forma q solo se puedan ver los datos y
     * detalles de la Recibo, imprimir y ANULAR, pero NO MODIFICAR
     *
     * @param recibo
     */
    private void setComprobanteUI(Recibo recibo) throws MessageException {
        if (jdReRe == null) {
            initRecibos(null, true, false);
        }
//        bloquearVentana(true);
        //por no redundar en DATOOOOOOOOOSS...!!!
        Cliente cliente;
        if (recibo.getDetalleReciboList().get(0).getFacturaVenta() != null) {
            cliente = new FacturaVentaController().findFacturaVenta(recibo.getDetalleReciboList().get(0).getFacturaVenta().getId()).getCliente();
        } else {
            cliente = new NotaDebitoJpaController().find(recibo.getDetalleReciboList().get(0).getNotaDebito().getId()).getCliente();
        }
        //que compare por String's..
        //por si el combo está vacio <VACIO> o no eligió ninguno
        //van a tirar error de ClassCastException
        UTIL.setSelectedItem(jdReRe.getCbSucursal(), recibo.getSucursal().getNombre());
        UTIL.setSelectedItem(jdReRe.getCbCaja(), recibo.getCaja().toString());
        UTIL.setSelectedItem(jdReRe.getCbClienteProveedor(), cliente.getNombre());
        jdReRe.setTfCuarto(UTIL.AGREGAR_CEROS(recibo.getSucursal().getPuntoVenta(), 4));
        jdReRe.setTfOcteto(UTIL.AGREGAR_CEROS(recibo.getNumero(), 8));
        jdReRe.setDcFechaReRe(recibo.getFechaRecibo());
        jdReRe.setDcFechaCarga(recibo.getFechaCarga());
        cargarDetalleReRe(recibo);
        updateTotales();
        SwingUtil.setComponentsEnabled(jdReRe.getPanelDatos().getComponents(), false, true);
        SwingUtil.setComponentsEnabled(jdReRe.getPanelAPagar().getComponents(), false, true);
        SwingUtil.setComponentsEnabled(jdReRe.getPanelPagos().getComponents(), false, true);
        jdReRe.setTfImporte(null);
        jdReRe.setTfPagado(null);
        jdReRe.setTfSaldo(null);
        jdReRe.getbImprimir().setEnabled(true);
        jdReRe.getLabelAnulado().setVisible(!recibo.getEstado());
    }

    private Recibo initBuscador(final boolean toAnular) {
        buscador.setParaRecibos();
        UTIL.loadComboBox(buscador.getCbClieProv(), JGestionUtils.getWrappedClientes(new ClienteController().findAll()), true);
        UTIL.loadComboBox(buscador.getCbCaja(), new UsuarioHelper().getCajas(Boolean.TRUE), true);
        UTIL.loadComboBox(buscador.getCbSucursal(), new UsuarioHelper().getWrappedSucursales(), true);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"Instance", "Nº Recibo", "Monto", "Fecha", "Caja", "Usuario", "Fecha/Hora (Sist)"},
                new int[]{1, 80, 50, 40, 50, 50, 70});
        buscador.getjTable1().getColumnModel().getColumn(2).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        buscador.getjTable1().getColumnModel().getColumn(3).setCellRenderer(FormatRenderer.getDateRenderer());
        buscador.getjTable1().getColumnModel().getColumn(6).setCellRenderer(FormatRenderer.getDateTimeRenderer());
        UTIL.hideColumnTable(buscador.getjTable1(), 0);
        buscador.getjTable1().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    int rowIndex = buscador.getjTable1().getSelectedRow();
                    int id = Integer.valueOf(buscador.getjTable1().getModel().getValueAt(rowIndex, 0).toString());
                    selectedRecibo = jpaController.find(id);
                    showReciboViewerMode(toAnular);
                }
            }
        });
        buscador.getbBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    armarQuery();
                } catch (MessageException ex) {
                    buscador.showMessage(ex.getMessage(), CLASS_NAME, 2);
                }
            }
        });
        buscador.setVisible(true);
        return selectedRecibo;
    }

    private void cargarFacturasCtaCtesYNotasDebito(Cliente cliente) {
        limpiarDetalle();
        List<CtacteCliente> ctacteClientePendientesList = new CtacteClienteController().findByCliente(cliente, Valores.CtaCteEstado.PENDIENTE.getId());
        List<NotaDebito> notas = new NotaDebitoJpaController().findBy(cliente, false);
        List<Object> l = new ArrayList<Object>(ctacteClientePendientesList.size() + notas.size());
        for (CtacteCliente o : ctacteClientePendientesList) {
            l.add(o);
        }
        for (NotaDebito o : notas) {
            l.add(o);
        }
        UTIL.loadComboBox(jdReRe.getCbCtaCtes(), JGestionUtils.getWrappedCtacteCliente(l), false);
    }

    private void limpiarDetalle() {
        jdReRe.limpiarDetalle();
        selectedFechaReRe = null;
    }

    /**
     * Resetea la ventana; - pone la fecha actual - clienteProveedor.index(0) -
     * setea el NextNumeroReRe - rereSelected = null;
     */
    private void resetPanel() {
        jdReRe.setDcFechaReRe(new Date());
        jdReRe.getCbClienteProveedor().setSelectedIndex(0);
        setNextNumeroReRe();
        jdReRe.getTfCreditoDebitoDisponible().setText(null);
        jdReRe.getTfTotalPagado().setText("0");
        jdReRe.getTfTotalAPagar().setText("0");
        selectedRecibo = null;
    }

    @SuppressWarnings("unchecked")
    private void armarQuery() throws MessageException {
        StringBuilder query = new StringBuilder(
                "SELECT o.*"
                + " FROM recibo o JOIN caja c ON (o.caja = c.id)"
                + " JOIN detalle_recibo dr ON (o.id = dr.recibo)"
                + " LEFT JOIN factura_venta f ON (dr.factura_venta = f.id)"
                + " LEFT JOIN nota_debito ON (dr.nota_debito_id = nota_debito.id)"
                + " LEFT JOIN cliente p ON (f.cliente = p.id) "
                + " LEFT JOIN cliente pp ON (nota_debito.cliente_id = pp.id) "
                + " JOIN usuario u ON (o.usuario = u.id) JOIN sucursal s ON (o.sucursal = s.id)"
                + " WHERE o.id is not null  ");

        long numero;
        //filtro por nº de ReRe
        if (buscador.getTfOcteto().length() > 0) {
            try {
                numero = Long.parseLong(buscador.getTfOcteto());
                query.append(" AND o.numero= ").append(numero);
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de " + CLASS_NAME + " no válido");
            }
        }

        //filtro por nº de factura
        if (buscador.getTfFactu8().length() > 0) {
            try {
                numero = Long.parseLong(buscador.getTfFactu8());
                query.append(" AND f.numero = ").append(numero);
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de " + CLASS_NAME + " no válido");
            }
        }
        if (buscador.getDcDesde() != null) {
            query.append(" AND o.fecha_recibo >= '").append(buscador.getDcDesde()).append("'");
        }
        if (buscador.getDcHasta() != null) {
            query.append(" AND o.fecha_recibo <= '").append(buscador.getDcHasta()).append("'");
        }
        if (buscador.getDcDesdeSistema() != null) {
            query.append(" AND o.fecha_carga >= '").append(UTIL.clearTimeFields(buscador.getDcDesdeSistema())).append("'");
        }
        if (buscador.getDcHastaSistema() != null) {
            query.append(" AND o.fecha_carga <= '").append(UTIL.clearTimeFields(buscador.getDcHastaSistema())).append("'");
        }
        if (buscador.getCbCaja().getSelectedIndex() > 0) {
            query.append(" AND o.caja = ").append(((Caja) buscador.getCbCaja().getSelectedItem()).getId());
        } else {
            query.append(" AND (");
            for (int i = 1; i < buscador.getCbCaja().getItemCount(); i++) {
                Caja caja = (Caja) buscador.getCbCaja().getItemAt(i);
                query.append(" o.caja=").append(caja.getId());
                if ((i + 1) < buscador.getCbCaja().getItemCount()) {
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
        if (buscador.isCheckAnuladaSelected()) {
            query.append(" AND o.estado = false");
        }
        if (buscador.getCbClieProv().getSelectedIndex() > 0) {
            query.append(" AND (")
                    .append(" p.id = ").append(((ComboBoxWrapper<Cliente>) buscador.getCbClieProv().getSelectedItem()).getId())
                    .append(" OR pp.id = ").append(((ComboBoxWrapper<Cliente>) buscador.getCbClieProv().getSelectedItem()).getId())
                    .append(")");
        }

        query.append(" GROUP BY o.id, o.numero, o.fecha_carga, o.monto, o.retencion, o.usuario, o.caja, o.sucursal, o.fecha_recibo, o.estado"
                + " ORDER BY o.sucursal, o.numero");
        LOG.debug(query.toString());
        cargarBuscador(query.toString());
    }

    private void cargarBuscador(String query) {
        DefaultTableModel dtm = (DefaultTableModel) buscador.getjTable1().getModel();
        dtm.setRowCount(0);
        List<Recibo> l = jpaController.findByNativeQuery(query);
        for (Recibo o : l) {
            dtm.addRow(new Object[]{
                        o.getId(),
                        JGestionUtils.getNumeracion(o, true),
                        BigDecimal.valueOf(o.getMonto()),
                        o.getFechaRecibo(),
                        o.getCaja().getNombre(),
                        o.getUsuario().getNick(),
                        o.getFechaCarga()
                    });
        }
    }

    private void cargarDetalleReRe(Recibo recibo) {
        List<DetalleRecibo> detalleReciboList = recibo.getDetalleReciboList();
        DefaultTableModel dtm = jdReRe.getDtmAPagar();
        dtm.setRowCount(0);
        for (DetalleRecibo r : detalleReciboList) {
            dtm.addRow(new Object[]{
                        r.getFacturaVenta() != null ? r.getFacturaVenta() : r.getNotaDebito(),
                        r.getFacturaVenta() != null ? JGestionUtils.getNumeracion(r.getFacturaVenta()) : JGestionUtils.getNumeracion(r.getNotaDebito(), true),
                        null,
                        r.getMontoEntrega()
                    });
        }
        loadPagos(recibo);
        dtm = jdReRe.getDtmPagos();
        dtm.setRowCount(0);
        for (Object object : recibo.getPagosEntities()) {
            if (object instanceof ChequePropio) {
                ChequePropio pago = (ChequePropio) object;
                dtm.addRow(new Object[]{pago, "CH", pago.getBanco().getNombre() + " N°" + pago.getNumero(), pago.getImporte()});
            } else if (object instanceof ChequeTerceros) {
                ChequeTerceros pago = (ChequeTerceros) object;
                dtm.addRow(new Object[]{pago, "CH", pago.getBanco().getNombre() + " N°" + pago.getNumero(), pago.getImporte()});
            } else if (object instanceof NotaCredito) {
                NotaCredito pago = (NotaCredito) object;
                dtm.addRow(new Object[]{pago, "NC", JGestionUtils.getNumeracion(pago, true), pago.getImporte()});
            } else if (object instanceof ComprobanteRetencion) {
                ComprobanteRetencion pago = (ComprobanteRetencion) object;
                dtm.addRow(new Object[]{pago, "RE", pago.getNumero(), pago.getImporte()});
            } else if (object instanceof DetalleCajaMovimientos) {
                DetalleCajaMovimientos pago = (DetalleCajaMovimientos) object;
                dtm.addRow(new Object[]{pago, "EF", null, BigDecimal.valueOf(pago.getMonto())});
            } else if (object instanceof CuentabancariaMovimientos) {
                CuentabancariaMovimientos pago = (CuentabancariaMovimientos) object;
                dtm.addRow(new Object[]{pago, "TR", pago.getDescripcion(), pago.getCredito()});
            }
        }
    }

    public void loadPagos(Recibo recibo) {
        List<Object> pagos = new ArrayList<Object>(recibo.getPagos().size());
        for (ReciboPagos pago : recibo.getPagos()) {
            if (pago.getFormaPago() == 0) {
                DetalleCajaMovimientos o = new DetalleCajaMovimientosController().findDetalleCajaMovimientos(pago.getComprobanteId());
                pagos.add(o);
            } else if (pago.getFormaPago() == 1) {
                ChequePropio o = new ChequePropioJpaController().find(pago.getComprobanteId());
                pagos.add(o);
            } else if (pago.getFormaPago() == 2) {
                ChequeTerceros o = new ChequeTercerosJpaController().find(pago.getComprobanteId());
                pagos.add(o);
            } else if (pago.getFormaPago() == 3) {
                NotaCredito o = new NotaCreditoJpaController().find(pago.getComprobanteId());
                pagos.add(o);
            } else if (pago.getFormaPago() == 4) {
                ComprobanteRetencion o = new ComprobanteRetencionJpaController().find(pago.getComprobanteId());
                pagos.add(o);
            } else if (pago.getFormaPago() == 5) {
                CuentabancariaMovimientos o = new CuentabancariaMovimientosJpaController().find(pago.getComprobanteId());
                pagos.add(o);
            } else {
                throw new IllegalArgumentException("Forma Pago Recibo no válida:" + pago.getFormaPago());
            }
        }
        recibo.setPagosEntities(pagos);
    }

    @Override
    public void focusGained(FocusEvent e) {
        //..........
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (buscador != null) {
            if (e.getSource().getClass().equals(javax.swing.JTextField.class)) {
                javax.swing.JTextField tf = (javax.swing.JTextField) e.getSource();
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
     * Carga el POSIBLE siguiente N°, en caso de multiples instancias no pincha
     * ni corta.
     */
    private void setNextNumeroReRe() {
        Sucursal sucursal = getSelectedSucursalFromJD();
        Integer nextRe = jpaController.getNextNumero(sucursal);
        jdReRe.setTfCuarto(UTIL.AGREGAR_CEROS(sucursal.getPuntoVenta(), 4));
        jdReRe.setTfOcteto(UTIL.AGREGAR_CEROS(nextRe, 8));
    }

    private void doReportRecibo(Recibo recibo) throws Exception {
        if (recibo == null && recibo.getId() == null) {
            throw new MessageException("No hay " + CLASS_NAME + " seleccionado");
        }
        DetalleRecibo d = recibo.getDetalleReciboList().get(0);
        Cliente cliente = d.getFacturaVenta() != null ? d.getFacturaVenta().getCliente() : d.getNotaDebito().getCliente();
        List<GenericBeanCollection> cc = new ArrayList<GenericBeanCollection>(recibo.getDetalleReciboList().size());
        for (DetalleRecibo dr : recibo.getDetalleReciboList()) {
            String comprobanteString = dr.getFacturaVenta() != null ? JGestionUtils.getNumeracion(dr.getFacturaVenta()) : JGestionUtils.getNumeracion(dr.getNotaDebito(), true);
            cc.add(new GenericBeanCollection(comprobanteString, dr.getMontoEntrega()));
        }
        List<GenericBeanCollection> pp = new ArrayList<GenericBeanCollection>(recibo.getPagos().size());
        for (Object object : recibo.getPagosEntities()) {
            if (object instanceof ChequePropio) {
                ChequePropio pago = (ChequePropio) object;
                pp.add(new GenericBeanCollection("CHP " + pago.getBanco().getNombre() + " N°" + pago.getNumero(), pago.getImporte()));
            } else if (object instanceof ChequeTerceros) {
                ChequeTerceros pago = (ChequeTerceros) object;
                pp.add(new GenericBeanCollection("CH " + pago.getBanco().getNombre() + " N°" + pago.getNumero(), pago.getImporte()));
            } else if (object instanceof NotaCredito) {
                NotaCredito pago = (NotaCredito) object;
                pp.add(new GenericBeanCollection("NC " + JGestionUtils.getNumeracion(pago, true), pago.getImporte()));
            } else if (object instanceof ComprobanteRetencion) {
                ComprobanteRetencion pago = (ComprobanteRetencion) object;
                pp.add(new GenericBeanCollection("RE " + pago.getNumero(), pago.getImporte()));
            } else if (object instanceof DetalleCajaMovimientos) {
                DetalleCajaMovimientos pago = (DetalleCajaMovimientos) object;
                pp.add(new GenericBeanCollection("EF", BigDecimal.valueOf(pago.getMonto())));
            } else if (object instanceof CuentabancariaMovimientos) {
                CuentabancariaMovimientos pago = (CuentabancariaMovimientos) object;
                int indexOf = pago.getDescripcion().indexOf(", ");
                pp.add(new GenericBeanCollection("TR " + pago.getDescripcion().substring(0, indexOf), pago.getCredito()));
            }
        }
        JRDataSource c = new JRBeanCollectionDataSource(cc);
        JRDataSource p = new JRBeanCollectionDataSource(pp);
        Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_Recibo_ctacte.jasper", "Recibo");
        r.addParameter("RECIBO_ID", recibo.getId());
        r.addParameter("CLIENTE_ID", cliente.getId());
        r.addCurrent_User();
        r.addMembreteParameter();
        r.addParameter("comprobantes", c);
        r.addParameter("pagos", p);
        r.addParameter("son_pesos", NumberToLetterConverter.convertNumberToLetter(recibo.getMonto(), false));
        r.viewReport();
    }

    /**
     * Retorna todos los {@link Recibo} que contengan en su
     * {@link DetalleRecibo} a factura.
     *
     * @param factura que deben contenedor los Recibos en su detalle
     * @return una lista de Recibo's
     */
    public List<Recibo> findRecibosByFactura(FacturaVenta factura) {
        List<DetalleRecibo> detalleReciboList = jpaController.findDetalleReciboEntitiesByFactura(factura);
        List<Recibo> recibosList = new ArrayList<Recibo>(detalleReciboList.size());
        for (DetalleRecibo detalleRecibo : detalleReciboList) {
            if (!recibosList.contains(detalleRecibo.getRecibo())) {
                recibosList.add(detalleRecibo.getRecibo());
            }
        }
        return recibosList;
    }

    public void initRecibosNumeracionManual(Window owner) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.VENTA_NUMERACION_MANUAL);
        unlockedNumeracion = true;
        initRecibos(owner, true, false);
        jdReRe.setTfOctetoEditable(true);
        jdReRe.setVisible(true);
    }
}
