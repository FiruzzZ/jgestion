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
import jgestion.JGestionUtils;
import jgestion.Main;
import jpa.controller.CajaMovimientosJpaController;
import jpa.controller.ReciboJpaController;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import utilities.general.UTIL;
import utilities.swing.components.ComboBoxWrapper;

/**
 *
 * @author FiruzzZ
 */
public class ReciboController implements ActionListener, FocusListener {

    private static final String CLASS_NAME = Recibo.class.getSimpleName();
    private static final String[] COLUMN_NAMES = {"facturaID", "Factura", "Observación", "Entrega", "Acredidato"};
    private static final int[] COLUMN_WIDTH = {1, 50, 150, 30, 10};
    private static final Class[] COLUMN_CLASS = {Object.class, Object.class, String.class, Double.class, Boolean.class};
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

    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }

    /**
     * Crea la ventana para realizar Recibo's
     *
     * @param frame owner/parent
     * @param modal debería ser
     * <code>true</code> siempre, no está implementado para false
     * @param setVisible
     * @throws MessageException
     */
    public void initRecibos(JFrame frame, boolean modal, boolean setVisible) throws MessageException {
        UsuarioJpaController.checkPermiso(PermisosJpaController.PermisoDe.VENTA);
        UsuarioHelper uh = new UsuarioHelper();
        if (uh.getSucursales().isEmpty()) {
            throw new MessageException(Main.resourceBundle.getString("unassigned.sucursal"));
        }
        if (uh.getCajas(true).isEmpty()) {
            throw new MessageException(Main.resourceBundle.getString("unassigned.caja"));
        }
        jdReRe = new JDReRe(frame, modal);
        jdReRe.setUIForRecibos();
        UTIL.getDefaultTableModel(jdReRe.getjTable1(), COLUMN_NAMES, COLUMN_WIDTH, COLUMN_CLASS);
        UTIL.hideColumnsTable(jdReRe.getjTable1(), new int[]{0, 4});
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
                    Recibo recibo = setEntity();
                    persist(recibo, jdReRe.getCheckPagoConCheque().isSelected());
                    rereSelected = recibo;
                    jdReRe.showMessage(CLASS_NAME + " creado..", CLASS_NAME, 1);
                    limpiarDetalle();
                    resetPanel();

                } catch (MessageException ex) {
                    jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    Logger.getLogger(ReciboController.class).log(Level.ERROR, null, ex);
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
                        Recibo recibo = setEntity();
                        persist(recibo, jdReRe.getCheckPagoConCheque().isSelected());
                        rereSelected = recibo;
                        imprimirRecibo(rereSelected);
                        limpiarDetalle();
                        resetPanel();
                    }
                } catch (MessageException ex) {
                    jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    Logger.getLogger(ReciboController.class).log(org.apache.log4j.Level.ERROR, null, ex);
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
                    jdReRe.getTfCreditoDebito().setText(UTIL.PRECIO_CON_PUNTO.format(credito));
                    jdReRe.getTfRestanteCreditoDebito().setText(UTIL.PRECIO_CON_PUNTO.format(credito));
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
                setNextNumeroReRe();
            }
        });
        jdReRe.getbCancelar().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                resetPanel();
                limpiarDetalle();
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
        jdReRe.setListener(this);
        jdReRe.setLocationRelativeTo(frame);
        jdReRe.setVisible(setVisible);
    }

    private void displayDetalleCredito(Cliente cliente) {
        JTable tabla = UTIL.getDefaultTableModel(null,
                new String[]{"Nº Nota crédito", "Fecha", "Importe", "Desacreditado", "Total Acum."},
                new int[]{50, 50, 50, 50, 100},
                new Class<?>[]{null, null, Double.class, Double.class, Double.class});
        List<NotaCredito> lista = new NotaCreditoController().findNotaCreditoFrom(cliente, false);
        DefaultTableModel dtm = (DefaultTableModel) tabla.getModel();
        double acumulativo = 0.0;
        for (NotaCredito notaCredito : lista) {
            acumulativo += (notaCredito.getImporte() - notaCredito.getDesacreditado());
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
        if (jdReRe.getDtm().getRowCount() < 1) {
            throw new MessageException("No ha hecho ninguna entrega");
        }

        if (jdReRe.getDcFechaReRe() == null) {
            throw new MessageException("Fecha de " + CLASS_NAME + " no válida");
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

    Sucursal getSelectedSucursalFromJD() {
        @SuppressWarnings("unchecked")
        ComboBoxWrapper<Sucursal> cbw = (ComboBoxWrapper<Sucursal>) jdReRe.getCbSucursal().getSelectedItem();
        return cbw.getEntity();
    }

    private Recibo setEntity() throws Exception {
        Recibo recibo = new Recibo();
        recibo.setCaja((Caja) jdReRe.getCbCaja().getSelectedItem());
        recibo.setSucursal(getSelectedSucursalFromJD());
        if (unlockedNumeracion) {
            recibo.setNumero(Integer.valueOf(jdReRe.getTfOcteto()));
        } else {
            recibo.setNumero(jpaController.getNextNumero(recibo.getSucursal()));
        }
        recibo.setUsuario(UsuarioJpaController.getCurrentUser());
        recibo.setEstado(true);
        recibo.setFechaRecibo(jdReRe.getDcFechaReRe());
        // 30% faster on ArrayList with initialCapacity :O
        recibo.setDetalleReciboList(new ArrayList<DetalleRecibo>(jdReRe.getDtm().getRowCount()));
        DefaultTableModel dtm = jdReRe.getDtm();
        FacturaVentaController fcc = new FacturaVentaController();
        DetalleRecibo detalle;
        String observacion;
        for (int i = 0; i < dtm.getRowCount(); i++) {
            detalle = new DetalleRecibo();
            detalle.setFacturaVenta(fcc.findFacturaVenta(Integer.valueOf(dtm.getValueAt(i, 0).toString())));
            observacion = dtm.getValueAt(i, 2) != null ? dtm.getValueAt(i, 2).toString() : null;
            detalle.setObservacion(observacion);
            detalle.setMontoEntrega(Double.parseDouble(dtm.getValueAt(i, 3).toString()));
            detalle.setAcreditado((Boolean) dtm.getValueAt(i, 4));
            detalle.setRecibo(recibo);
            recibo.getDetalleReciboList().add(detalle);
        }
        recibo.setMonto(Double.parseDouble(jdReRe.getTfTotalPagado()));
        return recibo;
    }

    private void persist(Recibo recibo, boolean payWithCheque)
            throws MessageException, PreexistingEntityException, NonexistentEntityException, Exception {
        ChequeTerceros cheque = null;
        BigDecimal montoParaDesacreditar = new BigDecimal(0);
        if (payWithCheque) {
            cheque = getChequeToBind(recibo);
        }
        jpaController.create(recibo);
        if (payWithCheque) { //cheque != null
            cheque.setBoundId(recibo.getId().longValue());
            new ChequeTercerosJpaController().edit(cheque);

            //se modifica la observación de los items que hayan sido pagados por
            //un cheque
            for (DetalleRecibo detalleRecibo : recibo.getDetalleReciboList()) {
                if (!detalleRecibo.isAcreditado()) {
                    detalleRecibo.setObservacion("Cheque N°" + cheque.getNumero());
                }
            }
            jpaController.merge(recibo);
        }
        Iterator<DetalleRecibo> iterator = recibo.getDetalleReciboList().iterator();
        while (iterator.hasNext()) {
            DetalleRecibo detalle = iterator.next();
            //actuliza saldo pagado de cada ctacte
            actualizarMontoEntrega(detalle.getFacturaVenta(), detalle.getMontoEntrega());
            //aprovechando el bucle, sumamos el importe acreditado
            if (detalle.isAcreditado()) {
                montoParaDesacreditar = montoParaDesacreditar.add(new BigDecimal(detalle.getMontoEntrega()));
            }
        }
        if (!payWithCheque) {
            //registrando pago en CAJA
            new CajaMovimientosJpaController().asentarMovimiento(recibo);
        }
        if (montoParaDesacreditar.doubleValue() > 0) {
            new NotaCreditoController().desacreditar(
                    recibo,
                    (Cliente) jdReRe.getCbClienteProveedor().getSelectedItem(),
                    montoParaDesacreditar.doubleValue());
        }
    }

    private void actualizarMontoEntrega(FacturaVenta factu, double monto) {
        CtacteCliente ctacte = new CtacteClienteJpaController().findCtacteClienteByFactura(factu.getId());
        Logger.getLogger(ReciboController.class).debug("updatingMontoEntrega: CtaCte:" + ctacte.getId() + " -> Importe: " + ctacte.getImporte() + " Entregado:" + ctacte.getEntregado() + " + " + monto);

        ctacte.setEntregado(ctacte.getEntregado() + monto);
        if (ctacte.getImporte() == ctacte.getEntregado()) {
            ctacte.setEstado(Valores.CtaCteEstado.PAGADA.getId());
            Logger.getLogger(ReciboController.class).debug("CtaCte Nº:" + ctacte.getId() + " PAGADA");
        }
        DAO.doMerge(ctacte);
    }

    private void limpiarDetalle() {
        UTIL.limpiarDtm(jdReRe.getDtm());
        jdReRe.setTfImporte("0");
        jdReRe.setTfEntrega("");
        jdReRe.setTfObservacion("");
        jdReRe.setTfSaldo("0");
        jdReRe.setTfTotalPagado("0");
        selectedFechaReRe = null;
    }

    private void addEntregaToDetalle() throws MessageException {
        if (jdReRe.getDcFechaReRe() == null) {
            throw new MessageException("Debe especificar una fecha de " + CLASS_NAME + " antes de empezar a cargar Facturas.");
        }
        if (selectedCtaCte == null) {
            throw new MessageException("No hay Factura seleccionada.");
        }
        //hay que quitar las HH:MM:ss:mmmm de las fechas para hacer las comparaciones
        if (UTIL.getDateYYYYMMDD(jdReRe.getDcFechaReRe()).before(UTIL.getDateYYYYMMDD(selectedCtaCte.getFactura().getFechaVenta()))) {
            throw new MessageException("La fecha de " + CLASS_NAME + " no puede ser anterior"
                    + "\n a la fecha de Facturación ("
                    + UTIL.DATE_FORMAT.format(selectedCtaCte.getFactura().getFechaVenta()) + ")");
        }

        // si hay cargado al menos un detalle de entrega
        // ctrla que la fecha de ReRe siga siendo la misma
        if ((selectedFechaReRe != null) && (jdReRe.getDtm().getRowCount() > 0)
                && (!UTIL.DATE_FORMAT.format(selectedFechaReRe).equals(UTIL.DATE_FORMAT.format(jdReRe.getDcFechaReRe())))) {
            throw new MessageException("La fecha de " + CLASS_NAME + " a sido cambiada"
                    + "\nAnterior: " + UTIL.DATE_FORMAT.format(selectedFechaReRe)
                    + "\nActual: " + UTIL.DATE_FORMAT.format(jdReRe.getDcFechaReRe()));
        } else {
            selectedFechaReRe = jdReRe.getDcFechaReRe();
        }
        double entrega;
        String observacion = jdReRe.getTfObservacion().length() > 0 ? jdReRe.getTfObservacion() : null;
        try {
            entrega = Double.parseDouble(jdReRe.getTfEntrega());
        } catch (NumberFormatException ex) {
            throw new MessageException("Monto de entrega no válido");
        }
        if (entrega <= 0) {
            throw new MessageException("Monto de entrega no válido (Debe ser mayor a 0)");
        }
        if (entrega > (selectedCtaCte.getImporte() - selectedCtaCte.getEntregado())) {
            throw new MessageException("Monto de entrega no puede ser mayor al Saldo restante");
        }
        if (observacion != null && observacion.length() > 200) {
            throw new MessageException("La Observación no puede superar los 200 caracteres (no es una novela)");
        }
        FacturaVenta facturaToAddToDetail = selectedCtaCte.getFactura();
        boolean acreditado = jdReRe.getCheckAcreditarEntrega().isSelected();

        //check que no se inserte una entrega (acreditada o no) de la misma factura
        double entregaParcial = 0;
        for (int i = 0; i < jdReRe.getDtm().getRowCount(); i++) {
            if (facturaToAddToDetail.getId() == (Integer) jdReRe.getDtm().getValueAt(i, 0)) {
                entregaParcial += (Double) jdReRe.getDtm().getValueAt(i, 3);
                if (acreditado == (Boolean) jdReRe.getDtm().getValueAt(i, 4)) {
                    throw new MessageException("El detalle ya contiene una entrega "
                            + (acreditado ? " (ACREDITADA)" : "") + " de esta factura.");
                }
            }
        }
        if ((entrega + entregaParcial) > (selectedCtaCte.getImporte() - selectedCtaCte.getEntregado())) {
            throw new MessageException("El monto de esta entrega $" + entrega + " + $" + entregaParcial
                    + "\nsuperan la deuda de la Factura $" + (selectedCtaCte.getImporte() - selectedCtaCte.getEntregado()));
        }
        double restante;
        if (acreditado) {
            restante = Double.parseDouble(jdReRe.getTfRestanteCreditoDebito().getText());
            if (restante < entrega) {
                throw new MessageException("El crédito no es suficiente para cubrir esta entrega");
            }
        }

        jdReRe.getDtm().addRow(new Object[]{
                    facturaToAddToDetail.getId(),
                    //se concat al final ** (doble asterisco) cuando es acreditada la entrega
                    JGestionUtils.getNumeracion(facturaToAddToDetail) + (acreditado ? "**" : ""),
                    observacion,
                    entrega,
                    acreditado
                });
        double total;
        if (acreditado) {
            //actualiza total acreditado
            total = Double.valueOf(jdReRe.getTfPorCreditoDebito().getText());
            jdReRe.getTfPorCreditoDebito().setText(UTIL.PRECIO_CON_PUNTO.format(total + entrega));

            restante = Double.parseDouble(jdReRe.getTfCreditoDebito().getText()) - (total + entrega);
            jdReRe.getTfRestanteCreditoDebito().setText(UTIL.PRECIO_CON_PUNTO.format(restante));
        } else {
            //actualiza total "efectivo"
            total = Double.valueOf(jdReRe.getTfTotalPagado());
            jdReRe.setTfTotalPagado(UTIL.PRECIO_CON_PUNTO.format(total + entrega));
        }
        updateTotalReRe();
    }

    /**
     * Borra la fila seleccionada, del DetalleRecibo
     */
    private void delEntragaFromDetalle() {
        int selectedRow = jdReRe.getjTable1().getSelectedRow();
        if (selectedRow > -1) {
            double entrega = (Double) jdReRe.getDtm().getValueAt(selectedRow, 3);
            double entregado;
            //si es acreditado o no
            if ((Boolean) jdReRe.getDtm().getValueAt(selectedRow, 4)) {
                entregado = Double.parseDouble(jdReRe.getTfRestanteCreditoDebito().getText());
                jdReRe.getTfRestanteCreditoDebito().setText(UTIL.PRECIO_CON_PUNTO.format(entregado + entrega));

                //reutilizando variable.. no te asustes!
                entregado = Double.valueOf(jdReRe.getTfPorCreditoDebito().getText());
                jdReRe.getTfPorCreditoDebito().setText(UTIL.PRECIO_CON_PUNTO.format(entregado - entrega));
            } else {
                entregado = Double.valueOf(jdReRe.getTfTotalPagado());
                jdReRe.setTfTotalPagado(UTIL.PRECIO_CON_PUNTO.format(entregado - entrega));
            }
            jdReRe.getDtm().removeRow(selectedRow);
            updateTotalReRe();
        }
    }

    public void initBuscador(JFrame frame, boolean modal) {
        // <editor-fold defaultstate="collapsed" desc="checking Permiso">
        try {
            UsuarioJpaController.checkPermiso(PermisosJpaController.PermisoDe.VENTA);
        } catch (MessageException ex) {
            javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage());
            return;
        }// </editor-fold>
        buscador = new JDBuscadorReRe(frame, "Buscador - " + CLASS_NAME, modal, "Cliente", "Nº " + CLASS_NAME);
        initBuscador();
    }

    private void initBuscador() {
        buscador.setParaRecibos();
        UTIL.loadComboBox(buscador.getCbClieProv(), new ClienteController().findEntities(), true);
        UTIL.loadComboBox(buscador.getCbCaja(), new UsuarioHelper().getCajas(Boolean.TRUE), true);
        UTIL.loadComboBox(buscador.getCbSucursal(), new UsuarioHelper().getWrappedSucursales(), true);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"Instance", "Nº Recibo", "Monto", "Fecha", "Caja", "Usuario", "Fecha/Hora (Sist)"},
                new int[]{1, 80, 30, 40, 50, 50, 70},
                new Class<?>[]{null, null, String.class, String.class, null, null, null});
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
        List<ComboBoxWrapper<CtacteCliente>> wrappedList = new ArrayList<ComboBoxWrapper<CtacteCliente>>(ctacteClientePendientesList.size());
        for (CtacteCliente ctacteCliente : ctacteClientePendientesList) {
            FacturaVenta factura = ctacteCliente.getFactura();
            wrappedList.add(new ComboBoxWrapper<CtacteCliente>(ctacteCliente, ctacteCliente.getId(), JGestionUtils.getNumeracion(factura)));
        }
        UTIL.loadComboBox(jdReRe.getCbCtaCtes(), wrappedList, false);
    }

    /**
     * Resetea la ventana; - pone la fecha actual - clienteProveedor.index(0) -
     * setea el NextNumeroReRe - rereSelected = null;
     */
    private void resetPanel() {
        jdReRe.setDcFechaReRe(new Date());
        jdReRe.getCbClienteProveedor().setSelectedIndex(0);
        setNextNumeroReRe();
        jdReRe.getTfTOTAL().setText("0.00");
        jdReRe.getCheckPagoConCheque().setSelected(false);
        jdReRe.getCheckAcreditarEntrega().setSelected(false);
        rereSelected = null;
    }

    @SuppressWarnings("unchecked")
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
            query.append(" AND p.id = ").append(((Cliente) buscador.getCbClieProv().getSelectedItem()).getId());
        }

        query.append(" GROUP BY o.id, o.numero, o.fecha_carga, o.monto, o.usuario, o.caja, o.sucursal, o.fecha_recibo, o.estado"
                + " ORDER BY o.sucursal, o.numero");
        cargarBuscador(query.toString());
    }

    private void cargarBuscador(String query) {
        buscador.dtmRemoveAll();
        DefaultTableModel dtm = buscador.getDtm();
        List<Recibo> l = jpaController.findByNativeQuery(query);
        for (Recibo o : l) {
            System.out.println(o);
            dtm.addRow(new Object[]{
                        o.getId(),
                        JGestionUtils.getNumeracion(o, true),
                        UTIL.PRECIO_CON_PUNTO.format(o.getMonto()),
                        UTIL.DATE_FORMAT.format(o.getFechaRecibo()),
                        o.getCaja().getNombre(),
                        o.getUsuario().getNick(),
                        UTIL.TIMESTAMP_FORMAT.format(o.getFechaCarga())
                    });
        }
    }

    private void setSelectedRecibo() {
        int rowIndex = buscador.getjTable1().getSelectedRow();
        int id = Integer.valueOf(buscador.getjTable1().getValueAt(rowIndex, 0).toString());
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
        cargarDetalleReRe(recibo.getDetalleReciboList());
        jdReRe.setTfImporte("");
        jdReRe.setTfPagado("");
        jdReRe.setTfSaldo("");
        jdReRe.setTfTotalPagado(String.valueOf(recibo.getMonto()));
    }

    private void cargarDetalleReRe(List<DetalleRecibo> detalleReciboList) {
        DefaultTableModel dtm = jdReRe.getDtm();
        UTIL.limpiarDtm(dtm);
        for (DetalleRecibo r : detalleReciboList) {
            dtm.addRow(new Object[]{
                        null, //no hace falta cargar facturaID
                        UTIL.AGREGAR_CEROS(String.valueOf(r.getFacturaVenta().getNumero()), 12) + (r.isAcreditado() ? "**" : ""),
                        r.getObservacion(),
                        r.getMontoEntrega(),
                        null //also needless
                    });
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
        jdReRe.getbAceptar().setEnabled(!habilitar);
        jdReRe.getCbCtaCtes().setEnabled(!habilitar);
        jdReRe.getCbCaja().setEnabled(!habilitar);
        jdReRe.getCbSucursal().setEnabled(!habilitar);
        jdReRe.getCbClienteProveedor().setEnabled(!habilitar);
        jdReRe.getDcFechaReRe(!habilitar);
    }

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
        EntityManager em = getEntityManager();
        if (recibo == null) {
            throw new MessageException(CLASS_NAME + " no válido");
        }
        if (!recibo.getEstado()) {
            throw new MessageException("Este " + CLASS_NAME + " ya está anulado");
        }

        List<DetalleRecibo> detalleReciboList = recibo.getDetalleReciboList();
        CtacteCliente ctaCteCliente;
        try {
            em.getTransaction().begin();
            for (DetalleRecibo dr : detalleReciboList) {
                //se resta la entrega ($) que implicaba este detalle con respecto a CADA factura
                ctaCteCliente = new CtacteClienteJpaController().findCtacteClienteByFactura(dr.getFacturaVenta().getId());
                ctaCteCliente.setEntregado(ctaCteCliente.getEntregado() - dr.getMontoEntrega());
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
     * Retorna todos los {@link Recibo} que contengan en su {@link DetalleRecibo}
     * a factura.
     *
     * @param factura que deben contenedor los Recibos en su detalle
     * @return una lista de Recibo's
     */
    public List<Recibo> findRecibosByFactura(FacturaVenta factura) {
        List<DetalleRecibo> detalleReciboList = jpaController.findDetalleReciboEntitiesByFactura(factura);
        List recibosList = new ArrayList(detalleReciboList.size());
        for (DetalleRecibo detalleRecibo : detalleReciboList) {
            if (!recibosList.contains(detalleRecibo.getRecibo())) {
                recibosList.add(detalleRecibo.getRecibo());
            }
        }
        return recibosList;
    }

    private void updateTotalReRe() {
        jdReRe.getTfTOTAL().setText(UTIL.PRECIO_CON_PUNTO.format(
                Double.valueOf(jdReRe.getTfPorCreditoDebito().getText())
                + Double.valueOf(jdReRe.getTfTotalPagado())));
    }

    private ChequeTerceros getChequeToBind(Recibo recibo) throws MessageException {
        return new ChequeTercerosJpaController().getABMCheque(recibo);
    }

    public void unlockedABM(JFrame owner) throws MessageException {
        UsuarioJpaController.checkPermiso(PermisosJpaController.PermisoDe.VENTA_NUMERACION_MANUAL);
        unlockedNumeracion = true;
        initRecibos(owner, true, false);
        jdReRe.setTfOctetoEditable(true);
        jdReRe.setVisible(true);
    }
}
