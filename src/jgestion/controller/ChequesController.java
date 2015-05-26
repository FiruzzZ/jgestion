package jgestion.controller;

import jgestion.controller.exceptions.MessageException;
import jgestion.entity.Cheque;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Administrador
 */
public class ChequesController {

    static void checkUniquenessOnTable(DefaultTableModel dtm, Cheque cheque) throws MessageException {
        for (int row = 0; row < dtm.getRowCount(); row++) {
            Object obj = dtm.getValueAt(row, 0);
            if (obj instanceof Cheque) {
                Cheque old = (Cheque) obj;
                System.out.println(old.toString());
                System.out.println(cheque.toString());
//                if (old.equals(cheque)) {
                if (old.getNumero().equals(cheque.getNumero()) && old.getBanco().equals(cheque.getBanco())) {
                    throw new MessageException("Ya se agrego el Cheque N°" + old.getNumero() + " del Banco " + old.getBanco().getNombre());
                }
            }
        }
    }
}
