package controller;

import controller.exceptions.*;
import entity.DetallesCompra;
import entity.DetallesVenta;
import entity.FacturaCompra;
import entity.FacturaVenta;
import entity.Producto;
import entity.Stock;
import entity.Sucursal;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;

/**
 *
 * @author FiruzzZ
 */
public class StockJpaController {

   public static final String CLASS_NAME = "Stock";

   public StockJpaController() {
   }

   public EntityManager getEntityManager() {
      return DAO.getEntityManager();
   }

   // <editor-fold defaultstate="collapsed" desc="create, edit, destroy, listings...">
   public void create(Stock stock) throws Exception {
      DAO.create(stock);
   }

   public void edit(Stock stock) throws NonexistentEntityException, Exception {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         stock = em.merge(stock);
         em.getTransaction().commit();
      } catch (Exception ex) {
         String msg = ex.getLocalizedMessage();
         if (msg == null || msg.length() == 0) {
            Integer id = stock.getId();
            if (findStock(id) == null) {
               throw new NonexistentEntityException("The stock with id " + id + " no longer exists.");
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
         Stock stock;
         try {
            stock = em.getReference(Stock.class, id);
            stock.getId();
         } catch (EntityNotFoundException enfe) {
            throw new NonexistentEntityException("The stock with id " + id + " no longer exists.", enfe);
         }
         em.remove(stock);
         em.getTransaction().commit();
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public List<Stock> findStockEntities() {
      return findStockEntities(true, -1, -1);
   }

   public List<Stock> findStockEntities(int maxResults, int firstResult) {
      return findStockEntities(false, maxResults, firstResult);
   }

   private List<Stock> findStockEntities(boolean all, int maxResults, int firstResult) {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select object(o) from Stock as o");
         if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
         }
         return q.getResultList();
      } finally {
         em.close();
      }
   }

   public Stock findStock(Integer id) {
      EntityManager em = getEntityManager();
      try {
         return em.find(Stock.class, id);
      } finally {
         em.close();
      }
   }

   public int getStockCount() {
      EntityManager em = getEntityManager();
      try {
         return ((Long) em.createQuery("select count(o) from Stock as o").getSingleResult()).intValue();
      } finally {
         em.close();
      }
   }

   // </editor-fold>
   public List<Stock> findStocksByProducto(int productiID) {
      return DAO.getEntityManager().createNamedQuery("Stock.findByProducto").setParameter("producto", productiID).getResultList();
   }

   public Integer getStockGlobal(int productoID) {
      int cantidad = 0;
      List<Stock> lista = findStocksByProducto(productoID);
      for (Stock stock : lista) {
         cantidad += stock.getStockSucu();
      }
      return cantidad;
   }

   /**
    * Actualiza el Stock, en base al DetallesCompra de la FacturaCompra
    * y también actualiza Producto.stockActual
    * @param facturaCompra del cual se obtiene la List DetallesCompra para
    * actualizar los stock
    */
   void updateStock(FacturaCompra facturaCompra) {
      System.out.println("updateStock.. facturaCompra");
      EntityManager em = getEntityManager();
      try {
         em.getTransaction().begin();
         FacturaCompra newFacturaCompra = em.find(FacturaCompra.class, facturaCompra.getId());
         List<DetallesCompra> listaDetallesCompra;
         listaDetallesCompra = new DetallesCompraJpaController().findDetallesCompraEntitiesFromFactura(newFacturaCompra.getId());
         ProductoJpaController productoCtrl = new ProductoJpaController();
         Stock stock;
         for (DetallesCompra detallesCompra : listaDetallesCompra) {
            try {
               // checks la pre-existencia del Producto EN ESTA Sucursal
               stock = findStock(detallesCompra.getProducto(), newFacturaCompra.getSucursal());
            } catch (NoResultException ex) {
               // por lo visto no existe
               stock = new Stock();
               stock.setProducto(detallesCompra.getProducto());
               stock.setSucursal(newFacturaCompra.getSucursal());
               stock.setUsuario(newFacturaCompra.getUsuario());
            }
            // sets la fecha de carga de la factura al stock para evitar desfasajes de tiempo y ...!!!
            stock.setFechaCarga(newFacturaCompra.getFechaalta());
            stock.setHoraCarga(newFacturaCompra.getHoraalta());
            // stockActual + stock del nuevo facturaCompra ->
            stock.setStockSucu(stock.getStockSucu() + detallesCompra.getCantidad());

            if (stock.getId() == null) {
               em.persist(stock);
            } else {
               em.merge(stock);
            }
            productoCtrl.updateStockActual(stock.getProducto(),  detallesCompra.getCantidad());
         }
         em.getTransaction().commit();
      } catch (Exception ex) {
         em.getTransaction().rollback();
         ex.printStackTrace();
      } finally {
         if (em != null) {
            em.close();
         }
      }

   }

   /**
    * Actualiza el Stock, en base al DetalleVenta de la FacturaVenta
    * @param facturaVenta del cual se obtiene la List DetallesVenta
    */
   void updateStock(FacturaVenta facturaVenta) throws Exception {
      System.out.println("updating Stock: FROM FacturaVenta.id=" + facturaVenta.getId());
      System.out.println("-------------------------------------------");
      EntityManager em = getEntityManager();
      try {
         em.clear();
         em.getTransaction().begin();
         List<DetallesVenta> detallesVentaList = new DetallesVentaJpaController().findDetallesVentaFromFactura(facturaVenta.getId());
         ProductoJpaController productoCtrl = new ProductoJpaController();
         Stock stock;
         for (DetallesVenta detalleVenta : detallesVentaList) {
            try {
               // checks la pre-existencia del Producto en la Sucursal
               stock = findStock(detalleVenta.getProducto(), facturaVenta.getSucursal());
               System.out.println("-->Si existía");
            } catch (NoResultException ex) {
               System.out.println("-->No existía");
               // quiere decir que NO había ningún registro de compra/existencia
               // de stock de ESE Producto && ESA Sucursal
               stock = new Stock();
               stock.setProducto(detalleVenta.getProducto());
               stock.setSucursal(facturaVenta.getSucursal());
               stock.setStockSucu(0);
            }
            // sets la fecha de carga de la factura al stock para evitar desfasajes de tiempo y ****!!
            stock.setUsuario(facturaVenta.getUsuario());
            stock.setFechaCarga(facturaVenta.getFechaalta());
            stock.setHoraCarga(facturaVenta.getFechaalta());
            // stock de Sucursal - stock vendido ->
            stock.setStockSucu(stock.getStockSucu() - detalleVenta.getCantidad());
            System.out.println("--->Stock: id=" + stock.getId() + ", Producto=" + stock.getProducto() + ", Sucu=" + stock.getSucursal() + ", cant=" + stock.getStockSucu());
            if (stock.getId() == null) {
               em.persist(stock);
            } else {
               em.merge(stock);
            }
            // resta stock vendido al stockGlobal
            productoCtrl.updateStockActual(stock.getProducto(), (-detalleVenta.getCantidad()));
         }
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

   /**
    * Busca el Stock del Producto de esa Sucursal.
    * @param Producto
    * @param Sucursal
    * @return a entity Stock
    * @exception Lanza un NoResultException si no existe el Producto en la Sucursal.
    */
   private Stock findStock(Producto producto, Sucursal sucursal) {
      return (Stock) DAO.getEntityManager()
              .createNativeQuery("SELECT * FROM " + CLASS_NAME + " o WHERE "
              + " o.producto = " + producto.getId() + " AND o.sucursal = " + sucursal.getId(), Stock.class)
              .getSingleResult();
   }

}
