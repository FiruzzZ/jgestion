package jgestion.jpa.controller;

import java.util.List;
import jgestion.entity.Banco;
import jgestion.entity.Banco_;

/**
 *
 * @author FiruzzZ
 */
public class BancoJpaController extends JGestionJpaImpl<Banco, Integer> {

    @Override
    public List<Banco> findAll() {
        return super.findAll(getOrder(Banco_.nombre, true));
    }

}
