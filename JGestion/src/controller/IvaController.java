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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import jpa.controller.IvaJpaController;
import org.apache.log4j.Logger;

/**
 *
 * @author Administrador
 */
public class IvaController implements ActionListener {

    private JDMiniABM abm;
    private final String[] colsName = {"Nº", "IVA (%)"};
    private final int[] colsWidth = {20, 20};
    private final static String CLASS_NAME = Iva.class.getSimpleName();
    private Iva EL_OBJECT;
    private IvaJpaController jpaController;

    public IvaController() {
        jpaController = new IvaJpaController();
    }

    // <editor-fold defaultstate="collapsed" desc="CRUD....">
    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
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
        if (all) {
            return jpaController.findAll();
        } else {
            return jpaController.findRange(firstResult, maxResults);
        }
    }

    public Iva find(Integer id) {
        return jpaController.find(id);
    }
    // </editor-fold>

    public void initABM(JFrame owner, boolean modal) throws MessageException {
        UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.ABM_PRODUCTOS);
        abm = new JDMiniABM(owner, modal);
        abm.setLocationRelativeTo(owner);
        abm.getTaInformacion().setText("ABM de las Alicuotas (IVA) de los Productos.");
        // solo queda visible tfCodigo....
        abm.getjLabelCodigo().setText("IVA %");
        abm.hideFieldNombre();
        abm.hideBtnLock();
        abm.hideFieldExtra();
        abm.setTitle("ABM - " + jpaController.getEntityClass().getSimpleName());
        UTIL.getDefaultTableModel(abm.getjTable1(), colsName, colsWidth, new Class<?>[]{null, String.class});
        UTIL.setHorizonalAlignment(abm.getjTable1(), String.class, SwingConstants.RIGHT);
        cargarTablaIvas(abm.getjTable1(), null);
        abm.setListeners(this);
        abm.getjTable1().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                Integer selectedRow = ((javax.swing.JTable) e.getSource()).getSelectedRow();
                if (selectedRow > -1) {
                    EL_OBJECT = find(Integer.parseInt(UTIL.getSelectedValue(abm.getjTable1(), 0).toString()));
                }
                if (EL_OBJECT != null) {
                    setPanelFields(EL_OBJECT);
                }
            }
        });
        abm.setVisible(true);
    }

    @SuppressWarnings("unchecked")
    private void cargarTablaIvas(JTable jtable, String nativeQuery) {
        DefaultTableModel dtm = (DefaultTableModel) jtable.getModel();
        dtm.setRowCount(0);
        List<Iva> l = null;
        if (nativeQuery == null || nativeQuery.length() < 10) {
            l = jpaController.findAll();
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
                    jpaController.remove(EL_OBJECT);
                    destroy(EL_OBJECT.getId());
                    clearPanelFields();
                    cargarTablaIvas(abm.getjTable1(), null);
                    abm.showMessage("Eliminado..", CLASS_NAME, 1);
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
                } catch (IllegalOrphanException ex) {
                    abm.showMessage("El IVA " + EL_OBJECT.getIva() + " no puede ser eliminado por estar relacionado a los siguientes Productos\n"
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

    private void clearPanelFields() {
        EL_OBJECT = null;
        abm.setTfCodigo("");
    }

    private void setPanelFields(Iva iva) {
    }

    private void setEntity() throws MessageException {
        EL_OBJECT = new Iva();
        try {
            EL_OBJECT.setIva(Float.parseFloat(abm.getTfCodigo()));
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
            jpaController.create(iva);
        } else {
            jpaController.merge(iva);
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
