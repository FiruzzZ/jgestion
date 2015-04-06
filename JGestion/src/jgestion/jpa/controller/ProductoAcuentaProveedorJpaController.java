package jgestion.jpa.controller;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import jgestion.entity.Producto;
import jgestion.entity.ProductoAcuentaProveedor;
import jgestion.entity.ProductoAcuentaProveedor_;
import jgestion.entity.Proveedor;
import jgestion.entity.RemitoCompra;
import jgestion.entity.RemitoCompraDetalle;

/**
 *
 * @author FiruzzZ
 */
public class ProductoAcuentaProveedorJpaController extends JGestionJpaImpl<ProductoAcuentaProveedor, Integer> {

    public ProductoAcuentaProveedorJpaController() {
    }

    public void updateCuenta(RemitoCompra remito) {
        for (RemitoCompraDetalle detalle : remito.getDetalle()) {
            if (!detalle.isBonificado()) {
                ProductoAcuentaProveedor pap = findBy(remito.getProveedor(), detalle.getProducto());
                if (pap == null) {
                    pap = new ProductoAcuentaProveedor();
                    pap.setCantidad(detalle.getCantidad());
                    pap.setProducto(detalle.getProducto());
                    pap.setProveedor(remito.getProveedor());
                    persist(pap);
                } else {
                    pap.setCantidad(pap.getCantidad() + detalle.getCantidad());
                    merge(pap);
                }
            }
        }
    }

    private ProductoAcuentaProveedor findBy(Proveedor proveedor, Producto producto) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<ProductoAcuentaProveedor> cq = cb.createQuery(getEntityClass());
        Root<ProductoAcuentaProveedor> from = cq.from(getEntityClass());
        cq.where(cb.equal(from.get(ProductoAcuentaProveedor_.producto), producto),
                cb.equal(from.get(ProductoAcuentaProveedor_.proveedor), proveedor)
        );
        try {
            return getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}
