package controller;

import controller.exceptions.MessageException;
import entity.CuentabancariaMovimientos;
import entity.OperacionesBancarias;
import gui.JDABM;
import gui.JDCuentabancariaManager;
import gui.PanelOperacionBancariaDeposito;
import gui.PanelOperacionBancariaTransferencia;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import jgestion.JGestionUtils;
import jpa.controller.CuentabancariaMovimientosJpaController;
import jpa.controller.OperacionesBancariasJpaController;
import org.apache.log4j.Logger;
import utilities.general.UTIL;
import utilities.swing.components.ComboBoxWrapper;

/**
 *
 * @author FiruzzZ
 */
public class CuentabancariaMovimientosController {

    private static final Logger LOG = Logger.getLogger(CuentabancariaMovimientosController.class.getName());
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
                String jpql = armarQuery();
                cargarManagerTable(jpql);
            }
        });
        manager.getBtnAgregar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                @SuppressWarnings("unchecked")
                OperacionesBancarias op = ((ComboBoxWrapper<OperacionesBancarias>) manager.getCbOperacionesBancarias().getSelectedItem()).getEntity();
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
                        cb = ((ComboBoxWrapper<CuentaBancaria>) panelDeposito.getCbCuentabancaria().getSelectedItem()).getEntity();
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
                    OperacionesBancarias op = ((ComboBoxWrapper<OperacionesBancarias>) manager.getCbOperacionesBancarias().getSelectedItem()).getEntity();
                    CuentabancariaMovimientos cbm = new CuentabancariaMovimientos(fechaOP, descrip, fechaCre, monto, BigDecimal.ZERO, false, UsuarioController.getCurrentUser(), op, cb, null, null);
                    new CuentabancariaMovimientosJpaController().create(cbm);
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
                        cb = ((ComboBoxWrapper<CuentaBancaria>) panelDeposito.getCbCuentabancaria().getSelectedItem()).getEntity();
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
                    OperacionesBancarias op = ((ComboBoxWrapper<OperacionesBancarias>) manager.getCbOperacionesBancarias().getSelectedItem()).getEntity();
                    CuentabancariaMovimientos cbm = new CuentabancariaMovimientos(fechaOP, descrip, fechaCre, BigDecimal.ZERO, monto, false, UsuarioController.getCurrentUser(), op, cb, null, null);
                    new CuentabancariaMovimientosJpaController().create(cbm);
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
        JGestionUtils.getFocusCurrencyFormatterManager(panelTransf.getTfMonto());
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
                        origen = ((ComboBoxWrapper<CuentaBancaria>) panelTransf.getCbCuentabancaria().getSelectedItem()).getEntity();
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
                    OperacionesBancarias op = ((ComboBoxWrapper<OperacionesBancarias>) manager.getCbOperacionesBancarias().getSelectedItem()).getEntity();
                    CuentabancariaMovimientos cbmOrigen = new CuentabancariaMovimientos(fechaOP, descripOrigen, null, BigDecimal.ZERO, monto, false, UsuarioController.getCurrentUser(), op, origen, null, null);
                    if (panelTransf.getRbPropia().isSelected()) {
                        try {
                            destino = ((ComboBoxWrapper<CuentaBancaria>) panelTransf.getCbCuentabancariaDestino().getSelectedItem()).getEntity();
                            if (origen.equals(destino)) {
                                throw new MessageException("Las Cuentas bancarias Origen y Destino no pueden ser la misma.");
                            }
                        } catch (ClassCastException ex) {
                            throw new MessageException("Cuenta bancaria destino no válida");
                        }
                        descripOrigen = "Out: " + destino.getBanco().getNombre() + " N° " + destino.getNumero() + " (" + descripOrigen + ")";
                        cbmOrigen.setDescripcion(descripOrigen);
                        descripDestino = "In: " + origen.getBanco().getNombre() + " N° " + origen.getNumero();
                        cbmDestino = new CuentabancariaMovimientos(fechaOP, descripDestino, null, monto, BigDecimal.ZERO, false, UsuarioController.getCurrentUser(), op, destino, null, null);
                    }
                    new CuentabancariaMovimientosJpaController().create(cbmOrigen);
                    String x = "Operación de Transferencia n° " + cbmOrigen.getId() + " realizada";
                    if (cbmDestino != null) {
                        new CuentabancariaMovimientosJpaController().create(cbmDestino);
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

    public CuentabancariaMovimientos displayTransferenciaProveedor(Window owner, final String destinatario) {
        final PanelOperacionBancariaTransferencia panelTransf = new PanelOperacionBancariaTransferencia();
        abm = new JDABM(owner, "Transferencia a Proveedor", true, panelTransf);
        panelTransf.getRbPropia().setSelected(false);
        panelTransf.getRbPropia().setEnabled(false);
        panelTransf.getTfDescripcionMov().setText(destinatario);
        abm.getbAceptar().addActionListener(new ActionListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                try {
                    CuentaBancaria origen;
                    BigDecimal monto;
                    try {
                        origen = ((ComboBoxWrapper<CuentaBancaria>) panelTransf.getCbCuentabancaria().getSelectedItem()).getEntity();
                    } catch (ClassCastException ex) {
                        throw new MessageException("Cuenta bancaria origen no válida");
                    }

                    try {
                        monto = new BigDecimal(panelTransf.getTfMonto().getText());
                        if (monto.compareTo(BigDecimal.ZERO) != 1) {
                            throw new MessageException("Importe no válido, debe ser mayor a cero");
                        }
                    } catch (Exception ex) {
                        throw new MessageException("Importe no válido, ingrese solo números y utilice el punto como separador decimal");
                    }
                    String descrip = panelTransf.getTfDescripcionMov().getText().trim();
                    if (descrip.isEmpty()) {
                        throw new MessageException("Descripción de transferencia no válida");
                    }
                    Date fechaOP = panelTransf.getDcFechaOperacion().getDate();
                    OperacionesBancarias op = new OperacionesBancariasController().getOperacion(OperacionesBancariasController.TRANSFERENCIA);
                    CuentabancariaMovimientos cbmEXTRACCION = new CuentabancariaMovimientos(fechaOP, descrip, null, BigDecimal.ZERO, monto, false, UsuarioController.getCurrentUser(), op, origen, null, null);
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

    public CuentabancariaMovimientos displayTransferenciaCliente(Window owner, final String destinatario) {
        final PanelOperacionBancariaTransferencia panelTransf = new PanelOperacionBancariaTransferencia();
        abm = new JDABM(owner, "Transferencia de Cliente", true, panelTransf);
        panelTransf.getRbPropia().setSelected(false);
        panelTransf.getRbPropia().setEnabled(false);
        panelTransf.getTfDescripcionMov().setText(destinatario);
        abm.getbAceptar().addActionListener(new ActionListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                try {
                    CuentaBancaria origen;
                    BigDecimal monto;
                    try {
                        origen = ((ComboBoxWrapper<CuentaBancaria>) panelTransf.getCbCuentabancaria().getSelectedItem()).getEntity();
                    } catch (ClassCastException ex) {
                        throw new MessageException("Cuenta bancaria origen no válida");
                    }

                    try {
                        monto = new BigDecimal(panelTransf.getTfMonto().getText());
                        if (monto.compareTo(BigDecimal.ZERO) != 1) {
                            throw new MessageException("Importe no válido, debe ser mayor a cero");
                        }
                    } catch (Exception ex) {
                        throw new MessageException("Importe no válido, ingrese solo números y utilice el punto como separador decimal");
                    }
                    String descrip = panelTransf.getTfDescripcionMov().getText().trim();
                    if (descrip.isEmpty()) {
                        throw new MessageException("Descripción de transferencia no válida");
                    }
                    Date fechaOP = panelTransf.getDcFechaOperacion().getDate();
                    OperacionesBancarias op = new OperacionesBancariasController().getOperacion(OperacionesBancariasController.TRANSFERENCIA);
                    CuentabancariaMovimientos cbmEXTRACCION = new CuentabancariaMovimientos(fechaOP, descrip, null, monto, BigDecimal.ZERO, false, UsuarioController.getCurrentUser(), op, origen, null, null);
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

    private String armarQuery() {
        StringBuilder query = new StringBuilder("SELECT o FROM " + jpaController.getEntityClass().getSimpleName() + " o "
                + "WHERE o.id is not null ");
        if (manager.getCbBancos().getSelectedIndex() > 0) {
            query.append(" AND o.cuentaBancaria.banco.id=").append(((ComboBoxWrapper<?>) manager.getCbBancos().getSelectedItem()).getId());
        } else {
            query.append(" AND (");
            for (int i = 1; i < manager.getCbBancos().getItemCount(); i++) {
                query.append(" o.cuentaBancaria.banco.id=").append(((ComboBoxWrapper<?>) manager.getCbBancos().getItemAt(i)).getId());
                if ((i + 1) < manager.getCbBancos().getItemCount()) {
                    query.append(" OR ");
                }
            }
            query.append(")");
        }
        if (manager.getCbCuentabancaria().getSelectedIndex() > 0) {
            query.append(" AND o.cuentaBancaria.id=").append(((ComboBoxWrapper<?>) manager.getCbCuentabancaria().getSelectedItem()).getId());
        } else {
            //if (no seleccionó banco) => cuentas bancarias está vacía
            if (manager.getCbBancos().getSelectedIndex() > 0) {
                query.append(" AND (");
                for (int i = 1; i < manager.getCbCuentabancaria().getItemCount(); i++) {
                    query.append(" o.cuentaBancaria.id=").append(((ComboBoxWrapper<?>) manager.getCbCuentabancaria().getItemAt(i)).getId());
                    if ((i + 1) < manager.getCbCuentabancaria().getItemCount()) {
                        query.append(" OR ");
                    }
                }
                query.append(")");
            }
        }
        if (manager.getCbOperacionesBancariasFiltro().getSelectedIndex() > 0) {
            query.append(" AND o.operacionesBancarias.id=").append(((ComboBoxWrapper<?>) manager.getCbOperacionesBancariasFiltro().getSelectedItem()).getId());
        }
        if (manager.getDcDesde() != null || manager.getDcHasta() != null) {
            if (manager.getRbOperacion()) {
                if (manager.getDcDesde() != null) {
                    query.append(" AND o.fechaOperacion >='").append(UTIL.DATE_FORMAT.format(manager.getDcDesde())).append("'");
                }
                if (manager.getDcHasta() != null) {
                    query.append(" AND o.fechaOperacion <='").append(UTIL.DATE_FORMAT.format(manager.getDcHasta())).append("'");
                }
            } else {
                if (manager.getDcDesde() != null) {
                    query.append(" AND o.fechaCreditoDebito >='").append(UTIL.DATE_FORMAT.format(manager.getDcDesde())).append("'");
                }
                if (manager.getDcHasta() != null) {
                    query.append(" AND o.fechaCreditoDebito <='").append(UTIL.DATE_FORMAT.format(manager.getDcHasta())).append("'");
                }
            }
        }
        LOG.debug(query.toString());
        return query.toString();
    }

    private void cargarManagerTable(String jpql) {
        DefaultTableModel dtm = (DefaultTableModel) manager.getjTable1().getModel();
        dtm.setRowCount(0);
        List<CuentabancariaMovimientos> l = jpaController.findByQuery(jpql);
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
}
