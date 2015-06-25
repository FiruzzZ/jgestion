package jgestion;

import jgestion.entity.NotaDebitoProveedor;
import jgestion.entity.Vendedor;
import jgestion.entity.Presupuesto;
import jgestion.entity.Sucursal;
import jgestion.entity.NotaCredito;
import jgestion.entity.SubCuenta;
import jgestion.entity.Recibo;
import jgestion.entity.OperacionesBancarias;
import jgestion.entity.CuentaBancaria;
import jgestion.entity.FacturaCompra;
import jgestion.entity.ListaPrecios;
import jgestion.entity.Rubro;
import jgestion.entity.Cliente;
import jgestion.entity.CtacteProveedor;
import jgestion.entity.NotaCreditoProveedor;
import jgestion.entity.NotaDebito;
import jgestion.entity.Cuenta;
import jgestion.entity.Remesa;
import jgestion.entity.FacturaVenta;
import jgestion.entity.Proveedor;
import jgestion.entity.Usuario;
import jgestion.entity.UnidadDeNegocio;
import jgestion.entity.Iva;
import jgestion.entity.Marca;
import jgestion.entity.CtacteCliente;
import jgestion.entity.Remito;
import jgestion.entity.Dominio;
import jgestion.entity.Banco;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import jgestion.controller.UsuarioController;
import jgestion.entity.Caja;
import jgestion.entity.RemitoCompra;
import jgestion.jpa.controller.JGestionJpaImpl;
import utilities.general.EntityWrapper;
import utilities.general.UTIL;

/**
 *
 * @author FiruzzZ
 */
public class JGestionUtils {

    /**
     * Conserva el path del directorio del último archivo seleccionado
     */
    public volatile static String LAST_DIRECTORY_PATH;

    public JGestionUtils() {
    }
    private static final String[] yyy = {
        "",
        "",
        "",
        "¡Bien! ",
        "¡Que grande! ",
        "Seguí así ",
        "Cuando sea grande quiero ser como vos "
    };
    private static final String[] xxx = {
        "", // <--- para confundir al usuario!!
        "",
        "O que você está querendo fazer?!",
        "No no no.. nada que ver!",
        "WTF!",
        "Alerta de usuario peligroso!",
        "No te hagás del vivo/a",
        "Ojito vo!"
    };

    public static String getRandomMotivation() {
        String s = yyy[new Random().nextInt(yyy.length)];
        return s.isEmpty() ? s : "\n" + s + UsuarioController.getCurrentUser().getNick();
    }

    public static String getRandomAgression() {
        return xxx[new Random().nextInt(xxx.length)];
    }

    public static Date getServerDate() {
        return new JGestionJpaImpl<Object, Serializable>() {
        }.getServerDate();
    }

    public static File showSaveDialogFileChooser(Component parent, String description, File fileDir, String fileExtension) throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        File file = null;
        if (fileExtension != null) {
            FileNameExtensionFilter filter = new FileNameExtensionFilter(description, fileExtension);
            fileChooser.setFileFilter(filter);
            fileChooser.addChoosableFileFilter(filter);
        }
        fileChooser.setCurrentDirectory(fileDir);
        int stateFileChoosed = fileChooser.showSaveDialog(parent);
        if (stateFileChoosed == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
            if (fileExtension != null && !file.getName().endsWith("." + fileExtension)) {
                file = new File(file.getPath() + "." + fileExtension);
            }
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

    public static List<EntityWrapper<?>> getWrappedCtacteProveedor(List<?> list) {
        List<EntityWrapper<?>> l = new ArrayList<>(list.size());
        for (Object o : list) {
            if (o instanceof CtacteProveedor) {
                CtacteProveedor cc = (CtacteProveedor) o;
                l.add(new EntityWrapper<Object>(cc, cc.getId(), getNumeracion(cc.getFactura())));
            } else {
                NotaDebitoProveedor nota = (NotaDebitoProveedor) o;
                l.add(new EntityWrapper<Object>(nota, nota.getId(), getNumeracion(nota)));

            }
        }
        return l;
    }

    public static List<EntityWrapper<?>> getWrappedCtacteCliente(List<CtacteCliente> list) {
        List<EntityWrapper<?>> l = new ArrayList<>(list.size());
        for (CtacteCliente ccc : list) {
            l.add(new EntityWrapper<Object>(ccc, ccc.getId(), ccc.getFactura() != null ? getNumeracion(ccc.getFactura()) : getNumeracion(ccc.getNotaDebito())));
        }
        return l;
    }

    public static List<EntityWrapper<Cliente>> getWrappedClientes(List<Cliente> list) {
        List<EntityWrapper<Cliente>> l = new ArrayList<>(list.size());
        for (Cliente o : list) {
            l.add(new EntityWrapper<>(o, o.getId(), o.getNombre()));
        }
        return l;
    }

    public static List<EntityWrapper<Proveedor>> getWrappedProveedores(List<Proveedor> list) {
        List<EntityWrapper<Proveedor>> l = new ArrayList<>(list.size());
        for (Proveedor o : list) {
            l.add(new EntityWrapper<>(o, o.getId(), o.getNombre()));
        }
        return l;
    }

    public static List<EntityWrapper<ListaPrecios>> getWrappedListaPrecios(List<ListaPrecios> list) {
        List<EntityWrapper<ListaPrecios>> l = new ArrayList<>(list.size());
        for (ListaPrecios o : list) {
            l.add(new EntityWrapper<>(o, o.getId(), o.getNombre()));
        }
        return l;
    }

    public static List<EntityWrapper<Marca>> getWrappedMarcas(List<Marca> list) {
        List<EntityWrapper<Marca>> l = new ArrayList<>(list.size());
        for (Marca o : list) {
            l.add(new EntityWrapper<>(o, o.getId(), o.getNombre()));
        }
        return l;
    }

    public static List<EntityWrapper<Remesa>> getWrappedRemesas(List<Remesa> list) {
        List<EntityWrapper<Remesa>> l = new ArrayList<>(list.size());
        for (Remesa remesa : list) {
            l.add(new EntityWrapper<>(remesa, remesa.getId(), JGestionUtils.getNumeracion(remesa, true)));
        }
        return l;
    }

    public static List<EntityWrapper<Rubro>> getWrappedRubros(List<Rubro> list) {
        List<EntityWrapper<Rubro>> l = new ArrayList<>(list.size());
        for (Rubro o : list) {
            l.add(new EntityWrapper<>(o, o.getId(), o.getNombre()));
        }
        return l;
    }

    public static List<EntityWrapper<SubCuenta>> getWrappedSubCuentas(List<SubCuenta> list) {
        List<EntityWrapper<SubCuenta>> l = new ArrayList<>(list.size());
        for (SubCuenta o : list) {
            l.add(new EntityWrapper<>(o, o.getId(), o.getNombre()));
        }
        return l;
    }

    public static List<EntityWrapper<UnidadDeNegocio>> getWrappedUnidadDeNegocios(List<UnidadDeNegocio> list) {
        List<EntityWrapper<UnidadDeNegocio>> l = new ArrayList<>(list.size());
        for (UnidadDeNegocio o : list) {
            l.add(new EntityWrapper<>(o, o.getId(), o.getNombre()));
        }
        return l;
    }

    public static List<EntityWrapper<Usuario>> getWrappedUsuarios(List<Usuario> list) {
        List<EntityWrapper<Usuario>> l = new ArrayList<>(list.size());
        for (Usuario usuario : list) {
            l.add(new EntityWrapper<>(usuario, usuario.getId(), usuario.getNick()));
        }
        return l;
    }

    public static List<EntityWrapper<Sucursal>> getWrappedSucursales(List<Sucursal> list) {
        List<EntityWrapper<Sucursal>> l = new ArrayList<>(list.size());
        for (Sucursal o : list) {
            l.add(new EntityWrapper<>(o, o.getId(), o.getNombre()));
        }
        return l;
    }

    public static List<EntityWrapper<Cuenta>> getWrappedCuentas(List<Cuenta> list) {
        List<EntityWrapper<Cuenta>> l = new ArrayList<>(list.size());
        for (Cuenta o : list) {
            l.add(new EntityWrapper<>(o, o.getId(), o.getNombre()));
        }
        return l;
    }

    public static List<EntityWrapper<CuentaBancaria>> getWrappedCuentasBancarias(List<CuentaBancaria> list) {
        List<EntityWrapper<CuentaBancaria>> l = new ArrayList<>(list.size());
        for (CuentaBancaria o : list) {
            l.add(new EntityWrapper<>(o, o.getId(), o.getNumero().toString()));
        }
        return l;
    }

    public static List<EntityWrapper<Dominio>> getWrappedDominios(List<Dominio> list) {
        List<EntityWrapper<Dominio>> l = new ArrayList<>(list.size());
        for (Dominio o : list) {
            l.add(new EntityWrapper<>(o, o.getId(), o.getNombre()));
        }
        return l;
    }

    public static List<EntityWrapper<OperacionesBancarias>> getWrappedOperacionesBancarias(List<OperacionesBancarias> list) {
        List<EntityWrapper<OperacionesBancarias>> l = new ArrayList<>(list.size());
        for (OperacionesBancarias o : list) {
            l.add(new EntityWrapper<>(o, o.getId(), o.getNombre()));
        }
        return l;
    }

    public static List<EntityWrapper<Banco>> getWrappedBancos(final List<Banco> list) {
        List<EntityWrapper<Banco>> l = new ArrayList<>(list.size());
        for (Banco o : list) {
            l.add(new EntityWrapper<>(o, o.getId(), o.getNombre()));
        }
        return l;
    }

    public static List<EntityWrapper<Caja>> getWrappedCajas(List<Caja> list) {
        List<EntityWrapper<Caja>> l = new ArrayList<>(list.size());
        for (Caja o : list) {
            l.add(new EntityWrapper<>(o, o.getId(), o.getNombre()));
        }
        return l;
    }

    /**
     * Si es Factura: <br> "F" + TipoFactura + #### (punto de venta) + ######## (número). <br>EJ:
     * FA0001-00001234 <br>Si es Interno: <br>"FI" + #### (punto de venta) + ######## (N° Movimiento
     * interno)
     *
     * @param o
     * @return
     */
    public static String getNumeracion(FacturaVenta o) {
        if (String.valueOf(o.getTipo()).equalsIgnoreCase("I")) {
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
     * Si es Factura: <br> "F"+ TipoFactura + #### (punto de venta) + ######## (número). <br>EJ:
     * FA0001-00001234 <br>Si es Interno: <br>"FI" + #### (punto de venta DONDE SER REALIZO LA
     * CARGA) + ######## (N° Movimiento interno)
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

    public static String getNumeracion(RemitoCompra o, boolean conGuion) {
        String guion = conGuion ? "-" : "";
        return UTIL.AGREGAR_CEROS(o.getSucursal().getPuntoVenta(), 4) + guion + UTIL.AGREGAR_CEROS(o.getNumero(), 8);
    }

    public static String getNumeracion(Recibo o, boolean conGuion) {
        String guion = conGuion ? "-" : "";
        return "R" + o.getTipo() + UTIL.AGREGAR_CEROS(o.getSucursal().getPuntoVenta(), 4) + guion + UTIL.AGREGAR_CEROS(o.getNumero(), 8);
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

    public static EntityWrapper<Banco> wrap(Banco o) {
        return new EntityWrapper<>(o, o.getId(), o.getNombre());
    }

    public static EntityWrapper<CuentaBancaria> wrap(CuentaBancaria o) {
        return new EntityWrapper<>(o, o.getId(), o.getNumero());
    }

    public static List<EntityWrapper<Vendedor>> getWrappedVendedor(List<Vendedor> list) {
        List<EntityWrapper<Vendedor>> l = new ArrayList<>(list.size());
        for (Vendedor o : list) {
            l.add(new EntityWrapper<>(o, o.getId(), o.getApellido() + " " + o.getNombre()));
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

    public static List<EntityWrapper<Iva>> getWrappedIva(List<Iva> list) {
        List<EntityWrapper<Iva>> l = new ArrayList<>(list.size());
        for (Iva iva : list) {
            l.add(new EntityWrapper<>(iva, iva.getId(), iva.getIva().toString() + "%"));
        }
        return l;
    }
}
