package jgestion;

import entity.*;
import utilities.general.UTIL;

/**
 *
 * @author FiruzzZ
 */
public class JGestionUtils {

    /**
     * return TipoFactura + #### (punto de venta) + ######## (número). <br>EJ:
     * A0001-00001234
     *
     * @param factura
     * @return
     */
    public static String getFullIdentificador(FacturaVenta factura) {
        return factura.getTipo() + UTIL.AGREGAR_CEROS(factura.getSucursal().getPuntoVenta(), 4) + "-" + UTIL.AGREGAR_CEROS(factura.getNumero(), 8);
    }

    private JGestionUtils() {
    }

    public static String getNumeracion(Presupuesto o, boolean conGuion) {
        String guion = conGuion ? "-" : "";
        return UTIL.AGREGAR_CEROS(o.getSucursal().getPuntoVenta(), 4) + guion + UTIL.AGREGAR_CEROS(o.getNumero(), 8);
    }

    public static String getNumeracion(Remito o, boolean conGuion) {
        String guion = conGuion ? "-" : "";
        return UTIL.AGREGAR_CEROS(o.getSucursal().getPuntoVenta(), 4) + guion + UTIL.AGREGAR_CEROS(o.getNumero(), 8);
    }

    public static String getNumeracion(FacturaVenta o, boolean conGuion) {
        if (o.getMovimientoInterno() == 0) {
            String guion = conGuion ? "-" : "";
            return UTIL.AGREGAR_CEROS(o.getSucursal().getPuntoVenta(), 4) + guion + UTIL.AGREGAR_CEROS(o.getNumero(), 8);
        } else {
            return "I" + String.valueOf(o.getMovimientoInterno());
        }
    }

    public static String getNumeracion(Recibo o, boolean conGuion) {
        String guion = conGuion ? "-" : "";
        return UTIL.AGREGAR_CEROS(o.getSucursal().getPuntoVenta(), 4) + guion + UTIL.AGREGAR_CEROS(o.getNumero(), 8);
    }

    public static String getNumeracion(NotaCredito o, boolean conGuion) {
        String guion = conGuion ? "-" : "";
        return UTIL.AGREGAR_CEROS(o.getSucursal().getPuntoVenta(), 4) + guion + UTIL.AGREGAR_CEROS(o.getNumero(), 8);
    }
}
