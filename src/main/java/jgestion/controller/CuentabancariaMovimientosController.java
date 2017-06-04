package jgestion.controller;

import jgestion.entity.CuentaBancaria;
import jgestion.controller.exceptions.MessageException;
import jgestion.entity.Banco;
import jgestion.entity.CuentabancariaMovimientos;
import jgestion.entity.OperacionesBancarias;
import jgestion.gui.JDABM;
import jgestion.gui.JDConciliacionBancaria;
import jgestion.gui.JDCuentabancariaManager;
import jgestion.gui.PanelOperacionBancariaDeposito;
import jgestion.gui.PanelOperacionBancariaTransferencia;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import jgestion.JGestionUtils;
import jgestion.jpa.controller.CuentabancariaMovimientosJpaController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utilities.general.UTIL;
import utilities.gui.SwingUtil;
import utilities.general.EntityWrapper;

/**
 *
 * @author FiruzzZ
 */
public class CuentabancariaMovimientosController {

    private static final Logger LOG = LogManager.getLogger();
    private CuentabancariaMovimientosJpaController jpaController;
    private JDCuentabancariaManager manager;
    private JDABM abm;
    private CuentabancariaMovimientos EL_OBJECT;

    public CuentabancariaMovimientosController() {
        jpaController = new CuentabancariaMovimientosJpaController();
    }

    public JDialog getManager(Window owner) {
        manager = new JDCuentabancariaManager(owner);
        manager.getBtnBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (manager.getCbCuentabancaria().getSelectedIndex() > 0) {
                    CuentaBancaria cb = new CuentaBancaria();
                    cb.setId((Integer) ((EntityWrapper<?>) manager.getCbCuentabancaria().getSelectedItem()).getId());
                    BigDecimal saldo = jpaController.getSaldo(cb);
                    manager.getTfSaldoTotal().setText(UTIL.DECIMAL_FORMAT.format(saldo));
                } else {
                    manager.getTfSaldoTotal().setText(null);
                }
                String jpql = armarQuery();
                List<CuentabancariaMovimientos> l = jpaController.findAll(jpql);
                cargarManagerTable(l);
            }
        });
        manager.getBtnAgregar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                @SuppressWarnings("unchecked")
                OperacionesBancarias op = ((EntityWrapper<OperacionesBancarias>) manager.getCbOperacionesBancarias().getSelectedItem()).getEntity();
                if (op.getNombre().equalsIgnoreCase("DEPÓSITO")) {
                    displayDepositoGUI();
                } else if (op.getNombre().equalsIgnoreCase("EXTRACCIÓN")) {
                    displayExtraccionGUI();
                } else if (op.getNombre().equalsIgnoreCase("TRANSFERENCIA")) {
                    displayTransferenciaGUI(manager);
                }
            }
        });
        return manager;
    }

    private void displayDepositoGUI() {
        final PanelOperacionBancariaDeposito panelDeposito = new PanelOperacionBancariaDeposito();
        abm = new JDABM(manager, "Depósito", true, panelDeposito);
        abm.getbAceptar().addActionListener(new ActionListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                try {
                    CuentaBancaria cb;
                    BigDecimal monto;
                    try {
                        cb = ((EntityWrapper<CuentaBancaria>) panelDeposito.getCbCuentabancaria().getSelectedItem()).getEntity();
                    } catch (ClassCastException ex) {
                        throw new MessageException("Cuenta bancanria no válida");
                    }
                    try {
                        monto = new BigDecimal(panelDeposito.getTfDebe().getText());
                        if (monto.compareTo(BigDecimal.ZERO) != 1) {
                            throw new MessageException("Importe no válido, debe ser mayor a cero");
                        }
                    } catch (Exception ex) {
                        throw new MessageException("Importe no válido, ingrese solo números y utilice el punto como separador decimal");
                    }
                    String descrip = panelDeposito.getTfDescripcionMov().getText().trim();
                    if (descrip.isEmpty()) {
                        throw new MessageException("Descripción de depósito no válida");
                    }
                    Date fechaOP = panelDeposito.getDcFechaOperacion().getDate();
                    Date fechaCre = panelDeposito.getDcFechaCreditoDebito().getDate();
                    OperacionesBancarias op = ((EntityWrapper<OperacionesBancarias>) manager.getCbOperacionesBancarias().getSelectedItem()).getEntity();
                    CuentabancariaMovimientos cbm = new CuentabancariaMovimientos(fechaOP, descrip, fechaCre, monto, BigDecimal.ZERO, false, UsuarioController.getCurrentUser(), op, cb, null, null, false);
                    new CuentabancariaMovimientosJpaController().persist(cbm);
                    abm.showMessage("operación n° " + cbm.getId() + " realizada", null, 1);
                    abm.dispose();

                } catch (MessageException ex) {
                    ex.displayMessage(abm);
                }
            }
        });
        abm.getbCancelar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abm.dispose();
            }
        });
        abm.setLocationRelativeTo(manager);
        abm.setVisible(true);
    }

    private void displayExtraccionGUI() {
        final PanelOperacionBancariaDeposito panelDeposito = new PanelOperacionBancariaDeposito();
        abm = new JDABM(manager, "Extracción", true, panelDeposito);
        abm.getbAceptar().addActionListener(new ActionListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                try {
                    CuentaBancaria cb;
                    BigDecimal monto;
                    try {
                        cb = ((EntityWrapper<CuentaBancaria>) panelDeposito.getCbCuentabancaria().getSelectedItem()).getEntity();
                    } catch (ClassCastException ex) {
                        throw new MessageException("Cuenta bancanria no válida");
                    }
                    try {
                        monto = new BigDecimal(panelDeposito.getTfDebe().getText());
                        if (monto.compareTo(BigDecimal.ZERO) != 1) {
                            throw new MessageException("Importe no válido, debe ser mayor a cero");
                        }
                    } catch (Exception ex) {
                        throw new MessageException("Importe no válido, ingrese solo números y utilice el punto como separador decimal");
                    }
                    String descrip = panelDeposito.getTfDescripcionMov().getText().trim();
                    if (descrip.isEmpty()) {
                        throw new MessageException("Descripción de extracción no válida");
                    }
                    Date fechaOP = panelDeposito.getDcFechaOperacion().getDate();
                    Date fechaCre = panelDeposito.getDcFechaCreditoDebito().getDate();
                    OperacionesBancarias op = ((EntityWrapper<OperacionesBancarias>) manager.getCbOperacionesBancarias().getSelectedItem()).getEntity();
                    CuentabancariaMovimientos cbm = new CuentabancariaMovimientos(fechaOP, descrip, fechaCre, BigDecimal.ZERO, monto, false, UsuarioController.getCurrentUser(), op, cb, null, null, false);
                    new CuentabancariaMovimientosJpaController().persist(cbm);
                    abm.showMessage("operación n° " + cbm.getId() + " realizada", null, 1);
                    abm.dispose();
                } catch (MessageException ex) {
                    ex.displayMessage(abm);
                }
            }
        });
        abm.getbCancelar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abm.dispose();
            }
        });
        abm.setLocationRelativeTo(manager);
        abm.setVisible(true);
    }

    private void displayTransferenciaGUI(Window owner) {
        final PanelOperacionBancariaTransferencia panelTransf = new PanelOperacionBancariaTransferencia();
        panelTransf.getTfMonto().addFocusListener(SwingUtil.getCurrencyFormatterFocusListener());
        abm = new JDABM(owner, "Transferencia", true, panelTransf);
        abm.getbAceptar().addActionListener(new ActionListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                try {
                    CuentaBancaria origen;
                    CuentabancariaMovimientos cbmDestino = null;
                    CuentaBancaria destino;
                    String descripDestino;
                    BigDecimal monto;
                    try {
                        origen = ((EntityWrapper<CuentaBancaria>) panelTransf.getCbCuentabancaria().getSelectedItem()).getEntity();
                    } catch (ClassCastException ex) {
                        throw new MessageException("Cuenta bancaria origen no válida");
                    }

                    try {
                        monto = new BigDecimal(UTIL.parseToDouble(panelTransf.getTfMonto().getText()));
                        if (monto.compareTo(BigDecimal.ZERO) != 1) {
                            throw new MessageException("Importe no válido, debe ser mayor a cero");
                        }
                    } catch (Exception ex) {
                        throw new MessageException("Importe no válido, ingrese solo números y utilice el punto como separador decimal");
                    }
                    String descripOrigen = panelTransf.getTfDescripcionMov().getText().trim();
                    if (descripOrigen.isEmpty() && !panelTransf.getRbPropia().isSelected()) {
                        throw new MessageException("Si la transferencia es Externa, debe ingresar una descripción de la misma");
                    }
                    Date fechaOP = panelTransf.getDcFechaOperacion().getDate();
                    OperacionesBancarias op = ((EntityWrapper<OperacionesBancarias>) manager.getCbOperacionesBancarias().getSelectedItem()).getEntity();
                    CuentabancariaMovimientos cbmOrigen = new CuentabancariaMovimientos(fechaOP, descripOrigen, null, BigDecimal.ZERO, monto, false, UsuarioController.getCurrentUser(), op, origen, null, null, false);
                    if (panelTransf.getRbPropia().isSelected()) {
                        try {
                            destino = ((EntityWrapper<CuentaBancaria>) panelTransf.getCbCuentabancariaDestino().getSelectedItem()).getEntity();
                            if (origen.equals(destino)) {
                                throw new MessageException("Las Cuentas bancarias Origen y Destino no pueden ser la misma.");
                            }
                        } catch (ClassCastException ex) {
                            throw new MessageException("Cuenta bancaria destino no válida");
                        }
                        descripOrigen = destino.getBanco().getNombre() + " N° " + destino.getNumero() + " (" + descripOrigen + ")";
                        cbmOrigen.setDescripcion(descripOrigen);
                        descripDestino = origen.getBanco().getNombre() + " N° " + origen.getNumero();
                        cbmDestino = new CuentabancariaMovimientos(fechaOP, descripDestino, null, monto, BigDecimal.ZERO, false, UsuarioController.getCurrentUser(), op, destino, null, null, false);
                    }
                    new CuentabancariaMovimientosJpaController().persist(cbmOrigen);
                    String x = "Operación de Transferencia n° " + cbmOrigen.getId() + " realizada";
                    if (cbmDestino != null) {
                        new CuentabancariaMovimientosJpaController().persist(cbmDestino);
                        x = "\nOperación de Transferencia (depósito) n° " + cbmDestino.getId() + " realizada";
                    }
                    JOptionPane.showMessageDialog(abm, x);
                    abm.dispose();
                } catch (MessageException ex) {
                    ex.displayMessage(abm);
                }
            }
        });
        abm.getbCancelar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abm.dispose();
            }
        });
        abm.setLocationRelativeTo(owner);
        abm.setVisible(true);
    }

    /**
     *
     * @param owner
     * @param destinatario Quien va recibir la transacción
     * @return
     */
    CuentabancariaMovimientos displayTransferenciaProveedor(Window owner, final String destinatario) {
        final PanelOperacionBancariaTransferencia panelTransf = new PanelOperacionBancariaTransferencia();
        abm = new JDABM(owner, "Transferencia a Proveedor", true, panelTransf);
        panelTransf.getRbPropia().setEnabled(false);
        panelTransf.getRbPropia().setSelected(false);
        panelTransf.getRbExterna().setEnabled(false);
        panelTransf.getRbExterna().setSelected(true);
        panelTransf.getTfDescripcionMov().setText(destinatario);
        abm.getbAceptar().addActionListener(new ActionListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                try {
                    CuentaBancaria origen;
                    BigDecimal importe;
                    try {
                        origen = ((EntityWrapper<CuentaBancaria>) panelTransf.getCbCuentabancaria().getSelectedItem()).getEntity();
                    } catch (ClassCastException ex) {
                        throw new MessageException("Cuenta bancaria origen no válida");
                    }

                    try {
                        importe = new BigDecimal(panelTransf.getTfMonto().getText());
                        if (importe.compareTo(BigDecimal.ZERO) != 1) {
                            throw new MessageException("Importe no válido, debe ser mayor a cero");
                        }
                    } catch (Exception ex) {
                        throw new MessageException("Importe no válido, ingrese solo números y utilice el punto como separador decimal");
                    }
                    String cuenta = panelTransf.getTfCuentaExterna().getText().trim();
                    if (cuenta.isEmpty() || cuenta.length() > 22) {
                        throw new MessageException("Número de cuenta no válido, ingrese solo números (hasta 22 dígitos)");
                    }
                    Banco banco = ((EntityWrapper<Banco>) panelTransf.getCbDestinoBancosExternos().getSelectedItem()).getEntity();
                    String d = panelTransf.getTfDescripcionMov().getText().trim();
                    String descrip = banco.getNombre() + " N° " + cuenta + (d.isEmpty() ? "" : ", " + d);
                    if (descrip.isEmpty()) {
                        throw new MessageException("Descripción de transferencia no válida");
                    }
                    Date fechaOP = panelTransf.getDcFechaOperacion().getDate();
                    OperacionesBancarias op = new OperacionesBancariasController().getOperacion(OperacionesBancariasController.TRANSFERENCIA);
                    CuentabancariaMovimientos cbmEXTRACCION = new CuentabancariaMovimientos(fechaOP, descrip, null, BigDecimal.ZERO, importe, false, UsuarioController.getCurrentUser(), op, origen, null, null, false);
                    EL_OBJECT = cbmEXTRACCION;
                    abm.dispose();
                } catch (MessageException ex) {
                    ex.displayMessage(abm);
                }
            }
        });
        abm.getbCancelar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EL_OBJECT = null;
                abm.dispose();
            }
        });
        abm.setLocationRelativeTo(owner);
        abm.setVisible(true);
        return EL_OBJECT;
    }

    /**
     *
     * @param owner
     * @param transaccionOrigen nombre del Cliente que realiza la transacción
     * @return
     */
    public CuentabancariaMovimientos displayTransferenciaCliente(Window owner, final String transaccionOrigen) {
        final PanelOperacionBancariaTransferencia panelTransf = new PanelOperacionBancariaTransferencia();
        abm = new JDABM(owner, "Transferencia de Cliente", true, panelTransf);
        panelTransf.getRbPropia().setEnabled(false);
        panelTransf.getRbExterna().setEnabled(false);
        panelTransf.getRbExterna().setSelected(true);
        panelTransf.getTfDescripcionMov().setText(transaccionOrigen);
        panelTransf.getTfDescripcionMov().setEnabled(false);
        abm.getbAceptar().addActionListener(new ActionListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                try {
                    CuentaBancaria destino;
                    BigDecimal importe;
                    try {
                        destino = ((EntityWrapper<CuentaBancaria>) panelTransf.getCbCuentabancaria().getSelectedItem()).getEntity();
                    } catch (ClassCastException ex) {
                        throw new MessageException("Cuenta bancaria origen no válida");
                    }

                    try {
                        importe = new BigDecimal(panelTransf.getTfMonto().getText());
                        if (importe.compareTo(BigDecimal.ZERO) != 1) {
                            throw new MessageException("Importe no válido, debe ser mayor a cero");
                        }
                    } catch (Exception ex) {
                        throw new MessageException("Importe no válido, ingrese solo números y utilice el punto como separador decimal");
                    }
                    String cuenta = panelTransf.getTfCuentaExterna().getText().trim();
                    if (cuenta.isEmpty() || cuenta.length() > 22) {
                        throw new MessageException("Número de cuenta no válido, ingrese solo números (hasta 22 dígitos)");
                    }
                    Banco banco = ((EntityWrapper<Banco>) panelTransf.getCbDestinoBancosExternos().getSelectedItem()).getEntity();
                    String descrip = banco.getNombre() + " N°" + cuenta + ", " + panelTransf.getTfDescripcionMov().getText().trim();

//                    if (descrip.isEmpty()) {
//                        throw new MessageException("Descripción de transferencia no válida");
//                    }
                    Date fechaOP = panelTransf.getDcFechaOperacion().getDate();
                    OperacionesBancarias op = new OperacionesBancariasController().getOperacion(OperacionesBancariasController.TRANSFERENCIA);
                    CuentabancariaMovimientos cbm = new CuentabancariaMovimientos(fechaOP, descrip, null, importe, BigDecimal.ZERO, false, UsuarioController.getCurrentUser(), op, destino, null, null, false);
                    EL_OBJECT = cbm;
                    abm.dispose();
                } catch (MessageException ex) {
                    ex.displayMessage(abm);
                }
            }
        });
        abm.getbCancelar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EL_OBJECT = null;
                abm.dispose();
            }
        });
        abm.setLocationRelativeTo(owner);
        abm.setVisible(true);
        return EL_OBJECT;
    }

    private String armarQuery() {
        StringBuilder query = new StringBuilder("SELECT o FROM " + jpaController.getEntityClass().getSimpleName() + " o "
                + "WHERE o.id is not null ");
        if (manager.getCbBancos().getSelectedIndex() > 0) {
            query.append(" AND o.cuentaBancaria.banco.id=").append(((EntityWrapper<?>) manager.getCbBancos().getSelectedItem()).getId());
        } else {
            query.append(" AND (");
            for (int i = 1; i < manager.getCbBancos().getItemCount(); i++) {
                query.append(" o.cuentaBancaria.banco.id=").append(((EntityWrapper<?>) manager.getCbBancos().getItemAt(i)).getId());
                if ((i + 1) < manager.getCbBancos().getItemCount()) {
                    query.append(" OR ");
                }
            }
            query.append(")");
        }
        if (manager.getCbCuentabancaria().getSelectedIndex() > 0) {
            query.append(" AND o.cuentaBancaria.id=").append(((EntityWrapper<?>) manager.getCbCuentabancaria().getSelectedItem()).getId());
        } else {
            //if (no seleccionó banco) => cuentas bancarias está vacía
            if (manager.getCbBancos().getSelectedIndex() > 0) {
                query.append(" AND (");
                for (int i = 1; i < manager.getCbCuentabancaria().getItemCount(); i++) {
                    query.append(" o.cuentaBancaria.id=").append(((EntityWrapper<?>) manager.getCbCuentabancaria().getItemAt(i)).getId());
                    if ((i + 1) < manager.getCbCuentabancaria().getItemCount()) {
                        query.append(" OR ");
                    }
                }
                query.append(")");
            }
        }
        if (manager.getCbOperacionesBancariasFiltro().getSelectedIndex() > 0) {
            query.append(" AND o.operacionesBancarias.id=").append(((EntityWrapper<?>) manager.getCbOperacionesBancariasFiltro().getSelectedItem()).getId());
        }
        if (manager.getDcDesde() != null || manager.getDcHasta() != null) {
            if (manager.getRbOperacion()) {
                if (manager.getDcDesde() != null) {
                    query.append(" AND o.fechaOperacion >='").append(UTIL.yyyy_MM_dd.format(manager.getDcDesde())).append("'");
                }
                if (manager.getDcHasta() != null) {
                    query.append(" AND o.fechaOperacion <='").append(UTIL.yyyy_MM_dd.format(manager.getDcHasta())).append("'");
                }
            } else {
                if (manager.getDcDesde() != null) {
                    query.append(" AND o.fechaCreditoDebito >='").append(UTIL.yyyy_MM_dd.format(manager.getDcDesde())).append("'");
                }
                if (manager.getDcHasta() != null) {
                    query.append(" AND o.fechaCreditoDebito <='").append(UTIL.yyyy_MM_dd.format(manager.getDcHasta())).append("'");
                }
            }
        }
        LOG.debug(query.toString());
        return query.toString();
    }

    private void cargarManagerTable(List<CuentabancariaMovimientos> l) {
        DefaultTableModel dtm = (DefaultTableModel) manager.getjTable1().getModel();
        dtm.setRowCount(0);
        for (CuentabancariaMovimientos o : l) {
            dtm.addRow(new Object[]{
                o.getId(),
                o.getDescripcion(),
                o.getCredito(),
                o.getDebito(),
                o.getFechaCreditoDebito(),
                o.getOperacionesBancarias().getNombre(),
                o.getFechaOperacion(),
                o.getUsuario(),
                o.getFechaSistema()
            });
        }
    }

    public JDialog getConciliacion(Window owner) throws MessageException {
        List<EntityWrapper<Banco>> l = JGestionUtils.getWrappedBancos(new BancoController().findAllWithCuentasBancarias());
        if (l.isEmpty()) {
            throw new MessageException("No existen banco con cuenta bancaria asociada");
        }
        final JDConciliacionBancaria jd = new JDConciliacionBancaria(owner, true);
        UTIL.loadComboBox(jd.getCbBancos(), l, false, "<No existen banco con cuenta bancaria asociada>");
        UTIL.getDefaultTableModel(jd.getjTableMovimientos(),
                new String[]{"Object", "Fecha", "Concepto", "Débito", "Crédito", "Salgo", "Conciliado"},
                new int[]{1, 60, 300, 100, 100, 100, 30},
                new Class<?>[]{null, Date.class, null, BigDecimal.class, BigDecimal.class, BigDecimal.class, Boolean.class}, new int[]{6});
        UTIL.hideColumnTable(jd.getjTableMovimientos(), 0);
        jd.getCbBancos().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (jd.getCbBancos().getItemCount() > 0) {
                    @SuppressWarnings("unchecked")
                    Banco b = ((EntityWrapper<Banco>) jd.getCbBancos().getSelectedItem()).getEntity();
                    UTIL.loadComboBox(jd.getCbCuentabancaria(), JGestionUtils.getWrappedCuentasBancarias(b.getCuentasbancaria()), false);
                } else {
                    UTIL.loadComboBox(jd.getCbCuentabancaria(), null, true);
                }
            }
        });
        jd.getBtnImportar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    CuentaBancaria cuentaBancaria;
                    try {
                        cuentaBancaria = ((EntityWrapper<CuentaBancaria>) jd.getCbCuentabancaria().getSelectedItem()).getEntity();
                    } catch (ClassCastException ex) {
                        throw new MessageException("Cuenta bancaria no válida");
                    }
                    if (jd.getDcDesde().getDate() == null || jd.getDcHasta().getDate() == null) {
                        throw new MessageException("Debe especificar una rango de fechas (Desde y Hasta) para recuperar los movimientos de la cuenta bancaria");
                    }
                    StringBuilder query = new StringBuilder("SELECT o FROM " + jpaController.getEntityClass().getSimpleName() + " o "
                            + "WHERE ");
                    query.append(" o.cuentaBancaria.id=").append(cuentaBancaria.getId())
                            .append(" AND o.").append("fechaCreditoDebito")
                            .append(" BETWEEN '").append(UTIL.yyyy_MM_dd.format(jd.getDcDesde().getDate())).append("'")
                            .append("  AND '").append(UTIL.yyyy_MM_dd.format(jd.getDcHasta().getDate())).append("'");
                    query.append(" ORDER BY o.").append("fechaCreditoDebito").append(" DESC");
                    LOG.debug(query.toString());
                    List<CuentabancariaMovimientos> l = jpaController.findAll(query.toString());
                    DefaultTableModel dtm = (DefaultTableModel) jd.getjTableMovimientos().getModel();
                    dtm.setRowCount(0);
                    BigDecimal saldo = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
                    for (CuentabancariaMovimientos cbm : l) {
                        saldo = saldo.add(cbm.getCredito());
                        saldo = saldo.subtract(cbm.getDebito());
                        dtm.addRow(new Object[]{cbm, cbm.getFechaCreditoDebito(), cbm.getDescripcion(), cbm.getDebito(), cbm.getCredito(), saldo, cbm.isConciliado()});
                    }
                } catch (MessageException ex) {
                    ex.displayMessage(jd);
                } catch (Exception ex) {
                    LOG.error("query conciliacion", ex);
                }
            }
        });
        jd.getBtnAgregar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    CuentaBancaria cuentaBancaria;
                    try {
                        cuentaBancaria = ((EntityWrapper<CuentaBancaria>) jd.getCbCuentabancaria().getSelectedItem()).getEntity();
                    } catch (ClassCastException ex) {
                        throw new MessageException("Cuenta bancaria no válida");
                    }
                    String concepto = jd.getTfConcepto().getText().trim();
                    BigDecimal importe;
                    if (concepto.isEmpty()) {
                        throw new MessageException("Concepto no válido");
                    }
                    if (jd.getDcConceptoFecha().getDate() == null) {
                        throw new MessageException("Fecha no válida");
                    }
                    try {
                        importe = new BigDecimal(jd.getTfImporte().getText());
                    } catch (Exception ex) {
                        throw new MessageException("Importe no válido");
                    }
                    CuentabancariaMovimientos cbm = new CuentabancariaMovimientos(new Date(), concepto, jd.getDcConceptoFecha().getDate(),
                            jd.getRadioDebito().isSelected() ? BigDecimal.ZERO : importe,
                            jd.getRadioDebito().isSelected() ? importe : BigDecimal.ZERO, false, UsuarioController.getCurrentUser(),
                            new OperacionesBancariasController().getOperacion(OperacionesBancariasController.AJUSTE), cuentaBancaria, null, null, false);
                } catch (MessageException ex) {
                    ex.displayMessage(jd);
                } catch (Exception ex) {
                    LOG.error("Agregando concepto en conciliacion", ex);
                    JOptionPane.showMessageDialog(jd, "Algo salió mal", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        return jd;
    }
}
