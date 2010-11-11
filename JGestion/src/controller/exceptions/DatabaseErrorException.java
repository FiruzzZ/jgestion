/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package controller.exceptions;

/**
 *
 * @author Administrador
 */
public class DatabaseErrorException extends Exception {

   public DatabaseErrorException() {
      super("Error de conexión con la Base de datos");
   }
   

}
