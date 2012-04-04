package jpa.controller;

import controller.CtacteClienteJpaController;
import controller.DAO;
import controller.Valores;
import controller.exceptions.MessageException;
import entity.*;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

/**
 *
 * @author FiruzzZ
 */
public class ReciboJpaController extends AbstractDAO<Recibo, Integer> {

    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    public Integer getNextNumero(Sucursal sucursal) {
        Long l = (Long) getEntityManager().createQuery("SELECT MAX(o.numero)"
                + " FROM " + getEntityClass().getSimpleName() + " o"
                + " WHERE o.sucursal.id = " + sucursal.getId()).getSingleResult();
        if (l != null) {
            return l.intValue();
        } else {
            System.out.println("Pintó el 1er de " + getEntityClass().getSimpleName() + ", Sucursa=" + sucursal.getNombre() + "(" + sucursal.getPuntoVenta() + ")");
            return 1;
        }
    }

    /**
     * La anulación de una Recibo, resta a
     * <code>CtaCteCliente.entregado</code> los pagos/entregas
     * (parciales/totales) realizados de cada DetalleRecibo y cambia
     * <code>Recibo.estado = false<code>
     *
     * @param recibo
     * @throws MessageException
     * @throws Exception si Recibo es null, o si ya está anulado
     */
    public void anular(Recibo recibo) throws MessageException, Exception {
        EntityManager em = getEntityManager();
        if (recibo == null) {
            throw new MessageException(getEntityClass().getSimpleName() + " no válido");
        }
        if (!recibo.getEstado()) {
            throw new MessageException("Este " + getEntityClass().getSimpleName() + " ya está anulado");
        }

        List<DetalleRecibo> detalleReciboList = recibo.getDetalleReciboList();
        CtacteCliente ctaCteCliente;
        try {
            em.getTransaction().begin();
            for (DetalleRecibo dr : detalleReciboList) {
                //se resta la entrega ($) que implicaba este detalle con respecto a CADA factura
                ctaCteCliente = new CtacteClienteJpaController().findCtacteClienteByFactura(dr.getFacturaVenta().getId());
                ctaCteCliente.setEntregado(ctaCteCliente.getEntregado() - dr.getMontoEntrega());
                // y si había sido pagada en su totalidad..
                if (ctaCteCliente.getEstado() == Valores.CtaCteEstado.PAGADA.getId()) {
                    ctaCteCliente.setEstado(Valores.CtaCteEstado.PENDIENTE.getId());
                }
                em.merge(ctaCteCliente);
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
        recibo.setEstado(false);
        merge(recibo);
        new CajaMovimientosJpaController().anular(recibo);
    }
}
