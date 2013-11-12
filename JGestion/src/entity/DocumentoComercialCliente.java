package entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "documento_comercial_cliente")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "DocumentoComercialCliente.findAll", query = "SELECT d FROM DocumentoComercialCliente d")})
public class DocumentoComercialCliente implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(nullable = false)
    private Integer id;
    @Basic(optional = false)
    @Column(nullable = false, length = 40)
    private String numero;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;
    @Basic(optional = false)
    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date fecha;
    @Basic(optional = false)
    @Column(nullable = false, length = 200)
    private String origen;
    @Basic(optional = false)
    @Column(nullable = false)
    private boolean anulado;
    @Column(length = 200)
    private String observacion;
    @JoinColumn(name = "cliente_id", nullable = false)
    @ManyToOne(optional = false)
    private Cliente cliente;
    @JoinColumn(name = "documento_comercial_id", nullable = false)
    @ManyToOne(optional = false)
    private DocumentoComercial documentoComercial;

    public DocumentoComercialCliente() {
    }

    public DocumentoComercialCliente(Integer id) {
        this.id = id;
    }

    public DocumentoComercialCliente(String numero, BigDecimal importe, Date fecha, String origen, boolean anulado, String observacion, Cliente cliente, DocumentoComercial documentoComercial) {
        this.numero = numero;
        this.importe = importe;
        this.fecha = fecha;
        this.origen = origen;
        this.anulado = anulado;
        this.observacion = observacion;
        this.cliente = cliente;
        this.documentoComercial = documentoComercial;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public boolean getAnulado() {
        return anulado;
    }

    public void setAnulado(boolean anulado) {
        this.anulado = anulado;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public DocumentoComercial getDocumentoComercial() {
        return documentoComercial;
    }

    public void setDocumentoComercial(DocumentoComercial documentoComercial) {
        this.documentoComercial = documentoComercial;
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
        if (!(object instanceof DocumentoComercialCliente)) {
            return false;
        }
        DocumentoComercialCliente other = (DocumentoComercialCliente) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DocumentoComercialCliente{" + "id=" + id + ", numero=" + numero + ", importe=" + importe + ", fecha=" + fecha + ", origen=" + origen + ", anulado=" + anulado + ", observacion=" + observacion + ", cliente=" + cliente + ", documentoComercial=" + documentoComercial + '}';
    }

}
