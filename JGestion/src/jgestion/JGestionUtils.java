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
     * Si es Factura: <br> "F" + TipoFactura + #### (punto de venta) + ########
     * (número). <br>EJ: A0001-00001234 <br>Si es Interno: <br>"I" + #### (punto
     * de venta) + ######## (N° Movimiento interno)
     *
     * @param o
     * @return
     */
    public static String getNumeracion(FacturaVenta o) {
        if (o.getNumero() == 0) {
            return "FI" + UTIL.AGREGAR_CEROS(o.getSucursal().getPuntoVenta(), 4) + "-" + UTIL.AGREGAR_CEROS(o.getMovimientoInterno(), 8);
        } else {
            return o.getTipo() + UTIL.AGREGAR_CEROS(o.getSucursal().getPuntoVenta(), 4) + "-" + UTIL.AGREGAR_CEROS(o.getNumero(), 8);
        }
    }

    /**
     * Si es Factura: <br> "F"+ TipoFactura + #### (punto de venta) + ########
     * (número). <br>EJ: A0001-00001234 <br>Si es Interno: <br>"FI" + #### (punto
     * de venta DONDE SER REALIZO LA CARGA) + ######## (N° Movimiento interno)
     *
     * @param o
     * @param conGuion
     * @return
     */
    public static String getNumeracion(FacturaCompra o, boolean conGuion) {
        if (o.getNumero() != 0) {
            String guion = conGuion ? "-" : "";
            String numero = UTIL.AGREGAR_CEROS(o.getNumero(), 12);
            return "F" + o.getTipo() + numero.substring(0, 4) + guion + numero.substring(4);
        } else {
            return "FI" + UTIL.AGREGAR_CEROS(o.getSucursal().getPuntoVenta(), 4) + UTIL.AGREGAR_CEROS(o.getMovimientoInterno(), 8);
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
