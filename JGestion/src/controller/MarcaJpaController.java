package controller;

import controller.exceptions.IllegalOrphanException;
import controller.exceptions.MessageException;
import controller.exceptions.NonexistentEntityException;
import entity.Marca;
import java.awt.event.ActionEvent;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import utilities.general.UTIL;
import gui.JDMiniABM;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.NoResultException;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author FiruzzZ
 */
public class MarcaJpaController implements ActionListener, MouseListener {

    public static final String CLASS_NAME = Marca.class.getSimpleName();
    private final String[] colsName = {"Nº", "Nombre", "Código"};
    private final int[] colsWidth = {20, 120, 80};
    private JDMiniABM abm;
    private Marca marca;

    // <editor-fold defaultstate="collapsed" desc="DAO - CRUD Methods">
    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }

    public void create(Marca marca) throws Exception {
        DAO.create(marca);
    }

    public void edit(Marca marca) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        em = getEntityManager();
        em.getTransaction().begin();
        if (em.find(Marca.class, marca.getId()) == null) {
            throw new NonexistentEntityException("The marca with id " + marca.getId() + " no longer exists.");
        }
        marca = em.merge(marca);
        em.getTransaction().commit();
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Marca marca;
            try {
                marca = em.getReference(Marca.class, id);
                marca.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The marca with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Long cantProductos = (Long) em.createQuery("SELECT COUNT(o) FROM Producto o WHERE o.marca.id=" + marca.getId()).getSingleResult();
            if (cantProductos > 0) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("Esta marca no puede ser eliminada por que está relacionada a " + cantProductos + " Producto/s");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(marca);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Marca> findMarcaEntities() {
        return findMarcaEntities(true, -1, -1);
    }

    public List<Marca> findMarcaEntities(int maxResults, int firstResult) {
        return findMarcaEntities(false, maxResults, firstResult);
    }

    private List<Marca> findMarcaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from Marca as o order by o.nombre");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Marca findMarca(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Marca.class, id);
        } finally {
            em.close();
        }
    }

    public int getMarcaCount() {
        EntityManager em = getEntityManager();
        try {
            return ((Long) em.createQuery("select count(o) from Marca as o").getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }// </editor-fold>

    public void getABM(JFrame owner, boolean modal) throws MessageException {
        UsuarioJpaController.checkPermiso(PermisosJpaController.PermisoDe.ABM_PRODUCTOS);
        abm = new JDMiniABM(owner, modal);
        abm.setLocationRelativeTo(owner);
        initABM();
    }

    public void getABM(JDialog owner, boolean modal) throws MessageException {
        UsuarioJpaController.checkPermiso(PermisosJpaController.PermisoDe.ABM_PRODUCTOS);
        abm = new JDMiniABM(owner, modal);
        abm.setLocationRelativeTo(owner);
        initABM();
    }

    private void initABM() {
        abm.hideBtnLock();
        abm.hideFieldExtra();
        abm.setTitle("ABM - " + CLASS_NAME + "s");
        abm.getTaInformacion().setText("La Marca es un atributo del Producto, "
                + "es necesario crear al menos una para poder crear Productos."
                + "\nPuede ser usado como una forma de agrupación/clasificación.");
        UTIL.getDefaultTableModel(abm.getjTable1(), colsName, colsWidth);
        cargarDTM(abm.getDTM(), null);
        abm.setListeners(this);
        abm.setVisible(true);
    }

    public void mouseReleased(MouseEvent e) {
        Integer selectedRow = ((javax.swing.JTable) e.getSource()).getSelectedRow();
        if (selectedRow > -1) {
            marca = (Marca) DAO.getEntityManager().find(Marca.class,
                    Integer.valueOf((((javax.swing.JTable) e.getSource()).getValueAt(selectedRow, 0)).toString()));
        }
        if (marca != null) {
            setPanelFields(marca);
        }
    }

    private void cargarDTM(DefaultTableModel dtm, String query) {
        UTIL.limpiarDtm(dtm);
        List<Marca> l;
        if (query == null || query.length() < 10) {
            l = DAO.getEntityManager().createNamedQuery(CLASS_NAME + ".findAll").getResultList();
        } else {
            // para cuando se usa el Buscador del ABM
            l = DAO.getEntityManager().createNativeQuery(query, this.getClass()).getResultList();
        }
        for (Marca o : l) {
            dtm.addRow(new Object[]{
                        o.getId(),
                        o.getNombre(),
                        o.getCodigo()
                    });
        }
    }

    private void setPanelFields(Marca o) {
        abm.setTfNombre(o.getNombre());
        if (o.getCodigo() != null) {
            abm.setTfCodigo(o.getCodigo());
        } else {
            abm.setTfCodigo("");
        }
        abm.tfNombreRequestFocus();
    }

    private void eliminar() throws MessageException, NonexistentEntityException, IllegalOrphanException {
        if (marca == null) {
            throw new MessageException("No hay " + CLASS_NAME + " seleccionada");
        }
        destroy(marca.getId());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // <editor-fold defaultstate="collapsed" desc="JButton">
        if (e.getSource().getClass().equals(javax.swing.JButton.class)) {
            javax.swing.JButton boton = (javax.swing.JButton) e.getSource();

            if (boton.getName().equalsIgnoreCase("new")) {
                marca = null;
                abm.clearPanelFields();
            } else if (boton.getName().equalsIgnoreCase("del")) {
                try {
                    eliminar();
                    marca = null;
                    abm.clearPanelFields();
                    cargarDTM(abm.getDTM(), "");
                    abm.showMessage("Eliminado..", CLASS_NAME, 1);
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
                } catch (NonexistentEntityException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    ex.printStackTrace();
                } catch (IllegalOrphanException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (boton.getName().equalsIgnoreCase("cancelar")) {
                marca = null;
                abm.clearPanelFields();
            } else if (boton.getName().equalsIgnoreCase("guardar")) {
                try {
                    if (marca == null) {
                        marca = new Marca();
                    }
                    setEntity(marca);
                    checkConstraints(marca);
                    marca = null;
                    abm.clearPanelFields();
                    cargarDTM(abm.getDTM(), "");
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return;
        }// </editor-fold>
    }

    private void checkConstraints(Marca object) throws MessageException, IllegalOrphanException, NonexistentEntityException, Exception {
        String idQuery = "";
        if (object.getId() != null) {
            idQuery = "o.id!=" + object.getId() + " AND ";
        }
        try {
            DAO.getEntityManager().createNativeQuery("SELECT * FROM " + CLASS_NAME + " o "
                    + " WHERE " + idQuery + " o.nombre='" + object.getNombre() + "' ", Marca.class).getSingleResult();
            throw new MessageException("Ya existe otra " + CLASS_NAME + " con este nombre.");
        } catch (NoResultException ex) {
        }

        if (object.getCodigo() != null && object.getCodigo().length() > 0) {
            try {
                DAO.getEntityManager().createNativeQuery("SELECT * FROM " + CLASS_NAME + " o "
                        + " WHERE " + idQuery + " o.codigo='" + object.getCodigo() + "' ", Marca.class).getSingleResult();
                throw new MessageException("Ya existe otra " + CLASS_NAME + " con este código.");
            } catch (NoResultException ex) {
            }
        }

        //persistiendo......
        if (object.getId() == null) {
            create(object);
        } else {
            edit(object);
        }
    }

    private void setEntity(Marca marca) throws MessageException {
        if (abm.getTfNombre() == null || abm.getTfNombre().trim().length() < 1) {
            throw new MessageException("Debe ingresar un nombre de " + CLASS_NAME.toLowerCase());
        }
        if (abm.getTfNombre().length() > 50) {
            throw new MessageException("Nombre no puede superar los 50 caracteres");
        }
        String nombre = abm.getTfNombre().trim().toUpperCase();
        String codigo = null;
        if (abm.getTfCodigo().trim().length() > 0) {
            if (abm.getTfNombre().length() > 50) {
                throw new MessageException("El código no puede superar los 50 caracteres");
            }
            codigo = abm.getTfCodigo().trim().toUpperCase();
        }
        marca.setNombre(nombre);
        marca.setCodigo(codigo);
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
}
