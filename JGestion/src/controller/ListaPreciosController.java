package controller;

import controller.exceptions.*;
import entity.ListaPrecios;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import entity.DetalleListaPrecios;
import entity.Rubro;
import utilities.general.UTIL;
import gui.JDABM;
import gui.JDContenedor;
import gui.PanelABMListaPrecio;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.NoResultException;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author FiruzzZ
 */
public class ListaPreciosController implements ActionListener, MouseListener, KeyListener {

   public static final String CLASS_NAME = "ListaPrecios";
   private final String[] colsName = {"id", "Nombre", "M. General", "Margen (%)", "Cat. Web"};
   private final int[] colsWidth = {10, 150, 20, 20, 20};
   private final Class[] colsClass = {Object.class, Object.class, Object.class, Object.class, Boolean.class};
   private JDContenedor contenedor = null;
   private JDABM abm;
   private PanelABMListaPrecio panel;
   private ListaPrecios EL_OBJECT;
   private List<Rubro> listaDeRubros;

   // <editor-fold defaultstate="collapsed" desc="CRUD y demás">
   public EntityManager getEntityManager() {
      return DAO.getEntityManager();
   }

   public void create(ListaPrecios listaPrecios) throws Exception {
      DAO.create(listaPrecios);
   }

   public void edit(ListaPrecios listaPrecios) throws IllegalOrphanException, NonexistentEntityException, Exception {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         em.merge(listaPrecios);

         em.getTransaction().commit();
      } catch (Exception ex) {
         String msg = ex.getLocalizedMessage();
         if (msg == null || msg.length() == 0) {
            Integer id = listaPrecios.getId();
            if (findListaPrecios(id) == null) {
               throw new NonexistentEntityException("The listaPrecios with id " + id + " no longer exists.");
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
         ListaPrecios listaPrecios;
         try {
            listaPrecios = em.getReference(ListaPrecios.class, id);
            listaPrecios.getId();
         } catch (EntityNotFoundException enfe) {
            throw new NonexistentEntityException("The listaPrecios with id " + id + " no longer exists.", enfe);
         }
         List<DetalleListaPrecios> detalleListaPreciosListOrphanCheck = listaPrecios.getDetalleListaPreciosList();
         for (DetalleListaPrecios d : detalleListaPreciosListOrphanCheck) {
            em.remove(d);
         }

         em.remove(listaPrecios);
         em.getTransaction().commit();
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public List<ListaPrecios> findAll() {
      return findListaPreciosEntities(true, -1, -1);
   }

   public List<ListaPrecios> findListaPreciosEntities(int maxResults, int firstResult) {
      return findListaPreciosEntities(false, maxResults, firstResult);
   }

   private List<ListaPrecios> findListaPreciosEntities(boolean all, int maxResults, int firstResult) {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select object(o) from ListaPrecios as o ORDER BY o.nombre");
         if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
         }
         return q.getResultList();
      } finally {
         em.close();
      }
   }

   public ListaPrecios findListaPrecios(Integer id) {
      EntityManager em = getEntityManager();
      try {
         return em.find(ListaPrecios.class, id);
      } finally {
         em.close();
      }
   }

   public int getListaPreciosCount() {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select count(o) from ListaPrecios as o");
         return ((Long) q.getSingleResult()).intValue();
      } finally {
         em.close();
      }
   }// </editor-fold>

   public void initContenedor(java.awt.Frame frame, boolean modal) {
      // <editor-fold defaultstate="collapsed" desc="checking Permiso">
      try {
         UsuarioController.checkPermiso(PermisosController.PermisoDe.ABM_LISTA_PRECIOS);
      } catch (MessageException ex) {
         javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage());
         return;
      }// </editor-fold>
      contenedor = new JDContenedor(frame, modal, "ABM " + CLASS_NAME);
      contenedor.hideBtmImprimir();
      contenedor.setSize(570 + 80, 300 + 50);
      try {
         UTIL.getDefaultTableModel(contenedor.getjTable1(), colsName, colsWidth, colsClass);
         UTIL.hideColumnTable(contenedor.getjTable1(), 0);
      } catch (Exception ex) {
         Logger.getLogger(FacturaCompraController.class.getName()).log(Level.SEVERE, null, ex);
      }
      //esconde la columna IVA-Producto
      cargarDTM(contenedor.getDTM(), null);
      contenedor.setListener(this);
      contenedor.setVisible(true);
   }

   private void cargarDTM(DefaultTableModel dtm, String query) {
      UTIL.limpiarDtm(dtm);
      List<ListaPrecios> l;
      if (query == null || query.length() < 1) {
         l = DAO.getEntityManager().createNamedQuery(CLASS_NAME + ".findAll").getResultList();
      } else {
         // para cuando se usa el Buscador del ABM
         l = DAO.getEntityManager().createNativeQuery(query, ListaPrecios.class).getResultList();
      }
      for (ListaPrecios o : l) {
         dtm.addRow(new Object[]{
                    o.getId(),
                    o.getNombre(),
                    o.getMargenGeneral() ? "Si" : "No",
                    o.getMargen().toString(),
                    o.getParaCatalogoWeb()
                 });
      }
   }

   private void initABM(boolean isEditing, ActionEvent e) throws MessageException {
      // <editor-fold defaultstate="collapsed" desc="checking Permiso">
      try {
         UsuarioController.checkPermiso(PermisosController.PermisoDe.ABM_LISTA_PRECIOS);
      } catch (MessageException ex) {
         javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage());
         return;
      }// </editor-fold>
      if (isEditing && EL_OBJECT == null) {
         throw new MessageException("Debe elegir una fila de la tabla");
      }

      //lista usada para comparación entre tablas (Rubros y RubrosAfectados)
      listaDeRubros = new RubroController().findRubros();
      panel = new PanelABMListaPrecio();
      panel.setListener(this);
      UTIL.getDefaultTableModel(
              panel.getjTable1(),
              new String[]{"id", "Rubro"},
              new int[]{5, 200});
      UTIL.hideColumnTable(panel.getjTable1(), 0);
      UTIL.getDefaultTableModel(
              panel.getjTable2(),
              new String[]{"id", "Rubro", "Margen %"},
              new int[]{5, 80, 40});
      UTIL.hideColumnTable(panel.getjTable2(), 0);

      if (isEditing) {
         setPanel(EL_OBJECT);
      }

      cargarRubros();
      abm = new JDABM(contenedor, "ABM " + CLASS_NAME, true, panel);
      if (e != null) {
         abm.setLocation(((java.awt.Component) e.getSource()).getLocation());
      }
      abm.setListener(this);
      abm.setVisible(true);
   }

   private void setPanel(ListaPrecios listaPrecios) {
      if (listaPrecios.getMargenGeneral()) {
         panel.setTfMargenGeneral(String.valueOf(listaPrecios.getMargen()));
      } else {
         panel.setTfMargenGeneral("");
      }
      panel.getCheckMargenGeneral().setSelected(listaPrecios.getMargenGeneral());
      panel.setTfNombre(listaPrecios.getNombre());
      panel.setTfMargenGeneral(listaPrecios.getMargen().toString());
      panel.setCheckCatalagoWEB(listaPrecios.getParaCatalogoWeb());
      cargarRubrosAfectados(listaPrecios.getDetalleListaPreciosList());
   }

   private void setEntity() throws MessageException, Exception {
      String nombre = panel.getTfNombre().trim();
      if (nombre.length() < 1) {
         throw new MessageException("Nombre no válido");
      }

      if (panel.getCheckMargenGeneral().isSelected()) {
         try {
            if (Double.valueOf(panel.getTfMargenGeneral()) < 0) {
               throw new MessageException("Margen general no puede ser menor a 0");
            }
         } catch (NumberFormatException e) {
            throw new MessageException("Margen general no válido");
         }
      } else {
         if (panel.getDTMAfectados().getRowCount() < 1) {
            throw new MessageException("No hay rubros afectados en la lista de precios"
                    + ", debe elegir al menos uno \no un marge general.");
         }
      }

      if (panel.getCheckCatalagoWEB().isSelected()) {
         ListaPrecios uniqueCatalogo = findListaPreciosParaCatalogo();
         if (EL_OBJECT != null && uniqueCatalogo != null) {
            if (!EL_OBJECT.equals(uniqueCatalogo)) {
               throw new MessageException("Ya existe una Lista de Precios de referencia para el Catlálogo Web.\n"
                       + uniqueCatalogo.getNombre());
            }
         }
      }
      ////////////////////////////////////////////////////////
      if (EL_OBJECT == null) {
         EL_OBJECT = new ListaPrecios();
      }

      EL_OBJECT.setNombre(nombre.toUpperCase());
      EL_OBJECT.setParaCatalogoWeb(panel.getCheckCatalagoWEB().isSelected());
      EL_OBJECT.setMargenGeneral(panel.getCheckMargenGeneral().isSelected());
      //si elegió margen general
      if (EL_OBJECT.getMargenGeneral()) {
         EL_OBJECT.setMargen(Double.valueOf(panel.getTfMargenGeneral()));
         //se borran los posibles detalles_lista_precios
         if (EL_OBJECT.getId() != null) {
            removeDetallesListaPrecios(EL_OBJECT.getId());
         }
         EL_OBJECT.setDetalleListaPreciosList(new ArrayList<DetalleListaPrecios>());
      } else {
         EL_OBJECT.setMargen(0.0);
         DefaultTableModel dtm = panel.getDTMAfectados();
         DetalleListaPrecios detalle = null;

         if (EL_OBJECT.getDetalleListaPreciosList() != null
                 && EL_OBJECT.getDetalleListaPreciosList().size() > 0) {
            System.out.println(" != null && size() > 0 y que loco");
            removeDetallesListaPrecios(EL_OBJECT.getId());
         }
         EL_OBJECT.setDetalleListaPreciosList(new ArrayList<DetalleListaPrecios>());
         for (int i = dtm.getRowCount() - 1; i >= 0; i--) {
            detalle = new DetalleListaPrecios();
            detalle.setListaPrecio(EL_OBJECT);
            detalle.setRubro((Rubro) dtm.getValueAt(i, 1));
            detalle.setMargen(Double.valueOf(dtm.getValueAt(i, 2).toString()));
            EL_OBJECT.getDetalleListaPreciosList().add(detalle);
         }
      }
   }

   private void removeDetallesListaPrecios(int id) throws Exception {
      EntityManager em = getEntityManager();
      try {
         em.getTransaction().begin();
         em.createNativeQuery("DELETE FROM detalle_lista_precios o where o.lista_precio = " + id).executeUpdate();
         em.getTransaction().commit();
      } catch (Exception e) {
         throw e;
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   private void checkConstraints(ListaPrecios object) throws MessageException, Exception {
      String idQuery = "";
      if (object.getId() != null) {
         idQuery = "o.id!=" + object.getId() + " AND ";
      }
      try {
         DAO.getEntityManager().createNativeQuery("SELECT * FROM lista_precios o "
                 + " WHERE " + idQuery + " o.nombre='" + object.getNombre() + "' ", ListaPrecios.class).getSingleResult();
         //si no sale NoResultException... es porque
         throw new MessageException("Ya existe otra " + CLASS_NAME + " con este nombre.");
      } catch (NoResultException ex) {
      }

      //persistiendo......
      if (object.getId() == null) {
         create(object);
      } else {
         edit(object);
      }
   }

   private void cargarRubrosAfectados(List<DetalleListaPrecios> detalleListaPreciosList) {
      if (detalleListaPreciosList != null) {
         DefaultTableModel dtmRubrosAfectados = (DefaultTableModel) panel.getjTable2().getModel();
         for (int i = dtmRubrosAfectados.getRowCount(); i > 0; i--) {
            dtmRubrosAfectados.removeRow(i - 1);
         }
         for (DetalleListaPrecios detalleListaPrecios : detalleListaPreciosList) {
            dtmRubrosAfectados.addRow(new Object[]{
                       detalleListaPrecios.getRubro().getIdrubro(),
                       detalleListaPrecios.getRubro(),
                       detalleListaPrecios.getMargen(),});
         }
         cargarRubros();
      }
   }

   private void cargarRubros() {
      DefaultTableModel dtmRubros = panel.getDTMRubros();
      for (int i = dtmRubros.getRowCount(); i > 0; i--) {
         dtmRubros.removeRow(i - 1);
      }

      DefaultTableModel dtmRubrosAfectados = panel.getDTMAfectados();
      boolean cargarRubroATabla = true;
      for (Rubro rubro : listaDeRubros) {
         for (int i = dtmRubrosAfectados.getRowCount() - 1; i >= 0; i--) {
            if (dtmRubrosAfectados.getValueAt(i, 1).toString().equals(rubro.getNombre())) {
               cargarRubroATabla = false;
               break;
            }
         }
         if (cargarRubroATabla) {
            dtmRubros.addRow(new Object[]{
                       rubro.getIdrubro(),
                       rubro
                    });
         }
         cargarRubroATabla = true;
      }
   }

   private void addRubro() throws MessageException {
      int selectedRow = panel.getjTable1().getSelectedRow();
      if (selectedRow > -1) {
         try {
            if (Double.valueOf(panel.getTfMargenPorRubro()) < 0) {
               throw new MessageException("Margen del Rubro no puede ser menor a 0.");
            }
         } catch (NumberFormatException e) {
            throw new MessageException("Margen no válido");
         }
         DefaultTableModel dtmRubrosAfectados = panel.getDTMAfectados();
         Rubro rubro = (Rubro) panel.getSelectedRubro();
         dtmRubrosAfectados.addRow(new Object[]{
                    rubro.getIdrubro(),
                    rubro,
                    panel.getTfMargenPorRubro()
                 });

         //quitar el rubro seleccionado de la tabla izquierda
         panel.getDTMRubros().removeRow(selectedRow);
      } else {
         throw new MessageException("Debe seleccionar un Rubro");
      }
   }

   private void delRubro() {
      int selectedRow = panel.getjTable2().getSelectedRow();
      if (selectedRow > -1) {
         DefaultTableModel dtmRubrosAfectados = panel.getDTMAfectados();
         dtmRubrosAfectados.removeRow(selectedRow);
         cargarRubros();
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

   public void keyTyped(KeyEvent e) {
   }

   public void keyPressed(KeyEvent e) {
   }

   public void actionPerformed(ActionEvent e) {
      // <editor-fold defaultstate="collapsed" desc="JButton">
      if (e.getSource().getClass().equals(javax.swing.JButton.class)) {
         javax.swing.JButton boton = (javax.swing.JButton) e.getSource();
         if (boton.getName().equalsIgnoreCase("new")) {
            try {
               EL_OBJECT = null;
               initABM(false, e);

            } catch (MessageException ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(SucursalController.class.getName()).log(Level.SEVERE, null, ex);
            }
         } else if (boton.getName().equalsIgnoreCase("edit")) {
            try {
               initABM(true, e);
            } catch (MessageException ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(SucursalController.class.getName()).log(Level.SEVERE, null, ex);
            }

         } else if (boton.getName().equalsIgnoreCase("del")) {
            try {
               if (EL_OBJECT == null) {
                  throw new MessageException("No hay " + CLASS_NAME + " seleccionada");
               }
               destroy(EL_OBJECT.getId());
               contenedor.showMessage("Lista de precios eliminada", CLASS_NAME, 1);
               cargarDTM(contenedor.getDTM(), null);
            } catch (MessageException ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (NonexistentEntityException ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(SucursalController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(SucursalController.class.getName()).log(Level.SEVERE, null, ex);
            }
         } else if (boton.getName().equalsIgnoreCase("Print")) {
         } else if (boton.getName().equalsIgnoreCase("exit")) {
            contenedor.dispose();
            contenedor = null;
         } else if (boton.getName().equalsIgnoreCase("aceptar")) {
            try {
               String msg = EL_OBJECT == null ? "creada." : "modificada.";
               setEntity();
               String msj = "Lista de precios " + msg;
               checkConstraints(EL_OBJECT);
               abm.showMessage(msj, CLASS_NAME, 1);
               cargarDTM(contenedor.getDTM(), null);
               if (EL_OBJECT.getId() != null) {
                  abm.dispose();
               }
            } catch (MessageException ex) {
               abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
               Logger.getLogger(SucursalController.class.getName()).log(Level.SEVERE, null, ex);
            }
         } else if (boton.getName().equalsIgnoreCase("cancelar")) {
            abm.dispose();
            panel = null;
            abm = null;
            EL_OBJECT = null;
         } else if (boton.getName().equalsIgnoreCase("addRubro")) {
            try {
               addRubro();
            } catch (MessageException ex) {
               abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
               ex.printStackTrace();
            }

         } else if (boton.getName().equalsIgnoreCase("delRubro")) {
            delRubro();
         }
         return;
      }// </editor-fold>

   }

   public void mouseReleased(MouseEvent e) {
      Integer selectedRow = ((javax.swing.JTable) e.getSource()).getSelectedRow();
      DefaultTableModel dtm =
              (DefaultTableModel) ((javax.swing.JTable) e.getSource()).getModel();
      if (selectedRow > -1) {
         EL_OBJECT = DAO.getEntityManager().find(ListaPrecios.class,
                 Integer.valueOf((dtm.getValueAt(selectedRow, 0)).toString()));
      }
   }

   public void keyReleased(KeyEvent e) {
      if (e.getComponent().getClass().equals(javax.swing.JTextField.class)) {
         javax.swing.JTextField tf = (javax.swing.JTextField) e.getComponent();
         if (tf.getName().equalsIgnoreCase("tfFiltro")) {
//                armarQuery(tf.getText().trim());
         }
      }
   }

   /**
    * Retorna la ListaPrecio marcada como referencia para el Catalogo Web or <code>null</code>
    * if there is not.
    * @return una instancia de {@link ListaPrecios}
    */
   public ListaPrecios findListaPreciosParaCatalogo() {
      ListaPrecios o;
      try {
         o = (ListaPrecios) DAO.getEntityManager().createQuery("SELECT o FROM " + ListaPrecios.class.getSimpleName() + " o WHERE o.paraCatalogoWeb = TRUE").getSingleResult();
      } catch (NoResultException e) {
         o = null;
      }
      return o;
   }
}
