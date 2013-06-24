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
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.NoResultException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author FiruzzZ
 */
public class MarcaController implements ActionListener{

    public static final String CLASS_NAME = Marca.class.getSimpleName();
    private final String[] colsName = {"Nº", "Nombre", "Código"};
    private final int[] colsWidth = {20, 120, 80};
    private JDMiniABM abm;
    private Marca entity;

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

    public List<Marca> findAll() {
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

    public void getABM(Window owner, boolean modal) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.ABM_PRODUCTOS);
        abm = new JDMiniABM(owner, modal);
        abm.setLocationRelativeTo(owner);
        initABM();
    }

    private void initABM() {
        abm.hideBtnLock();
        abm.hideFieldExtra();
        abm.setTitle("ABM - " + CLASS_NAME + "s");
        abm.getTaInformacion().setText("La Marca es un atributo del Producto.");
        UTIL.getDefaultTableModel(abm.getjTable1(), colsName, colsWidth);
        abm.getjTable1().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                Integer selectedRow = abm.getjTable1().getSelectedRow();
                if (selectedRow > -1) {
                    entity = DAO.getEntityManager().find(Marca.class,
                            Integer.valueOf(UTIL.getSelectedValue(abm.getjTable1(), 0).toString()));
                }
                if (entity != null) {
                    abm.setTfNombre(entity.getNombre());
                    if (entity.getCodigo() != null) {
                        abm.setTfCodigo(entity.getCodigo());
                    } else {
                        abm.setTfCodigo("");
                    }
                    abm.tfNombreRequestFocus();
                }
            }
        });
        cargarDTM();
        abm.setListeners(this);
        abm.setVisible(true);
    }

    private void cargarDTM() {
        DefaultTableModel dtm = (DefaultTableModel) abm.getjTable1().getModel();
        dtm.setRowCount(0);
        for (Marca o : findAll()) {
            dtm.addRow(new Object[]{
                        o.getId(),
                        o.getNombre(),
                        o.getCodigo()
                    });
        }
    }

    private void eliminar() throws MessageException, NonexistentEntityException, IllegalOrphanException {
        if (entity == null) {
            throw new MessageException("No hay " + CLASS_NAME + " seleccionada");
        }
        destroy(entity.getId());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // <editor-fold defaultstate="collapsed" desc="JButton">
        if (e.getSource().getClass().equals(JButton.class)) {
            JButton boton = (JButton) e.getSource();
            //<editor-fold defaultstate="collapsed" desc="abm Actions">
            if (abm != null) {
                if (boton.equals(abm.getbNuevo())) {
                    entity = null;
                    abm.clearPanelFields();
                } else if (boton.equals(abm.getbEliminar())) {
                    try {
                        eliminar();
                        entity = null;
                        abm.clearPanelFields();
                        cargarDTM();
                        JOptionPane.showMessageDialog(abm, "Eliminado");
                    } catch (MessageException ex) {
                        JOptionPane.showMessageDialog(abm, ex.getMessage(), null, JOptionPane.WARNING_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(abm, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else if (boton.equals(abm.getbCancelar())) {
                    entity = null;
                    abm.clearPanelFields();
                } else if (boton.equals(abm.getbAceptar())) {
                    try {
                        setEntity();
                        checkConstraints(entity);
                        entity = null;
                        abm.clearPanelFields();
                        cargarDTM();
                    } catch (MessageException ex) {
                        JOptionPane.showMessageDialog(abm, ex.getMessage(), null, JOptionPane.WARNING_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(abm, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }//</editor-fold>
        }// </editor-fold>
    }

    private void checkConstraints(Marca o) throws MessageException, IllegalOrphanException, NonexistentEntityException, Exception {
        String idQuery = "";
        if (o.getId() != null) {
            idQuery = "o.id!=" + o.getId() + " AND ";
        }
        try {
            DAO.getEntityManager().createNativeQuery("SELECT * FROM " + CLASS_NAME + " o "
                    + " WHERE " + idQuery + " o.nombre='" + o.getNombre() + "' ", Marca.class).getSingleResult();
            throw new MessageException("Ya existe otra " + CLASS_NAME + " con este nombre.");
        } catch (NoResultException ex) {
        }

        if (o.getCodigo() != null && o.getCodigo().length() > 0) {
            try {
                DAO.getEntityManager().createNativeQuery("SELECT * FROM " + CLASS_NAME + " o "
                        + " WHERE " + idQuery + " o.codigo='" + o.getCodigo() + "' ", Marca.class).getSingleResult();
                throw new MessageException("Ya existe otra " + CLASS_NAME + " con este código.");
            } catch (NoResultException ex) {
            }
        }

        //persistiendo......
        if (o.getId() == null) {
            create(o);
        } else {
            edit(o);
        }
    }

    private void setEntity() throws MessageException {
        if (entity == null) {
            entity = new Marca();
        }
        String nombre = abm.getTfNombre().trim().toUpperCase();
        if (nombre.isEmpty()) {
            throw new MessageException("Nombre no válido");
        }
        if (nombre.length() > 50) {
            throw new MessageException("El nombre no puede superar los 50 caracteres");
        }
        String codigo = null;
        if (abm.getTfCodigo().trim().length() > 0) {
            if (abm.getTfNombre().length() > 50) {
                throw new MessageException("El código no puede superar los 50 caracteres");
            }
            codigo = abm.getTfCodigo().trim().toUpperCase();
        }
        entity.setNombre(nombre);
        entity.setCodigo(codigo);
    }
}
