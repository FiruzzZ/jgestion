package jgestion;

import entity.CuentaBancaria;
import entity.*;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextField;
import utilities.general.UTIL;
import utilities.swing.components.ComboBoxWrapper;

/**
 *
 * @author FiruzzZ
 */
public class JGestionUtils {

    public static List<ComboBoxWrapper<CtacteProveedor>> getWrappedCtacteProveedor(List<CtacteProveedor> list) {
        List<ComboBoxWrapper<CtacteProveedor>> l = new ArrayList<ComboBoxWrapper<CtacteProveedor>>(list.size());
        for (CtacteProveedor o : list) {
            l.add(new ComboBoxWrapper<CtacteProveedor>(o, o.getId(), getNumeracion(o.getFactura())));
        }
        return l;
    }

    public static List<ComboBoxWrapper<CtacteCliente>> getWrappedCtacteCliente(List<CtacteCliente> list) {
        List<ComboBoxWrapper<CtacteCliente>> wrappedList = new ArrayList<ComboBoxWrapper<CtacteCliente>>(list.size());
        for (CtacteCliente o : list) {
            wrappedList.add(new ComboBoxWrapper<CtacteCliente>(o, o.getId(), getNumeracion(o.getFactura())));
        }
        return wrappedList;
    }

//    public static List<ComboBoxWrapper<Librado>> getWrappedLibrado(List<Librado> list) {
//        List<ComboBoxWrapper<Librado>> l = new ArrayList<ComboBoxWrapper<Librado>>(list.size());
//        for (Librado o : list) {
//            l.add(new ComboBoxWrapper<Librado>(o, o.getId(), o.getNombre()));
//        }
//        return l;
//    }
    public static List<ComboBoxWrapper<Cliente>> getWrappedClientes(List<Cliente> list) {
        List<ComboBoxWrapper<Cliente>> l = new ArrayList<ComboBoxWrapper<Cliente>>(list.size());
        for (Cliente o : list) {
            l.add(new ComboBoxWrapper<Cliente>(o, o.getId(), o.getNombre()));
        }
        return l;
    }

    public static List<ComboBoxWrapper<Proveedor>> getWrappedProveedores(List<Proveedor> list) {
        List<ComboBoxWrapper<Proveedor>> l = new ArrayList<ComboBoxWrapper<Proveedor>>(list.size());
        for (Proveedor o : list) {
            l.add(new ComboBoxWrapper<Proveedor>(o, o.getId(), o.getNombre()));
        }
        return l;
    }

    public static List<ComboBoxWrapper<ListaPrecios>> getWrappedListaPrecios(List<ListaPrecios> list) {
        List<ComboBoxWrapper<ListaPrecios>> l = new ArrayList<ComboBoxWrapper<ListaPrecios>>(list.size());
        for (ListaPrecios o : list) {
            l.add(new ComboBoxWrapper<ListaPrecios>(o, o.getId(), o.getNombre()));
        }
        return l;
    }

    public static List<ComboBoxWrapper<Marca>> getWrappedMarcas(List<Marca> list) {
        List<ComboBoxWrapper<Marca>> l = new ArrayList<ComboBoxWrapper<Marca>>(list.size());
        for (Marca o : list) {
            l.add(new ComboBoxWrapper<Marca>(o, o.getId(), o.getNombre()));
        }
        return l;
    }

    public static List<ComboBoxWrapper<Rubro>> getWrappedRubros(List<Rubro> list) {
        List<ComboBoxWrapper<Rubro>> l = new ArrayList<ComboBoxWrapper<Rubro>>(list.size());
        for (Rubro o : list) {
            l.add(new ComboBoxWrapper<Rubro>(o, o.getIdrubro(), o.getNombre()));
        }
        return l;
    }

    public static List<ComboBoxWrapper<SubCuenta>> getWrappedSubCuentas(List<SubCuenta> list) {
        List<ComboBoxWrapper<SubCuenta>> l = new ArrayList<ComboBoxWrapper<SubCuenta>>(list.size());
        for (SubCuenta o : list) {
            l.add(new ComboBoxWrapper<SubCuenta>(o, o.getId(), o.getNombre()));
        }
        return l;
    }

    public static List<ComboBoxWrapper<UnidadDeNegocio>> getWrappedUnidadDeNegocios(List<UnidadDeNegocio> list) {
        List<ComboBoxWrapper<UnidadDeNegocio>> l = new ArrayList<ComboBoxWrapper<UnidadDeNegocio>>(list.size());
        for (UnidadDeNegocio o : list) {
            l.add(new ComboBoxWrapper<UnidadDeNegocio>(o, o.getId(), o.getNombre()));
        }
        return l;
    }

    public static List<ComboBoxWrapper<Sucursal>> getWrappedSucursales(List<Sucursal> list) {
        List<ComboBoxWrapper<Sucursal>> l = new ArrayList<ComboBoxWrapper<Sucursal>>(list.size());
        for (Sucursal o : list) {
            l.add(new ComboBoxWrapper<Sucursal>(o, o.getId(), o.getNombre()));
        }
        return l;
    }

    public static List<ComboBoxWrapper<Cuenta>> getWrappedCuentas(List<Cuenta> list) {
        List<ComboBoxWrapper<Cuenta>> l = new ArrayList<ComboBoxWrapper<Cuenta>>(list.size());
        for (Cuenta o : list) {
            l.add(new ComboBoxWrapper<Cuenta>(o, o.getId(), o.getNombre()));
        }
        return l;
    }

    public static List<ComboBoxWrapper<CuentaBancaria>> getWrappedCuentasBancarias(List<CuentaBancaria> list) {
        List<ComboBoxWrapper<CuentaBancaria>> l = new ArrayList<ComboBoxWrapper<CuentaBancaria>>(list.size());
        for (CuentaBancaria o : list) {
            l.add(new ComboBoxWrapper<CuentaBancaria>(o, o.getId(), o.getNumero().toString()));
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
            return "F" + o.getTipo() + UTIL.AGREGAR_CEROS(o.getSucursal().getPuntoVenta(), 4) + "-" + UTIL.AGREGAR_CEROS(o.getNumero(), 8);
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

    public static String getNumeracion(NotaCreditoProveedor o, boolean conGuion) {
        String guion = conGuion ? "-" : "";
        String numero = UTIL.AGREGAR_CEROS(o.getNumero(), 12);
        return numero.substring(0, 4) + guion + numero.substring(4);
    }

    public static String getNumeracion(Remesa o, boolean conGuion) {
        String guion = conGuion ? "-" : "";
        return UTIL.AGREGAR_CEROS(o.getSucursal().getPuntoVenta(), 4) + guion + UTIL.AGREGAR_CEROS(o.getNumero(), 8);
    }

    public static void setCurrencyFormatterFocusListener(JTextField tf) {
        tf.addFocusListener(
                new FocusListener() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        JTextField t = (JTextField) e.getSource();
                        if (!t.getText().trim().isEmpty()) {
                            t.setText(UTIL.parseToDouble(t.getText()).toString());
                        }
                    }

                    @Override
                    public void focusLost(FocusEvent e) {
                        JTextField t = (JTextField) e.getSource();
                        try {
                            if (!t.getText().trim().isEmpty()) {
                                t.setText(UTIL.DECIMAL_FORMAT.format(new BigDecimal(t.getText())));
                            }
                            t.setBackground(Color.WHITE);
                        } catch (Exception ex) {
                            t.setBackground(Color.RED);
                        }
                    }
                });
    }

    public static ComboBoxWrapper<Banco> wrap(Banco o) {
        return new ComboBoxWrapper<Banco>(o, o.getId(), o.getNombre());
    }

    public static ComboBoxWrapper<CuentaBancaria> wrap(CuentaBancaria o) {
        return new ComboBoxWrapper<CuentaBancaria>(o, o.getId(), o.getNumero());
    }

    private JGestionUtils() {
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}
