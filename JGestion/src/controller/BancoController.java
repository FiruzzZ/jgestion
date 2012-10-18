package controller;

import controller.exceptions.DatabaseErrorException;
import controller.exceptions.IllegalOrphanException;
import controller.exceptions.MessageException;
import controller.exceptions.NonexistentEntityException;
import entity.Banco;
import entity.BancoSucursal;
import entity.CuentaBancaria;
import gui.JDABM;
import gui.JDContenedor;
import gui.PanelABMBancoSucursales;
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
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import utilities.general.UTIL;

/**
 *
 * @author Administrador
 */
public class BancoController {

    public final String CLASS_NAME = Banco.class.getSimpleName();
    private Banco EL_OBJECT;
    private final String[] colsName = {"id", "Nombre", "Página Web"};
    private final int[] colsWidth = {20, 120, 100};
    private JDContenedor contenedor;
    private JDABM abm;
    private PanelABMBancoSucursales panelABM;
    private boolean permitirFiltroVacio;
    private EntityManager entityManager;
    private static Logger LOG = Logger.getLogger(BancoController.class);

    public BancoController() {
    }

    //<editor-fold defaultstate="collapsed" desc="DAO - CRUD Methods">
    public EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            LOG.trace(this.getClass() + " -> getting EntityManager");
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    public void create(Banco banco) {
        try {
            DAO.create(banco);
        } catch (Exception ex) {
            Logger.getLogger(BancoController.class.getName()).fatal(ex, ex);
        }
    }

    public void edit(Banco banco) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            banco = em.merge(banco);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = banco.getId();
                if (findBanco(id) == null) {
                    throw new NonexistentEntityException("The banco with id " + id + " no longer exists.");
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
            Banco banco;
            try {
                banco = em.getReference(Banco.class, id);
                //check if exist a BancoSucursal bound to this Banco
                List<BancoSucursal> bancoSucursalEntitiesBound = new BancoSucursalController().findBy(banco);
                if (bancoSucursalEntitiesBound != null
                        && !bancoSucursalEntitiesBound.isEmpty()) {
                    List<String> msg = new ArrayList<String>(bancoSucursalEntitiesBound.size() + 1);
                    msg.add("Este " + CLASS_NAME + " está relacionado a " + bancoSucursalEntitiesBound.size() + " sucursal/es:");
                    for (BancoSucursal bancoSucursal : bancoSucursalEntitiesBound) {
                        msg.add(bancoSucursal.getNombre() + ", " + bancoSucursal.getDireccion());
                    }
                    throw new IllegalOrphanException(msg);
                }
                banco.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The banco with id " + id + " no longer exists.", enfe);
            }
            em.remove(banco);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Banco> findEntities() {
        return findBancoEntities(true, -1, -1);
    }

    public List<Banco> findBancoEntities(int maxResults, int firstResult) {
        return findBancoEntities(false, maxResults, firstResult);
    }

    private List<Banco> findBancoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from Banco as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Banco findBanco(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Banco.class, id);
        } finally {
            em.close();
        }
    }
    //</editor-fold>

    public JDialog initContenedor(JFrame owner, boolean modal, boolean modoBuscador) throws DatabaseErrorException {
        contenedor = new JDContenedor(owner, modal, "ABM - " + CLASS_NAME);
        contenedor.hideBtmImprimir();
        contenedor.getTfFiltro().setToolTipText("Filtra por nombre del " + CLASS_NAME);
        contenedor.getTfFiltro().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (contenedor.getTfFiltro().getText().trim().length() > 0) {
                    permitirFiltroVacio = true;
                    try {
                        armarQuery(contenedor.getTfFiltro().getText().trim());
                    } catch (DatabaseErrorException ex) {
                        contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    }
                } else {
                    if (permitirFiltroVacio) {
                        permitirFiltroVacio = false;
                        try {
                            armarQuery(contenedor.getTfFiltro().getText().trim());
                        } catch (DatabaseErrorException ex) {
                            contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
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
                    try {
                        cargarContenedorTabla(null);
                    } catch (DatabaseErrorException ex) {
                        contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    }
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
                    try {
                        cargarContenedorTabla(null);
                    } catch (DatabaseErrorException ex) {
                        contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    }
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    LOG.error(ex);
                }
            }
        });
        contenedor.getbBorrar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    destroy(Integer.valueOf(UTIL.getSelectedValue(contenedor.getjTable1(), 0).toString()));
                    try {
                        cargarContenedorTabla(null);
                    } catch (DatabaseErrorException ex) {
                        contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    }
                    contenedor.showMessage("Eliminado..", CLASS_NAME, 1);
                } catch (IllegalOrphanException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (NonexistentEntityException ex) {
                    Logger.getLogger(BancoController.class.getName()).log(Level.ERROR, null, ex);
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                }
            }
        });
        UTIL.getDefaultTableModel(contenedor.getjTable1(), colsName, colsWidth);
        UTIL.hideColumnTable(contenedor.getjTable1(), 0);
        //no permite filtro de vacio en el inicio
        permitirFiltroVacio = false;
        cargarContenedorTabla(null);
        return contenedor;
    }

    /**
     * Arma la query, la cual va filtrar los datos en el JDContenedor
     *
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
            List<Banco> l;
            if (query == null) {
                l = (List<Banco>) findEntities();
            } else {
                l = (List<Banco>) DAO.getNativeQueryResultList(query, EL_OBJECT.getClass());
            }
            for (Banco o : l) {
                dtm.addRow(new Object[]{
                            o.getId(),
                            o.getNombre(),
                            o.getWebpage()
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
     * Crea una instancia del ABM
     *
     * @param parent
     * @param isEditing
     * @return una ventana para la creación de Bancos
     * @throws MessageException
     */
    private JDialog initABM(JDialog parent, boolean isEditing) throws MessageException {
        UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.DATOS_GENERAL);
        if (isEditing) {
            EL_OBJECT = getSelectedFromContenedor();
            if (EL_OBJECT == null) {
                throw new MessageException("Debe elegir una fila");
            }
        }
        return settingABM(parent, isEditing);
    }

    private JDialog settingABM(JDialog parent, boolean isEditing) {
        panelABM = new PanelABMBancoSucursales();
        panelABM.hideFieldsSucursal();
        abm = new JDABM(parent, "ABM - " + CLASS_NAME, true, panelABM);
        if (isEditing) {
            setPanelABM(EL_OBJECT);
        }
        abm.getbAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (EL_OBJECT == null) {
                        EL_OBJECT = new Banco();
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

    private void setPanelABM(Banco o) {
        panelABM.getTfNombre().setText(o.getNombre());
        panelABM.getTfPaginaWeb().setText(o.getWebpage());
    }

    private Banco getSelectedFromContenedor() {
        Integer selectedRow = contenedor.getjTable1().getSelectedRow();
        if (selectedRow > -1) {
            return (Banco) DAO.getEntityManager().find(Banco.class,
                    Integer.valueOf(
                    (contenedor.getDTM().getValueAt(selectedRow, 0)).toString()));
        } else {
            return null;
        }
    }

    private void checkConstraints(Banco o) throws MessageException {
        String idQuery = "";
        if (o.getId() != null) {
            idQuery = "o.id!=" + o.getId() + " AND ";
        }
        try {
            DAO.getEntityManager().createNativeQuery("SELECT * FROM " + CLASS_NAME + " o "
                    + " WHERE " + idQuery + " o.nombre='" + o.getNombre() + "' ", o.getClass()).getSingleResult();
            throw new MessageException("Ya existe otra " + CLASS_NAME + " con este nombre.");
        } catch (NoResultException ex) {
        }
        try {
            DAO.getEntityManager().createNativeQuery("SELECT * FROM " + CLASS_NAME + " o "
                    + " WHERE " + idQuery + " o.webpage='" + o.getNombre() + "' ", o.getClass()).getSingleResult();
            throw new MessageException("Ya existe otra " + CLASS_NAME + " con esta página web.");
        } catch (NoResultException ex) {
        }
    }

    private void setEntity(Banco o) throws MessageException {
        String nombre;
        String webpage;
        if (panelABM.getTfNombre().getText() == null || panelABM.getTfNombre().getText().trim().length() < 1) {
            throw new MessageException("Debe ingresar un nombre");
        }
        webpage = panelABM.getTfPaginaWeb().getText().trim();
        if (webpage.isEmpty()) {
            webpage = null;
        }
        nombre = panelABM.getTfNombre().getText();
        o.setNombre(nombre);
        o.setWebpage(webpage);
    }

    @SuppressWarnings("unchecked")
    public List<Banco> findWithCuentasBancarias() {
        return DAO.getEntityManager().createQuery("SELECT o.banco FROM " + CuentaBancaria.class.getSimpleName() + " o").getResultList();
    }
}
