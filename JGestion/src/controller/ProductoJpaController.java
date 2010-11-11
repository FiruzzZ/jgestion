package controller;

import controller.exceptions.*;
import entity.Iva;
import entity.Marca;
import entity.Producto;
import entity.Rubro;
import entity.Stock;
import entity.Sucursal;
import generics.UTIL;
import entity.Unidadmedida;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import gui.JDABM;
import gui.JDBuscador;
import gui.JDContenedor;
import gui.JDStockGral;
import gui.JDcambiarHora;
import gui.PanelABMProductos;
import gui.PanelBuscadorMovimientosPro;
import gui.PanelProductoListados;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.io.File;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.NoResultException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author FiruzzZ
 */
public class ProductoJpaController implements ActionListener, KeyListener {

   public final String CLASS_NAME = Producto.class.getSimpleName();
   private JDContenedor contenedor;
   private JDABM abm;
   private final String[] colsName = {"Nº", "Código", "Nombre", "Marca", "Stock Gral."};
   private final int[] colsWidth = {15, 50, 100, 50, 20};
   private PanelABMProductos panel;
   private Producto EL_OBJECT;
   /** almacena temporalmente el archivo de la imagen del producto */
   private File fotoFile;
   /** Formas de registrar el COSTO COMPRA de los productos */
   public static final int PPP = 3, ANTIGUO = 2, ULTIMA_COMPRA = 1;
   private PanelBuscadorMovimientosPro panelito;
   private JDBuscador buscador;
   private PanelProductoListados panelProductoListados;

   public EntityManager getEntityManager() {
      return DAO.getEntityManager();
   }

   // <editor-fold defaultstate="collapsed" desc="create, edit, destroy, Listing's...">
   public void create(Producto producto) {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         Iva iva = producto.getIva();
         if (iva != null) {
            iva = em.getReference(iva.getClass(), iva.getId());
            producto.setIva(iva);
         }
         Marca marca = producto.getMarca();
         if (marca != null) {
            marca = em.getReference(marca.getClass(), marca.getId());
            producto.setMarca(marca);
         }
         em.persist(producto);
         em.getTransaction().commit();
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public void edit(Producto producto) throws NonexistentEntityException, Exception {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         Iva ivaNew = producto.getIva();
         Marca marcaNew = producto.getMarca();
         if (ivaNew != null) {
            ivaNew = em.getReference(ivaNew.getClass(), ivaNew.getId());
            producto.setIva(ivaNew);
         }
         if (marcaNew != null) {
            marcaNew = em.getReference(marcaNew.getClass(), marcaNew.getId());
            producto.setMarca(marcaNew);
         }
         producto = em.merge(producto);
         em.getTransaction().commit();
      } catch (Exception ex) {
         String msg = ex.getLocalizedMessage();
         if (msg == null || msg.length() == 0) {
            Integer id = producto.getId();
            if (findProducto(id) == null) {
               throw new NonexistentEntityException("The producto with id " + id + " no existe mas.");
            }
         }
         throw ex;
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public void destroy(Integer id) throws NonexistentEntityException, MessageException {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         Producto producto;
         try {
            producto = em.getReference(Producto.class, id);
            producto.getId();
         } catch (EntityNotFoundException enfe) {
            throw new NonexistentEntityException("The producto with id " + id + " no existe mas.", enfe);
         }
         em.remove(producto);
         em.getTransaction().commit();
      } catch (Exception ex) {
         throw new MessageException("No puede eliminar este producto, porque contiene un historial de compra/venta.");
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public List<Producto> findProductoEntities() {
      return findProductoEntities(true, -1, -1);
   }

   public List<Producto> findProductoEntities(int maxResults, int firstResult) {
      return findProductoEntities(false, maxResults, firstResult);
   }

   private List<Producto> findProductoEntities(boolean all, int maxResults, int firstResult) {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select o from Producto as o order by o.nombre");
         if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
         }
         return q.getResultList();
      } finally {
         em.close();
      }
   }

   public Producto findProducto(Integer id) {
      EntityManager em = getEntityManager();
      try {
         return em.find(Producto.class, id);
      } finally {
         em.close();
      }
   }

   public int getProductoCount() {
      EntityManager em = getEntityManager();
      try {
         return ((Long) em.createQuery("select count(o) from Producto as o").getSingleResult()).intValue();
      } finally {
         em.close();
      }
   }
   // </editor-fold>

   public void initContenedor(JFrame owner, boolean modal, boolean modoBuscador) {
      contenedor = new JDContenedor(owner, modal, "ABM - " + CLASS_NAME);
      contenedor.getTfFiltro().setToolTipText("Filtra por nombre del " + CLASS_NAME);
      contenedor.setModoBuscador(modoBuscador);
      contenedor.getbNuevo().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            try {
               EL_OBJECT = null;
               initABM(false);
            } catch (MessageException ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(DepartamentoJpaController.class.getName()).log(Level.SEVERE, null, ex);
            }
         }
      });
      contenedor.getbModificar().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            try {
               initABM(true);
            } catch (MessageException ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(DepartamentoJpaController.class.getName()).log(Level.SEVERE, null, ex);
            }
         }
      });
      contenedor.getbBorrar().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            try {
               eliminarProducto();
               contenedor.showMessage("Eliminado..", CLASS_NAME, 1);
            } catch (MessageException ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(DepartamentoJpaController.class.getName()).log(Level.SEVERE, null, ex);
            }
         }
      });
      contenedor.getbImprimir().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            try {
               doReportProductList();
            } catch (MessageException ex) {
               contenedor.showMessage(ex.getMessage(), "Advertencia", 2);
            } catch (Exception ex) {
               contenedor.showMessage(ex.getMessage(), "Error REPORT!", 0);
            }
         }
      });
      UTIL.getDefaultTableModel(contenedor.getjTable1(), colsName, colsWidth);
      UTIL.hideColumnTable(contenedor.getjTable1(), 0);
      cargarDTMContenedor(null);
      contenedor.setListener(this);
      contenedor.setVisible(true);
   }

   private void initABM(boolean isEditing) throws Exception {
      // <editor-fold defaultstate="collapsed" desc="checking Permiso">
      try {
         UsuarioJpaController.checkPermisos(PermisosJpaController.PermisoDe.ABM_PRODUCTOS);
      } catch (MessageException ex) {
         javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage());
         return;
      }// </editor-fold>
      if (isEditing) {
         EL_OBJECT = getSelectedFromContenedor();
         if (EL_OBJECT == null) {
            throw new MessageException("Debe elegir una fila");
         }
      }
      panel = new PanelABMProductos();
      panel.hideSucursal();
      panel.setListeners(this);
      panel.getbQuitarFoto().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            panel.getjLabelFoto().setText("[ Sin imagen ]");
            panel.getjLabelFoto().setIcon(null);
            fotoFile = null;
         }
      });
      UTIL.loadComboBox(panel.getCbIVA(), new IvaJpaController().findIvaEntities(), false);
      UTIL.loadComboBox(panel.getCbMarcas(), new MarcaJpaController().findMarcaEntities(), false);
      UTIL.loadComboBox(panel.getCbMedicion(), new UnidadmedidaJpaController().findUnidadmedidaEntities(), false);
      UTIL.loadComboBox(panel.getCbRubro(), new RubroJpaController().findRubros(), false);
      UTIL.loadComboBox(panel.getCbSubRubro(), new RubroJpaController().findRubros(), true);

      abm = new JDABM(true, contenedor, panel);
      if (isEditing) {
         setPanelABM(EL_OBJECT);
      } else {
         fotoFile = null;
      }
      abm.setTitle("ABM - " + CLASS_NAME + "s");
      abm.setLocationRelativeTo(contenedor);
      abm.setListener(this);
      abm.setVisible(true);
   }

   private void cargarDTMContenedor(String query) {
      try {
         DefaultTableModel dtm = contenedor.getDTM();
         UTIL.limpiarDtm(dtm);
         List<Producto> l;
         if (query == null) {
            l = DAO.getEntityManager().createQuery("SELECT o FROM " + CLASS_NAME + " o ORDER BY o.codigo").setHint("toplink.refresh", true).getResultList();
         } else {
            l = (List<Producto>) DAO.getNativeQueryResultList(query, Producto.class);
         }
         for (Producto o : l) {
            dtm.addRow(new Object[]{
                       o.getId(),
                       o.getCodigo(),
                       o.getNombre(),
                       o.getMarca().getNombre(),
                       o.getStockactual()
                    });
         }
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   private void setPanelABM(Producto m) throws IOException, Exception {
      UTIL.setSelectedItem(panel.getCbMarcas(), m.getMarca().getNombre());
      UTIL.setSelectedItem(panel.getCbIVA(), m.getIva().getIva().toString());
      UTIL.setSelectedItem(panel.getCbMedicion(), m.getIdunidadmedida().getNombre());
      UTIL.setSelectedItem(panel.getCbRubro(), m.getRubro().getNombre());
      if (m.getSubrubro() != null) {
         UTIL.setSelectedItem(panel.getCbSubRubro(), m.getSubrubro().getNombre());
      }
      panel.setTfCodigo(m.getCodigo());
      panel.setTfNombre(m.getNombre());
      panel.getCbTipoMargen().setSelectedIndex(m.getTipomargen() - 1);
      panel.setTfMargen(String.valueOf(m.getMargen()));
      panel.setTfStockMinimo(String.valueOf(m.getStockminimo()));
      panel.setTfStockMax(String.valueOf(m.getStockmaximo()));
      panel.setTfStockActual(String.valueOf(m.getStockactual()));
      panel.setTfPrecio(UTIL.PRECIO_CON_PUNTO.format(m.getPrecioVenta()));
      panel.setTfCostoCompra(UTIL.PRECIO_CON_PUNTO.format(m.getCostoCompra()));
      panel.setDateUltimaCompra(m.getUltimaCompra());
      if (m.getFoto() != null) {
         if (m.getFoto().length > 0) {
            UTIL.setImageAsIconLabel(panel.getjLabelFoto(), UTIL.imageToFile(m.getFoto(), null));
         }
      }
   }

   public Integer getStockGlobal(int productoID) {
      try {
         return new StockJpaController().getStockGlobal(productoID);
      } catch (Exception ex) {
         ex.printStackTrace();
      }
      return 666;

   }

   private void setEntity() throws MessageException, IOException, Exception {
      if (EL_OBJECT == null) {
         EL_OBJECT = new Producto();
      }
      String codigo = panel.getTfCodigo().trim();
      String nombre = panel.getTfNombre().trim();
      // <editor-fold defaultstate="collapsed" desc="CTRL restrictions......">
      if (codigo.length() < 1) {
         throw new MessageException("Código no válido");
      }
      if (nombre.length() < 1) {
         throw new MessageException("Ingrese un nombre");
      }
      if (nombre.length() > 250) {
         throw new MessageException("Nombre del producto ridículamente largo (máximo 250 caracteres)");
      }

      try {
         if (Integer.valueOf(panel.getTfStockMinimo()) < 0) {
            throw new MessageException("Stock mínimo no válido. Debe ser mayor o igual a 0");
         }
      } catch (NumberFormatException ex) {
         throw new MessageException("Número de stock mínimo no válido");
      }
      try {
         if (Integer.valueOf(panel.getTfStockMax()) < 0) {
            throw new MessageException("Stock máximo no válido. Debe ser mayor o igual a 0");

         }
      } catch (NumberFormatException ex) {
         throw new MessageException("número de stock máximo no válido");
      }
      try {
         if (panel.getTfMargen().length() > 0) {
            if (Double.valueOf(panel.getTfMargen()) < 0) {
               throw new MessageException("Margen de ganancia no puede ser menor a 0");
            }
         }
      } catch (NumberFormatException ex) {
         throw new MessageException("número de Margen no válido");
      }
      try {
         if (panel.getTfPrecio().length() > 0) {
            if (Double.valueOf(panel.getTfPrecio()) < 0) {
               throw new MessageException("El precio no puede ser menor a 0");
            }
         }
      } catch (NumberFormatException ex) {
         throw new MessageException("monto de Precio no válido");
      }
      try {
         EL_OBJECT.setMarca((Marca) panel.getCbMarcas().getSelectedItem());
      } catch (ClassCastException ex) {
         throw new MessageException("Debe especificar una Marca");
      }
      try {
         EL_OBJECT.setIva((Iva) panel.getCbIVA().getSelectedItem());
      } catch (ClassCastException ex) {
         throw new MessageException("Debe especificar un IVA");
      }
      try {
         EL_OBJECT.setIdunidadmedida((Unidadmedida) panel.getCbMedicion().getSelectedItem());
      } catch (ClassCastException ex) {
         throw new MessageException("Debe especificar una Unidad de medida");
      }// </editor-fold>

      // NOT NULL's
      EL_OBJECT.setCodigo(codigo);
      EL_OBJECT.setNombre(nombre);
      EL_OBJECT.setStockminimo(Integer.valueOf(panel.getTfStockMinimo()));
      EL_OBJECT.setStockmaximo(Integer.valueOf(panel.getTfStockMax()));
      try {
         EL_OBJECT.setRubro((Rubro) panel.getCbRubro().getSelectedItem());
      } catch (ClassCastException ex) {
         throw new MessageException("Debe crear al menos un Rubro para poder dar de alta los Productos."
                 + "\nMenú: Productos -> Rubros");
      }
      if (panel.getCbSubRubro().getSelectedIndex() > 0) {
         EL_OBJECT.setSubrubro((Rubro) panel.getCbSubRubro().getSelectedItem());
      } else {
         EL_OBJECT.setSubrubro(null);
      }
      EL_OBJECT.setTipomargen(panel.getCbTipoMargen().getSelectedIndex() + 1);
      EL_OBJECT.setMargen((panel.getTfMargen().length() > 0) ? Double.valueOf(panel.getTfMargen()) : 0);
      EL_OBJECT.setPrecioVenta(panel.getTfPrecio().length() > 0 ? Double.valueOf(panel.getTfPrecio()) : 0.0);

      // no setteable desde la GUI
      // default's....
      EL_OBJECT.setRemunerativo(true);
      if (EL_OBJECT.getCostoCompra() == null) {
         // este se actualiza cuando se cargan FacturaCompra's
         EL_OBJECT.setCostoCompra(0.0);
      }

      // NULLABLE'sssssssss

      if (panel.getTaDescrip().length() > 0) {
         EL_OBJECT.setDescripcion(panel.getTaDescrip());
      }

      if (fotoFile != null) {
         //si se elijió algún archivo imagen ...
         EL_OBJECT.setFoto(UTIL.getBytesFromFile(fotoFile));
      } else {
         if (panel.getjLabelFoto().getIcon() == null) {
            //si se quitó la que había seleccionado o la que estaba antiguamente
            EL_OBJECT.setFoto(null);
         }
      }
   }

   @Deprecated
   public void keyPressed(KeyEvent e) {
   }

   public void keyTyped(KeyEvent e) {
      if (e.getComponent().getClass().equals(javax.swing.JTextField.class)) {
         javax.swing.JTextField tf = (javax.swing.JTextField) e.getComponent();
         if (tf.getName().equalsIgnoreCase("tfprecio") || tf.getName().equalsIgnoreCase("tfmargen")) {
            UTIL.solo_numeros_y_un_punto(tf.getText(), e);
         }
      }
   }

   public void keyReleased(KeyEvent e) {
      if (e.getComponent().getClass().equals(javax.swing.JTextField.class)) {
         javax.swing.JTextField tf = (javax.swing.JTextField) e.getComponent();
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
         query = "SELECT * FROM " + CLASS_NAME + " o WHERE o.nombre LIKE '" + filtro + "%' ORDER BY o.codigo";
      }
      cargarDTMContenedor(query);
   }

   private void checkConstraints(Producto object) throws MessageException, Exception {
      EntityManager em = getEntityManager();
      String idQuery = "";
      if (object.getId() != null) {
         idQuery = "o.id!=" + object.getId() + " AND ";
      }
      try {
         em.createNativeQuery("SELECT * FROM " + CLASS_NAME + " o "
                 + " WHERE " + idQuery + "  " + " o.codigo='" + object.getCodigo() + "'", Producto.class).getSingleResult();
         throw new MessageException("Ya existe un " + CLASS_NAME + " con este codigo.");
      } catch (NoResultException ex) {
      }
      try {
         em.createNativeQuery("SELECT * FROM " + CLASS_NAME + " o "
                 + " WHERE " + idQuery + "  " + " o.nombre='" + object.getNombre() + "'", Producto.class).getSingleResult();
         throw new MessageException("Ya existe un " + CLASS_NAME + " con este nombre.");
      } catch (NoResultException ex) {
      }

      if (object.getId() == null) {
         create(object);
      } else {
         edit(object);
      }

   }

   private void cargarImagen() throws IOException, Exception {
      javax.swing.JFileChooser filec = new javax.swing.JFileChooser();
      javax.swing.filechooser.FileNameExtensionFilter filter = new javax.swing.filechooser.FileNameExtensionFilter("Imagenes", "jpg", "bmp", "jpeg", "png");
      filec.setFileFilter(filter);
      filec.addChoosableFileFilter(filter);
      int val = filec.showOpenDialog(null);
      if (val == javax.swing.JFileChooser.APPROVE_OPTION) {
         fotoFile = filec.getSelectedFile();
         if (UTIL.isImagenExtension(fotoFile)) {
            panel.getjLabelFoto().setText(null);
            panel.setjLabelFoto(UTIL.setImageAsIconLabel(panel.getjLabelFoto(), fotoFile));
         } else {
            abm.showMessage("El archivo debe ser una imagen (bmp, jpg, jpeg, png, tif)", "Extensión de archivo", 0);
         }
      }
   }

   public void actionPerformed(ActionEvent e) {
      // <editor-fold defaultstate="collapsed" desc="JButton">
      if (e.getSource().getClass().equals(javax.swing.JButton.class)) {
         javax.swing.JButton boton = (javax.swing.JButton) e.getSource();
         if (boton.getName().equalsIgnoreCase("exit")) {
            contenedor.dispose();
            if (!contenedor.isModoBuscador()) {
               contenedor = null;
            }
         } else if (boton.getName().equalsIgnoreCase("aceptar")) {
            try {
               setEntity();
               String msj = EL_OBJECT.getId() == null ? "Registrado.." : "Modificado..";
               checkConstraints(EL_OBJECT);
               abm.showMessage(msj, CLASS_NAME, 1);
               //si se está editando.. cierra la ventana post aceptación
               if (EL_OBJECT.getId() != null) {
                  abm.dispose();
               }

               cargarDTMContenedor(null);
               EL_OBJECT = null;
            } catch (MessageException ex) {
               abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
               Logger.getLogger(DepartamentoJpaController.class.getName()).log(Level.SEVERE, null, ex);
            }
         } else if (boton.getName().equalsIgnoreCase("cancelar")) {
            abm.dispose();
            panel = null;
            abm = null;
            EL_OBJECT = null;

         } else if (boton.getName().equalsIgnoreCase("buscarFoto")) {
            try {
               cargarImagen();
            } catch (IOException ex) {
               abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(ProductoJpaController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
               abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(ProductoJpaController.class.getName()).log(Level.SEVERE, null, ex);
            }
         } else if (boton.getName().equalsIgnoreCase("marcas")) {
            new MarcaJpaController().initJD(abm, true);
            UTIL.loadComboBox(panel.getCbMarcas(), new MarcaJpaController().findMarcaEntities(), false);
         } else if (boton.getName().equalsIgnoreCase("bStockGral")) {
            try {
               initStockGral(EL_OBJECT);
            } catch (MessageException ex) {
               Logger.getLogger(ProductoJpaController.class.getName()).log(Level.SEVERE, null, ex);
            }
         }
         return;
      } // </editor-fold>

   }

   /**
    * Busca un producto por su código
    * @param codigoProducto
    * @return a entity of Producto. If the Producto.codigo doesn't exist
    * will return <code>null</code>
    */
   Producto findProductoByCodigo(String codigoProducto) {
      try {
         return (Producto) DAO.getEntityManager().createNamedQuery(CLASS_NAME + ".findByCodigo").setParameter("codigo", codigoProducto).getSingleResult();
      } catch (NoResultException ex) {
         return null;
      }
   }

   Producto getProductoSelected() {
      int selectedRow = contenedor.getjTable1().getSelectedRow();
      if (selectedRow > -1) {
         return (Producto) DAO.getEntityManager().find(Producto.class,
                 Integer.valueOf((contenedor.getDTM().getValueAt(selectedRow, 0)).toString()));
      } else {
         return null;
      }
   }

   /**
    * Actualiza el costo de compra del producto, dependiendo del cambio q se elija
    * @param producto
    * @param newPrecioUnitario
    * @param cantidad
    * @param costoCompra 1 = ULTIMA_COMPRA, 2 = ANTIGUO (o sea no cambia nada), 3 = PPP
    */
   void updateCostoCompra(Producto producto, double newPrecioUnitario, int cantidad, int costoCompra) throws Exception {
      if (costoCompra == ULTIMA_COMPRA) {
         producto.setCostoCompra(newPrecioUnitario);

      } else if (costoCompra == PPP) {
         double ppp = ((producto.getCostoCompra() * producto.getStockactual())
                 + (newPrecioUnitario * cantidad));
         if (producto.getStockactual() < 0) {
            throw new MessageException("No se puede hacer un cálculo de PPP siendo el stock actual del producto menor a 0\nProducto: " + producto.getNombre() + "\nStock general: " + producto.getStockactual());
         }
         int totalStock = producto.getStockactual() + cantidad;
         ppp = (ppp / totalStock);
         producto.setCostoCompra(Double.parseDouble(UTIL.PRECIO_CON_PUNTO.format(ppp)));

      } else if (costoCompra == ANTIGUO) {
         //respeta el costoCompra anterior, o sea.. no hace nada     
      }

      producto.setUltimaCompra(new Date());
      DAO.doMerge(producto);
   }

   /**
    * Actualiza el atributo stockActual de la entidad Producto según stock.
    * @param producto al cual se le modificará el {@link Producto#stockactual}
    * @param cantidad si es una Venta, DEBE pasarse un valor NEGATIVO (para restar);
    */
   void updateStockActual(Producto producto, int cantidad) {
      System.out.println("updateStockActual (General): " + producto.getNombre() + " = " + producto.getStockactual() + " + " + cantidad);
      producto.setStockactual(producto.getStockactual() + cantidad);
      try {
         edit(producto);
      } catch (NonexistentEntityException ex) {
         Logger.getLogger(ProductoJpaController.class.getName()).log(Level.SEVERE, null, ex);
      } catch (Exception ex) {
         Logger.getLogger(ProductoJpaController.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

   private void initStockGral(Producto p) throws MessageException {
      if (p == null) {
         return;
      }

      JDStockGral jdStockGral = new JDStockGral(abm);
      jdStockGral.setLocationRelativeTo(abm);
      try {
         UTIL.getDefaultTableModel(
                 jdStockGral.getjTable1(),
                 new String[]{"Sucursal", "Stock", "Último movi.", "Usuario"},
                 new int[]{50, 20, 70, 40});
      } catch (Exception ex) {
         Logger.getLogger(ProductoJpaController.class.getName()).log(Level.SEVERE, null, ex);
      }
      List<Stock> stockList = new StockJpaController().findStocksByProducto(p.getId());
      DefaultTableModel dtm = jdStockGral.getDtm();
      for (Stock stock : stockList) {
         dtm.addRow(new Object[]{
                    stock.getSucursal(),
                    stock.getStockSucu(),
                    UTIL.DATE_FORMAT.format(stock.getFechaCarga()) + " / " + UTIL.TIME_FORMAT.format(stock.getFechaCarga()),
                    stock.getUsuario()
                 });
      }
      jdStockGral.setVisible(true);

   }

   private void eliminarProducto() throws MessageException, NonexistentEntityException {
      // <editor-fold defaultstate="collapsed" desc="checking Permiso">
      try {
         UsuarioJpaController.checkPermisos(PermisosJpaController.PermisoDe.ABM_PRODUCTOS);
      } catch (MessageException ex) {
         javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage());
         return;
      }// </editor-fold>

      if (EL_OBJECT == null) {
         throw new MessageException("No hay " + CLASS_NAME + " seleccionado");
      }
      destroy(EL_OBJECT.getId());
      EL_OBJECT = null;
      cargarDTMContenedor(null);
   }

   private Producto getSelectedFromContenedor() {
      Integer selectedRow = contenedor.getjTable1().getSelectedRow();
      if (selectedRow > -1) {
         return (Producto) DAO.getEntityManager().find(Producto.class,
                 Integer.valueOf(
                 (contenedor.getDTM().getValueAt(selectedRow, 0)).toString()));
      } else {
         return null;
      }
   }

   private void doReportProductList() throws Exception {
      if (getProductoCount() == 0) {
         throw new MessageException("No existen " + CLASS_NAME + "s para imprimir un listado.");
      }
      Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_ProductosList.jasper", "Listado Productos");
      r.addCurrent_User();
      r.addParameter("SUBREPORT_DIR", Reportes.FOLDER_REPORTES);
      r.printReport(true);
   }

   public void initMovimientoProducto(JFrame frame, boolean modal) {
      panelito = new PanelBuscadorMovimientosPro();
      UTIL.loadComboBox(panelito.getCbMarcas(), new MarcaJpaController().findMarcaEntities(), "<Elegir>");
      UTIL.loadComboBox(panelito.getCbRubros(), new RubroJpaController().findRubros(), "<Elegir>");
      UTIL.loadComboBox(panelito.getCbSubRubros(), new RubroJpaController().findRubros(), "<Elegir>");
      UTIL.loadComboBox(panelito.getCbSucursales(), new SucursalJpaController().findSucursalEntities(), "<Elegir>");
      buscador = new JDBuscador(frame, modal, panelito, "Movimientos de productos");
      buscador.agrandar(200, 0);
      UTIL.getDefaultTableModel(
              buscador.getjTable1(),
              new String[]{"RAZÓN", "CÓDIGO", "NOMBRE", "MARCA", "CANTIDAD", "LETRA", "NÚMERO", "INTERNO", "FECHA (HORA)", "RUBRO/S.RUB", "SUCURSAL"},
              new int[]{25, 50, 150, 30, 20, 10, 40, 20, 60, 30, 30});
      buscador.getbBuscar().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            try {
               String query = armarQueryMovimientosProductos();
               cargarTablaMovimientosProductos(query);
            } catch (Exception ex) {
               JOptionPane.showMessageDialog(buscador, ex.getMessage());
            }

         }
      });

      buscador.getbImprimir().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            buscador.getbImprimir().setEnabled(false);
            try {
               String query = armarQueryMovimientosProductos();
               cargarTablaMovimientosProductos(query);
               Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_MovimientosProductos.jasper", "Movimientos Productos");
               r.addCurrent_User();
               r.addParameter("SUBREPORT_DIR", Reportes.FOLDER_REPORTES);
               r.addParameter("QUERY", query);
               r.printReport(true);
            } catch (MessageException ex) {
               JOptionPane.showMessageDialog(buscador, ex.getMessage());
            } catch (Exception ex) {
               JOptionPane.showMessageDialog(buscador, ex.getMessage());
            } finally {
               buscador.getbImprimir().setEnabled(true);
            }
         }
      });
      buscador.getbLimpiar().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            UTIL.limpiarDtm(buscador.getjTable1());
         }
      });
      buscador.setVisible(true);

   }

   private void cargarTablaMovimientosProductos(String query) {
      UTIL.limpiarDtm(buscador.getjTable1());
      List l = DAO.getEntityManager().createNativeQuery(query).getResultList();
      for (Object object : l) {
         Object[] o = ((Vector) object).toArray();
         if ((Boolean) o[9]) { // es decir.. si factura.anulada == true
            o[0] = "ANULADA " + String.valueOf(o[0]);
            o[4] = -Integer.valueOf(o[4].toString());
         }
         UTIL.getDtm(buscador.getjTable1()).addRow(new Object[]{
                    o[0],
                    o[1], //código
                    o[2],
                    o[3], //marca
                    o[4], //cantidad
                    o[6],
                    UTIL.AGREGAR_CEROS(((Object) o[7]).toString(), 12),
                    o[8],
                    UTIL.DATE_FORMAT.format(((Date) o[5])) + "(" + UTIL.TIME_FORMAT.format((Date) o[5]) + ")",
                    o[10],
                    o[11]
                 });
      }
   }

   private String armarQueryMovimientosProductos() {
      String query = "SELECT * FROM ( ";
      String queryIngreso = "SELECT 'COMPRA'::character varying(10) AS razon, p.codigo, p.nombre, marca.nombre AS marca, d.cantidad, f.fechaalta, f.tipo, f.numero, f.movimiento, f.anulada, rubro.nombre AS rubro, sucursal.nombre as sucursal"
              + " FROM producto p"
              + " JOIN detalle_compra d ON p.id = d.producto"
              + " JOIN factura_compra f ON f.id = d.factura"
              + " JOIN marca ON p.marca = marca.id"
              + " JOIN rubro ON p.rubro = rubro.idrubro"
              + " JOIN sucursal ON f.sucursal = sucursal.id"
              + " WHERE p.codigo IS NOT NULL";
      String queryEgreso = "SELECT 'VENTA'::character varying(10) AS razon, p.codigo, p.nombre, marca.nombre AS marca, - d.cantidad, f.fechaalta, f.tipo, f.numero, f.movimiento_interno AS movimiento, f.anulada, rubro.nombre AS rubro, sucursal.nombre as sucursal"
              + " FROM producto p"
              + " JOIN detalle_venta d ON p.id = d.producto"
              + " JOIN factura_venta f ON f.id = d.factura"
              + " JOIN marca ON p.marca = marca.id"
              + " JOIN rubro ON p.rubro = rubro.idrubro"
              + " JOIN sucursal ON f.sucursal = sucursal.id"
              + " WHERE p.codigo IS NOT NULL ";
      int ingreso_egreso = panelito.getCbIngresoEgreso().getSelectedIndex();
      if (ingreso_egreso == 0) {
         queryIngreso = concatQuery(queryIngreso);
         queryEgreso = concatQuery(queryEgreso);
         query += queryIngreso + " UNION (" + queryEgreso + ")";
      } else if (ingreso_egreso == 1) {
         queryIngreso = concatQuery(queryIngreso);
         query += queryIngreso;
      } else {
         queryEgreso = concatQuery(queryEgreso);
         query += queryEgreso;
      }
      query += ") as t ";
      query += " ORDER BY fechaalta";
      System.out.println(query);
      return query;
   }

   private String concatQuery(String query) {
      if (panelito.getTfCodigo().getText().trim().length() > 0) {
         query += " AND p.codigo = '" + panelito.getTfCodigo().getText().trim() + "'";
      }
      if (panelito.getTfNombre().getText().trim().length() > 0) {
         query += " AND p.nombre ILIKE '" + panelito.getTfNombre().getText().trim() + "%'";
      }
      if (panelito.getCbMarcas().getSelectedIndex() > 0) {
         query += " AND p.marca = " + ((Marca) panelito.getCbMarcas().getSelectedItem()).getId();
      }
      if (panelito.getCbRubros().getSelectedIndex() > 0) {
         query += " AND p.rubro = " + ((Rubro) panelito.getCbRubros().getSelectedItem()).getIdrubro();
      }
      if (panelito.getCbSubRubros().getSelectedIndex() > 0) {
         query += " AND p.subrubro = " + ((Rubro) panelito.getCbSubRubros().getSelectedItem()).getIdrubro();
      }
      if (panelito.getCbSucursales().getSelectedIndex() > 0) {
         query += " AND sucursal.id = " + ((Sucursal) panelito.getCbSucursales().getSelectedItem()).getId();
      }
      if (panelito.getDcDesde().getDate() != null) {
         query += " AND f.fechaalta >= '" + panelito.getDcDesde().getDate() + "'";
      }
      if (panelito.getDcHasta().getDate() != null) {
         query += " AND f.fechaalta <= '" + panelito.getDcHasta().getDate() + "'";
      }
      return query;
   }

   public List<Producto> findProductoToCombo() {
      try {
         return (List<Producto>) DAO.getNativeQueryResultList("SELECT p.id, p.codigo, p.nombre, p.remunerativo FROM Producto p ORDER BY p.nombre, p.codigo", "ProductoToBuscador");
      } catch (DatabaseErrorException ex) {
         Logger.getLogger(ProductoJpaController.class.getName()).log(Level.SEVERE, null, ex);
      }
      return null;
   }

   public void initListadoProducto(JFrame owner) {
      panelProductoListados = new PanelProductoListados();
//      panelProductoListados.getbAcomodar().addActionListener(new ActionListener() {
//
//         @Override
//         public void actionPerformed(ActionEvent e) {
//            if(buscador.getjTable1().getSelectedRow() > -1) {
//               cambiarHora((Producto)UTIL.getSelectedValue(buscador.getjTable1(), 0));
//            }
//         }
//      });
      buscador = new JDBuscador(owner, false, panelProductoListados, "Productos - Listados");
      UTIL.loadComboBox(panelProductoListados.getCbMarcas(), new MarcaJpaController().findMarcaEntities(), "<Todas>");
      UTIL.loadComboBox(panelProductoListados.getCbRubros(), new RubroJpaController().findRubros(), "<Todos>");
      UTIL.loadComboBox(panelProductoListados.getCbSubRubros(), new RubroJpaController().findRubros(), "<Todos>");
      UTIL.getDefaultTableModel(
              buscador.getjTable1(),
              new String[]{"NOMBRE", "CÓDIGO", "MARCA", "RUBRO", "SUB RUBRO"},
              new int[]{250,           60,          60,     50,          50});
      buscador.getbBuscar().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            try {
               armarQueryProductosListado(false);
            } catch (Exception ex) {
               JOptionPane.showMessageDialog(buscador, ex.getMessage());
            }
         }
      });
      buscador.getbImprimir().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            try {
               armarQueryProductosListado(true);
            } catch (Exception ex) {
               ex.printStackTrace();
               JOptionPane.showMessageDialog(buscador, ex.getMessage());
            }
         }
      });
      buscador.setVisible(true);
   }

//   private void cambiarHora(Producto producto) {
//      JDcambiarHora jj = new JDcambiarHora(null, true);
//      jj.p = producto;
//      jj.getTfHora().setText(UTIL.TIME_FORMAT.format(producto.getFechaAlta()).substring(0, 2));
//      jj.getTfMin().setText(UTIL.TIME_FORMAT.format(producto.getFechaAlta()).substring(3, 5));
//      jj.getTfSec().setText(UTIL.TIME_FORMAT.format(producto.getFechaAlta()).substring(6));
//      jj.setLocationRelativeTo(buscador);
//      jj.setVisible(true);
//      try {
//         armarQueryProductosListado(false);
//      } catch (DatabaseErrorException ex) {
//         Logger.getLogger(ProductoJpaController.class.getName()).log(Level.SEVERE, null, ex);
//      } catch (Exception ex) {
//         Logger.getLogger(ProductoJpaController.class.getName()).log(Level.SEVERE, null, ex);
//      }
//   }
   
   private void armarQueryProductosListado(boolean imprimirReport) throws DatabaseErrorException, Exception {
      String query = "SELECT p.*, marca.nombre as marca_nombre, r.nombre as rubro_nombre, sr.nombre as subrubro_nombre "
              + " FROM producto p"
              + " JOIN marca ON p.marca = marca.id"
              + " JOIN rubro r ON p.rubro = r.idrubro"
              + " LEFT JOIN rubro sr ON p.subrubro = sr.idrubro"
              + " WHERE p.id IS NOT NULL";

      if (panelProductoListados.getCbMarcas().getSelectedIndex() > 0) {
         query += " AND p.marca = " + ((Marca) panelProductoListados.getCbMarcas().getSelectedItem()).getId();
      }

      if (panelProductoListados.getCbRubros().getSelectedIndex() > 0) {
         query += " AND p.rubro = " + ((Rubro) panelProductoListados.getCbRubros().getSelectedItem()).getIdrubro();
      }

      if (panelProductoListados.getCbSubRubros().getSelectedIndex() > 0) {
         query += " AND p.subrubro = " + ((Rubro) panelProductoListados.getCbSubRubros().getSelectedItem()).getIdrubro();
      }

      if (panelProductoListados.getCheckOrdenarPorFechaCreacion().isSelected()) {
         query += " ORDER BY p.fecha_alta ASC";
      } else {
         query += " ORDER BY p.nombre";
      }
      cargarTablaProductosListado((List<Producto>) DAO.getNativeQueryResultList(query, Producto.class));
      if (imprimirReport) {
         doReportProductosListado(query);
      }
   }

   private void cargarTablaProductosListado(List<Producto> listado) {
      UTIL.limpiarDtm(buscador.getjTable1());
      for (Producto producto : listado) {
         buscador.getDtm().addRow(new Object[]{
                    producto,
                    producto.getCodigo(),
                    producto.getMarca(),
                    producto.getRubro(),
                    (producto.getSubrubro() != null ? producto.getSubrubro() : null)
                 });
      }
   }

   private void doReportProductosListado(String query) throws Exception {
//      if (panelProductoListados.getCheckParaJurado().isSelected()) {
//         Reportes r = new Reportes("evento_parajurado.jasper", "Lista para jurado");
//         r.addCurrent_User();
//         r.addParameter("QUERY", query);
//         r.addParameter("SUBREPORT_DIR", Reportes.FOLDER_REPORTES);
//         r.viewReport();
//      } else {
         Reportes r = new Reportes("JGestion_ProductosListado.jasper", "Productos - Listado");
         r.addCurrent_User();
         r.addParameter("QUERY", query);
         r.addParameter("SUBREPORT_DIR", Reportes.FOLDER_REPORTES);
         r.viewReport();
//      }
   }
}
