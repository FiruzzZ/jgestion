package controller;

import controller.exceptions.MessageException;
import controller.exceptions.NonexistentEntityException;
import controller.exceptions.PreexistingEntityException;
import entity.Producto;
import entity.ProductosWeb;
import gui.JDCatalogoWEB;
import gui.JDOfertas;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.swing.JFrame;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Administrador
 */
public class ProductosWebJpaController {

   public static final String CLASS_NAME = ProductosWeb.class.getSimpleName();
   private JDCatalogoWEB jDCatalogoWEB;
   private static String URL_DE_ACTUALIZACION_DE_CATALOGO_WEB = "http://10.0.0.10/sistema/chequear";
   private JDOfertas jdOfertas;

   public ProductosWebJpaController() {
   }

   // <editor-fold defaultstate="collapsed" desc="CRUD">
   public EntityManager getEntityManager() {
      return DAO.getEntityManager();
   }

   public void create(ProductosWeb productosWeb) throws PreexistingEntityException, Exception {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         em.persist(productosWeb);
         em.getTransaction().commit();
      } catch (Exception ex) {
         if (findProductosWeb(productosWeb.getId()) != null) {
            throw new PreexistingEntityException("ProductosWeb " + productosWeb + " already exists.", ex);
         }
         throw ex;
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public void edit(ProductosWeb productosWeb) throws NonexistentEntityException, Exception {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         productosWeb = em.merge(productosWeb);
         em.getTransaction().commit();
      } catch (Exception ex) {
         String msg = ex.getLocalizedMessage();
         if (msg == null || msg.length() == 0) {
            Integer id = productosWeb.getId();
            if (findProductosWeb(id) == null) {
               throw new NonexistentEntityException("The productosWeb with id " + id + " no longer exists.");
            }
         }
         throw ex;
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public void destroy(Integer id) throws NonexistentEntityException {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         ProductosWeb productosWeb;
         try {
            productosWeb = em.getReference(ProductosWeb.class, id);
            productosWeb.getId();
         } catch (EntityNotFoundException enfe) {
            throw new NonexistentEntityException("The productosWeb with id " + id + " no longer exists.", enfe);
         }
         em.remove(productosWeb);
         em.getTransaction().commit();
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public List<ProductosWeb> findProductosWebEntities() {
      return findProductosWebEntities(true, -1, -1);
   }

   public List<ProductosWeb> findProductosWebEntities(int maxResults, int firstResult) {
      return findProductosWebEntities(false, maxResults, firstResult);
   }

   private List<ProductosWeb> findProductosWebEntities(boolean all, int maxResults, int firstResult) {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select object(o) from ProductosWeb as o");
         if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
         }
         return q.getResultList();
      } finally {
         em.close();
      }
   }

   public ProductosWeb findProductosWeb(Integer id) {
      EntityManager em = getEntityManager();
      try {
         return em.find(ProductosWeb.class, id);
      } finally {
         em.close();
      }
   }

   public int getProductosWebCount() {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select count(o) from ProductosWeb as o");
         return ((Long) q.getSingleResult()).intValue();
      } finally {
         em.close();
      }
   }// </editor-fold>

   public void initCatalogoGUI(JFrame owner) throws MessageException {
      UsuarioJpaController.CHECK_PERMISO(PermisosJpaController.PermisoDe.ABM_CATALOGOWEB);
      if (new ListaPreciosJpaController().findListaPreciosParaCatalogo() != null) {
         try {
            jDCatalogoWEB = new JDCatalogoWEB(owner, true);
            jDCatalogoWEB.setLocationRelativeTo(owner);
            jDCatalogoWEB.setVisible(true);
         } catch (SQLException ex) {
            Logger.getLogger(ProductosWebJpaController.class.getName()).log(Level.ERROR, null, ex);
         }
      } else {
         throw new MessageException("Para poder Administrar el Catálogo Web, primero debe designar una Lista de Precios."
                 + "\nMenú -> Productos -> Lista Precios (eligir una) -> Modificar -> y tildar la opción \"para catálogo web\".");
      }
   }

   public void initOfertasUI(JFrame owner) throws MessageException {
      UsuarioJpaController.CHECK_PERMISO(PermisosJpaController.PermisoDe.ABM_OFERTASWEB);
      if (new ListaPreciosJpaController().findListaPreciosParaCatalogo() != null) {
         jdOfertas = new JDOfertas(owner, true);
         jdOfertas.setLocationRelativeTo(owner);
         jdOfertas.setVisible(true);
      } else {
         throw new MessageException("Para poder Administrar ofertas, primero debe designar una Lista de Precios."
                 + "\nMenú -> Productos -> Lista Precios (eligir una) -> Modificar -> y tildar la opción \"para catálogo web\".");
      }
   }

   public List<ProductosWeb> findProductosWebEnOferta() {
      return DAO.getEntityManager().createQuery("SELECT o FROM " + CLASS_NAME + " o WHERE o.destacado = true OR o.oferta = true").getResultList();
   }

   /**
    * Es lo mismo que {@link ProductosWebJpaController#findProductosWeb(java.lang.Integer) }
    * porque ProductosWeb.id == ProductosWeb.producto.id
    * @param producto
    * @return la instancia encontrada sinó null........
    */
   public ProductosWeb findProductosWebByProducto(Producto producto) {
      try {
         return (ProductosWeb) getEntityManager().createQuery("SELECT o FROM " + CLASS_NAME + " o WHERE o.producto.id=" + producto.getId()).getSingleResult();
      } catch (NoResultException ex) {
         return null;
      }
   }

   /**
    * Hace una llamada a la URL.
    * Ojo con el DEAD LOCK!
    * @return Mensaje resultado de la llamada a la page.
    */
   public static synchronized String UPDATE_CATALOGOWEB() {
      char[] data = new char[1023];
      int x;
      try {
         URL uRL = new URL(URL_DE_ACTUALIZACION_DE_CATALOGO_WEB);
         BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(uRL.openStream(), "UTF-8"));
         x = bufferedReader.read(data, 0, data.length);
         System.out.println("Bytes=" + x);
         System.out.println("Data=" + String.valueOf(data));
      } catch (MalformedURLException ex) {
         Logger.getLogger(ProductosWebJpaController.class.getName()).log(Level.ERROR, null, ex);
      } catch (IOException ex) {
         Logger.getLogger(ProductosWebJpaController.class.getName()).log(Level.ERROR, null, ex);
      }
      return String.valueOf(data);
   }

   /**
    * Settea el Producto en {@link ProductosWeb#BAJA}
    * @param productoID id del Producto que se va dar de baja
    * @throws NonexistentEntityException
    * @throws Exception
    */
   public void bajarFromCatalogo(int productoID) throws NonexistentEntityException, Exception {
      ProductosWeb productosWeb = findProductosWeb(productoID);

      productosWeb.setEstado(ProductosWeb.BAJA);
      productosWeb.setChequeado((short) 0);
      productosWeb.setDestacado(false);
      productosWeb.setOferta(false);
      productosWeb.setInicioOferta(null);
      productosWeb.setFinOferta(null);
      edit(productosWeb);
   }

   /**
    * Chequea si el Producto está vigente (es decir estado != 3).
    * @param selectedProducto
    * @return true si el Producto existe en la tabla y NO como BORRADO.. shalala!-
    */
   public boolean isInCatalogoWeb(Producto selectedProducto) {
      return isInCatalogo(selectedProducto.getId());
   }

   private boolean isInCatalogo(Integer id) {
      try {
         Object singleResult = getEntityManager().
                 createQuery("SELECT o.id FROM " + CLASS_NAME + " o WHERE o.estado <> 3 AND o.id=" + id).getSingleResult();
         Integer entityID = (Integer) singleResult;
         return true;
      } catch (NoResultException ex) {
         return false;
      }
   }
}
