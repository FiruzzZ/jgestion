/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jgestion.controller.exceptions;

import org.eclipse.persistence.exceptions.DatabaseException;

/**
 *
 * @author Administrador
 */
public class DatabaseErrorException extends Exception {

   public DatabaseErrorException() {
      super("Error de conexión con la Base de datos");
   }

   public DatabaseErrorException(DatabaseException e) {
      super("Error de conexión con la Base de datos", e);
   }
   

}
