/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import controller.exceptions.IllegalOrphanException;
import controller.exceptions.MessageException;
import controller.exceptions.NonexistentEntityException;
import entity.MovimientoConcepto;
import gui.JDMiniABM;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import utilities.general.UTIL;

/**
 *
 * @author Administrador
 */
public class MovimientoConceptoJpaController implements Serializable {

   public static final String CLASS_NAME = MovimientoConcepto.class.getSimpleName();
   private JDMiniABM abm;
   private MovimientoConcepto ELOBJECT;
   private EntityManagerFactory emf = null;
   public static final MovimientoConcepto EFECTIVO;
   
   static {
      EFECTIVO = DAO.getEntityManager().find(MovimientoConcepto.class, 1);
   }

   public MovimientoConceptoJpaController(EntityManagerFactory emf) {
      this.emf = emf;
   }

   public EntityManager getEntityManager() {
      return emf != null ? emf.createEntityManager() : DAO.getEntityManager();
   }

   //<editor-fold defaultstate="collapsed" desc="CRUD">
   public void create(MovimientoConcepto movimientoConcepto) {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         em.persist(movimientoConcepto);
         em.getTransaction().commit();
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public void edit(MovimientoConcepto movimientoConcepto) throws NonexistentEntityException, Exception {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         movimientoConcepto = em.merge(movimientoConcepto);
         em.getTransaction().commit();
      } catch (Exception ex) {
         String msg = ex.getLocalizedMessage();
         if (msg == null || msg.length() == 0) {
            Integer id = movimientoConcepto.getId();
            if (findMovimientoConcepto(id) == null) {
               throw new NonexistentEntityException("The movimientoConcepto with id " + id + " no longer exists.");
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
      ArrayList<String> lis;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         MovimientoConcepto movimientoConcepto;
         try {
            movimientoConcepto = em.getReference(MovimientoConcepto.class, id);
            movimientoConcepto.getId();
         } catch (EntityNotFoundException enfe) {
            throw new NonexistentEntityException("The movimientoConcepto with id " + id + " no longer exists.", enfe);
         }
         if (em.createQuery("SELECT o.id FROM " + DetalleCajaMovimientosJpaController.CLASS_NAME + " o WHERE o.movimientoConcepto.id=" + movimientoConcepto.getId()).getResultList().isEmpty()) {
            lis = new ArrayList<String>(1);
            lis.add("No se puede eliminar el registro " + CLASS_NAME + " porque está relacionado a otro/s registro/s.");
            throw new IllegalOrphanException(lis);
         }
         em.remove(movimientoConcepto);
         em.getTransaction().commit();
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public List<MovimientoConcepto> findMovimientoConceptoEntities() {
      return findMovimientoConceptoEntities(true, -1, -1);
   }

   public List<MovimientoConcepto> findMovimientoConceptoEntities(int maxResults, int firstResult) {
      return findMovimientoConceptoEntities(false, maxResults, firstResult);
   }

   private List<MovimientoConcepto> findMovimientoConceptoEntities(boolean all, int maxResults, int firstResult) {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select object(o) from MovimientoConcepto as o");
         if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
         }
         return q.getResultList();
      } finally {
         em.close();
      }
   }

   public MovimientoConcepto findMovimientoConcepto(Integer id) {
      EntityManager em = getEntityManager();
      try {
         return em.find(MovimientoConcepto.class, id);
      } finally {
         em.close();
      }
   }

   public int getMovimientoConceptoCount() {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select count(o) from MovimientoConcepto as o");
         return ((Long) q.getSingleResult()).intValue();
      } finally {
         em.close();
      }
   }
   //</editor-fold>

   /**
    * Init UI ABM de MovimientoConcepto
    * @param jFrame owner, patern bla bla 
    * @throws MessageException End-User messages
    */
   public void initUIABM(JFrame jFrame) throws MessageException {
      UsuarioJpaController.checkPermiso(PermisosJpaController.PermisoDe.TESORERIA);
      abm = new JDMiniABM(jFrame, true);
      abm.hideBtnLock();
      abm.hideFieldCodigo();
      abm.hideFieldExtra();
      abm.setVisibleTaInformacion(false);
      abm.pack();
      abm.setTitle("ABM - " + CLASS_NAME + "'s");
      UTIL.getDefaultTableModel(abm.getjTable1(),
              new String[]{"Nº", "Nombre"},
              new int[]{10, 150});
      cargarDTM(abm.getDTM(), null);
      abm.getbAceptar().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            if (ELOBJECT == null) {
               ELOBJECT = new MovimientoConcepto();
            }
            ELOBJECT.setNombre(abm.getTfNombre());
            try {
               abm.getbAceptar().setEnabled(false);
               checkConstraints(ELOBJECT);
               //persistiendo......
               String msg;
               if (ELOBJECT.getId() == null) {
                  create(ELOBJECT);
                  msg = " creado";
               } else {
                  edit(ELOBJECT);
                  msg = " editado";
               }
               JOptionPane.showMessageDialog(abm, CLASS_NAME + msg);
               abm.clearPanelFields();
               cargarDTM(abm.getDTM(), null);
               ELOBJECT = null;
            } catch (MessageException ex) {
               JOptionPane.showMessageDialog(abm, ex.getMessage(), ex.getClass().toString(), JOptionPane.WARNING_MESSAGE);
               Logger.getLogger(MovimientoConceptoJpaController.class.getName()).log(Level.ERROR, null, ex);
            } catch (Exception ex) {
               JOptionPane.showMessageDialog(abm, ex.getMessage(), ex.getClass().toString(), JOptionPane.WARNING_MESSAGE);
               Logger.getLogger(MovimientoConceptoJpaController.class.getName()).log(Level.ERROR, null, ex);
            } finally {
               abm.getbAceptar().setEnabled(true);
            }
         }
      });
      abm.getbEliminar().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(abm, "¿Eliminar movimiento concepto \"" + ELOBJECT + "\"?", null, JOptionPane.YES_NO_OPTION)) {
               try {
                  abm.getbEliminar().setEnabled(false);
                  destroy(ELOBJECT.getId());
                  ELOBJECT = null;
                  abm.clearPanelFields();
                  cargarDTM(abm.getDTM(), null);
                  JOptionPane.showMessageDialog(abm, "Eliminado..");
               } catch (IllegalOrphanException ex) {
                  JOptionPane.showMessageDialog(abm, ex.getMessage());
               } catch (NonexistentEntityException ex) {
                  JOptionPane.showMessageDialog(abm, ex.getMessage());
               } catch (Exception ex) {
                  JOptionPane.showMessageDialog(abm, ex.getMessage(), ex.getClass().toString(), JOptionPane.ERROR_MESSAGE);
                  Logger.getLogger(MovimientoConceptoJpaController.class).log(Level.ERROR, ex);
               } finally {
                  abm.getbEliminar().setEnabled(true);
               }
            }
         }
      });
      abm.getjTable1().addMouseListener(new MouseAdapter() {

         @Override
         public void mouseReleased(MouseEvent e) {
            int selectedRow = abm.getjTable1().getSelectedRow();
            if (selectedRow > -1) {
               ELOBJECT = (MovimientoConcepto) DAO.getEntityManager().find(MovimientoConcepto.class,
                       Integer.valueOf((abm.getjTable1().getModel().getValueAt(selectedRow, 0)).toString()));
               if (ELOBJECT != null) {
                  abm.setFields(ELOBJECT.getNombre(), null, null);
               }
            }
         }
      });
      abm.setVisible(true);
   }

   private void cargarDTM(DefaultTableModel dtm, String query) {
      UTIL.limpiarDtm(dtm);
      List<MovimientoConcepto> list;
      if (query == null || query.length() < 10) {
         list = DAO.getEntityManager().createQuery("SELECT o FROM " + CLASS_NAME + " o WHERE o.id > 1 ORDER BY o.id").getResultList();
      } else {
         // para cuando se usa el Buscador del ABM
         list = DAO.getEntityManager().createNativeQuery(query, MovimientoConcepto.class).getResultList();
      }

      for (MovimientoConcepto o : list) {
         dtm.addRow(new Object[]{
                    o.getId(),
                    o.getNombre(),});
      }
   }

   private void checkConstraints(MovimientoConcepto o) throws MessageException, Exception {
      String idQuery = "";
      if (o.getId() != null) {
         idQuery = "o.id <> " + o.getId() + " AND ";
      }

      try {
         DAO.getEntityManager().createQuery("SELECT o FROM " + CLASS_NAME + " o "
                 + " WHERE " + idQuery + " o.nombre='" + o.getNombre() + "'").getSingleResult().equals(o.getClass());
         throw new MessageException("Ya existe un registro con el nombre \"" + o.getNombre() + "\"");
      } catch (NoResultException ex) {
      }
   }

}
