package entity;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "documento_comercial", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"nombre"})
})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "DocumentoComercial.findAll", query = "SELECT d FROM DocumentoComercial d")})
public class DocumentoComercial implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(nullable = false)
    private Integer id;
    @Basic(optional = false)
    @Column(nullable = false, length = 100)
    private String nombre;
    @Basic(optional = false)
    @Column(nullable = false)
    private int minlength;
    @Basic(optional = false)
    @Column(nullable = false)
    private int maxlength;
    @Basic(optional = false)
    @Column(nullable = false)
    private boolean alphanumeric;
    @Basic(optional = false)
    @Column(nullable = false)
    private boolean unicoPorEmpresa;
//    @OneToMany(mappedBy = "documentoComercial")
//    private List<DocumentoComercialCliente> documentoComercialClienteList;
//    @OneToMany(mappedBy = "documentoComercial")
//    private List<DocumentoComercialProveedor> documentoComercialProveedorList;

    public DocumentoComercial() {
    }

    public DocumentoComercial(Integer id) {
        this.id = id;
    }

    public DocumentoComercial(Integer id, String nombre, int minlength, int maxlength, boolean alphanumeric, boolean unicoPorEmpresa) {
        this.id = id;
        this.nombre = nombre;
        this.minlength = minlength;
        this.maxlength = maxlength;
        this.alphanumeric = alphanumeric;
        this.unicoPorEmpresa = unicoPorEmpresa;
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

    public int getMinlength() {
        return minlength;
    }

    public void setMinlength(int minlength) {
        this.minlength = minlength;
    }

    public int getMaxlength() {
        return maxlength;
    }

    public void setMaxlength(int maxlength) {
        this.maxlength = maxlength;
    }

    public void setAlphanumeric(boolean alphanumeric) {
        this.alphanumeric = alphanumeric;
    }

    public boolean isAlphanumeric() {
        return alphanumeric;
    }

    public boolean isUnicoPorEmpresa() {
        return unicoPorEmpresa;
    }

    public void setUnicoPorEmpresa(boolean unicoPorEmpresa) {
        this.unicoPorEmpresa = unicoPorEmpresa;
    }

//    @XmlTransient
//    public List<DocumentoComercialCliente> getDocumentoComercialClienteList() {
//        return documentoComercialClienteList;
//    }
//
//    public void setDocumentoComercialClienteList(List<DocumentoComercialCliente> documentoComercialClienteList) {
//        this.documentoComercialClienteList = documentoComercialClienteList;
//    }
//
//    @XmlTransient
//    public List<DocumentoComercialProveedor> getDocumentoComercialProveedorList() {
//        return documentoComercialProveedorList;
//    }
//
//    public void setDocumentoComercialProveedorList(List<DocumentoComercialProveedor> documentoComercialProveedorList) {
//        this.documentoComercialProveedorList = documentoComercialProveedorList;
//    }
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DocumentoComercial)) {
            return false;
        }
        DocumentoComercial other = (DocumentoComercial) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DocumentoComercial{" + "id=" + id + ", nombre=" + nombre + ", minlength=" + minlength + ", maxlength=" + maxlength + ", alphanumeric=" + alphanumeric + ", unicoporempresa=" + unicoPorEmpresa + '}';
    }

}
