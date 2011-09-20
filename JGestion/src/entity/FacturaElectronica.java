
package entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "factura_electronica", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"cbte_numero"})})
@NamedQueries({
    @NamedQuery(name = "FacturaElectronica.findAll", query = "SELECT f FROM FacturaElectronica f"),
    @NamedQuery(name = "FacturaElectronica.findById", query = "SELECT f FROM FacturaElectronica f WHERE f.id = :id"),
    @NamedQuery(name = "FacturaElectronica.findByCae", query = "SELECT f FROM FacturaElectronica f WHERE f.cae = :cae")
})
public class FacturaElectronica implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "fecha_proceso", nullable = false, length = 14)
    private String fechaProceso;
    @Basic(optional = false)
    @Column(name = "resultado", nullable = false, length = 1)
    private String resultado;
    @Basic(optional = false)
    @Column(name = "concepto", nullable = false)
    private int concepto;
    @Basic(optional = false)
    @Column(name = "cbte_tipo", nullable = false)
    private int cbteTipo;
    @Basic(optional = false)
    @Column(name = "cbte_numero", nullable = false)
    private int cbteNumero;
    @Column(name = "cae", length = 14)
    private String cae;
    @Column(name = "cae_fecha_vto")
    @Temporal(TemporalType.DATE)
    private Date caeFechaVto;
    @Column(name = "fecha_serv_desde")
    @Temporal(TemporalType.DATE)
    private Date fechaServDesde;
    @Column(name = "fecha_serv_hasta")
    @Temporal(TemporalType.DATE)
    private Date fechaServHasta;
    @Column(name = "observaciones", length = 2000)
    private String observaciones;

    public FacturaElectronica() {
    }

    public FacturaElectronica(Integer id, String fechaProceso, String resultado, int concepto, int cbteTipo, int cbteNumero) {
        this.id = id;
        this.fechaProceso = fechaProceso;
        this.resultado = resultado;
        this.concepto = concepto;
        this.cbteTipo = cbteTipo;
        this.cbteNumero = cbteNumero;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFechaProceso() {
        return fechaProceso;
    }

    public void setFechaProceso(String fechaProceso) {
        this.fechaProceso = fechaProceso;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public int getConcepto() {
        return concepto;
    }

    public void setConcepto(int concepto) {
        this.concepto = concepto;
    }

    public int getCbteTipo() {
        return cbteTipo;
    }

    public void setCbteTipo(int cbteTipo) {
        this.cbteTipo = cbteTipo;
    }

    public int getCbteNumero() {
        return cbteNumero;
    }

    public void setCbteNumero(int cbteNumero) {
        this.cbteNumero = cbteNumero;
    }

    public String getCae() {
        return cae;
    }

    public void setCae(String cae) {
        this.cae = cae;
    }

    public Date getCaeFechaVto() {
        return caeFechaVto;
    }

    public void setCaeFechaVto(Date caeFechaVto) {
        this.caeFechaVto = caeFechaVto;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Date getFechaServDesde() {
        return fechaServDesde;
    }

    public Date getFechaServHasta() {
        return fechaServHasta;
    }

    public void setFechaServDesde(Date fechaServDesde) {
        this.fechaServDesde = fechaServDesde;
    }

    public void setFechaServHasta(Date fechaServHasta) {
        this.fechaServHasta = fechaServHasta;
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
        if (!(object instanceof FacturaElectronica)) {
            return false;
        }
        FacturaElectronica other = (FacturaElectronica) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.getClass() + "{" + "id=" + id + ", fechaProceso=" + fechaProceso
                + ", resultado=" + resultado + ", concepto=" + concepto + ", cbteTipo="
                + cbteTipo + ", cbteNumero=" + cbteNumero + ", cae=" + cae + ", caeFechaVto="
                + caeFechaVto + ", observaciones=" + observaciones + '}';
    }
}
