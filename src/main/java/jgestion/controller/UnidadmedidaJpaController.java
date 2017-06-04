package jgestion.controller;

import jgestion.controller.exceptions.IllegalOrphanException;
import jgestion.controller.exceptions.MessageException;
import jgestion.controller.exceptions.NonexistentEntityException;
import utilities.general.UTIL;
import jgestion.entity.Unidadmedida;
import jgestion.gui.JDMiniABM;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.table.DefaultTableModel;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author FiruzzZ
 */
public class UnidadmedidaJpaController implements ActionListener, MouseListener {

    private String CLASS_NAME = "Unidadmedida";
    private final String[] colsName = {"Nº", "Nombre"};
    private final int[] colsWidth = {20, 20};
    private JDMiniABM abm;
    private Unidadmedida unidadMedida;

    public UnidadmedidaJpaController() {
    }

    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }

    public void create(Unidadmedida unidadmedida) throws Exception {
        DAO.create(unidadmedida);
    }

    public void edit(Unidadmedida unidadmedida) throws NonexistentEntityException, Exception {
        DAO.merge(unidadmedida);
    }

    public void destroy(Integer id) throws NonexistentEntityException, IllegalOrphanException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Unidadmedida unidadmedida;
            try {
                unidadmedida = em.getReference(Unidadmedida.class, id);
                unidadmedida.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The unidadmedida with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            //contrl de clientes orphans
            int cantOrphans = getProductosList(id);
            if (cantOrphans > 0) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new java.util.ArrayList<String>();
                }
                illegalOrphanMessages.add("No puede eliminar esta " + CLASS_NAME + " porque hay " + cantOrphans + " Producto/s relacionados a este");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(unidadmedida);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Unidadmedida> findUnidadmedidaEntities() {
        return findUnidadmedidaEntities(true, -1, -1);
    }

    public List<Unidadmedida> findUnidadmedidaEntities(int maxResults, int firstResult) {
        return findUnidadmedidaEntities(false, maxResults, firstResult);
    }

    private List<Unidadmedida> findUnidadmedidaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from Unidadmedida as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Unidadmedida findUnidadmedida(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Unidadmedida.class, id);
        } finally {
            em.close();
        }
    }

    public int getUnidadmedidaCount() {
        EntityManager em = getEntityManager();
        try {
            return ((Long) em.createQuery("select count(o) from Unidadmedida as o").getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    private int getProductosList(Integer id) {
        return DAO.getEntityManager().createNativeQuery(
                "SELECT * FROM Producto o WHERE o.idunidadmedida = " + id).getResultList().size();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // <editor-fold defaultstate="collapsed" desc="JButton">
        if (e.getSource().getClass().equals(JButton.class)) {
            JButton boton = (JButton) e.getSource();
            if (boton.getName().equalsIgnoreCase("new")) {
                clearPanelFields();
            } else if (boton.getName().equalsIgnoreCase("del")) {
                try {
                    if (unidadMedida == null) {
                        throw new MessageException("Debe seleccionar la fila que desea borrar");
                    }
                    destroy(unidadMedida.getId());
                    clearPanelFields();
                    cargarDTM(abm.getDTM(), "");
                    abm.showMessage("Eliminado..", CLASS_NAME, 1);
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
                } catch (NonexistentEntityException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
                } catch (IllegalOrphanException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
                } catch (Exception ex) {
                    LogManager.getLogger();//(this.getClass()).error(null, ex);
                }
            } else if (boton.getName().equalsIgnoreCase("cancelar")) {
                clearPanelFields();
            } else if (boton.getName().equalsIgnoreCase("guardar")) {
                try {
                    setEntity();
                    checkConstraints(unidadMedida);
                    clearPanelFields();
                    cargarDTM(abm.getDTM(), "");
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    LogManager.getLogger();//(this.getClass()).error(null, ex);
                }
            }

            return;
        }// </editor-fold>
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    private void clearPanelFields() {
        unidadMedida = null;
        abm.setTfNombre("");
    }

    private void cargarDTM(DefaultTableModel dtm, String naviteQuery) {
        for (int i = dtm.getRowCount(); i > 0; i--) {
            dtm.removeRow(i - 1);
        }
        java.util.List<Unidadmedida> l;
        if (naviteQuery == null || naviteQuery.length() < 10) {
            l = DAO.getEntityManager().createNamedQuery(CLASS_NAME + ".findAll").getResultList();
        } else // para cuando se usa el Buscador del ABM
        {
            l = DAO.getEntityManager().createNativeQuery(naviteQuery, Unidadmedida.class).getResultList();
        }
        for (Unidadmedida o : l) {
            dtm.addRow(new Object[]{
                        o.getId(),
                        o.getNombre(),});
        }
    }

    private void setEntity() throws MessageException {
        if (unidadMedida == null) {
            unidadMedida = new Unidadmedida();
        }
        if (abm.getTfNombre().trim().length() < 1) {
            throw new MessageException("Nombre de Unidad de medida no válido");
        }
        unidadMedida.setNombre(abm.getTfNombre().trim().toUpperCase());
    }

    private void checkConstraints(Unidadmedida unidadMedida) throws MessageException, NonexistentEntityException, Exception {
        String idQuery = "";
        if (unidadMedida.getId() != null) {
            idQuery = "o.id!=" + unidadMedida.getId() + " AND ";
        }
        try {
            DAO.getEntityManager().createNativeQuery("SELECT * FROM " + CLASS_NAME + " o "
                    + " WHERE " + idQuery + " o.nombre='" + unidadMedida.getNombre() + "'", Unidadmedida.class).getSingleResult();
            throw new MessageException("Ya existe otra " + CLASS_NAME + " con este nombre.");
        } catch (NoResultException ex) {
        }
        //persistiendo......
        if (unidadMedida.getId() == null) {
            create(unidadMedida);
        } else {
            edit(unidadMedida);
        }
    }

    public void initABM(JFrame frame, boolean modal) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.ABM_PRODUCTOS);
        abm = new JDMiniABM(frame, modal);
        abm.hideBtnLock();
        abm.hideFieldCodigo();
        abm.hideFieldExtra();
        abm.setTitle("ABM - Unidades de medida");
        abm.getTaInformacion().setText("Las Unidad de medida, expresan la forma de cuantificación del Producto, "
                + "sirven como información adicional en informes/reportes del sistema.");
        try {
            UTIL.getDefaultTableModel(abm.getjTable1(), colsName, colsWidth);
            UTIL.hideColumnTable(abm.getjTable1(), 0);
        } catch (Exception ex) {
            LogManager.getLogger();//(this.getClass()).error(null, ex);
        }
        cargarDTM(abm.getDTM(), null);
        abm.getjTable1().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                Integer selectedRow = abm.getjTable1().getSelectedRow();
                if (selectedRow > -1) {
                    unidadMedida = DAO.getEntityManager().find(Unidadmedida.class,
                            UTIL.getSelectedValue(abm.getjTable1(), 0));
                }
                if (unidadMedida != null) {
                    setPanelFields(unidadMedida);
                }
            }
        });
        abm.setListeners(this);
        abm.setVisible(true);
    }

    private void setPanelFields(Unidadmedida unidadMedida) {
        abm.setTfNombre(unidadMedida.getNombre());
    }
}
