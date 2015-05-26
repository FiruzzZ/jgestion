package jgestion.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "cliente", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tipodoc", "num_doc"}),
    @UniqueConstraint(columnNames = {"codigo"})
})
@NamedQueries({
    @NamedQuery(name = "Cliente.findAll", query = "SELECT c FROM Cliente c order by c.nombre"),
    @NamedQuery(name = "Cliente.findById", query = "SELECT c FROM Cliente c WHERE c.id = :id"),
    @NamedQuery(name = "Cliente.findByCodigo", query = "SELECT c FROM Cliente c WHERE c.codigo = :codigo")
})
public class Cliente implements Serializable {

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
    @Column(name = "direccion", nullable = false, length = 150)
    private String direccion;
    @Basic(optional = false)
    @Column(name = "tipodoc", nullable = false)
    private int tipodoc;
    @Basic(optional = false)
    @Column(name = "num_doc", nullable = false)
    private long numDoc;
    @Column(name = "tele1")
    private Long tele1;
    @Column(name = "tele2")
    private Long tele2;
    @Column(name = "interno1")
    private Integer interno1;
    @Column(name = "interno2")
    private Integer interno2;
    @Column(name = "email", length = 100)
    private String email;
    @Column(name = "estado", nullable = false)
    private Integer estado;
    @Column(name = "codigopostal")
    private Integer codigopostal;
    @Column(name = "observacion", length = 200)
    private String observacion;
    @Column(name = "contacto", length = 100)
    private String contacto;
    @Column(name = "webpage", length = 100)
    private String webpage;
    @Column(name = "codigo", length = 100)
    private String codigo;
    @JoinColumn(name = "contribuyente", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Contribuyente contribuyente;
    @JoinColumn(name = "departamento", referencedColumnName = "iddepto")
    @ManyToOne
    private Departamento departamento;
    @JoinColumn(name = "provincia", referencedColumnName = "idprovincia", nullable = false)
    @ManyToOne(optional = false)
    private Provincia provincia;
    @JoinColumn(name = "municipio", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Municipio municipio;
    @Basic(optional = false)
    @Column(name = "limite_ctacte", precision = 12, nullable = false)
    private BigDecimal limiteCtaCte;

    public Cliente() {
    }

    public Cliente(Integer id) {
        this.id = id;
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public int getTipodoc() {
        return tipodoc;
    }

    public void setTipodoc(int tipodoc) {
        this.tipodoc = tipodoc;
    }

    public long getNumDoc() {
        return numDoc;
    }

    public void setNumDoc(long numDoc) {
        this.numDoc = numDoc;
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

    public Integer getEstado() {
        return estado;
    }

    public void setEstado(Integer estado) {
        this.estado = estado;
    }

    public Integer getCodigopostal() {
        return codigopostal;
    }

    public void setCodigopostal(Integer codigopostal) {
        this.codigopostal = codigopostal;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public String getWebpage() {
        return webpage;
    }

    public void setWebpage(String webpage) {
        this.webpage = webpage;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
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

    public Provincia getProvincia() {
        return provincia;
    }

    public void setProvincia(Provincia provincia) {
        this.provincia = provincia;
    }

    public Municipio getMunicipio() {
        return municipio;
    }

    public void setMunicipio(Municipio municipio) {
        this.municipio = municipio;
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
        if (!(object instanceof Cliente)) {
            return false;
        }
        Cliente other = (Cliente) object;
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
