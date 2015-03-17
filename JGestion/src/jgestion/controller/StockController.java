package jgestion.controller;

import jgestion.entity.DetalleCompra;
import jgestion.entity.DetalleVenta;
import jgestion.entity.FacturaCompra;
import jgestion.entity.FacturaVenta;
import jgestion.entity.Producto;
import jgestion.entity.Stock;
import jgestion.entity.Sucursal;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import jgestion.jpa.controller.JGestionJpaImpl;
import org.apache.log4j.Logger;

/**
 *
 * @author FiruzzZ
 */
public class StockController extends JGestionJpaImpl<Stock, Integer> {

    public static final String CLASS_NAME = Stock.class.getSimpleName();
    public static final Logger LOG = Logger.getLogger(StockController.class.getName());

    public StockController() {
    }

    public List<Stock> findStocksByProducto(int productoID) {
        return findAll(getSelectFrom() + " WHERE o.producto.id=" + productoID);
    }

    public Integer getStockGlobal(int productoID) {
        return ((Number) findAttribute("SELECT sum(o.stockSucu) FROM " + getEntityClass().getSimpleName() + " o"
                + " WHERE o.producto.id=" + productoID)).intValue();
    }

    /**
     * Actualiza el Stock, en base al DetallesCompra de la FacturaCompra y también actualiza
     * Producto.stockActual
     *
     * @param facturaCompra ...
     */
    void updateStock(FacturaCompra facturaCompra) {
        System.out.println("updateStock::facturaCompra.id=" + facturaCompra.getId());
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            List<DetalleCompra> listaDetalleCompra = new DetalleCompraJpaController().findByFactura(facturaCompra);
            System.out.println("Cantidad de Items: " + listaDetalleCompra.size());
            Stock stock;
            for (DetalleCompra detalleCompra : listaDetalleCompra) {
                Producto producto = detalleCompra.getProducto();
                try {
                    // checks la pre-existencia del Producto EN ESTA Sucursal
                    stock = findStock(producto, facturaCompra.getSucursal());
                } catch (NoResultException ex) {
                    // por lo visto no existe
                    stock = new Stock();
                    stock.setProducto(producto);
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
                LOG.debug(producto);
                producto.setStockactual(producto.getStockactual() + detalleCompra.getCantidad());
                em.merge(producto);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            LOG.error(ex.getMessage(), ex);
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

    public Stock findStock(Producto producto, Sucursal sucursal) {
        return findByQuery(getSelectFrom()
                + " WHERE o.producto.id=" + producto.getId()
                + " AND o.sucursal.id=" + sucursal.getId());
    }

    public void modificarStockBySucursal(Producto producto, Sucursal sucursal, int cantidad) {
        Stock stock = findStock(producto, sucursal);
        stock.setStockSucu(stock.getStockSucu() + cantidad);
        stock.setFechaCarga(getServerDate());
        merge(stock);
    }
}
