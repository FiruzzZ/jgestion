package jgestion.jpa.controller;

import java.util.List;
import javax.persistence.NoResultException;
import jgestion.entity.FacturaElectronica;

/**
 *
 * @author FiruzzZ
 */
public class FacturaElectronicaJpaController extends JGestionJpaImpl<FacturaElectronica, Integer> {

    public FacturaElectronicaJpaController() {
        
    }

    /**
     *
     * @return
     */
    public List<FacturaElectronica> findAllPendientes() {
        return findAll(getSelectFrom() + " WHERE o.cae IS NULL ORDER BY o.ptoVta ASC, o.cbteNumero ASC, o.cbteTipo ASC ");
    }

    public FacturaElectronica findBy(int cbteTipo, int ptoVta, long numero) {
        try {
            return findByQuery(getSelectFrom()
                    + " WHERE o.cbteNumero=" + numero
                    + " AND o.ptoVta=" + ptoVta
                    + " AND o.cbteTipo=" + cbteTipo);
        } catch (NoResultException e) {
            return null;
        }
    }

}
