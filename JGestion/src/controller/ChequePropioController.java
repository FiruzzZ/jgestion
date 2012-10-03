package controller;

import controller.exceptions.DatabaseErrorException;
import controller.exceptions.MessageException;
import entity.Banco;
import entity.BancoSucursal;
import entity.ChequePropio;
import entity.ChequeTerceros;
import entity.Cliente;
import entity.Cuentabancaria;
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
    private final String[] columnNames = {"id", "Nº Cheque", "Fecha Cheque", "Vencimiento", "Banco/Sucursal", "Estado", "Importe", "Librado"};
    private final int[] columnWidths = {1, 60, 50, 50, 150, 50, 50, 50};
    private final Class[] columnClassTypes = {};
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
        panelABM = new PanelABMCheques();
        UTIL.loadComboBox(panelABM.getCbBancos(), new BancoController().findEntities(), true);
        UTIL.loadComboBox(panelABM.getCbBancoSucursales(), null, null, "<Seleccionar un Banco>");
        UTIL.loadComboBox(panelABM.getCbEmisor(), new ProveedorController().findEntities(), false);
        UTIL.loadComboBox(panelABM.getCbLibrado(), new LibradoJpaController().findEntities(), false);
        //Default para ChequePropio
        panelABM.getLabelEmisor().setText("Emisor (Proveedor)");
        panelABM.getCheckEndosado().setSelected(false);
        panelABM.getCheckEndosado().setEnabled(false);

        panelABM.getCbBancos().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (panelABM.getCbBancos().getSelectedIndex() > 0) {
                    Banco banco = (Banco) panelABM.getCbBancos().getSelectedItem();
                    UTIL.loadComboBox(panelABM.getCbBancoSucursales(), new BancoSucursalController().findBy(banco), false);
                } else {
                    UTIL.loadComboBox(panelABM.getCbBancoSucursales(), null, null, "<Seleccionar un Banco>");
                }
            }
        });
        panelABM.getCbLibrado().addActionListener(this);
        panelABM.getbAddEmisor().addActionListener(this);
        panelABM.getbAddBanco().addActionListener(this);
        panelABM.getbAddSucursal().addActionListener(this);

        if (isEditing) {
            setPanel(EL_OBJECT);
        }
        abm = new JDABM(true, contenedor, panelABM);
        abm.setTitle("ABM - " + CLASS_NAME + "s");
        abm.setListener(this);
        return abm;
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
                        if (EL_OBJECT == null) {
                            EL_OBJECT = new ChequePropio();
                        }
                        EL_OBJECT = setEntity(EL_OBJECT);
                        checkConstraints(EL_OBJECT);
                        String msg = EL_OBJECT.getId() == null ? "Registrado" : "Modificado";
                        if (EL_OBJECT.getId() == null) {
                            jpaController.create(EL_OBJECT);
                        } else {
                            jpaController.merge(EL_OBJECT);
                        }
                        abm.showMessage(msg, CLASS_NAME, 1);
                        cargarContenedorTabla(null);
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
                    UTIL.loadComboBox(panelABM.getCbBancos(), new BancoController().findEntities(), true);
                    UTIL.loadComboBox(panelABM.getCbBancoSucursales(), null, null, "<Seleccionar Banco>");
                } else if (boton.equals(panelABM.getbAddSucursal())) {
                    Banco bancoSelected = (Banco) panelABM.getCbBancos().getSelectedItem();
                    try {
                        JDialog initABM = new BancoSucursalController().initABM(abm);
                        initABM.setLocationRelativeTo(abm);
                        initABM.setVisible(true);
                        UTIL.setSelectedItem(panelABM.getCbBancos(), bancoSelected);
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
        UTIL.setSelectedItem(panelABM.getCbBancos(), o.getBanco());
        UTIL.setSelectedItem(panelABM.getCbBancoSucursales(), o.getBancoSucursal());
        UTIL.setSelectedItem(panelABM.getCbLibrado(), o.getLibrado());
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

    private ChequePropio setEntity(ChequePropio o) throws MessageException {
        Date fechaCheque, fechaCobro, fechaEndoso = null;
        Long numero = null;
        BigDecimal importe = null;
        Banco banco;
        BancoSucursal sucursal = null;
        Librado librado = null;
        Proveedor proveedor = null;
        boolean cruzado, propio, endosado;
        String endosatario = null, observacion = null;

        if (panelABM.getDcCheque() == null) {
            throw new MessageException("Debe ingresar la Fecha del Cheque.");
        }
        if (panelABM.getDcCobro() == null) {
            throw new MessageException("Debe ingresar la Fecha de cobro del Cheque.");
        }
        fechaCheque = panelABM.getDcCheque();
        fechaCobro = panelABM.getDcCobro();

        if (UTIL.compararIgnorandoTimeFields(fechaCheque, fechaCobro) < 0) {
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
        if (panelABM.getCbBancos().getSelectedIndex() < 1) {
            throw new MessageException("Debe elegir un Banco");
        }
        banco = (Banco) panelABM.getCbBancos().getSelectedItem();
        try {
            sucursal = (BancoSucursal) panelABM.getCbBancoSucursales().getSelectedItem();
        } catch (ClassCastException e) {
//            throw new MessageException("Sucursal de Banco no válida");
        }
//        librado = (Librado) panelABM.getCbLibrado().getSelectedItem();
//        if (librado.getId() == 1) {
//            proveedor = (Proveedor) panelABM.getCbEmisor().getSelectedItem();
//        }
        cruzado = panelABM.getCheckCruzado().isSelected();
        observacion = panelABM.getTaObservacion().getText();
        ChequePropio newCheque = new ChequePropio(proveedor, numero, banco, sucursal, importe, fechaCheque, fechaCobro, cruzado, observacion, ChequeEstado.CARTERA, endosatario, fechaEndoso, UsuarioController.getCurrentUser(), librado);
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

    public JDialog initManager(JFrame parent, ActionListener buttonListener) {
        jdChequeManager = (JDChequesManager) new ChequeTercerosController().initManager(parent, buttonListener);
        jdChequeManager.getLabelEmisor().setText("Emitido a");
        UTIL.loadComboBox(jdChequeManager.getCbEmisor(), new ProveedorController().findEntities(), true);
        jdChequeManager.setTitle("Administración de Cheques Propios");
        return jdChequeManager;
    }

    JDialog initManager(JFrame parent, boolean selectionMode) {
        jdChequeManager = new JDChequesManager(parent, true);
        UTIL.loadComboBox(jdChequeManager.getCbBancos(), JGestionUtils.getWrappedBancos(new BancoController().findWithCuentasBancarias()), true);
        UTIL.loadComboBox(jdChequeManager.getCbBancoSucursales(), null, null, "<Seleccionar un Banco>");
        UTIL.loadComboBox(jdChequeManager.getCbEmisor(), new ProveedorController().findEntities(), true);
        UTIL.loadComboBox(jdChequeManager.getCbLibrado(), new LibradoJpaController().findEntities(), true);
        UTIL.loadComboBox(jdChequeManager.getCbEstados(), Arrays.asList(ChequeEstado.values()), true);
//        UTIL.getDefaultTableModel(jdChequeManager.getjTable1(),
//                new String[]{},
//                new int[]{},
//                new Class[]{});
        jdChequeManager.getCbBancos().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jdChequeManager.getCbBancos().getSelectedIndex() > 0) {
                    Banco banco = ((ComboBoxWrapper<Banco>) jdChequeManager.getCbBancos().getSelectedItem()).getEntity();
                    List<Cuentabancaria> cuentasbancaria = banco.getCuentasbancaria();
                    UTIL.loadComboBox(jdChequeManager.getCbCuentaBancaria(), JGestionUtils.getWrappedCuentasBancarias(cuentasbancaria), true);
                    UTIL.loadComboBox(jdChequeManager.getCbBancoSucursales(), new BancoSucursalController().findBy(banco), true);
                } else {
                    UTIL.loadComboBox(jdChequeManager.getCbBancoSucursales(), null, null, "<Seleccionar un Banco>");
                }
            }
        });
        jdChequeManager.addButtonListener(this);
        return jdChequeManager;
    }

    ChequePropio initABM(Window owner, boolean isEditing, Proveedor proveedor) throws MessageException {
        UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.TESORERIA);
        if (isEditing && EL_OBJECT == null) {
            throw new MessageException("Debe elegir una fila de la tabla");
        }

        panelABM = new PanelABMCheques();
        panelABM.setUIChequeTerceros();
        UTIL.loadComboBox(panelABM.getCbBancos(), new BancoController().findEntities(), true);
//        UTIL.loadComboBox(panelABM.getCbBancoSucursales(), null, null, "<Seleccionar un Banco>");
        UTIL.loadComboBox(panelABM.getCbEmisor(), new ProveedorController().findEntities(), false);
        UTIL.setSelectedItem(panelABM.getCbEmisor(), proveedor);
//        UTIL.loadComboBox(panelABM.getCbLibrado(), new LibradoJpaController().findEntities(), false);

        panelABM.getCbBancos().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (panelABM.getCbBancos().getSelectedIndex() > 0) {
                    Banco banco = (Banco) panelABM.getCbBancos().getSelectedItem();
                    UTIL.loadComboBox(panelABM.getCbBancoSucursales(), new BancoSucursalController().findBy(banco), false);
                } else {
                    UTIL.loadComboBox(panelABM.getCbBancoSucursales(), null, null, "<Seleccionar un Banco>");
                }
            }
        });
        if (isEditing) {
            setPanel(EL_OBJECT);
        }
        abm = new JDABM(owner, panelABM);
        abm.setTitle("ABM - Cheque Propio");
        abm.setListener(this);
        abm.setVisible(true);
        return EL_OBJECT;
    }
}
