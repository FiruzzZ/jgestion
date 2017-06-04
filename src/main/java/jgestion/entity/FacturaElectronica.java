package jgestion.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import javax.persistence.*;

/**
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "factura_electronica", uniqueConstraints = {
    @UniqueConstraint(name = "factura_electronica_cbte_fields_unique", columnNames = {"pto_vta", "cbte_tipo", "cbte_numero"}),
    @UniqueConstraint(name = "factura_electronica_cae_unique", columnNames = {"cae"})
})
public class FacturaElectronica implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(name = "fecha_proceso")
    @Temporal(TemporalType.DATE)
    private Date fechaProceso;
    /**
     * A=APROBADO, R=RECHAZADO, P=PARCIAL
     */
    @Basic(optional = false)
    @Column(name = "resultado", length = 1)
    private String resultado;
    @Basic(optional = false)
    /**
     * 1= PRODUCTO, 2= SERVICIO, 3= PRODUCTO Y SERVICIO
     */
    @Column(name = "concepto", nullable = false)
    private int concepto;
    @Column(name = "pto_vta", nullable = false, precision = 4)
    private int ptoVta;
    @Column(name = "cbte_numero", nullable = false, precision = 8)
    private long cbteNumero;
    /**
     * <br>1, Factura A, 20100917, NULL
     * <br>2, Nota de Débito A, 20100917, NULL
     * <br>3, Nota de Crédito A, 20100917, NULL
     * <br>6, Factura B, 20100917, NULL
     * <br>7, Nota de Débito B, 20100917, NULL
     * <br>8, Nota de Crédito B, 20100917, NULL
     * <br>4, Recibos A, 20100917, NULL
     * <br>5, Notas de Venta al contado A, 20100917, NULL
     * <br>9, Recibos B, 20100917, NULL
     * <br>10, Notas de Venta al contado B, 20100917, NULL
     * <br>63, Liquidacion A, 20100917, NULL
     * <br>64, Liquidacion B, 20100917, NULL
     * <br>34, Cbtes. A del Anexo I, Apartado A,inc.f),R.G.Nro. 1415, 20100917, NULL
     * <br>35, Cbtes. B del Anexo I,Apartado A,inc. f),R.G. Nro. 1415, 20100917, NULL
     * <br>39, Otros comprobantes A que cumplan con R.G.Nro. 1415, 20100917, NULL
     * <br>40, Otros comprobantes B que cumplan con R.G.Nro. 1415, 20100917, NULL
     * <br>60, Cta de Vta y Liquido prod. A, 20100917, NULL
     * <br>61, Cta de Vta y Liquido prod. B, 20100917, NULL
     * <br>11, Factura C, 20110330, NULL
     * <br>12, Nota de Débito C, 20110330, NULL
     * <br>13, Nota de Crédito C, 20110330, NULL
     * <br>15, Recibo C, 20110330, NULL
     * <br>49, Comprobante de Compra de Bienes Usados a Consumidor Final, 20130401, NULL
     * <br>51, Factura M, 20150522, NULL
     * <br>52, Nota de Débito M, 20150522, NULL
     * <br>53, Nota de Crédito M, 20150522, NULL
     * <br>54, Recibo M, 20150522, NULL
     */
    @Basic(optional = false)
    @Column(name = "cbte_tipo", nullable = false)
    private int cbteTipo;
    @Column(name = "cae", length = 14, unique = true)
    private String cae;
    @Column(name = "cae_fecha_vto")
    @Temporal(TemporalType.DATE)
    private Date caeFechaVto;
    private String observaciones;

    public FacturaElectronica() {
    }

    public FacturaElectronica(Integer id, int cbteTipo, int ptoVta, long cbteNumero, Date fechaProceso, String resultado, int concepto, String cae, Date caeFechaVto, String observaciones) {
        this.id = id;
        this.ptoVta = ptoVta;
        this.cbteNumero = cbteNumero;
        this.fechaProceso = fechaProceso;
        this.resultado = resultado;
        this.concepto = concepto;
        this.cbteTipo = cbteTipo;
        this.cae = cae;
        this.caeFechaVto = caeFechaVto;
        this.observaciones = observaciones;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getFechaProceso() {
        return fechaProceso;
    }

    public void setFechaProceso(Date fechaProceso) {
        this.fechaProceso = fechaProceso;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    /**
     *
     * @return {@link #concepto}
     */
    public int getConcepto() {
        return concepto;
    }

    /**
     * 1= PRODUCTO, 2= SERVICIO, 3= PRODUCTO Y SERVICIO
     * @param concepto
     */
    public void setConcepto(int concepto) {
        this.concepto = concepto;
    }

    public int getPtoVta() {
        return ptoVta;
    }

    public void setPtoVta(int ptoVta) {
        this.ptoVta = ptoVta;
    }

    /**
     *
     * @return {@link #cbteTipo}
     */
    public int getCbteTipo() {
        return cbteTipo;
    }

    /**
     *
     * @param cbteTipo {@link #cbteTipo}
     */
    public void setCbteTipo(int cbteTipo) {
        this.cbteTipo = cbteTipo;
    }

    public long getCbteNumero() {
        return cbteNumero;
    }

    public void setCbteNumero(long cbteNumero) {
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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.id);
        hash = 89 * hash + this.ptoVta;
        hash = 89 * hash + this.cbteTipo;
        hash = 89 * hash + Objects.hashCode(this.cae);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FacturaElectronica other = (FacturaElectronica) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.cae, other.cae)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FacturaElectronica{" + "id=" + id
                + ", ptoVta=" + ptoVta + ", cbteNumero=" + cbteNumero + ", cbteTipo=" + cbteTipo + ", cae=" + cae
                + ", fechaProceso=" + fechaProceso + ", resultado=" + resultado + ", concepto=" + concepto
                + ", caeFechaVto=" + caeFechaVto + ", observaciones=" + observaciones + '}';
    }

}
