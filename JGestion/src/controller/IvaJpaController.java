package controller;

import controller.exceptions.*;
import entity.Iva;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import entity.Producto;
import entity.Producto_;
import utilities.general.UTIL;
import gui.JDMiniABM;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Logger;

/**
 *
 * @author Administrador
 */
public class IvaJpaController implements ActionListener, MouseListener {

    private JDMiniABM abm;
    private final String[] colsName = {"Nº", "IVA (%)"};
    private final int[] colsWidth = {20, 20};
    private final static String CLASS_NAME = Iva.class.getSimpleName();
    private Iva EL_OBJECT;

    // <editor-fold defaultstate="collapsed" desc="CRUD....">
    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }

    public void create(Iva iva) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(iva);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findIva(iva.getId()) != null) {
                throw new PreexistingEntityException("Iva " + iva + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Iva iva) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.merge(iva);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = iva.getId();
                if (findIva(id) == null) {
                    throw new NonexistentEntityException("The iva with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Iva iva;
            try {
                iva = em.getReference(Iva.class, id);
                iva.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The iva with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Producto> productoListOrphanCheck = new ProductoController().findProductoByIva(iva);
            for (Producto productoListOrphanCheckProducto : productoListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("Código=" + productoListOrphanCheckProducto.getCodigo() + ", Nombre=" + productoListOrphanCheckProducto.getNombre());
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(iva);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Iva> findIvaEntities() {
        return findIvaEntities(true, -1, -1);
    }

    public List<Iva> findIvaEntities(int maxResults, int firstResult) {
        return findIvaEntities(false, maxResults, firstResult);
    }

    private List<Iva> findIvaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from Iva as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Iva findIva(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Iva.class, id);
        } finally {
            em.close();
        }
    }

    public int getIvaCount() {
        EntityManager em = getEntityManager();
        try {
            return ((Long) em.createQuery("select count(o) from Iva as o").getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }// </editor-fold>

    public void initABM(JFrame owner, boolean modal) throws MessageException {
        UsuarioJpaController.checkPermiso(PermisosJpaController.PermisoDe.ABM_PRODUCTOS);
        abm = new JDMiniABM(owner, modal);
        abm.setLocationRelativeTo(owner);
        abm.getTaInformacion().setText("ABM de las Alicuotas (IVA) de los Productos.");
        // solo queda visible tfCodigo....
        abm.getjLabelCodigo().setText("IVA %");
        abm.hideFieldNombre();
        abm.hideBtnLock();
        abm.hideFieldExtra();
        abm.setTitle("ABM - " + CLASS_NAME);
        UTIL.getDefaultTableModel(abm.getjTable1(), colsName, colsWidth, new Class<?>[]{null, String.class});
        UTIL.setHorizonalAlignment(abm.getjTable1(), String.class, SwingConstants.RIGHT);
        cargarTablaIvas(abm.getjTable1(), null);
        abm.setListeners(this);
        abm.setVisible(true);
    }

    @SuppressWarnings("unchecked")
    private void cargarTablaIvas(JTable jtable, String nativeQuery) {
        DefaultTableModel dtm = (DefaultTableModel) jtable.getModel();
        dtm.setRowCount(0);
        List<Iva> l = null;
        if (nativeQuery == null || nativeQuery.length() < 10) {
            l = DAO.getEntityManager().createNamedQuery(CLASS_NAME + ".findAll").getResultList();
        } else {
            try {
                // para cuando se usa el Buscador del ABM
                l = (List<Iva>) DAO.getNativeQueryResultList(nativeQuery, Iva.class);
            } catch (DatabaseErrorException ex) {
                //ignored...
            }
        }
        for (Iva o : l) {
            dtm.addRow(new Object[]{o.getId(), UTIL.PRECIO_CON_PUNTO.format(o.getIva())});
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // <editor-fold defaultstate="collapsed" desc="JButton">
        if (e.getSource().getClass().equals(javax.swing.JButton.class)) {
            javax.swing.JButton boton = (javax.swing.JButton) e.getSource();
            if (boton.getName().equalsIgnoreCase("new")) {
                clearPanelFields();
            } else if (boton.getName().equalsIgnoreCase("del")) {
                try {
                    if (EL_OBJECT == null) {
                        throw new MessageException("Debe seleccionar la fila que desea borrar");
                    }
                    destroy(EL_OBJECT.getId());
                    clearPanelFields();
                    cargarTablaIvas(abm.getjTable1(), null);
                    abm.showMessage("Eliminado..", CLASS_NAME, 1);
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
                } catch (NonexistentEntityException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    ex.printStackTrace();
                } catch (IllegalOrphanException ex) {
                    abm.showMessage("El IVA " + EL_OBJECT.toString() + " no puede ser eliminado por estar relacionado a los siguientes Productos\n"
                            + ex.getMessage(), CLASS_NAME, 0);
                } catch (Exception ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    ex.printStackTrace();
                }
            } else if (boton.getName().equalsIgnoreCase("cancelar")) {
                clearPanelFields();
            } else if (boton.getName().equalsIgnoreCase("guardar")) {
                try {
                    setEntity();
                    checkConstraints(EL_OBJECT);
                    clearPanelFields();
                    cargarTablaIvas(abm.getjTable1(), null);
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }// </editor-fold>
    }

    public void mouseReleased(MouseEvent e) {
        Integer selectedRow = ((javax.swing.JTable) e.getSource()).getSelectedRow();
        if (selectedRow > -1) {
            EL_OBJECT = findIva(Integer.parseInt(UTIL.getSelectedValue(abm.getjTable1(), 0).toString()));
        }
        if (EL_OBJECT != null) {
            setPanelFields(EL_OBJECT);
        }
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
        EL_OBJECT = null;
        abm.setTfCodigo("");
    }

    private void setPanelFields(Iva iva) {
    }

    private void setEntity() throws MessageException {
        EL_OBJECT = new Iva();
        try {
            EL_OBJECT.setIva(Float.valueOf(abm.getTfCodigo()));
        } catch (NumberFormatException ex) {
            throw new MessageException("Porcentaje no válido");
        }
    }

    private void checkConstraints(Iva iva) throws MessageException, PreexistingEntityException, Exception {
        DefaultTableModel dtm = abm.getDTM();
        for (int i = dtm.getRowCount() - 1; i > -1; i--) {
            if (iva.getIva() == Double.valueOf(dtm.getValueAt(i, 1).toString())) {
                if (iva.getId() != null && (iva.getId() == Integer.valueOf(dtm.getValueAt(i, 0).toString()))) {
                    throw new MessageException("Ya existe un IVA con este porcentaje");
                }
            }
        }

        //persistiendo......
        if (iva.getId() == null) {
            create(iva);
        } else {
            edit(iva);
        }
    }

    Iva findByProducto(Integer productoID) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Iva> query = cb.createQuery(Iva.class);
        Root<Producto> from = query.from(Producto.class);
        query.select(from.get(Producto_.iva)).where(cb.equal(from.get(Producto_.id), productoID));
        return getEntityManager().createQuery(query).getSingleResult();
    }
}
