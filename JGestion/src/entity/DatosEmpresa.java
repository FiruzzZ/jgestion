package entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "datos_empresa", uniqueConstraints = {
   @UniqueConstraint(columnNames = {"nombre"})})
@NamedQueries({
   @NamedQuery(name = "DatosEmpresa.findAll", query = "SELECT d FROM DatosEmpresa d"),
   @NamedQuery(name = "DatosEmpresa.findById", query = "SELECT d FROM DatosEmpresa d WHERE d.id = :id")
})
public class DatosEmpresa implements Serializable {

   private static final long serialVersionUID = 1L;
   @Id
   @Basic(optional = false)
   @Column(name = "id", nullable = false)
   private Integer id;
   @Basic(optional = false)
   @Column(name = "nombre", nullable = false, length = 214)
   private String nombre;
   @Basic(optional = false)
   @Column(name = "cuit", nullable = false)
   private long cuit;
   @Basic(optional = false)
   @Column(name = "direccion", nullable = false, length = 214)
   private String direccion;
   @Column(name = "encargado", length = 214)
   private String encargado;
   @Column(name = "cta_cte")
   private Long ctaCte;
   @Column(name = "email", length = 214)
   private String email;
   @Column(name = "web_page", length = 214)
   private String webPage;
   @Basic(optional = false)
   @Column(name = "tele1", nullable = false)
   private long tele1;
   @Column(name = "tele2")
   private Long tele2;
   @Basic(optional = false)
//   @Lob
   @Column(name = "logo")
   private byte[] logo;
   @Basic(optional = false)
   @Column(name = "fecha_inicio_actividad", nullable = false)
   @Temporal(TemporalType.DATE)
   private Date fechaInicioActividad;

   public DatosEmpresa() {
   }

   public DatosEmpresa(Integer id) {
      this.id = id;
   }

   //NO BORRAR!!!
   public DatosEmpresa(Integer id, String nombre, long cuit, String direccion, long tele1, java.util.Date fechaInicioActividad) {
      this.id = id;
      this.nombre = nombre;
      this.cuit = cuit;
      this.direccion = direccion;
      this.tele1 = tele1;
      this.fechaInicioActividad = fechaInicioActividad;
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

   public long getCuit() {
      return cuit;
   }

   public void setCuit(long cuit) {
      this.cuit = cuit;
   }

   public String getDireccion() {
      return direccion;
   }

   public void setDireccion(String direccion) {
      this.direccion = direccion;
   }

   public String getEncargado() {
      return encargado;
   }

   public void setEncargado(String encargado) {
      this.encargado = encargado;
   }

   public Long getCtaCte() {
      return ctaCte;
   }

   public void setCtaCte(Long ctaCte) {
      this.ctaCte = ctaCte;
   }

   public String getEmail() {
      return email;
   }

   public void setEmail(String email) {
      this.email = email;
   }

   public String getWebPage() {
      return webPage;
   }

   public void setWebPage(String webPage) {
      this.webPage = webPage;
   }

   public Long getTele1() {
      return tele1;
   }

   public void setTele1(long tele1) {
      this.tele1 = tele1;
   }

   public Long getTele2() {
      return tele2;
   }

   public void setTele2(Long tele2) {
      this.tele2 = tele2;
   }

   public byte[] getLogo() {
      return logo;
   }

   public void setLogo(byte[] logo) {
      this.logo = logo;
   }

   public Date getFechaInicioActividad() {
      return fechaInicioActividad;
   }

   public void setFechaInicioActividad(Date fechaInicioActividad) {
      this.fechaInicioActividad = fechaInicioActividad;
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
      if (!(object instanceof DatosEmpresa)) {
         return false;
      }
      DatosEmpresa other = (DatosEmpresa) object;
      if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      return "entity.DatosEmpresa[id=" + id + "]";
   }
}
