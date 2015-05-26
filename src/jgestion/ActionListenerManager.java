/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jgestion;

import jgestion.controller.CuentaController;
import jgestion.controller.UsuarioHelper;
import jgestion.entity.Cuenta;
import jgestion.entity.SubCuenta;
import jgestion.entity.Sucursal;
import jgestion.entity.UnidadDeNegocio;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.JComboBox;
import jgestion.jpa.controller.UnidadDeNegocioJpaController;
import utilities.general.UTIL;
import utilities.general.EntityWrapper;

/**
 *
 * @author FiruzzZ
 */
public class ActionListenerManager {

    public static void setCuentasIESubcuentaActionListener(final JComboBox cuenta, boolean cuentaElegible, final JComboBox subCuenta, final boolean subCuentaElegible, boolean loadSubCuenta) {
        UTIL.loadComboBox(cuenta, JGestionUtils.getWrappedCuentas(new CuentaController().findAll()), cuentaElegible);
        cuenta.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object o = cuenta.getSelectedItem();
                if (o instanceof EntityWrapper) {
                    @SuppressWarnings("unchecked")
                    EntityWrapper<Cuenta> c = (EntityWrapper<Cuenta>) o;
                    List<SubCuenta> l = c.getEntity().getSubCuentas();
                    UTIL.loadComboBox(subCuenta, JGestionUtils.getWrappedSubCuentas(l), subCuentaElegible);
                } else {
                    UTIL.loadComboBox(subCuenta, null, "<Elegir Cuenta>");
                }
            }
        });
        if (loadSubCuenta) {
            Object o = cuenta.getSelectedItem();
            if (o instanceof EntityWrapper) {
                @SuppressWarnings("unchecked")
                EntityWrapper<Cuenta> c = (EntityWrapper<Cuenta>) o;
                List<SubCuenta> l = c.getEntity().getSubCuentas();
                UTIL.loadComboBox(subCuenta, JGestionUtils.getWrappedSubCuentas(l), subCuentaElegible);
            } else {
                UTIL.loadComboBox(subCuenta, null, "<Elegir Cuenta>");
            }
        }
    }

    public static void setCuentasIngresosSubcuentaActionListener(final JComboBox cuentasIngreso, boolean cuentaElegible, final JComboBox subCuenta, final boolean subCuentaElegible, boolean loadSubCuenta) {
        UTIL.loadComboBox(cuentasIngreso, JGestionUtils.getWrappedCuentas(new CuentaController().findByTipo(true)), cuentaElegible);
        cuentasIngreso.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object o = cuentasIngreso.getSelectedItem();
                if (o instanceof EntityWrapper) {
                    @SuppressWarnings("unchecked")
                    EntityWrapper<Cuenta> c = (EntityWrapper<Cuenta>) o;
                    List<SubCuenta> l = c.getEntity().getSubCuentas();
                    UTIL.loadComboBox(subCuenta, JGestionUtils.getWrappedSubCuentas(l), subCuentaElegible);
                } else {
                    UTIL.loadComboBox(subCuenta, null, "<Elegir Cuenta>");
                }
            }
        });
        if (loadSubCuenta) {
            Object o = cuentasIngreso.getSelectedItem();
            if (o instanceof EntityWrapper) {
                @SuppressWarnings("unchecked")
                EntityWrapper<Cuenta> c = (EntityWrapper<Cuenta>) o;
                List<SubCuenta> l = c.getEntity().getSubCuentas();
                UTIL.loadComboBox(subCuenta, JGestionUtils.getWrappedSubCuentas(l), subCuentaElegible);
            } else {
                UTIL.loadComboBox(subCuenta, null, "<Elegir Cuenta>");
            }
        }
    }

    public static void setCuentasEgresosSubcuentaActionListener(final JComboBox cuentasEgresos, boolean cuentaElegible, final JComboBox subCuenta, final boolean subCuentaElegible, boolean loadSubCuenta) {
        UTIL.loadComboBox(cuentasEgresos, JGestionUtils.getWrappedCuentas(new CuentaController().findByTipo(false)), cuentaElegible);
        cuentasEgresos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object o = cuentasEgresos.getSelectedItem();
                if (o instanceof EntityWrapper) {
                    @SuppressWarnings("unchecked")
                    EntityWrapper<Cuenta> c = (EntityWrapper<Cuenta>) o;
                    List<SubCuenta> l = c.getEntity().getSubCuentas();
                    UTIL.loadComboBox(subCuenta, JGestionUtils.getWrappedSubCuentas(l), subCuentaElegible);
                } else {
                    UTIL.loadComboBox(subCuenta, null, "<Elegir Cuenta>");
                }
            }
        });
        if (loadSubCuenta) {
            Object o = cuentasEgresos.getSelectedItem();
            if (o instanceof EntityWrapper) {
                @SuppressWarnings("unchecked")
                EntityWrapper<Cuenta> c = (EntityWrapper<Cuenta>) o;
                List<SubCuenta> l = c.getEntity().getSubCuentas();
                UTIL.loadComboBox(subCuenta, JGestionUtils.getWrappedSubCuentas(l), subCuentaElegible);
            } else {
                UTIL.loadComboBox(subCuenta, null, "<Elegir Cuenta>");
            }
        }
    }

    public static void setUnidadDeNegocioSucursalActionListener(final JComboBox cbUnidadDeNegocio, boolean unidadElegible, final JComboBox cbSucursales, final boolean sucursalElegible, boolean loadSucursal) {
        List<UnidadDeNegocio> all = new UnidadDeNegocioJpaController().findAll();
        List<UnidadDeNegocio> unidades = new ArrayList<UnidadDeNegocio>(all.size());
        for (UnidadDeNegocio candidata : all) {
            Set<Sucursal> uni = candidata.getSucursales();
            List<Sucursal> permitidas = new UsuarioHelper().getSucursales();
            for (Sucursal sucursal : permitidas) {
                if (uni.contains(sucursal)) {
                    if (!unidades.contains(candidata)) {
                        unidades.add(candidata);
                        continue;
                    }
                }
            }
        }
        UTIL.loadComboBox(cbUnidadDeNegocio, JGestionUtils.getWrappedUnidadDeNegocios(unidades), unidadElegible);
        cbUnidadDeNegocio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object o = cbUnidadDeNegocio.getSelectedItem();
                if (o instanceof EntityWrapper) {
                    @SuppressWarnings("unchecked")
                    EntityWrapper<UnidadDeNegocio> c = (EntityWrapper<UnidadDeNegocio>) o;
                    Set<Sucursal> uni = c.getEntity().getSucursales();
                    List<Sucursal> permitidas = new UsuarioHelper().getSucursales();
                    List<Sucursal> x = new ArrayList<Sucursal>();
                    for (Sucursal sucursal : permitidas) {
                        if (uni.contains(sucursal)) {
                            x.add(sucursal);
                        }
                    }
                    UTIL.loadComboBox(cbSucursales, JGestionUtils.getWrappedSucursales(x), sucursalElegible);
                } else {
                    UTIL.loadComboBox(cbSucursales, null, sucursalElegible, "<Elegir Unidad de Negocios>");
                    cbSucursales.setSelectedIndex(0);
                }
            }
        });
        if (loadSucursal) {
            Object o = cbUnidadDeNegocio.getSelectedItem();
            if (o instanceof EntityWrapper) {
                @SuppressWarnings("unchecked")
                EntityWrapper<UnidadDeNegocio> c = (EntityWrapper<UnidadDeNegocio>) o;
                Set<Sucursal> uni = c.getEntity().getSucursales();
                List<Sucursal> permitidas = new UsuarioHelper().getSucursales();
                List<Sucursal> x = new ArrayList<Sucursal>();
                for (Sucursal sucursal : permitidas) {
                    if (uni.contains(sucursal)) {
                        x.add(sucursal);
                    }
                }
                UTIL.loadComboBox(cbSucursales, JGestionUtils.getWrappedSucursales(x), sucursalElegible);
            } else {
                UTIL.loadComboBox(cbSucursales, null, sucursalElegible, "<Elegir Unidad de Negocios>");
            }
        }
    }
}
