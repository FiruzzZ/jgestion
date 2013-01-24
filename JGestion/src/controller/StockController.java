package controller;

import controller.exceptions.*;
import entity.DetalleCompra;
import entity.DetalleVenta;
import entity.FacturaCompra;
import entity.FacturaVenta;
import entity.Producto;
import entity.Stock;
import entity.Sucursal;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import org.apache.log4j.Logger;

/**
 *
 * @author FiruzzZ
 */
public class StockController {

    public static final String CLASS_NAME = Stock.class.getSimpleName();

    public StockController() {
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
     * Actualiza el Stock, en base al DetallesCompra de la FacturaCompra y
     * también actualiza Producto.stockActual
     *
     * @param facturaCompra ...
     */
    void updateStock(FacturaCompra facturaCompra) {
        System.out.println("updateStock::facturaCompra.id=" + facturaCompra.getId());
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
//         facturaCompra = DAO.getEntityManager().find(FacturaCompra.class, facturaCompra.getId());
            List<DetalleCompra> listaDetalleCompra = new DetalleCompraJpaController().findByFactura(facturaCompra);
            System.out.println("Cantidad de Items: " + listaDetalleCompra.size());
            ProductoController productoCtrl = new ProductoController();
            Stock stock;
            for (DetalleCompra detalleCompra : listaDetalleCompra) {
                try {
                    // checks la pre-existencia del Producto EN ESTA Sucursal
                    stock = findStock(detalleCompra.getProducto(), facturaCompra.getSucursal());
                } catch (NoResultException ex) {
                    // por lo visto no existe
                    stock = new Stock();
                    stock.setProducto(detalleCompra.getProducto());
                    stock.setSucursal(facturaCompra.getSucursal());
                    stock.setUsuario(facturaCompra.getUsuario());
                }
                // sets la fecha de carga de la factura al stock para evitar desfasajes de tiempo y ...!!!
                stock.setFechaCarga(facturaCompra.getFechaalta());
                // stockActual + stock del nuevo facturaCompra ->
                stock.setStockSucu(stock.getStockSucu() + detalleCompra.getCantidad());

                if (stock.getId() == null) {
                    em.persist(stock);
                } else {
                    em.merge(stock);
                }
                productoCtrl.updateStockActual(stock.getProducto(), detalleCompra.getCantidad());
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            Logger.getLogger(StockController.class).error(ex.getMessage(), ex);
        } finally {
            if (em != null) {
                em.close();
            }
        }

    }

    /**
     * Actualiza el Stock, en base al DetalleVenta de la FacturaVenta
     *
     * @param facturaVenta del cual se obtiene la List DetallesVenta
     */
    void updateStock(FacturaVenta facturaVenta) throws Exception {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            List<DetalleVenta> detallesVentaList = new DetalleVentaJpaController().findByFactura(facturaVenta.getId());
            ProductoController productoCtrl = new ProductoController();
            Stock stock;
            for (DetalleVenta detalleVenta : detallesVentaList) {
                try {
                    // checks la pre-existencia del Producto en la Sucursal
                    stock = findStock(detalleVenta.getProducto(), facturaVenta.getSucursal());
                } catch (NoResultException ex) {
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
                // stock de Sucursal - stock vendido ->
                stock.setStockSucu(stock.getStockSucu() - detalleVenta.getCantidad());
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
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

    }

    /**
     * Busca el Stock del Producto de esa Sucursal.
     *
     * @param Producto
     * @param Sucursal
     * @return a entity Stock
     * @exception Lanza un NoResultException si no existe el Producto en la
     * Sucursal.
     */
    private Stock findStock(Producto producto, Sucursal sucursal) {
        return (Stock) DAO.getEntityManager().createNativeQuery("SELECT * FROM " + CLASS_NAME + " o WHERE "
                + " o.producto = " + producto.getId() + " AND o.sucursal = " + sucursal.getId(), Stock.class).getSingleResult();
    }

    public void modificarStockBySucursal(Producto producto, Sucursal sucursal, int cantidad) {
        Stock stock = findStock(producto, sucursal);
        stock.setStockSucu(stock.getStockSucu() + cantidad);
        stock.setFechaCarga(new Date());
        DAO.doMerge(stock);
    }
}
