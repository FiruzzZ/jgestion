package jgestion.controller;

import java.util.Arrays;
import java.util.List;

/**
 * Clase contenedora de Enums y variables genéricas (estados de cuentas, tipos
 * de transacción).
 *
 * @author FiruzzZ
 */
public abstract class Valores {

    /**
     * Formas de pago usadas en operaciones Venta y Compras
     */
    public enum FormaPago {

        CONTADO(1, "CONTADO"), CTA_CTE(2, "CTA CTE");//, CHEQUE(3, "CHEQUE"), CONTADO_CHEQUE(4, "CONTADO/CHEQUE");
        private final int id;
        private final String nombre;

        private FormaPago(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public static FormaPago find(int formaPagoID) {
            for (FormaPago formaPago : FormaPago.values()) {
                if (formaPago.getId() == formaPagoID) {
                    return formaPago;
                }
            }
            return null;
        }

        public int getId() {
            return this.id;
        }

        public String getNombre() {
            return this.nombre;
        }

        public static List<FormaPago> getFormasDePago() {
            return Arrays.asList(FormaPago.values());
        }

        @Override
        public String toString() {
            return this.nombre;
        }
    }

    /**
     * Estados posibles de una CtaCte (Proveedor o Cliente)
     */
    public enum CtaCteEstado {

        PENDIENTE(1, "PENDIENTE"), PAGADA(2, "PAGADA"), ANULADA(3, "ANULADA");
        private final short id;
        private final String nombre;

        private CtaCteEstado(int id, String nombre) {
            this.id = (short) id;
            this.nombre = nombre;
        }

        public short getId() {
            return this.id;
        }

        public static CtaCteEstado find(int id) {
            for (CtaCteEstado ctaCteEstado : CtaCteEstado.values()) {
                if (ctaCteEstado.getId() == id) {
                    return ctaCteEstado;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return this.nombre;
        }
    }
}
