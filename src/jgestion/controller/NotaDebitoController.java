package jgestion.controller;

import java.awt.Color;
import jgestion.controller.exceptions.MessageException;
import jgestion.controller.exceptions.MissingReportException;
import jgestion.entity.Cliente;
import jgestion.entity.CtacteCliente;
import jgestion.entity.DetalleNotaDebito;
import jgestion.entity.Iva;
import jgestion.entity.NotaDebito;
import jgestion.entity.Sucursal;
import jgestion.gui.JDBuscadorReRe;
import jgestion.gui.JDNotaDebito;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import jgestion.JGestionUtils;
import jgestion.entity.FacturaElectronica;
import jgestion.jpa.controller.ClienteJpaController;
import jgestion.jpa.controller.FacturaElectronicaJpaController;
import jgestion.jpa.controller.IvaJpaController;
import jgestion.jpa.controller.NotaDebitoJpaController;
import net.sf.jasperreports.engine.JRException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import utilities.general.UTIL;
import utilities.gui.SwingUtil;
import utilities.general.EntityWrapper;
import utilities.general.TableExcelExporter;
import utilities.swing.components.FormatRenderer;
import utilities.swing.components.NumberRenderer;

/**
 *
 * @author FiruzzZ
 */
public class NotaDebitoController {

    private static final Logger LOG = LogManager.getLogger();
    private JDNotaDebito abm;
    private final NotaDebitoJpaController jpaController;
    private NotaDebito EL_OBJECT;
    private boolean unlockedNumeracion = false;
    private static final int LIMITE_DE_ITEMS = 15;
    private JDBuscadorReRe buscador;
    private boolean viewMode = false;
    private boolean editMode = false;

    public NotaDebitoController() {
        jpaController = new NotaDebitoJpaController();
    }

    public void initContenedor(Window owner, boolean modal) {
        initComprobanteUI(owner, modal, true);
        abm.setLocationRelativeTo(owner);
        abm.setVisible(true);
    }

    private void calcularSubTotalItem() {
        try {
            BigDecimal ca = new BigDecimal(abm.getTfCantidad().getText());
            BigDecimal subTotal = new BigDecimal(abm.getTfImporte().getText()).multiply(ca);
            @SuppressWarnings("unchecked")
            Iva iva = ((EntityWrapper<Iva>) abm.getCbIVA().getSelectedItem()).getEntity();
            BigDecimal porcentaje = UTIL.getPorcentaje(subTotal, BigDecimal.valueOf(iva.getIva()));
            subTotal = subTotal.add(porcentaje);
            abm.getTfSubTotal().setText(UTIL.PRECIO_CON_PUNTO.format(subTotal));
        } catch (Exception e) {
            //ignored..
        }
    }

    void initComprobanteUI(Window owner, boolean modal, boolean loadDefaultData) {
        abm = new JDNotaDebito(owner, modal, false);
        abm.getCbCliente().addActionListener(new ActionListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                if (abm.getCbCliente().getItemCount() > 0) {
                    try {
                        JGestionUtils.cargarComboTiposFacturas(abm.getCbFacturaTipo(), ((EntityWrapper<Cliente>) abm.getCbCliente().getSelectedItem()).getEntity());
                    } catch (MessageException ex) {
                        ex.displayMessage(abm);
                    }
                }
            }
        });
        abm.getCbSucursal().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sucursalSelectedActionPerformanceOnComboBox();
            }
        });
        abm.getCbIVA().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calcularSubTotalItem();
            }
        });
        abm.getTfImporte().addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                calcularSubTotalItem();
            }
        });
        abm.getTfImporte().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                calcularSubTotalItem();
            }
        });
        if (loadDefaultData) {
            UTIL.loadComboBox(abm.getCbCliente(), JGestionUtils.getWrappedClientes(new ClienteJpaController().findAll()), false);
            AutoCompleteDecorator.decorate(abm.getCbCliente());
            UTIL.loadComboBox(abm.getCbSucursal(), JGestionUtils.getWrappedSucursales(new UsuarioHelper().getSucursales()), false);
            UTIL.loadComboBox(abm.getCbIVA(), JGestionUtils.getWrappedIva(new IvaJpaController().findAll()), false);
        }
        UTIL.getDefaultTableModel(abm.getjTable1(), new String[]{"DetalleNotaDebito Object", "Cantidad", "Concepto", "Importe"},
                new int[]{1, 50, 500, 100},
                new Class<?>[]{null, Integer.class, null, null});
        abm.getjTable1().getColumnModel().getColumn(3).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        UTIL.hideColumnTable(abm.getjTable1(), 0);
        abm.getBtnADD().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String concepto = abm.getTfConcepto().getText().trim();
                    BigDecimal importe;
                    Integer cantidad;
                    if (concepto.length() > 200) {
                        throw new MessageException("Concepto no puede superar los 200 caracteres. (No es una novela!)");
                    } else if (concepto.length() < 5) {
                        throw new MessageException("Muy pobre el concepto");
                    }
                    try {
                        cantidad = Integer.valueOf(abm.getTfCantidad().getText().trim());
                        if (cantidad < 1) {
                            throw new MessageException("Cantidad no puede ser menor a 1");
                        }
                    } catch (NumberFormatException ex) {
                        throw new MessageException("Importe no válido");
                    }
                    try {
                        importe = new BigDecimal(abm.getTfImporte().getText().trim());
                        if (importe.intValue() < 0) {
                            throw new MessageException("Importe no puede ser menor a 0");
                        }
                    } catch (NumberFormatException ex) {
                        throw new MessageException("Importe no válido");
                    }
                    Iva iva = ((EntityWrapper<Iva>) abm.getCbIVA().getSelectedItem()).getEntity();
                    DetalleNotaDebito d = new DetalleNotaDebito(null, cantidad, concepto, importe, iva);
                    addDetalle(d);
                    refreshResumen();
                } catch (MessageException ex) {
                    ex.displayMessage(abm);
                }
            }
        });
        abm.getBtnDEL().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UTIL.removeSelectedRows(abm.getjTable1());
                refreshResumen();
            }
        });
        abm.getBtnAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (editMode) {
                        jpaController.merge(EL_OBJECT);
                    } else {
                        setAndPersist();
                    }
                    if (!viewMode) {
                        EL_OBJECT = null;
                        resetUI();
                    }
                } catch (MessageException ex) {
                    ex.displayMessage(abm);
                } catch (Exception ex) {
                    LOG.error("aceptando NotaDebito >" + EL_OBJECT, ex);
                }
            }
        });
        abm.getBtnCancelar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EL_OBJECT = null;
                abm.dispose();
            }
        });
    }

    private void setAndPersist() throws MessageException {
        if (viewMode) {
            if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(abm,
                    "¿Re-Imprimir comprobante?", jpaController.getEntityClass().getSimpleName(), JOptionPane.OK_CANCEL_OPTION)) {
                try {
                    doReportComprobante(EL_OBJECT);
                } catch (MissingReportException | JRException ex) {
                    JOptionPane.showMessageDialog(abm, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            NotaDebito newNotaDebito = getEntity();
            String msg = newNotaDebito.getId() == null ? " registrada" : " modificada";
            if (newNotaDebito.getId() == null) {
                try {
                    jpaController.persist(newNotaDebito);
                    if (newNotaDebito.getSucursal().isWebServices()) {
                        FacturaElectronica fee = FacturaElectronicaController.createFrom(newNotaDebito);
                        new FacturaElectronicaJpaController().persist(fee);
                        FacturaElectronicaController.initSolicitudCAEs();
                    }
                    new CtacteClienteController().addToCtaCte(newNotaDebito);
                    doReportComprobante(newNotaDebito);
                } catch (MissingReportException | JRException ex) {
                    JOptionPane.showMessageDialog(abm, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(abm, ex.getMessage(), "Algo salió mal", JOptionPane.ERROR_MESSAGE);
                    LOG.error("persistiendo " + newNotaDebito, ex);
                }
            } else {
            }
            JOptionPane.showMessageDialog(abm, JGestionUtils.getNumeracion(newNotaDebito) + msg);
        }
    }

    private void sucursalSelectedActionPerformanceOnComboBox() {
        if (abm.getCbSucursal().getSelectedIndex() == -1) {
            return;
        }
        @SuppressWarnings("unchecked")
        Sucursal s = ((EntityWrapper<Sucursal>) abm.getCbSucursal().getSelectedItem()).getEntity();
        if (editMode) {
            if (EL_OBJECT.getSucursal().equals(s)) {
                setNumeroComprobante(s, EL_OBJECT.getNumero());
            } else {
                setNumeroComprobante(s, jpaController.getNextNumero(s, abm.getCbFacturaTipo().getSelectedItem().toString().charAt(0)));
            }
        } else {
            setNumeroComprobante(s, jpaController.getNextNumero(s, abm.getCbFacturaTipo().getSelectedItem().toString().charAt(0)));
        }
    }

    /**
     * Agrega a <code>numero</code> tantos 0 (ceros) a la izq hasta completar los 12 dígitos (####
     * ########) y setea en la GUI
     *
     * @param numero
     */
    void setNumeroComprobante(Sucursal s, Integer numero) {
        abm.getTfFacturaCuarto().setText(UTIL.AGREGAR_CEROS(s.getPuntoVenta(), 4));
        abm.getTfFacturaOcteto().setText(UTIL.AGREGAR_CEROS(numero, 8));
    }

    private void resetUI() {
        SwingUtil.resetJComponets(abm.getPanelDatosFacturacion().getComponents());
        SwingUtil.resetJComponets(abm.getPanelDetalle().getComponents());
        DefaultTableModel dtm = (DefaultTableModel) abm.getjTable1().getModel();
        dtm.setRowCount(0);
        refreshResumen();
        sucursalSelectedActionPerformanceOnComboBox();
    }

    @SuppressWarnings("unchecked")
    private NotaDebito getEntity() throws MessageException {
        checkConstraints();
        NotaDebito o = new NotaDebito();
        o.setCliente(((EntityWrapper<Cliente>) abm.getCbCliente().getSelectedItem()).getEntity());
        o.setSucursal(((EntityWrapper<Sucursal>) abm.getCbSucursal().getSelectedItem()).getEntity());
        o.setFechaNotaDebito(abm.getDcFechaFactura().getDate());
        o.setTipo(abm.getCbFacturaTipo().getSelectedItem().toString().charAt(0));
        if (unlockedNumeracion) {
            o.setNumero(Integer.valueOf(abm.getTfFacturaOcteto().getText()));
        } else {
            o.setNumero(jpaController.getNextNumero(o.getSucursal(), o.getTipo()));
        }
        String obs = abm.getTfObservacion().getText().trim();
        if (obs.isEmpty()) {
            obs = null;
        }
        o.setObservacion(obs);
        o.setAnulada(false);
        o.setImporte(new BigDecimal(UTIL.parseToDouble(abm.getTfTotal().getText())));
        o.setGravado(new BigDecimal(UTIL.parseToDouble(abm.getTfGravado().getText())));
        o.setNoGravado(new BigDecimal(UTIL.parseToDouble(abm.getTfTotalNoGravado().getText())));
        o.setIva10(new BigDecimal(UTIL.parseToDouble(abm.getTfTotalIVA105().getText())));
        o.setIva21(new BigDecimal(UTIL.parseToDouble(abm.getTfTotalIVA21().getText())));
        o.setOtrosIvas(new BigDecimal(UTIL.parseToDouble(abm.getTfTotalOtrosImps().getText())));
        o.setImpuestosRecuperables(BigDecimal.ZERO);
        o.setUsuario(UsuarioController.getCurrentUser());
        DefaultTableModel dtm = (DefaultTableModel) abm.getjTable1().getModel();
        List<DetalleNotaDebito> detalle = new ArrayList<>(dtm.getRowCount());
        for (int row = 0; row < dtm.getRowCount(); row++) {
            DetalleNotaDebito d = (DetalleNotaDebito) dtm.getValueAt(row, 0);
            d.setNotaDebito(o);
            detalle.add(d);
        }
        o.setDetalle(detalle);
        return o;
    }

    private void checkConstraints() throws MessageException {
        Date fecha = abm.getDcFechaFactura().getDate();
        if (fecha == null) {
            throw new MessageException("Fecha de comprobante no válida");
        }
        Sucursal s = ((EntityWrapper<Sucursal>) abm.getCbSucursal().getSelectedItem()).getEntity();
        if (s.isWebServices()) {
            if (UTIL.getDaysBetween(fecha, JGestionUtils.getServerDate()) > 5) {
                throw new MessageException("La AFIP no permite informar comprobantes con mas de 5 días de antigüedad");
            }
        }
        if (abm.getTfObservacion().getText().trim().length() > 100) {
            throw new MessageException("Observación no válida, no debe superar los 100 caracteres (no es una novela)");
        }
        if (abm.getjTable1().getRowCount() < 1) {
            throw new MessageException("El comprobante debe tener al menos un item.");
        } else if (abm.getjTable1().getRowCount() > LIMITE_DE_ITEMS) {
            throw new MessageException("El comprobante no puede tener mas de " + LIMITE_DE_ITEMS + " items.");
        }
        if (unlockedNumeracion) {
            try {
                Integer octeto = Integer.valueOf(abm.getTfFacturaOcteto().getText());
                if (octeto < 1 && octeto > 99999999) {
                    throw new MessageException("Número de comprobante no válido, debe ser mayor a 0 y menor o igual a 99999999");
                }
                char letra = abm.getCbFacturaTipo().getSelectedItem().toString().charAt(0);
                NotaDebito old = jpaController.findBy(s, letra, octeto);
                if (old != null) {
                    throw new MessageException("Ya existe un registro del comprobante N° " + JGestionUtils.getNumeracion(old));
                }
            } catch (NumberFormatException numberFormatException) {
                throw new MessageException("Número de comprobante no válido, ingrese solo dígitos");
            }
        }
    }

    private void refreshResumen() {
        BigDecimal redondeoTotal = BigDecimal.ZERO;
        BigDecimal gravado = BigDecimal.ZERO;
        BigDecimal noGravado = BigDecimal.ZERO;
        BigDecimal iva10 = BigDecimal.ZERO;
        BigDecimal iva21 = BigDecimal.ZERO;
        BigDecimal otrosImps = BigDecimal.ZERO;
        BigDecimal desc = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        DefaultTableModel dtm = (DefaultTableModel) abm.getjTable1().getModel();
        for (int rowIndex = 0; rowIndex < dtm.getRowCount(); rowIndex++) {
            DetalleNotaDebito d = (DetalleNotaDebito) dtm.getValueAt(rowIndex, 0);
            BigDecimal alicuota = BigDecimal.valueOf(d.getIva().getIva());
            BigDecimal cantidad = BigDecimal.valueOf(d.getCantidad());
            BigDecimal precioUnitarioSinIVA = d.getImporte();
            BigDecimal subTotal = precioUnitarioSinIVA.multiply(cantidad);
            if (alicuota.intValue() > 0) {
                gravado = gravado.add(precioUnitarioSinIVA.multiply(cantidad));
                BigDecimal precioUnitarioConIva = subTotal
                        .multiply((alicuota.divide(new BigDecimal("100")).add(BigDecimal.ONE)))
                        .setScale(4, RoundingMode.HALF_UP); //subTotal
                redondeoTotal = redondeoTotal.add(precioUnitarioConIva);
            }
            BigDecimal iva = UTIL.getPorcentaje(subTotal, alicuota);
            if (alicuota.intValue() == 0) {
                noGravado = noGravado.add(subTotal);
            } else if (alicuota.toString().equalsIgnoreCase("10.5")) {
                iva10 = iva10.add(iva);
            } else if (alicuota.intValue() == 21) {
                iva21 = iva21.add(iva);
            } else if (alicuota.compareTo(BigDecimal.ZERO) > 0) {
                otrosImps = otrosImps.add(iva);
            } else {
                throw new IllegalArgumentException("IVA no determinado");
            }
            total = total.add(subTotal.add(iva));
        }
        abm.getTfGravado().setText(UTIL.DECIMAL_FORMAT.format(gravado));
        abm.getTfTotalNoGravado().setText(UTIL.DECIMAL_FORMAT.format(noGravado));
        abm.getTfTotalDesc().setText(UTIL.DECIMAL_FORMAT.format(desc));
        abm.getTfTotalIVA105().setText(UTIL.DECIMAL_FORMAT.format(iva10));
        abm.getTfTotalIVA21().setText(UTIL.DECIMAL_FORMAT.format(iva21));
        abm.getTfTotalOtrosImps().setText(UTIL.DECIMAL_FORMAT.format(otrosImps));
        redondeoTotal = gravado.add(iva10).add(iva21).add(otrosImps).subtract(redondeoTotal);
        abm.getTfDiferenciaRedondeo().setText(UTIL.DECIMAL_FORMAT.format(redondeoTotal));
        abm.getTfTotal().setText(UTIL.DECIMAL_FORMAT.format(total));
        LOG.debug("Tota=" + total + ", Gravado:" + gravado + ", Desc.:" + desc + ", IVA105:" + iva10 + ", IVA21:" + iva21 + ", OtrosImp.:" + otrosImps + ", Redondeo:" + redondeoTotal);
    }

    private void addDetalle(DetalleNotaDebito detalle) {
        DefaultTableModel dtm = (DefaultTableModel) abm.getjTable1().getModel();
        BigDecimal importe = (detalle.getImporte().add(UTIL.getPorcentaje(detalle.getImporte(), BigDecimal.valueOf(detalle.getIva().getIva()))));
        dtm.addRow(new Object[]{
            detalle, detalle.getCantidad(), detalle.getConcepto(), importe
        });
    }

    private void cargarTablaBuscador(String query) {
        DefaultTableModel dtm = (DefaultTableModel) buscador.getjTable1().getModel();
        dtm.setRowCount(0);
        List<NotaDebito> l = jpaController.findAll(query);
        for (NotaDebito notaDebito : l) {
            CtacteCliente ccc = new CtacteClienteController().findByNotaDebito(notaDebito.getId());
            dtm.addRow(new Object[]{
                notaDebito.getId(), // <--- no es visible
                JGestionUtils.getNumeracion(notaDebito),
                notaDebito.getCliente().getNombre(),
                notaDebito.getImporte(),
                notaDebito.getFechaNotaDebito(),
                notaDebito.getSucursal().getNombre(),
                notaDebito.getImporte().subtract(ccc.getEntregado()),
                notaDebito.getUsuario().getNick(),
                notaDebito.getFechaCarga()
            });
        }
    }

    public void initBuscador(Window frame, final boolean modal, final boolean toAnular) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.VENTA);
        if (toAnular) {
            UsuarioController.checkPermiso(PermisosController.PermisoDe.ANULAR_COMPROBANTES);
        }
        buscador = new JDBuscadorReRe(frame, "Buscador - Notas de Débito", modal, "Cliente", "Nº Nota");
        buscador.setParaNotaDebito();
        UTIL.loadComboBox(buscador.getCbClieProv(), new ClienteController().findAll(), true);
        UTIL.loadComboBox(buscador.getCbSucursal(), JGestionUtils.getWrappedSucursales(new UsuarioHelper().getSucursales()), true);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"id", "Nº Nota Débito", "Cliente", "Importe", "Fecha", "Sucursal", "Saldo", "Usuario", "Fecha (Sistema)"},
                new int[]{1, 90, 100, 50, 50, 80, 80, 50, 80},
                new Class<?>[]{Integer.class, null, null, BigDecimal.class, Date.class, null, BigDecimal.class, null, Date.class});
        buscador.getjTable1().getColumnModel().getColumn(3).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        buscador.getjTable1().getColumnModel().getColumn(4).setCellRenderer(FormatRenderer.getDateRenderer());
        buscador.getjTable1().getColumnModel().getColumn(6).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        buscador.getjTable1().getColumnModel().getColumn(8).setCellRenderer(FormatRenderer.getDateTimeRenderer());
        UTIL.hideColumnTable(buscador.getjTable1(), 0);
        buscador.getjTable1().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    if (buscador.getjTable1().getSelectedRow() > -1) {
                        try {
                            EL_OBJECT = jpaController.find((Integer) buscador.getDtm().getValueAt(buscador.getjTable1().getSelectedRow(), 0));
                            viewMode = true;
                            show(EL_OBJECT, toAnular);
                        } catch (MessageException ex) {
                            buscador.showMessage(ex.getMessage(), "Error de datos", 0);
                        }
                    }
                }
            }
        });
        if (toAnular) {
            buscador.getCheckAnulada().setEnabled(false);
        }
        buscador.getbBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String query = armarQuery();
                    cargarTablaBuscador(query);
                } catch (MessageException ex) {
                    buscador.showMessage(ex.getMessage(), "Buscador - " + jpaController.getEntityClass().getSimpleName(), 0);
                }
            }
        });
        buscador.getbImprimir().setEnabled(false);
        buscador.getbImprimir().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String jpql = armarQuery();
                    cargarTablaBuscador(jpql);
//                    doReportFacturas(null);
//                } catch (MissingReportException ex) {
//                    JOptionPane.showMessageDialog(null, ex.getMessage());
                } catch (MessageException ex) {
                    ex.displayMessage(buscador);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                    LOG.error(ex.getLocalizedMessage(), ex);
                }
            }
        });
        //editar button
        buscador.getbExtra().setEnabled(false);
        buscador.getbExtra().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (buscador.getjTable1().getSelectedRow() > -1) {
//                    try {
//                        setDatosToEdit();
//                        String query = armarQuery();
//                        cargarTablaBuscador(query);
//                    } catch (MessageException ex) {
//                        buscador.showMessage(ex.getMessage(), "Error de datos", 0);
//                    }
                } else {
                    buscador.showMessage("Seleccione la fila que corresponde a la Factura que desea editar", null, JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        buscador.getBtnToExcel().addActionListener((evt) -> {
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
                JOptionPane.showMessageDialog(buscador, "Algo salió mal: " + ex. getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                LOG.error(ex, ex);
            }
        });
        viewMode = true;
        buscador.setLocationRelativeTo(frame);
        buscador.setVisible(true);
    }

    @SuppressWarnings("unchecked")
    private String armarQuery() throws MessageException {
        StringBuilder query = new StringBuilder("SELECT o FROM " + jpaController.getEntityClass().getSimpleName() + " o"
                + " WHERE o.anulada = " + buscador.isCheckAnuladaSelected());

        long numero;
        if (buscador.getTfOcteto().length() > 0) {
            try {
                numero = Long.parseLong(buscador.getTfOcteto());
                query.append(" AND o.numero = ").append(numero);
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de " + jpaController.getEntityClass().getSimpleName() + " no válido");
            }
        }

        if (buscador.getDcDesde() != null) {
            query.append(" AND o.fechaNotaDebito >= '").append(UTIL.yyyy_MM_dd.format(buscador.getDcDesde())).append("'");
        }
        if (buscador.getDcHasta() != null) {
            query.append(" AND o.fechaNotaDebito <= '").append(UTIL.yyyy_MM_dd.format(buscador.getDcHasta())).append("'");
        }
        if (buscador.getDcDesdeSistema() != null) {
            query.append(" AND o.fechaCarga >= '").append(UTIL.yyyy_MM_dd.format(buscador.getDcDesdeSistema())).append("'");
        }
        if (buscador.getDcHastaSistema() != null) {
            query.append(" AND o.fechaCarga < '").append(UTIL.yyyy_MM_dd.format(UTIL.customDateByDays(buscador.getDcHastaSistema(), 1))).append("'");
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

        if (buscador.getCbClieProv().getSelectedIndex() > 0) {
            query.append(" AND o.cliente.id = ").append(((Cliente) buscador.getCbClieProv().getSelectedItem()).getId());
        }

        query.append(" ORDER BY o.fechaNotaDebito");
        LOG.trace("queryBuscador=" + query);
        return query.toString();
    }

    void show(NotaDebito notaDebito, boolean toAnular) throws MessageException {
        initComprobanteUI(buscador, true, false);
        SwingUtil.setComponentsEnabled(abm.getPanelDatosFacturacion().getComponents(), false, true, (Class<? extends Component>[]) null);
        SwingUtil.setComponentsEnabled(abm.getPanelDetalle().getComponents(), false, true, (Class<? extends Component>[]) null);
        abm.getBtnAnular().setVisible(toAnular);
        setPanel(notaDebito);
        abm.setVisible(true);
    }

    private void setPanel(NotaDebito notaDebito) {
        Sucursal s = notaDebito.getSucursal();
        Cliente c = notaDebito.getCliente();
        abm.getCbCliente().addItem(new EntityWrapper<>(c, c.getId(), c.getNombre()));
        abm.getCbSucursal().addItem(new EntityWrapper<>(s, s.getId(), s.getNombre()));
        abm.getDcFechaFactura().setDate(notaDebito.getFechaNotaDebito());
        abm.getTfObservacion().setText(notaDebito.getObservacion());
        FacturaElectronica fe = new FacturaElectronicaController().findBy(notaDebito);
        if (fe != null) {
            if (fe.getCae() == null) {
                abm.getTfCAE().setForeground(Color.RED);
                abm.getTfCAE().setText("PENDIENTE!");
            } else {
                abm.getTfCAE().setText(fe.getCae());
            }
        }
        try {
            //Los tipos de factura se tienen q cargar antes, sinó modifica el Nº de factura y muestra el siguiente
            //y no el de Factura seleccionada
            JGestionUtils.cargarComboTiposFacturas(abm.getCbFacturaTipo(), notaDebito.getCliente());
        } catch (MessageException ex) {
            ex.displayMessage(abm);
        }
        abm.getTfFacturaCuarto().setText(UTIL.AGREGAR_CEROS(notaDebito.getSucursal().getPuntoVenta(), 4));
        abm.getTfFacturaOcteto().setText(UTIL.AGREGAR_CEROS(notaDebito.getNumero(), 8));
        DefaultTableModel dtm = (DefaultTableModel) abm.getjTable1().getModel();
        dtm.setRowCount(0);
        for (DetalleNotaDebito detalleNotaDebito : notaDebito.getDetalle()) {
            addDetalle(detalleNotaDebito);
        }
        refreshResumen();
    }

    private void doReportComprobante(NotaDebito o) throws MissingReportException, JRException, MessageException {
        if (o.getSucursal().isWebServices()) {
            new FacturaElectronicaController().doReport(o);
        } else {
            Reportes r = new Reportes("JGestion_NotaDebito.jasper", jpaController.getEntityClass().getSimpleName() + " " + JGestionUtils.getNumeracion(o));
            r.addParameter("CBTE_ID", o.getId());
            r.addCurrent_User();
            r.printReport(true);
        }
    }

    void view(NotaDebito notaDebito) throws MessageException {
        viewMode = true;
        show(notaDebito, false);
    }
}
