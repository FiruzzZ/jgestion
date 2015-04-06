package jgestion.controller;

import java.math.BigDecimal;
import jgestion.entity.Cuenta;
import jgestion.entity.DetalleCajaMovimientos;
import jgestion.entity.CajaMovimientos;
import jgestion.controller.exceptions.MessageException;
import java.util.List;
import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import jgestion.entity.DetalleCajaMovimientos_;
import jgestion.entity.FacturaVenta;
import jgestion.entity.Producto;
import org.apache.log4j.Logger;

/**
 *
 * @author Administrador
 */
public class DetalleCajaMovimientosController {

    public final static String CLASS_NAME = DetalleCajaMovimientos.class.getSimpleName();
    /**
     * nº 1
     */
    public final static short FACTU_COMPRA = 1;
    /**
     * nº 2
     */
    public final static short FACTU_VENTA = 2;
    /**
     * nº 3
     */
    public final static short REMESA = 3;
    /**
     * nº 4
     */
    public final static short RECIBO = 4;
    /**
     * nº 5, son los movimientos monetarios entre Cajas (tipo de mov. interno). No son un INGRESO o
     * EGRESO real
     */
    public final static short MOVIMIENTO_CAJA = 5;
    /**
     * nº 6
     */
    public final static short ANULACION = 6;
    /**
     * nº 7
     */
    public final static short APERTURA_CAJA = 7;
    /**
     * nº 8
     */
    public final static short MOVIMIENTO_VARIOS = 8;
    /**
     * nº 9
     */
    public final static short MOVIMIENTO_INTERNO = 9;
    /**
     * nº 10
     */
    public final static short CHEQUE_TERCEROS = 10;
    /**
     * nº 11
     */
    public final static short CHEQUE_PROPIO = 11;
    private static Logger LOG = Logger.getLogger(DetalleCajaMovimientosController.class);

    public DetalleCajaMovimientosController() {
    }

    // <editor-fold defaultstate="collapsed" desc="CRUD...">
    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }

    public void create(DetalleCajaMovimientos detalleCajaMovimientos) throws MessageException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();

            //default MovimientoConcepto.EFECTIVO
            if (detalleCajaMovimientos.getCuenta() == null) {
                detalleCajaMovimientos.setCuenta(CuentaController.SIN_CLASIFICAR);
            }
            CajaMovimientos cajaMovimientos = detalleCajaMovimientos.getCajaMovimientos();
            if (cajaMovimientos != null) {
                cajaMovimientos = em.getReference(cajaMovimientos.getClass(), cajaMovimientos.getId());
                detalleCajaMovimientos.setCajaMovimientos(cajaMovimientos);
            }
            em.persist(detalleCajaMovimientos);
            em.getTransaction().commit();
        } catch (EntityNotFoundException ex) {
            throw new MessageException("La Caja en la que intenta hacer el movimiento no está mas disponible."
                    + "\nIntente cerrando y volviendo a abrir la ventana.");
        } catch (Exception ex) {
            throw new MessageException(ex.getMessage());
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    void merge(DetalleCajaMovimientos o) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        DetalleCajaMovimientos old = em.find(o.getClass(), o.getId());
        old = o;
        em.merge(old);
        em.getTransaction().commit();
        em.close();
    }

    public void remove(DetalleCajaMovimientos o) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        em.remove(o);
        em.getTransaction().commit();
        em.close();
    }

    public List<DetalleCajaMovimientos> findDetalleCajaMovimientosEntities() {
        return findDetalleCajaMovimientosEntities(true, -1, -1);
    }

    public List<DetalleCajaMovimientos> findDetalleCajaMovimientosEntities(int maxResults, int firstResult) {
        return findDetalleCajaMovimientosEntities(false, maxResults, firstResult);
    }

    private List<DetalleCajaMovimientos> findDetalleCajaMovimientosEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from DetalleCajaMovimientos as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public DetalleCajaMovimientos findDetalleCajaMovimientos(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(DetalleCajaMovimientos.class, id);
        } finally {
            em.close();
        }
    }

    public int getDetalleCajaMovimientosCount() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select count(o) from DetalleCajaMovimientos as o");
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }// </editor-fold>

    DetalleCajaMovimientos findDetalleCajaMovimientosByNumero(Integer numero, short tipoMovimiento) {
        return (DetalleCajaMovimientos) DAO.getEntityManager().createQuery("SELECT o FROM " + CLASS_NAME + " o"
                + " WHERE o.numero =" + numero + " AND o.tipo =" + tipoMovimiento).getSingleResult();
    }

    /**
     *
     * @param cajaMovimientosID
     * @return List de DetalleCajaMovimientos ordenado por DetalleCajaMovimientos.id
     */
    List<DetalleCajaMovimientos> getDetalleCajaMovimientosByCajaMovimiento(int cajaMovimientosID) {
        return (List<DetalleCajaMovimientos>) DAO.createQuery("SELECT o FROM " + CLASS_NAME + " o"
                + " WHERE o.cajaMovimientos.id =" + cajaMovimientosID
                + " ORDER BY o.id", true).getResultList();
    }

    List<Cuenta> findDetalleCajaMovimientosBy(Cuenta movimientoConcepto) {
        return getEntityManager().createQuery("SELECT o FROM " + CLASS_NAME + " o WHERE o.movimientoConcepto.id=" + movimientoConcepto.getId()).getResultList();
    }

    public DetalleCajaMovimientos find(Integer id) {
        return getEntityManager().find(DetalleCajaMovimientos.class, id);
    }

    /**
     * Busca en los {@link DetalleCajaMovimientos}, el comprobante por número y tipo; y retorna.
     *
     * @param numero
     * @param tipo
     * @return instance of {@code DetalleCajaMovimientos} if exist, else {@code null}
     */
    public DetalleCajaMovimientos findBy(long numero, short tipo) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<DetalleCajaMovimientos> query = cb.createQuery(DetalleCajaMovimientos.class);
        Root<DetalleCajaMovimientos> from = query.from(DetalleCajaMovimientos.class);
        query.select(from).
                where(cb.equal(from.get(DetalleCajaMovimientos_.numero), numero),
                        cb.equal(from.get(DetalleCajaMovimientos_.tipo), tipo));
        try {
            return getEntityManager().createQuery(query).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
/**
 * Recupera el importa de ventas (solo los generados por {@link FacturaVenta})generado por el Producto
 * @param cm
 * @param p
 * @return {@code BigDecimal.ZERO} si no existe ninguna venta
 */
    public BigDecimal getTotalVentas(CajaMovimientos cm, Producto p) {
        return (BigDecimal) getEntityManager().createQuery("SELECT COALESCE(SUM(p.precioVenta * detalleVenta.cantidad), 0)"
                + " FROM " + DetalleCajaMovimientos.class.getSimpleName() + " detalle"
                + ", " + FacturaVenta.class.getSimpleName() + " fv JOIN fv.detallesVentaList detalleVenta JOIN detalleVenta.producto p "
                + " WHERE fv.anulada = false"
                + " AND detalle.cajaMovimientos.id = " + cm.getId() + " "
                + " AND detalle.tipo = " + FACTU_VENTA
                + " AND detalle.numero = fv.id"
                + " AND p.id=" + p.getId()
                + "").getSingleResult();
    }
}
