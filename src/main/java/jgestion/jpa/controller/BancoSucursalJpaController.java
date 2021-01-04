package jgestion.jpa.controller;

import java.util.HashMap;
import jgestion.entity.Banco;
import jgestion.entity.BancoSucursal;

/**
 *
 * @author FiruzzZ
 */
public class BancoSucursalJpaController extends JGestionJpaImpl<BancoSucursal, Integer> {

    /**
     *
     * @param banco
     * @param nombre CI comparation
     * @return
     */
    public BancoSucursal findByNombre(Banco banco, String nombre) {
        return findByQuery(getSelectFrom()
                + " WHERE upper(o.nombre)='" + nombre.toUpperCase() + "' AND o.banco.id=" + banco.getId());
    }

    public BancoSucursal findByCodigo(String codigo) {
        HashMap<String, Object> p = new HashMap<>();
        p.put("codigo", codigo.toUpperCase());
        return findByQuery(getSelectFrom() + " WHERE upper(o.codigo) =:codigo", p);
    }

}
