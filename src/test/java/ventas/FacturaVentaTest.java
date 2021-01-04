/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ventas;

import java.util.List;
import jgestion.entity.FacturaVenta;
import jgestion.jpa.controller.FacturaVentaJpaController;
import junit.framework.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

/**
 *
 * @author JoseLuis
 */
public class FacturaVentaTest extends TestCase {

    private FacturaVentaJpaController dao;
    private static final Logger LOG = LogManager.getLogger();

    public FacturaVentaTest(String testName) {
        super(testName);
        dao = new FacturaVentaJpaController();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testSumatoriaDeConceptosIgualATotal() {
        List<FacturaVenta> ll = dao.findAll(dao.getSelectFrom() + " WHERE o.importe <>"
                + " (o.iva10 + o.iva21 + o.gravado + o.noGravado - o.descuento - o.diferenciaRedondeo)");
        for (FacturaVenta f : ll) {
            LOG.trace("Id {}, importe {} , iva10 {}, iva21 {}, gravad {}, noGravado {}, descuento {}, dif {} = "
                    ,f.getId(), f.getImporte(), f.getIva10());
        }
        assertTrue("incorrectas: " + ll.size(), ll.isEmpty());
    }
}
