package jgestion.controller;

import jgestion.controller.exceptions.DatabaseErrorException;
import jgestion.controller.exceptions.IllegalOrphanException;
import jgestion.controller.exceptions.NonexistentEntityException;
import jgestion.controller.exceptions.MessageException;
import jgestion.entity.Banco;
import jgestion.entity.BancoSucursal;
import jgestion.entity.ChequePropio;
import jgestion.entity.ChequeTerceros;
import jgestion.gui.JDABM;
import jgestion.gui.JDContenedor;
import jgestion.gui.PanelABMBancoSucursales;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Logger;
import utilities.general.UTIL;

/**
 *
 * @author FiruzzZ
 */
public class BancoSucursalController implements Serializable {

    public final String CLASS_NAME = BancoSucursal.class.getSimpleName();
    private BancoSucursal EL_OBJECT;
    private final String[] colsName = {"SucursalBanco.id", "Código", "Nombre", "Banco", "Dirección", "Teléfono"};
    private final int[] colsWidth = {20, 40, 100, 100, 100, 50};
    private JDABM abm;
    private PanelABMBancoSucursales panelABM;
    private JDContenedor contenedor;
    private boolean permitirFiltroVacio;
    private EntityManager entityManager;
    private static Logger LOGGER = Logger.getLogger(BancoSucursalController.class);

    public BancoSucursalController() {
    }

    public EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            LOGGER.trace(this.getClass() + " -> getting EntityManager");
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    //<editor-fold defaultstate="collapsed" desc="DAO - CRUD Methods">
    public void create(BancoSucursal bancoSucursal) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(bancoSucursal);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(BancoSucursal bancoSucursal) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            bancoSucursal = em.merge(bancoSucursal);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = bancoSucursal.getId();
                if (findBancoSucursal(id) == null) {
                    throw new NonexistentEntityException("The bancoSucursal with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException, IllegalOrphanException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            BancoSucursal bancoSucursal;
            try {
                bancoSucursal = em.getReference(BancoSucursal.class, id);
                bancoSucursal.getId();
                List chequesPropiosList = em.createQuery("SELECT o.id FROM " + ChequePropio.class.getSimpleName() + " o WHERE o.bancoSucursal.id = " + bancoSucursal.getId()).getResultList();
                List chequesTercerosList = em.createQuery("SELECT o.id FROM " + ChequeTerceros.class.getSimpleName() + " o WHERE o.bancoSucursal.id = " + bancoSucursal.getId()).getResultList();
                if (!chequesPropiosList.isEmpty() || !chequesTercerosList.isEmpty()) {
                    List<String> msg = new ArrayList<String>(2);
                    msg.add("La Sucursal de Banco \"" + bancoSucursal.getNombre() + "\" no puede ser eliminda porque está relacionada a "
                            + chequesPropiosList.size() + " Cheque(s).");
                    throw new IllegalOrphanException(msg);
                }

            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The bancoSucursal with id " + id + " no longer exists.", enfe);
            }
            em.remove(bancoSucursal);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<BancoSucursal> findBancoSucursalEntities() {
        return findBancoSucursalEntities(true, -1, -1);
    }

    public List<BancoSucursal> findBancoSucursalEntities(int maxResults, int firstResult) {
        return findBancoSucursalEntities(false, maxResults, firstResult);
    }

    private List<BancoSucursal> findBancoSucursalEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from BancoSucursal as o ORDER BY o.nombre");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public BancoSucursal findBancoSucursal(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(BancoSucursal.class, id);
        } finally {
            em.close();
        }
    }

    public int getBancoSucursalCount() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select count(o) from BancoSucursal as o");
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    List<BancoSucursal> findBy(Banco banco) {
        EntityManager em = getEntityManager();
        List<BancoSucursal> l = null;
        l = em.createQuery("SELECT o FROM " + CLASS_NAME + " o "
                + "WHERE o.banco.id=" + banco.getId()).getResultList();
        return l;
    }
    //</editor-fold>

    public JDialog initContenedor(JFrame owner, boolean modal, boolean modoBuscador) throws DatabaseErrorException {
        contenedor = new JDContenedor(owner, modal, "ABM - Sucursales de Banco");
        contenedor.getTfFiltro().setToolTipText("Filtra por nombre de la Sucursal de Banco");
        contenedor.getTfFiltro().addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (contenedor.getTfFiltro().getText().trim().length() > 0) {
                    permitirFiltroVacio = true;
                    try {
                        armarQuery(contenedor.getTfFiltro().getText().trim());
                    } catch (DatabaseErrorException ex) {
                        JOptionPane.showMessageDialog(null, ex);
                    }
                } else {
                    if (permitirFiltroVacio) {
                        permitirFiltroVacio = false;
                        try {
                            armarQuery(contenedor.getTfFiltro().getText().trim());
                        } catch (DatabaseErrorException ex) {
                            JOptionPane.showMessageDialog(null, ex);
                        }
                    }
                }
            }
        });
        contenedor.setModoBuscador(modoBuscador);
        contenedor.getbNuevo().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    EL_OBJECT = null;
                    initABM(contenedor, false);
                    abm.setLocationRelativeTo(contenedor);
                    abm.setVisible(true);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
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
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        });
        contenedor.getbBorrar().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    destroy(Integer.valueOf(UTIL.getSelectedValue(contenedor.getjTable1(), 0).toString()));
                    contenedor.showMessage("Eliminado..", CLASS_NAME, 1);
                } catch (IllegalOrphanException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (NonexistentEntityException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                }
            }
        });
        contenedor.getbImprimir().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        UTIL.getDefaultTableModel(contenedor.getjTable1(), colsName, colsWidth);
        UTIL.hideColumnTable(contenedor.getjTable1(), 0);
        //no permite filtro de vacio en el inicio
        permitirFiltroVacio = false;
        cargarContenedorTabla(null);
//        contenedor.setListener(this);
        return contenedor;
    }

    /**
     * Arma la query, la cual va filtrar los datos en el JDContenedor
     * @param filtro
     */
    private void armarQuery(String filtro) throws DatabaseErrorException {
        String query = null;
        if (filtro != null && filtro.length() > 0) {
            query = "SELECT * FROM " + CLASS_NAME + " o WHERE o.nombre ILIKE '" + filtro + "%' ORDER BY o.nombre";
        }
        cargarContenedorTabla(query);
    }

    private void cargarContenedorTabla(String query) throws DatabaseErrorException {
        if (contenedor != null) {
            DefaultTableModel dtm = contenedor.getDTM();
            UTIL.limpiarDtm(dtm);
            List<BancoSucursal> l;
            if (query == null) {
                l = (List<BancoSucursal>) findBancoSucursalEntities();
            } else {
                l = (List<BancoSucursal>) DAO.getNativeQueryResultList(query, EL_OBJECT.getClass());
            }
            for (BancoSucursal o : l) {
                dtm.addRow(new Object[]{
                            o.getId(),
                            o.getCodigo(),
                            o.getNombre(),
                            o.getBanco().getNombre(),
                            o.getDireccion(),
                            o.getTelefono()
                        });
            }
        }
    }

    /**
     * @see BancoJpaController#initABM(javax.swing.JDialog, boolean)
     * @param parent
     * @return
     * @throws MessageException 
     */
    public JDialog initABM(JDialog parent) throws MessageException {
        return initABM(parent, false);
    }

    /**
     * Crea una instancia modal del ABM
     * @param parent
     * @param isEditing
     * @return una ventana ABM
     * @throws MessageException 
     */
    private JDialog initABM(JDialog parent, boolean isEditing) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.DATOS_GENERAL);
        if (isEditing) {
            EL_OBJECT = getSelectedFromContenedor();
            if (EL_OBJECT == null) {
                throw new MessageException("Debe elegir una fila");
            }
        }
        return getJDialogABM(parent, isEditing);
    }

    private JDialog getJDialogABM(JDialog parent, boolean isEditing) {
        panelABM = new PanelABMBancoSucursales();
        UTIL.loadComboBox(panelABM.getCbBancos(), new BancoController().findEntities(), false);
        abm = new JDABM(parent, "ABM - " + CLASS_NAME, true, panelABM);
        if (isEditing) {
            setPanelABM(EL_OBJECT);
        }
        abm.getbAceptar().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (EL_OBJECT == null) {
                        EL_OBJECT = new BancoSucursal();
                    }
                    setEntity(EL_OBJECT);
                    checkConstraints(EL_OBJECT);
                    String msg;
                    if (EL_OBJECT.getId() == null) {
                        create(EL_OBJECT);
                        msg = "Creado..";
                    } else {
                        edit(EL_OBJECT);
                        msg = "Modificado..";
                    }
                    EL_OBJECT = null;
                    abm.showMessage(msg, CLASS_NAME, 1);
                    panelABM.clearFields();
                    cargarContenedorTabla(null);
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
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

    private BancoSucursal getSelectedFromContenedor() {
        Integer selectedRow = contenedor.getjTable1().getSelectedRow();
        if (selectedRow > -1) {
            return (BancoSucursal) DAO.getEntityManager().find(BancoSucursal.class,
                    Integer.valueOf(
                    (contenedor.getDTM().getValueAt(selectedRow, 0)).toString()));
        } else {
            return null;
        }
    }

    /**
     * Chequea que:
     * <ul>
     * <li>El nombre de la sucursal sea único para ese banco</li>
     * <li>El código sea único</li>
     * </ul>
     * @param o
     * @throws MessageException
     */
    private void checkConstraints(BancoSucursal o) throws MessageException {
        String idQuery = "";
        if (o.getId() != null) {
            idQuery = "o.id<>" + o.getId() + " AND ";
        }
        try {
            DAO.getEntityManager().createQuery("SELECT o FROM " + CLASS_NAME + " o "
                    + " WHERE " + idQuery + " o.nombre='" + o.getNombre() + "' AND o.banco.id=" + o.getBanco().getId(), o.getClass()).getSingleResult();
            throw new MessageException("Ya existe otra Sucursal de Banco con este nombre.");
        } catch (NoResultException ex) {
        }
        try {
            DAO.getEntityManager().createQuery("SELECT o FROM " + CLASS_NAME + " o "
                    + " WHERE " + idQuery + " o.codigo='" + o.getCodigo() + "' ", o.getClass()).getSingleResult();
            throw new MessageException("Ya existe otra Sucursal de Banco con este código.");
        } catch (NoResultException ex) {
        }
    }

    private void setPanelABM(BancoSucursal bancoSucursal) {
        UTIL.setSelectedItem(panelABM.getCbBancos(), bancoSucursal.getBanco());
        panelABM.getTfCodigo().setText(bancoSucursal.getCodigo());
        panelABM.getTfNombre().setText(bancoSucursal.getNombre());
        panelABM.getTfDireccion().setText(bancoSucursal.getDireccion());
        if (bancoSucursal.getTelefono() != null) {
            panelABM.getTfTelefono().setText(bancoSucursal.getTelefono().toString());
        }
    }

    private void setEntity(BancoSucursal o) throws MessageException {
        String nombre, codigo, direccion;
        Long telefono = null;
        Banco banco = null;

        if (panelABM.getTfCodigo().getText() == null || panelABM.getTfCodigo().getText().trim().length() < 1) {
            throw new MessageException("Debe ingresar un código");
        }

        if (panelABM.getTfNombre().getText() == null || panelABM.getTfNombre().getText().trim().length() < 1) {
            throw new MessageException("Debe ingresar un nombre");
        }

        if (panelABM.getTfDireccion().getText() == null || panelABM.getTfDireccion().getText().trim().length() < 1) {
            throw new MessageException("Debe ingresar una dirección");
        }

        if (panelABM.getTfTelefono().getText() != null) {
            try {
                if(panelABM.getTfTelefono().getText().trim().length() > 12) {
                    throw new MessageException("Número de teléfono no puede tener mas de 12 dígitos.");
                }
                telefono = Long.valueOf(panelABM.getTfTelefono().getText());
            } catch (NumberFormatException numberFormatException) {
                throw new MessageException("Número de teléfono no válido, debe ingresar solo números.");
            }
        }
        nombre = panelABM.getTfNombre().getText();
        codigo = panelABM.getTfCodigo().getText();
        direccion = panelABM.getTfDireccion().getText();
        banco = (Banco) panelABM.getCbBancos().getSelectedItem();
        o.setNombre(nombre);
        o.setCodigo(codigo);
        o.setDireccion(direccion);
        o.setTelefono(telefono);
        o.setBanco(banco);
    }
}
