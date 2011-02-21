package controller;

import controller.PermisosJpaController.PermisoDe;
import controller.exceptions.*;
import entity.Caja;
import entity.PermisosCaja;
import entity.Usuario;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import entity.Permisos;
import generics.UTIL;
import gui.JDABM;
import gui.JDContenedor;
import gui.JDLogin;
import gui.JDcambiarPass;
import gui.PanelABMUsuarios;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.NoResultException;
import javax.swing.JFrame;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Administrador
 */
public class UsuarioJpaController implements ActionListener, MouseListener, KeyListener {

   public static final String ESTADO_ACTIVO = "Activo";
   public static final String ESTADO_BAJA = "Baja";
   /**
    * Usuario loggeado en la instancia de la app
    */
   private static Usuario CURRENT_USER;
   private JDLogin jDLogin;
   private final String[] colsName = {"Nº", "Nombre", "Estado", "Fecha Alta"};
   private final int[] colsWidth = {15, 50, 30, 50};
   private JDContenedor contenedor = null;
   private JDcambiarPass jdCambiarPass;
   private String CLASS_NAME = "Usuario";
   private JDABM abm;
   private PanelABMUsuarios panel;
   private Usuario EL_OBJECT;
   private boolean resetPwds = false;

   // <editor-fold defaultstate="collapsed" desc="CRUD...">
   public EntityManager getEntityManager() {
      return DAO.getEntityManager();
   }

   public void create(Usuario usuario) throws PreexistingEntityException, Exception {
      if (usuario.getPermisosCajaList() == null) {
         usuario.setPermisosCajaList(new ArrayList<PermisosCaja>());
      }
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         Permisos permisos = usuario.getPermisos();
         if (permisos == null) {
            throw new IllegalArgumentException("El objeto permisos no puede ser null");
         }
         permisos = em.merge(permisos);
         usuario.setPermisos(permisos);

         List<PermisosCaja> attachedPermisosCajaList = new ArrayList<PermisosCaja>();
         for (PermisosCaja permisosCajaListPermisosCajaToAttach : usuario.getPermisosCajaList()) {
            permisosCajaListPermisosCajaToAttach = em.merge(permisosCajaListPermisosCajaToAttach);
            attachedPermisosCajaList.add(permisosCajaListPermisosCajaToAttach);
         }
         usuario.setPermisosCajaList(attachedPermisosCajaList);
         em.persist(usuario);
         em.getTransaction().commit();
      } catch (Exception ex) {
         if (findUsuario(usuario.getId()) != null) {
            throw new PreexistingEntityException("Usuario " + usuario + " already exists.", ex);
         }
         throw ex;
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public void edit(Usuario usuario) throws IllegalOrphanException, NonexistentEntityException, Exception {
      System.out.println("edit Usuario..");
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         Usuario oldUsuario = em.find(Usuario.class, usuario.getId());
         List<PermisosCaja> oldPermisosCajaList = oldUsuario.getPermisosCajaList();
         List<PermisosCaja> newPermisosCajaList = usuario.getPermisosCajaList();
         oldPermisosCajaList.removeAll(newPermisosCajaList);
         for (PermisosCaja permisosCaja : oldPermisosCajaList) {
            em.remove(permisosCaja);
         }

         Permisos permisos = usuario.getPermisos();
         if (permisos == null) {
            throw new IllegalArgumentException("El objeto permisos no puede ser null");
         }
         permisos = em.merge(permisos);
         usuario.setPermisos(permisos);

         em.merge(usuario);
         em.getTransaction().commit();
      } catch (Exception ex) {
         if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
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
         Usuario usuario;
         try {
            usuario = em.getReference(Usuario.class, id);
         } catch (EntityNotFoundException enfe) {
            throw new NonexistentEntityException("The usuario with id " + id + " no longer exists.", enfe);
         }
         int cantBorradas = em.createQuery(""
                 + "DELETE FROM PermisosCaja WHERE usuario.id = " + usuario.getId()).executeUpdate();
         System.out.println("PermisosCaja borrados:" + cantBorradas);

         String createQuery = "UPDATE Permisos SET ";
         for (PermisoDe permisoDe : PermisoDe.values()) {
            createQuery += permisoDe + "= FALSE,";
         }
         createQuery += " WHERE usuario.id = " + usuario.getId();

         int cantPermisos = em.createQuery(createQuery).executeUpdate();
         System.out.println("Permisos borrados:" + cantPermisos);

         usuario.setEstado(2);
         em.remove(usuario);
         em.getTransaction().commit();
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public List<Usuario> findUsuarioEntities() {
      return findUsuarioEntities(true, -1, -1);
   }

   public List<Usuario> findUsuarioEntities(int maxResults, int firstResult) {
      return findUsuarioEntities(false, maxResults, firstResult);
   }

   private List<Usuario> findUsuarioEntities(boolean all, int maxResults, int firstResult) {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select object(o) from Usuario as o");
         if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
         }
         return q.getResultList();
      } finally {
         em.close();
      }
   }

   public Usuario findUsuario(Integer id) {
      EntityManager em = getEntityManager();
      try {
         return em.find(Usuario.class, id);
      } finally {
         em.close();
      }
   }

   public int getUsuarioCount() {
      EntityManager em = getEntityManager();
      try {
         return ((Long) em.createQuery("select count(o) from Usuario as o").getSingleResult()).intValue();
      } finally {
         em.close();
      }
   }// </editor-fold>

   /**
    *
    * @param nick
    * @param pwd
    * @return
    * @throws MessageException
    * @throws Exception
    */
   public Usuario checkLoginUser(String nick, String pwd) throws MessageException, Exception {
      try {
         CURRENT_USER = (Usuario) getEntityManager()
                 .createQuery("SELECT u FROM Usuario u WHERE u.nick ='" + nick + "' AND u.pass = '" + pwd + "' ")
                 .getSingleResult();
         if (CURRENT_USER != null && CURRENT_USER.getEstado() != 1) {
            CURRENT_USER = null;
            throw new MessageException("Usuario deshabilitado");
         }
      } catch (NoResultException ex) {
         throw new MessageException("Usuario/Contraseña no válido");
      }
      return CURRENT_USER;
   }

   public static Usuario getCurrentUser() {
      return CURRENT_USER;
   }

   public void initCambiarPass(java.awt.Frame frame) {
      jdCambiarPass = new JDcambiarPass(frame, true);
      jdCambiarPass.getLabelUsuario().setText(CURRENT_USER.getNick());
      jdCambiarPass.setListener(this);
      jdCambiarPass.setLocationRelativeTo(frame);
      jdCambiarPass.setVisible(true);
   }

   public void initLogin(JFrame frame) {
      jDLogin = new JDLogin(frame, true);
      jDLogin.setListener(this);
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      jDLogin.setLocation((screenSize.width - jDLogin.getWidth()) / 2, (screenSize.height - jDLogin.getHeight()) / 2);
      jDLogin.setAlwaysOnTop(true);
      jDLogin.setVisible(true);
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
      if (contenedor != null) {
//         Integer selectedRow = ((javax.swing.JTable) e.getSource()).getSelectedRow();
         int selectedRow = contenedor.getjTable1().getSelectedRow();
//         DefaultTableModel dtm = (DefaultTableModel) ((javax.swing.JTable) e.getSource()).getModel();
         if (selectedRow > -1) {
            EL_OBJECT = (Usuario) DAO.getEntityManager().find(Usuario.class,
                    Integer.valueOf((contenedor.getDTM().getValueAt(selectedRow, 0)).toString()));
         }
      }
   }

   public void keyTyped(KeyEvent e) {
   }

   public void keyPressed(KeyEvent e) {
   }

   public void keyReleased(KeyEvent e) {
      if (e.getComponent().getClass().equals(javax.swing.JTextField.class)) {
         javax.swing.JTextField tf = (javax.swing.JTextField) e.getComponent();
         // <editor-fold defaultstate="collapsed" desc="JDLogin">
         if (tf.getName().equalsIgnoreCase("ulogin") && (e.getKeyCode() == 10)) {
            try {
               CURRENT_USER = checkLoginUser(jDLogin.getTfU(), jDLogin.getPass());
               if (CURRENT_USER != null) {
                  jDLogin.dispose();
               }
            } catch (MessageException ex) {
               jDLogin.getjLabel3().setText(ex.getMessage());
            } catch (Exception ex) {
               jDLogin.showMessage(ex.getMessage(), "Error Login usuario", 0);
               Logger.getLogger(UsuarioJpaController.class.getName()).log(Level.FATAL, null, ex);
            }
         }// </editor-fold>
      } else if (e.getComponent().getClass().equals(javax.swing.JPasswordField.class)) {
         javax.swing.JPasswordField tf = (javax.swing.JPasswordField) e.getComponent();
         // <editor-fold defaultstate="collapsed" desc="JDLogin">
         if (tf.getName().equalsIgnoreCase("plogin") && (e.getKeyCode() == 10)) {
            try {
               CURRENT_USER = checkLoginUser(jDLogin.getTfU(), jDLogin.getPass());
               if (CURRENT_USER != null) {
                  System.out.println("Usuario " + CURRENT_USER + "logeado.");
                  jDLogin.dispose();
               }
            } catch (MessageException ex) {
               System.out.println("Messageexcep.......");
               jDLogin.getjLabel3().setText(ex.getMessage());
            } catch (Exception ex) {
               System.out.println("Excep.......");
               jDLogin.showMessage(ex.getMessage(), "Error Login usuario", 0);
               Logger.getLogger(UsuarioJpaController.class.getName()).log(Level.FATAL, null, ex);
            }
         }// </editor-fold>
         // <editor-fold defaultstate="collapsed" desc="JDCambiarPass">
         else if ((tf.getName().equalsIgnoreCase("p1")
                 || tf.getName().equalsIgnoreCase("p2")
                 || tf.getName().equalsIgnoreCase("p3")) && (e.getKeyCode() == 10)) {
            try {
               cambiarPwd();
            } catch (MessageException ex) {
               jdCambiarPass.getLabelMsj().setText(ex.getMessage());
            }

         }// </editor-fold>
      }
   }

   private void cambiarPwd() throws MessageException {
      if (jdCambiarPass.getPass1().equals(CURRENT_USER.getPass())) {
         if (jdCambiarPass.getPass2().equals(jdCambiarPass.getPass3())) {
            if (jdCambiarPass.getPass2().length() >= 5) {
               CURRENT_USER.setPass(jdCambiarPass.getPass2());
               DAO.doMerge(CURRENT_USER);
               jdCambiarPass.getLabelMsj().setText("Contraseña actualizada");
            } else {
               throw new MessageException("<html>La contraseña actual debe tener al menos<br> 5 caracteres</html>");
            }
         } else {
            throw new MessageException("Las nuevas contraseñas no coinciden");
         }
      } else {
         throw new MessageException("La contraseña actual no es correcta");
      }
   }

   public void initContenedor(javax.swing.JFrame frame) {
      contenedor = new JDContenedor(frame, true, "ABM - " + CLASS_NAME + "s");
      contenedor.getTfFiltro().setToolTipText("Filtra por nombre de " + CLASS_NAME);
      contenedor.setModoBuscador(false);
      contenedor.hideBtmEliminar();
      contenedor.hideBtmImprimir();
      UTIL.getDefaultTableModel(contenedor.getjTable1(), colsName, colsWidth);
      UTIL.hideColumnTable(contenedor.getjTable1(), 0);
      cargarDTM(contenedor.getDTM(), null);
      contenedor.setListener(this);
      contenedor.setVisible(true);
   }

   private void cargarDTM(DefaultTableModel dtm, String query) {
      UTIL.limpiarDtm(dtm);
      List<Usuario> l;
      if (query == null) {
         l = DAO.getEntityManager().createNamedQuery(CLASS_NAME + ".findAll").setHint("toplink.refresh", true).getResultList();
      } else { // para cuando se usa el Buscador del ABM
         l = DAO.getEntityManager().createNativeQuery(query, Usuario.class).getResultList();
      }

      for (Usuario o : l) {
         dtm.addRow(new Object[]{
                    o.getId(),
                    o.getNick(),
                    o.getEstado() == 1 ? "Activo" : "Baja",
                    UTIL.DATE_FORMAT.format(o.getFechaalta())
                 });
      }
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      try {
         if (e.getSource().getClass().equals(javax.swing.JButton.class)) {
            javax.swing.JButton boton = (javax.swing.JButton) e.getSource();

            if (boton.getName().equalsIgnoreCase("new")) {
               initABM(false);
            } else if (boton.getName().equalsIgnoreCase("edit")) {
               initABM(true);
            } else if (boton.getName().equalsIgnoreCase("resetPwd")) {
               resetPwds = true;
               panel.setEnabledPwdFields(resetPwds);
            } else if (boton.getName().equalsIgnoreCase("aceptar")) {
               String msj = EL_OBJECT == null ? "Registrado.." : "Modificado..";
               setAndPersistEntity();
               abm.showMessage(msj, CLASS_NAME, 1);
               abm.dispose();
               cargarDTM(contenedor.getDTM(), null);
            } else if (boton.getName().equalsIgnoreCase("cancelar")) {
               abm.dispose();
               EL_OBJECT = null;
            } else if (boton.getName().equalsIgnoreCase("exit")) {
               contenedor.dispose();
               contenedor = null;
            }
         }
      } catch (MessageException ex) {
         contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
      } catch (Exception ex) {
         contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
         ex.printStackTrace();
      }
   }

   private void initABM(boolean isEditing) throws Exception {
      // <editor-fold defaultstate="collapsed" desc="checking Permiso">
      try {
         UsuarioJpaController.CHECK_PERMISO(PermisosJpaController.PermisoDe.ABM_USUARIOS);
      } catch (MessageException ex) {
         javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage());
         return;
      }// </editor-fold>

      if (isEditing) {
         mouseReleased(null);
         if (EL_OBJECT == null) {
            throw new MessageException("Debe elegir una fila");
         }
      } else {
         EL_OBJECT = null;
      }

      panel = new PanelABMUsuarios();
      UTIL.getDefaultTableModel(
              panel.getTableCajas(),
              new String[]{"Caja", "Estado", "Permitir"},
              new int[]{150, 20, 1},
              new Class[]{String.class, String.class, Boolean.class},
              new int[]{2});

      cargarTablaCajas();
      panel.setListener(this);
      panel.getCbEstado().addItem(ESTADO_ACTIVO);
      panel.getCbEstado().addItem(ESTADO_BAJA);

      abm = new JDABM(true, contenedor, panel);
      if (isEditing) {
         setPanelABM(EL_OBJECT);
         panel.setEnabledPwdFields(false);
      }
      abm.setTitle("ABM - " + CLASS_NAME + "s");
      abm.setListener(this);
      abm.setVisible(true);
   }

   private void setPanelABM(Usuario u) {
      panel.setEnableTfNick(false);
      panel.setTfNick(u.getNick());
      panel.getCbEstado().setSelectedIndex(u.getEstado() - 1);

      panel.getCheckCajas().setSelected(u.getPermisos().getAbmCajas());
      panel.getCheckClientes().setSelected(u.getPermisos().getAbmClientes());
      panel.getCheckCompra().setSelected(u.getPermisos().getCompra());
      panel.getCheckDatosGeneral().setSelected(u.getPermisos().getDatosGeneral());
      panel.getCheckListaPrecios().setSelected(u.getPermisos().getAbmListaPrecios());
      panel.getCheckProductos().setSelected(u.getPermisos().getAbmProductos());
      panel.getCheckProveedores().setSelected(u.getPermisos().getAbmProveedores());
      panel.getCheckTesoreria().setSelected(u.getPermisos().getTesoreria());
      panel.getCheckUsuarios().setSelected(u.getPermisos().getAbmUsuarios());
      panel.getCheckVenta().setSelected(u.getPermisos().getVenta());
      panel.getCheckCerrarCajas().setSelected(u.getPermisos().getCerrarCajas());
      panel.getCheckABMCatalogoweb().setSelected(u.getPermisos().getAbmCatalogoweb());
      panel.getCheckABMOfertas().setSelected(u.getPermisos().getAbmOfertasweb());
      //estableciendo permisosCajas en la tabla
      List<PermisosCaja> permisosCajaList = u.getPermisosCajaList();
      for (PermisosCaja permisosCaja : permisosCajaList) {
         for (int i = panel.getDtm().getRowCount() - 1; i > -1; i--) {
            if (((Caja) panel.getDtm().getValueAt(i, 0)).equals(permisosCaja.getCaja())) {
               panel.getDtm().setValueAt(true, i, 2);
            }
         }
      }
   }

   private void setAndPersistEntity() throws MessageException, PreexistingEntityException, Exception {
      if (EL_OBJECT == null) {
         EL_OBJECT = new Usuario();
         EL_OBJECT.setPermisosCajaList(new ArrayList<PermisosCaja>());
         EL_OBJECT.setNick(panel.getTfNick());
      } else {
      }

      if (panel.getTfNick().length() < 1) {
         throw new MessageException("Ingrese un nombre de usuario");
      }

      //si el obj.id == null (porque es nuevo) || si decidió resetear la pwd
      if (EL_OBJECT.getId() == null || resetPwds == true) {
         //longitud de la password...
         if (panel.getjPasswordField1().length() >= 5) {
            //que coincidan las pwd's
            if (panel.getjPasswordField1().equals(panel.getjPasswordField2())) {
               EL_OBJECT.setPass(panel.getjPasswordField1());
            } else {
               throw new MessageException("Las contraseñas no coinciden");
            }
         } else {
            throw new MessageException("La contraseña debe tener como mínimo 5 caracteres");
         }
      }

      // <editor-fold defaultstate="collapsed" desc="checking UniqueConstraint Usuario.nick">
      try {
         String conID = "";
         if (EL_OBJECT.getId() != null) {
            conID = " AND o.id !=" + EL_OBJECT.getId();
         }
         getEntityManager().createNativeQuery(""
                 + "SELECT * FROM Usuario o "
                 + "WHERE o.nick ='" + panel.getTfNick() + "' " + conID, Usuario.class).getSingleResult();

         throw new MessageException("Ya existe un Usuario con este Nombre");
      } catch (NoResultException ex) {
         System.out.println("UNIQUE CONSTRAINT Usuario.nick Checked.... OK");
      }// </editor-fold>


      // 1 activo , 2 baja
      EL_OBJECT.setEstado(panel.getCbEstado().getSelectedIndex() + 1);
      EL_OBJECT.setPermisosCajaList(getPermisosCaja(EL_OBJECT.getPermisosCajaList()));

      Permisos permisos;
      if (EL_OBJECT.getId() == null) {
         permisos = new Permisos();
         EL_OBJECT.setPermisos(setPermisos(permisos));
         create(EL_OBJECT);
      } else {
         setPermisos(EL_OBJECT.getPermisos());
         edit(EL_OBJECT);
      }
   }

   private Permisos setPermisos(Permisos permisos) {
      permisos.setAbmCajas(panel.getCheckCajas().isSelected());
      permisos.setAbmClientes(panel.getCheckClientes().isSelected());
      permisos.setAbmListaPrecios(panel.getCheckListaPrecios().isSelected());
      permisos.setAbmProductos(panel.getCheckProductos().isSelected());
      permisos.setAbmProveedores(panel.getCheckProveedores().isSelected());
      permisos.setAbmUsuarios(panel.getCheckUsuarios().isSelected());
      permisos.setCompra(panel.getCheckCompra().isSelected());
      permisos.setVenta(panel.getCheckVenta().isSelected());
      permisos.setDatosGeneral(panel.getCheckDatosGeneral().isSelected());
      permisos.setTesoreria(panel.getCheckTesoreria().isSelected());
      permisos.setCerrarCajas(panel.getCheckCerrarCajas().isSelected());
      permisos.setAbmCatalogoweb(panel.getCheckABMCatalogoweb().isSelected());
      permisos.setAbmOfertasweb(panel.getCheckABMOfertas().isSelected());
      return permisos;
   }

   private List<PermisosCaja> getPermisosCaja(List<PermisosCaja> permisosCajaList) {
      List<Caja> cajaList = new ArrayList<Caja>();
      for (PermisosCaja permisosCaja : permisosCajaList) {
         cajaList.add(permisosCaja.getCaja());
      }
      DefaultTableModel dtm = panel.getDtm();
      PermisosCaja permisosCaja;
      for (int i = 0; i <= dtm.getRowCount() - 1; i++) {
         Caja cajaSelected = (Caja) dtm.getValueAt(i, 0);
         if (dtm.getValueAt(i, 2).toString().equalsIgnoreCase("true")) {
            if (!cajaList.contains(cajaSelected)) {
               permisosCaja = new PermisosCaja();
               permisosCaja.setCaja(cajaSelected);
               permisosCaja.setUsuario(EL_OBJECT);
               permisosCajaList.add(permisosCaja);
               System.out.println("ADD PermisoCaja -> Caja:" + cajaSelected);
            } else {
               System.out.println("Ya tiene permiso de Caja:" + cajaSelected.getNombre());
            }
         } else if (cajaList.contains(cajaSelected)) {
            //si la Caja está DESMARCADA && está caja ESTÁ PRESENTE EN cajaList
            // es porque se QUITO el permisosCaja de esta.
            PermisosCaja permisosCajaToDelete = null;
            // así que busca el permisosCaja que contiene cajaSelected
            for (PermisosCaja permisosCaja1 : permisosCajaList) {
               if (permisosCaja1.getCaja().equals(cajaSelected)) {
                  permisosCajaToDelete = permisosCaja1;
               }
            }
            // y lo borramos
            permisosCajaList.remove(permisosCajaToDelete);
            System.out.println("DEL PermisosCaja nº" + permisosCajaToDelete.getId()
                    + ", Caja:" + permisosCajaToDelete.getCaja()
                    + ", INDEX=" + cajaList.indexOf(cajaSelected));
         } else {
            System.out.println("No tenía ni va tener Caja:" + cajaSelected.getNombre());
         }
      }
      System.out.println("perisosCajaList=>" + permisosCajaList.size());
      return permisosCajaList;
   }

   public static void cerrarSessionActual() {
      System.out.println("cerrando session:" + getCurrentUser());
      CURRENT_USER = null;
   }

   /**
    * Verifica si el Usuario (actualmente logeado) tiene permiso para realizar
    * la acción.
    * @param permisoToCheck Permiso a checkear.
    * @throws MessageException Si no tiene permiso o si no se pudo conectarse
    * con la base de datos para checkear el permiso.
    */
   public static void CHECK_PERMISO(PermisoDe permisoToCheck) throws MessageException {
      CURRENT_USER = DAO.getEntityManager().find(Usuario.class, CURRENT_USER.getId());
      if (CURRENT_USER == null) {
         throw new MessageException("Error chequeando los permisos del usuario.\nVerificar conexión con la Base de datos");
      }
      Boolean permitido = null;
      if (PermisoDe.ABM_PRODUCTOS.equals(permisoToCheck)) {
         permitido = CURRENT_USER.getPermisos().getAbmProductos();

      } else if (PermisoDe.ABM_PROVEEDORES.equals(permisoToCheck)) {
         permitido = CURRENT_USER.getPermisos().getAbmProveedores();

      } else if (PermisoDe.ABM_CLIENTES.equals(permisoToCheck)) {
         permitido = CURRENT_USER.getPermisos().getAbmClientes();

      } else if (PermisoDe.ABM_CAJAS.equals(permisoToCheck)) {
         permitido = CURRENT_USER.getPermisos().getAbmCajas();

      } else if (PermisoDe.ABM_USUARIOS.equals(permisoToCheck)) {
         permitido = CURRENT_USER.getPermisos().getAbmUsuarios();

      } else if (PermisoDe.ABM_LISTA_PRECIOS.equals(permisoToCheck)) {
         permitido = CURRENT_USER.getPermisos().getAbmListaPrecios();

      } else if (PermisoDe.TESORERIA.equals(permisoToCheck)) {
         permitido = CURRENT_USER.getPermisos().getTesoreria();

      } else if (PermisoDe.DATOS_GENERAL.equals(permisoToCheck)) {
         permitido = CURRENT_USER.getPermisos().getDatosGeneral();

      } else if (PermisoDe.VENTA.equals(permisoToCheck)) {
         permitido = CURRENT_USER.getPermisos().getVenta();

      } else if (PermisoDe.COMPRA.equals(permisoToCheck)) {
         permitido = CURRENT_USER.getPermisos().getCompra();
      } else if (PermisoDe.CERRAR_CAJAS.equals(permisoToCheck)) {
         permitido = CURRENT_USER.getPermisos().getCerrarCajas();
      } else if( PermisoDe.ABM_CATALOGOWEB.equals(permisoToCheck)) {
         permitido = CURRENT_USER.getPermisos().getAbmCatalogoweb();
      } else if( PermisoDe.ABM_OFERTASWEB.equals(permisoToCheck)) {
         permitido = CURRENT_USER.getPermisos().getAbmOfertasweb();
      }
      if (!permitido) {
         throw new MessageException("Acceso denegado: No tiene permiso para "
                 + permisoToCheck.toString().replaceAll("_", " "));
      }
   }

   private void cargarTablaCajas() {
      List<Caja> cajasList = new CajaJpaController().findCajaEntities();
      UTIL.limpiarDtm(panel.getDtm());
      for (Caja caja : cajasList) {
         panel.getDtm().addRow(new Object[]{
                    caja,
                    caja.getEstado() ? "Activa" : "Baja",
                    false
                 });
      }
   }
}
