package controller;

import controller.exceptions.*;
import entity.Proveedor;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import entity.Contribuyente;
import entity.Departamento;
import entity.Municipio;
import entity.Provincia;
import entity.Rubro;
import entity.UTIL;
import gui.JDABM;
import gui.JDContenedor;
import gui.PanelABMProveedores;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.NoResultException;
import javax.swing.JButton;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author FiruzzZ
 */
public class ProveedorJpaController implements ActionListener, MouseListener, KeyListener {

   public final String CLASS_NAME = "Proveedor";
   private JDContenedor contenedor = null;
   private JDABM abm;
   private final String[] colsName = {"ID", "Código", "Razón social", "CUIT", "Teléfonos"};
   private final int[] colsWidth = {10, 20, 120, 40, 90};
   private Proveedor EL_OBJECT;
   private PanelABMProveedores panel;

   // <editor-fold defaultstate="collapsed" desc="CRUD y List's">
   public EntityManager getEntityManager() {
      return DAO.getEntityManager();
   }

   public void create(Proveedor proveedor) throws Exception {
      DAO.create(proveedor);
   }

   public void edit(Proveedor proveedor) {
      DAO.doMerge(proveedor);
   }

   public void destroy(Integer id) throws NonexistentEntityException, IllegalOrphanException {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         Proveedor proveedor;
         try {
            proveedor = em.getReference(Proveedor.class, id);
            proveedor.getId();
         } catch (EntityNotFoundException enfe) {
            throw new NonexistentEntityException("The proveedor with id " + id + " no longer exists.", enfe);
         }

         em.remove(proveedor);
         em.getTransaction().commit();
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public List<Proveedor> findProveedorEntities() {
      return findProveedorEntities(true, -1, -1);
   }

   public List<Proveedor> findProveedorEntities(int maxResults, int firstResult) {
      return findProveedorEntities(false, maxResults, firstResult);
   }

   private List<Proveedor> findProveedorEntities(boolean all, int maxResults, int firstResult) {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select object(o) from Proveedor as o order by o.nombre");
         if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
         }
         return q.getResultList();
      } finally {
         em.close();
      }
   }

   public Proveedor findProveedor(Integer id) {
      EntityManager em = getEntityManager();
      try {
         return em.find(Proveedor.class, id);
      } finally {
         em.close();
      }
   }

   public int getProveedorCount() {
      EntityManager em = getEntityManager();
      try {
         return ((Long) em.createQuery("select count(o) from Proveedor as o").getSingleResult()).intValue();
      } finally {
         em.close();
      }
   }
   // </editor-fold>

   @Override
   public void actionPerformed(ActionEvent e) {
      // <editor-fold defaultstate="collapsed" desc="JButton">
      if (e.getSource().getClass().equals(JButton.class)) {
         JButton boton = (JButton) e.getSource();
         if (boton.getName().equalsIgnoreCase("new")) {
            try {
               EL_OBJECT = null;
               initABM(false, e);
            } catch (MessageException ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(SucursalJpaController.class.getName()).log(Level.SEVERE, null, ex);
            }
         } else if (boton.getName().equalsIgnoreCase("edit")) {
            try {
               initABM(true, e);
            } catch (MessageException ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(SucursalJpaController.class.getName()).log(Level.SEVERE, null, ex);
            }

         } else if (boton.getName().equalsIgnoreCase("del")) {
            try {
               if (EL_OBJECT == null) {
                  throw new MessageException("No hay " + CLASS_NAME + " seleccionada");
               }
               destroy(EL_OBJECT.getId());
            } catch (MessageException ex) {
               abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (IllegalOrphanException ex) {
               abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(SucursalJpaController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NonexistentEntityException ex) {
               abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(SucursalJpaController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
               abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(SucursalJpaController.class.getName()).log(Level.SEVERE, null, ex);
            }
         } else if (boton.getName().equalsIgnoreCase("Print")) {
         } else if (boton.getName().equalsIgnoreCase("exit")) {
            contenedor.dispose();
            contenedor = null;
         } else if (boton.getName().equalsIgnoreCase("aceptar")) {
            try {
               setEntity();
               checkConstraints(EL_OBJECT);
               abm.showMessage(EL_OBJECT.getId() == null ? "Registrado" : "Modificado", CLASS_NAME, 1);
               cargarDTM(contenedor.getDTM(), "");
               //si la ventana se abrió en modo edición
               if (EL_OBJECT.getId() != null) {
                  abm.dispose();
               }
            } catch (MessageException ex) {
               abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
               Logger.getLogger(SucursalJpaController.class.getName()).log(Level.SEVERE, null, ex);
            }
         } else if (boton.getName().equalsIgnoreCase("cancelar")) {
            abm.dispose();
            panel = null;
            abm = null;
            EL_OBJECT = null;
         } else if (boton.getName().equalsIgnoreCase("bDepartaMentOs")) {
            new DepartamentoJpaController().initContenedor(null, true);
            //cuando cierra el abm
            if (panel.getCbProvincias().getSelectedIndex() > 0) {
               UTIL.loadComboBox(panel.getCbDepartamentos(),
                       new DepartamentoJpaController().findDeptosFromProvincia(
                       ((Provincia) panel.getCbProvincias().getSelectedItem()).getId()), true);
            } else {
               UTIL.loadComboBox(panel.getCbDepartamentos(), null, true);
            }

         } else if (boton.getName().equalsIgnoreCase("bmunicipios")) {
            new MunicipioJpaController().initContenedor(null, true);
            if (panel.getCbDepartamentos().getSelectedIndex() > 0) {
               UTIL.loadComboBox(panel.getCbMunicipios(),
                       new MunicipioJpaController().findMunicipiosFromDepto(
                       ((Departamento) panel.getCbDepartamentos().getSelectedItem()).getId()), true);
            } else {
               UTIL.loadComboBox(panel.getCbMunicipios(), null, true);
            }
         }
         return;
      }// </editor-fold>

      // <editor-fold defaultstate="collapsed" desc="JTextField">
      if (e.getSource().equals(javax.swing.JTextField.class)) {
         javax.swing.JTextField tf = (javax.swing.JTextField) e.getSource();
         if (tf.getName().equalsIgnoreCase("tfFiltro")) {
         }
      }// </editor-fold>

      // <editor-fold defaultstate="collapsed" desc="ComboBox">
      else if (e.getSource().getClass().equals(javax.swing.JComboBox.class)) {
         javax.swing.JComboBox combo = (javax.swing.JComboBox) e.getSource();
         if (combo.getName().equalsIgnoreCase("cbProvincias")) {
            if (combo.getSelectedIndex() > 0) {
               UTIL.loadComboBox(panel.getCbDepartamentos(), new DepartamentoJpaController().findDeptosFromProvincia(((Provincia) combo.getSelectedItem()).getId()), true);
            } else {
               UTIL.loadComboBox(panel.getCbDepartamentos(), null, true);
            }

         } else if (combo.getName().equalsIgnoreCase("cbDepartamentos")) {
            if (combo.getSelectedIndex() > 0) {
               UTIL.loadComboBox(panel.getCbMunicipios(), new MunicipioJpaController().findMunicipiosFromDepto(((Departamento) combo.getSelectedItem()).getId()), true);
            } else {
               UTIL.loadComboBox(panel.getCbMunicipios(), null, true);
            }
         }
      }
      // </editor-fold>
   }

   @Override
   public void mouseReleased(MouseEvent e) {
      Integer selectedRow = ((javax.swing.JTable) e.getSource()).getSelectedRow();
      javax.swing.table.DefaultTableModel dtm =
              (javax.swing.table.DefaultTableModel) ((javax.swing.JTable) e.getSource()).getModel();
      if (selectedRow > -1) {
         EL_OBJECT = DAO.getEntityManager().find(Proveedor.class,
                 Integer.valueOf((dtm.getValueAt(selectedRow, 0)).toString()));
      }
   }

   public void initContenedor(java.awt.Frame frame, boolean modal) {
      //init contenedor
      contenedor = new JDContenedor(frame, modal, "ABM - " + CLASS_NAME);
      contenedor.hideBtmEliminar();
      try {
         UTIL.getDefaultTableModel(contenedor.getjTable1(), colsName, colsWidth);
      } catch (Exception ex) {
         Logger.getLogger(ProveedorJpaController.class.getName()).log(Level.SEVERE, null, ex);
      }
      //esconde el ID column
      UTIL.hideColumnTable(contenedor.getjTable1(), 0);
      cargarDTM(contenedor.getDTM(), null);
      //listener
      contenedor.setListener(this);
      contenedor.setVisible(true);
   }

   private void cargarDTM(DefaultTableModel dtm, String query) {
      UTIL.limpiarDtm(dtm);
      java.util.List<Proveedor> l;
      if (query == null) {
         l = DAO.getEntityManager().createNamedQuery(CLASS_NAME + ".findAll")
                 .getResultList();
      } else {
         // para cuando se usa el Buscador del ABM
         l = DAO.getEntityManager().createNativeQuery(query, Proveedor.class)
                 .getResultList();
      }

      for (Proveedor o : l) {
         dtm.addRow(new Object[] {
                    o.getId(),
                    o.getCodigo(),
                    o.getNombre(),
                    o.getCuit(),
                    ((o.getTele1() != null) ? o.getTele1().toString() : "-")
                      + ((o.getTele2() != null) ? o.getTele2().toString() : "-")
                  });
      }
   }

   private void initABM(boolean isEditing, ActionEvent e) throws MessageException {
      // <editor-fold defaultstate="collapsed" desc="checking Permiso">
      try {
         UsuarioJpaController.checkPermisos(PermisosJpaController.PermisoDe.ABM_PROVEEDORES);
      } catch (MessageException ex) {
         javax.swing.JOptionPane.showMessageDialog(null,ex.getMessage());
         return;
      }// </editor-fold>
      if (isEditing && EL_OBJECT == null) {
         throw new MessageException("Debe elegir una fila de la tabla");
      }
      panel = new PanelABMProveedores();
      UTIL.loadComboBox(panel.getCbCondicIVA(), new ContribuyenteJpaController().findContribuyenteEntities(), false);
      UTIL.loadComboBox(panel.getCbProvincias(), new ProvinciaJpaController().findProvinciaEntities(), true);
      UTIL.loadComboBox(panel.getCbDepartamentos(), null, true);
      UTIL.loadComboBox(panel.getCbMunicipios(), null, true);
      panel.setListener(this);
      if (isEditing) {
         setPanel(EL_OBJECT);
      }
      abm = new JDABM(true, ((javax.swing.JDialog) contenedor), panel);
      abm.setTitle("ABM " + CLASS_NAME + "es");
      abm.setListener(this);
      abm.setLocationRelativeTo(contenedor);
//      abm.setLocationByPlatform(true);
      abm.setVisible(true);
   }

   private void setPanel(Proveedor p) {
      panel.setTfCodigo(p.getCodigo());
      panel.setTfNombre(p.getNombre());
      panel.setTfDireccion(p.getDireccion());
      panel.setTfCUIT(String.valueOf(p.getCuit()));
      UTIL.setSelectedItem(panel.getCbCondicIVA(), p.getContribuyente().getNombre());
      UTIL.setSelectedItem(panel.getCbProvincias(), p.getProvincia().getNombre());
      UTIL.setSelectedItem(panel.getCbDepartamentos(), p.getDepartamento().getNombre());
      UTIL.setSelectedItem(panel.getCbMunicipios(), p.getMunicipio().getNombre());

      if (p.getCodigopostal() != null) {
         panel.setTfCP(p.getCodigopostal().toString());
      }

      if (p.getTele1() != null) {
         panel.setTfTele1(p.getTele1().toString());
         if (p.getInterno1() != null) {
            panel.setTfInterno1(p.getInterno1().toString());
         }
      }

      if (p.getTele2() != null) {
         panel.setTfTele2(p.getTele2().toString());
         if (p.getInterno2() != null) {
            panel.setTfInterno2(p.getInterno2().toString());
         }
      }

      if (p.getContacto() != null) {
         panel.setTfContacto(p.getContacto());
      }

      if (p.getEmail() != null) {
         panel.setTfEmail(p.getEmail());
      }

      if (p.getWebpage() != null) {
         panel.setTfWEB(p.getWebpage());
      }

   }

   private void setEntity() throws MessageException {
      if (EL_OBJECT == null) {
         EL_OBJECT = new Proveedor();
      }

      if (panel.getTfCodigo().length() < 1) {
         throw new MessageException("Ingresar un código");
      }

      if (panel.getTfNombre() == null || panel.getTfNombre().length() < 1) {
         throw new MessageException("Debe ingresar un nombre");
      }
      try {
         UTIL.CONTROLAR_CUIL(panel.getTfCUIT());
         panel.setIconoValidadorCUIT(true, "CUIT válido");
      } catch (NumberFormatException ex) {
         throw new MessageException(ex.getMessage());
      } catch (Exception ex) {
         panel.setIconoValidadorCUIT(false, ex.getMessage());
      }

      if (panel.getTfDireccion() == null) {
         throw new MessageException("Debe indicar la dirección de la proveedor");
      }
      if (panel.getCbProvincias().getSelectedIndex() < 1) {
         throw new MessageException("Debe especificar una Provincia y Departamento");
      }
      if (panel.getCbDepartamentos().getSelectedIndex() < 1) {
         throw new MessageException("Debe especificar un Departamento");
      }

      if (panel.getTfInterno1() != null && panel.getTfTele1() == null) {
         throw new MessageException("Especifique un número de teléfono 1 para el interno 1");
      }
      if (panel.getTfInterno2() != null && panel.getTfTele2() == null) {
         throw new MessageException("Especifique un número de teléfono 2 para el interno 2");
      }

      // NOT NULLABLE's
      EL_OBJECT.setCodigo(panel.getTfCodigo());
      EL_OBJECT.setNombre(panel.getTfNombre().toUpperCase());
      EL_OBJECT.setDireccion(panel.getTfDireccion());
      EL_OBJECT.setProvincia((Provincia) panel.getSelectedProvincia());
      EL_OBJECT.setDepartamento((Departamento) panel.getSelectedDepartamento());
      EL_OBJECT.setMunicipio((Municipio) panel.getSelectedMunicipio());
      EL_OBJECT.setContribuyente((Contribuyente) panel.getSelectedCondicIVA());
      EL_OBJECT.setCuit(new Long(panel.getTfCUIT()));

      //NULLABLE's
      if (panel.getTfCP().length() > 0) {
         EL_OBJECT.setCodigopostal(new Integer(panel.getTfCP()));
      }

      if (panel.getTfEmail().length() > 0) {
         EL_OBJECT.setEmail(panel.getTfEmail());
      }

      if (panel.getTfWEB().length() > 0) {
         EL_OBJECT.setWebpage(panel.getTfWEB());
      }
      if (panel.getTfTele1().length() > 0 && panel.getTfTele1().length() > 0) {
         EL_OBJECT.setTele1(new Long(panel.getTfTele1()));
         if (panel.getTfInterno1().length() > 0) {
            EL_OBJECT.setInterno1(new Integer(panel.getTfInterno1()));
         }
      }
      if (panel.getTfTele2().length() > 0 && panel.getTfTele2().length() > 0) {
         EL_OBJECT.setTele2(new Long(panel.getTfTele2()));
         if (panel.getTfInterno2().length() > 0) {
            EL_OBJECT.setInterno2(new Integer(panel.getTfInterno2()));
         }
      }

   }

   private void checkConstraints(Proveedor object) throws MessageException, Exception {
      String idQuery = "";

      if (object.getId() != null) {
         idQuery = "o.id!=" + object.getId() + " AND ";
      }
      try {
         DAO.getEntityManager().createNativeQuery("SELECT * FROM " + CLASS_NAME + " o "
                 + " WHERE " + idQuery + " o.nombre='" + object.getNombre() + "' ", Proveedor.class).getSingleResult();
         throw new MessageException("Ya existe otro " + CLASS_NAME + " con este nombre.");
      } catch (NoResultException ex) {
      }
      try {
         DAO.getEntityManager().createNativeQuery("SELECT * FROM " + CLASS_NAME + " o "
                 + " WHERE " + idQuery + " o.cuit=" + object.getCuit(), Proveedor.class).getSingleResult();
         throw new MessageException("Ya existe otro " + CLASS_NAME + " con este CUIT.");
      } catch (NoResultException ex) {
      }
      try {
         DAO.getEntityManager().createNativeQuery("SELECT * FROM " + CLASS_NAME + " o "
                 + " WHERE " + idQuery + " o.codigo='" + object.getCodigo() + "'", Proveedor.class).getSingleResult();
         throw new MessageException("Ya existe otro " + CLASS_NAME + " con este Código.");
      } catch (NoResultException ex) {
      }


      //persistiendo......
      if (object.getId() == null) {
         create(object);
      } else {
         edit(object);
      }
   }

   public void keyReleased(KeyEvent e) {
   }

   public void mouseClicked(MouseEvent e) {
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
}
