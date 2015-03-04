package jgestion.entity;

import java.io.Serializable;
import java.math.BigDecimal;
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
    @NamedQuery(name = "Proveedor.findByNombre", query = "SELECT p FROM Proveedor p WHERE p.nombre = :nombre")
})
public class Proveedor implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;
    @Basic(optional = false)
    @Column(name = "direccion", nullable = false, length = 200)
    private String direccion;
    @Column(name = "sucursal")
    private Integer sucursal;
    @Column(name = "tele1")
    private Long tele1;
    @Column(name = "tele2")
    private Long tele2;
    @Column(name = "email", length = 60)
    private String email;
    @Column(name = "contacto", length = 100)
    private String contacto;
    @Basic(optional = false)
    @Column(name = "cuit", nullable = false)
    private long cuit;
    @Column(name = "retencion_dgr")
    private Boolean retencionDgr;
    @Column(name = "retencion_iva")
    private Boolean retencionIva;
    @Column(name = "fechaalta")
    @Temporal(value = TemporalType.DATE)
    private Date fechaalta;
    @Column(name = "observacion", length = 200)
    private String observacion;
    @Basic(optional = false)
    @Column(name = "estado", nullable = false)
    private int estado;
    @Column(name = "codigopostal")
    private Integer codigopostal;
    @Column(name = "codigo", length = 20)
    private String codigo;
    @Column(name = "interno1")
    private Integer interno1;
    @Column(name = "interno2")
    private Integer interno2;
    @JoinColumn(name = "contribuyente", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Contribuyente contribuyente;
    @JoinColumn(name = "departamento", referencedColumnName = "iddepto")
    @ManyToOne(optional = false)
    private Departamento departamento;
    @JoinColumn(name = "municipio", referencedColumnName = "id")
    @ManyToOne
    private Municipio municipio;
    @JoinColumn(name = "provincia", referencedColumnName = "idprovincia", nullable = false)
    @ManyToOne(optional = false)
    private Provincia provincia;
    @JoinColumn(name = "rubro", referencedColumnName = "idrubro")
    @ManyToOne
    private Rubro rubro;
    @Column(name = "webpage", length = 50)
    private String webpage;
    @Basic(optional = false)
    @Column(name = "limite_ctacte", precision = 12, nullable = false)
    private BigDecimal limiteCtaCte;

    public Proveedor() {
    }

    public Proveedor(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

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

    public Integer getSucursal() {
        return sucursal;
    }

    public void setSucursal(Integer sucursal) {
        this.sucursal = sucursal;
    }

    public Long getTele1() {
        return tele1;
    }

    public void setTele1(Long tele1) {
        this.tele1 = tele1;
    }

    public Long getTele2() {
        return tele2;
    }

    public void setTele2(Long tele2) {
        this.tele2 = tele2;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public long getCuit() {
        return cuit;
    }

    public void setCuit(long cuit) {
        this.cuit = cuit;
    }

    public Boolean getRetencionDgr() {
        return retencionDgr;
    }

    public void setRetencionDgr(Boolean retencionDgr) {
        this.retencionDgr = retencionDgr;
    }

    public Boolean getRetencionIva() {
        return retencionIva;
    }

    public void setRetencionIva(Boolean retencionIva) {
        this.retencionIva = retencionIva;
    }

    public Date getFechaalta() {
        return fechaalta;
    }

    public void setFechaalta(Date fechaalta) {
        this.fechaalta = fechaalta;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public Integer getCodigopostal() {
        return codigopostal;
    }

    public void setCodigopostal(Integer codigopostal) {
        this.codigopostal = codigopostal;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Integer getInterno1() {
        return interno1;
    }

    public void setInterno1(Integer interno1) {
        this.interno1 = interno1;
    }

    public Integer getInterno2() {
        return interno2;
    }

    public void setInterno2(Integer interno2) {
        this.interno2 = interno2;
    }

    public Contribuyente getContribuyente() {
        return contribuyente;
    }

    public void setContribuyente(Contribuyente contribuyente) {
        this.contribuyente = contribuyente;
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public void setDepartamento(Departamento departamento) {
        this.departamento = departamento;
    }

    public Municipio getMunicipio() {
        return municipio;
    }

    public void setMunicipio(Municipio municipio) {
        this.municipio = municipio;
    }

    public Provincia getProvincia() {
        return provincia;
    }

    public void setProvincia(Provincia provincia) {
        this.provincia = provincia;
    }

    public Rubro getRubro() {
        return rubro;
    }

    public void setRubro(Rubro rubro) {
        this.rubro = rubro;
    }

    public BigDecimal getLimiteCtaCte() {
        return limiteCtaCte;
    }

    public void setLimiteCtaCte(BigDecimal limiteCtaCte) {
        this.limiteCtaCte = limiteCtaCte;
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

//    @Override
//    public String toString() {
//        return this.getNombre();
//    }

    @Override
    public String toString() {
        return "Proveedor{" + "id=" + id + ", nombre=" + nombre + ", direccion=" + direccion + ", sucursal=" + sucursal + ", tele1=" + tele1 + ", tele2=" + tele2 + ", email=" + email + ", contacto=" + contacto + ", cuit=" + cuit + ", retencionDgr=" + retencionDgr + ", retencionIva=" + retencionIva + ", fechaalta=" + fechaalta + ", observacion=" + observacion + ", estado=" + estado + ", codigopostal=" + codigopostal + ", codigo=" + codigo + ", interno1=" + interno1 + ", interno2=" + interno2 + ", provincia=" + provincia + ", webpage=" + webpage + ", limiteCtaCte=" + limiteCtaCte + '}';
    }
}
