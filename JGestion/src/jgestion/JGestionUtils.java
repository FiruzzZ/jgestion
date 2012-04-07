package jgestion;

import entity.*;
import utilities.general.UTIL;

/**
 *
 * @author FiruzzZ
 */
public class JGestionUtils {

    private JGestionUtils() {
    }

    /**
     * si es FacturaVenta TipoFactura + #### (punto de venta) + ########
     * (número). <br>EJ: A0001-00001234 <br>si es Interno: "I" + #### (punto de
     * venta) + ######## (N° Movimiento interno)
     *
     * @param facturaVenta
     * @return
     */
    public static String getNumeracion(FacturaVenta facturaVenta) {
        if (facturaVenta.getNumero() == 0) {
            return "FI" + UTIL.AGREGAR_CEROS(facturaVenta.getSucursal().getPuntoVenta(), 4) + "-" + UTIL.AGREGAR_CEROS(facturaVenta.getMovimientoInterno(), 8);
        } else {
            return "F" + facturaVenta.getTipo() + UTIL.AGREGAR_CEROS(facturaVenta.getSucursal().getPuntoVenta(), 4) + "-" + UTIL.AGREGAR_CEROS(facturaVenta.getNumero(), 8);
        }
    }

    public static String getNumeracion(FacturaCompra o, boolean conGuion) {
        if (o.getMovimientoInterno() == 0) {
            String guion = conGuion ? "-" : "";
            return "F" + o.getTipo() + UTIL.AGREGAR_CEROS(o.getSucursal().getPuntoVenta(), 4) + guion + UTIL.AGREGAR_CEROS(o.getNumero(), 8);
        } else {
            return "I" + UTIL.AGREGAR_CEROS(o.getSucursal().getPuntoVenta(), 4) + UTIL.AGREGAR_CEROS(o.getMovimientoInterno(), 8);
        }
    }

    public static String getNumeracion(Presupuesto o, boolean conGuion) {
        String guion = conGuion ? "-" : "";
        return UTIL.AGREGAR_CEROS(o.getSucursal().getPuntoVenta(), 4) + guion + UTIL.AGREGAR_CEROS(o.getNumero(), 8);
    }

    public static String getNumeracion(Remito o, boolean conGuion) {
        String guion = conGuion ? "-" : "";
        return UTIL.AGREGAR_CEROS(o.getSucursal().getPuntoVenta(), 4) + guion + UTIL.AGREGAR_CEROS(o.getNumero(), 8);
    }

    public static String getNumeracion(Recibo o, boolean conGuion) {
        String guion = conGuion ? "-" : "";
        return UTIL.AGREGAR_CEROS(o.getSucursal().getPuntoVenta(), 4) + guion + UTIL.AGREGAR_CEROS(o.getNumero(), 8);
    }

    public static String getNumeracion(NotaCredito o, boolean conGuion) {
        String guion = conGuion ? "-" : "";
        return UTIL.AGREGAR_CEROS(o.getSucursal().getPuntoVenta(), 4) + guion + UTIL.AGREGAR_CEROS(o.getNumero(), 8);
    }

    public static String getNumeracion(Remesa o, boolean conGuion) {
        String guion = conGuion ? "-" : "";
        return UTIL.AGREGAR_CEROS(o.getSucursal().getPuntoVenta(), 4) + guion + UTIL.AGREGAR_CEROS(o.getNumero(), 8);
    }
}
