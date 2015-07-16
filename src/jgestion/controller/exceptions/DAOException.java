package jgestion.controller.exceptions;

/**
 * Para levantar errores desde las operaciones CRUD
 * @author FiruzzZ
 */
public class DAOException extends RuntimeException {

    public DAOException(String message, Throwable cause) {
        super(message, cause);
    }

}
