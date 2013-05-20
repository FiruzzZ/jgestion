package jgestion;

import entity.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import utilities.general.UTIL;
import utilities.swing.components.ComboBoxWrapper;

/**
 *
 * @author FiruzzZ
 */
public class JGestionUtils {

    public JGestionUtils() {
    }

    public File openFileChooser(Component parent, String description, File fileDir, String... fileExtensionsAllow) throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        File file = null;
        if (fileExtensionsAllow != null && fileExtensionsAllow.length > 0) {
            FileNameExtensionFilter filter = new FileNameExtensionFilter(description, fileExtensionsAllow);
            fileChooser.setFileFilter(filter);
            fileChooser.addChoosableFileFilter(filter);
        }
        fileChooser.setCurrentDirectory(fileDir);
        int stateFileChoosed = fileChooser.showOpenDialog(parent);
        if (stateFileChoosed == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
            if (file.exists() && JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(parent, "Ya existe el archivo " + file.getName() + ", ¿Desea reemplazarlo?", null, JOptionPane.YES_NO_OPTION)) {
                return null;
            }
        }
        return file;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public static List<ComboBoxWrapper<?>> getWrappedCtacteProveedor(List<?> list) {
        List<ComboBoxWrapper<?>> l = new ArrayList<ComboBoxWrapper<?>>(list.size());
        for (Object o : list) {
            if (o instanceof CtacteProveedor) {
                CtacteProveedor cc = (CtacteProveedor) o;
                l.add(new ComboBoxWrapper<Object>(cc, cc.getId(), getNumeracion(cc.getFactura())));
            } else {
                NotaDebitoProveedor nota = (NotaDebitoProveedor) o;
                l.add(new ComboBoxWrapper<Object>(nota, nota.getId(), getNumeracion(nota)));

            }
        }
        return l;
    }

    public static List<ComboBoxWrapper<?>> getWrappedCtacteCliente(List<?> list) {
        List<ComboBoxWrapper<?>> l = new ArrayList<ComboBoxWrapper<?>>(list.size());
        for (Object o : list) {
            if (o instanceof CtacteCliente) {
                CtacteCliente ccc = (CtacteCliente) o;
                l.add(new ComboBoxWrapper<Object>(ccc, ccc.getId(), getNumeracion(ccc.getFactura())));
            } else {
                NotaDebito nota = (NotaDebito) o;
                l.add(new ComboBoxWrapper<Object>(nota, nota.getId(), getNumeracion(nota)));
            }
        }
        return l;
    }

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

    public static List<ComboBoxWrapper<Remesa>> getWrappedRemesas(List<Remesa> list) {
        List<ComboBoxWrapper<Remesa>> l = new ArrayList<ComboBoxWrapper<Remesa>>(list.size());
        for (Remesa remesa : list) {
            l.add(new ComboBoxWrapper<Remesa>(remesa, remesa.getId(), JGestionUtils.getNumeracion(remesa, true)));
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

    public static List<ComboBoxWrapper<Usuario>> getWrappedUsuarios(List<Usuario> list) {
        List<ComboBoxWrapper<Usuario>> l = new ArrayList<ComboBoxWrapper<Usuario>>(list.size());
        for (Usuario usuario : list) {
            l.add(new ComboBoxWrapper<Usuario>(usuario, usuario.getId(), usuario.getNick()));
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

    public static String getNumeracion(NotaDebito o) {
        return "ND" + o.getTipo() + UTIL.AGREGAR_CEROS(o.getSucursal().getPuntoVenta(), 4) + "-" + UTIL.AGREGAR_CEROS(o.getNumero(), 8);
    }

    public static String getNumeracion(NotaDebitoProveedor o) {
        String numero = UTIL.AGREGAR_CEROS(o.getNumero(), 12);
        return "ND" + o.getTipo() + numero.substring(0, 4) + "-" + numero.substring(4);
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

    public static List<ComboBoxWrapper<Vendedor>> getWrappedVendedor(List<Vendedor> list) {
        List<ComboBoxWrapper<Vendedor>> l = new ArrayList<ComboBoxWrapper<Vendedor>>(list.size());
        for (Vendedor o : list) {
            l.add(new ComboBoxWrapper<Vendedor>(o, o.getId(), o.getApellido() + " " + o.getNombre()));
        }
        return l;
    }

    @SuppressWarnings("unchecked")
    public static void cargarComboTiposFacturas(JComboBox cb, Proveedor o) {
        if (o != null) {
            cb.removeAllItems();
            if (o.getContribuyente().getFactuA()) {
                cb.addItem("A");
            }
            if (o.getContribuyente().getFactuB()) {
                cb.addItem("B");
            }
            if (o.getContribuyente().getFactuC()) {
                cb.addItem("C");
            }
            if (o.getContribuyente().getFactuM()) {
                cb.addItem("M");
            }
            if (o.getContribuyente().getFactuX()) {
                cb.addItem("X");
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void cargarComboTiposFacturas(JComboBox cb, Cliente o) {
        if (o != null) {
            cb.removeAllItems();
            if (o.getContribuyente().getFactuA()) {
                cb.addItem("A");
            }
            if (o.getContribuyente().getFactuB()) {
                cb.addItem("B");
            }
            if (o.getContribuyente().getFactuC()) {
                cb.addItem("C");
            }
            if (o.getContribuyente().getFactuM()) {
                cb.addItem("M");
            }
            if (o.getContribuyente().getFactuX()) {
                cb.addItem("X");
            }
        }
    }

    public static List<ComboBoxWrapper<Iva>> getWrappedIva(List<Iva> list) {
        List<ComboBoxWrapper<Iva>> l = new ArrayList<ComboBoxWrapper<Iva>>(list.size());
        for (Iva iva : list) {
            l.add(new ComboBoxWrapper<Iva>(iva, iva.getId(), iva.getIva().toString() + "%"));
        }
        return l;
    }
}
