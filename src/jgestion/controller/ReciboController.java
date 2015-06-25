package jgestion.controller;

import jgestion.entity.Cliente;
import jgestion.entity.ComprobanteRetencion;
import jgestion.entity.ReciboPagos;
import jgestion.entity.ChequeTerceros;
import jgestion.entity.NotaDebito;
import jgestion.entity.Caja;
import jgestion.entity.Sucursal;
import jgestion.entity.DetalleCajaMovimientos;
import jgestion.entity.FacturaVenta;
import jgestion.entity.NotaCredito;
import jgestion.entity.ChequePropio;
import jgestion.entity.CajaMovimientos;
import jgestion.entity.DetalleRecibo;
import jgestion.entity.Recibo;
import jgestion.entity.Especie;
import jgestion.entity.CuentabancariaMovimientos;
import jgestion.entity.CtacteCliente;
import jgestion.controller.exceptions.MessageException;
import jgestion.entity.enums.ChequeEstado;
import generics.GenericBeanCollection;
import jgestion.gui.JDBuscadorReRe;
import jgestion.gui.JDReRe;
import generics.gui.JDialogTable;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Window;
import java.awt.event.*;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import jgestion.JGestionUtils;
import jgestion.JGestion;
import jgestion.jpa.controller.CajaMovimientosJpaController;
import jgestion.jpa.controller.ChequePropioJpaController;
import jgestion.jpa.controller.ChequeTercerosJpaController;
import jgestion.jpa.controller.ComprobanteRetencionJpaController;
import jgestion.jpa.controller.CuentabancariaMovimientosJpaController;
import jgestion.jpa.controller.EspecieJpaController;
import jgestion.jpa.controller.NotaCreditoJpaController;
import jgestion.jpa.controller.NotaDebitoJpaController;
import jgestion.jpa.controller.ReciboJpaController;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import utilities.general.NumberToLetterConverter;
import utilities.general.TableExcelExporter;
import utilities.general.UTIL;
import utilities.gui.SwingUtil;
import utilities.general.EntityWrapper;
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
    private JDBuscadorReRe buscador;
    private Recibo selectedRecibo;
    private final ReciboJpaController jpaController = new ReciboJpaController();
    private boolean unlockedNumeracion = false;
    private boolean viewMode;
    private boolean toConciliar = false;
    private boolean conciliando = false;

    public ReciboController() {
    }

    /**
     * Crea la ventana para realizar Recibo's
     *
     * @param owner owner/parent
     * @param modal debería ser <code>true</code> siempre, no está implementado para false
     * @param setVisible
     * @throws MessageException
     */
    public void displayABMRecibos(Window owner, boolean modal, boolean setVisible) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.VENTA);
        UsuarioHelper uh = new UsuarioHelper();
        if (uh.getSucursales().isEmpty()) {
            throw new MessageException(JGestion.resourceBundle.getString("unassigned.sucursal"));
        }
        if (uh.getCajas(true).isEmpty()) {
            throw new MessageException(JGestion.resourceBundle.getString("unassigned.caja"));
        }
        jdReRe = new JDReRe(owner, modal);
        SwingUtil.setComponentsEnabled(jdReRe.getPanelPagos().getComponents(), false, true);
        jdReRe.setUIForRecibos();
        UTIL.getDefaultTableModel(jdReRe.getTableAPagar(),
                new String[]{"facturaID", "Factura", "Entrega"},
                new int[]{1, 60, 50});
        jdReRe.getTableAPagar().getColumnModel().getColumn(2).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        UTIL.hideColumnTable(jdReRe.getTableAPagar(), 0);
        UTIL.loadComboBox(jdReRe.getCbSucursal(), JGestionUtils.getWrappedSucursales(uh.getSucursales()), false);
        UTIL.loadComboBox(jdReRe.getCbCaja(), uh.getCajas(true), false);
        UTIL.loadComboBox(jdReRe.getCbClienteProveedor(), new ClienteController().findAll(), true);
        AutoCompleteDecorator.decorate(jdReRe.getCbClienteProveedor());
        UTIL.loadComboBox(jdReRe.getCbCtaCtes(), null, false);
        setNextNumeroReRe();
        jdReRe.getbAnular().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(jdReRe, "La anulación de un Recibo implica:"
                            + "\n- Deshacer el pago realizado a cada Factura."
                            + "\n- La eliminación de las Retenciones y Transferencias."
                            + "\n- La desvinculación de los Cheques Terceros (vuelven a la cartera) y cheques Propios como " + ChequeEstado.ENDOSADO + " nuevamente."
                            + "\n- La desvinculación de las Notas de Crédito con el Recibo."
                            + "\n- La desvinculación de las Notas de Débito con el Recibo."
                            + "\n- El cambio de estado de Cheques Propios a \"Entregados\" y dejando en blanco el concepto \"Comprobante de Ingreso\"."
                            + "\n- La generación de un movimiento de Egreso de la entrega en Efectivo, en la misma Caja en la cual se originó el ingreso por el Recibo."
                            + "\n¿Confirmar anulación?",
                            "Anulación de Recibo " + JGestionUtils.getNumeracion(selectedRecibo, true), JOptionPane.YES_NO_OPTION)) {
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
                        jdReRe.showMessage(jpaController.getEntityClass().getSimpleName() + " " + JGestionUtils.getNumeracion(re, true) + " registrado.", null, 1);
                        jdReRe.limpiarDetalles();
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
                    if (!conciliando && selectedRecibo != null) {
                        // cuando se re-imprime un recibo elegido desde el buscador
                        doReportRecibo(selectedRecibo);
                    } else {
                        //cuando se está creando un recibo y se va imprimir al tokesaun!
                        Recibo recibo = setAndPersist();
                        doReportRecibo(recibo);
                        resetPanel();
                        jdReRe.limpiarDetalles();
                        if (conciliando) {
                            jdReRe.dispose();
                        }
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
                    jdReRe.limpiarDetalles();
                    SwingUtil.setComponentsEnabled(jdReRe.getPanelPagos().getComponents(), false, true);
                }
            }
        });
        jdReRe.getCbCtaCtes().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    @SuppressWarnings("unchecked")
                    EntityWrapper<?> cbw = (EntityWrapper<?>) jdReRe.getCbCtaCtes().getSelectedItem();
                    if (cbw.getEntity() instanceof CtacteCliente) {
                        selectedCtaCte = (CtacteCliente) cbw.getEntity();
                        jdReRe.setTfImporte(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getImporte()));
                        jdReRe.setTfPagado(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getEntregado()));
                        jdReRe.setTfSaldo(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getImporte() - selectedCtaCte.getEntregado()));
                        jdReRe.setTfEntrega(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getImporte() - selectedCtaCte.getEntregado()));
                        jdReRe.getTfEntrega().setEditable(true);
                    }
                } catch (ClassCastException | NullPointerException ex) {
                    selectedCtaCte = null;
                }
            }
        });
        jdReRe.getCbTipo().addActionListener((ActionEvent e) -> {
            if (jdReRe.getCbSucursal().isEnabled()) {
                setNextNumeroReRe();
            }
        });
        jdReRe.getCbSucursal().addActionListener((ActionEvent e) -> {
            if (jdReRe.getCbSucursal().isEnabled()) {
                setNextNumeroReRe();
            }
        });
        jdReRe.getbCancelar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetPanel();
                jdReRe.limpiarDetalles();
            }
        });
        jdReRe.getBtnAddPago().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    showUIPagos(jdReRe.getCbFormasDePago().getSelectedIndex());
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
                            showABMEfectivo((DetalleCajaMovimientos) o);
                        } else if (o instanceof ComprobanteRetencion) {
                            showABMRetencion((ComprobanteRetencion) o);
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
                    showDetalleCredito((Cliente) jdReRe.getCbClienteProveedor().getSelectedItem());
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
    private void showUIPagos(int formaPago) throws MessageException {

        if (formaPago == 0) {
            showABMEfectivo(null);
        } else if (formaPago == 1) {
            showABMChequePropio();
        } else if (formaPago == 2) {
            showABMChequeTerceros();
        } else if (formaPago == 3) {
            showABMNotaCredito();
        } else if (formaPago == 4) {
            showABMRetencion(null);
        } else if (formaPago == 5) {
            showABMTransferencia();
        } else if (formaPago == 6) {
            showABMEspecie(null);
        }
    }

    private void showABMEfectivo(DetalleCajaMovimientos toEdit) {
        BigDecimal monto = jdReRe.displayABMEfectivo(toEdit == null ? null : toEdit.getMonto());
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
                d.setMonto(monto);
                dtm.addRow(new Object[]{d, "EF", null, monto});
            } else {
                toEdit.setMonto(monto);
                int selectedRow = jdReRe.getTablePagos().getSelectedRow();
                dtm.setValueAt(toEdit, selectedRow, 0);
                dtm.setValueAt(toEdit.getNumero(), selectedRow, 2);
                dtm.setValueAt(monto, selectedRow, 3);
            }
        }
    }

    private void showABMChequePropio() throws MessageException {
        ChequePropio cheque = new ChequePropioController().initManagerBuscador(jdReRe);
        if (cheque != null) {
            ChequesController.checkUniquenessOnTable(jdReRe.getDtmPagos(), cheque);
            DefaultTableModel dtm = jdReRe.getDtmPagos();
            dtm.addRow(new Object[]{cheque, "CH", cheque.getNumero(), cheque.getImporte()});
        }
    }

    private void showABMChequeTerceros() {
        try {
            String[] options = {"Seleccionar", "Crear"};
            int opt = JOptionPane.showOptionDialog(null, "* Puede seleccionar Cheque existente o crear uno", "Cheques Terceros", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
            ChequeTerceros cheque = null;
            if (opt == 0) {
                cheque = new ChequeTercerosController().initManagerBuscador(null);
                if (cheque.getComprobanteIngreso() != null) {
                    throw new MessageException("El cheque seleccionado ya está asociado a un comprobante de ingreso:"
                            + "\n" + cheque.getComprobanteIngreso());
                }
            } else if (opt == 1) {
                Cliente c = null;
                try {
                    c = (Cliente) jdReRe.getCbClienteProveedor().getSelectedItem();
                } catch (ClassCastException ex) {
                    throw new MessageException("Debe especificar un cliente para poder agregar un Cheque");
                }
                cheque = new ChequeTercerosController().displayABM(jdReRe, null, c);
            }
            if (cheque != null) {
                ChequesController.checkUniquenessOnTable(jdReRe.getDtmPagos(), cheque);
                DefaultTableModel dtm = jdReRe.getDtmPagos();
                dtm.addRow(new Object[]{cheque, "CH", cheque.getBanco().getNombre() + " " + cheque.getNumero(), cheque.getImporte()});
            }
        } catch (MessageException ex) {
            ex.displayMessage(jdReRe);
        }
    }

    private void showABMNotaCredito() {
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

    private void showABMEspecie(Especie toEdit) {
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

    private void showABMRetencion(ComprobanteRetencion toEdit) {
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

    private void showABMTransferencia() {
        Cliente c = (Cliente) jdReRe.getCbClienteProveedor().getSelectedItem();
        CuentabancariaMovimientos comprobante = new CuentabancariaMovimientosController().displayTransferenciaCliente(jdReRe, c.getNombre());
        if (comprobante != null) {
            DefaultTableModel dtm = jdReRe.getDtmPagos();
            dtm.addRow(new Object[]{comprobante, "TR", comprobante.getDescripcion(), comprobante.getCredito()});
        }
    }

    private void showDetalleCredito(Cliente cliente) {
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
        if (jdReRe.getDcFechaReRe().getDate() == null) {
            throw new MessageException("Fecha de " + jpaController.getEntityClass().getSimpleName() + " no válida");
        }
        if (jdReRe.getDtmPagos().getRowCount() < 1) {
            throw new MessageException("No ha ingresado ningún pago");
        }
        if (!toConciliar) {
            if (jdReRe.getDtmAPagar().getRowCount() < 1) {
                throw new MessageException("No ha hecho ninguna entrega");
            }
        }
        if (unlockedNumeracion) {
            try {
                Integer numero = Integer.valueOf(jdReRe.getTfOcteto());
                if (numero < 1 || numero > 99999999) {
                    throw new MessageException("Número de " + jpaController.getEntityClass().getSimpleName() + " no válido, debe ser mayor a 0 y menor o igual a 99999999");
                }
                Recibo old = jpaController.find(getSelectedSucursalFromJD(), numero);
                if (old != null) {
                    throw new MessageException("Ya existe un registro de " + jpaController.getEntityClass().getSimpleName() + " " + JGestionUtils.getNumeracion(old, true));
                }
            } catch (NumberFormatException numberFormatException) {
                throw new MessageException("Número de " + jpaController.getEntityClass().getSimpleName() + " no válido, ingrese solo dígitos");
            }
        }
    }

    private Sucursal getSelectedSucursalFromJD() {
        @SuppressWarnings("unchecked")
        EntityWrapper<Sucursal> cbw = (EntityWrapper<Sucursal>) jdReRe.getCbSucursal().getSelectedItem();
        return cbw.getEntity();
    }

    private Recibo setAndPersist() throws MessageException, Exception {
        checkConstraints();
        Recibo re = new Recibo();
        try {
            re.setCaja((Caja) jdReRe.getCbCaja().getSelectedItem());
        } catch (ClassCastException e) {
            re.setCaja(null);
        }
        re.setTipo((char) jdReRe.getCbTipo().getSelectedItem());
        re.setSucursal(getSelectedSucursalFromJD());
        if (unlockedNumeracion || conciliando) {
            re.setNumero(Integer.valueOf(jdReRe.getTfOcteto()));
        } else {
            re.setNumero(jpaController.getNextNumero(re.getSucursal(), re.getTipo()));
        }
        re.setUsuario(UsuarioController.getCurrentUser());
        re.setEstado(true);
        re.setFechaRecibo(jdReRe.getDcFechaReRe().getDate());
        re.setDetalle(new ArrayList<>(jdReRe.getDtmAPagar().getRowCount()));
        re.setPagos(new ArrayList<>(jdReRe.getDtmPagos().getRowCount()));
        re.setPorConciliar(toConciliar);
        re.setCliente((Cliente) jdReRe.getCbClienteProveedor().getSelectedItem());
        DefaultTableModel dtm = jdReRe.getDtmAPagar();
        FacturaVentaController fcc = new FacturaVentaController();
        BigDecimal monto = BigDecimal.ZERO;
        for (int rowIndex = 0; rowIndex < dtm.getRowCount(); rowIndex++) {
            Object o = dtm.getValueAt(rowIndex, 0);
            DetalleRecibo detalle = new DetalleRecibo();
            if (o instanceof FacturaVenta) {
                FacturaVenta fv = fcc.find(((FacturaVenta) o).getId());
                if (UTIL.compararIgnorandoTimeFields(re.getFechaRecibo(), fv.getFechaVenta()) < 0) {
                    throw new MessageException("La fecha de la factura es posterior a la " + JGestionUtils.getNumeracion(fv));
                }
                detalle.setFacturaVenta(fv);
            } else if (o instanceof NotaDebito) {
                NotaDebito nota = new NotaDebitoJpaController().find(((NotaDebito) o).getId());
                if (UTIL.compararIgnorandoTimeFields(re.getFechaRecibo(), nota.getFechaNotaDebito()) < 0) {
                    throw new MessageException("La fecha de la Nota de Débito es posterior a la " + JGestionUtils.getNumeracion(nota));
                }
                detalle.setNotaDebito(nota);
            } else {
                throw new IllegalArgumentException();
            }
            detalle.setMontoEntrega((BigDecimal) dtm.getValueAt(rowIndex, 2));
            detalle.setRecibo(re);
            re.getDetalle().add(detalle);
            monto = monto.add(detalle.getMontoEntrega());
        }
        re.setMonto(monto);
        dtm = jdReRe.getDtmPagos();
        List<Object> pagos = new ArrayList<>(dtm.getRowCount());
        BigDecimal importePagado = BigDecimal.ZERO;
        for (int row = 0; row < dtm.getRowCount(); row++) {
            pagos.add(dtm.getValueAt(row, 0));
            importePagado = importePagado.add((BigDecimal) dtm.getValueAt(row, 3));
        }
        boolean asentarDiferenciaEnCaja = false;
        if (!re.isPorConciliar() || conciliando) {
            if (0 != jdReRe.getTfTotalAPagar().getText().compareTo(jdReRe.getTfTotalPagado().getText())) {
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(jdReRe, "El importe a pagar no coincide con el detalle de pagos."
                        + "¿Desea que la diferencia ($" + UTIL.DECIMAL_FORMAT.format(re.getMonto().subtract(importePagado)) + ") sea reflejada en la Caja?", "Arqueo de valores", JOptionPane.YES_NO_OPTION)) {
                    asentarDiferenciaEnCaja = true;
                } else {
                    throw new MessageException("Operación cancelada");
                }
            }
        }
        re.setPagosEntities(pagos);

        //persisting.....
        if (conciliando) {
            jpaController.conciliar(re);
        } else {
            jpaController.persist(re);
        }
        for (DetalleRecibo detalle : re.getDetalle()) {
            if (detalle.getFacturaVenta() != null) {
                actualizarMontoEntrega(detalle.getFacturaVenta(), detalle.getMontoEntrega());
            } else {
                actualizarMontoEntrega(detalle.getNotaDebito(), detalle.getMontoEntrega());
            }
        }
        if (asentarDiferenciaEnCaja) {
            BigDecimal diferencia = importePagado.subtract(re.getMonto());
            DetalleCajaMovimientos d = new DetalleCajaMovimientos();
            d.setIngreso(diferencia.compareTo(BigDecimal.ZERO) == -1);
            d.setTipo(DetalleCajaMovimientosController.RECIBO);
            d.setUsuario(UsuarioController.getCurrentUser());
            d.setCuenta(CuentaController.SIN_CLASIFICAR);
            d.setDescripcion(jpaController.getEntityClass().getSimpleName() + " " + JGestionUtils.getNumeracion(re, true) + " (DIFERENCIA)");
            CajaMovimientos cm = new CajaMovimientosJpaController().findCajaMovimientoAbierta(re.getCaja());
            d.setCajaMovimientos(cm); // no te olvides este tampoco!! 
            d.setNumero(re.getId());
            d.setMonto(d.getIngreso() ? diferencia : diferencia.negate());
            new DetalleCajaMovimientosController().create(d);
        }
        return re;
    }

    private void actualizarMontoEntrega(NotaDebito notaDebito, BigDecimal entrega) {
        CtacteCliente ctacte = new CtacteClienteController().findByNotaDebito(notaDebito.getId());
        ctacte.setEntregado(ctacte.getEntregado() + entrega.doubleValue());
        if (notaDebito.getImporte().compareTo(BigDecimal.valueOf(ctacte.getEntregado())) == 0) {
            ctacte.setEstado(Valores.CtaCteEstado.PAGADA.getId());
        }
        new CtacteClienteController().edit(ctacte);

    }

    private void actualizarMontoEntrega(FacturaVenta facturaVenta, BigDecimal entrega) {
        CtacteCliente ctacte = new CtacteClienteController().findBy(facturaVenta);
        ctacte.setEntregado(ctacte.getEntregado() + entrega.doubleValue());
        if (BigDecimal.valueOf(facturaVenta.getImporte()).compareTo(BigDecimal.valueOf(ctacte.getEntregado())) == 0) {
            ctacte.setEstado(Valores.CtaCteEstado.PAGADA.getId());
        }
        new CtacteClienteController().edit(ctacte);
    }

    private void addEntregaToDetalle() throws MessageException {
        Date comprobanteFecha;
        if (jdReRe.getDcFechaReRe().getDate() == null) {
            throw new MessageException("Debe especificar una fecha de " + CLASS_NAME + " antes de agregar comprobantes a pagar.");
        }
        if (selectedCtaCte == null) {
            throw new MessageException("No hay Factura o Nota de Débito seleccionada.");
        }
        if (selectedCtaCte.getFactura() != null) {
            comprobanteFecha = selectedCtaCte.getFactura().getFechaVenta();
        } else {
            comprobanteFecha = selectedCtaCte.getNotaDebito().getFechaNotaDebito();
        }
        //hay que ignorar las HH:MM:ss:mmmm de las fechas para hacer las comparaciones
        if (UTIL.getDateYYYYMMDD(jdReRe.getDcFechaReRe().getDate()).before(UTIL.getDateYYYYMMDD(comprobanteFecha))) {
            throw new MessageException("La fecha de " + CLASS_NAME + " no puede ser anterior"
                    + "\n a la fecha del comprobante ("
                    + UTIL.DATE_FORMAT.format(comprobanteFecha) + ")");
        }

        BigDecimal entrega$;
        try {
            entrega$ = new BigDecimal(jdReRe.getTfEntrega().getText());
        } catch (NumberFormatException ex) {
            throw new MessageException("Monto de entrega no válido");
        }
        if (entrega$.doubleValue() <= 0) {
            throw new MessageException("Monto de entrega no válido (Debe ser mayor a 0)");
        }
        BigDecimal d = BigDecimal.valueOf(selectedCtaCte.getImporte()).setScale(2, RoundingMode.HALF_EVEN)
                .subtract(
                        BigDecimal.valueOf(selectedCtaCte.getEntregado()).setScale(2, RoundingMode.HALF_EVEN));
        if (entrega$.compareTo(d) == 1) {
            throw new MessageException("Monto de entrega no puede ser mayor al Saldo restante (" + d + ")");
        }
        Object comprobante = selectedCtaCte.getFactura() != null ? selectedCtaCte.getFactura() : selectedCtaCte.getNotaDebito();
        //ctrl que no se inserte + de una entrega de la misma factura
        //        double entregaParcial = 0;
        for (int i = 0; i < jdReRe.getDtmAPagar().getRowCount(); i++) {
            if (comprobante.equals(jdReRe.getDtmAPagar().getValueAt(i, 0))) {
                if (selectedCtaCte.getFactura() != null) {
                    throw new MessageException("La factura " + JGestionUtils.getNumeracion(selectedCtaCte.getFactura()) + " ya se ha agregada al detalle");
                } else {
                    throw new MessageException("La Nota de Débito " + JGestionUtils.getNumeracion(selectedCtaCte.getNotaDebito()) + " ya se ha agregada al detalle");
                }
            }
        }
        jdReRe.getDtmAPagar().addRow(new Object[]{
            selectedCtaCte.getFactura() != null ? selectedCtaCte.getFactura() : selectedCtaCte.getNotaDebito(),
            selectedCtaCte.getFactura() != null ? JGestionUtils.getNumeracion(selectedCtaCte.getFactura()) : JGestionUtils.getNumeracion(selectedCtaCte.getNotaDebito()),
            entrega$
        });
        updateTotales();
    }

    private void updateTotales() {
        BigDecimal totalAPagar = BigDecimal.ZERO;
        BigDecimal totalPagado = BigDecimal.ZERO;
        DefaultTableModel dtm = (DefaultTableModel) jdReRe.getTableAPagar().getModel();
        for (int row = 0; row < dtm.getRowCount(); row++) {
            BigDecimal monto = (BigDecimal) dtm.getValueAt(row, 2);
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
        if (toAnular) {
            buscador.setTitle(buscador.getTitle() + " para ANULAR");
            buscador.getCheckAnulada().setEnabled(false);
            buscador.getCheckAnulada().setSelected(true);
        }
        showBuscador(toAnular);
    }

    public void showBuscadorToConciliar(Window owner) {
        conciliando = true;
        showBuscador(owner, true, false);
    }

    private void showReciboViewerMode(boolean toAnular) {
        try {
            setComprobanteUI(selectedRecibo);
            if (selectedRecibo.isPorConciliar()) {
                viewMode = false;
                SwingUtil.setComponentsEnabled(jdReRe.getPanelAPagar().getComponents(), true, true, (Class<? extends Component>[]) null);
                jdReRe.getCbCtaCtes().setSelectedIndex(0);
            } else {
                viewMode = true;
            }
            jdReRe.getbAceptar().setEnabled(false);
            jdReRe.getbCancelar().setEnabled(false);
            jdReRe.getbAnular().setEnabled(toAnular);
            jdReRe.setLocationRelativeTo(buscador);
            jdReRe.setVisible(true);
        } catch (MessageException ex) {
            jdReRe.showMessage(ex.getMessage(), null, JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Setea la ventana de JDReRe de forma q solo se puedan ver los datos y detalles de la Recibo,
     * imprimir y ANULAR, pero NO MODIFICAR
     *
     * @param recibo
     */
    private void setComprobanteUI(Recibo recibo) throws MessageException {
        if (jdReRe == null) {
            displayABMRecibos(null, true, false);
        }
//        bloquearVentana(true);
        //por no redundar en DATOOOOOOOOOSS...!!!
        Cliente cliente;
        if (recibo.isPorConciliar()) {
            cliente = recibo.getCliente();
        } else if (recibo.getDetalle().get(0).getFacturaVenta() != null) {
            cliente = new FacturaVentaController().find(recibo.getDetalle().get(0).getFacturaVenta().getId()).getCliente();
        } else {
            cliente = new NotaDebitoJpaController().find(recibo.getDetalle().get(0).getNotaDebito().getId()).getCliente();
        }
        //que compare por String's..
        //por si el combo está vacio <VACIO> o no eligió ninguno
        //van a tirar error de ClassCastException
        UTIL.setSelectedItem(jdReRe.getCbSucursal(), recibo.getSucursal().getNombre());
        UTIL.setSelectedItem(jdReRe.getCbCaja(), recibo.getCaja().toString());
        UTIL.setSelectedItem(jdReRe.getCbClienteProveedor(), cliente.getNombre());
        jdReRe.setTfCuarto(UTIL.AGREGAR_CEROS(recibo.getSucursal().getPuntoVenta(), 4));
        jdReRe.setTfOcteto(UTIL.AGREGAR_CEROS(recibo.getNumero(), 8));
        jdReRe.getDcFechaReRe().setDate(recibo.getFechaRecibo());
        jdReRe.setDcFechaCarga(recibo.getFechaCarga());
        cargarDetalleReRe(recibo);
        updateTotales();
        SwingUtil.setComponentsEnabled(jdReRe.getPanelDatos().getComponents(), false, true);
        SwingUtil.setComponentsEnabled(jdReRe.getPanelAPagar().getComponents(), false, true);
        SwingUtil.setComponentsEnabled(jdReRe.getPanelPagos().getComponents(), false, true);
        if (recibo.isPorConciliar()) {
            jdReRe.getDcFechaReRe().setEnabled(true);
        }
        jdReRe.setTfImporte(null);
        jdReRe.setTfPagado(null);
        jdReRe.setTfSaldo(null);
        jdReRe.getbImprimir().setEnabled(true);
        jdReRe.getLabelAnulado().setVisible(!recibo.getEstado());
    }

    private Recibo showBuscador(final boolean toAnular) {
        buscador.setParaRecibos();
        UTIL.loadComboBox(buscador.getCbClieProv(), JGestionUtils.getWrappedClientes(new ClienteController().findAll()), true);
        UTIL.loadComboBox(buscador.getCbCaja(), new UsuarioHelper().getCajas(Boolean.TRUE), true);
        UTIL.loadComboBox(buscador.getCbSucursal(), JGestionUtils.getWrappedSucursales(new UsuarioHelper().getSucursales()), true);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"Instance", "Nº Recibo", "Cliente", "Monto", "Fecha", "Caja", "Usuario", "Fecha/Hora (Sist)"},
                new int[]{1, 80, 200, 50, 40, 50, 50, 70});
        buscador.getjTable1().getColumnModel().getColumn(3).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        buscador.getjTable1().getColumnModel().getColumn(4).setCellRenderer(FormatRenderer.getDateRenderer());
        buscador.getjTable1().getColumnModel().getColumn(7).setCellRenderer(FormatRenderer.getDateTimeRenderer());
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
        buscador.getbImprimir().setVisible(true);
        buscador.getbImprimir().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "No implementado aún");
            }
        });
        buscador.getBtnToExcel().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (buscador.getjTable1().getRowCount() < 1) {
                        throw new MessageException(JGestion.resourceBundle.getString("warn.emptytable"));
                    }
                    File file = JGestionUtils.showSaveDialogFileChooser(buscador, "Archivo Excel (.xls)", new File("recibos.xls"), "xls");
                    TableExcelExporter tee = new TableExcelExporter(file, buscador.getjTable1());
                    tee.export();
                    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(buscador, "¿Abrir archivo generado?", null, JOptionPane.YES_NO_OPTION)) {
                        Desktop.getDesktop().open(file);
                    }
                } catch (MessageException ex) {
                    JOptionPane.showMessageDialog(buscador, ex.getMessage(), null, 2);
                } catch (Exception ex) {
                    LOG.error(null, ex);
                    JOptionPane.showMessageDialog(buscador, ex.getMessage(), "ERROR", 0);
                }
            }
        });
        if (conciliando) {
            buscador.setTitle(buscador.getTitle() + " para Conciliar");
        }
        buscador.setVisible(true);
        return selectedRecibo;
    }

    private void cargarFacturasCtaCtesYNotasDebito(Cliente cliente) {
        jdReRe.limpiarDetalles();
        List<CtacteCliente> l = new CtacteClienteController().findByCliente(cliente, Valores.CtaCteEstado.PENDIENTE.getId());
        UTIL.loadComboBox(jdReRe.getCbCtaCtes(), JGestionUtils.getWrappedCtacteCliente(l), false);
    }

    /**
     * Resetea la ventana; - pone la fecha actual - clienteProveedor.index(0) - setea el
     * NextNumeroReRe - rereSelected = null;
     */
    private void resetPanel() {
        jdReRe.getDcFechaReRe().setDate(null);
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
                "   SELECT o.id"
                + " FROM recibo o"
                + " LEFT JOIN detalle_recibo dr ON (o.id = dr.recibo)"
                + " LEFT JOIN factura_venta f ON (dr.factura_venta = f.id)"
                + " LEFT JOIN nota_debito ON (dr.nota_debito_id = nota_debito.id)"
                + " LEFT JOIN cliente p ON (f.cliente = p.id) "
                + " LEFT JOIN cliente pp ON (nota_debito.cliente_id = pp.id) "
                + " JOIN caja c ON (o.caja = c.id)"
                + " JOIN sucursal s ON (o.sucursal = s.id)"
                + " JOIN usuario u ON (o.usuario = u.id)"
                + " WHERE o.id is not null "
                + " AND o.por_conciliar=" + conciliando);
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
            query.append(" AND o.fecha_recibo >= '").append(UTIL.yyyy_MM_dd.format(buscador.getDcDesde())).append("'");
        }
        if (buscador.getDcHasta() != null) {
            query.append(" AND o.fecha_recibo <= '").append(UTIL.yyyy_MM_dd.format(buscador.getDcHasta())).append("'");
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
            query.append(" AND o.sucursal = ").append(((EntityWrapper<Sucursal>) buscador.getCbSucursal().getSelectedItem()).getId());
        } else {
            query.append(" AND (");
            for (int i = 1; i < buscador.getCbSucursal().getItemCount(); i++) {
                EntityWrapper<Sucursal> cbw = (EntityWrapper<Sucursal>) buscador.getCbSucursal().getItemAt(i);
                query.append(" o.sucursal=").append(cbw.getId());
                if ((i + 1) < buscador.getCbSucursal().getItemCount()) {
                    query.append(" OR ");
                }
            }
            query.append(")");
        }
        if (!buscador.getCheckAnulada().isEnabled()) {
            //buscador PARA ANULAR
            query.append(" AND o.estado = ").append(buscador.isCheckAnuladaSelected());
        } else {
            query.append(" AND o.estado = ").append(!buscador.isCheckAnuladaSelected());
        }
        if (buscador.getCbClieProv().getSelectedIndex() > 0) {
            query.append(" AND (")
                    .append(" p.id = ").append(((EntityWrapper<Cliente>) buscador.getCbClieProv().getSelectedItem()).getId())
                    .append(" OR pp.id = ").append(((EntityWrapper<Cliente>) buscador.getCbClieProv().getSelectedItem()).getId())
                    .append(" OR o.cliente_id = ").append(((EntityWrapper<Cliente>) buscador.getCbClieProv().getSelectedItem()).getId())
                    .append(")");
        }

        query.append(" GROUP BY o.id");
//        LOG.debug(query.toString());
        cargarBuscador(query.toString());
    }

    private void cargarBuscador(String query) {
        DefaultTableModel dtm = (DefaultTableModel) buscador.getjTable1().getModel();
        dtm.setRowCount(0);
        List<Recibo> l = jpaController.findByNativeQuery("SELECT r.*"
                + " FROM " + jpaController.getEntityClass().getSimpleName() + " r"
                + " WHERE r.id IN (" + query + ")"
                + " ORDER BY r.sucursal, r.numero");
        if (l.isEmpty()) {
            JOptionPane.showMessageDialog(buscador, "La busqueda no produjo ningún resultado", null, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        for (Recibo o : l) {
            dtm.addRow(new Object[]{
                o.getId(),
                JGestionUtils.getNumeracion(o, true),
                o.getCliente().getNombre(),
                o.getMonto(),
                o.getFechaRecibo(),
                o.getCaja().getNombre(),
                o.getUsuario().getNick(),
                o.getFechaCarga()
            });
        }
    }

    private void cargarDetalleReRe(Recibo recibo) {
        List<DetalleRecibo> detalleReciboList = recibo.getDetalle();
        DefaultTableModel dtm = jdReRe.getDtmAPagar();
        dtm.setRowCount(0);
        for (DetalleRecibo r : detalleReciboList) {
            dtm.addRow(new Object[]{
                r.getFacturaVenta() != null ? r.getFacturaVenta() : r.getNotaDebito(),
                r.getFacturaVenta() != null ? JGestionUtils.getNumeracion(r.getFacturaVenta()) : JGestionUtils.getNumeracion(r.getNotaDebito()),
                r.getMontoEntrega()
            });
        }
        loadPagos(recibo);
        dtm = jdReRe.getDtmPagos();
        dtm.setRowCount(0);
        for (Object object : recibo.getPagosEntities()) {
            if (object instanceof ChequePropio) {
                ChequePropio pago = (ChequePropio) object;
                dtm.addRow(new Object[]{pago, "CHP", pago.getBanco().getNombre() + " N°" + pago.getNumero(), pago.getImporte()});
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
                dtm.addRow(new Object[]{pago, "EF", null, pago.getMonto()});
            } else if (object instanceof CuentabancariaMovimientos) {
                CuentabancariaMovimientos pago = (CuentabancariaMovimientos) object;
                dtm.addRow(new Object[]{pago, "TR", pago.getDescripcion(), pago.getCredito()});
            } else if (object instanceof Especie) {
                Especie pago = (Especie) object;
                dtm.addRow(new Object[]{pago, "ES", pago.getDescripcion(), pago.getImporte()});
            }
        }
    }

    public void loadPagos(Recibo recibo) {
        List<Object> pagos = new ArrayList<>(recibo.getPagos().size());
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
            } else if (pago.getFormaPago() == 6) {
                Especie o = new EspecieJpaController().find(pago.getComprobanteId());
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
            if (e.getSource().getClass().equals(JTextField.class)) {
                JTextField tf = (JTextField) e.getSource();
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
     * Carga el POSIBLE siguiente N°, en caso de multiples instancias no pincha ni corta.
     */
    private void setNextNumeroReRe() {
        Sucursal sucursal = getSelectedSucursalFromJD();
        Integer nextRe = jpaController.getNextNumero(sucursal, jdReRe.getCbTipo().getSelectedItem().toString());
        jdReRe.setTfCuarto(UTIL.AGREGAR_CEROS(sucursal.getPuntoVenta(), 4));
        jdReRe.setTfOcteto(UTIL.AGREGAR_CEROS(nextRe, 8));
    }

    private void doReportRecibo(Recibo recibo) throws Exception {
        if (recibo == null || recibo.getId() == null) {
            throw new MessageException("No hay " + CLASS_NAME + " seleccionado");
        }
        DetalleRecibo d = recibo.getDetalle().get(0);
        Cliente cliente = d.getFacturaVenta() != null ? d.getFacturaVenta().getCliente() : d.getNotaDebito().getCliente();
        List<GenericBeanCollection> cc = new ArrayList<>(recibo.getDetalle().size());
        for (DetalleRecibo dr : recibo.getDetalle()) {
            String comprobanteString = dr.getFacturaVenta() != null ? JGestionUtils.getNumeracion(dr.getFacturaVenta()) : JGestionUtils.getNumeracion(dr.getNotaDebito());
            cc.add(new GenericBeanCollection(comprobanteString, dr.getMontoEntrega()));
        }
        List<GenericBeanCollection> pp = new ArrayList<>(recibo.getPagos().size());
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
            } else if (object instanceof Especie) {
                Especie pago = (Especie) object;
                pp.add(new GenericBeanCollection("ES " + pago.getDescripcion(), pago.getImporte()));
            } else if (object instanceof DetalleCajaMovimientos) {
                DetalleCajaMovimientos pago = (DetalleCajaMovimientos) object;
                pp.add(new GenericBeanCollection("EF", pago.getMonto()));
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
     * Retorna todos los {@link Recibo} que contengan en su {@link DetalleRecibo} a factura.
     *
     * @param factura que deben contenedor los Recibos en su detalle
     * @return una lista de Recibo's
     */
    public List<Recibo> findByFactura(FacturaVenta factura) {
        List<DetalleRecibo> detalleReciboList = jpaController.findDetalleReciboBy(factura);
        List<Recibo> recibosList = new ArrayList<>(detalleReciboList.size());
        for (DetalleRecibo detalleRecibo : detalleReciboList) {
            if (!recibosList.contains(detalleRecibo.getRecibo())) {
                recibosList.add(detalleRecibo.getRecibo());
            }
        }
        return recibosList;
    }

    public List<Recibo> findByNotaDebito(NotaDebito notaDebito) {
        List<DetalleRecibo> detalleReciboList = jpaController.findDetalleReciboBy(notaDebito);
        List<Recibo> recibosList = new ArrayList<>(detalleReciboList.size());
        for (DetalleRecibo detalleRecibo : detalleReciboList) {
            if (!recibosList.contains(detalleRecibo.getRecibo())) {
                recibosList.add(detalleRecibo.getRecibo());
            }
        }
        return recibosList;
    }

    public void showABMRecibosNumeracionManual(Window owner) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.VENTA_NUMERACION_MANUAL);
        unlockedNumeracion = true;
        displayABMRecibos(owner, true, false);
        jdReRe.setTfOctetoEditable(true);
        jdReRe.setVisible(true);
    }

    public void showABMReciboAConciliar(Window owner) throws MessageException {
        displayABMRecibos(owner, true, false);
        jdReRe.setTitle("Recibo a conciliar");
        toConciliar = true;
        conciliando = false;
        jdReRe.getbImprimir().setEnabled(false);
        SwingUtil.setComponentsEnabled(jdReRe.getPanelAPagar().getComponents(), false, true, (Class<? extends Component>[]) null);
        jdReRe.setVisible(true);
    }
}
