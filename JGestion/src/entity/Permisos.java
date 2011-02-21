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
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "permisos")
@NamedQueries({
   @NamedQuery(name = "Permisos.findAll", query = "SELECT p FROM Permisos p"),
   @NamedQuery(name = "Permisos.findById", query = "SELECT p FROM Permisos p WHERE p.id = :id")
})
public class Permisos implements Serializable {

   private static final long serialVersionUID = 1L;
   @Id
   @Basic(optional = false)
   @Column(name = "id", nullable = false)
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer id;
   @Basic(optional = false)
   @Column(name = "abm_productos", nullable = false)
   private boolean abmProductos;
   @Basic(optional = false)
   @Column(name = "abm_proveedores", nullable = false)
   private boolean abmProveedores;
   @Basic(optional = false)
   @Column(name = "abm_clientes", nullable = false)
   private boolean abmClientes;
   @Basic(optional = false)
   @Column(name = "abm_cajas", nullable = false)
   private boolean abmCajas;
   @Basic(optional = false)
   @Column(name = "abm_usuarios", nullable = false)
   private boolean abmUsuarios;
   @Basic(optional = false)
   @Column(name = "abm_lista_precios", nullable = false)
   private boolean abmListaPrecios;
   @Basic(optional = false)
   @Column(name = "tesoreria", nullable = false)
   private boolean tesoreria;
   @Basic(optional = false)
   @Column(name = "datos_general", nullable = false)
   private boolean datosGeneral;
   @Basic(optional = false)
   @Column(name = "venta", nullable = false)
   private boolean venta;
   @Basic(optional = false)
   @Column(name = "compra", nullable = false)
   private boolean compra;
   @Basic(optional = false)
   @Column(name = "cerrar_cajas", nullable = false)
   private boolean cerrarCajas;
   @Basic(optional = false)
   @Column(name = "abm_catalogoweb", nullable = false)
   private boolean abmCatalogoweb;
   @Basic(optional = false)
   @Column(name = "abm_ofertasweb", nullable = false)
   private boolean abmOfertasweb;
   @OneToOne(mappedBy = "permisos")
   private Usuario usuario;

   public Permisos() {
   }

   public Permisos(Integer id) {
      this.id = id;
   }

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public boolean getAbmProductos() {
      return abmProductos;
   }

   public void setAbmProductos(boolean abmProductos) {
      this.abmProductos = abmProductos;
   }

   public boolean getAbmProveedores() {
      return abmProveedores;
   }

   public void setAbmProveedores(boolean abmProveedores) {
      this.abmProveedores = abmProveedores;
   }

   public boolean getAbmClientes() {
      return abmClientes;
   }

   public void setAbmClientes(boolean abmClientes) {
      this.abmClientes = abmClientes;
   }

   public boolean getAbmCajas() {
      return abmCajas;
   }

   public void setAbmCajas(boolean abmCajas) {
      this.abmCajas = abmCajas;
   }

   public boolean getAbmUsuarios() {
      return abmUsuarios;
   }

   public void setAbmUsuarios(boolean abmUsuarios) {
      this.abmUsuarios = abmUsuarios;
   }

   public boolean getAbmListaPrecios() {
      return abmListaPrecios;
   }

   public void setAbmListaPrecios(boolean abmListaPrecios) {
      this.abmListaPrecios = abmListaPrecios;
   }

   public boolean getTesoreria() {
      return tesoreria;
   }

   public void setTesoreria(boolean tesoreria) {
      this.tesoreria = tesoreria;
   }

   public boolean getDatosGeneral() {
      return datosGeneral;
   }

   public void setDatosGeneral(boolean datosGeneral) {
      this.datosGeneral = datosGeneral;
   }

   public boolean getVenta() {
      return venta;
   }

   public void setVenta(boolean venta) {
      this.venta = venta;
   }

   public boolean getCompra() {
      return compra;
   }

   public void setCompra(boolean compra) {
      this.compra = compra;
   }

   public boolean getCerrarCajas() {
      return cerrarCajas;
   }

   public void setCerrarCajas(boolean cerrarCajas) {
      this.cerrarCajas = cerrarCajas;
   }

   public boolean getAbmCatalogoweb() {
      return abmCatalogoweb;
   }

   public boolean getAbmOfertasweb() {
      return abmOfertasweb;
   }

   public void setAbmCatalogoweb(boolean abmCatalogoweb) {
      this.abmCatalogoweb = abmCatalogoweb;
   }

   public void setAbmOfertasweb(boolean abmOfertasweb) {
      this.abmOfertasweb = abmOfertasweb;
   }

   /**
    * Usuario al que pertenece estos Permisos
    * @return a entity Usuario.
    */
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
      if (!(object instanceof Permisos)) {
         return false;
      }
      Permisos other = (Permisos) object;
      if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      return "entity.Permisos[id=" + id + "]";
   }
}
