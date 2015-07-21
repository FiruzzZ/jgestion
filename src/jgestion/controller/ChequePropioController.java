package jgestion.controller;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.DynamicReportBuilder;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import ar.com.fdvs.dj.domain.constants.Page;
import jgestion.controller.exceptions.DatabaseErrorException;
import jgestion.controller.exceptions.MessageException;
import jgestion.entity.Banco;
import jgestion.entity.BancoSucursal;
import jgestion.entity.ChequePropio;
import jgestion.entity.CuentaBancaria;
import jgestion.entity.CuentabancariaMovimientos;
import jgestion.entity.Proveedor;
import jgestion.entity.UsuarioAcciones;
import jgestion.entity.enums.ChequeEstado;
import generics.CustomABMJDialog;
import generics.GenericBeanCollection;
import jgestion.gui.JDABM;
import jgestion.gui.JDChequesManager;
import jgestion.gui.JDContenedor;
import jgestion.gui.PanelABMCheques;
import jgestion.gui.PanelChequesColumnsReport;
import generics.gui.GroupLayoutPanelBuilder;
import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.persistence.NoResultException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import jgestion.JGestionUtils;
import jgestion.jpa.controller.ChequePropioJpaController;
import jgestion.jpa.controller.CuentabancariaMovimientosJpaController;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utilities.general.UTIL;
import utilities.swing.RowColorRender;
import utilities.general.EntityWrapper;
import utilities.swing.components.FormatRenderer;
import utilities.swing.components.NumberRenderer;

/**
 *
 * @author FiruzzZ
 */
public class ChequePropioController implements ActionListener {

    private static final Logger LOG = LogManager.getLogger();
    private ChequePropio EL_OBJECT;
    private JDContenedor contenedor;
    private JDABM abm;
    private PanelABMCheques panelABM;
    private ChequePropioJpaController jpaController;
    private JDChequesManager jdChequeManager;
    //exclusively related to the GUI
    //Este se pone en el comboBox
    private final String[] orderByToComboBoxList = {"N° Cheque", "Fecha de Emisión", "Fecha de Cobro", "Importe", "Banco/Sucursal", "Proveedor", "Estado"};
    //Y este es el equivalente (de lo seleccionado en el combo) para el SQL.
    private final String[] orderByToQueryKeyList = {"numero", "fecha_cheque", "fecha_cobro", "importe", "banco.nombre, banco_sucursal.nombre", "proveedor.nombre", "estado"};

    public ChequePropioController() {
        jpaController = new ChequePropioJpaController();
    }

    /**
     * Levanta la UI para settear un chequePropio, sino que retorna la instancia lista para ser
     * persistida.
     *
     * @param owner
     * @param isEditing si se debe persistir la instancia creada cuando se Acepte el formulario
     * @param proveedor if {@code proveedor != null}, the combo is selected with this and the
     * combobox is disabled.
     * @return
     * @throws MessageException
     */
    ChequePropio initABM(Window owner, boolean isEditing, Proveedor proveedor) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.TESORERIA);
        if (isEditing && EL_OBJECT == null) {
            throw new MessageException("Debe elegir una fila de la tabla");
        }
        initPanelABM(isEditing && EL_OBJECT != null, proveedor);

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
            //<editor-fold defaultstate="collapsed" desc="ABM">
            if (abm != null && panelABM != null) {
                if (boton.equals(abm.getbAceptar())) {
                    try {
                        setEntity();
                        checkConstraints(EL_OBJECT);
                        if (panelABM.persist()) {
                            String msg = EL_OBJECT.getId() == null ? "Registrado" : "Modificado";
                            if (EL_OBJECT.getId() == null) {
                                jpaController.persist(EL_OBJECT);
                            } else {
                                jpaController.merge(EL_OBJECT);
                            }
                            abm.showMessage(msg, jpaController.getEntityClass().getSimpleName(), 1);
                        }
                        abm.dispose();
                    } catch (MessageException ex) {
                        abm.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                    } catch (Exception ex) {
                        abm.showMessage(ex.getLocalizedMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                        LOG.error(ex, ex);
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
                    UTIL.loadComboBox(panelABM.getCbEmisor(), JGestionUtils.getWrappedProveedores(c.findAll()), false);

                }
            }//</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Administrador">
            else if (jdChequeManager != null) {
                if (boton.equals(jdChequeManager.getbBuscar())) {
                    try {
                        cargarTablaChequeManager(armarQuery(), false);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "Error ejecutando consulta", JOptionPane.ERROR_MESSAGE);
                    }
                } else if (boton.equals(jdChequeManager.getBtnNuevo())) {
                } else if (boton.equals(jdChequeManager.getBtnModificar())) {
                    try {
                        EL_OBJECT = jpaController.find(getSelectedChequeID());
                        initABM(jdChequeManager, true, EL_OBJECT.getProveedor());
                        EL_OBJECT = null;
                        cargarTablaChequeManager(armarQuery(), false);
                    } catch (MessageException ex) {
                        ex.displayMessage(jdChequeManager);
                    }
                } else if (boton.equals(jdChequeManager.getbAnular())) {
                    try {
                        ChequePropio cheque = jpaController.find(getSelectedChequeID());
                        if (!cheque.getChequeEstado().equals(ChequeEstado.CARTERA)) {
                            throw new MessageException("Solo los cheques en " + ChequeEstado.CARTERA + " pueden ser anulados");
                        }
                        showAnulacionDialog(jdChequeManager, cheque);
                    } catch (MessageException ex) {
                        ex.displayMessage(jdChequeManager);
                    }
                    cargarTablaChequeManager(armarQuery(), false);
                } else if (boton.equals(jdChequeManager.getbDepositar())) {
                    try {
                        ChequePropio cheque = jpaController.find(getSelectedChequeID());
                        if (cheque.getChequeEstado().equals(ChequeEstado.CARTERA)) {
                            new CuentabancariaController().initDebitoUI(cheque);
                        } else {
                            throw new MessageException("Solo los cheques en " + ChequeEstado.CARTERA + " pueden ser " + ChequeEstado.DEBITADO);
                        }
                        cargarTablaChequeManager(armarQuery(), false);
                    } catch (MessageException ex) {
                        ex.displayMessage(jdChequeManager);
                    }
                } else if (boton.equals(jdChequeManager.getBtnImprimir())) {
                    try {
                        cargarTablaChequeManager(armarQuery(), true);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "Error ejecutando consulta", JOptionPane.ERROR_MESSAGE);
                        LOG.error("Cargando tabla cheques propios", ex);
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
        if (o.getBancoSucursal() != null) {
            UTIL.setSelectedItem(panelABM.getCbBancoSucursales(), o.getBancoSucursal().getNombre());
        }
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
    private void setEntity() throws MessageException {
        Date fechaCheque, fechaCobro, fechaEndoso = null;
        Long numero = null;
        BigDecimal importe = null;
        Banco banco;
        BancoSucursal sucursal = null;
        Proveedor proveedor = ((EntityWrapper<Proveedor>) panelABM.getCbEmisor().getSelectedItem()).getEntity();
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
        cuentaBancaria = ((EntityWrapper<CuentaBancaria>) panelABM.getCbCuentaBancaria().getSelectedItem()).getEntity();
        try {
            sucursal = (BancoSucursal) panelABM.getCbBancoSucursales().getSelectedItem();
        } catch (ClassCastException e) {
//            throw new MessageException("Sucursal de Banco no válida");
        }
        cruzado = panelABM.getCheckCruzado().isSelected();
        if (!panelABM.getTaObservacion().getText().isEmpty()) {
            observacion = panelABM.getTaObservacion().getText();
        }
        if (EL_OBJECT != null) {
            //los objetos con herencia no puede ser inicializados con new porque el ORM no reconoce la asociación
            EL_OBJECT.setProveedor(proveedor);
            EL_OBJECT.setNumero(numero);
            EL_OBJECT.setBanco(banco);
            EL_OBJECT.setBancoSucursal(sucursal);
            EL_OBJECT.setImporte(importe);
            EL_OBJECT.setFechaCheque(fechaCheque);
            EL_OBJECT.setFechaCobro(fechaCobro);
            EL_OBJECT.setCruzado(cruzado);
            EL_OBJECT.setObservacion(observacion);
//            EL_OBJECT.setEstado(ChequeEstado.CARTERA.getId());
            EL_OBJECT.setEndosatario(endosatario);
            EL_OBJECT.setFechaEndoso(fechaEndoso);
            EL_OBJECT.setCuentabancaria(cuentaBancaria);
        } else {
            EL_OBJECT = new ChequePropio(proveedor, numero, banco, sucursal, importe, fechaCheque,
                    fechaCobro, cruzado, observacion, ChequeEstado.CARTERA, endosatario, fechaEndoso,
                    UsuarioController.getCurrentUser(), cuentaBancaria);
        }
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
            UsuarioController.checkPermiso(PermisosController.PermisoDe.TESORERIA);
        }
        List<Banco> l = new BancoController().findWithCuentasBancarias(true);
        if (l.isEmpty()) {
            throw new MessageException("Para emitir cheques propios, primero tiene que crear una Cuenta bancaria relacionado a un Banco."
                    + "\nDatos Generales > Bancos/Cuentas > Cuentas Bancarias");
        }
        jdChequeManager = new JDChequesManager(parent, true);
        jdChequeManager.getBtnNuevo().setEnabled(false);
//        jdChequeManager.getBtnModificar().setEnabled(false);
        jdChequeManager.getbAnular().setEnabled(true);
        jdChequeManager.getbDepositar().setText("Debidar");
        jdChequeManager.getLabelEmisor().setText("Emitido a");
        jdChequeManager.setTitle("Administración de Cheques Propios");
        jdChequeManager.getCbBancoSucursales().setVisible(false);
        jdChequeManager.getLabelSucursales().setVisible(false);
        UTIL.getDefaultTableModel(jdChequeManager.getjTable1(),
                // los valores de las columnas 4 (F. Cobro) y 7 (Estado) son usados en otros lados!!! ojo piojo!
                new String[]{"id", "Nº Cheque", "F. Cheque", "Emitido a", "F. Cobro", "Banco", "Importe", "Estado", "Cruzado", "Endosatario", "F. Endoso", "C. Ingreso", "C. Egreso", "Observacion", "Usuario"},
                new int[]{1, 80, 80, 100, 80, 100, 100, 80, 50, 100, 100, 150, 150, 100, 80},
                new Class<?>[]{Integer.class, Number.class, null, null, null, null, Number.class, null, Boolean.class, null});
        jdChequeManager.getjTable1().getColumnModel().getColumn(2).setCellRenderer(FormatRenderer.getDateRenderer());
        jdChequeManager.getjTable1().getColumnModel().getColumn(4).setCellRenderer(FormatRenderer.getDateRenderer());
        jdChequeManager.getjTable1().getColumnModel().getColumn(6).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        jdChequeManager.getjTable1().setDefaultRenderer(Object.class, new RowColorRender() {
            private static final long serialVersionUID = 1L;
            final Date hoy = new Date();

            @Override
            public Color condicionByRow(int row) {
                Color c = null;
                Date fechaCobro = (Date) jdChequeManager.getjTable1().getModel().getValueAt(row, 4);
                String estado = jdChequeManager.getjTable1().getModel().getValueAt(row, 7).toString();
                if (fechaCobro.before(hoy) && ChequeEstado.CARTERA.toString().equalsIgnoreCase(estado)) {
                    c = Color.RED;
                }
                return c;
            }
        });
        UTIL.hideColumnTable(jdChequeManager.getjTable1(), 0);
        UTIL.loadComboBox(jdChequeManager.getCbBancos(), JGestionUtils.getWrappedBancos(l), true);
        UTIL.loadComboBox(jdChequeManager.getCbEmisor(), JGestionUtils.getWrappedProveedores(new ProveedorController().findAll()), true);
        UTIL.loadComboBox(jdChequeManager.getCbEstados(), Arrays.asList(ChequeEstado.values()), true);
        UTIL.loadComboBox(jdChequeManager.getCbOrderBy(), Arrays.asList(orderByToComboBoxList), false);
        jdChequeManager.getCbOrderBy().setSelectedIndex(2);
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
                    Banco banco = ((EntityWrapper<Banco>) jdChequeManager.getCbBancos().getSelectedItem()).getEntity();
                    List<CuentaBancaria> cuentasbancaria = banco.getCuentasbancaria();
                    UTIL.loadComboBox(jdChequeManager.getCbCuentaBancaria(), JGestionUtils.getWrappedCuentasBancarias(cuentasbancaria), true);
//                    UTIL.loadComboBox(jdChequeManager.getCbBancoSucursales(), new BancoSucursalController().findBy(banco), true);
                } else {
                    UTIL.loadComboBox(jdChequeManager.getCbCuentaBancaria(), null, true);
//                    UTIL.loadComboBox(jdChequeManager.getCbBancoSucursales(), null, null, "<Seleccionar un Banco>");
                }
            }
        });
        jdChequeManager.addButtonsListener(this);
    }

    @SuppressWarnings("unchecked")
    private Banco getSelectedBancoFromPanel() {
        return ((EntityWrapper<Banco>) panelABM.getCbBancos().getSelectedItem()).getEntity();
    }

    private void initPanelABM(boolean persistir, Proveedor proveedor) {
        panelABM = new PanelABMCheques();
        panelABM.setUIChequePropio();
        panelABM.setPersistible(persistir);
        UTIL.loadComboBox(panelABM.getCbBancos(), JGestionUtils.getWrappedBancos(new BancoController().findWithCuentasBancarias(true)), false);
//        UTIL.loadComboBox(panelABM.getCbBancoSucursales(), null, null, "<Seleccionar un Banco>");
        UTIL.loadComboBox(panelABM.getCbEmisor(), JGestionUtils.getWrappedProveedores(new ProveedorController().findAll()), false);
        if (proveedor != null) {
            panelABM.getCbEmisor().setEnabled(false);
            UTIL.setSelectedItem(panelABM.getCbEmisor(), proveedor);
        }
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

    private String armarQuery() {
        StringBuilder query = new StringBuilder("SELECT "
                + " c.id, c.numero, c.fecha_cheque, ccc.nombre as cliente, c.fecha_cobro,"
                + " banco.nombre as banco, c.importe, cheque_estado.nombre as estado, c.cruzado"
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
            query.append(" AND c.fecha_cheque >='").append(UTIL.DATE_FORMAT.format(jdChequeManager.getDcEmisionDesde())).append("'");
        }
        if (jdChequeManager.getDcEmisionHasta() != null) {
            query.append(" AND c.fecha_cheque <='").append(UTIL.DATE_FORMAT.format(jdChequeManager.getDcEmisionHasta())).append("'");
        }
        if (jdChequeManager.getDcCobroDesde() != null) {
            query.append(" AND c.fecha_cobro >='").append(UTIL.DATE_FORMAT.format(jdChequeManager.getDcCobroDesde())).append("'");
        }
        if (jdChequeManager.getDcCobroHasta() != null) {
            query.append(" AND c.fecha_cobro <='").append(UTIL.DATE_FORMAT.format(jdChequeManager.getDcCobroHasta())).append("'");
        }
        if (jdChequeManager.getTfImporte().getText().trim().length() > 0) {
            try {
                Double importe = Double.valueOf(jdChequeManager.getTfImporte().getText());
                query.append(" AND c.importe").append(jdChequeManager.getCbImporteCondicion().getSelectedItem().toString()).append(importe);
            } catch (NumberFormatException numberFormatException) {
            }
        }
        if (jdChequeManager.getCbBancos().getSelectedIndex() > 0) {
            query.append(" AND c.banco=").append(((EntityWrapper<Banco>) jdChequeManager.getCbBancos().getSelectedItem()).getId());
        }
        if (jdChequeManager.getCbEmisor().getSelectedIndex() > 0) {
            query.append(" AND c.proveedor=").append(((EntityWrapper<Proveedor>) jdChequeManager.getCbEmisor().getSelectedItem()).getId());
        }
        if (jdChequeManager.getCbEstados().getSelectedIndex() > 0) {
            query.append(" AND c.estado=").append(((ChequeEstado) jdChequeManager.getCbEstados().getSelectedItem()).getId());
        }

        query.append(" ORDER BY ").append(orderByToQueryKeyList[jdChequeManager.getCbOrderBy().getSelectedIndex()]);
        LOG.debug(query.toString());
        return query.toString();
    }

    private void cargarTablaChequeManager(String query, boolean imprimir) {
        try {
            DefaultTableModel dtm = UTIL.getDtm(jdChequeManager.getjTable1());
            dtm.setRowCount(0);
            List<?> l = DAO.getNativeQueryResultList(query);
            for (Object object : l) {
                //"id", "Nº Cheque", "F. Cheque", "Emisor", "F. Cobro", "Banco", "Importe", "Estado", "Cruzado", "Endosatario", "F. Endoso", "C. Ingreso", "C. Egreso", "Observacion", "Usuario"};
                dtm.addRow((Object[]) object);
            }
            if (imprimir) {
                if (jdChequeManager.getjTable1().getModel().getRowCount() < 1) {
                    JOptionPane.showMessageDialog(jdChequeManager, "No sea han filtrados cheques para crear el reporte");
                } else {
                    doChequePropioReport();
                }
            }
        } catch (DatabaseErrorException ex) {
            LOG.error(ex, ex);
            JOptionPane.showMessageDialog(jdChequeManager, ex.getLocalizedMessage() + ":\n" + ex.getCause(), "Error recuperando Cheques Propios", JOptionPane.ERROR_MESSAGE);
        }
        totalizarSegunFechaCobro();
    }

    private void totalizarSegunFechaCobro() {
        BigDecimal $cobrables = BigDecimal.ZERO;
        BigDecimal $30 = BigDecimal.ZERO;
        BigDecimal $60 = BigDecimal.ZERO;
        BigDecimal $90 = BigDecimal.ZERO;
        BigDecimal $90mas = BigDecimal.ZERO;
        Date now = new Date();
        DefaultTableModel dtm = (DefaultTableModel) jdChequeManager.getjTable1().getModel();
        final long dayOnMilli = 24 * 60 * 60 * 1000;
        for (int row = 0; row < dtm.getRowCount(); row++) {
            if (dtm.getValueAt(row, 7).toString().equalsIgnoreCase(ChequeEstado.CARTERA.name())) {
                Date fechaCobro = (Date) dtm.getValueAt(row, 4);
                BigDecimal importe = (BigDecimal) dtm.getValueAt(row, 6);
                long diff = fechaCobro.getTime() - now.getTime();
                long diffDays = diff / (dayOnMilli);
                if (diffDays <= 0) {
                    $cobrables = $cobrables.add(importe);
                } else if (diffDays <= 30) {
                    $30 = $30.add(importe);
                } else if (diffDays <= 60) {
                    $60 = $60.add(importe);
                } else if (diffDays <= 90) {
                    $90 = $90.add(importe);
                } else if (diffDays > 90) {
                    $90mas = $90mas.add(importe);
                }
            }
        }
        jdChequeManager.getTfCobrables().setText(UTIL.DECIMAL_FORMAT.format($cobrables));
        jdChequeManager.getTf30().setText(UTIL.DECIMAL_FORMAT.format($30));
        jdChequeManager.getTf60().setText(UTIL.DECIMAL_FORMAT.format($60));
        jdChequeManager.getTf90().setText(UTIL.DECIMAL_FORMAT.format($90));
        jdChequeManager.getTf90mas().setText(UTIL.DECIMAL_FORMAT.format($90mas));
    }

    private void doChequePropioReport() {
        final PanelChequesColumnsReport p = new PanelChequesColumnsReport();
        final JDABM jd = new JDABM(null, "Informe: Cheques Propios", true, p);
        jd.getbAceptar().addActionListener(new ActionListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                try {
                    Reportes r = new Reportes(null, true);
                    r.showWaitingDialog();
                    //"id",
                    //"Nº Cheque", "F. Cheque", "Emisor", "F. Cobro", "Banco", 
                    //"Importe", "Estado", "Cruzado", "Endosatario", "F. Endoso", 
                    //"C. Ingreso", "C. Egreso", "Observacion", "Usuario"};
                    DefaultTableModel dtm = (DefaultTableModel) jdChequeManager.getjTable1().getModel();
                    List<GenericBeanCollection> data = new ArrayList<>(dtm.getRowCount());
                    for (int row = 0; row < dtm.getRowCount(); row++) {
                        data.add(new GenericBeanCollection(
                                dtm.getValueAt(row, 1),
                                dtm.getValueAt(row, 2),
                                dtm.getValueAt(row, 3),
                                dtm.getValueAt(row, 4),
                                dtm.getValueAt(row, 5),
                                dtm.getValueAt(row, 6),
                                dtm.getValueAt(row, 7),
                                dtm.getValueAt(row, 8),
                                dtm.getValueAt(row, 11),
                                dtm.getValueAt(row, 12),
                                dtm.getValueAt(row, 13),
                                dtm.getValueAt(row, 14)));
                    }

                    DynamicReportBuilder drb = new DynamicReportBuilder();
                    Style currencyStyle = new Style();
                    currencyStyle.setFont(Font.ARIAL_MEDIUM);
                    currencyStyle.setHorizontalAlign(HorizontalAlign.RIGHT);
                    Style textStyle = new Style();
                    textStyle.setFont(Font.ARIAL_MEDIUM);
                    drb.addColumn(ColumnBuilder.getNew().setColumnProperty("o1", Object.class).setTitle("Número").setWidth(60).setStyle(currencyStyle).setFixedWidth(true).build());
                    drb.addColumn(ColumnBuilder.getNew().setColumnProperty("o6", Object.class).setTitle("Importe").setWidth(80).setStyle(currencyStyle).setPattern("¤ #,##0.00").setFixedWidth(true).build());
                    if (p.getCheckFechaCheque()) {
                        drb.addColumn(ColumnBuilder.getNew().setColumnProperty("o2", Object.class).setTitle("F. Cheque").setWidth(60).setPattern("dd/MM/yyyy").setFixedWidth(true).build());
                    }
                    if (p.getCheckEmisor()) {
                        drb.addColumn(ColumnBuilder.getNew().setColumnProperty("o3", Object.class).setTitle("Emisor").setWidth(200).setStyle(textStyle).setFixedWidth(true).build());
                    }
                    if (p.getCheckFechaCobro()) {
                        drb.addColumn(ColumnBuilder.getNew().setColumnProperty("o4", Object.class).setTitle("F. Cobro").setWidth(60).setPattern("dd/MM/yyyy").setFixedWidth(true).build());
                    }
                    if (p.getCheckBanco()) {
                        drb.addColumn(ColumnBuilder.getNew().setColumnProperty("o5", Object.class).setTitle("Banco").setWidth(80).setStyle(textStyle).build());
                    }
                    if (p.getCheckEstado()) {
                        drb.addColumn(ColumnBuilder.getNew().setColumnProperty("o7", Object.class).setTitle("Estado").setWidth(45).setStyle(textStyle).build());
                    }
                    if (p.getCheckCruzado()) {
                        drb.addColumn(ColumnBuilder.getNew().setColumnProperty("o8", Object.class).setTitle("Cruzado").setWidth(40).setStyle(textStyle).build());
                    }
                    if (p.getCheckCompIngreso()) {
                        drb.addColumn(ColumnBuilder.getNew().setColumnProperty("o9", Object.class).setTitle("Comp. Ingreso").setWidth(100).setStyle(textStyle).build());
                    }
                    if (p.getCheckCompEgreso()) {
                        drb.addColumn(ColumnBuilder.getNew().setColumnProperty("o10", Object.class).setTitle("Comp. Egreso").setWidth(100).setStyle(textStyle).build());
                    }
                    if (p.getCheckObservacion()) {
                        drb.addColumn(ColumnBuilder.getNew().setColumnProperty("o11", Object.class).setTitle("Observ.").setWidth(100).setStyle(textStyle).build());
                    }
                    if (p.getCheckUsuario()) {
                        drb.addColumn(ColumnBuilder.getNew().setColumnProperty("o12", Object.class).setTitle("Usuario").setWidth(50).setStyle(textStyle).setFixedWidth(true).build());
                    }
                    if (p.getCheckLandscapePage()) {
                        drb.setPageSizeAndOrientation(Page.Page_A4_Landscape());
                    }
                    drb.setTitle("Informe: Cheques Propios")
                            .setSubtitle(UTIL.TIMESTAMP_FORMAT.format(new Date()))
                            .setPrintBackgroundOnOddRows(true)
                            .setUseFullPageWidth(true);
                    //                    SubReportBuilder srb = new SubReportBuilder();
                    //                    srb.setPathToReport(Reportes.FOLDER_REPORTES + "JGestion_membrete.jasper");
                    //                    srb.setDataSource("");
                    //                    Subreport sr = srb.build();
                    //                    drb.addSubreportInGroupHeader(0, sr);
                    DynamicReport dr = drb.build();
                    JRDataSource ds = new JRBeanCollectionDataSource(data);
                    JasperPrint jp = DynamicJasperHelper.generateJasperPrint(dr, new ClassicLayoutManager(), ds);
                    r.setjPrint(jp);
                    r.viewReport();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "algo salió mal");
                    LOG.error("Creando dynamic report chequepropios", ex);
                }
            }
        });
        jd.setVisible(true);
    }

    ChequePropio getChequePropioInstance() {
        return EL_OBJECT;
    }

    private void showAnulacionDialog(Window owner, final ChequePropio cheque) throws MessageException {
        final CuentabancariaMovimientos asiento = new CuentabancariaMovimientosJpaController().findBy(cheque);
        if (asiento != null && asiento.isConciliado()) {
            throw new MessageException("El movimiento bancario N° " + asiento.getId() + " relacionado al cheque ya a sido conciliado.");
        }
        final GroupLayoutPanelBuilder glpb = new GroupLayoutPanelBuilder();
        final JTextArea tfObservacion = new JTextArea(3, 10);
        tfObservacion.setText(cheque.getObservacion());
        tfObservacion.setLineWrap(true);
        tfObservacion.setWrapStyleWord(true);
        glpb.addFormItem(new JLabel("Observación"), tfObservacion);
        JPanel panel = glpb.build();
        final CustomABMJDialog dialog = new CustomABMJDialog(owner, panel, "Anulación de Cheque Propio", true,
                "¿Confirma la anulación del Cheque?"
                + "\nN°: " + cheque.getNumero()
                + "\nImporte: " + UTIL.DECIMAL_FORMAT.format(cheque.getImporte()));
        dialog.setToolBarVisible(false);
        dialog.getBtnAceptar().addActionListener(new ActionListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                cheque.setObservacion(tfObservacion.getText().trim().isEmpty() ? null : tfObservacion.getText().trim());
                cheque.setEstado(ChequeEstado.ANULADO.getId());
                jpaController.merge(cheque);
                if (asiento != null) {
                    asiento.setAnulada(true);
                    new CuentabancariaMovimientosJpaController().merge(asiento);
                }
                UsuarioAcciones ua = new UsuarioAcciones('u', "Anulación cheque propio N° " + cheque.getNumero(), null, ChequePropio.class.getSimpleName(), cheque.getId(), UsuarioController.getCurrentUser());
                new UsuarioAccionesController().create(ua);
                dialog.dispose();
            }
        });
        dialog.getBtnCancelar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        dialog.setVisible(true);
    }

    private Integer getSelectedChequeID() throws MessageException {
        Integer chequeID = (Integer) UTIL.getSelectedValueFromModel(jdChequeManager.getjTable1(), 0);
        if (chequeID == null) {
            throw new MessageException("Debe seleccionar un registro de la tabla");
        }
        return chequeID;
    }
}
