
package jgestion.entity;

import java.io.Serializable;
import javax.persistence.*;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "permisos_caja", uniqueConstraints = {
   @UniqueConstraint(columnNames = {"usuario", "caja"})})
@NamedQueries({
   @NamedQuery(name = "PermisosCaja.findAll", query = "SELECT p FROM PermisosCaja p"),
   @NamedQuery(name = "PermisosCaja.findById", query = "SELECT p FROM PermisosCaja p WHERE p.id = :id")})
public class PermisosCaja implements Serializable {
   private static final long serialVersionUID = 1L;
   @Id
   @Basic(optional = false)
   @Column(name = "id", nullable = false)
   @GeneratedValue(strategy= GenerationType.IDENTITY)
   private Integer id;
   @JoinColumn(name = "caja", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private Caja caja;
   @JoinColumn(name = "usuario", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private Usuario usuario;

   public PermisosCaja() {
   }

   public PermisosCaja(Integer id) {
      this.id = id;
   }

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public Caja getCaja() {
      return caja;
   }

   public void setCaja(Caja caja) {
      this.caja = caja;
   }

   public Usuario getUsuario() {
      return usuario;
   }

   public void setUsuario(Usuario usuario) {
      this.usuario = usuario;
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
      if (!(object instanceof PermisosCaja)) {
         return false;
      }
      PermisosCaja other = (PermisosCaja) object;
      if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      return "entity.PermisosCaja[id=" + id + "]";
   }

}
