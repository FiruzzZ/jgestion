package controller;

import controller.exceptions.*;
import entity.Caja;
import entity.ChequePropio;
import entity.ChequeTerceros;
import entity.ComprobanteRetencion;
import entity.CreditoProveedor;
import entity.CtacteProveedor;
import entity.CuentabancariaMovimientos;
import entity.DetalleCajaMovimientos;
import entity.DetalleRemesa;
import entity.Especie;
import entity.FacturaCompra;
import entity.NotaCreditoProveedor;
import entity.NotaDebitoProveedor;
import entity.Proveedor;
import entity.Remesa;
import entity.RemesaPagos;
import entity.Sucursal;
import gui.JDBuscadorReRe;
import gui.JDReRe;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import jgestion.JGestionUtils;
import jpa.controller.ChequePropioJpaController;
import jpa.controller.ChequeTercerosJpaController;
import jpa.controller.ComprobanteRetencionJpaController;
import jpa.controller.CreditoProveedorJpaController;
import jpa.controller.CuentabancariaMovimientosJpaController;
import jpa.controller.EspecieJpaController;
import jpa.controller.FacturaCompraJpaController;
import jpa.controller.NotaCreditoProveedorJpaController;
import jpa.controller.NotaDebitoProveedorJpaController;
import jpa.controller.ProveedorJpaController;
import jpa.controller.RemesaJpaController;
import org.apache.log4j.Logger;
import utilities.general.UTIL;
import utilities.gui.SwingUtil;
import utilities.swing.components.ComboBoxWrapper;
import utilities.swing.components.NumberRenderer;

/**
 *
 * @author Administrador
 */
public class RemesaController implements FocusListener {

    private final String CLASS_NAME = Remesa.class.getSimpleName();
    private static final String[] COLUMN_NAMES = {"facturaID", "Factura", "Observación", "Entrega"};
    private static final int[] COLUMN_WIDTH = {1, 50, 100, 50};
    private JDReRe jdReRe;
    private CtacteProveedor selectedCtaCte;
    private NotaDebitoProveedor selectedNotaDebito;
    private JDBuscadorReRe buscador;
    private Remesa selectedRemesa;
    private static final Logger LOG = Logger.getLogger(RemesaController.class);
    private final RemesaJpaController jpaController = new RemesaJpaController();
    private boolean unlockedNumeracion = false;
    private boolean toConciliar;
    private boolean conciliando;

    public RemesaController() {
    }

    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }

    public void initRemesaAConciliar(Window owner) throws MessageException {
        displayABMRemesa(owner, true, false);
        jdReRe.setTitle("Recibo a conciliar");
        toConciliar = true;
        conciliando = false;
        jdReRe.getbImprimir().setEnabled(false);
        SwingUtil.setComponentsEnabled(jdReRe.getPanelAPagar().getComponents(), false, true, (Class<? extends Component>[]) null);
        jdReRe.setVisible(true);
    }

    public void displayABMRemesa(Window owner, boolean modal, boolean visible) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.COMPRA);
        jdReRe = new JDReRe(owner, modal);
        jdReRe.setUIForRemesas();
        UTIL.getDefaultTableModel(jdReRe.getTableAPagar(), COLUMN_NAMES, COLUMN_WIDTH);
        jdReRe.getTableAPagar().getColumnModel().getColumn(3).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        UTIL.hideColumnTable(jdReRe.getTableAPagar(), 0);
        UTIL.loadComboBox(jdReRe.getCbSucursal(), new UsuarioHelper().getWrappedSucursales(), false);
        UTIL.loadComboBox(jdReRe.getCbCaja(), new UsuarioHelper().getCajas(true), false);
        UTIL.loadComboBox(jdReRe.getCbClienteProveedor(), JGestionUtils.getWrappedProveedores(new ProveedorJpaController().findAll()), true);
        UTIL.loadComboBox(jdReRe.getCbCtaCtes(), null, false);
        jdReRe.getbImprimir().setVisible(false);
        jdReRe.getbAnular().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    anular(selectedRemesa);
                    jdReRe.showMessage(CLASS_NAME + " anulada ..", CLASS_NAME, 1);
                    armarQuery();
                } catch (MessageException ex) {
                    ex.displayMessage(jdReRe);
                } catch (Exception ex) {
                    jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    LOG.error("Anulando Remesa.id=" + selectedRemesa.getId(), ex);
                }
            }
        });
        jdReRe.getbAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Remesa re = setAndPersist();
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
                    cargarFacturasCtaCtesYNotasDebito(((ComboBoxWrapper<Proveedor>) jdReRe.getCbClienteProveedor().getSelectedItem()).getEntity());
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
                    @SuppressWarnings("unchecked")
                    ComboBoxWrapper<?> cbw = (ComboBoxWrapper<?>) jdReRe.getCbCtaCtes().getSelectedItem();
                    if (cbw.getEntity() instanceof CtacteProveedor) {
                        selectedCtaCte = (CtacteProveedor) cbw.getEntity();
                        selectedNotaDebito = null;
                        jdReRe.setTfImporte(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getImporte()));
                        jdReRe.setTfPagado(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getEntregado()));
                        jdReRe.setTfSaldo(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getImporte().subtract(selectedCtaCte.getEntregado())));
                        jdReRe.setTfEntrega(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getImporte().subtract(selectedCtaCte.getEntregado())));
                        jdReRe.getTfEntrega().setEditable(true);
                    } else {
                        selectedNotaDebito = (NotaDebitoProveedor) cbw.getEntity();
                        selectedCtaCte = null;
                        jdReRe.setTfImporte(UTIL.PRECIO_CON_PUNTO.format(selectedNotaDebito.getImporte()));
                        jdReRe.setTfPagado(null);
                        jdReRe.setTfSaldo(null);
                        jdReRe.setTfEntrega(UTIL.PRECIO_CON_PUNTO.format(selectedNotaDebito.getImporte()));
                        jdReRe.getTfEntrega().setEditable(false);
                    }
                } catch (NullPointerException ex) {
                    selectedCtaCte = null;
                    selectedNotaDebito = null;
                } catch (ClassCastException ex) {
                    selectedCtaCte = null;
                    selectedNotaDebito = null;
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
                } else if (formaPago == 6) {
                    displayABMEspecie(null);
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

    private void displayABMEfectivo() {
        BigDecimal monto = jdReRe.displayABMEfectivo(null);
        if (monto != null) {
            DetalleCajaMovimientos d = new DetalleCajaMovimientos();
            d.setIngreso(false);
            d.setMonto(monto.negate());
            d.setTipo(DetalleCajaMovimientosController.REMESA);
            d.setUsuario(UsuarioController.getCurrentUser());
            d.setCuenta(CuentaController.SIN_CLASIFICAR);
            d.setDescripcion(null); // <--- setear con el N° del comprobante
            d.setCajaMovimientos(null); // no te olvides este tampoco!! 
            d.setNumero(0l); // Comprobante.id!!!!
            DefaultTableModel dtm = jdReRe.getDtmPagos();
            dtm.addRow(new Object[]{d, "EF", null, monto});
        }
    }

    private void displayABMChequePropio() {
        try {
            ChequePropio cheque = new ChequePropioController().initABM(jdReRe, false, ((ComboBoxWrapper<Proveedor>) jdReRe.getCbClienteProveedor().getSelectedItem()).getEntity());
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
            NotaCreditoProveedor notaCredito = new NotaCreditoProveedorController().
                    initBuscador(jdReRe, false,((ComboBoxWrapper<Proveedor>) jdReRe.getCbClienteProveedor().getSelectedItem()).getEntity(), true);
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

    private void displayABMEspecie(Especie toEdit) {
        Especie especie = new EspecieController().displayEspecie(jdReRe, toEdit);
        if (especie != null) {
            DefaultTableModel dtm = jdReRe.getDtmPagos();
            try {
                for (int row = 0; row < dtm.getRowCount(); row++) {
                    if (dtm.getValueAt(row, 0) instanceof Especie) {
                        Especie old = (Especie) dtm.getValueAt(row, 0);
                        if (especie.getDescripcion().equalsIgnoreCase(old.getDescripcion())) {
                            throw new MessageException("Ya existe un pago en Especie en este Recibo con la misma descripción: " + old.getDescripcion());
                        }
                    }
                }
                if (toEdit == null) {
                    dtm.addRow(new Object[]{especie, "ES", especie.getDescripcion(), especie.getImporte()});
                } else {
                    int selectedRow = jdReRe.getTablePagos().getSelectedRow();
                    dtm.setValueAt(especie, selectedRow, 0);
                    dtm.setValueAt(especie.getDescripcion(), selectedRow, 2);
                    dtm.setValueAt(especie.getImporte(), selectedRow, 3);
                }
            } catch (MessageException ex) {
                ex.displayMessage(jdReRe);
            }
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

    private void checkConstraints() throws MessageException {
        if (!toConciliar) {
            if (jdReRe.getDtmAPagar().getRowCount() < 1) {
                throw new MessageException("No ha hecho ninguna entrega");
            }
        }
        if (jdReRe.getDtmPagos().getRowCount() < 1) {
            throw new MessageException("No ha ingresado ningún pago");
        }
        if (jdReRe.getDcFechaReRe().getDate() == null) {
            throw new MessageException("Fecha de " + jpaController.getEntityClass().getSimpleName() + " no válida");
        }
        Date fecha = jdReRe.getDcFechaReRe().getDate();
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
        TableModel dtm = jdReRe.getTableAPagar().getModel();
        for (int rowIndex = 0; rowIndex < dtm.getRowCount(); rowIndex++) {
            Object o = dtm.getValueAt(rowIndex, 0);
            if (o instanceof FacturaCompra) {
                FacturaCompra fv = new FacturaCompraJpaController().find(((FacturaCompra) o).getId());
                if (UTIL.compararIgnorandoTimeFields(fecha, fv.getFechaCompra()) < 0) {
                    throw new MessageException("La fecha de la factura es posterior a la " + JGestionUtils.getNumeracion(fv));
                }
            } else if (o instanceof NotaDebitoProveedor) {
                NotaDebitoProveedor nota = new NotaDebitoProveedorJpaController().find(((NotaDebitoProveedor) o).getId());
                if (UTIL.compararIgnorandoTimeFields(fecha, nota.getFechaNotaDebito()) < 0) {
                    throw new MessageException("La fecha de la Nota de Débito es posterior a la " + JGestionUtils.getNumeracion(nota));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Remesa setAndPersist() throws MessageException, Exception {
        checkConstraints();
        Remesa re = new Remesa();
        re.setCaja((Caja) jdReRe.getCbCaja().getSelectedItem());
        re.setSucursal(((ComboBoxWrapper<Sucursal>) jdReRe.getCbSucursal().getSelectedItem()).getEntity());
        re.setEstado(true);
        re.setFechaRemesa(jdReRe.getDcFechaReRe().getDate());
        re.setPagos(new ArrayList<RemesaPagos>(jdReRe.getDtmPagos().getRowCount()));
        re.setDetalle(new ArrayList<DetalleRemesa>(jdReRe.getDtmAPagar().getRowCount()));
        re.setPorConciliar(toConciliar);
        re.setProveedor(((ComboBoxWrapper<Proveedor>) jdReRe.getCbClienteProveedor().getSelectedItem()).getEntity());
        re.setUsuario(UsuarioController.getCurrentUser());

        DefaultTableModel dtm = (DefaultTableModel) jdReRe.getTableAPagar().getModel();
        BigDecimal monto = BigDecimal.ZERO;
        for (int rowIndex = 0; rowIndex < dtm.getRowCount(); rowIndex++) {
            Object o = dtm.getValueAt(rowIndex, 0);
            DetalleRemesa detalle = new DetalleRemesa();
            if (o instanceof FacturaCompra) {
                FacturaCompra fv = new FacturaCompraJpaController().find(((FacturaCompra) o).getId());
                detalle.setFacturaCompra(fv);
            } else if (o instanceof NotaDebitoProveedor) {
                NotaDebitoProveedor nota = new NotaDebitoProveedorJpaController().find(((NotaDebitoProveedor) o).getId());
                nota.setRemesa(re);
                detalle.setNotaDebitoProveedor(nota);
            } else {
                throw new IllegalArgumentException();
            }
            detalle.setMontoEntrega((BigDecimal) dtm.getValueAt(rowIndex, 3));
            detalle.setRemesa(re);
            re.getDetalle().add(detalle);
            monto = monto.add(detalle.getMontoEntrega());
        }
        re.setMontoEntrega(monto.doubleValue());
        dtm = jdReRe.getDtmPagos();
        List<Object> pagos = new ArrayList<>(dtm.getRowCount());
        BigDecimal importePagado = BigDecimal.ZERO;
        for (int row = 0; row < dtm.getRowCount(); row++) {
            importePagado = importePagado.add((BigDecimal) dtm.getValueAt(row, 3));
            pagos.add(dtm.getValueAt(row, 0));
        }
        re.setPagosEntities(pagos);
        boolean asentarDiferenciaEnCaja = false;
        if (!re.isPorConciliar() || conciliando) {
            if (0 != jdReRe.getTfTotalAPagar().getText().compareTo(jdReRe.getTfTotalPagado().getText())) {
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(jdReRe, "El importe a pagar no coincide con el detalle de pagos."
                        + "\n¿Confirma que la diferencia ($" + UTIL.DECIMAL_FORMAT.format(re.getMonto() - importePagado.doubleValue()) + ") sea acreditada?", "Confirmación de diferencia", JOptionPane.YES_NO_OPTION)) {
                    asentarDiferenciaEnCaja = true;
                } else {
                    throw new MessageException("Operación cancelada");
                }
            }
        }
        if (unlockedNumeracion || conciliando) {
            re.setNumero(Integer.valueOf(jdReRe.getTfOcteto()));
        } else {
            re.setNumero(jpaController.getNextNumero(re.getSucursal()));
        }
        if (conciliando) {
            jpaController.conciliar(re);
        } else {
            try {
                jpaController.create(re);
            } catch (Exception ex) {
                LOG.error("Error fantástico de doble persistencia de remesa: " + re, ex);
                jpaController.create(re);
            }
        }
        for (DetalleRemesa detalle : re.getDetalle()) {
            if (detalle.getFacturaCompra() != null) {
                actualizarMontoEntrega(detalle.getFacturaCompra(), detalle.getMontoEntrega());
            }
        }
        if (asentarDiferenciaEnCaja) {
            BigDecimal diferencia = BigDecimal.valueOf(re.getMonto() - importePagado.doubleValue());
            boolean debe = false;
            if (diferencia.doubleValue() < 0) {
                diferencia = diferencia.negate();
                debe = true;
            }
            DetalleRemesa d = re.getDetalle().get(0);
            Proveedor proveedor;
            if (d.getFacturaCompra() != null) {
                proveedor = d.getFacturaCompra().getProveedor();
            } else {
                proveedor = d.getNotaDebitoProveedor().getProveedor();
            }
            CreditoProveedor cp = new CreditoProveedor(null, debe, diferencia, "Remesa N° " + JGestionUtils.getNumeracion(re, true), proveedor);
            new CreditoProveedorJpaController().create(cp);
        }
        return re;
    }

    private void actualizarMontoEntrega(FacturaCompra factu, BigDecimal monto) {
        CtacteProveedor ctacte = new CtacteProveedorController().findCtacteProveedorByFactura(factu.getId());
        ctacte.setEntregado(ctacte.getEntregado().add(monto));
        if (ctacte.getImporte().compareTo(ctacte.getEntregado()) == 0) {
            ctacte.setEstado(Valores.CtaCteEstado.PAGADA.getId());
        }
        new CtacteProveedorController().edit(ctacte);
    }

    private void limpiarDetalle() {
        jdReRe.limpiarDetalles();
    }

    private void addEntregaToDetalle() throws MessageException {
        Date comprobanteFecha;
        if (jdReRe.getDcFechaReRe().getDate() == null) {
            throw new MessageException("Debe especificar una fecha de " + jpaController.getEntityClass().getSimpleName() + " antes de agregar comprobaste a pagar.");
        }
        if (selectedCtaCte == null && selectedNotaDebito == null) {
            throw new MessageException("No hay Factura o Nota de Débito seleccionada.");
        }
        if (selectedCtaCte != null) {
            comprobanteFecha = selectedCtaCte.getFactura().getFechaCompra();
        } else {
            comprobanteFecha = selectedNotaDebito.getFechaNotaDebito();
        }
        //hay que ignorar las HH:MM:ss:mmmm de las fechas para hacer las comparaciones
        if (UTIL.getDateYYYYMMDD(jdReRe.getDcFechaReRe().getDate()).before(UTIL.getDateYYYYMMDD(comprobanteFecha))) {
            throw new MessageException("La fecha de " + jpaController.getEntityClass().getSimpleName() + " no puede ser anterior"
                    + "\n a la fecha del comprobante ("
                    + UTIL.DATE_FORMAT.format(comprobanteFecha) + ")");
        }

        BigDecimal entrega$;
        try {
            entrega$ = new BigDecimal(jdReRe.getTfEntrega().getText());
            if (entrega$.doubleValue() <= 0) {
                throw new MessageException("Monto de entrega no válido (Debe ser mayor a 0)");
            }

        } catch (NumberFormatException e) {
            throw new MessageException("Monto de entrega no válido, ingrese solo números y utilice el punto como separador decimal.");
        }

        FacturaCompra facturaToAddToDetail = null;
        if (selectedCtaCte != null) {
            if (entrega$.compareTo(selectedCtaCte.getImporte().subtract(selectedCtaCte.getEntregado())) == 1) {
                throw new MessageException("La entrega no puede ser mayor al Saldo restante (" + selectedCtaCte.getImporte().subtract(selectedCtaCte.getEntregado()) + ")");
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
                    throw new MessageException("La Nota de Débito " + JGestionUtils.getNumeracion(selectedNotaDebito) + " ya se ha agregado al detalle");
                }
            }
        }
        jdReRe.getDtmAPagar().addRow(new Object[]{
            selectedCtaCte != null ? facturaToAddToDetail : selectedNotaDebito,
            selectedCtaCte != null ? JGestionUtils.getNumeracion(facturaToAddToDetail) : JGestionUtils.getNumeracion(selectedNotaDebito),
            null,
            entrega$
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

    public void showBuscador(Window owner, boolean modal, final boolean toAnular) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.COMPRA);
        if (toAnular) {
            UsuarioController.checkPermiso(PermisosController.PermisoDe.ANULAR_COMPROBANTES);
        }
        buscador = new JDBuscadorReRe(owner, "Buscador - " + CLASS_NAME, modal, "Proveedor", "Nº " + CLASS_NAME);
        if (toAnular) {
            buscador.setTitle(buscador.getTitle() + " para ANULAR");
        }
        if (conciliando) {
            buscador.getCheckAnulada().setEnabled(false);
            buscador.setTitle(buscador.getTitle() + " para Conciliar");
        }
        buscador.hideFormaPago();
        buscador.hideVendedor();
        buscador.hideUDNCuentaSubCuenta();
        buscador.setLocationRelativeTo(owner);
        UTIL.loadComboBox(buscador.getCbClieProv(), JGestionUtils.getWrappedProveedores(new ProveedorJpaController().findAllLite()), true);
        UTIL.loadComboBox(buscador.getCbCaja(), new UsuarioHelper().getCajas(true), true);
        UTIL.loadComboBox(buscador.getCbSucursal(), JGestionUtils.getWrappedSucursales(new UsuarioHelper().getSucursales()), true);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"ID", "(Sucursal) Nº", "Proveedor", "Monto", "Fecha", "Caja", "Usuario", "Fecha/Hora (Sist)"},
                new int[]{1, 50, 150, 50, 50, 50, 50, 80});
        buscador.getjTable1().getColumnModel().getColumn(3).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        UTIL.hideColumnTable(buscador.getjTable1(), 0);
        buscador.getjTable1().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    int rowIndex = buscador.getjTable1().getSelectedRow();
                    Integer id = (Integer) buscador.getjTable1().getModel().getValueAt(rowIndex, 0);
                    selectedRemesa = jpaController.find(id);
                    showRemesaViewerMode(toAnular);
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
                } catch (Exception ex) {
                    LOG.error("Error en Buscador Remesa", ex);
                    buscador.showMessage(ex.getMessage(), CLASS_NAME, 2);
                }
            }
        });
        buscador.setLocationRelativeTo(owner);
        buscador.setVisible(true);
    }

    public void showBuscadorToConciliar(Window owner) throws MessageException {
        conciliando = true;
        showBuscador(owner, true, false);
    }

    private void cargarFacturasCtaCtesYNotasDebito(Proveedor proveedor) {
        limpiarDetalle();
        List<CtacteProveedor> ccList = new CtacteProveedorController().findCtacteProveedorByProveedor(proveedor.getId(), Valores.CtaCteEstado.PENDIENTE.getId());
        List<NotaDebitoProveedor> notas = new NotaDebitoProveedorJpaController().findBy(proveedor, false);
        List<Object> l = new ArrayList<>(ccList.size() + notas.size());
        for (CtacteProveedor o : ccList) {
            l.add(o);
        }
        for (NotaDebitoProveedor o : notas) {
            l.add(o);
        }
        UTIL.loadComboBox(jdReRe.getCbCtaCtes(), JGestionUtils.getWrappedCtacteProveedor(l), false);
    }

    private void resetPanel() {
        jdReRe.getDcFechaReRe().setDate(null);
        jdReRe.getCbClienteProveedor().setSelectedIndex(0);
        SwingUtil.setComponentsEnabled(jdReRe.getComponents(), false, true);
    }

    @SuppressWarnings("unchecked")
    private void armarQuery() throws MessageException {
        StringBuilder query = new StringBuilder("SELECT o.id "
                + " FROM remesa o "
                + " LEFT JOIN detalle_remesa dr ON o.id = dr.remesa"
                + " LEFT JOIN factura_compra f ON f.id = dr.factura_compra"
                + " LEFT JOIN nota_debito_proveedor ndp ON ndp.id = dr.nota_debito_proveedor_id"
                + " LEFT JOIN proveedor p ON p.id = f.proveedor"
                + " LEFT JOIN proveedor pp ON pp.id = ndp.proveedor_id"
                + " LEFT JOIN proveedor ppp ON ppp.id = o.proveedor_id"
                + " JOIN caja c ON o.caja = c.id"
                + " JOIN sucursal s ON o.sucursal = s.id"
                + " JOIN usuario u ON o.usuario = u.id"
                + " WHERE o.id is not null "
                + " AND o.por_conciliar=" + conciliando);

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
                query.append(" AND f.numero = ").append(numero);
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de " + CLASS_NAME + " no válido");
            }
        }
        if (buscador.getDcDesde() != null) {
            query.append(" AND o.fecha_remesa >= '").append(UTIL.yyyy_MM_dd.format(buscador.getDcDesde())).append("'");
        }
        if (buscador.getDcHasta() != null) {
            query.append(" AND o.fecha_remesa <= '").append(UTIL.yyyy_MM_dd.format(buscador.getDcHasta())).append("'");
        }
        if (buscador.getDcDesdeSistema() != null) {
            query.append(" AND o.fecha_carga >= '").append(UTIL.yyyy_MM_dd.format(buscador.getDcDesdeSistema())).append("'");
        }
        if (buscador.getDcHastaSistema() != null) {
            query.append(" AND o.fecha_carga <= '").append(UTIL.yyyy_MM_dd.format(buscador.getDcHastaSistema())).append("'");
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
                    .append(" p.id = ").append(((ComboBoxWrapper<Proveedor>) buscador.getCbClieProv().getSelectedItem()).getId())
                    .append(" OR pp.id = ").append(((ComboBoxWrapper<Proveedor>) buscador.getCbClieProv().getSelectedItem()).getId())
                    .append(" OR ppp.id = ").append(((ComboBoxWrapper<Proveedor>) buscador.getCbClieProv().getSelectedItem()).getId())
                    .append(")");
        }
        query.append(" GROUP BY o.id");
//        System.out.println("QUERY: " + query);
        cargarDtmBuscador(query.toString());
    }

    private void cargarDtmBuscador(String query) {
        DefaultTableModel dtm = (DefaultTableModel) buscador.getjTable1().getModel();
        dtm.setRowCount(0);
        List<Remesa> l = jpaController.findByNativeQuery("SELECT r.*"
                + " FROM " + jpaController.getEntityClass().getSimpleName() + " r"
                + " WHERE r.id IN (" + query + ")"
                + " ORDER BY r.id");
        if (l.isEmpty()) {
            JOptionPane.showMessageDialog(buscador, "La busqueda no produjo ningún resultado", null, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        for (Remesa remesa : l) {
            //new String[]{"ID", "Nº", "Monto", "Fecha", "Caja", "Usuario", "Fecha/Hora (Sist)"},
            Proveedor p;
            if (!remesa.getDetalle().isEmpty()) {
                if (remesa.getDetalle().get(0).getFacturaCompra() != null) {
                    p = remesa.getDetalle().get(0).getFacturaCompra().getProveedor();
                } else {
                    p = remesa.getDetalle().get(0).getNotaDebitoProveedor().getProveedor();
                }
            } else {
                p = remesa.getProveedor();
            }
            dtm.addRow(new Object[]{
                remesa.getId(),
                JGestionUtils.getNumeracion(remesa, true),
                remesa.getAnulada() == null ? p.getNombre() : "[ANULADA] " + UTIL.TIMESTAMP_FORMAT.format(remesa.getAnulada()),
                remesa.getMonto(),
                UTIL.DATE_FORMAT.format(remesa.getFechaRemesa()),
                remesa.getCaja().getNombre() + "(" + remesa.getCaja().getId() + ")",
                remesa.getUsuario(),
                UTIL.TIMESTAMP_FORMAT.format(remesa.getFechaCarga())
            });
        }
    }

    private void showRemesaViewerMode(boolean toAnular) {
        try {
            setComprobanteUI(selectedRemesa);
            if (selectedRemesa.isPorConciliar()) {
                SwingUtil.setComponentsEnabled(jdReRe.getPanelAPagar().getComponents(), true, true, (Class<? extends Component>[]) null);
                jdReRe.getCbCtaCtes().setSelectedIndex(0);
                jdReRe.getbAceptar().setEnabled(true);
                jdReRe.getDcFechaReRe().setDate(new Date());
            }
            jdReRe.setLocationRelativeTo(buscador);
            jdReRe.getbAnular().setVisible(toAnular);
            jdReRe.getbAnular().setEnabled(toAnular);
            jdReRe.setVisible(true);
        } catch (MessageException ex) {
            ex.displayMessage(buscador);
        }
    }

    /**
     * Setea la ventana de JDReRe de forma q solo se puedan ver los datos y
     * detalles de la Remesa, imprimir y ANULAR, pero NO MODIFICAR
     *
     * @param remesa
     */
    private void setComprobanteUI(Remesa remesa) throws MessageException {
        if (jdReRe == null) {
            displayABMRemesa(null, true, false);
        }
        //por no redundar en DATOOOOOOOOOSS...!!!
        Proveedor p;
        if (remesa.getProveedor() != null) {
            p = remesa.getProveedor();
        } else if (remesa.getDetalle().get(0).getFacturaCompra() != null) {
            p = remesa.getDetalle().get(0).getFacturaCompra().getProveedor();
        } else {
            p = remesa.getDetalle().get(0).getNotaDebitoProveedor().getProveedor();
        }
        jdReRe.getLabelAnulado().setVisible(!remesa.getEstado());
        //que compare por String's..
        //por si el combo está vacio <VACIO> o no eligió ninguno
        //van a tirar error de ClassCastException
        UTIL.setSelectedItem(jdReRe.getCbSucursal(), remesa.getSucursal().getNombre());
        UTIL.setSelectedItem(jdReRe.getCbCaja(), remesa.getCaja().getNombre());
        UTIL.setSelectedItem(jdReRe.getCbClienteProveedor(), p);
        jdReRe.setTfCuarto(UTIL.AGREGAR_CEROS(remesa.getSucursal().getPuntoVenta(), 4));
        jdReRe.setTfOcteto(UTIL.AGREGAR_CEROS(String.valueOf(remesa.getNumero()), 8));
        jdReRe.getDcFechaReRe().setDate(remesa.getFechaRemesa());
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
        List<DetalleRemesa> detalle = remesa.getDetalle();
        DefaultTableModel dtm = jdReRe.getDtmAPagar();
        dtm.setRowCount(0);
        for (DetalleRemesa r : detalle) {
            dtm.addRow(new Object[]{
                r.getFacturaCompra() != null ? r.getFacturaCompra() : r.getNotaDebitoProveedor(),
                r.getFacturaCompra() != null ? JGestionUtils.getNumeracion(r.getFacturaCompra()) : JGestionUtils.getNumeracion(r.getNotaDebitoProveedor()),
                null,
                r.getMontoEntrega()
            });
        }
        loadPagos(remesa);
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
            } else if (object instanceof Especie) {
                Especie pago = (Especie) object;
                dtm.addRow(new Object[]{pago, "ES", pago.getDescripcion(), pago.getImporte()});
            } else if (object instanceof DetalleCajaMovimientos) {
                DetalleCajaMovimientos pago = (DetalleCajaMovimientos) object;
                dtm.addRow(new Object[]{pago, "EF", null, pago.getMonto().negate()});
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

    public void anular(Remesa remesa) throws MessageException, Exception {
        if (remesa == null) {
            throw new MessageException("Remesa is NULL");
        }
        if (!remesa.getEstado()) {
            throw new MessageException("Esta " + CLASS_NAME + " ya está anulada");
        }
        jpaController.anular(remesa);
    }

    List<Remesa> findByFactura(FacturaCompra factura) {
        List<DetalleRemesa> detalleRemesaList = jpaController.findDetalleRemesaByFactura(factura);
        List<Remesa> remesas = new ArrayList<>(detalleRemesaList.size());
        for (DetalleRemesa detalleRecibo : detalleRemesaList) {
            remesas.add(detalleRecibo.getRemesa());
        }
        return remesas;
    }

    public List<Object> loadPagos(Remesa remesa) {
        List<Object> pagos = new ArrayList<>(remesa.getPagos().size());
        for (RemesaPagos pago : remesa.getPagos()) {
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
                NotaCreditoProveedor o = new NotaCreditoProveedorJpaController().find(pago.getComprobanteId());
                pagos.add(o);
            } else if (pago.getFormaPago() == 4) {
                ComprobanteRetencion o = new ComprobanteRetencionJpaController().find(pago.getComprobanteId());
                pagos.add(o);
            } else if (pago.getFormaPago() == 5) {
                CuentabancariaMovimientos o = new CuentabancariaMovimientosJpaController().find(pago.getComprobanteId());
                pagos.add(o);
            } else if (pago.getFormaPago() == 6) {
                Especie o = new EspecieJpaController().find(pago.getComprobanteId());
                pagos.add(o);
            } else {
                throw new IllegalArgumentException("Forma Pago Remesa no válida:" + pago.getFormaPago());
            }
        }
        remesa.setPagosEntities(pagos);
        return pagos;
    }
}
