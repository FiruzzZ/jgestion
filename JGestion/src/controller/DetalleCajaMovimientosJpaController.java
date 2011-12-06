package controller;

import controller.exceptions.MessageException;
import controller.exceptions.NonexistentEntityException;
import entity.DetalleCajaMovimientos;
import entity.MovimientoConcepto;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import entity.CajaMovimientos;

/**
 *
 * @author Administrador
 */
public class DetalleCajaMovimientosJpaController {

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
     * nº 5, son los movimientos monetarios entre Cajas (tipo de mov. interno).
     * No son un INGRESO o EGRESO real 
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
            if (detalleCajaMovimientos.getMovimientoConcepto() == null) {
                detalleCajaMovimientos.setMovimientoConcepto(MovimientoConceptoJpaController.EFECTIVO);
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
                    + "\nIntente cerrar y volver a abrir la ventana.");
        } catch (Exception ex) {
            throw new MessageException(ex.getMessage());
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    void edit(DetalleCajaMovimientos detalleCajaMovimientos) throws NonexistentEntityException, Exception {
        DAO.doMerge(detalleCajaMovimientos);
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

    List<MovimientoConcepto> findDetalleCajaMovimientosBy(MovimientoConcepto movimientoConcepto) {
        return getEntityManager().createQuery("SELECT o FROM " + CLASS_NAME + " o WHERE o.movimientoConcepto.id=" + movimientoConcepto.getId()).getResultList();
    }
}
