package entity.enums;

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
     * {@link ChequeTerceros} : Son los recibidos, aún no está
     * {@link ChequeEstado#DEPOSITADO} o cobrados {@link ChequeEstado#CAJA}.
     * <br>{@link ChequePropio} : Los emitidos que aun no fueron debitados
     * efectivamente de las cuentas (no conciliados).
     */
    CARTERA(1),
    /**
     * Fue depositado en una cuenta bancaria.
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
    REEMPLAZADO(7);
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
