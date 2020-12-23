package jgestion.jpa.controller;

import jgestion.entity.Configuracion;

/**
 *
 * @author FiruzzZ
 */
public class ConfiguracionDAO extends JGestionJpaImpl<Configuracion, String> {

    public ConfiguracionDAO() {
    }

    public boolean isAFIPWSProduction() {
        Configuracion cfg = find("afip_ws_production");
        return Boolean.valueOf(cfg.getValue());
    }

    public boolean isPermitidoStockNegativo() {
        Configuracion cfg = find("permitir_stock_negativo");
        return Boolean.valueOf(cfg.getValue());
    }

    public String getAFIPClave() {
        return find("afip_fe_pkcs12").getValue();
    }

    public void saveAFIPClave(String pwd) {
        Configuracion c = find("afip_fe_pkcs12");
        c.setValue(pwd);
        merge(c);
    }

    public String getAFIPWSTicketAccess() {
        return find("afip_fe_ticket").getValue();
    }

    /**
     * Guarda en el atributo, el XML del ticket access (TA) del WS
     *
     * @param token
     */
    public void updateAFIPWSTicketAccess(String token) {
        Configuracion c = find("afip_fe_ticket");
        c.setValue(token);
        merge(c);
    }

    public int getCantidadDecimales() {
        return Integer.valueOf(find("cantidad_decimales").getValue());
    }
}
