package jgestion.controller;

import jgestion.controller.exceptions.MessageException;
import jgestion.controller.exceptions.MissingReportException;
import jgestion.entity.Caja;
import jgestion.entity.ChequePropio;
import jgestion.entity.ChequeTerceros;
import jgestion.entity.ComprobanteRetencion;
import jgestion.entity.CreditoProveedor;
import jgestion.entity.CtacteProveedor;
import jgestion.entity.CuentabancariaMovimientos;
import jgestion.entity.DetalleCajaMovimientos;
import jgestion.entity.DetalleRemesa;
import jgestion.entity.Especie;
import jgestion.entity.FacturaCompra;
import jgestion.entity.NotaCreditoProveedor;
import jgestion.entity.NotaDebitoProveedor;
import jgestion.entity.Proveedor;
import jgestion.entity.Remesa;
import jgestion.entity.RemesaPagos;
import jgestion.entity.Sucursal;
import generics.GenericBeanCollection;
import jgestion.gui.JDBuscadorReRe;
import jgestion.gui.JDReRe;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.persistence.EntityManager;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import jgestion.JGestionUtils;
import jgestion.jpa.controller.ChequePropioJpaController;
import jgestion.jpa.controller.ChequeTercerosJpaController;
import jgestion.jpa.controller.ComprobanteRetencionJpaController;
import jgestion.jpa.controller.CreditoProveedorJpaController;
import jgestion.jpa.controller.CtacteProveedorJpaController;
import jgestion.jpa.controller.CuentabancariaMovimientosJpaController;
import jgestion.jpa.controller.EspecieJpaController;
import jgestion.jpa.controller.FacturaCompraJpaController;
import jgestion.jpa.controller.NotaCreditoProveedorJpaController;
import jgestion.jpa.controller.NotaDebitoProveedorJpaController;
import jgestion.jpa.controller.ProveedorJpaController;
import jgestion.jpa.controller.RemesaJpaController;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utilities.general.NumberToLetterConverter;
import utilities.general.UTIL;
import utilities.gui.SwingUtil;
import utilities.general.EntityWrapper;
import utilities.general.TableExcelExporter;
import utilities.swing.components.FormatRenderer;
import utilities.swing.components.NumberRenderer;

/**
 *
 * @author Administrador
 */
public class RemesaController implements FocusListener {

    private final String CLASS_NAME = Remesa.class.getSimpleName();
    private JDReRe jdReRe;
    private CtacteProveedor selectedCtaCte;
    private JDBuscadorReRe buscador;
    private Remesa selectedRemesa;
    private static final Logger LOG = LogManager.getLogger();
    private final RemesaJpaController jpaController = new RemesaJpaController();
    private boolean unlockedNumeracion = false;
    private boolean toConciliar;
    private boolean conciliando;

    public RemesaController() {
    }

    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }

    public void displayRemesaAConciliar(Window owner) throws MessageException {
        displayABMRemesa(owner, true, false);
        jdReRe.setTitle("Recibos a conciliar");
        toConciliar = true;
        conciliando = false;
        jdReRe.getbImprimir().setEnabled(false);
        SwingUtil.setComponentsEnabled(jdReRe.getPanelAPagar().getComponents(), false, true, (Class<? extends Component>[]) null);
        jdReRe.setVisible(true);
    }

    public void displayABMRemesa(Window owner, boolean modal, boolean visible) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.COMPRA);
        jdReRe = new JDReRe(owner, modal);
        jdReRe.setUIRecibosProveedores();
        UTIL.loadComboBox(jdReRe.getCbSucursal(), JGestionUtils.getWrappedSucursales(new UsuarioHelper().getSucursales()), false);
        UTIL.loadComboBox(jdReRe.getCbCaja(), new UsuarioHelper().getCajas(true), false);
        UTIL.loadComboBox(jdReRe.getCbClienteProveedor(), JGestionUtils.getWrappedProveedores(new ProveedorJpaController().findAll()), true);
        jdReRe.getBtnBuscarCliente().addActionListener(evt -> {
            new ProveedorController().displaySelector(t -> {
                UTIL.setSelectedItem(jdReRe.getCbClienteProveedor(), t);
            });
        });
        UTIL.loadComboBox(jdReRe.getCbCtaCtes(), null, false);
        jdReRe.getbImprimir().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedRemesa != null && selectedRemesa.getId() != null) {
                    try {
                        doReportRemesa(selectedRemesa);
                    } catch (MissingReportException ex) {
                        JOptionPane.showMessageDialog(jdReRe, ex);
                    } catch (Exception ex) {
                        LOG.error("Error en reporte REMESA: " + selectedRemesa.toString(), ex);
                        JOptionPane.showMessageDialog(jdReRe, ex);
                    }
                }
            }

        });
        jdReRe.getbAnular().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    anular(selectedRemesa);
                    jdReRe.showMessage("Recibo anulado..", "Anulación", 1);
                    armarQuery();
                } catch (MessageException ex) {
                    ex.displayMessage(jdReRe);
                } catch (Exception ex) {
                    LOG.error("Anulando Remesa.id=" + selectedRemesa.getId(), ex);
                    jdReRe.showMessage(ex.getMessage(), "Algo salió mal", 2);
                }
            }
        });
        jdReRe.getbAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Remesa re = setAndPersist();
                    jdReRe.showMessage(jpaController.getEntityClass().getSimpleName() + "Nº" + JGestionUtils.getNumeracion(re, true) + " registrada..", null, 1);
                    doReportRemesa(re);
                    limpiarDetalle();
                    resetPanel();
                } catch (MessageException ex) {
                    ex.displayMessage(jdReRe);
                } catch (Exception ex) {
                    LOG.error(ex.getMessage(), ex);
                    jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 0);
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
                    cargarFacturasCtaCtesYNotasDebito(((EntityWrapper<Proveedor>) jdReRe.getCbClienteProveedor().getSelectedItem()).getEntity());
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
                    EntityWrapper<?> cbw = (EntityWrapper<?>) jdReRe.getCbCtaCtes().getSelectedItem();
                    selectedCtaCte = (CtacteProveedor) cbw.getEntity();
                    jdReRe.setTfImporte(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getImporte()));
                    jdReRe.setTfPagado(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getEntregado()));
                    jdReRe.setTfSaldo(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getImporte().subtract(selectedCtaCte.getEntregado())));
                    jdReRe.setTfEntrega(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getImporte().subtract(selectedCtaCte.getEntregado())));
                    jdReRe.getTfEntrega().setEditable(true);
                } catch (NullPointerException | ClassCastException ex) {
                    selectedCtaCte = null;
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
             * Efectivo, Cheque Propio, Cheque Tercero, Nota de Crédito, Retención
             */
            private void displayUIPagos(int formaPago) throws MessageException {

                if (formaPago == 0) {
                    displayABMEfectivo();
                } else if (formaPago == 1) {
                    displayABMChequePropio();
                } else if (formaPago == 2) {
                    displayABMChequeTerceros();
                } else if (formaPago == 3) {
                    displayNotaCreditoSelector();
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

    private void doReportRemesa(Remesa remesa) throws MissingReportException, JRException {
        DetalleRemesa d = remesa.getDetalle().get(0);
        Proveedor proveedor = d.getFacturaCompra() != null ? d.getFacturaCompra().getProveedor() : d.getNotaDebitoProveedor().getProveedor();
        List<GenericBeanCollection> cc = new ArrayList<>(remesa.getDetalle().size());
        for (DetalleRemesa dr : remesa.getDetalle()) {
            String comprobanteString = dr.getFacturaCompra() != null ? JGestionUtils.getNumeracion(dr.getFacturaCompra()) : JGestionUtils.getNumeracion(dr.getNotaDebitoProveedor());
            cc.add(new GenericBeanCollection(comprobanteString, dr.getMontoEntrega()));
        }
        List<GenericBeanCollection> pp = new ArrayList<>(remesa.getPagos().size());
        for (Object object : remesa.getPagosEntities()) {
            if (object instanceof ChequePropio) {
                ChequePropio pago = (ChequePropio) object;
                pp.add(new GenericBeanCollection("CHP " + pago.getBanco().getNombre() + " N°" + pago.getNumero(), pago.getImporte()));
            } else if (object instanceof ChequeTerceros) {
                ChequeTerceros pago = (ChequeTerceros) object;
                pp.add(new GenericBeanCollection("CH " + pago.getBanco().getNombre() + " N°" + pago.getNumero(), pago.getImporte()));
            } else if (object instanceof NotaCreditoProveedor) {
                NotaCreditoProveedor pago = (NotaCreditoProveedor) object;
                pp.add(new GenericBeanCollection(JGestionUtils.getNumeracion(pago, true), pago.getImporte()));
            } else if (object instanceof ComprobanteRetencion) {
                ComprobanteRetencion pago = (ComprobanteRetencion) object;
                pp.add(new GenericBeanCollection("RE " + pago.getNumero(), pago.getImporte()));
            } else if (object instanceof Especie) {
                Especie pago = (Especie) object;
                pp.add(new GenericBeanCollection("ES " + pago.getDescripcion(), pago.getImporte()));
            } else if (object instanceof DetalleCajaMovimientos) {
                DetalleCajaMovimientos pago = (DetalleCajaMovimientos) object;
                pp.add(new GenericBeanCollection("EF", pago.getMonto().negate()));
            } else if (object instanceof CuentabancariaMovimientos) {
                CuentabancariaMovimientos pago = (CuentabancariaMovimientos) object;
                int indexOf = pago.getDescripcion().indexOf(", ");
                pp.add(new GenericBeanCollection("TR " + pago.getDescripcion().substring(0, indexOf), pago.getDebito()));
            }
        }
        JRDataSource c = new JRBeanCollectionDataSource(cc);
        JRDataSource p = new JRBeanCollectionDataSource(pp);
        Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_Remesa_ctacte.jasper", "Recibo N°" + JGestionUtils.getNumeracion(remesa, true));
        r.addParameter("REMESA_ID", remesa.getId());
        r.addParameter("PROVEEDOR_ID", proveedor.getId());
        r.addCurrent_User();
        r.addMembreteParameter();
        r.addParameter("comprobantes", c);
        r.addParameter("pagos", p);
        r.addParameter("son_pesos", NumberToLetterConverter.convertNumberToLetter(remesa.getMonto(), false));
        r.viewReport();
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
            ChequePropio cheque = new ChequePropioController().initABM(jdReRe, false, ((EntityWrapper<Proveedor>) jdReRe.getCbClienteProveedor().getSelectedItem()).getEntity());
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

    private void displayNotaCreditoSelector() {
        try {
            NotaCreditoProveedor notaCredito = new NotaCreditoProveedorController().displayBuscador(jdReRe, false, ((EntityWrapper<Proveedor>) jdReRe.getCbClienteProveedor().getSelectedItem()).getEntity(), true);
            if (notaCredito != null) {
                DefaultTableModel dtm = jdReRe.getDtmPagos();
                for (int row = 0; row < dtm.getRowCount(); row++) {
                    if (dtm.getValueAt(row, 0) instanceof NotaCreditoProveedor) {
                        NotaCreditoProveedor old = (NotaCreditoProveedor) dtm.getValueAt(row, 0);
                        if (notaCredito.equals(old)) {
                            throw new MessageException("La nota de crédito  " + JGestionUtils.getNumeracion(old, true) + " ya está agregada");
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
        if (jdReRe.getCbTipo().getSelectedIndex() < 1) {
            throw new MessageException("Debe seleccionar el tipo/letra del comprobante");
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
                Remesa old = jpaController.find(((EntityWrapper<Sucursal>) jdReRe.getCbSucursal().getSelectedItem()).getEntity(), numero);
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

    private Remesa setAndPersist() throws MessageException, Exception {
        checkConstraints();
        Remesa remesa = new Remesa();
        remesa.setCaja((Caja) jdReRe.getCbCaja().getSelectedItem());
        remesa.setSucursal(((EntityWrapper<Sucursal>) jdReRe.getCbSucursal().getSelectedItem()).getEntity());
        remesa.setEstado(true);
        remesa.setFechaRemesa(jdReRe.getDcFechaReRe().getDate());
        remesa.setPagos(new ArrayList<>(jdReRe.getDtmPagos().getRowCount()));
        remesa.setDetalle(new ArrayList<>(jdReRe.getDtmAPagar().getRowCount()));
        remesa.setPorConciliar(toConciliar);
        remesa.setProveedor(((EntityWrapper<Proveedor>) jdReRe.getCbClienteProveedor().getSelectedItem()).getEntity());
        remesa.setUsuario(UsuarioController.getCurrentUser());

        DefaultTableModel dtm = (DefaultTableModel) jdReRe.getTableAPagar().getModel();
        BigDecimal monto = BigDecimal.ZERO;
        for (int rowIndex = 0; rowIndex < dtm.getRowCount(); rowIndex++) {
            DetalleRemesa detalle = (DetalleRemesa) dtm.getValueAt(rowIndex, 0);
            detalle.setRemesa(remesa);
            remesa.getDetalle().add(detalle);
            monto = monto.add(detalle.getMontoEntrega());
        }
        remesa.setMontoEntrega(monto.doubleValue());
        dtm = jdReRe.getDtmPagos();
        List<Object> pagos = new ArrayList<>(dtm.getRowCount());
        BigDecimal importePagado = BigDecimal.ZERO;
        for (int row = 0; row < dtm.getRowCount(); row++) {
            importePagado = importePagado.add((BigDecimal) dtm.getValueAt(row, 3));
            pagos.add(dtm.getValueAt(row, 0));
        }
        remesa.setPagosEntities(pagos);
        boolean asentarDiferenciaEnCaja = false;
        if (!remesa.isPorConciliar() || conciliando) {
            if (0 != jdReRe.getTfTotalAPagar().getText().compareTo(jdReRe.getTfTotalPagado().getText())) {
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(jdReRe, "El importe a pagar no coincide con el detalle de pagos."
                        + "\n¿Confirma que la diferencia ($" + UTIL.DECIMAL_FORMAT.format(remesa.getMonto() - importePagado.doubleValue()) + ") sea asentada en Caja?", "Confirmación de diferencia", JOptionPane.YES_NO_OPTION)) {
                    asentarDiferenciaEnCaja = true;
                } else {
                    throw new MessageException("Operación cancelada");
                }
            }
        }
        if (unlockedNumeracion || conciliando) {
            remesa.setNumero(Integer.valueOf(jdReRe.getTfOcteto()));
        } else {
            remesa.setNumero(jpaController.getNextNumero(remesa.getSucursal()));
        }
        if (conciliando) {
            jpaController.conciliar(remesa);
        } else {
            try {
                jpaController.persist(remesa);
            } catch (Exception ex) {
                LOG.error("Error fantástico de doble persistencia de remesa: " + remesa, ex);
                jpaController.persist(remesa);
            }
        }
        for (DetalleRemesa detalle : remesa.getDetalle()) {
            if (detalle.getFacturaCompra() != null) {
                actualizarMontoEntrega(detalle.getFacturaCompra(), detalle.getMontoEntrega());
            } else {
                actualizarMontoEntrega(detalle.getNotaDebitoProveedor(), detalle.getMontoEntrega());
            }
        }
        if (asentarDiferenciaEnCaja) {
            BigDecimal diferencia = BigDecimal.valueOf(remesa.getMonto() - importePagado.doubleValue());
            boolean debe = false;
            if (diferencia.doubleValue() < 0) {
                diferencia = diferencia.negate();
                debe = true;
            }
            DetalleRemesa d = remesa.getDetalle().get(0);
            Proveedor proveedor;
            if (d.getFacturaCompra() != null) {
                proveedor = d.getFacturaCompra().getProveedor();
            } else {
                proveedor = d.getNotaDebitoProveedor().getProveedor();
            }
            CreditoProveedor cp = new CreditoProveedor(null, debe, diferencia, "Recibo N° " + JGestionUtils.getNumeracion(remesa, true), proveedor);
            new CreditoProveedorJpaController().persist(cp);
        }
        return remesa;
    }

    private void actualizarMontoEntrega(NotaDebitoProveedor factu, BigDecimal monto) {
        CtacteProveedor ctacte = new CtacteProveedorJpaController().findBy(factu);
        actualizarMontoEntrega(ctacte, monto);
    }

    private void actualizarMontoEntrega(FacturaCompra factu, BigDecimal monto) {
        CtacteProveedor ctacte = new CtacteProveedorJpaController().findBy(factu);
        actualizarMontoEntrega(ctacte, monto);
    }

    private void actualizarMontoEntrega(CtacteProveedor ctacte, BigDecimal monto) {
        ctacte.setEntregado(ctacte.getEntregado().add(monto));
        if (ctacte.getImporte().compareTo(ctacte.getEntregado()) == 0) {
            ctacte.setEstado(Valores.CtaCteEstado.PAGADA.getId());
        }
        new CtacteProveedorJpaController().merge(ctacte);
    }

    private void limpiarDetalle() {
        jdReRe.limpiarDetalles();
    }

    private void addEntregaToDetalle() throws MessageException {
        Date comprobanteFecha;
        if (jdReRe.getDcFechaReRe().getDate() == null) {
            throw new MessageException("Debe especificar una fecha de recibo antes de agregar comprobaste a pagar.");
        }
        if (selectedCtaCte.getFactura() != null) {
            comprobanteFecha = selectedCtaCte.getFactura().getFechaCompra();
        } else {
            comprobanteFecha = selectedCtaCte.getNotaDebito().getFechaNotaDebito();
        }
        if (UTIL.compararIgnorandoTimeFields(jdReRe.getDcFechaReRe().getDate(), comprobanteFecha) < 0) {
            throw new MessageException("La fecha de recibo no puede ser anterior"
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

        FacturaCompra fc = null;
        NotaDebitoProveedor nd = null;
        if (entrega$.compareTo(selectedCtaCte.getImporte().subtract(selectedCtaCte.getEntregado())) == 1) {
            throw new MessageException("La entrega no puede ser mayor al Saldo restante ("
                    + selectedCtaCte.getImporte().subtract(selectedCtaCte.getEntregado()) + ")");
        }
        if (selectedCtaCte.getFactura() != null) {
            fc = selectedCtaCte.getFactura();
        } else {
            nd = selectedCtaCte.getNotaDebito();
        }
        for (int i = 0; i < jdReRe.getDtmAPagar().getRowCount(); i++) {
            DetalleRemesa oldRemesa = (DetalleRemesa) jdReRe.getDtmAPagar().getValueAt(i, 0);
            if ((Objects.nonNull(fc) && Objects.equals(oldRemesa.getFacturaCompra(), fc))
                    || (Objects.nonNull(nd) && nd.equals(oldRemesa.getNotaDebitoProveedor()))) {
                if (fc != null) {
                    throw new MessageException("La factura " + JGestionUtils.getNumeracion(fc) + " ya se ha agregada al detalle");
                } else {
                    throw new MessageException("La Nota de Débito " + JGestionUtils.getNumeracion(nd) + " ya se ha agregado al detalle");
                }
            }
        }
        DetalleRemesa remesa = new DetalleRemesa();
        remesa.setFacturaCompra(fc);
        remesa.setNotaDebitoProveedor(nd);
        remesa.setMontoEntrega(entrega$);
        jdReRe.getDtmAPagar().addRow(new Object[]{
            remesa,
            remesa.getFacturaCompra() != null ? JGestionUtils.getNumeracion(fc) : JGestionUtils.getNumeracion(nd),
            remesa.getMontoEntrega()
        });
        updateTotales();
    }

    private void updateTotales() {
        BigDecimal totalAPagar = BigDecimal.ZERO;
        BigDecimal totalPagado = BigDecimal.ZERO;
        DefaultTableModel dtm = (DefaultTableModel) jdReRe.getTableAPagar().getModel();
        for (int row = 0; row < dtm.getRowCount(); row++) {
            DetalleRemesa monto = (DetalleRemesa) dtm.getValueAt(row, 0);
            totalAPagar = totalAPagar.add(monto.getMontoEntrega());
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

    public void displayBuscador(Window owner, boolean modal, final boolean toAnular) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.COMPRA);
        if (toAnular) {
            UsuarioController.checkPermiso(PermisosController.PermisoDe.ANULAR_COMPROBANTES);
        }
        buscador = new JDBuscadorReRe(owner, "Buscador - Recibos", modal, "Proveedor", "Nº Recibo");
        if (toAnular) {
            buscador.setTitle(buscador.getTitle() + " para ANULAR");
        }
        buscador.getCheckAnulada().setEnabled(!toAnular);
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
        buscador.getjTable1().getColumnModel().getColumn(4).setCellRenderer(FormatRenderer.getDateRenderer());
        buscador.getjTable1().getColumnModel().getColumn(7).setCellRenderer(NumberRenderer.getDateTimeRenderer());
        UTIL.hideColumnTable(buscador.getjTable1(), 0);
        buscador.getjTable1().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    int rowIndex = buscador.getjTable1().getSelectedRow();
                    Integer id = (Integer) buscador.getjTable1().getModel().getValueAt(rowIndex, 0);
                    selectedRemesa = jpaController.find(id);
                    displayRemesaViewerMode(toAnular);
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
        buscador.getbImprimir().setVisible(false);
        buscador.getBtnToExcel().addActionListener((evt) -> {
            try {
                if (buscador.getjTable1().getRowCount() < 1) {
                    throw new MessageException("No hay info para exportar.");
                }
                File file = SwingUtil.showSaveDialogExcelFileChooser(buscador, null);
                if (file == null) {
                    return;
                }
                TableExcelExporter tee = new TableExcelExporter(file, buscador.getjTable1());
                tee.export();
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(buscador, "¿Abrir archivo generado?", null, JOptionPane.YES_NO_OPTION)) {
                    Desktop.getDesktop().open(file);
                }
            } catch (MessageException ex) {
                JOptionPane.showMessageDialog(buscador, ex.getMessage());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(buscador, "Algo salió mal: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                LOG.error(ex, ex);
            }
        });
        buscador.setLocationRelativeTo(owner);
        buscador.setVisible(true);
    }

    public void showBuscadorToConciliar(Window owner) throws MessageException {
        conciliando = true;
        displayBuscador(owner, true, false);
    }

    private void cargarFacturasCtaCtesYNotasDebito(Proveedor proveedor) {
        limpiarDetalle();
        List<CtacteProveedor> ccList = new CtacteProveedorController().findCtacteProveedorByProveedor(proveedor, Valores.CtaCteEstado.PENDIENTE);
        UTIL.loadComboBox(jdReRe.getCbCtaCtes(), JGestionUtils.getWrappedCtacteProveedor(ccList), false);
    }

    private void resetPanel() {
        jdReRe.getDcFechaReRe().setDate(null);
        jdReRe.getCbClienteProveedor().setSelectedIndex(0);
        SwingUtil.setComponentsEnabled(jdReRe.getComponents(), false, true);
    }

    @SuppressWarnings("unchecked")
    private void armarQuery() throws MessageException {
        StringBuilder query = new StringBuilder(" FROM " + Remesa.class.getSimpleName() + " o "
                + " WHERE o.id is not null "
                + " AND o.porConciliar=" + conciliando);

        long numero;
        //filtro por nº de ReRe
        if (buscador.getTfOcteto().length() > 0) {
            try {
                numero = Long.parseLong(buscador.getTfOcteto());
                query.append(" AND o.numero = ").append(numero);
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de recibo no válido");
            }
        }

        //filtro por nº de factura
        if (buscador.getTfFactu4().length() > 0 && buscador.getTfFactu8().length() > 0) {
            try {
                numero = Long.parseLong(buscador.getTfFactu4() + buscador.getTfFactu8());
                query.append(" AND f.numero = ").append(numero);
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de recibo no válido");
            }
        }
        if (buscador.getDcDesde() != null) {
            query.append(" AND o.").append("fechaRemesa").append(" >= '").append(UTIL.yyyy_MM_dd.format(buscador.getDcDesde())).append("'");
        }
        if (buscador.getDcHasta() != null) {
            query.append(" AND o.").append("fechaRemesa").append(" <= '").append(UTIL.yyyy_MM_dd.format(buscador.getDcHasta())).append("'");
        }
        if (buscador.getDcDesdeSistema() != null) {
            query.append(" AND o.").append("fechaCarga").append(" >= '").append(UTIL.yyyy_MM_dd.format(buscador.getDcDesdeSistema())).append("'");
        }
        if (buscador.getDcHastaSistema() != null) {
            query.append(" AND o.").append("fechaCarga").append(" <= '").append(UTIL.yyyy_MM_dd.format(buscador.getDcHastaSistema())).append("'");
        }

        if (buscador.getCbCaja().getSelectedIndex() > 0) {
            query.append(" AND o.caja.id = ").append(((Caja) buscador.getCbCaja().getSelectedItem()).getId());
        } else {
            query.append(" AND (");
            for (int i = 1; i < buscador.getCbCaja().getItemCount(); i++) {
                Caja caja = (Caja) buscador.getCbCaja().getItemAt(i);
                query.append(" o.caja.id=").append(caja.getId());
                if ((i + 1) < buscador.getCbCaja().getItemCount()) {
                    query.append(" OR ");
                }
            }
            query.append(")");
        }
        if (buscador.getCbSucursal().getSelectedIndex() > 0) {
            query.append(" AND o.sucursal.id = ").append(((EntityWrapper<Sucursal>) buscador.getCbSucursal().getSelectedItem()).getId());
        } else {
            query.append(" AND (");
            for (int i = 1; i < buscador.getCbSucursal().getItemCount(); i++) {
                EntityWrapper<Sucursal> cbw = (EntityWrapper<Sucursal>) buscador.getCbSucursal().getItemAt(i);
                query.append(" o.sucursal.id=").append(cbw.getId());
                if ((i + 1) < buscador.getCbSucursal().getItemCount()) {
                    query.append(" OR ");
                }
            }
            query.append(")");
        }
        if (buscador.getCheckAnulada().isSelected()) {
            query.append(" AND o.estado = false");
        } else {
            query.append(" AND o.estado = true");
        }
        if (buscador.getCbClieProv().getSelectedIndex() > 0) {
            query.append(" AND o.proveedor.id = "
            ).append(((EntityWrapper<Proveedor>) buscador.getCbClieProv().getSelectedItem()).getId());

        }
//        query.append(" GROUP BY o.id");
//        System.out.println("QUERY: " + query);
        cargarDtmBuscador(query.toString());
    }

    private void cargarDtmBuscador(String query) {
        DefaultTableModel dtm = (DefaultTableModel) buscador.getjTable1().getModel();
        dtm.setRowCount(0);
        List<Object[]> l = jpaController.findAttributes("SELECT o.id,"
                + " CONCAT("
                + " SUBSTRING(CAST((10000+o.sucursal.puntoVenta) AS TEXT), 2),"
                + " '-',"
                + " SUBSTRING(CAST(100000000+ o.numero AS TEXT), 2)"
                + "),"
                + " o.proveedor.nombre"
                + ", CAST(o." + "montoEntrega" + " AS NUMERIC(12,2))"
                + ", o." + "fechaRemesa"
                + ", o.caja.nombre, o.usuario.nick, o.fechaCarga "
                + query);
        if (l.isEmpty()) {
            JOptionPane.showMessageDialog(buscador, "La busqueda no produjo ningún resultado", null, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        for (Object[] remesa : l) {
            dtm.addRow(remesa);
        }
    }

    private void displayRemesaViewerMode(boolean toAnular) {
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
     * Setea la ventana de JDReRe de forma q solo se puedan ver los datos y detalles de la Remesa,
     * imprimir y ANULAR, pero NO MODIFICAR
     *
     * @param remesa
     */
    private void setComprobanteUI(Remesa remesa) throws MessageException {
        if (jdReRe == null) {
            displayABMRemesa(null, true, false);
        }
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
                r,
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
            throw new MessageException("Ya está anulada");
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
                throw new IllegalArgumentException("Forma Pago no válida:" + pago.getFormaPago());
            }
        }
        remesa.setPagosEntities(pagos);
        return pagos;
    }
}
