/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jgestion;

import controller.CuentaController;
import controller.UsuarioHelper;
import entity.Cuenta;
import entity.SubCuenta;
import entity.Sucursal;
import entity.UnidadDeNegocio;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.JComboBox;
import jpa.controller.UnidadDeNegocioJpaController;
import utilities.general.UTIL;
import utilities.swing.components.ComboBoxWrapper;

/**
 *
 * @author FiruzzZ
 */
public class ActionListenerManager {

    public static void setCuentaSubcuentaActionListener(final JComboBox cuenta, boolean cuentaElegible, final JComboBox subCuenta, final boolean subCuentaElegible, boolean loadSubCuenta) {
        UTIL.loadComboBox(cuenta, JGestionUtils.getWrappedCuentas(new CuentaController().findAll()), cuentaElegible);
        cuenta.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object o = cuenta.getSelectedItem();
                if (o instanceof ComboBoxWrapper) {
                    @SuppressWarnings("unchecked")
                    ComboBoxWrapper<Cuenta> c = (ComboBoxWrapper<Cuenta>) o;
                    List<SubCuenta> l = c.getEntity().getSubCuentas();
                    UTIL.loadComboBox(subCuenta, JGestionUtils.getWrappedSubCuentas(l), subCuentaElegible);
                } else {
                    UTIL.loadComboBox(subCuenta, null, subCuentaElegible);
                }
            }
        });
        if (loadSubCuenta) {
            Object o = cuenta.getSelectedItem();
            if (o instanceof ComboBoxWrapper) {
                @SuppressWarnings("unchecked")
                ComboBoxWrapper<Cuenta> c = (ComboBoxWrapper<Cuenta>) o;
                List<SubCuenta> l = c.getEntity().getSubCuentas();
                UTIL.loadComboBox(subCuenta, JGestionUtils.getWrappedSubCuentas(l), subCuentaElegible);
            }
        }
    }

    public static void setUnidadDeNegocioSucursalActionListener(final JComboBox cbUnidadDeNegocio, boolean unidadElegible, final JComboBox cbSucursales, final boolean sucursalElegible, boolean loadSucursal) {
        UTIL.loadComboBox(cbUnidadDeNegocio, JGestionUtils.getWrappedUnidadDeNegocios(new UnidadDeNegocioJpaController().findBySucursalesPermitidas()), unidadElegible);
        cbUnidadDeNegocio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object o = cbUnidadDeNegocio.getSelectedItem();
                if (o instanceof ComboBoxWrapper) {
                    @SuppressWarnings("unchecked")
                    ComboBoxWrapper<UnidadDeNegocio> c = (ComboBoxWrapper<UnidadDeNegocio>) o;
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
            if (o instanceof ComboBoxWrapper) {
                @SuppressWarnings("unchecked")
                ComboBoxWrapper<UnidadDeNegocio> c = (ComboBoxWrapper<UnidadDeNegocio>) o;
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
