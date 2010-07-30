package controller;

import controller.exceptions.*;
import entity.Cliente;
import entity.Depto;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import entity.Provincia;
import entity.Municipio;
import java.util.ArrayList;
import java.util.List;
import entity.Proveedor;
import entity.UTIL;
import gui.JDABM;
import gui.JDContenedor;
import gui.PanelABMDeptos;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import javax.persistence.NoResultException;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author FiruzzZ
 */
public class DeptoJpaController implements ActionListener, MouseListener, KeyListener {

   public final String CLASS_NAME = "Depto";
   private JDContenedor contenedor;
   private JDABM abm;
   private final String[] colsName = {"Nº", "Nombre", "Provincia", "Cód. area", "Abrev."};
   private final int[] colsWidth = {20, 120, 100, 20, 20};
   private PanelABMDeptos panel;
   private Depto departamento;

   // <editor-fold defaultstate="collapsed" desc="CRUD...">
   private EntityManager getEntityManager() {
      return DAO.getEntityManager();
   }

   private void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         Depto depto;
         try {
            depto = em.getReference(Depto.class, id);
            depto.getIddepto();
         } catch (EntityNotFoundException enfe) {
            throw new NonexistentEntityException("The depto with id " + id + " no longer exists.", enfe);
         }
         //ctrl de Deptos -> municipios
         List<String> illegalOrphanMessages = null;
         List<Municipio> muniOrphanList = depto.getMunicipioList();
         if (muniOrphanList.size() > 0) {
            if (illegalOrphanMessages == null) {
               illegalOrphanMessages = new ArrayList<String>();
            }
            illegalOrphanMessages.add("Este Departamento está relacionado a " + muniOrphanList.size() + " Municipios");

         }
         //ctrl proveedores huerfanos
         List<Proveedor> proveedorListOrphanCheck = depto.getProveedorList();
         if (proveedorListOrphanCheck.size() > 0) {
            if (illegalOrphanMessages == null) {
               illegalOrphanMessages = new ArrayList<String>();
            }
            illegalOrphanMessages.add("Este Departamento está relacionado a " + proveedorListOrphanCheck.size() + " Proveedor/es");
         }
         //ctrl clientes huerfanos
         List<Cliente> clienteListOrphan = depto.getClienteList();
         if (clienteListOrphan.size() > 0) {
            if (illegalOrphanMessages == null) {
               illegalOrphanMessages = new ArrayList<String>();
            }
            illegalOrphanMessages.add("Este Departamento está relacionado a " + clienteListOrphan.size() + " Cliente/s");
         }

         if (illegalOrphanMessages != null) {
            throw new IllegalOrphanException(illegalOrphanMessages);
         }
         //disatache the entity Depto from entity Provincia
         Provincia idprovincia = depto.getProvincia();
         if (idprovincia != null) {
            idprovincia.getDeptoList().remove(depto);
            idprovincia = em.merge(idprovincia);
         }
         em.remove(depto);
         em.getTransaction().commit();
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public List<Depto> findDeptoEntities() {
      return findDeptoEntities(true, -1, -1);
   }

   public List<Depto> findDeptoEntities(int maxResults, int firstResult) {
      return findDeptoEntities(false, maxResults, firstResult);
   }

   private List<Depto> findDeptoEntities(boolean all, int maxResults, int firstResult) {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select object(o) from Depto as o");
         if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
         }
         return q.getResultList();
      } finally {
         em.close();
      }
   }

   public Depto findDepto(Integer id) {
      EntityManager em = getEntityManager();
      try {
         return em.find(Depto.class, id);
      } finally {
         em.close();
      }
   }

   public Depto findDeptoByNombre(String nombreDepto) {
      EntityManager em = getEntityManager();
      try {
         return (Depto) em.createNamedQuery("Depto.findByNombre").setParameter("nombre", nombreDepto).getSingleResult();
      } finally {
         em.close();
      }
   }

   public int getDeptoCount() {
      EntityManager em = getEntityManager();
      try {
         return ((Long) em.createQuery("select count(o) from Depto as o").getSingleResult()).intValue();
      } finally {
         em.close();
      }
   }// </editor-fold>

   public void initContenedor(java.awt.Frame frame, boolean modal) {
      contenedor = new JDContenedor(frame, modal, "ABM - Departamentos");
      contenedor.hideBtmImprimir();
      try {
         UTIL.getDefaultTableModel(colsName, colsWidth, contenedor.getjTable1());
      } catch (Exception ex) {
         Logger.getLogger(DeptoJpaController.class.getName()).log(Level.SEVERE, null, ex);
      }
      cargarDTM(contenedor.getDTM(), null);
      contenedor.setListener(this);
      contenedor.setVisible(true);
   }

   private void cargarDTM(DefaultTableModel dtm, String query) {
      UTIL.limpiarDtm(dtm);

      java.util.List<Depto> l;
      if (query == null) {
         l = DAO.getEntityManager().createNamedQuery(CLASS_NAME + ".findAll").getResultList();
      } else {
         // para cuando se usa el Buscador del ABM
         l = DAO.getEntityManager().createNativeQuery(query, Depto.class).getResultList();
      }

      for (Depto o : l) {
         dtm.addRow(new Object[]{
                    o.getIddepto(),
                    o.getNombre(),
                    o.getProvincia().getNombre(),
                    o.getCodigoArea(),
                    o.getAbreviatura(),});
      }
   }

   private void checkConstraints(Depto object) throws MessageException, Exception {
      EntityManager em = getEntityManager();
      Depto OldDepto;
      String idQuery = "";
      //check UNIQUE nombre por Provincia
      if (object.getIddepto() != null) {
         idQuery = "o.iddepto!=" + object.getIddepto() + " AND ";
      }

      try {
         OldDepto = (Depto) em.createNativeQuery("SELECT * FROM " + CLASS_NAME + " o "
                 + " WHERE " + idQuery + " o.idprovincia=" + object.getProvincia().getCodigo()
                 + " AND o.nombre='" + object.getNombre() + "'", Depto.class).getSingleResult();
         if (OldDepto != null) {
            throw new MessageException(
                    "Ya existe un Departamento con este nombre en esta provincia.");
         }
      } catch (NoResultException ex) {
         System.out.println("safó nombre....");
      }

      if (object.getCodigoArea() != null) {
         try {
            OldDepto = (Depto) em.createNativeQuery("SELECT * FROM " + CLASS_NAME + " o "
                    + " WHERE " + idQuery + " o.idprovincia=" + object.getProvincia().getCodigo()
                    + " AND o.codigo_area=" + object.getCodigoArea(), Depto.class).getSingleResult();
            if (OldDepto != null) {
               throw new MessageException("Ya existe un Departamento (" + OldDepto.getNombre() + ") con este código de area en esta Provincia.");
            }
         } catch (NoResultException ex) {
            System.out.println("safó codigoArea....");
         }
      }

      if (object.getAbreviatura() != null) {
         try {
            OldDepto = (Depto) em.createNativeQuery("SELECT * FROM " + CLASS_NAME + " o "
                    + " WHERE " + idQuery + " o.idprovincia=" + object.getProvincia().getCodigo()
                    + " AND o.nombre='" + object.getNombre() + "'", Depto.class).getSingleResult();
            if (OldDepto != null) {
               throw new MessageException("Ya existe un Departamento (" + OldDepto.getNombre() + ") con esta abreviatura.");
            }
         } catch (NoResultException ex) {
            System.out.println("safó Abreviatura....");
         }
      }

      if (object.getIddepto() == null) {
         DAO.create(object);
      } else {
         DAO.doMerge(object);
      }
   }

   public void actionPerformed(ActionEvent e) {
      // <editor-fold defaultstate="collapsed" desc="JButton">
      if (e.getSource().getClass().equals(JButton.class)) {
         JButton boton = (JButton) e.getSource();
         if (boton.getName().equalsIgnoreCase("new")) {
            try {
               initABM(false, e);
            } catch (MessageException ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(DeptoJpaController.class.getName()).log(Level.SEVERE, null, ex);
            }
         } else if (boton.getName().equalsIgnoreCase("edit")) {
            try {
               initABM(true, e);
            } catch (MessageException ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(DeptoJpaController.class.getName()).log(Level.SEVERE, null, ex);
            }

         } else if (boton.getName().equalsIgnoreCase("del")) {
            try {
               if (departamento == null) {
                  throw new MessageException("No hay departamento seleccionado");
               }
               destroy(departamento.getIddepto());
            } catch (MessageException ex) {
               abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
               Logger.getLogger(DeptoJpaController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalOrphanException ex) {
               abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(DeptoJpaController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NonexistentEntityException ex) {
               abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(DeptoJpaController.class.getName()).log(Level.SEVERE, null, ex);
            }
         } else if (boton.getName().equalsIgnoreCase("Print")) {
         } else if (boton.getName().equalsIgnoreCase("exit")) {
            contenedor.dispose();
            contenedor = null;
         } else if (boton.getName().equalsIgnoreCase("aceptar")) {
            try {
               String msjAccion = "modificado";
               if (departamento == null) {
                  msjAccion = "registrado";
               }
               setEntity();
               checkConstraints(departamento);
               abm.showMessage(msjAccion, CLASS_NAME, 1);
               cargarDTM(contenedor.getDTM(), "");
            } catch (MessageException ex) {
               abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
               Logger.getLogger(DeptoJpaController.class.getName()).log(Level.SEVERE, null, ex);
            }
         } else if (boton.getName().equalsIgnoreCase("cancelar")) {
            abm.dispose();
            panel = null;
            abm = null;
            departamento = null;
         }
         return;
      } // </editor-fold>

      // <editor-fold defaultstate="collapsed" desc="JTextField">
      if (e.getSource().getClass().equals(javax.swing.JTextField.class)) {
         javax.swing.JTextField tf = (javax.swing.JTextField) e.getSource();
         if (tf.getName().equalsIgnoreCase("tfFiltro")) {
         }
      }
      // </editor-fold>

   }

   public void mouseClicked(MouseEvent e) {
      if (e.getSource().getClass().equals(javax.swing.JTable.class)) {
         //abre el PanelAfiliado y carga los datos
         if (((javax.swing.JTable) e.getSource()).getName().equals("tfFiltro")) {
            if (e.getClickCount() >= 2) {
               mouseReleased(e);
            }
         } else if (((javax.swing.JTable) e.getSource()).getName().equals("jdContenedor")) {
            if (e.getClickCount() >= 2) //inicializa el obj afiliado según
            {
               mouseReleased(e);       //la row selected
            }
         }
      }
   }

   public void mouseReleased(MouseEvent e) {
      Integer selectedRow = ((javax.swing.JTable) e.getSource()).getSelectedRow();
      if (selectedRow > -1) {
         departamento = (Depto) DAO.getEntityManager().find(Depto.class,
                 Integer.valueOf((((javax.swing.JTable) e.getSource()).getValueAt(selectedRow, 0)).toString()));
      }
   }

   private void initABM(boolean isEditting, ActionEvent e) throws Exception {
      if (isEditting && departamento == null) {
         throw new MessageException("Debe elegir una fila");
      }
      panel = new PanelABMDeptos();
      panel.hideDepto();
      UTIL.loadComboBox(panel.getCbProvincias(), new ProvinciaJpaController().findProvinciaEntities(), false);
      if (isEditting) {
         setPanel(departamento);
      }
      abm = new JDABM(contenedor, true, panel);
      abm.setTitle("ABM Departamentos");
      if (e != null) {
         abm.setLocationRelativeTo((java.awt.Component) e.getSource());
      }
      abm.setListener(this);
      abm.setVisible(true);
   }

   private void setPanel(Depto departamento) {
      javax.swing.JComboBox combo = panel.getCbProvincias();
      for (int i = 0; i < combo.getItemCount(); i++) {
         if ((combo.getItemAt(i).toString()).equals(departamento.getProvincia().getNombre())) {
            combo.setSelectedIndex(i);
         }
      }
      panel.setTfNombre(departamento.getNombre());
      if (departamento.getCodigoArea() != null) {
         panel.setTfCodigoArea(departamento.getCodigoArea().toString());
      }
      panel.setTfAbreviacion(departamento.getAbreviatura());
   }

   private void setEntity() throws MessageException {
      if (departamento == null) {
         departamento = new Depto();
      }

      departamento.setNombre(panel.getTfNombre().trim().toUpperCase());
      if (departamento.getNombre() == null || departamento.getNombre().length() < 1) {
         throw new MessageException("Nombre no válido");
      }
      try {
         //si puso algo en el TextField de codigoArea
         if (panel.getTfCodigoArea().length() > 0) {
            Integer.valueOf(panel.getTfCodigoArea());
            departamento.setCodigoArea(Integer.valueOf(panel.getTfCodigoArea()));
         }
      } catch (NumberFormatException ex) {
         throw new MessageException("Código de area no válido (solo números enteros)");
      }
      departamento.setProvincia((Provincia) panel.getSelectedCbProvincias());
      departamento.setAbreviatura(panel.getTfAbreviacion().trim().toUpperCase());
   }

   public void mousePressed(MouseEvent e) {
   }

   public void mouseEntered(MouseEvent e) {
   }

   public void mouseExited(MouseEvent e) {
   }

   public void keyTyped(KeyEvent e) {
   }

   public void keyPressed(KeyEvent e) {
   }

   public void keyReleased(KeyEvent e) {
      if (e.getComponent().getClass().equals(JTextField.class)) {
         JTextField tf = (JTextField) e.getComponent();

         if (tf.getName().equalsIgnoreCase("tfFiltro")) {
            armarQuery(tf.getText().trim());
         }
      }
   }

   /**
    * Arma la query, la cual va filtrar los datos en el JDContenedor
    * @param filtro
    */
   private void armarQuery(String filtro) {
      String query = null;
      if (filtro != null && filtro.length() > 0) {
         query = "SELECT * FROM " + CLASS_NAME + " o WHERE o.nombre ILIKE '" + filtro + "%'";
      }
      cargarDTM(contenedor.getDTM(), query);
   }

   public List<Depto> findDeptosFromProvincia(int idProvincia) {
      return DAO.getEntityManager().createNativeQuery("SELECT * FROM Depto o WHERE o.idprovincia =" + idProvincia, Depto.class).getResultList();
   }
}
