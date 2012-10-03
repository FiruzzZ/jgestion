package entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "movimiento_concepto", uniqueConstraints = {
   @UniqueConstraint(columnNames = {"nombre"})})
@XmlRootElement
@NamedQueries({
   @NamedQuery(name = "MovimientoConcepto.findAll", query = "SELECT m FROM MovimientoConcepto m"),
   @NamedQuery(name = "MovimientoConcepto.findById", query = "SELECT m FROM MovimientoConcepto m WHERE m.id = :id"),
   @NamedQuery(name = "MovimientoConcepto.findByNombre", query = "SELECT m FROM MovimientoConcepto m WHERE m.nombre = :nombre")})
public class MovimientoConcepto implements Serializable {

   private static final long serialVersionUID = 1L;
   @Id
   @Basic(optional = false)
   @Column(name = "id", nullable = false)
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer id;
   @Basic(optional = false)
   @Column(name = "nombre", nullable = false, length = 30)
   @OrderBy(value = "nombre")
   private String nombre;

   public MovimientoConcepto() {
   }

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public String getNombre() {
      return nombre;
   }

   public void setNombre(String nombre) {
      this.nombre = nombre;
   }

   @Override
   public int hashCode() {
      int hash = 0;
      hash += (id != null ? id.hashCode() : 0);
      return hash;
   }

   @Override
   public boolean equals(Object object) {
      // TODO: Warning - this method won't work in the case the id fields are not set
      if (!(object instanceof MovimientoConcepto)) {
         return false;
      }
      MovimientoConcepto other = (MovimientoConcepto) object;
      if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      return nombre;
   }
}
