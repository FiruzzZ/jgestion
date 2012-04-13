package controller;

import controller.exceptions.DatabaseErrorException;
import controller.exceptions.MessageException;
import controller.exceptions.NonexistentEntityException;
import entity.Banco;
import entity.BancoSucursal;
import entity.ChequePropio;
import entity.Librado;
import entity.Proveedor;
import entity.enums.ChequeEstado;
import gui.JDABM;
import gui.JDChequesManager;
import gui.JDContenedor;
import gui.PanelABMCheques;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Logger;
import utilities.general.UTIL;

/**
 *
 * @author Administrador
 */
public class ChequePropioJpaController implements Serializable, ActionListener {

    public final String CLASS_NAME = ChequePropio.class.getSimpleName();
    private ChequePropio EL_OBJECT;
    private final String[] columnNames = {"id", "Nº Cheque", "Fecha Cheque", "Vencimiento", "Banco/Sucursal", "Estado", "Importe", "Librado"};
    private final int[] columnWidths = {1, 60, 50, 50, 150, 50, 50, 50};
    private final Class[] columnClassTypes = {};
    private JDContenedor contenedor;
    private EntityManager entityManager;
    private static Logger LOGGER = Logger.getLogger(ChequePropioJpaController.class);
    private JDABM abm;
    private PanelABMCheques panelABM;
    private JDChequesManager jdChequeManager;

    public ChequePropioJpaController() {
    }

    public EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    //<editor-fold defaultstate="collapsed" desc="DAO - CRUD Methods">
    public void create(ChequePropio chequePropio) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(chequePropio);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(ChequePropio chequePropio) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            chequePropio = em.merge(chequePropio);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = chequePropio.getId();
                if (findChequePropio(id) == null) {
                    throw new NonexistentEntityException("The chequePropio with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            ChequePropio chequePropio;
            try {
                chequePropio = em.getReference(ChequePropio.class, id);
                chequePropio.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The chequePropio with id " + id + " no longer exists.", enfe);
            }
            em.remove(chequePropio);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<ChequePropio> findChequePropioEntities() {
        return findChequePropioEntities(true, -1, -1);
    }

    public List<ChequePropio> findChequePropioEntities(int maxResults, int firstResult) {
        return findChequePropioEntities(false, maxResults, firstResult);
    }

    private List<ChequePropio> findChequePropioEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from ChequePropio as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public ChequePropio findChequePropio(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(ChequePropio.class, id);
        } finally {
            em.close();
        }
    }

    public int getChequePropioCount() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select count(o) from ChequePropio as o");
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    //</editor-fold>

    public JDialog initContenedor(JFrame frame, boolean modal) {
        contenedor = new JDContenedor(frame, modal, "ABM - " + CLASS_NAME + "s");
        contenedor.setSize(800, contenedor.getHeight());
        contenedor.getLabelMensaje().setText("Puede filtrar los cheques por número");
        contenedor.getTfFiltro().addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                armarQuery(contenedor.getTfFiltro().getText().trim());
            }
        });
        UTIL.getDefaultTableModel(contenedor.getjTable1(), columnNames, columnWidths);
        UTIL.hideColumnTable(contenedor.getjTable1(), 0);
        cargarContenedorTabla(null);
        contenedor.setListener(this);
        return contenedor;
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
     * @param isEditing 
     * @param ee se posicionará a la ventana en relación a este, can be null.
     * @throws MessageException 
     */
    private JDialog initABM(boolean isEditing, ActionEvent ee) throws MessageException {
        UsuarioJpaController.checkPermiso(PermisosJpaController.PermisoDe.TESORERIA);
        panelABM = new PanelABMCheques();
        //Default para ChequePropio
        panelABM.getLabelEmisor().setText("Emisor (Proveedor)");
        panelABM.getCheckPropio().setVisible(false);
        panelABM.getCheckEndosado().setSelected(false);
        panelABM.getCheckEndosado().setEnabled(false);

        panelABM.getCbBancos().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (panelABM.getCbBancos().getSelectedIndex() > 0) {
                    Banco banco = (Banco) panelABM.getCbBancos().getSelectedItem();
                    UTIL.loadComboBox(panelABM.getCbBancoSucursales(), new BancoSucursalJpaController().findEntitiesFrom(banco), false);
                } else {
                    UTIL.loadComboBox(panelABM.getCbBancoSucursales(), null, null, "<Seleccionar un Banco>");
                }
            }
        });
        panelABM.getCbBancoSucursales().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    BancoSucursal bs = (BancoSucursal) panelABM.getCbBancoSucursales().getSelectedItem();
                    panelABM.getTfSucursalDireccion().setText(bs.getDireccion());
                } catch (Exception ex) {
                    panelABM.getTfSucursalDireccion().setText("");
                }
            }
        });
        panelABM.getCbLibrado().addActionListener(this);
        panelABM.getbAddEmisor().addActionListener(this);
        panelABM.getbAddBanco().addActionListener(this);
        panelABM.getbAddSucursal().addActionListener(this);
        UTIL.loadComboBox(panelABM.getCbBancos(), new BancoJpaController().findEntities(), true);
        UTIL.loadComboBox(panelABM.getCbBancoSucursales(), null, null, "<Seleccionar un Banco>");
        UTIL.loadComboBox(panelABM.getCbEmisor(), new ProveedorController().findEntities(), false);
        UTIL.loadComboBox(panelABM.getCbLibrado(), new LibradoJpaController().findEntities(), false);
        if (isEditing) {
            setPanel(EL_OBJECT);
        }
        abm = new JDABM(true, contenedor, panelABM);
        abm.setTitle("ABM - " + CLASS_NAME + "s");
        if (ee != null) {
            abm.setLocation(((java.awt.Component) ee.getSource()).getLocation());
        }

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
                        initABM(false, e);
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
                            EL_OBJECT = DAO.getEntityManager().find(ChequePropio.class,
                                    Integer.valueOf((contenedor.getDTM().getValueAt(selectedRow, 0)).toString()));
                            if (EL_OBJECT == null) {
                                throw new MessageException("Debe elegir una fila de la tabla");
                            }
                            initABM(true, e);
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
                        destroy(EL_OBJECT.getId());
                    } catch (MessageException ex) {
                        contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    } catch (NonexistentEntityException ex) {
                        contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                        LOGGER.error(ex);
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
                            create(EL_OBJECT);
                        } else {
                            edit(EL_OBJECT);
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
                        JDialog initABM = new BancoJpaController().initABM(abm);
                        initABM.setLocationRelativeTo(abm);
                        initABM.setVisible(true);
                    } catch (MessageException ex) {
                        abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    }
                    UTIL.loadComboBox(panelABM.getCbBancos(), new BancoJpaController().findEntities(), true);
                    UTIL.loadComboBox(panelABM.getCbBancoSucursales(), null, null, "<Seleccionar Banco>");
                } else if (boton.equals(panelABM.getbAddSucursal())) {
                    Banco bancoSelected = (Banco) panelABM.getCbBancos().getSelectedItem();
                    try {
                        JDialog initABM = new BancoSucursalJpaController().initABM(abm);
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
        panelABM.getTfEstado().setText(o.getEstado().toString());
    }

    private ChequePropio setEntity(ChequePropio o) throws MessageException {
        List<String> erroresList = new ArrayList<String>(0);
        Date fechaCheque, fechaCobro = null, fechaEndoso = null;
        Long numero = null;
        BigDecimal importe = null;
        Banco banco;
        BancoSucursal sucursal = null;
        Librado librado;
        Proveedor proveedor = null;
        boolean cruzado, propio, endosado;
        String endosatario = null, observacion = null;

        if (panelABM.getDcCheque() == null) {
            erroresList.add("Debe ingresar la Fecha del Cheque.");
        }
        fechaCheque = panelABM.getDcCheque();
        if (panelABM.getDcCobro() != null) {
            fechaCobro = panelABM.getDcCobro();
            fechaCheque = UTIL.getDateYYYYMMDD(fechaCheque);
            fechaCobro = UTIL.getDateYYYYMMDD(fechaCobro);
            if (fechaCheque.after(fechaCobro)) {
                erroresList.add("Fecha de cobro no puede ser anterior a Fecha de cheque.");
            }
        }
        try {
            numero = Long.valueOf(panelABM.getTfNumero().getText());
        } catch (NumberFormatException numberFormatException) {
            erroresList.add("Número de cheque no válido");
        }
        try {
            importe = BigDecimal.valueOf(Double.valueOf(panelABM.getTfImporte().getText()));
        } catch (NumberFormatException numberFormatException) {
            erroresList.add("Importe no válido");
        }
        if (panelABM.getCbBancos().getSelectedIndex() < 1) {
            erroresList.add("Debe elegir un Banco");
        }
        banco = (Banco) panelABM.getCbBancos().getSelectedItem();
        try {
            sucursal = (BancoSucursal) panelABM.getCbBancoSucursales().getSelectedItem();
        } catch (ClassCastException e) {
            erroresList.add("Sucursal de Banco no válida");
        }
        librado = (Librado) panelABM.getCbLibrado().getSelectedItem();
        if (librado.getId() == 1) {
            proveedor = (Proveedor) panelABM.getCbEmisor().getSelectedItem();
        }
        cruzado = panelABM.getCheckCruzado().isSelected();
        observacion = panelABM.getTaObservacion().getText();
        if (!erroresList.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String string : erroresList) {
                sb.append(string);
                sb.append("\n");
            }
            throw new MessageException(sb.toString());
        }
        ChequePropio newCheque = new ChequePropio(o.getId(), numero, fechaCheque,
                cruzado, observacion, ChequeEstado.CARTERA, fechaCobro, endosatario, fechaEndoso, importe,
                UsuarioJpaController.getCurrentUser(), proveedor, librado, sucursal, banco);
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
        jdChequeManager = (JDChequesManager) new ChequeTercerosJpaController().initManager(parent, buttonListener);
        jdChequeManager.getLabelEmisor().setText("Emitido a");
        UTIL.loadComboBox(jdChequeManager.getCbEmisor(), new ProveedorController().findEntities(), true);
        jdChequeManager.setTitle("Administración de Cheques Propios");
        return jdChequeManager;
    }

    JDialog initManager(JFrame parent, boolean selectionMode) {
        jdChequeManager = new JDChequesManager(parent, true);
        UTIL.loadComboBox(jdChequeManager.getCbBancos(), new BancoJpaController().findEntities(), true);
        UTIL.loadComboBox(jdChequeManager.getCbBancoSucursales(), null, null, "<Seleccionar un Banco>");
        UTIL.loadComboBox(jdChequeManager.getCbEmisor(), new ProveedorController().findEntities(), true);
        UTIL.loadComboBox(jdChequeManager.getCbLibrado(), new LibradoJpaController().findEntities(), true);
        UTIL.loadComboBox(jdChequeManager.getCbEstados(), Arrays.asList(ChequeEstado.values()), true);
        UTIL.getDefaultTableModel(jdChequeManager.getjTable1(),
                new String[]{},
                new int[]{},
                new Class[]{});
        jdChequeManager.getCbBancos().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (jdChequeManager.getCbBancos().getSelectedIndex() > 0) {
                    Banco banco = (Banco) jdChequeManager.getCbBancos().getSelectedItem();
                    UTIL.loadComboBox(jdChequeManager.getCbBancoSucursales(), new BancoSucursalJpaController().findEntitiesFrom(banco), true);
                } else {
                    UTIL.loadComboBox(jdChequeManager.getCbBancoSucursales(), null, null, "<Seleccionar un Banco>");
                }
            }
        });
        jdChequeManager.addButtonListener(this);
        return jdChequeManager;
    }
}
