package controller;

import entity.CuentaBancaria;
import controller.exceptions.MessageException;
import entity.Banco;
import entity.Cheque;
import entity.ChequePropio;
import entity.ChequeTerceros;
import entity.CuentabancariaMovimientos;
import entity.enums.ChequeEstado;
import gui.JDABM;
import gui.JDContenedor;
import gui.PanelABMCuentabancaria;
import gui.PanelDepositoCheque;
import java.awt.Component;
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
import jpa.controller.ChequePropioJpaController;
import jpa.controller.ChequeTercerosJpaController;
import jpa.controller.CuentabancariaJpaController;
import jpa.controller.CuentabancariaMovimientosJpaController;
import org.apache.log4j.Logger;
import utilities.general.EntityWrapper;
import utilities.general.UTIL;
import utilities.gui.SwingUtil;
import utilities.swing.components.ComboBoxWrapper;

/**
 *
 * @author FiruzzZ
 */
public class CuentabancariaController {

    private CuentabancariaJpaController jpaController;
    private JDABM abm;
    private JDContenedor contenedor;
    private boolean permitirFiltroVacio;
    private CuentaBancaria EL_OBJECT;
    private static final Logger LOG = Logger.getLogger(CuentabancariaController.class.getName());
    private PanelABMCuentabancaria panelABM;
    private PanelDepositoCheque panelDeposito;

    public CuentabancariaController() {
        jpaController = new CuentabancariaJpaController();
    }

    public JDialog initContenedor(Window owner) throws MessageException {
        contenedor = new JDContenedor(owner, true, "Administrar Cuentas Bancarias");
        contenedor.getbNuevo().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    EL_OBJECT = null;
                    initABM(contenedor, false);
                    abm.setLocationRelativeTo(contenedor);
                    abm.setVisible(true);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                }
            }
        });
        contenedor.getbModificar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    initABM(contenedor, true);
                    abm.setLocationRelativeTo(contenedor);
                    abm.setVisible(true);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 0);
                    LOG.error(ex.getMessage(), ex);
                }
            }
        });
        contenedor.getbBorrar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (contenedor.getjTable1().getSelectedRow() > -1) {
                        CuentaBancaria cb = getSelectedFromContenedor();
                        remove(cb);
                        contenedor.showMessage("Eliminado..", jpaController.getEntityClass().getSimpleName(), 1);
                    }
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 0);
                    LOG.error(ex.getMessage(), ex);
                }
            }
        });
        contenedor.getbImprimir().setVisible(false);
//        contenedor.getbImprimir().addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//            }
//        });
        UTIL.getDefaultTableModel(contenedor.getjTable1(), new String[]{"id", "Banco", "N° Cuenta", "Activa"}, new int[]{1, 200, 120, 20}, new Class<?>[]{null, null, null, Boolean.class});
        UTIL.hideColumnTable(contenedor.getjTable1(), 0);
        //no permite filtro de vacio en el inicio
        permitirFiltroVacio = false;
        cargarContenedorTabla(null);
        return contenedor;
    }

    /**
     * Crea una instancia del ABM
     *
     * @param parent
     * @param isEditing
     * @return una ventana para la creación de Bancos
     * @throws MessageException
     */
    private JDialog initABM(JDialog parent, boolean isEditing) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.TESORERIA);
        if (isEditing) {
            EL_OBJECT = getSelectedFromContenedor();
            if (EL_OBJECT == null) {
                throw new MessageException("Debe elegir una fila");
            }
        }
        return settingABM(parent, isEditing);
    }

    private CuentaBancaria getSelectedFromContenedor() {
        Integer selectedRow = contenedor.getjTable1().getSelectedRow();
        if (selectedRow > -1) {
            return jpaController.find(Integer.valueOf(contenedor.getDTM().getValueAt(selectedRow, 0).toString()));
        } else {
            return null;
        }
    }

    private JDialog settingABM(JDialog parent, boolean isEditing) {
        panelABM = new PanelABMCuentabancaria();
        UTIL.loadComboBox(panelABM.getCbBancos(), JGestionUtils.getWrappedBancos(new BancoController().findEntities()), false);
        if (isEditing) {
            setPanelABM(EL_OBJECT);
        }
        abm = new JDABM(parent, "ABM - " + jpaController.getEntityClass().getSimpleName(), true, panelABM);
        abm.getbAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (EL_OBJECT == null) {
                        EL_OBJECT = new CuentaBancaria();
                        EL_OBJECT.setSaldo(BigDecimal.ZERO);
                    }
                    setEntity(EL_OBJECT);
                    checkConstraints(EL_OBJECT);
                    String msg;
                    if (EL_OBJECT.getId() == null) {
                        jpaController.create(EL_OBJECT);
                        msg = "Creado..";
                    } else {
                        jpaController.merge(EL_OBJECT);
                        msg = "Modificado..";
                    }
                    EL_OBJECT = null;
                    cargarContenedorTabla(null);
                    abm.showMessage(msg, jpaController.getEntityClass().getSimpleName(), 1);
                    abm.dispose();
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                } catch (Exception ex) {
                    LOG.error(ex);
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
        return abm;
    }

    private void setEntity(CuentaBancaria o) throws MessageException {
        o.setBanco(((ComboBoxWrapper<Banco>) panelABM.getCbBancos().getSelectedItem()).getEntity());
        String numeroCuenta = panelABM.getTfNumero().getText().trim();
        if (numeroCuenta == null || numeroCuenta.length() < 1) {
            throw new MessageException("Número de cuenta no válido, ingrese solo números enteros (hasta 22 dígitos).");
        }

        try {
            for (char c : numeroCuenta.toCharArray()) {
                if (!Character.isDigit(c)) {
                    throw new NumberFormatException();
                }
            }
            o.setNumero(numeroCuenta);
        } catch (NumberFormatException ex) {
            throw new MessageException("Número de cuenta no válido, ingrese solo números enteros (hasta 22 dígitos).");
        } catch (Exception ex) {
            LOG.error("setEntity(CuentaBancaria)", ex);
            throw new MessageException("Algo salió mal:\n" + ex.getLocalizedMessage());
        }
        o.setActiva(panelABM.getjCheckBox1().isSelected());
    }

    private void checkConstraints(CuentaBancaria o) throws MessageException {
        String idquery = o.getId() != null ? "o.id <>" + o.getId() + " AND " : "";
        if (!jpaController.findByQuery("SELECT o FROM " + jpaController.getEntityClass().getSimpleName() + " o "
                + "WHERE " + idquery + " o.banco.id=" + o.getBanco().getId() + " AND o.numero='" + o.getNumero() + "'").isEmpty()) {
            throw new MessageException("Ya existe un registro del Banco " + o.getBanco().getNombre() + " con el N° " + o.getNumero());
        }
    }

    private void armarQuery(String filtro) {
        String query = null;
        if (filtro != null && filtro.length() > 0) {
            query = "SELECT * FROM " + jpaController.getEntityClass().getSimpleName() + " o WHERE o.numero >= " + filtro;
        }
        cargarContenedorTabla(query);
    }

    private void cargarContenedorTabla(String query) {
        if (contenedor != null) {
            DefaultTableModel dtm = contenedor.getDTM();
            dtm.setRowCount(0);
            List<CuentaBancaria> l;
            if (query == null) {
                l = jpaController.findAll();
            } else {
                l = jpaController.findByNativeQuery(query);
            }
            for (CuentaBancaria o : l) {
                dtm.addRow(new Object[]{o.getId(), o.getBanco().getNombre(), o.getNumero(), o.getActiva()});
            }
        }
    }

    private void remove(CuentaBancaria find) throws MessageException {
        try {
            jpaController.remove(find);
        } catch (Exception e) {
            throw new MessageException(e.getLocalizedMessage());
        }
    }

    private void setPanelABM(CuentaBancaria o) {
        UTIL.setSelectedItem(panelABM.getCbBancos(), o.getBanco().getNombre());
        panelABM.getTfNumero().setText(o.getNumero());
        panelABM.getjCheckBox1().setSelected(o.getActiva());
    }

    void initDepositoUI(final ChequeTerceros cheque) {
        panelDeposito = new PanelDepositoCheque();
        panelDeposito.getLabelNcuenta().setVisible(false);
        panelDeposito.getCbCuentaBancaria().setVisible(false);
        SwingUtil.setComponentsEnabled(panelDeposito.getPanelInfoCheque().getComponents(), false, true);
        setChequePanelDeposito(cheque);
        UTIL.loadComboBox(panelDeposito.getCbDepositoBancos(), JGestionUtils.getWrappedBancos(new BancoController().findWithCuentasBancarias(true)), true);
        panelDeposito.getCbOperacionesBancarias().addItem(new OperacionesBancariasController().getOperacion(OperacionesBancariasController.DEPOSITO).getNombre());
        panelDeposito.getCbDepositoBancos().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (panelDeposito.getCbDepositoBancos().getSelectedIndex() > 0) {
                    @SuppressWarnings("unchecked")
                    Banco b = ((ComboBoxWrapper<Banco>) panelDeposito.getCbDepositoBancos().getSelectedItem()).getEntity();
                    UTIL.loadComboBox(panelDeposito.getCbDepositoCuentaBancaria(), JGestionUtils.getWrappedCuentasBancarias(b.getCuentasbancaria()), false);
                }
            }
        });
        abm = new JDABM(null, "Deposito de cheque", true, panelDeposito);
        abm.getbAceptar().addActionListener(new ActionListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                try {
                    ChequeTerceros chequeToDeposit = cheque;
                    Date fechaOperacion = panelDeposito.getDcFechaOperacion().getDate();
                    Date fechaCreditoDebito = panelDeposito.getDcFechaCreditoDebito().getDate();
                    String descripcion = panelDeposito.getTfDescripcion().getText().trim();
                    CuentaBancaria cb;
                    if (panelDeposito.getCbDepositoBancos().getSelectedIndex() <= 0) {
                        throw new MessageException("Banco no válido");
                    }
                    cb = ((ComboBoxWrapper<CuentaBancaria>) panelDeposito.getCbDepositoCuentaBancaria().getSelectedItem()).getEntity();
                    if (fechaOperacion == null) {
                        throw new MessageException("Fecha de operación no válida");
                    }
                    if (descripcion.isEmpty()) {
                        throw new MessageException("Descripción de operación no válida");
                    }
                    CuentabancariaMovimientos cbm = new CuentabancariaMovimientos(fechaOperacion, descripcion, fechaCreditoDebito, chequeToDeposit.getImporte(), BigDecimal.ZERO, false, UsuarioController.getCurrentUser(),
                            new OperacionesBancariasController().getOperacion(OperacionesBancariasController.DEPOSITO), cb, chequeToDeposit, null, false);
                    new CuentabancariaMovimientosJpaController().create(cbm);
                    chequeToDeposit.setEstado(ChequeEstado.DEPOSITADO.getId());
                    new ChequeTercerosJpaController().merge(chequeToDeposit);
                    JOptionPane.showMessageDialog(abm, "Movimiento de cuenta N° " + cbm.getId() + " creado.", null, JOptionPane.INFORMATION_MESSAGE);
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
        abm.setVisible(true);
    }

    private void setChequePanelDeposito(Cheque cheque) {
        if (cheque instanceof ChequeTerceros) {
            panelDeposito.getCbEmisor().addItem(((ChequeTerceros)cheque).getCliente().getNombre());
        } else {
            ChequePropio cp = (ChequePropio)cheque;
            panelDeposito.getCbEmisor().addItem(cp.getProveedor().getNombre());
            panelDeposito.getCbDepositoBancos().addItem(new EntityWrapper<Banco>(cp.getBanco(), cp.getBanco().getId(), cp.getBanco().getNombre()));
            panelDeposito.getCbDepositoCuentaBancaria().addItem(new EntityWrapper<CuentaBancaria>(cp.getCuentabancaria(), cp.getCuentabancaria().getId(), cp.getCuentabancaria().getNumero()));
        }
        panelDeposito.getDcCheque().setDate(cheque.getFechaCheque());
        panelDeposito.getDcCobro().setDate(cheque.getFechaCobro());
        panelDeposito.getCbBancos().addItem(cheque.getBanco().getNombre());
        panelDeposito.getTfNumero().setText(cheque.getNumero().toString());
        panelDeposito.getTfImporte().setText(UTIL.DECIMAL_FORMAT.format(cheque.getImporte()));
        panelDeposito.getCbChequeEstados().addItem(ChequeEstado.findById(cheque.getEstado()));
        panelDeposito.getTaObservacion().setText(cheque.getObservacion());
        panelDeposito.getCheckEndosado().setSelected(cheque.getEndosatario() != null);
        panelDeposito.getTfEndosatario().setText(cheque.getEndosatario());
        panelDeposito.getDcEndoso().setDate(cheque.getFechaEndoso());
        panelDeposito.getCheckCruzado().setSelected(cheque.isCruzado());
        panelDeposito.getDcFechaOperacion().setMinSelectableDate(cheque.getFechaCobro());
        panelDeposito.getDcFechaCreditoDebito().setMinSelectableDate(cheque.getFechaCobro());
        panelDeposito.getDcFechaOperacion().setDate(cheque.getFechaCobro());
        panelDeposito.getTfDescripcion().setText("Cheque N°" + cheque.getNumero());
    }

    void initDebitoUI(final ChequePropio cheque) {
        panelDeposito = new PanelDepositoCheque();
        panelDeposito.getLabelEmisor().setText("Proveedor");
        SwingUtil.setComponentsEnabled(panelDeposito.getPanelInfoCheque().getComponents(), false, true, (Class<? extends Component>[]) null);
        panelDeposito.getCbOperacionesBancarias().addItem(new OperacionesBancariasController().getOperacion(OperacionesBancariasController.EXTRACCION).getNombre());
        setChequePanelDeposito(cheque);
        abm = new JDABM(null, "Débito de cheque", true, panelDeposito);
        abm.getbAceptar().addActionListener(new ActionListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                try {
                    ChequePropio chequeToDeposit = cheque;
                    Date fechaOperacion = panelDeposito.getDcFechaOperacion().getDate();
                    Date fechaCreditoDebito = panelDeposito.getDcFechaCreditoDebito().getDate();
                    String descripcion = panelDeposito.getTfDescripcion().getText().trim();
                    if (fechaOperacion == null) {
                        throw new MessageException("Fecha de operación no válida");
                    }
                    if (fechaCreditoDebito == null) {
                        throw new MessageException("Fecha de operación no válida");
                    }
                    if (fechaCreditoDebito.before(chequeToDeposit.getFechaCobro())) {
                        throw new MessageException("Fecha de débito no válida, no puede ser anterior a la de cobro");
                    }
                    if (descripcion.isEmpty()) {
                        throw new MessageException("Descripción de operación no válida");
                    }
                    CuentabancariaMovimientos cbm = new CuentabancariaMovimientos(fechaOperacion, descripcion, fechaCreditoDebito, BigDecimal.ZERO, chequeToDeposit.getImporte(), false, UsuarioController.getCurrentUser(),
                            new OperacionesBancariasController().getOperacion(OperacionesBancariasController.EXTRACCION), chequeToDeposit.getCuentabancaria(), null, chequeToDeposit, false);
                    new CuentabancariaMovimientosJpaController().create(cbm);
                    chequeToDeposit.setEstado(ChequeEstado.DEBITADO.getId());
                    new ChequePropioJpaController().merge(chequeToDeposit);
                    JOptionPane.showMessageDialog(abm, "Movimiento de cuenta N° " + cbm.getId() + " creado.", null, JOptionPane.INFORMATION_MESSAGE);
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
        abm.setVisible(true);
    }
}
