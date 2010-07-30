
package entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "msj_informativos")
@NamedQueries({
   @NamedQuery(name = "MsjInformativos.findAll", query = "SELECT m FROM MsjInformativos m"),
   @NamedQuery(name = "MsjInformativos.findById", query = "SELECT m FROM MsjInformativos m WHERE m.id = :id"),
   @NamedQuery(name = "MsjInformativos.findByAbmCaja", query = "SELECT m FROM MsjInformativos m WHERE m.abmCaja = :abmCaja"),
   @NamedQuery(name = "MsjInformativos.findByAbmCajaMsj", query = "SELECT m FROM MsjInformativos m WHERE m.abmCajaMsj = :abmCajaMsj")})
public class MsjInformativos implements Serializable {
   private static final long serialVersionUID = 1L;
   @Id
   @Basic(optional = false)
   @Column(name = "id", nullable = false)
   private Integer id;
   @Basic(optional = false)
   @Column(name = "abm_caja", nullable = false)
   private boolean abmCaja;
   @Basic(optional = false)
   @Column(name = "abm_caja_msj", nullable = false, length = 2147483647)
   private String abmCajaMsj;

   public MsjInformativos() {
   }

   public MsjInformativos(Integer id) {
      this.id = id;
   }

   public MsjInformativos(Integer id, boolean abmCaja, String abmCajaMsj) {
      this.id = id;
      this.abmCaja = abmCaja;
      this.abmCajaMsj = abmCajaMsj;
   }

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public boolean getAbmCaja() {
      return abmCaja;
   }

   public void setAbmCaja(boolean abmCaja) {
      this.abmCaja = abmCaja;
   }

   public String getAbmCajaMsj() {
      return abmCajaMsj;
   }

   public void setAbmCajaMsj(String abmCajaMsj) {
      this.abmCajaMsj = abmCajaMsj;
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
      if (!(object instanceof MsjInformativos)) {
         return false;
      }
      MsjInformativos other = (MsjInformativos) object;
      if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      return "entity.MsjInformativos[id=" + id + "]";
   }

}
