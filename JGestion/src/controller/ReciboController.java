package controller;

import controller.exceptions.MessageException;
import controller.exceptions.NonexistentEntityException;
import controller.exceptions.PreexistingEntityException;
import entity.*;
import gui.JDBuscadorReRe;
import gui.JDReRe;
import gui.generics.JDialogTable;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import jgestion.JGestionUtils;
import jgestion.Main;
import jpa.controller.CajaMovimientosJpaController;
import jpa.controller.ReciboJpaController;
import org.apache.log4j.Logger;
import utilities.general.UTIL;
import utilities.swing.components.ComboBoxWrapper;
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
    private Date selectedFechaReRe = null;
    private JDBuscadorReRe buscador;
    private Recibo rereSelected;
    private ReciboJpaController jpaController;
    private boolean unlockedNumeracion = false;

    public ReciboController() {
        jpaController = new ReciboJpaController();
    }

    /**
     * Crea la ventana para realizar Recibo's
     *
     * @param frame owner/parent
     * @param modal debería ser <code>true</code> siempre, no está implementado
     * para false
     * @param setVisible
     * @throws MessageException
     */
    public void initRecibos(JFrame frame, boolean modal, boolean setVisible) throws MessageException {
        UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.VENTA);
        UsuarioHelper uh = new UsuarioHelper();
        if (uh.getSucursales().isEmpty()) {
            throw new MessageException(Main.resourceBundle.getString("unassigned.sucursal"));
        }
        if (uh.getCajas(true).isEmpty()) {
            throw new MessageException(Main.resourceBundle.getString("unassigned.caja"));
        }
        jdReRe = new JDReRe(frame, modal);
        jdReRe.setUIForRecibos();
        UTIL.getDefaultTableModel(jdReRe.getTableAPagar(),
                new String[]{"facturaID", "Factura", "Observación", "Entrega"},
                new int[]{1, 50, 150, 30},
                new Class<?>[]{null, null, null, Double.class});
        jdReRe.getTableAPagar().getColumnModel().getColumn(3).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        UTIL.hideColumnTable(jdReRe.getTableAPagar(), 0);
        UTIL.loadComboBox(jdReRe.getCbSucursal(), uh.getWrappedSucursales(), false);
        UTIL.loadComboBox(jdReRe.getCbCaja(), uh.getCajas(true), false);
        UTIL.loadComboBox(jdReRe.getCbClienteProveedor(), new ClienteController().findEntities(), true);
        UTIL.loadComboBox(jdReRe.getCbCtaCtes(), null, false);
        setNextNumeroReRe();
        jdReRe.getbAnular().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    jpaController.anular(rereSelected);
                    jdReRe.showMessage(CLASS_NAME + " anulada..", CLASS_NAME, 1);
                    resetPanel();
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
                delEntragaFromDetalle();
            }
        });
        jdReRe.getbAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    checkConstraints();
                    Recibo recibo = getEntity();
                    persist(recibo);
                    rereSelected = recibo;
                    jdReRe.showMessage(CLASS_NAME + " creado..", CLASS_NAME, 1);
                    limpiarDetalle();
                    resetPanel();
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
                    if (rereSelected != null) {
                        // cuando se re-imprime un recibo elegido desde el buscador (uno pre existente)
                        imprimirRecibo(rereSelected);
                    } else {
                        //cuando se está creando un recibo y se va imprimir al tokesaun!
                        checkConstraints();
                        Recibo recibo = getEntity();
                        persist(recibo);
                        rereSelected = recibo;
                        imprimirRecibo(rereSelected);
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
                    cargarFacturasCtaCtes(cliente);
                    double credito = new NotaCreditoController().getCreditoDisponible(cliente);
                    jdReRe.getTfCreditoDebitoDisponible().setText(UTIL.PRECIO_CON_PUNTO.format(credito));
                    jdReRe.getTfCreditoDebitoRestante().setText(UTIL.PRECIO_CON_PUNTO.format(credito));
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
                        @SuppressWarnings("unchecked")
                        ComboBoxWrapper<CtacteCliente> cbw = (ComboBoxWrapper<CtacteCliente>) jdReRe.getCbCtaCtes().getSelectedItem();
                        selectedCtaCte = cbw.getEntity();
                        jdReRe.setTfImporte(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getImporte()));
                        jdReRe.setTfPagado(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getEntregado()));
                        jdReRe.setTfSaldo(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getImporte() - selectedCtaCte.getEntregado()));
                        jdReRe.setTfEntrega(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getImporte() - selectedCtaCte.getEntregado()));
                    } catch (ClassCastException ex) {
                        selectedCtaCte = null;
                        System.out.println("No se pudo caster a CombBoxWrapper<CtaCteProveedor> -> " + jdReRe.getCbCtaCtes().getSelectedItem());
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
                displayUIPagos(jdReRe.getCbFormasDePago().getSelectedIndex());
                updateTotales();

            }
        });
        jdReRe.getBtnDelPago().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UTIL.removeSelectedRows(jdReRe.getTablePagos());
                updateTotales();
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
        jdReRe.setLocationRelativeTo(frame);
        jdReRe.setVisible(setVisible);
    }

    /**
     * Efectivo, Cheque Propio, Cheque Tercero, Nota de Crédito, Retención
     */
    private void displayUIPagos(int formaPago) {

        if (formaPago == 0) {
            jdReRe.displayABMEfectivo();
        } else if (formaPago == 1) {
            displayABMChequePropio();
        } else if (formaPago == 2) {
            displayABMChequeTerceros();
        } else if (formaPago == 3) {
            displayABMNotaCredito();
        } else if (formaPago == 4) {
            displayABMRetencion();
        }
    }

    private void displayABMChequePropio() {
        ChequePropio cheque = null;
        JDialog jd = new ChequePropioController().initManager(null, true);
        jd.setLocationRelativeTo(jdReRe);
        jd.setVisible(true);
        if (cheque != null) {
            DefaultTableModel dtm = jdReRe.getDtmPagos();
            dtm.addRow(new Object[]{cheque, "CH", cheque.getNumero(), cheque.getImporte()});
        }
    }

    private void displayABMChequeTerceros() {
        try {
            ChequeTerceros cheque = new ChequeTercerosController().initABM(jdReRe, false, (Cliente) jdReRe.getCbClienteProveedor().getSelectedItem());
            if (cheque != null) {
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

    private void displayABMRetencion() {
        ComprobanteRetencion comprobante = new ComprobanteRetencionController().displayComprobanteRetencion(jdReRe, null);
        if (comprobante != null) {
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

    private void displayDetalleCredito(Cliente cliente) {
        JTable tabla = UTIL.getDefaultTableModel(null,
                new String[]{"Nº Nota crédito", "Fecha", "Importe", "Desacreditado", "Total Acum."},
                new int[]{50, 50, 50, 50, 100},
                new Class<?>[]{null, null, Double.class, Double.class, Double.class});
        TableColumnModel tcm = tabla.getColumnModel();
        tcm.getColumn(2).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        tcm.getColumn(3).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        tcm.getColumn(4).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        List<NotaCredito> lista = new NotaCreditoController().findNotaCreditoFrom(cliente, false);
        DefaultTableModel dtm = (DefaultTableModel) tabla.getModel();
        BigDecimal acumulativo = BigDecimal.ZERO;
        for (NotaCredito notaCredito : lista) {
            acumulativo = acumulativo.add(notaCredito.getImporte().subtract(notaCredito.getDesacreditado()));
            dtm.addRow(new Object[]{
                        JGestionUtils.getNumeracion(notaCredito, true),
                        UTIL.DATE_FORMAT.format(notaCredito.getFechaNotaCredito()),
                        notaCredito.getImporte(),
                        notaCredito.getDesacreditado(),
                        acumulativo});
        }
        JDialogTable jd = new JDialogTable(jdReRe, "Detalle de crédito: " + cliente.getNombre(), true, dtm);
        jd.setSize(600, 400);
        jd.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JButton) {
            if (jdReRe != null && jdReRe.isActive()) {
            }
        }
    }

    private void checkConstraints() throws MessageException {
        if (jdReRe.getDtmAPagar().getRowCount() < 1) {
            throw new MessageException("No ha hecho ninguna entrega");
        }

        if (jdReRe.getDcFechaReRe() == null) {
            throw new MessageException("Fecha de " + CLASS_NAME + " no válida");
        }

        if (0 != new BigDecimal(jdReRe.getTfTotalAPagar().getText()).compareTo(new BigDecimal(jdReRe.getTfTotalPagado().getText()))) {
            throw new MessageException("El importe del recibo no coincide el detalle de pagos.");
        }
        if (unlockedNumeracion) {
            try {
                Integer numero = Integer.valueOf(jdReRe.getTfOcteto());
                if (numero < 1 && numero > 99999999) {
                    throw new MessageException("Número de Recibo no válido, debe ser mayor a 0 y menor o igual a 99999999");
                }
                Recibo oldRecibo = jpaController.find(getSelectedSucursalFromJD(), numero);
                if (oldRecibo != null) {
                    throw new MessageException("Ya existe un registro de Recibo N° " + JGestionUtils.getNumeracion(oldRecibo, true));
                }
            } catch (NumberFormatException numberFormatException) {
                throw new MessageException("Número de Recibo no válido, ingrese solo dígitos");
            }
        }
    }

    private Sucursal getSelectedSucursalFromJD() {
        @SuppressWarnings("unchecked")
        ComboBoxWrapper<Sucursal> cbw = (ComboBoxWrapper<Sucursal>) jdReRe.getCbSucursal().getSelectedItem();
        return cbw.getEntity();
    }

    private Recibo getEntity() throws Exception {
        Recibo recibo = new Recibo();
        recibo.setCaja((Caja) jdReRe.getCbCaja().getSelectedItem());
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
        for (int i = 0; i < dtm.getRowCount(); i++) {
            DetalleRecibo detalle = new DetalleRecibo();
            detalle.setFacturaVenta(fcc.findFacturaVenta((Integer) dtm.getValueAt(i, 0)));
            detalle.setMontoEntrega((BigDecimal) dtm.getValueAt(i, 3));
//            detalle.setAcreditado((Boolean) dtm.getValueAt(i, 4));
            detalle.setRecibo(recibo);
            recibo.getDetalleReciboList().add(detalle);
        }
        dtm = jdReRe.getDtmPagos();
        List<Object> pagos = new ArrayList<Object>(dtm.getRowCount());
        for (int row = 0; row < dtm.getRowCount(); row++) {
            pagos.add(dtm.getValueAt(row, 0));
        }
        recibo.setPagosEntities(pagos);
        recibo.setMonto(Double.parseDouble(jdReRe.getTfTotalAPagar().getText()));
        recibo.setRetencion(BigDecimal.ZERO);
        return recibo;
    }

    private void persist(Recibo recibo) throws MessageException, Exception {
        jpaController.create(recibo);
        Iterator<DetalleRecibo> iterator = recibo.getDetalleReciboList().iterator();
        while (iterator.hasNext()) {
            DetalleRecibo detalle = iterator.next();
            //actuliza saldo pagado de cada ctacte
            actualizarMontoEntrega(detalle.getFacturaVenta(), detalle.getMontoEntrega());
        }
    }

    private void actualizarMontoEntrega(FacturaVenta factu, BigDecimal monto) {
        CtacteCliente ctacte = new CtacteClienteJpaController().findCtacteClienteByFactura(factu.getId());
        LOG.debug("updatingMontoEntrega: CtaCte:" + ctacte.getId() + " -> Importe: " + ctacte.getImporte() + " Entregado:" + ctacte.getEntregado() + " + " + monto);
        ctacte.setEntregado(ctacte.getEntregado() + monto.doubleValue());
        if (ctacte.getImporte() == ctacte.getEntregado()) {
            ctacte.setEstado(Valores.CtaCteEstado.PAGADA.getId());
            LOG.trace("CtaCte Nº:" + ctacte.getId() + " PAGADA");
        }
        DAO.doMerge(ctacte);
    }

    private void addEntregaToDetalle() throws MessageException {
        if (jdReRe.getDcFechaReRe() == null) {
            throw new MessageException("Debe especificar una fecha de " + CLASS_NAME + " antes de empezar a cargar Facturas.");
        }
        if (selectedCtaCte == null) {
            throw new MessageException("No hay Factura seleccionada.");
        }
        //hay que ignorar las HH:MM:ss:mmmm de las fechas para hacer las comparaciones
        if (UTIL.getDateYYYYMMDD(jdReRe.getDcFechaReRe()).before(UTIL.getDateYYYYMMDD(selectedCtaCte.getFactura().getFechaVenta()))) {
            throw new MessageException("La fecha de " + CLASS_NAME + " no puede ser anterior"
                    + "\n a la fecha de Facturación ("
                    + UTIL.DATE_FORMAT.format(selectedCtaCte.getFactura().getFechaVenta()) + ")");
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
        String observacion = null;//@Deprecated
        try {
            entrega = Double.parseDouble(jdReRe.getTfEntrega());
        } catch (NumberFormatException ex) {
            throw new MessageException("Monto de entrega no válido");
        }
        if (entrega <= 0) {
            throw new MessageException("Monto de entrega no válido (Debe ser mayor a 0)");
        }
        if (entrega > (selectedCtaCte.getImporte() - selectedCtaCte.getEntregado())) {
            throw new MessageException("Monto de entrega no puede ser mayor al Saldo restante (" + (selectedCtaCte.getImporte() - selectedCtaCte.getEntregado()) + ")");
        }
        if (observacion != null && observacion.length() > 200) {
            throw new MessageException("La Observación no puede superar los 200 caracteres (no es una novela)");
        }
        FacturaVenta facturaToAddToDetail = selectedCtaCte.getFactura();
        //check que no se inserte + de una entrega de la misma factura
//        double entregaParcial = 0;
        for (int i = 0; i < jdReRe.getDtmAPagar().getRowCount(); i++) {
            if (facturaToAddToDetail.getId() == (Integer) jdReRe.getDtmAPagar().getValueAt(i, 0)) {
                throw new MessageException("La factura " + JGestionUtils.getNumeracion(facturaToAddToDetail) + " ya se ha agregada al detalle");
            }
        }
        jdReRe.getDtmAPagar().addRow(new Object[]{
                    facturaToAddToDetail.getId(),
                    JGestionUtils.getNumeracion(facturaToAddToDetail),
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
        jdReRe.getTfTotalAPagar().setText(UTIL.PRECIO_CON_PUNTO.format(totalAPagar));
        jdReRe.getTfTotalPagado().setText(UTIL.PRECIO_CON_PUNTO.format(totalPagado));
    }

    /**
     * Borra la fila seleccionada, del DetalleRecibo
     */
    private void delEntragaFromDetalle() {
        if (UTIL.removeSelectedRows(jdReRe.getTableAPagar()) > 0) {
            updateTotales();
        }
    }

    public void initBuscador(JFrame frame, boolean modal) {
        // <editor-fold defaultstate="collapsed" desc="checking Permiso">
        try {
            UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.VENTA);
        } catch (MessageException ex) {
            javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage());
            return;
        }// </editor-fold>
        buscador = new JDBuscadorReRe(frame, "Buscador - " + CLASS_NAME, modal, "Cliente", "Nº " + CLASS_NAME);
        initBuscador();
    }

    private void initBuscador() {
        buscador.setParaRecibos();
        UTIL.loadComboBox(buscador.getCbClieProv(), new ClienteController().findAllWrapped(), true);
        UTIL.loadComboBox(buscador.getCbCaja(), new UsuarioHelper().getCajas(Boolean.TRUE), true);
        UTIL.loadComboBox(buscador.getCbSucursal(), new UsuarioHelper().getWrappedSucursales(), true);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"Instance", "Nº Recibo", "Monto", "Retención", "Fecha", "Caja", "Usuario", "Fecha/Hora (Sist)"},
                new int[]{1, 80, 30, 30, 40, 50, 50, 70},
                new Class<?>[]{null, null, String.class, String.class, String.class, null, null, null});
        UTIL.hideColumnTable(buscador.getjTable1(), 0);
        UTIL.setHorizonalAlignment(buscador.getjTable1(), String.class, SwingConstants.RIGHT);
        buscador.getjTable1().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    setSelectedRecibo();
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
    }

    private void cargarFacturasCtaCtes(Cliente cliente) {
        limpiarDetalle();
        List<CtacteCliente> ctacteClientePendientesList = new CtacteClienteJpaController().findCtacteClienteByCliente(cliente.getId(), Valores.CtaCteEstado.PENDIENTE.getId());
        UTIL.loadComboBox(jdReRe.getCbCtaCtes(), JGestionUtils.getWrappedCtacteCliente(ctacteClientePendientesList), false);
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
        jdReRe.getTfCreditoDebitoRestante().setText(null);
        jdReRe.getTfTotalPagado().setText("0");
        jdReRe.getTfTotalAPagar().setText("0");
        rereSelected = null;
    }

    @SuppressWarnings({"unchecked", "unchecked"})
    private void armarQuery() throws MessageException {
        StringBuilder query = new StringBuilder(
                "SELECT o.*"
                + " FROM recibo o JOIN caja c ON (o.caja = c.id)"
                + " JOIN detalle_recibo dr ON (o.id = dr.recibo) JOIN factura_venta f ON (dr.factura_venta = f.id)"
                + " JOIN cliente p ON (f.cliente = p.id) JOIN usuario u ON (o.usuario = u.id) JOIN sucursal s ON (o.sucursal = s.id)"
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
            query.append(" AND p.id = ").append(((ComboBoxWrapper<Cliente>) buscador.getCbClieProv().getSelectedItem()).getId());
        }

        query.append(" GROUP BY o.id, o.numero, o.fecha_carga, o.monto, o.retencion, o.usuario, o.caja, o.sucursal, o.fecha_recibo, o.estado"
                + " ORDER BY o.sucursal, o.numero");
        cargarBuscador(query.toString());
    }

    private void cargarBuscador(String query) {
        buscador.dtmRemoveAll();
        DefaultTableModel dtm = (DefaultTableModel) buscador.getjTable1().getModel();
        List<Recibo> l = jpaController.findByNativeQuery(query);
        for (Recibo o : l) {
            dtm.addRow(new Object[]{
                        o.getId(),
                        JGestionUtils.getNumeracion(o, true),
                        UTIL.PRECIO_CON_PUNTO.format(o.getMonto()),
                        UTIL.PRECIO_CON_PUNTO.format(o.getRetencion()),
                        UTIL.DATE_FORMAT.format(o.getFechaRecibo()),
                        o.getCaja().getNombre(),
                        o.getUsuario().getNick(),
                        UTIL.TIMESTAMP_FORMAT.format(o.getFechaCarga())
                    });
        }
    }

    private void setSelectedRecibo() {
        int rowIndex = buscador.getjTable1().getSelectedRow();
        int id = Integer.valueOf(buscador.getjTable1().getModel().getValueAt(rowIndex, 0).toString());
        rereSelected = jpaController.find(id);
        if (rereSelected != null) {
            if (jdReRe == null) {
                try {
                    initRecibos(null, true, false);
                } catch (MessageException ex) {
                    jdReRe.showMessage(ex.getMessage(), null, JOptionPane.WARNING_MESSAGE);
                }
            }
            buscador.dispose();
            setDatosRecibo(rereSelected);
            jdReRe.setVisible(true);
        }
    }

    /**
     * Setea la ventana de JDReRe de forma q solo se puedan ver los datos y
     * detalles de la Recibo, imprimir y ANULAR, pero NO MODIFICAR
     *
     * @param recibo
     */
    private void setDatosRecibo(Recibo recibo) {
        bloquearVentana(true);
        jdReRe.setTfCuarto(UTIL.AGREGAR_CEROS(recibo.getSucursal().getPuntoVenta(), 4));
        jdReRe.setTfOcteto(UTIL.AGREGAR_CEROS(recibo.getNumero(), 8));
        //por no redundar en DATOOOOOOOOOSS...!!!
        Cliente cliente = new FacturaVentaController().findFacturaVenta(recibo.getDetalleReciboList().get(0).getFacturaVenta().getId()).getCliente();

        jdReRe.setDcFechaReRe(recibo.getFechaRecibo());
        jdReRe.setDcFechaCarga(recibo.getFechaCarga());
        //Uso los toString() para que compare String's..
        //por si el combo está vacio <VACIO> o no eligió ninguno
        //van a tirar error de ClassCastException
        UTIL.setSelectedItem(jdReRe.getCbSucursal(), recibo.getSucursal().getNombre());
        UTIL.setSelectedItem(jdReRe.getCbCaja(), recibo.getCaja().toString());
        UTIL.setSelectedItem(jdReRe.getCbClienteProveedor(), cliente.toString());
        cargarDetalleReRe(recibo);
        jdReRe.setTfImporte("");
        jdReRe.setTfPagado("");
        jdReRe.setTfSaldo("");
        updateTotales();
    }

    private void cargarDetalleReRe(Recibo recibo) {
        List<DetalleRecibo> detalleReciboList = recibo.getDetalleReciboList();
        DefaultTableModel dtm = jdReRe.getDtmAPagar();
        dtm.setRowCount(0);
        for (DetalleRecibo r : detalleReciboList) {
            dtm.addRow(new Object[]{
                        null, //no hace falta cargar facturaID
                        JGestionUtils.getNumeracion(r.getFacturaVenta()),
                        null,
                        r.getMontoEntrega(),
                        null //also needless
                    });
        }
        dtm = jdReRe.getDtmPagos();
        dtm.setRowCount(0);
        for (ReciboPagos reciboPagos : recibo.getPagos()) {
            if (reciboPagos.getFormaPago() == 1) {
                ChequePropio o = (ChequePropio) DAO.findEntity(ChequePropio.class, reciboPagos.getComprobanteId());
                dtm.addRow(new Object[]{o, "CHP", o.getBanco().getNombre() + " " + o.getNumero(), o.getImporte()});
            } else if (reciboPagos.getFormaPago() == 2) {
                ChequeTerceros o = (ChequeTerceros) DAO.findEntity(ChequeTerceros.class, reciboPagos.getComprobanteId());
                dtm.addRow(new Object[]{o, "CH", o.getBanco().getNombre() + " " + o.getNumero(), o.getImporte()});
            } else if (reciboPagos.getFormaPago() == 3) {
                NotaCredito o = (NotaCredito) DAO.findEntity(NotaCredito.class, reciboPagos.getComprobanteId());
                dtm.addRow(new Object[]{o, "NC", JGestionUtils.getNumeracion(o, true), o.getImporte()});
            } else if (reciboPagos.getFormaPago() == 4) {
                ComprobanteRetencion o = (ComprobanteRetencion) DAO.findEntity(ComprobanteRetencion.class, reciboPagos.getComprobanteId());
                dtm.addRow(new Object[]{o, "RE", o.getNumero(), o.getImporte()});
            }
        }
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

    private void bloquearVentana(boolean habilitar) {
        jdReRe.getbAnular().setEnabled(habilitar);
//      contenedor.getbImprimir().setEnabled(habilitar);
        // !habilitar
        jdReRe.getBtnADD().setEnabled(!habilitar);
        jdReRe.getBtnDEL().setEnabled(!habilitar);
        jdReRe.getBtnAddPago().setEnabled(!habilitar);
        jdReRe.getBtnDelPago().setEnabled(!habilitar);
        jdReRe.getbAceptar().setEnabled(!habilitar);
        jdReRe.getCbCtaCtes().setEnabled(!habilitar);
        jdReRe.getCbCaja().setEnabled(!habilitar);
        jdReRe.getCbSucursal().setEnabled(!habilitar);
        jdReRe.getCbClienteProveedor().setEnabled(!habilitar);
        jdReRe.getDcFechaReRe(!habilitar);
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

    /**
     * La anulación de una Recibo, resta a
     * <code>CtaCteCliente.entregado</code> los pagos/entregas
     * (parciales/totales) realizados de cada DetalleRecibo y cambia
     * <code>Recibo.estado = false<code>
     *
     * @param recibo
     * @throws MessageException
     * @throws Exception si Recibo es null, o si ya está anulado
     */
    public void anular(Recibo recibo) throws MessageException, Exception {
        if (recibo == null) {
            throw new MessageException(CLASS_NAME + " no válido");
        }
        if (!recibo.getEstado()) {
            throw new MessageException("Este " + CLASS_NAME + " ya está anulado");
        }

        EntityManager em = DAO.getEntityManager();
        List<DetalleRecibo> detalleReciboList = recibo.getDetalleReciboList();
        CtacteCliente ctaCteCliente;
        try {
            em.getTransaction().begin();
            for (DetalleRecibo dr : detalleReciboList) {
                //se resta la entrega ($) que implicaba este detalle con respecto a CADA factura
                ctaCteCliente = new CtacteClienteJpaController().findCtacteClienteByFactura(dr.getFacturaVenta().getId());
                ctaCteCliente.setEntregado(ctaCteCliente.getEntregado() - dr.getMontoEntrega().doubleValue());
                // y si había sido pagada en su totalidad..
                if (ctaCteCliente.getEstado() == Valores.CtaCteEstado.PAGADA.getId()) {
                    ctaCteCliente.setEstado(Valores.CtaCteEstado.PENDIENTE.getId());
                }
                em.merge(ctaCteCliente);
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
        recibo.setEstado(false);
        DAO.doMerge(recibo);
        new CajaMovimientosJpaController().anular(recibo);
    }

    private void imprimirRecibo(Recibo recibo) throws Exception {
        if (recibo == null && recibo.getId() == null) {
            throw new MessageException("No hay " + CLASS_NAME + " seleccionado");
        }

        Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_Recibo_ctacte.jasper", "Recibo");
        r.addParameter("RECIBO_N", recibo.getId());
        r.addCurrent_User();
        r.printReport(true);
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

    public void unlockedABM(JFrame owner) throws MessageException {
        UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.VENTA_NUMERACION_MANUAL);
        unlockedNumeracion = true;
        initRecibos(owner, true, false);
        jdReRe.setTfOctetoEditable(true);
        jdReRe.setVisible(true);
    }
}
