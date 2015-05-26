package jgestion.entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "unidadmedida", uniqueConstraints = {
   @UniqueConstraint(columnNames = {"nombre"})
})
@NamedQueries({
   @NamedQuery(name = "Unidadmedida.findAll", query = "SELECT u FROM Unidadmedida u"),
   @NamedQuery(name = "Unidadmedida.findById", query = "SELECT u FROM Unidadmedida u WHERE u.id = :id"),
   @NamedQuery(name = "Unidadmedida.findByNombre", query = "SELECT u FROM Unidadmedida u WHERE u.nombre = :nombre")
})
public class Unidadmedida implements Serializable {

   private static final long serialVersionUID = 1L;
   @Id
   @Basic(optional = false)
   @Column(name = "id", nullable = false)
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer id;
   @Basic(optional = false)
   @Column(name = "nombre", length = 50, nullable = false)
   private String nombre;

   public Unidadmedida() {
   }

   public Unidadmedida(Integer id, String nombre) {
      this.id = id;
      this.nombre = nombre;
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
      if (!(object instanceof Unidadmedida)) {
         return false;
      }
      Unidadmedida other = (Unidadmedida) object;
      if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      return this.getNombre();
   }
}
