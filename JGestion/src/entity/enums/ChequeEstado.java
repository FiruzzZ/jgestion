/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package entity.enums;

import entity.ChequePropio;
import entity.ChequeTerceros;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Estados posibles de un cheque.
 * @author FiruzzZ
 */
//@Entity
//@Table(name = "cheque_estado", uniqueConstraints = {
//    @UniqueConstraint(columnNames = {"nombre"})})
public enum ChequeEstado {

    /**
     * Estado exclusivo de {@link ChequePropio}, e indica que fue debitado (cobrado).
     */
    DEBIDATO(1),
    /**
     * {@link ChequeTerceros} : Son los recibidos, aún no está {@link ChequeEstado#DEPOSITADO}
     * o cobrados {@link ChequeEstado#CAJA}.
     * <br>{@link ChequePropio} : Los emitidos (LIBRADOS) que aun no fueron debitados 
     * efectivamente de las cuentas (no conciliados).
     */
    CARTERA(2),
    /**
     * Fue depositado en una cuenta bancaria.
     */
    DEPOSITADO(3),
    /**
     * Pasó a formar parte de una Caja (se hizo dinero efectivo).
     */
    CAJA(4),
    /**
     * Hace falta explicación?
     */
    RECHAZADO(5);
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
}
