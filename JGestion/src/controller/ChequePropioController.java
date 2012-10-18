package controller;

import controller.exceptions.DatabaseErrorException;
import controller.exceptions.MessageException;
import entity.Banco;
import entity.BancoSucursal;
import entity.ChequePropio;
import entity.ChequeTerceros;
import entity.Cliente;
import entity.CuentaBancaria;
import entity.Librado;
import entity.Proveedor;
import entity.enums.ChequeEstado;
import gui.JDABM;
import gui.JDChequesManager;
import gui.JDContenedor;
import gui.JDReRe;
import gui.PanelABMCheques;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.table.DefaultTableModel;
import jgestion.JGestionUtils;
import jpa.controller.ChequePropioJpaController;
import org.apache.log4j.Logger;
import utilities.general.UTIL;
import utilities.swing.components.ComboBoxWrapper;

/**
 *
 * @author Administrador
 */
public class ChequePropioController implements ActionListener {

    public final String CLASS_NAME = ChequePropio.class.getSimpleName();
    private ChequePropio EL_OBJECT;
//    private final String[] columnNames = {"id", "Nº Cheque", "Fecha Cheque", "Vencimiento", "Banco/Sucursal", "Estado", "Importe", "Librado"};
//    private final int[] columnWidths = {1, 60, 50, 50, 150, 50, 50, 50};
//    private final Class[] columnClassTypes = {};
    private JDContenedor contenedor;
    private static Logger LOGGER = Logger.getLogger(ChequePropioController.class);
    private JDABM abm;
    private PanelABMCheques panelABM;
    private JDChequesManager jdChequeManager;
    private ChequePropioJpaController jpaController;

    public ChequePropioController() {
        jpaController = new ChequePropioJpaController();
    }

    private void cargarContenedorTabla(String query) {
        DefaultTableModel dtm = (DefaultTableModel) contenedor.getjTable1().getModel();
        UTIL.limpiarDtm(dtm);
        List<ChequePropio> l;
        if (query == null || query.length() < 1) {
            l = DAO.getEntityManager().createNamedQuery(CLASS_NAME + ".findAll").getResultList();
        } else {
            // para cuando se usa el Buscador del ABM
            l = DAO.getEntityManager().createNativeQuery(query).getResultList();
        }

        for (ChequePropio o : l) {
            dtm.addRow(new Object[]{
                        o.getId(),
                        o.getNumero(),
                        UTIL.DATE_FORMAT.format(o.getFechaCheque()),
                        o.getFechaCobro() != null ? UTIL.DATE_FORMAT.format(o.getFechaCobro()) : null,
                        o.getBanco().getNombre() + "/" + o.getBancoSucursal().getNombre(),
                        o.getEstado(),
                        UTIL.PRECIO_CON_PUNTO.format(o.getImporte()),
                        o.getLibrado().getNombre()
                    });
        }
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
        abm = new JDABM(contenedor, "ABM - " + CLASS_NAME + "s", true, panelABM);
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
            if (contenedor != null) {
                if (boton.equals(contenedor.getbNuevo())) {
                    try {
                        EL_OBJECT = null;
                        initABM(false);
                        abm.setVisible(true);
                    } catch (MessageException ex) {
                        contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    } catch (Exception ex) {
                        contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                        LOGGER.error(ex);
                    }
                } else if (boton.equals(contenedor.getbModificar())) {
                    try {
                        int selectedRow = contenedor.getjTable1().getSelectedRow();
                        if (selectedRow > -1) {
                            EL_OBJECT = jpaController.find(Integer.valueOf((contenedor.getDTM().getValueAt(selectedRow, 0)).toString()));
                            if (EL_OBJECT == null) {
                                throw new MessageException("Debe elegir una fila de la tabla");
                            }
                            initABM(true);
                            abm.setVisible(true);

                        }
                    } catch (MessageException ex) {
                        contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    } catch (Exception ex) {
                        contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                        LOGGER.error(ex);
                    }

                } else if (boton.equals(contenedor.getbBorrar())) {
                    try {
                        int selectedRow = contenedor.getjTable1().getSelectedRow();
                        if (selectedRow > -1) {
                            EL_OBJECT = DAO.getEntityManager().find(ChequePropio.class,
                                    Integer.valueOf((contenedor.getDTM().getValueAt(selectedRow, 0)).toString()));
                        }
                        if (EL_OBJECT == null) {
                            throw new MessageException("No hay " + CLASS_NAME + " seleccionado");
                        }
                        jpaController.remove(EL_OBJECT);
                    } catch (MessageException ex) {
                        contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    } catch (Exception ex) {
                        contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                        LOGGER.error(ex);
                    }
                } else if (boton.getName().equalsIgnoreCase("Print")) {
                    //no implementado aun...
                } else if (boton.getName().equalsIgnoreCase("exit")) {
                    contenedor.dispose();
                    contenedor = null;
                }
            } //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="abm Actions">
            else if (abm != null && panelABM != null) {
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
                        abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    } catch (Exception ex) {
                        abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                        LOGGER.error(ex);
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
                        abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    }
                    UTIL.loadComboBox(panelABM.getCbBancos(), JGestionUtils.getWrappedBancos(new BancoController().findWithCuentasBancarias()), true);
                    UTIL.loadComboBox(panelABM.getCbBancoSucursales(), null, null, "<Seleccionar Banco>");
                } else if (boton.equals(panelABM.getbAddSucursal())) {
                    Banco bancoSelected = getSelectedBancoFromPanel();
                    try {
                        JDialog initABM = new BancoSucursalController().initABM(abm);
                        initABM.setLocationRelativeTo(abm);
                        initABM.setVisible(true);
                        UTIL.setSelectedItem(panelABM.getCbBancos(), bancoSelected.toString());
                    } catch (MessageException ex) {
                        abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    }
                } else if (boton.equals(panelABM.getbAddEmisor())) {
                    ProveedorController c = new ProveedorController();
                    JDialog jd = c.initContenedor(null, true);
                    jd.setLocationRelativeTo(abm);
                    jd.setVisible(true);
                    UTIL.loadComboBox(panelABM.getCbEmisor(), c.findEntities(), false);

                }
            }//</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="JComboBox">
            else if (e.getSource() instanceof JComboBox) {
                JComboBox combo = (JComboBox) e.getSource();
                //<editor-fold defaultstate="collapsed" desc="abm Actions">
                if (combo.equals(panelABM.getCbLibrado())) {
                    Librado libradoSelected = (Librado) panelABM.getCbLibrado().getSelectedItem();
                    //Cuando librado == 2 (AL PORTADOR)
                    panelABM.getCbEmisor().setEnabled(libradoSelected.getId() != 2);
                }
                //</editor-fold>
            }//</editor-fold>
        }// </editor-fold>
    }

    /**
     * Arma la query, para filtrar filas en la tabla del JDContenedor
     *
     * @param filtro atributo "nombre" del objeto; ej.: o.nombre ILIKE 'filtro%'
     */
    private void armarQuery(String filtro) {
        String query = null;
        if (filtro != null && filtro.length() > 0) {
            query = "SELECT * FROM " + CLASS_NAME + " o WHERE o.numero >= " + filtro;
        }
        cargarContenedorTabla(query);
    }

    private void setPanel(ChequePropio o) {
        panelABM.setDcCheque(o.getFechaCheque());
        panelABM.setDcCobro(o.getFechaCobro());
        panelABM.getTfNumero().setText(o.getNumero().toString());
        panelABM.getTfImporte().setText(UTIL.PRECIO_CON_PUNTO.format(o.getImporte()));
        UTIL.setSelectedItem(panelABM.getCbBancos(), o.getBanco().getNombre());
        UTIL.setSelectedItem(panelABM.getCbBancoSucursales(), o.getBancoSucursal().getNombre());
        UTIL.setSelectedItem(panelABM.getCbLibrado(), o.getLibrado().getNombre());
        if (o.getLibrado().getId() == 1) {
            UTIL.setSelectedItem(panelABM.getCbEmisor(), o.getProveedor());
        } else {
            // AL PORTADOR, no se emitió a nadie
        }
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
        Date fechaEmisionCheque, fechaCobro, fechaEndoso = null;
        Long numero = null;
        BigDecimal importe = null;
        Banco banco;
        BancoSucursal sucursal = null;
        Librado librado;
        Proveedor proveedor = ((ComboBoxWrapper<Proveedor>) panelABM.getCbEmisor().getSelectedItem()).getEntity();
        boolean cruzado, propio, endosado;
        String endosatario = null, observacion = null;
        CuentaBancaria cuentaBancaria;

        if (panelABM.getDcCheque() == null) {
            throw new MessageException("Debe ingresar la Fecha del Cheque.");
        }
        if (panelABM.getDcCobro() == null) {
            throw new MessageException("Debe ingresar la Fecha de cobro del Cheque.");
        }
        fechaEmisionCheque = panelABM.getDcCheque();
        fechaCobro = panelABM.getDcCobro();

        if (UTIL.compararIgnorandoTimeFields(fechaEmisionCheque, fechaCobro) > 0) {
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
        librado = (Librado) panelABM.getCbLibrado().getSelectedItem();
//        if (librado.getId() == 1) {
//            proveedor = (Proveedor) panelABM.getCbEmisor().getSelectedItem();
//        }
        cruzado = panelABM.getCheckCruzado().isSelected();
        if (!panelABM.getTaObservacion().getText().isEmpty()) {
            observacion = panelABM.getTaObservacion().getText();
        }
        ChequePropio newCheque = new ChequePropio(proveedor, numero, banco, sucursal, importe, fechaEmisionCheque, fechaCobro, cruzado, observacion, ChequeEstado.CARTERA, endosatario, fechaEndoso, UsuarioController.getCurrentUser(), librado, cuentaBancaria);
        return newCheque;
    }

    private void checkConstraints(ChequePropio object) throws DatabaseErrorException, MessageException {
        String idQuery = "";
        if (object.getId() != null) {
            idQuery = "o.id<>" + object.getId() + " AND ";
        }
        try {
            DAO.getEntityManager().createQuery("SELECT o FROM " + CLASS_NAME + " o "
                    + "WHERE " + idQuery
                    + " o.numero=" + object.getNumero(), object.getClass()).getSingleResult();
            throw new MessageException("Ya existe un " + CLASS_NAME + " con este número");
        } catch (NoResultException noResultException) {
        }
    }

    public JDialog initManager(JFrame owner, ActionListener buttonListener) throws MessageException {
        jdChequeManager = (JDChequesManager) new ChequeTercerosController().initManager(owner, buttonListener, null);
        jdChequeManager.getLabelEmisor().setText("Emitido a");
        UTIL.loadComboBox(jdChequeManager.getCbEmisor(), JGestionUtils.getWrappedProveedores(new ProveedorController().findEntities()), true);
        jdChequeManager.setTitle("Administración de Cheques Propios");
        return jdChequeManager;
    }

    JDialog initManager(Window parent, boolean selectionMode) throws MessageException {
        jdChequeManager = new JDChequesManager(parent, true);
        List<Banco> l = new BancoController().findWithCuentasBancarias();
        if (l.isEmpty()) {
            throw new MessageException("Para emitor cheques propios tiene que crear una Cuenta bancaria relacionado a un Banco."
                    + "\nDatos Generales > Bancos/Sucursales/Cuentas > Cuentas Bancarias");
        }
        UTIL.loadComboBox(jdChequeManager.getCbBancos(), JGestionUtils.getWrappedBancos(l), true);
//        UTIL.loadComboBox(jdChequeManager.getCbBancoSucursales(), null, null, "<Seleccionar un Banco>");
        UTIL.loadComboBox(jdChequeManager.getCbEmisor(), JGestionUtils.getWrappedProveedores(new ProveedorController().findEntities()), true);
        UTIL.loadComboBox(jdChequeManager.getCbLibrado(), JGestionUtils.getWrappedLibrado(new LibradoJpaController().findEntities()), true);
        UTIL.loadComboBox(jdChequeManager.getCbEstados(), Arrays.asList(ChequeEstado.values()), true);
        jdChequeManager.getCbBancos().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jdChequeManager.getCbBancos().getSelectedIndex() > 0) {
                    Banco banco = ((ComboBoxWrapper<Banco>) jdChequeManager.getCbBancos().getSelectedItem()).getEntity();
                    List<CuentaBancaria> cuentasbancaria = banco.getCuentasbancaria();
                    UTIL.loadComboBox(jdChequeManager.getCbCuentaBancaria(), JGestionUtils.getWrappedCuentasBancarias(cuentasbancaria), true);
//                    UTIL.loadComboBox(jdChequeManager.getCbBancoSucursales(), new BancoSucursalController().findBy(banco), true);
                } else {
//                    UTIL.loadComboBox(jdChequeManager.getCbBancoSucursales(), null, null, "<Seleccionar un Banco>");
                }
            }
        });
        jdChequeManager.addButtonListener(this);
        return jdChequeManager;
    }

    @SuppressWarnings("unchecked")
    private Banco getSelectedBancoFromPanel() {
        return ((ComboBoxWrapper<Banco>) panelABM.getCbBancos().getSelectedItem()).getEntity();
    }

    private void initPanelABM() {
        panelABM = new PanelABMCheques();
        panelABM.setUIChequePropio();
        UTIL.loadComboBox(panelABM.getCbBancos(), JGestionUtils.getWrappedBancos(new BancoController().findWithCuentasBancarias()), false);
//        UTIL.loadComboBox(panelABM.getCbBancoSucursales(), null, null, "<Seleccionar un Banco>");
        UTIL.loadComboBox(panelABM.getCbEmisor(), JGestionUtils.getWrappedProveedores(new ProveedorController().findEntities()), false);
        UTIL.loadComboBox(panelABM.getCbLibrado(), new LibradoJpaController().findEntities(), false);

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
        panelABM.getCbLibrado().addActionListener(this);
        panelABM.getbAddEmisor().addActionListener(this);
        panelABM.getbAddBanco().addActionListener(this);
        panelABM.getbAddSucursal().addActionListener(this);
    }
}
