package jgestion.controller;

import java.awt.Window;
import jgestion.controller.exceptions.IllegalOrphanException;
import jgestion.controller.exceptions.MessageException;
import jgestion.controller.exceptions.NonexistentEntityException;
import jgestion.entity.Contribuyente;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import jgestion.entity.Cliente;
import java.util.ArrayList;
import java.util.List;
import jgestion.entity.Proveedor;
import utilities.general.UTIL;
import jgestion.gui.JDMiniABM;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import javax.persistence.NoResultException;
import javax.swing.table.DefaultTableModel;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author FiruzzZ
 */
public class ContribuyenteController implements ActionListener, MouseListener {

    public final String CLASS_NAME = "Contribuyente";
    private final String[] colsName = {"Nº", "Nombre"};
    private final int[] colsWidth = {20, 150};
    private JDMiniABM abm;
    private Contribuyente contribuyente;

    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }

    public void create(Contribuyente contribuyente) throws Exception {
        DAO.create(contribuyente);
    }

    public void edit(Contribuyente contribuyente) throws IllegalOrphanException, NonexistentEntityException, Exception {
        DAO.merge(contribuyente);
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Contribuyente contribuyente;
            try {
                contribuyente = em.getReference(Contribuyente.class, id);
                contribuyente.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The contribuyente with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Cliente> clienteListOrphanCheck = DAO.getEntityManager().createQuery("SELECT o FROM " + Cliente.class.getSimpleName() + " o WHERE o.contribuyente.id = " + contribuyente.getId()).getResultList();
            for (Cliente clienteListOrphanCheckCliente : clienteListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Contribuyente (" + contribuyente + ") no puede ser borrado porque el Cliente " + clienteListOrphanCheckCliente + " está ligado.");
            }
            List<Proveedor> proveedorListOrphanCheck = DAO.getEntityManager().createQuery("SELECT o FROM " + Proveedor.class.getSimpleName() + " o WHERE o.contribuyente.id = " + contribuyente.getId()).getResultList();
            for (Proveedor proveedorListOrphanCheckProveedor : proveedorListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Contribuyente (" + contribuyente + ") no puede ser borrado porque el Proveedor " + proveedorListOrphanCheckProveedor + " está ligado.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(contribuyente);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Contribuyente> findAll() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from Contribuyente as o order by o.id");
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Contribuyente findContribuyente(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Contribuyente.class, id);
        } finally {
            em.close();
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

    public void mouseReleased(MouseEvent e) {
        Integer selectedRow = ((javax.swing.JTable) e.getSource()).getSelectedRow();
        DefaultTableModel dtm = (DefaultTableModel) ((javax.swing.JTable) e.getSource()).getModel();
        if (selectedRow > -1) {
            contribuyente = (Contribuyente) DAO.getEntityManager().find(Contribuyente.class,
                    Integer.valueOf((dtm.getValueAt(selectedRow, 0)).toString()));
        }
        if (contribuyente != null) {
            setPanelFields(contribuyente);
        }
    }

    public void actionPerformed(ActionEvent e) {
        // <editor-fold defaultstate="collapsed" desc="JButton">
        if (e.getSource().getClass().equals(javax.swing.JButton.class)) {
            javax.swing.JButton boton = (javax.swing.JButton) e.getSource();

            if (boton.getName().equalsIgnoreCase("new")) {
                contribuyente = null;
                abm.clearPanelFields();
            } else if (boton.getName().equalsIgnoreCase("del")) {
                try {
                    destroy(contribuyente.getId());
                    contribuyente = null;
                    abm.clearPanelFields();
                    cargarDTM(abm.getDTM(), "");
                    abm.showMessage("Eliminado..", CLASS_NAME, 1);
                } catch (NonexistentEntityException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    ex.printStackTrace();
                } catch (IllegalOrphanException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (boton.getName().equalsIgnoreCase("cancelar")) {
                contribuyente = null;
                abm.clearPanelFields();
            } else if (boton.getName().equalsIgnoreCase("guardar")) {
                try {
                    setEntity();
                    checkConstraints(contribuyente);
                    contribuyente = null;
                    abm.clearPanelFields();
                    cargarDTM(abm.getDTM(), "");
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    LogManager.getLogger().error(ex.getMessage(), ex);
                }
            }
            return;
        }// </editor-fold>
    }

    private void checkConstraints(Contribuyente object) throws MessageException, IllegalOrphanException, NonexistentEntityException, Exception {
        String idQuery = "";
        if (object.getId() != null) {
            idQuery = "o.id!=" + object.getId() + " AND ";
        }
        try {
            DAO.getEntityManager().createNativeQuery("SELECT * FROM " + CLASS_NAME + " o "
                    + " WHERE " + idQuery + " o.nombre='" + object.getNombre() + "' ", Contribuyente.class).getSingleResult();
            throw new MessageException("Ya existe este " + CLASS_NAME + ".");
        } catch (NoResultException ex) {
        }

        //persistiendo......
        if (object.getId() == null) {
            create(object);
        } else {
            edit(object);
        }
    }

    private void setEntity() throws MessageException {
        if (contribuyente == null) {
            contribuyente = new Contribuyente();
        }
        if (abm.getTfNombre() == null || abm.getTfNombre().trim().length() < 1) {
            throw new MessageException("Debe ingresar un nombre de " + CLASS_NAME.toLowerCase());
        }
        contribuyente.setNombre(abm.getTfNombre().trim().toUpperCase());
    }

    private void cargarDTM(DefaultTableModel dtm, String query) {
        dtm.setRowCount(0);
        List<Contribuyente> l;
        if (query == null || query.length() < 10) {
            l = findAll();
        } else {
            // para cuando se usa el Buscador del ABM
            l = DAO.getEntityManager().createNativeQuery(query, Contribuyente.class).getResultList();
        }
        for (Contribuyente o : l) {
            dtm.addRow(new Object[]{
                o.getId(),
                o.getNombre()
            });
        }
    }

    private void setPanelFields(Contribuyente o) {
        abm.setTfNombre(o.getNombre());
        abm.tfNombreRequestFocus();
    }

    public void initABM(Window owner, boolean modal) {
        try {
            UsuarioController.checkPermiso(PermisosController.PermisoDe.DATOS_GENERAL);
        } catch (MessageException ex) {
            javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage());
            return;
        }
        abm = new JDMiniABM(owner, modal);
        abm.hideFieldExtra();
        abm.hideFieldCodigo();
        abm.setTitle("ABM - " + CLASS_NAME + "s");
        UTIL.getDefaultTableModel(abm.getjTable1(), colsName, colsWidth);
        cargarDTM(abm.getDTM(), null);
        abm.setListeners(this);
        abm.setVisible(true);
    }
}
