package jgestion;

import entity.*;
import java.util.ArrayList;
import java.util.List;
import utilities.general.UTIL;
import utilities.swing.components.ComboBoxWrapper;

/**
 *
 * @author FiruzzZ
 */
public class JGestionUtils {

    public static List<ComboBoxWrapper<CtacteProveedor>> getWrappedCtacteProveedor(List<CtacteProveedor> list) {
        List<ComboBoxWrapper<CtacteProveedor>> l = new ArrayList<ComboBoxWrapper<CtacteProveedor>>(list.size());
        for (CtacteProveedor ctacteProveedor : list) {
            l.add(new ComboBoxWrapper<CtacteProveedor>(ctacteProveedor, ctacteProveedor.getId(), getNumeracion(ctacteProveedor.getFactura())));
        }
        return l;
    }

    public static List<ComboBoxWrapper<CtacteCliente>> getWrappedCtacteCliente(List<CtacteCliente> list) {
        List<ComboBoxWrapper<CtacteCliente>> wrappedList = new ArrayList<ComboBoxWrapper<CtacteCliente>>(list.size());
        for (CtacteCliente ctacteCliente : list) {
            FacturaVenta factura = ctacteCliente.getFactura();
            wrappedList.add(new ComboBoxWrapper<CtacteCliente>(ctacteCliente, ctacteCliente.getId(), JGestionUtils.getNumeracion(factura)));
        }
        return wrappedList;
    }

    private JGestionUtils() {
    }

    public static List<ComboBoxWrapper<Cuentabancaria>> getWrappedCuentasBancarias(List<Cuentabancaria> list) {
        List<ComboBoxWrapper<Cuentabancaria>> l = new ArrayList<ComboBoxWrapper<Cuentabancaria>>(list.size());
        for (Cuentabancaria cuentabancaria : list) {
            l.add(new ComboBoxWrapper<Cuentabancaria>(cuentabancaria, cuentabancaria.getId(), cuentabancaria.getNumero().toString()));
        }
        return l;
    }

    public static List<ComboBoxWrapper<OperacionesBancarias>> getWrappedOperacionesBancarias(List<OperacionesBancarias> list) {
        List<ComboBoxWrapper<OperacionesBancarias>> l = new ArrayList<ComboBoxWrapper<OperacionesBancarias>>(list.size());
        for (OperacionesBancarias o : list) {
            l.add(new ComboBoxWrapper<OperacionesBancarias>(o, o.getId(), o.getNombre()));
        }
        return l;
    }

    public static List<ComboBoxWrapper<Banco>> getWrappedBancos(final List<Banco> list) {
        List<ComboBoxWrapper<Banco>> l = new ArrayList<ComboBoxWrapper<Banco>>(list.size());
        for (Banco o : list) {
            l.add(new ComboBoxWrapper<Banco>(o, o.getId(), o.getNombre()));
        }
        return l;
    }

    /**
     * Si es Factura: <br> "F" + TipoFactura + #### (punto de venta) + ########
     * (número). <br>EJ: FA0001-00001234 <br>Si es Interno: <br>"FI" + ####
     * (punto de venta) + ######## (N° Movimiento interno)
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
     * (número). <br>EJ: FA0001-00001234 <br>Si es Interno: <br>"FI" + ####
     * (punto de venta DONDE SER REALIZO LA CARGA) + ######## (N° Movimiento
     * interno)
     *
     * @param o
     * @return
     */
    public static String getNumeracion(FacturaCompra o) {
        if (o.getNumero() == 0) {
            return "FI" + UTIL.AGREGAR_CEROS(o.getSucursal().getPuntoVenta(), 4) + UTIL.AGREGAR_CEROS(o.getMovimientoInterno(), 8);
        } else {
            String numero = UTIL.AGREGAR_CEROS(o.getNumero(), 12);
            return "F" + o.getTipo() + numero.substring(0, 4) + "-" + numero.substring(4);
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
