package controller;

import entity.CuentaBancaria;
import controller.exceptions.DatabaseErrorException;
import controller.exceptions.MessageException;
import entity.Banco;
import entity.BancoSucursal;
import entity.ChequePropio;
import entity.Cliente;
import entity.Proveedor;
import entity.enums.ChequeEstado;
import gui.JDABM;
import gui.JDChequesManager;
import gui.JDContenedor;
import gui.PanelABMCheques;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.persistence.NoResultException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import jgestion.JGestionUtils;
import jpa.controller.ChequePropioJpaController;
import org.apache.log4j.Logger;
import utilities.general.UTIL;
import utilities.swing.components.ComboBoxWrapper;
import utilities.swing.components.NumberRenderer;

/**
 *
 * @author FiruzzZ
 */
public class ChequePropioController implements ActionListener {

    private static Logger LOG = Logger.getLogger(ChequePropioController.class);
    private ChequePropio EL_OBJECT;
    private JDContenedor contenedor;
    private JDABM abm;
    private PanelABMCheques panelABM;
    private ChequePropioJpaController jpaController;
    private JDChequesManager jdChequeManager;
    //exclusively related to the GUI
    private final String[] columnNames = {"id", "Nº Cheque", "F. Cheque", "Emitido a", "F. Cobro", "Banco", "Sucursal", "Importe", "Estado", "Cruzado", "Endosatario", "F. Endoso", "C. Ingreso", "C. Egreso", "Observacion", "Usuario"};
    private final int[] columnWidths = {1, 80, 80, 100, 80, 100, 100, 100, 80, 50, 100, 100, 150, 150, 100, 80};
    private final Class[] columnClassTypes = {Integer.class, Number.class, null, null, null, null, null, Number.class, null, Boolean.class, null};
    //Este se pone en el comboBox
    private final String[] orderByToComboBoxList = {"N° Cheque", "Fecha de Emisión", "Fecha de Cobro", "Importe", "Banco/Sucursal", "Proveedor", "Estado"};
    //Y este es el equivalente (de lo seleccionado en el combo) para el SQL.
    private final String[] orderByToQueryKeyList = {"numero", "fecha_cheque", "fecha_cobro", "importe", "banco.nombre, banco_sucursal.nombre", "proveedor.nombre", "estado"};

    public ChequePropioController() {
        jpaController = new ChequePropioJpaController();
    }

    /**
     *
     *
     * @param isEditing
     * @throws MessageException
     */
    private JDialog initABM(boolean isEditing) throws MessageException {
        UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.TESORERIA);
        initPanelABM();
        if (isEditing) {
            setPanel(EL_OBJECT);
        }
        abm = new JDABM(contenedor, "ABM - " + jpaController.getEntityClass().getSimpleName() + "s", true, panelABM);
        abm.setListener(this);
        return abm;
    }

    /**
     * Levanta la UI para settear un chequePropio PERO NO PERSISTE, sino que
     * retorna la instancia lista para ser persistida.
     *
     * @param owner
     * @param isEditing
     * @param proveedor if {@code proveedor != null}, the combo is selected with
     * this and the combobox is disabled.
     * @return
     * @throws MessageException
     */
    ChequePropio initABM(Window owner, boolean isEditing, Proveedor proveedor) throws MessageException {
        UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.TESORERIA);
        if (isEditing && EL_OBJECT == null) {
            throw new MessageException("Debe elegir una fila de la tabla");
        }

        initPanelABM();

        if (proveedor != null) {
            panelABM.getCbEmisor().setEnabled(false);
            UTIL.setSelectedItem(panelABM.getCbEmisor(), new ComboBoxWrapper<Proveedor>(proveedor, proveedor.getId(), proveedor.getNombre()));
        }
        if (isEditing) {
            panelABM.getCbEmisor().setEnabled(true);
            setPanel(EL_OBJECT);
        }
        abm = new JDABM(owner, null, true, panelABM);
        abm.setTitle("ABM - Cheque Propio");
        abm.setListener(this);
        abm.setVisible(true);
        return EL_OBJECT;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // <editor-fold defaultstate="collapsed" desc="JButton">
        if (e.getSource() instanceof JButton) {
            JButton boton = (JButton) e.getSource();
            //<editor-fold defaultstate="collapsed" desc="contenedor Actions">
//            if (contenedor != null) {
//                if (boton.equals(contenedor.getbNuevo())) {
//                    try {
//                        EL_OBJECT = null;
//                        initABM(false);
//                        abm.setVisible(true);
//                    } catch (MessageException ex) {
//                        contenedor.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
//                    } catch (Exception ex) {
//                        contenedor.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 0);
//                        LOG.error(ex);
//                    }
//                } else if (boton.equals(contenedor.getbModificar())) {
//                    try {
//                        int selectedRow = contenedor.getjTable1().getSelectedRow();
//                        if (selectedRow > -1) {
//                            EL_OBJECT = jpaController.find(Integer.valueOf((contenedor.getDTM().getValueAt(selectedRow, 0)).toString()));
//                            if (EL_OBJECT == null) {
//                                throw new MessageException("Debe elegir una fila de la tabla");
//                            }
//                            initABM(true);
//                            abm.setVisible(true);
//
//                        }
//                    } catch (MessageException ex) {
//                        contenedor.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
//                    } catch (Exception ex) {
//                        contenedor.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 0);
//                        LOG.error(ex);
//                    }
//
//                } else if (boton.equals(contenedor.getbBorrar())) {
//                    try {
//                        int selectedRow = contenedor.getjTable1().getSelectedRow();
//                        if (selectedRow > -1) {
//                            EL_OBJECT = DAO.getEntityManager().find(ChequePropio.class,
//                                    Integer.valueOf((contenedor.getDTM().getValueAt(selectedRow, 0)).toString()));
//                        }
//                        if (EL_OBJECT == null) {
//                            throw new MessageException("No hay " + jpaController.getEntityClass().getSimpleName() + " seleccionado");
//                        }
//                        jpaController.remove(EL_OBJECT);
//                    } catch (MessageException ex) {
//                        contenedor.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
//                    } catch (Exception ex) {
//                        contenedor.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 0);
//                        LOG.error(ex);
//                    }
//                } else if (boton.getName().equalsIgnoreCase("Print")) {
//                    //no implementado aun...
//                } else if (boton.getName().equalsIgnoreCase("exit")) {
//                    contenedor.dispose();
//                    contenedor = null;
//                }
//            } 
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="abm Actions">
            if (abm != null && panelABM != null) {
                if (boton.equals(abm.getbAceptar())) {
                    try {
                        Integer id = null;
                        if (EL_OBJECT != null) {
                            id = EL_OBJECT.getId();
                        }
                        ChequePropio cheque = getEntity();
                        if (id != null) {
                            cheque.setId(id);
                        }
                        checkConstraints(cheque);
                        if (panelABM.persist()) {
                            String msg = cheque.getId() == null ? "Registrado" : "Modificado";
                            if (cheque.getId() == null) {
                                jpaController.create(cheque);
                            } else {
                                jpaController.merge(cheque);
                            }
                            abm.showMessage(msg, jpaController.getEntityClass().getSimpleName(), 1);
                        }
                        EL_OBJECT = cheque;
                        abm.dispose();
                    } catch (MessageException ex) {
                        abm.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                    } catch (Exception ex) {
                        abm.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                        LOG.error(ex);
                    }
                } else if (boton.equals(abm.getbCancelar())) {
                    abm.dispose();
                    panelABM = null;
                    abm = null;
                    EL_OBJECT = null;
                } else if (boton.equals(panelABM.getbAddBanco())) {
                    try {
                        JDialog initABM = new BancoController().initABM(abm);
                        initABM.setLocationRelativeTo(abm);
                        initABM.setVisible(true);
                    } catch (MessageException ex) {
                        abm.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                    }
                    UTIL.loadComboBox(panelABM.getCbBancos(), JGestionUtils.getWrappedBancos(new BancoController().findWithCuentasBancarias(true)), true);
                    UTIL.loadComboBox(panelABM.getCbBancoSucursales(), null, null, "<Seleccionar Banco>");
                } else if (boton.equals(panelABM.getbAddSucursal())) {
                    Banco bancoSelected = getSelectedBancoFromPanel();
                    try {
                        JDialog initABM = new BancoSucursalController().initABM(abm);
                        initABM.setLocationRelativeTo(abm);
                        initABM.setVisible(true);
                        UTIL.setSelectedItem(panelABM.getCbBancos(), bancoSelected.toString());
                    } catch (MessageException ex) {
                        abm.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                    }
                } else if (boton.equals(panelABM.getbAddEmisor())) {
                    ProveedorController c = new ProveedorController();
                    JDialog jd = c.initContenedor(null, true);
                    jd.setLocationRelativeTo(abm);
                    jd.setVisible(true);
                    UTIL.loadComboBox(panelABM.getCbEmisor(), c.findEntities(), false);

                }
            }//</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="jdChequeManager EVENTS">
            else if (jdChequeManager != null) {
                if (boton.equals(jdChequeManager.getbBuscar())) {
                    try {
                        armarQuery(false);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "Error ejecutando consulta", JOptionPane.ERROR_MESSAGE);
                    }
                } else if (boton.equals(jdChequeManager.getBtnNuevo())) {
                } else if (boton.equals(jdChequeManager.getbACaja())) {
                } else if (boton.equals(jdChequeManager.getbAnular())) {
                    int row = jdChequeManager.getjTable1().getSelectedRow();
                    if (row > -1) {
                        ChequePropio cheque = jpaController.find((Integer) jdChequeManager.getjTable1().getModel().getValueAt(row, 0));
                        if (cheque.getChequeEstado().equals(ChequeEstado.CARTERA)) {
                            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(jdChequeManager, "¿Confirma la anulación del Cheque N°" + cheque.getNumero() + "?")) {
                                cheque.setEstado(ChequeEstado.ANULADO.getId());
                                jpaController.merge(cheque);
                            }
                        } else {
                            JOptionPane.showMessageDialog(jdChequeManager, "Solo los cheques en " + ChequeEstado.CARTERA + " pueden ser depositados", "Error", JOptionPane.WARNING_MESSAGE);
                        }
                        armarQuery(false);
                    }
                } else if (boton.equals(jdChequeManager.getbDeposito())) {
//                    int row = jdChequeManager.getjTable1().getSelectedRow();
//                    if (row > -1) {
//                        ChequePropio find = jpaController.find((Integer) jdChequeManager.getjTable1().getModel().getValueAt(row, 0));
//                        new CuentabancariaController().displayDepositoUI(find);
//                    }
                } else if (boton.equals(jdChequeManager.getbImprimir())) {
                    try {
                        armarQuery(true);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "Error ejecutando consulta", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="JComboBox">
            else if (e.getSource() instanceof JComboBox) {
                JComboBox combo = (JComboBox) e.getSource();
            }//</editor-fold>
        }// </editor-fold>
    }

    private void setPanel(ChequePropio o) {
        panelABM.setDcCheque(o.getFechaCheque());
        panelABM.setDcCobro(o.getFechaCobro());
        panelABM.getTfNumero().setText(o.getNumero().toString());
        panelABM.getTfImporte().setText(UTIL.PRECIO_CON_PUNTO.format(o.getImporte()));
        UTIL.setSelectedItem(panelABM.getCbBancos(), o.getBanco().getNombre());
        UTIL.setSelectedItem(panelABM.getCbBancoSucursales(), o.getBancoSucursal().getNombre());
        UTIL.setSelectedItem(panelABM.getCbEmisor(), o.getProveedor());
        panelABM.getTaObservacion().setText(o.getObservacion());
        panelABM.getCheckCruzado().setSelected(o.getCruzado());
//        if(o.getEndosatario() != null) {
//            panelABM.getCheckEndosado().setSelected(true);
//            panelABM.getTfEndosatario().setText(o.getEndosatario());
//            panelABM.setDcEndoso(o.getFechaEndoso());
//        }
        UTIL.setSelectedItem(panelABM.getCbChequeEstados(), o.getChequeEstado());
    }

    @SuppressWarnings("unchecked")
    private ChequePropio getEntity() throws MessageException {
        Date fechaCheque, fechaCobro, fechaEndoso = null;
        Long numero = null;
        BigDecimal importe = null;
        Banco banco;
        BancoSucursal sucursal = null;
        Proveedor proveedor = ((ComboBoxWrapper<Proveedor>) panelABM.getCbEmisor().getSelectedItem()).getEntity();
        boolean cruzado;
        String endosatario = null, observacion = null;
        CuentaBancaria cuentaBancaria;

        if (panelABM.getDcCheque().getDate() == null) {
            throw new MessageException("Debe ingresar la Fecha del Cheque.");
        }
        if (panelABM.getDcCobro().getDate() == null) {
            throw new MessageException("Debe ingresar la Fecha de cobro del Cheque.");
        }
        fechaCheque = panelABM.getDcCheque().getDate();
        fechaCobro = panelABM.getDcCobro().getDate();

        if (UTIL.compararIgnorandoTimeFields(fechaCheque, fechaCobro) > 0) {
            throw new MessageException("Fecha de cobro no puede ser anterior a Fecha de cheque.");
        }
        try {
            numero = Long.valueOf(panelABM.getTfNumero().getText());
        } catch (NumberFormatException numberFormatException) {
            throw new MessageException("Número de cheque no válido");
        }
        try {
            importe = new BigDecimal(panelABM.getTfImporte().getText());
        } catch (NumberFormatException numberFormatException) {
            throw new MessageException("Importe no válido (ingrese solo números y utilice el punto como separador decimal");
        }
        banco = getSelectedBancoFromPanel();
        cuentaBancaria = ((ComboBoxWrapper<CuentaBancaria>) panelABM.getCbCuentaBancaria().getSelectedItem()).getEntity();
        try {
            sucursal = (BancoSucursal) panelABM.getCbBancoSucursales().getSelectedItem();
        } catch (ClassCastException e) {
//            throw new MessageException("Sucursal de Banco no válida");
        }
        cruzado = panelABM.getCheckCruzado().isSelected();
        if (!panelABM.getTaObservacion().getText().isEmpty()) {
            observacion = panelABM.getTaObservacion().getText();
        }
        ChequePropio newCheque = new ChequePropio(proveedor, numero, banco, sucursal, importe, fechaCheque, fechaCobro, cruzado, observacion, ChequeEstado.CARTERA, endosatario, fechaEndoso, UsuarioController.getCurrentUser(), cuentaBancaria);
        return newCheque;
    }

    private void checkConstraints(ChequePropio object) throws DatabaseErrorException, MessageException {
        String idQuery = "";
        if (object.getId() != null) {
            idQuery = "o.id<>" + object.getId() + " AND ";
        }
        try {
            DAO.getEntityManager().createQuery("SELECT o FROM " + jpaController.getEntityClass().getSimpleName() + " o "
                    + "WHERE " + idQuery
                    + " o.numero=" + object.getNumero(), object.getClass()).getSingleResult();
            throw new MessageException("Ya existe un " + jpaController.getEntityClass().getSimpleName() + " con este número");
        } catch (NoResultException noResultException) {
        }
    }

    public JDialog getManager(Window owner) throws MessageException {
        initManager(owner, false);
        jdChequeManager.setLocationRelativeTo(owner);
        return jdChequeManager;
    }

    public ChequePropio initManagerBuscador(Window owner) throws MessageException {
        initManager(owner, true);
        jdChequeManager.setLocationRelativeTo(owner);
        jdChequeManager.setVisible(true);
        return EL_OBJECT;
    }

    private void initManager(Window parent, boolean selectionMode) throws MessageException {
        if (!selectionMode) {
            UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.TESORERIA);
        }
        List<Banco> l = new BancoController().findWithCuentasBancarias(true);
        if (l.isEmpty()) {
            throw new MessageException("Para emitor cheques propios, primero tiene que crear una Cuenta bancaria relacionado a un Banco."
                    + "\nDatos Generales > Bancos/Cuentas > Cuentas Bancarias");
        }
        jdChequeManager = new JDChequesManager(parent, true);
        jdChequeManager.getBtnNuevo().setEnabled(false);
        jdChequeManager.getbACaja().setEnabled(false);
        jdChequeManager.getbDeposito().setEnabled(false);
        jdChequeManager.getLabelEmisor().setText("Emitido a");
        jdChequeManager.setTitle("Administración de Cheques Propios");
        jdChequeManager.getCbBancoSucursales().setVisible(false);
        jdChequeManager.getLabelSucursales().setVisible(false);
        UTIL.getDefaultTableModel(jdChequeManager.getjTable1(), columnNames, columnWidths, columnClassTypes);
        jdChequeManager.getjTable1().getColumnModel().getColumn(7).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        UTIL.hideColumnTable(jdChequeManager.getjTable1(), 0);
        UTIL.loadComboBox(jdChequeManager.getCbBancos(), JGestionUtils.getWrappedBancos(l), true);
        UTIL.loadComboBox(jdChequeManager.getCbEmisor(), JGestionUtils.getWrappedProveedores(new ProveedorController().findEntities()), true);
        UTIL.loadComboBox(jdChequeManager.getCbEstados(), Arrays.asList(ChequeEstado.values()), true);
        if (selectionMode) {
            EL_OBJECT = null;
            UTIL.setSelectedItem(jdChequeManager.getCbEstados(), ChequeEstado.CARTERA);
            jdChequeManager.getCbEstados().setEnabled(false);
            jdChequeManager.getjTable1().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() > 1 && jdChequeManager.getjTable1().getSelectedRow() > -1) {
                        ChequePropio cheque = jpaController.find((Integer) jdChequeManager.getjTable1().getModel().getValueAt(jdChequeManager.getjTable1().getSelectedRow(), 0));
                        if (cheque.getChequeEstado().equals(ChequeEstado.CARTERA)) {
                            EL_OBJECT = cheque;
                            jdChequeManager.dispose();
                        } else {
                            JOptionPane.showMessageDialog(jdChequeManager, "Solo los cheques en " + ChequeEstado.CARTERA + " pueden ser recibidos como medio de pago", "Error", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
            });
        }
        jdChequeManager.getCbBancos().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jdChequeManager.getCbBancos().getSelectedIndex() > 0) {
                    Banco banco = ((ComboBoxWrapper<Banco>) jdChequeManager.getCbBancos().getSelectedItem()).getEntity();
                    List<CuentaBancaria> cuentasbancaria = banco.getCuentasbancaria();
                    UTIL.loadComboBox(jdChequeManager.getCbCuentaBancaria(), JGestionUtils.getWrappedCuentasBancarias(cuentasbancaria), true);
//                    UTIL.loadComboBox(jdChequeManager.getCbBancoSucursales(), new BancoSucursalController().findBy(banco), true);
                } else {
                    UTIL.loadComboBox(jdChequeManager.getCbCuentaBancaria(), null, true);
//                    UTIL.loadComboBox(jdChequeManager.getCbBancoSucursales(), null, null, "<Seleccionar un Banco>");
                }
            }
        });
        jdChequeManager.addButtonListener(this);
    }

    @SuppressWarnings("unchecked")
    private Banco getSelectedBancoFromPanel() {
        return ((ComboBoxWrapper<Banco>) panelABM.getCbBancos().getSelectedItem()).getEntity();
    }

    private void initPanelABM() {
        panelABM = new PanelABMCheques();
        panelABM.setUIChequePropio();
        UTIL.loadComboBox(panelABM.getCbBancos(), JGestionUtils.getWrappedBancos(new BancoController().findWithCuentasBancarias(true)), false);
//        UTIL.loadComboBox(panelABM.getCbBancoSucursales(), null, null, "<Seleccionar un Banco>");
        UTIL.loadComboBox(panelABM.getCbEmisor(), JGestionUtils.getWrappedProveedores(new ProveedorController().findEntities()), false);
//        UTIL.loadComboBox(panelABM.getCbLibrado(), new LibradoJpaController().findEntities(), false);

        panelABM.getCbBancos().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (panelABM.getCbBancos().getItemCount() > 0 && panelABM.getCbBancos().getSelectedIndex() >= 0) {
                    Banco banco = getSelectedBancoFromPanel();
//                    UTIL.loadComboBox(panelABM.getCbBancoSucursales(), new BancoSucursalController().findBy(banco), false);
                    UTIL.loadComboBox(panelABM.getCbCuentaBancaria(), JGestionUtils.getWrappedCuentasBancarias(banco.getCuentasbancaria()), false);
                } else {
//                    UTIL.loadComboBox(panelABM.getCbBancoSucursales(), null, null, "<Seleccionar un Banco>");
                }
            }
        });
        //force calling to ActionListener
        panelABM.getCbBancos().setSelectedIndex(0);

        //Default para ChequePropio
        panelABM.getLabelEmisor().setText("Proveedor");
        panelABM.getCheckEndosado().setSelected(false);
        panelABM.getCheckEndosado().setEnabled(false);
        panelABM.getbAddEmisor().addActionListener(this);
        panelABM.getbAddBanco().addActionListener(this);
    }

    private void armarQuery(boolean imprimir) {
        StringBuilder query = new StringBuilder("SELECT "
                + " c.id, c.numero, TO_CHAR(c.fecha_cheque,'DD/MM/YYYY') as fecha_cheque, ccc.nombre as cliente, TO_CHAR(c.fecha_cobro,'DD/MM/YYYY') as fecha_cobro,"
                + " banco.nombre as banco, banco_sucursal.nombre as sucursal, c.importe, cheque_estado.nombre as estado, c.cruzado"
                + ", c.endosatario, c.fecha_endoso"
                + ", c.comprobante_ingreso, c.comprobante_egreso, c.observacion"
                + ", usuario.nick as usuario "
                + " FROM cheque_propio c "
                + " JOIN banco ON (c.banco = banco.id) "
                + " JOIN cuentabancaria cb ON (c.cuentabancaria_id = cb.id)"
                + " JOIN proveedor ccc ON (c.proveedor = ccc.id) "
                + " JOIN usuario ON (c.usuario = usuario.id) "
                + " JOIN cheque_estado  ON (c.estado  = cheque_estado.id)"
                + " LEFT JOIN banco_sucursal ON (c.banco_sucursal = banco_sucursal.id) "
                + " WHERE c.id > -1");
        if (jdChequeManager.getTfChequeNumero().getText().trim().length() > 0) {
            try {
                Long numero = Long.valueOf(jdChequeManager.getTfChequeNumero().getText());
                query.append(" AND c.numero='").append(numero).append("'");
            } catch (NumberFormatException numberFormatException) {
            }
        }
        if (jdChequeManager.getDcEmisionDesde() != null) {
            query.append(" AND c.fecha_cheque >='").append(jdChequeManager.getDcEmisionDesde()).append("'");
        }
        if (jdChequeManager.getDcEmisionHasta() != null) {
            query.append(" AND c.fecha_cheque <='").append(jdChequeManager.getDcEmisionHasta()).append("'");
        }
        if (jdChequeManager.getDcCobroDesde() != null) {
            query.append(" AND c.fecha_cobro >='").append(jdChequeManager.getDcCobroDesde()).append("'");
        }
        if (jdChequeManager.getDcCobroHasta() != null) {
            query.append(" AND c.fecha_cobro <='").append(jdChequeManager.getDcCobroHasta()).append("'");
        }
        if (jdChequeManager.getTfImporte().getText().trim().length() > 0) {
            try {
                Double importe = Double.valueOf(jdChequeManager.getTfImporte().getText());
                query.append(" AND c.importe").append(jdChequeManager.getCbImporteCondicion().getSelectedItem().toString()).append(importe);
            } catch (NumberFormatException numberFormatException) {
            }
        }
        if (jdChequeManager.getCbBancos().getSelectedIndex() > 0) {
            query.append(" AND c.banco=").append(((ComboBoxWrapper<Banco>) jdChequeManager.getCbBancos().getSelectedItem()).getId());
        }
//        if (jdChequeManager.getCbBancoSucursales().getSelectedIndex() > 0) {
//            query.append(" AND c.banco_sucursal=").append(((BancoSucursal) jdChequeManager.getCbBancoSucursales().getSelectedItem()).getId());
//        }
        if (jdChequeManager.getCbEmisor().getSelectedIndex() > 0) {
            query.append(" AND c.cliente=").append(((Cliente) jdChequeManager.getCbEmisor().getSelectedItem()).getId());
        }
//        if (jdChequeManager.getCbLibrado().getSelectedIndex() > 0) {
//            query.append(" AND c.librado=").append(((Librado) jdChequeManager.getCbLibrado().getSelectedItem()).getId());
//        }
        if (jdChequeManager.getCbEstados().getSelectedIndex() > 0) {
            query.append(" AND c.estado=").append(((ChequeEstado) jdChequeManager.getCbEstados().getSelectedItem()).getId());
        }

        query.append(" ORDER BY ").append(orderByToQueryKeyList[jdChequeManager.getCbOrderBy().getSelectedIndex()]);
        LOG.debug(query.toString());
        cargarTablaChequeManager(query.toString());
        if (imprimir) {
            doChequePropioReport(query.toString());
        }
    }

    private void cargarTablaChequeManager(String query) {
        try {
            DefaultTableModel dtm = UTIL.getDtm(jdChequeManager.getjTable1());
            dtm.setRowCount(0);
            List<?> l = DAO.getNativeQueryResultList(query);
            for (Object object : l) {
                //"id", "Nº Cheque", "F. Cheque", "Emisor", "F. Cobro", "Banco", "Sucursal", "Importe", "Estado", "Cruzado", "Endosatario", "F. Endoso", "C. Ingreso", "C. Egreso", "Observacion", "Usuario"};
                dtm.addRow((Object[]) object);
            }
        } catch (DatabaseErrorException ex) {
            LOG.error(ex, ex);
            JOptionPane.showMessageDialog(jdChequeManager, ex.getLocalizedMessage(), "Error recuperando Cheques Propios", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doChequePropioReport(String query) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    ChequePropio getChequePropioInstance() {
        return EL_OBJECT;
    }
}
