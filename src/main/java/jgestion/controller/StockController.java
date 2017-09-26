package jgestion.controller;

import jgestion.entity.DetalleCompra;
import jgestion.entity.DetalleVenta;
import jgestion.entity.FacturaCompra;
import jgestion.entity.FacturaVenta;
import jgestion.entity.Producto;
import jgestion.entity.Stock;
import jgestion.entity.Sucursal;
import java.util.List;
import javax.persistence.NoResultException;
import jgestion.entity.RemitoCompra;
import jgestion.entity.RemitoCompraDetalle;
import jgestion.jpa.controller.ProductoJpaController;
import jgestion.jpa.controller.StockJpaController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author FiruzzZ
 */
public class StockController {

    public static final String CLASS_NAME = Stock.class.getSimpleName();
    public static final Logger LOG = LogManager.getLogger();
    private final StockJpaController jpaController = new StockJpaController();

    public StockController() {
    }

    public List<Stock> findStocksByProducto(int productoID) {
        return jpaController.findAll(jpaController.getSelectFrom() + " WHERE o.producto.id=" + productoID);
    }

    public Integer getStockGlobal(int productoID) {
        return ((Number) jpaController.findAttribute("SELECT sum(o.stockSucu) FROM " + jpaController.getEntityClass().getSimpleName() + " o"
                + " WHERE o.producto.id=" + productoID)).intValue();
    }

    void updateStock(FacturaCompra facturaCompra) {
        for (DetalleCompra item : facturaCompra.getDetalleCompraList()) {
            Producto producto = item.getProducto();
            modificarStockBySucursal(producto, facturaCompra.getSucursal(), item.getCantidad());

        }
    }

    void updateStock(RemitoCompra remito) {
        for (RemitoCompraDetalle item : remito.getDetalle()) {
            Producto producto = item.getProducto();
            modificarStockBySucursal(producto, remito.getSucursal(), item.getCantidad());
        }
    }

    void updateStock(FacturaVenta facturaVenta) throws Exception {
        for (DetalleVenta item : facturaVenta.getDetallesVentaList()) {
            Producto producto = item.getProducto();
            modificarStockBySucursal(producto, facturaVenta.getSucursal(), -item.getCantidad());
        }
    }

    public Integer findStockActual(Producto producto, Sucursal sucursal) {
        return (Integer) jpaController.findAttribute("SELECT o.stockSucu FROM " + jpaController.getAlias()
                + " WHERE o.producto.id=" + producto.getId()
                + " AND o.sucursal.id=" + sucursal.getId());
    }

    public void modificarStockBySucursal(Producto producto, Sucursal sucursal, int cantidad) {
        Stock stock;
        stock = jpaController.findBy(producto, sucursal);
        if (stock == null) {
            stock = new Stock();
            stock.setProducto(producto);
            stock.setSucursal(sucursal);
            stock.setStockSucu(cantidad);
            stock.setFechaCarga(jpaController.getServerDate());
            stock.setUsuario(UsuarioController.getCurrentUser());
            jpaController.persist(stock);
        } else {
            stock.setStockSucu(stock.getStockSucu() + cantidad);
            stock.setUsuario(UsuarioController.getCurrentUser());
            stock.setFechaCarga(jpaController.getServerDate());
            jpaController.merge(stock);
        }
        producto.setStockactual(producto.getStockactual() + cantidad);
        new ProductoJpaController().merge(producto);
    }
}
