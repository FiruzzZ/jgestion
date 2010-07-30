package entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "usuario", uniqueConstraints = {
   @UniqueConstraint(columnNames = {"nick"})})
@NamedQueries({
   @NamedQuery(name = "Usuario.findAll", query = "SELECT u FROM Usuario u ORDER BY u.nick"),
   @NamedQuery(name = "Usuario.findById", query = "SELECT u FROM Usuario u WHERE u.id = :id"),
   @NamedQuery(name = "Usuario.findByNick", query = "SELECT u FROM Usuario u WHERE u.nick = :nick"),
   @NamedQuery(name = "Usuario.findByEstado", query = "SELECT u FROM Usuario u WHERE u.estado = :estado")
})
public class Usuario implements Serializable {

   private static final long serialVersionUID = 1L;
   @Id
   @Basic(optional = false)
   @Column(name = "id", nullable = false)
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer id;
   @Basic(optional = false)
   @Column(name = "nick", nullable = false, length = 2147483647)
   private String nick;
   @Basic(optional = false)
   @Column(name = "pass", nullable = false, length = 2147483647)
   private String pass;
   @Basic(optional = false)
   @Column(name = "estado", nullable = false)
   private int estado;
//    @Basic(optional = true)
   @Column(name = "fechaalta", nullable = false)
   @Temporal(TemporalType.DATE)
   private Date fechaalta;
   @JoinColumn(name = "permisos", referencedColumnName = "id")
   @OneToOne
   private Permisos permisos;
   @OneToMany(cascade = CascadeType.ALL, mappedBy = "usuario")
   private List<FacturaVenta> facturaVentaList;
   @OneToMany(mappedBy = "usuario")
   private List<Stock> stockList;
   @OneToMany(cascade = CascadeType.ALL, mappedBy = "usuario")
   private List<FacturaCompra> facturaCompraList;
   @OneToMany(cascade = CascadeType.ALL, mappedBy = "usuario")
   private List<PermisosCaja> permisosCajaList;

   public Usuario() {
   }

   public Usuario(Integer id) {
      this.id = id;
   }

   public Usuario(Integer id, String nick, String pass, int estado, Date fechaalta, Permisos permisos) {
      this.id = id;
      this.nick = nick;
      this.pass = pass;
      this.estado = estado;
      this.fechaalta = fechaalta;
      this.permisos = permisos;
   }

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public String getNick() {
      return nick;
   }

   public void setNick(String nick) {
      this.nick = nick;
   }

   public String getPass() {
      return pass;
   }

   public void setPass(String pass) {
      this.pass = pass;
   }

   public int getEstado() {
      return estado;
   }

   public void setEstado(int estado) {
      this.estado = estado;
   }

   public Date getFechaalta() {
      return fechaalta;
   }

   public void setFechaalta(Date fechaalta) {
      this.fechaalta = fechaalta;
   }

   public Permisos getPermisos() {
      return permisos;
   }

   public void setPermisos(Permisos permisos) {
      this.permisos = permisos;
   }

   public List<FacturaVenta> getFacturaVentaList() {
      return facturaVentaList;
   }

   public void setFacturaVentaList(List<FacturaVenta> facturaVentaList) {
      this.facturaVentaList = facturaVentaList;
   }

   public List<Stock> getStockList() {
      return stockList;
   }

   public void setStockList(List<Stock> stockList) {
      this.stockList = stockList;
   }

   public List<FacturaCompra> getFacturaCompraList() {
      return facturaCompraList;
   }

   public void setFacturaCompraList(List<FacturaCompra> facturaCompraList) {
      this.facturaCompraList = facturaCompraList;
   }

   public List<PermisosCaja> getPermisosCajaList() {
      return permisosCajaList;
   }

   public void setPermisosCajaList(List<PermisosCaja> permisosCajaList) {
      this.permisosCajaList = permisosCajaList;
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
      if (!(object instanceof Usuario)) {
         return false;
      }
      Usuario other = (Usuario) object;
      if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      return this.getNick();
   }
}
