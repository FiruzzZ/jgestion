package controller;

import controller.exceptions.IllegalOrphanException;
import controller.exceptions.MessageException;
import controller.exceptions.NonexistentEntityException;
import entity.UTIL;
import entity.Unidadmedida;
import gui.JDMiniABM;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.swing.table.DefaultTableModel;

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
      DAO.doMerge(unidadmedida);
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

   public void actionPerformed(ActionEvent e) {
      // <editor-fold defaultstate="collapsed" desc="JButton">
      if (e.getSource().getClass().equals(javax.swing.JButton.class)) {
         javax.swing.JButton boton = (javax.swing.JButton) e.getSource();
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
               ex.printStackTrace();
            } catch (IllegalOrphanException ex) {
               abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
            } catch (Exception ex) {
               ex.printStackTrace();
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
               ex.printStackTrace();
            }
         }

         return;
      }// </editor-fold>
   }

   public void mouseReleased(MouseEvent e) {
      Integer selectedRow = ((javax.swing.JTable) e.getSource()).getSelectedRow();
      if (selectedRow > -1) {
         unidadMedida = (Unidadmedida) DAO.getEntityManager().find(Unidadmedida.class,
                 Integer.valueOf((((javax.swing.JTable) e.getSource()).getValueAt(selectedRow, 0)).toString()));
      }
      if (unidadMedida != null) {
         setPanelFields(unidadMedida);
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

   public void initABM(java.awt.Frame frame, boolean modal) {
      // <editor-fold defaultstate="collapsed" desc="checking Permiso">
      try {
         UsuarioJpaController.checkPermisos(PermisosJpaController.PermisoDe.DATOS_GENERAL);
      } catch (MessageException ex) {
         javax.swing.JOptionPane.showMessageDialog(null,ex.getMessage());
         return;
      }// </editor-fold>
      abm = new JDMiniABM(frame, modal);
      abm.hideFieldCodigo();
      abm.hideFieldExtra();
      abm.setTitle("ABM - Unidades de medida");
      try {
         UTIL.getDefaultTableModel(abm.getjTable1(), colsName, colsWidth);
      } catch (Exception ex) {
         Logger.getLogger(UnidadmedidaJpaController.class.getName()).log(Level.SEVERE, null, ex);
      }
      cargarDTM(abm.getDTM(), null);
      abm.setListeners(this);
      abm.setVisible(true);
   }

   private void setPanelFields(Unidadmedida unidadMedida) {
      abm.setTfNombre(unidadMedida.getNombre());
   }
}
