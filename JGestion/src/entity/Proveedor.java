
package entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

/**
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "proveedor", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"cuit"}),
    @UniqueConstraint(columnNames = {"nombre"}),
    @UniqueConstraint(columnNames = {"codigo"})
})
@NamedQueries({
    @NamedQuery(name = "Proveedor.findAll", query = "SELECT p FROM Proveedor p"),
    @NamedQuery(name = "Proveedor.findById", query = "SELECT p FROM Proveedor p WHERE p.id = :id"),
    @NamedQuery(name = "Proveedor.findByNombre", query = "SELECT p FROM Proveedor p WHERE p.nombre = :nombre"),
    @NamedQuery(name = "Proveedor.findByDireccion", query = "SELECT p FROM Proveedor p WHERE p.direccion = :direccion"),
    @NamedQuery(name = "Proveedor.findBySucursal", query = "SELECT p FROM Proveedor p WHERE p.sucursal = :sucursal"), @NamedQuery(name = "Proveedor.findByTele1", query = "SELECT p FROM Proveedor p WHERE p.tele1 = :tele1"), @NamedQuery(name = "Proveedor.findByTele2", query = "SELECT p FROM Proveedor p WHERE p.tele2 = :tele2"), @NamedQuery(name = "Proveedor.findByEmail", query = "SELECT p FROM Proveedor p WHERE p.email = :email"), @NamedQuery(name = "Proveedor.findByContacto", query = "SELECT p FROM Proveedor p WHERE p.contacto = :contacto"), @NamedQuery(name = "Proveedor.findByCuit", query = "SELECT p FROM Proveedor p WHERE p.cuit = :cuit"), @NamedQuery(name = "Proveedor.findByRetencionDgr", query = "SELECT p FROM Proveedor p WHERE p.retencionDgr = :retencionDgr"), @NamedQuery(name = "Proveedor.findByRetencionIva", query = "SELECT p FROM Proveedor p WHERE p.retencionIva = :retencionIva"), @NamedQuery(name = "Proveedor.findByFechaalta", query = "SELECT p FROM Proveedor p WHERE p.fechaalta = :fechaalta"), @NamedQuery(name = "Proveedor.findByObservacion", query = "SELECT p FROM Proveedor p WHERE p.observacion = :observacion"), @NamedQuery(name = "Proveedor.findByEstado", query = "SELECT p FROM Proveedor p WHERE p.estado = :estado"), @NamedQuery(name = "Proveedor.findByCodigopostal", query = "SELECT p FROM Proveedor p WHERE p.codigopostal = :codigopostal"), @NamedQuery(name = "Proveedor.findByCodigo", query = "SELECT p FROM Proveedor p WHERE p.codigo = :codigo"), @NamedQuery(name = "Proveedor.findByInterno1", query = "SELECT p FROM Proveedor p WHERE p.interno1 = :interno1"), @NamedQuery(name = "Proveedor.findByInterno2", query = "SELECT p FROM Proveedor p WHERE p.interno2 = :interno2")})


public class Proveedor implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String nombre;
    private String direccion;
    private Integer sucursal;
    private Long tele1;
    private Long tele2;
    private String email;
    private String contacto;
    private long cuit;
    private Boolean retencionDgr;
    private Boolean retencionIva;
    private Date fechaalta;
    private String observacion;
    private int estado;
    private Integer codigopostal;
    private String codigo;
    private Integer interno1;
    private Integer interno2;
    private Contribuyente contribuyente;
    private Depto departamento;
    private Municipio municipio;
    private Provincia provincia;
    private Rubro rubro;
    private String webpage;

    public Proveedor() { }

    public Proveedor(Integer id) {
        this.id = id;
    }

    public Proveedor(Integer id, String nombre, String direccion, long cuit, int estado) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.cuit = cuit;
        this.estado = estado;
    }

    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    @Basic(optional = false)
    @Column(name = "nombre", nullable = false, length = 2147483647)
    public String getNombre() {
        return nombre;
    }

    @Basic(optional = false)
    @Column(name = "direccion", nullable = false, length = 2147483647)
    public String getDireccion() {
        return direccion;
    }

    @Column(name = "webpage", length = 50)
    public String getWebpage() {
        return webpage;
    }

    public void setWebpage(String webpage) {
        this.webpage = webpage;
    }


    public void setId(Integer id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    @Column(name = "sucursal")
    public Integer getSucursal() {
        return sucursal;
    }

    public void setSucursal(Integer sucursal) {
        this.sucursal = sucursal;
    }

    @Column(name = "tele1")
    public Long getTele1() {
        return tele1;
    }

    public void setTele1(Long tele1) {
        this.tele1 = tele1;
    }

    @Column(name = "tele2")
    public Long getTele2() {
        return tele2;
    }

    public void setTele2(Long tele2) {
        this.tele2 = tele2;
    }

    @Column(name = "email", length = 2147483647)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Column(name = "contacto", length = 2147483647)
    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    @Basic(optional = false)
    @Column(name = "cuit", nullable = false)
    public long getCuit() {
        return cuit;
    }

    public void setCuit(long cuit) {
        this.cuit = cuit;
    }

    @Column(name = "retencion_dgr")
    public Boolean getRetencionDgr() {
        return retencionDgr;
    }

    public void setRetencionDgr(Boolean retencionDgr) {
        this.retencionDgr = retencionDgr;
    }

    @Column(name = "retencion_iva")
    public Boolean getRetencionIva() {
        return retencionIva;
    }

    public void setRetencionIva(Boolean retencionIva) {
        this.retencionIva = retencionIva;
    }

    @Column(name = "fechaalta")
    @Temporal(value = TemporalType.DATE)
    public Date getFechaalta() {
        return fechaalta;
    }

    public void setFechaalta(Date fechaalta) {
        this.fechaalta = fechaalta;
    }

    @Column(name = "observacion", length = 2147483647)
    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    @Basic(optional = false)
    @Column(name = "estado", nullable = false)
    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    @Column(name = "codigopostal")
    public Integer getCodigopostal() {
        return codigopostal;
    }

    public void setCodigopostal(Integer codigopostal) {
        this.codigopostal = codigopostal;
    }

    @Column(name = "codigo", length = 2147483647)
    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    @Column(name = "interno1")
    public Integer getInterno1() {
        return interno1;
    }

    public void setInterno1(Integer interno1) {
        this.interno1 = interno1;
    }

    @Column(name = "interno2")
    public Integer getInterno2() {
        return interno2;
    }

    public void setInterno2(Integer interno2) {
        this.interno2 = interno2;
    }

    @JoinColumn(name = "contribuyente", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    public Contribuyente getContribuyente() {
        return contribuyente;
    }

    public void setContribuyente(Contribuyente contribuyente) {
        this.contribuyente = contribuyente;
    }

    @JoinColumn(name = "departamento", referencedColumnName = "iddepto")
    @ManyToOne(optional = false)
    public Depto getDepartamento() {
        return departamento;
    }

    public void setDepartamento(Depto departamento) {
        this.departamento = departamento;
    }

    @JoinColumn(name = "municipio", referencedColumnName = "id")
    @ManyToOne
    public Municipio getMunicipio() {
        return municipio;
    }

    public void setMunicipio(Municipio municipio) {
        this.municipio = municipio;
    }

    @JoinColumn(name = "provincia", referencedColumnName = "idprovincia", nullable = false)
    @ManyToOne(optional = false)
    public Provincia getProvincia() {
        return provincia;
    }

    public void setProvincia(Provincia provincia) {
        this.provincia = provincia;
    }

    @JoinColumn(name = "rubro", referencedColumnName = "idrubro")
    @ManyToOne
    public Rubro getRubro() {
        return rubro;
    }

    public void setRubro(Rubro rubro) {
        this.rubro = rubro;
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
        if (!(object instanceof Proveedor)) {
            return false;
        }
        Proveedor other = (Proveedor) object;
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
