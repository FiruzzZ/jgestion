package jgestion.controller.exceptions;

import java.sql.SQLException;
import java.util.Objects;

/**
 * Cuando existan referencias al registro que se intenta borrar
 * <br>Wrapper independiente de la implementación (ORM)
 *
 * @author FiruzzZ
 */
public class ConstraintViolationJpaException extends RuntimeException {

    private static final long serialVersionUID = 1111111111111111L;

    private final String frontEndMesssage;
    private final SQLException sqlException;
    private final String sql;
    private final String constraintName;

    public ConstraintViolationJpaException(String frontEndMesssage, SQLException sqlException, String sql, String constraintName) {
        Objects.requireNonNull(frontEndMesssage);
        this.frontEndMesssage = frontEndMesssage;
        this.sqlException = sqlException;
        this.sql = sql;
        this.constraintName = constraintName;
    }

    @Override
    public String getMessage() {
        return frontEndMesssage;
    }

    public String getFrontEndMesssage() {
        return getMessage();
    }

    public SQLException getSqlException() {
        return sqlException;
    }

    public String getSql() {
        return sql;
    }

    public String getConstraintName() {
        return constraintName;
    }
}
