package jgestion.jpa.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import jgestion.entity.Producto;
import jgestion.entity.Producto_;
import jgestion.entity.Rubro;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import jgestion.entity.Marca;
import org.eclipse.persistence.config.QueryHints;

/**
 *
 * @author Administrador
 */
public class ProductoJpaController extends JGestionJpaImpl<Producto, Integer> {

    public ProductoJpaController() {
    }

    @Override
    public Producto find(Integer id) {
        boolean flag = false;
        if (!isForceRefresh()) {
            flag = true;
            setForceRefresh(true);
        }
        Producto o = super.find(id);
        if (flag) {
            setForceRefresh(false);
        }
        return o;
    }

    public Producto findByCodigo(String codigo) {
        HashMap<String, Object> p = new HashMap<>(1);
        p.put("codigo", codigo);
        return findByQuery(getSelectFrom() + " where o.codigo=:codigo", p);
    }

    @Override
    public List<Producto> findAll() {
        return findAll(getOrder(Producto_.nombre, true));
    }

    public List<Producto> findByBienDeCambio(Boolean bienDeCambio) {
        if (bienDeCambio == null) {
            return findAll();
        }
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Producto> cq = cb.createQuery(getEntityClass());
        Root<Producto> from = cq.from(getEntityClass());
        cq.select(from);
        cq.where(cb.equal(from.get(Producto_.bienDeCambio), bienDeCambio));
        cq.orderBy(cb.asc(from.get(Producto_.nombre)));
        return getEntityManager().createQuery(cq).getResultList();
    }

    public List<Producto> findBy(Rubro rubro) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Producto> cq = cb.createQuery(getEntityClass());
        Root<Producto> from = cq.from(getEntityClass());
        cq.select(from);
        cq.where(cb.equal(from.get(Producto_.bienDeCambio), true));
        cq.where(cb.equal(from.get(Producto_.rubro), rubro));
        cq.orderBy(cb.asc(from.get(Producto_.nombre)));
        return getEntityManager().createQuery(cq).getResultList();
    }

    /**
     * Id, codigo, nombre, marca, precioventa
     *
     * @param bienDeCambio
     * @return
     */
    public List<Producto> findAllLite(Boolean bienDeCambio) {
        List<Object[]> pp = findAttributes("SELECT o.id, o.codigo, o.nombre, o.marca, o.precioVenta FROM "
                + getAlias()
                + (bienDeCambio != null ? " WHERE  o.bienDeCambio=" + bienDeCambio : "")
                + " ORDER BY o.nombre");
        List<Producto> ll = new ArrayList<>(pp.size());
        for (Object[] o : pp) {
            Producto p = new Producto();
            p.setId((Integer) o[0]);
            p.setCodigo((String) o[1]);
            p.setNombre((String) o[2]);
            p.setMarca((Marca) o[3]);
            p.setPrecioVenta((BigDecimal) o[4]);
            ll.add(p);
        }
        return ll;
    }
}
