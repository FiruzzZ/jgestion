package entity.enums;

import entity.CuentaBancaria;
import entity.Cheque;
import entity.ChequePropio;
import entity.ChequeTerceros;

/**
 * Estados posibles de un {@link Cheque}.
 *
 * @author FiruzzZ
 */
public enum ChequeEstado {

    /**
     * {@link ChequeTerceros} : Son los recibidos, que aún no está
     * {@link #DEPOSITADO} o cobrados
     * {@link #ACREDITADO_EN_CAJA}. <br>{@link ChequePropio} : Los
     * emitidos que aún no fue {@link #DEBITADO} o {@link #ANULADO}
     */
    CARTERA(1),
    /**
     * Estado exclusivo de {@link ChequeTerceros}, fue depositado en una
     * {@link CuentaBancaria}
     */
    DEPOSITADO(2),
    /**
     * Estado exclusivo de {@link ChequePropio}, e indica que fue debitado
     * (cobrado).
     */
    DEBITADO(3),
    /**
     * Pasó a formar parte de una Caja (se hizo dinero efectivo).
     */
    ACREDITADO_EN_CAJA(4),
    /**
     * Hace falta explicación?
     */
    ANULADO(5),
    ENVIADO_SUCURSAL(6),
    REEMPLAZADO(7),
    /**
     * Estado exclusivo de {@link ChequePropio}, e indica que fue utilizado como
     * medio de pago.
     */
    ENDOSADO(8),
    /**
     * Exclusivo de {@link ChequeTerceros}, pendiente de ser recuperado, de este estado pueden
     * derivar 2: {@link #REEMPLAZADO} y {@link #ANULADO}
     */
    RECHAZADO(9);
    private final int id;

    private ChequeEstado(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    /**
     *
     * @param id
     * @return
     * @throws IllegalArgumentException if the id doesn't exist
     */
    public static ChequeEstado findById(Integer id) {
        for (ChequeEstado chequeEstado : values()) {
            if (chequeEstado.getId() == id) {
                return chequeEstado;
            }
        }
        throw new IllegalArgumentException("ID ChequeEstado no válido");
    }

    @Override
    public String toString() {
        return this.name().replaceAll("_", " ");
    }
}
