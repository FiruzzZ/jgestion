package controller;

import controller.exceptions.MessageException;
import entity.Cheque;
import entity.enums.ChequeEstado;
import java.sql.Connection;
import java.sql.SQLException;
import javax.persistence.EntityManager;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Logger;

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

    public ChequesController() throws SQLException {
        checkDefaultData();
    }

    private void checkDefaultData() throws SQLException {
        EntityManager em = DAO.getEntityManager();
        Connection con = DAO.getJDBCConnection();
        try {
            con.createStatement().execute(DDL_SQL_ENTITY);
            con.commit();
        } catch (SQLException sQLException) {
            //ignored..
//            Logger.getLogger(this.getClass()).trace("Ejecutando DDL SQL para la tabla cheque_estado MSG:" + sQLException.getMessage());
        }
        con.close();
        //check if tabla is already loaded...
        if (em.createNativeQuery("SELECT * FROM cheque_estado").getResultList().isEmpty()) {
            StringBuilder sb;
            //fill with Enum's
            Logger.getLogger(this.getClass()).info("Filling table cheque_estado..");
            con = DAO.getJDBCConnection();
            for (ChequeEstado chequeEstado : ChequeEstado.values()) {
                sb = new StringBuilder("INSERT INTO cheque_estado VALUES(");
                sb.append(chequeEstado.getId()).append(",'").append(chequeEstado.toString()).append("')");
                con.createStatement().execute(sb.toString());
            }
            con.commit();
        }
    }
    private static final String DDL_SQL_ENTITY = "CREATE TABLE cheque_estado ("
            + "id integer NOT NULL,"
            + "nombre character varying(20) NOT NULL,"
            + "CONSTRAINT cheque_estado_pkey PRIMARY KEY (id),"
            + "CONSTRAINT cheque_estado_nombre_key UNIQUE (nombre)"
            + ") WITH ( OIDS=FALSE);";
}
