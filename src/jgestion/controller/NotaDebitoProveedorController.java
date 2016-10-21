package jgestion.controller;

import jgestion.controller.exceptions.MessageException;
import jgestion.entity.DetalleNotaDebitoProveedor;
import jgestion.entity.Iva;
import jgestion.entity.NotaDebitoProveedor;
import jgestion.entity.Proveedor;
import jgestion.gui.JDBuscadorReRe;
import jgestion.gui.JDNotaDebito;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import jgestion.JGestionUtils;
import jgestion.entity.CtacteProveedor;
import jgestion.jpa.controller.CtacteProveedorJpaController;
import jgestion.jpa.controller.IvaJpaController;
import jgestion.jpa.controller.NotaDebitoProveedorJpaController;
import jgestion.jpa.controller.ProveedorJpaController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
public class NotaDebitoProveedorController {

    private static final Logger LOG = LogManager.getLogger();
    private JDNotaDebito abm;
    private final NotaDebitoProveedorJpaController jpaController;
    private NotaDebitoProveedor EL_OBJECT;
    private static final int LIMITE_DE_ITEMS = 15;
    private JDBuscadorReRe buscador;
    private boolean viewMode = false;

    public NotaDebitoProveedorController() {
        jpaController = new NotaDebitoProveedorJpaController();
    }

    public void displayABM(Window owner, boolean modal) throws MessageException {
        initABM(owner, modal, true);
        abm.setVisible(true);
    }

    private void calcularSubTotal() {
        try {
            BigDecimal subTotal = new BigDecimal(abm.getTfImporte().getText());
            if (abm.getCbIVA().isEnabled()) {
                Iva iva = abm.getCbIVA().isEnabled() ? ((EntityWrapper<Iva>) abm.getCbIVA().getSelectedItem()).getEntity() : null;
                BigDecimal porcentaje = UTIL.getPorcentaje(subTotal, BigDecimal.valueOf(iva.getIva()));
                subTotal = subTotal.add(porcentaje);
            }
            abm.getTfSubTotal().setText(UTIL.PRECIO_CON_PUNTO.format(subTotal));
        } catch (Exception e) {
            //ignored..
        }
    }

    void initABM(Window owner, boolean modal, boolean loadDefaultData) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.COMPRA);
        abm = new JDNotaDebito(owner, modal, true);
        abm.setLocationRelativeTo(owner);
        abm.getCbCliente().addActionListener(new ActionListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                if (abm.getCbCliente().getItemCount() > 0) {
                    Proveedor p = new ProveedorJpaController().find((Integer) ((EntityWrapper<Proveedor>) abm.getCbCliente().getSelectedItem()).getId());
                    JGestionUtils.cargarComboTiposFacturas(abm.getCbFacturaTipo(), p);
                    boolean comprobanteA = abm.getCbFacturaTipo().getSelectedItem().toString().equalsIgnoreCase("A");
                    abm.getCbIVA().setEnabled(comprobanteA);
                }
            }
        });
        abm.getCbIVA().addActionListener(e -> calcularSubTotal());
        abm.getTfImporte().addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                calcularSubTotal();
            }
        });
        if (loadDefaultData) {
            UTIL.loadComboBox(abm.getCbCliente(), JGestionUtils.getWrappedProveedores(new ProveedorJpaController().findAllLite()), false);
            UTIL.loadComboBox(abm.getCbIVA(), JGestionUtils.getWrappedIva(new IvaJpaController().findAll()), false);
        }
        UTIL.getDefaultTableModel(abm.getjTable1(), new String[]{"DetalleNotaDebito.object", "Concepto", "Importe"},
                new int[]{1, 500, 100});
        abm.getjTable1().getColumnModel().getColumn(2).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        UTIL.hideColumnTable(abm.getjTable1(), 0);
        abm.getBtnADD().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String concepto = abm.getTfConcepto().getText().trim();
                    BigDecimal importe;
                    if (concepto.length() > 200) {
                        throw new MessageException("Concepto no puede superar los 200 caracteres. (No es una novela!)");
                    } else if (concepto.length() < 5) {
                        throw new MessageException("Muy pobre el concepto");
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
                    DetalleNotaDebitoProveedor d = new DetalleNotaDebitoProveedor(null, concepto, importe, iva);
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
        abm.getBtnAceptar().addActionListener(event -> {
            try {
                boolean editing = EL_OBJECT != null;
                setAndPersist();
                if (editing) {
                    abm.getBtnCancelar().doClick();
                } else {
                    EL_OBJECT = null;
                    resetUI();
                }
            } catch (MessageException ex) {
                ex.displayMessage(abm);
            } catch (Exception ex) {
                LOG.error("aceptando NotaDebito >" + EL_OBJECT, ex);
                JOptionPane.showMessageDialog(abm, "Algo salió mal: " + ex.getMessage(), null, JOptionPane.ERROR_MESSAGE);
            }
        });
        abm.getBtnCancelar().addActionListener(e -> {
            EL_OBJECT = null;
            abm.dispose();
        });
    }

    private void setAndPersist() throws MessageException {
        if (viewMode) {
        } else {
            getEntity();
            String msg = EL_OBJECT.getId() == null ? " registrada" : " modificada";
            if (EL_OBJECT.getId() == null) {
                jpaController.persist(EL_OBJECT);
                new CtacteProveedorController().addToCtaCte(EL_OBJECT);
            } else {
                jpaController.merge(EL_OBJECT);
                CtacteProveedorJpaController ccpDao = new CtacteProveedorJpaController();
                CtacteProveedor ccp = ccpDao.findBy(EL_OBJECT);
                ccp.setFechaCarga(EL_OBJECT.getFechaNotaDebito());
                ccp.setImporte(EL_OBJECT.getImporte());
                ccpDao.merge(ccp);
            }
            JOptionPane.showMessageDialog(null, JGestionUtils.getNumeracion(EL_OBJECT) + msg);
        }
    }

    private void resetUI() {
        SwingUtil.resetJComponets(abm.getPanelDatosFacturacion().getComponents());
        SwingUtil.resetJComponets(abm.getPanelDetalle().getComponents());
        DefaultTableModel dtm = (DefaultTableModel) abm.getjTable1().getModel();
        dtm.setRowCount(0);
        refreshResumen();
    }

    private NotaDebitoProveedor getEntity() throws MessageException {
        checkConstraints();
        if (EL_OBJECT == null) {
            EL_OBJECT = new NotaDebitoProveedor();
        } else {
            EL_OBJECT.getDetalle().clear();
        }
        EL_OBJECT.setProveedor(((EntityWrapper<Proveedor>) abm.getCbCliente().getSelectedItem()).getEntity());
        EL_OBJECT.setFechaNotaDebito(abm.getDcFechaFactura().getDate());
        EL_OBJECT.setTipo(abm.getCbFacturaTipo().getSelectedItem().toString().charAt(0));
        EL_OBJECT.setNumero(Long.valueOf(abm.getTfFacturaCuarto().getText() + UTIL.AGREGAR_CEROS(abm.getTfFacturaOcteto().getText(), 8)));
        String obs = abm.getTfObservacion().getText().trim();
        if (obs.isEmpty()) {
            obs = null;
        }
        EL_OBJECT.setObservacion(obs);
        EL_OBJECT.setAnulada(false);
        EL_OBJECT.setImporte(new BigDecimal(UTIL.parseToDouble(abm.getTfTotal().getText())));
        EL_OBJECT.setGravado(new BigDecimal(UTIL.parseToDouble(abm.getTfGravado().getText())));
        EL_OBJECT.setNoGravado(new BigDecimal(UTIL.parseToDouble(abm.getTfTotalNoGravado().getText())));
        EL_OBJECT.setIva10(new BigDecimal(UTIL.parseToDouble(abm.getTfTotalIVA105().getText())));
        EL_OBJECT.setIva21(new BigDecimal(UTIL.parseToDouble(abm.getTfTotalIVA21().getText())));
        EL_OBJECT.setOtrosIvas(new BigDecimal(UTIL.parseToDouble(abm.getTfTotalOtrosImps().getText())));
        EL_OBJECT.setImpuestosRecuperables(BigDecimal.ZERO);
        EL_OBJECT.setUsuario(UsuarioController.getCurrentUser());
        DefaultTableModel dtm = (DefaultTableModel) abm.getjTable1().getModel();
        List<DetalleNotaDebitoProveedor> detalle = new ArrayList<>(dtm.getRowCount());
        for (int row = 0; row < dtm.getRowCount(); row++) {
            DetalleNotaDebitoProveedor d = (DetalleNotaDebitoProveedor) dtm.getValueAt(row, 0);
            d.setNotaDebitoProveedor(EL_OBJECT);
            detalle.add(d);
        }
        EL_OBJECT.setDetalle(detalle);
        return EL_OBJECT;
    }

    private void checkConstraints() throws MessageException {
        if (abm.getDcFechaFactura().getDate() == null) {
            throw new MessageException("Fecha de comprobante no válida");
        }
        if (abm.getTfObservacion().getText().trim().length() > 100) {
            throw new MessageException("Observación no válida, no debe superar los 100 caracteres (no es una novela)");
        }
        if (abm.getjTable1().getRowCount() < 1) {
            throw new MessageException("El comprobante debe tener al menos un item.");
        } else if (abm.getjTable1().getRowCount() > LIMITE_DE_ITEMS) {
            throw new MessageException("El comprobante no puede tener mas de " + LIMITE_DE_ITEMS + " items.");
        }
        if (abm.getTfFacturaCuarto().getText().length() > 4) {
            throw new MessageException("Los primeros 4 números del comprobante no son válido: " + abm.getTfFacturaCuarto().getText());
        }

        if (abm.getTfFacturaCuarto().getText().length() > 8) {
            throw new MessageException("Número de comprobante no válido (máximo 8 dígitos)");
        }

        try {
            Long.valueOf(abm.getTfFacturaCuarto().getText());
        } catch (NumberFormatException e) {
            throw new MessageException("Los primeros 4 números del comprobante no son válido");
        }
        try {
            if (Integer.valueOf(abm.getTfFacturaOcteto().getText()) < 1) {
                throw new MessageException("Número de comprobante no válido");
            }
        } catch (NumberFormatException e) {
            throw new MessageException("Los primeros 4 números de la factura no válido");
        }
        if (Long.valueOf(abm.getTfFacturaCuarto().getText() + abm.getTfFacturaOcteto().getText()) < 100000001) {
            throw new MessageException("Número de comprobante no válido (no puede ser menor a 0001-00000001");
        }
        Proveedor proveedor = ((EntityWrapper<Proveedor>) abm.getCbCliente().getSelectedItem()).getEntity();
        char tipo = abm.getCbFacturaTipo().getSelectedItem().toString().charAt(0);
        Long numero = Long.valueOf(abm.getTfFacturaCuarto().getText() + UTIL.AGREGAR_CEROS(abm.getTfFacturaOcteto().getText(), 8));
        NotaDebitoProveedor old = jpaController.findBy(proveedor, tipo, numero);
        if (old != null && (EL_OBJECT == null || !old.getId().equals(EL_OBJECT.getId()))) {
            throw new MessageException("Ya existe la " + JGestionUtils.getNumeracion(old) + " de este proveedor"
                    + "\nFecha: " + UTIL.DATE_FORMAT.format(old.getFechaNotaDebito())
                    + "\nImporte: $" + UTIL.DECIMAL_FORMAT.format(old.getImporte()));
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
        BigDecimal subTotal = BigDecimal.ZERO;
        DefaultTableModel dtm = (DefaultTableModel) abm.getjTable1().getModel();
        for (int rowIndex = 0; rowIndex < dtm.getRowCount(); rowIndex++) {
            DetalleNotaDebitoProveedor d = (DetalleNotaDebitoProveedor) dtm.getValueAt(rowIndex, 0);
            BigDecimal cantidad = BigDecimal.ONE;
            BigDecimal importe = d.getImporte();
            BigDecimal alicuota = d.getIva() == null ? null : new BigDecimal(d.getIva().getIva());

            // Gravado (precioSinIVA + cantidad)
            if (alicuota != null && alicuota.intValue() > 0) {
                gravado = gravado.add(importe.multiply(cantidad));
            }
            /**
             * Se calcula sin aplicar ningún redondeo (se trabaja posiblemente mas de 2 decimales).
             */
//            BigDecimal sinRedondeo = importe.multiply(cantidad).multiply(alicuota.divide(new BigDecimal("100")));
            if (alicuota == null || alicuota.intValue() == 0) {
                noGravado = noGravado.add(cantidad.multiply(importe));
            } else if (alicuota.toString().equalsIgnoreCase("10.5")) {
                iva10 = iva10.add(cantidad.multiply(importe).multiply((alicuota.divide(new BigDecimal("100")))));
            } else if (alicuota.intValue() == 21) {
                iva21 = iva21.add(cantidad.multiply(importe).multiply((alicuota.divide(new BigDecimal("100")))));
            } else if (alicuota.compareTo(BigDecimal.ZERO) > 0) {
                otrosImps = otrosImps.add(cantidad.multiply(importe).multiply((alicuota.divide(new BigDecimal("100")))));
            } else {
                throw new IllegalArgumentException("IVA no determinado");
            }
//            LOG.debug("alicuota=" + alicuota + ", redondeo=" + sinRedondeo);

            subTotal = subTotal.add((BigDecimal) dtm.getValueAt(rowIndex, 2));
        }
        abm.getTfGravado().setText(UTIL.DECIMAL_FORMAT.format(gravado));
        abm.getTfTotalNoGravado().setText(UTIL.DECIMAL_FORMAT.format(noGravado));
        abm.getTfTotalDesc().setText(UTIL.DECIMAL_FORMAT.format(desc));
        abm.getTfTotalIVA105().setText(UTIL.DECIMAL_FORMAT.format(iva10));
        abm.getTfTotalIVA21().setText(UTIL.DECIMAL_FORMAT.format(iva21));
        abm.getTfTotalOtrosImps().setText(UTIL.DECIMAL_FORMAT.format(otrosImps));
//        redondeoTotal = gravado.add(iva10).add(iva21).add(otrosImps).subtract(redondeoTotal);
        abm.getTfDiferenciaRedondeo().setText(UTIL.DECIMAL_FORMAT.format(redondeoTotal));
        abm.getTfTotal().setText(UTIL.DECIMAL_FORMAT.format(subTotal));
        LOG.debug("Gravado:" + gravado + ", Desc.:" + desc + ", IVA105:" + iva10 + ", IVA21:" + iva21 + ", OtrosImp.:" + otrosImps + ", Redondeo:" + redondeoTotal);
    }

    private void addDetalle(DetalleNotaDebitoProveedor detalle) {
        DefaultTableModel dtm = (DefaultTableModel) abm.getjTable1().getModel();
        BigDecimal importe = (detalle.getImporte().add(UTIL.getPorcentaje(detalle.getImporte(), BigDecimal.valueOf(detalle.getIva().getIva()))));
        dtm.addRow(new Object[]{
            detalle, detalle.getConcepto(), importe
        });
    }

    private void cargarTablaBuscador(String query) {
        DefaultTableModel dtm = (DefaultTableModel) buscador.getjTable1().getModel();
        dtm.setRowCount(0);
        List<NotaDebitoProveedor> l = jpaController.findAll(query);
        for (NotaDebitoProveedor notaDebito : l) {
            dtm.addRow(new Object[]{
                notaDebito.getId(), // <--- no es visible
                JGestionUtils.getNumeracion(notaDebito),
                notaDebito.getProveedor().getNombre(),
                notaDebito.getImporte(),
                notaDebito.getFechaNotaDebito(),
                new CtacteProveedorJpaController().getSaldo(notaDebito),
                notaDebito.getUsuario().getNick(),
                notaDebito.getFechaCarga()
            });
        }
    }

    public void displayBuscador(Window owner, final boolean modal, final boolean toAnular) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.VENTA);
        if (toAnular) {
            UsuarioController.checkPermiso(PermisosController.PermisoDe.ANULAR_COMPROBANTES);
        }
        buscador = new JDBuscadorReRe(owner, "Buscador - Notas de Débito de Proveedores", modal, "Proveedor", "Nº Nota");
        buscador.getbExtra().setVisible(true);
        buscador.setParaNotaDebito();
        List<Proveedor> ll = new ProveedorJpaController().findAllLite();
        UTIL.loadComboBox(buscador.getCbClieProv(), JGestionUtils.getWrappedProveedores(ll), true);
        UTIL.loadComboBox(buscador.getCbSucursal(), JGestionUtils.getWrappedSucursales(new UsuarioHelper().getSucursales()), true);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"id", "Nº Nota Débito", "Proveedor", "Importe", "Fecha", "Saldo (rest.)", "Usuario", "Fecha (Sistema)"},
                new int[]{1, 90, 150, 50, 50, 80, 50, 80},
                new Class<?>[]{Integer.class, null, null, BigDecimal.class, Date.class, BigDecimal.class, null, Date.class});
        buscador.getjTable1().getColumnModel().getColumn(3).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        buscador.getjTable1().getColumnModel().getColumn(4).setCellRenderer(FormatRenderer.getDateRenderer());
        buscador.getjTable1().getColumnModel().getColumn(7).setCellRenderer(FormatRenderer.getDateTimeRenderer());
        UTIL.hideColumnTable(buscador.getjTable1(), 0);
        buscador.getjTable1().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    if (buscador.getjTable1().getSelectedRow() > -1) {
                        try {
                            EL_OBJECT = jpaController.find((Integer) buscador.getDtm().getValueAt(buscador.getjTable1().getSelectedRow(), 0));
                            viewMode = true;
                            displaABM(EL_OBJECT, toAnular, false);
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
        buscador.getbImprimir().addActionListener(event -> {
            try {
                cargarTablaBuscador(armarQuery());
//                    doReportFacturas(null);
//                } catch (MissingReportException ex) {
//                    JOptionPane.showMessageDialog(null, ex.getMessage());
            } catch (MessageException ex) {
                ex.displayMessage(buscador);
            } catch (Exception ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
                JOptionPane.showMessageDialog(null, ex.getMessage());
            }
        });
        //editar button
        buscador.getbExtra().addActionListener(event -> {
            if (buscador.getjTable1().getSelectedRow() > -1) {
                try {
                    EL_OBJECT = jpaController.find((Integer) buscador.getDtm().getValueAt(buscador.getjTable1().getSelectedRow(), 0));
                    if (EL_OBJECT.isAnulada()) {
                        throw new MessageException("No se puede editar un comprobante ANULADO");
                    }
                    CtacteProveedor ccp = new CtacteProveedorJpaController().findBy(EL_OBJECT);
                    if (ccp.getEntregado().compareTo(BigDecimal.ZERO) > 0) {
                        throw new MessageException("No se puede editar porque existen acreditaciones del comprobante."
                                + "\nVease Remesas");
                    }
                    viewMode = false;
                    displaABM(EL_OBJECT, false, true);
                    cargarTablaBuscador(armarQuery());
                } catch (MessageException ex) {
                    buscador.showMessage(ex.getMessage(), null, 0);
                }
            } else {
                buscador.showMessage("Seleccione la fila del comprobante que desea editar", null, JOptionPane.WARNING_MESSAGE);
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
        buscador.setLocationRelativeTo(owner);
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
            query.append(" AND o.fechaCarga <= '").append(UTIL.yyyy_MM_dd.format(buscador.getDcHastaSistema())).append("'");
        }

        if (buscador.getCbClieProv().getSelectedIndex() > 0) {
            query.append(" AND o.proveedor.id = ").append(((EntityWrapper<?>) buscador.getCbClieProv().getSelectedItem()).getId());
        }

        query.append(" ORDER BY o.fechaNotaDebito");
        LOG.trace("queryBuscador=" + query);
        return query.toString();
    }

    void displaABM(NotaDebitoProveedor notaDebito, boolean toAnular, boolean toEdit) throws MessageException {
        initABM(buscador, true, toEdit);
        SwingUtil.setComponentsEnabled(abm.getPanelDatosFacturacion().getComponents(), toEdit, true);
        SwingUtil.setComponentsEnabled(abm.getPanelDetalle().getComponents(), toEdit, true);
        abm.getBtnAnular().setVisible(toAnular);
        setComprobanteUI(notaDebito, toEdit);
        abm.setVisible(true);
    }

    private void setComprobanteUI(NotaDebitoProveedor o, boolean toEdit) {
        Proveedor c = o.getProveedor();
        if (toEdit) {
            UTIL.setSelectedItem(abm.getCbCliente(), o.getProveedor());
        } else {
            abm.getCbCliente().addItem(new EntityWrapper<>(c, c.getId(), c.getNombre()));
        }
        abm.getDcFechaFactura().setDate(o.getFechaNotaDebito());
        abm.getTfObservacion().setText(o.getObservacion());
        UTIL.loadComboBox(abm.getCbFacturaTipo(), FacturaCompraController.TIPOS_FACTURA, false);
        String numero = UTIL.AGREGAR_CEROS(o.getNumero(), 12);
        abm.getTfFacturaCuarto().setText(numero.substring(0, 4));
        abm.getTfFacturaOcteto().setText(numero.substring(4));
        DefaultTableModel dtm = (DefaultTableModel) abm.getjTable1().getModel();
        dtm.setRowCount(0);
        for (DetalleNotaDebitoProveedor d : o.getDetalle()) {
            addDetalle(d);
        }
        refreshResumen();
    }
}
