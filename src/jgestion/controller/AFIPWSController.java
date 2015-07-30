package jgestion.controller;

import afip.ws.exception.WSAFIPErrorResponseException;
import afip.ws.wsaa.WSAA;

//entorno de test/homologación
//import afip.ws.jaxws.*;
//import afip.ws.wsfe.AFIPTestClient;
//entorno de producción
import afip.ws.produccion.fev1.*;
import afip.ws.wsfe.AFIPFEVClient;

import generics.CustomABMJDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.UnrecoverableKeyException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import jgestion.JGestionUtils;
import jgestion.controller.exceptions.MessageException;
import jgestion.entity.Cliente;
import jgestion.entity.DetalleNotaCredito;
import jgestion.entity.DetalleNotaDebito;
import jgestion.entity.DetalleVenta;
import jgestion.entity.FacturaElectronica;
import jgestion.entity.FacturaVenta;
import jgestion.entity.NotaCredito;
import jgestion.entity.NotaDebito;
import jgestion.gui.PanelAFIPWSConsultarCbte;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import utilities.general.UTIL;

/**
 *
 * @author FiruzzZ
 */
public class AFIPWSController {

    /**
     * ./ws/
     */
    private static final String WS_FOLDER = "." + File.separator + "ws" + File.separator;
    private static final String TICKET_ACCESS_XML = WS_FOLDER + "tar.xml";
    private static final String WS_CLIENT_PROPERTIES = WS_FOLDER + "wsaa_client.properties";
    private static final Logger LOG = LogManager.getLogger();

//    private AFIPTestClient aFIPClient;
    private AFIPFEVClient aFIPClient;
    private String eventos;
    private Document TA_XML;

    /**
     * Se encarga de instanciar un con un TickerAccess no expirado (si existiera uno pero ya
     * expirado, genera uno nuevo a través de {@link WSAA})
     *
     * @param pwdPKCS12
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws Exception
     */
    public AFIPWSController(String pwdPKCS12) throws ParserConfigurationException, SAXException, IOException, Exception {
        if (!aFIPClient.areServicesAvailable()) {
            throw new MessageException("Los servicios de la AFIP no se encuentran disponibles.");
        }
        boolean expired = true;
        File ticketAccessXMLFile = new File(TICKET_ACCESS_XML);
        if (ticketAccessXMLFile.exists()) {
            TA_XML = getDocument(ticketAccessXMLFile);
            Date expirationTime = WSAA.getExpirationTime(TA_XML);
            expired = expirationTime.before(JGestionUtils.getServerDate());
        }
        if (expired) {
            try {
                File f = new File(WS_CLIENT_PROPERTIES);
                if (!f.exists()) {
                    throw new MessageException("No se encontró el archivo de propiedades de WS: " + WS_CLIENT_PROPERTIES);
                }
                Properties config = new Properties();
                config.load(new FileInputStream(WS_CLIENT_PROPERTIES));
                //en el archivo de propiedad no se hace referencia a ningún path específico, solo al nombre del archivo PKCS#12
                config.setProperty("keystore", WS_FOLDER + config.getProperty("keystore"));
                WSAA wsaa = new WSAA(config);
                String tarXML = wsaa.getLoginTicketResponse(null, pwdPKCS12, null);
                UTIL.createFile(tarXML, ticketAccessXMLFile.getCanonicalPath());
            } catch (UnrecoverableKeyException ex) {
                throw new MessageException("No es posible crear el certificado porque la contraseña del archivo PCKS#12 no es correcta"
                        + "\n" + ex.getMessage());
            }
        }
        TA_XML = getDocument(ticketAccessXMLFile);
//        aFIPClient = new AFIPTestClient(TA_XML);
        aFIPClient = new AFIPFEVClient(TA_XML);
    }

    /**
     * Parse the content of the given file as an XML document and return a new DOM
     * {@link org.w3c.dom.Document} object normalized ({@link org.w3c.dom.Document#normalize()}). An
     * IllegalArgumentException is thrown if the File is null null.
     *
     * @param file
     * @return A new DOM Document object.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private Document getDocument(File file) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        TA_XML = documentBuilder.parse(file);
        TA_XML.normalize();
        return TA_XML;
    }

    /**
     *
     * @param ptoVta
     * @param cbteTipo para saber el ID usar {@link #getTipoComprobanteID(java.lang.Object)}
     * @return
     * @throws WSAFIPErrorResponseException
     */
    public int getUltimoCompActualizado(int ptoVta, int cbteTipo) throws WSAFIPErrorResponseException {
        CbteTipo cbteTipoo = new CbteTipo();
        cbteTipoo.setId(cbteTipo);
        return aFIPClient.getUltimoCompActualizado(ptoVta, cbteTipoo);
    }

    public FacturaElectronica requestCAE(FacturaElectronica fe, NotaDebito cbte) throws WSAFIPErrorResponseException, MessageException {
        //<editor-fold defaultstate="collapsed" desc="IVAs">
        List<IvaTipo> iVATipoList = getIVATipoList();
        HashMap<IvaTipo, BigDecimal> totalesAlicuotas = new HashMap<>();
        for (IvaTipo o : iVATipoList) {
            totalesAlicuotas.put(o, BigDecimal.ZERO);
        }
        //recorriendo el detalle de la facturaVenta
        for (DetalleNotaDebito detalleVenta : cbte.getDetalle()) {
            boolean unknownIVA = true;
            int cantidad = detalleVenta.getCantidad();
            BigDecimal precioUnitario = detalleVenta.getImporte();
            Double productoIVA = Double.valueOf(detalleVenta.getIva().getIva());
            //identificando el IVA de cada item del detalle
            for (IvaTipo o : iVATipoList) {
                Double AFIP_IVA = Double.valueOf(o.getDesc().replaceAll("%", ""));
                if (0 == (AFIP_IVA.compareTo(productoIVA))) {
                    BigDecimal currentTotalIVA = totalesAlicuotas.get(o);
                    currentTotalIVA = currentTotalIVA.add(precioUnitario.multiply(BigDecimal.valueOf(cantidad))).setScale(2, RoundingMode.HALF_UP);
                    totalesAlicuotas.put(o, currentTotalIVA);
                    unknownIVA = false;
                    break;
                }
            }
            if (unknownIVA) {
                //no existe la alicuota IVA que tiene el producto..
                throw new MessageException("El producto:"
                        + "\n" + detalleVenta.getConcepto()
                        + "\n tiene una alícuota (IVA) \"" + productoIVA + "\" que no existe en los registros de la AFIP.");
            }
        }
        //</editor-fold>
        DocTipo docTipo = new DocTipo();
        docTipo.setId(cbte.getCliente().getTipodoc().getAfipID());
        return invokeFE(fe, cbte.getCliente(), cbte.getFechaNotaDebito(), cbte.getImporte(),
                cbte.getGravado(), cbte.getNoGravado(), totalesAlicuotas, docTipo);
    }

    public FacturaElectronica requestCAE(FacturaElectronica fe, NotaCredito cbte) throws WSAFIPErrorResponseException, MessageException {
        List<IvaTipo> iVATipoList = getIVATipoList();
        HashMap<IvaTipo, BigDecimal> totalesAlicuotas = new HashMap<>();
        for (IvaTipo o : iVATipoList) {
            totalesAlicuotas.put(o, BigDecimal.ZERO);
        }
        //recorriendo el detalle de la facturaVenta
        for (DetalleNotaCredito detalleVenta : cbte.getDetalle()) {
            boolean unknownIVA = true;
            int cantidad = detalleVenta.getCantidad();
            BigDecimal precioUnitario = detalleVenta.getPrecioUnitario();
            Double productoIVA = Double.valueOf(detalleVenta.getProducto().getIva().getIva());
            //identificando el IVA de cada item del detalle
            for (IvaTipo o : iVATipoList) {
                Double AFIP_IVA = Double.valueOf(o.getDesc().replaceAll("%", ""));
                if (0 == (AFIP_IVA.compareTo(productoIVA))) {
                    BigDecimal currentTotalIVA = totalesAlicuotas.get(o);
                    currentTotalIVA = currentTotalIVA.add(precioUnitario.multiply(BigDecimal.valueOf(cantidad))).setScale(2, RoundingMode.HALF_UP);
                    totalesAlicuotas.put(o, currentTotalIVA);
                    unknownIVA = false;
                    break;
                }
            }
            if (unknownIVA) {
                //no existe la alicuota IVA que tiene el producto..
                throw new MessageException("El producto:"
                        + "\n(" + detalleVenta.getProducto().getCodigo() + ") "
                        + detalleVenta.getProducto().getNombre()
                        + "\n tiene una alícuota (IVA) \"" + productoIVA + "\" que no existe en los registros de la AFIP.");
            }
        }
        DocTipo docTipo = new DocTipo();
        docTipo.setId(cbte.getCliente().getTipodoc().getAfipID());
        return invokeFE(fe, cbte.getCliente(), cbte.getFechaNotaCredito(), cbte.getImporte(),
                cbte.getGravado(), cbte.getNoGravado(), totalesAlicuotas, docTipo);
    }

    public FacturaElectronica requestCAE(FacturaElectronica fe, FacturaVenta cbte) throws WSAFIPErrorResponseException, MessageException {
        List<IvaTipo> iVATipoList = getIVATipoList();
        HashMap<IvaTipo, BigDecimal> totalesAlicuotas = new HashMap<>();
        for (IvaTipo o : iVATipoList) {
            totalesAlicuotas.put(o, BigDecimal.ZERO);
        }
        for (DetalleVenta detalleVenta : cbte.getDetallesVentaList()) {
            boolean unknownIVA = true;
            int cantidad = detalleVenta.getCantidad();
            BigDecimal subTotalProducto = detalleVenta.getPrecioUnitario()
                    .multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
            Double productoIVA = Double.valueOf(detalleVenta.getProducto().getIva().getIva());
            //identificando el IVA de cada item del detalle
            for (IvaTipo o : iVATipoList) {
                Double AFIP_IVA = Double.valueOf(o.getDesc().replaceAll("%", ""));
                if (0 == (AFIP_IVA.compareTo(productoIVA))) {
                    BigDecimal currentTotalIVA = totalesAlicuotas.get(o);
                    currentTotalIVA = currentTotalIVA.add(subTotalProducto).setScale(2, RoundingMode.HALF_UP);
                    totalesAlicuotas.put(o, currentTotalIVA);
                    unknownIVA = false;
                    break;
                }
            }
            if (unknownIVA) {
                //no existe la alicuota IVA que tiene el producto..
                throw new MessageException("El producto:"
                        + "\n(" + detalleVenta.getProducto().getCodigo() + ") "
                        + detalleVenta.getProducto().getNombre()
                        + "\n tiene una alícuota (IVA) \"" + productoIVA + "\" que no existe en los registros de la AFIP.");
            }
        }
        ConceptoTipo conceptoTipo = new ConceptoTipo();
        conceptoTipo.setId(3);//prod y serv
        DocTipo docTipo = new DocTipo();
        docTipo.setId(cbte.getCliente().getTipodoc().getAfipID());
        return invokeFE(fe, cbte.getCliente(), cbte.getFechaVenta(), cbte.getImporte(), cbte.getGravado(), cbte.getNoGravado(), totalesAlicuotas, docTipo);
    }

    private FacturaElectronica invokeFE(FacturaElectronica fe, Cliente cliente, Date cbteFecha,
            BigDecimal importeTotal, BigDecimal gravado, BigDecimal noGravado,
            HashMap<IvaTipo, BigDecimal> totalesAlicuotas, DocTipo docTipo) throws WSAFIPErrorResponseException, MessageException {
        Moneda moneda = new Moneda();
        moneda.setId("PES");
        double monCotizacion = 1;
        CbteTipo cbteTipo = new CbteTipo();
        cbteTipo.setId(fe.getCbteTipo());
        final int nextOne = aFIPClient.getUltimoCompActualizado(fe.getPtoVta(), cbteTipo) + 1;
        if (!Objects.equals(nextOne + "", fe.getCbteNumero() + "")) {
            throw new MessageException("El número de Factura: " + fe.getCbteNumero() + " no coincide con el esperado por AFIP: " + nextOne);
        }
        FECAERequest fECAERequest = new FECAERequest();
        FECAECabRequest fECAECabRequest = new FECAECabRequest();

        fECAECabRequest.setCbteTipo(cbteTipo.getId());
        fECAECabRequest.setPtoVta(fe.getPtoVta());

        //inicializa todos los totales
        ArrayOfAlicIva arrayOfAlicIva = new ArrayOfAlicIva();
        //total de todos los IVA's
        double impIVA = 0;
        //se agregan las alic con importe > 0
        for (IvaTipo ivaTipo : totalesAlicuotas.keySet()) {
            if (Double.valueOf(ivaTipo.getDesc().replaceAll("%", "")) > 0) {
                BigDecimal totalAlic = totalesAlicuotas.get(ivaTipo);
                if (totalAlic.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal totalImporte = totalAlic.multiply(BigDecimal.valueOf((Double.valueOf(ivaTipo.getDesc().replaceAll("%", "")) / 100)))
                            .setScale(2, RoundingMode.HALF_UP);
                    AlicIva alic = new AlicIva();
                    alic.setId(Integer.parseInt(ivaTipo.getId()));
                    alic.setBaseImp(totalAlic.doubleValue());
                    alic.setImporte(totalImporte.doubleValue());
                    LOG.trace("IVA Tipo: " + ivaTipo.getDesc() + " | " + alic.toString());
                    arrayOfAlicIva.getAlicIva().add(alic);
                    impIVA += alic.getImporte();
                }
            }
        }

        double impTributos = 0;

        ArrayOfCbteAsoc arrayOfCbteAsoc = null;
        ArrayOfTributo arrayOfTributo = null;
        ArrayList<Tributo> tributoList = new ArrayList<>();
        if (!tributoList.isEmpty()) {
            arrayOfTributo = new ArrayOfTributo();
            for (Tributo tributo : tributoList) {
                arrayOfTributo.getTributo().add(tributo);
                impTributos += tributo.getImporte();
            }
        }
        ArrayOfFECAEDetRequest arrayOfFECAEDetRequest = new ArrayOfFECAEDetRequest();
        FECAEDetRequest detalle = new FECAEDetRequest();
        detalle.setConcepto(fe.getConcepto());
        detalle.setDocTipo(docTipo.getId());
        detalle.setDocNro(cliente.getNumDoc());
        detalle.setCbteDesde(fe.getCbteNumero());
        detalle.setCbteHasta(fe.getCbteNumero());
        detalle.setCbteFch(getXMLDate(cbteFecha));
        detalle.setImpTotal(importeTotal.doubleValue());
        detalle.setImpNeto(gravado.doubleValue());
        detalle.setImpTotConc(noGravado.doubleValue());
        detalle.setImpOpEx(0);
        detalle.setImpTrib(impTributos);
        detalle.setImpIVA(BigDecimal.valueOf(impIVA).setScale(2, RoundingMode.HALF_UP).doubleValue());
        detalle.setFchServDesde(getXMLDate(cbteFecha));
        detalle.setFchServHasta(getXMLDate(cbteFecha));
        detalle.setFchVtoPago(getXMLDate(cbteFecha));
        detalle.setMonId(moneda.getId());
        detalle.setMonCotiz(monCotizacion);
        detalle.setCbtesAsoc(arrayOfCbteAsoc);
        detalle.setTributos(arrayOfTributo);
        detalle.setIva(arrayOfAlicIva.getAlicIva().isEmpty() ? null : arrayOfAlicIva);
        detalle.setOpcionales(null);
        LOG.trace("total=" + detalle.getImpTotal() + ", neto=" + detalle.getImpNeto() + ", totConc=" + detalle.getImpTotConc()
                + ", OpEx=" + detalle.getImpOpEx() + ", Trib=" + detalle.getImpTrib() + ", IVA=" + detalle.getImpIVA());
        arrayOfFECAEDetRequest.getFECAEDetRequest().add(detalle);

        fECAECabRequest.setCantReg(arrayOfFECAEDetRequest.getFECAEDetRequest().size());
        fECAERequest.setFeCabReq(fECAECabRequest);
        fECAERequest.setFeDetReq(arrayOfFECAEDetRequest);

        FECAEResponse fECAE = aFIPClient.invokeFECAESolicitar(fECAERequest);
        FECAECabResponse feCabResp = fECAE.getFeCabResp();
        FECAEDetResponse fEDetalle = fECAE.getFeDetResp().getFECAEDetResponse().get(0);
        LOG.info(feCabResp);
        LOG.info(fEDetalle);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat caeVtoformat = new SimpleDateFormat("yyyyMMdd");
        StringBuilder sb = new StringBuilder(100);
        if (fEDetalle.getObservaciones() != null
                && fEDetalle.getObservaciones().getObs() != null
                && !fEDetalle.getObservaciones().getObs().isEmpty()) {
            sb = new StringBuilder("");
            for (Obs obs : fEDetalle.getObservaciones().getObs()) {
                sb.append("code=").append(obs.getCode()).append(", msg=").append(obs.getMsg());
            }
        }
        if (feCabResp.getResultado().equalsIgnoreCase("R")) {
            throw new WSAFIPErrorResponseException(sb.toString());
        }
        try {
            fe.setCae(fEDetalle.getCAE());
            fe.setCaeFechaVto(caeVtoformat.parse(fEDetalle.getCAEFchVto()));
            fe.setResultado(feCabResp.getResultado());
            fe.setFechaProceso(sdf.parse(feCabResp.getFchProceso()));
            fe.setObservaciones(sb.toString().isEmpty() ? null : sb.toString());
        } catch (ParseException ex) {
            LOG.error("parseando xml date", ex);
        }
        return fe;
    }

    public FacturaElectronica getFEComprobante(int ptoVenta, int cbteNro, int cbteTipo) throws WSAFIPErrorResponseException {
        FECompConsultaReq fec = new FECompConsultaReq();
        fec.setPtoVta(ptoVenta);
        fec.setCbteNro(cbteNro);
        fec.setCbteTipo(cbteTipo);
        FECompConsultaResponse o = aFIPClient.getComprobante(fec);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        FECompConsResponse res = o.getResultGet();
        String observaciones = null;
        if (res.getObservaciones() != null && res.getObservaciones().getObs() != null && !res.getObservaciones().getObs().isEmpty()) {
            observaciones = "";
            for (Obs ob : res.getObservaciones().getObs()) {
                observaciones += "code:" + ob.getCode() + ", msg:" + ob.getMsg();
            }
        }
        try {
            return new FacturaElectronica(null, cbteTipo, ptoVenta, cbteNro, sdf.parse(res.getFchProceso()), res.getResultado(), res.getConcepto(), res.getCodAutorizacion(), null, observaciones);
        } catch (ParseException ex) {
            LOG.error("parseando xml date=" + res.getFchProceso(), ex);
            throw new WSAFIPErrorResponseException("Fecha proceso no válida: " + res.getFchProceso());
        }
    }

    public List<CbteTipo> getComprobantesTipoList() throws WSAFIPErrorResponseException {
        return aFIPClient.getComprobantesTipoList();
    }

    public List<ConceptoTipo> getConceptoTipoList() throws WSAFIPErrorResponseException {
        return aFIPClient.getConceptoTipoList();
    }

    public List<DocTipo> getDocumentosTipoList() throws WSAFIPErrorResponseException {
        return aFIPClient.getDocumentosTipoList();
    }

    public List<IvaTipo> getIVATipoList() throws WSAFIPErrorResponseException {
        return aFIPClient.getIVATipoList();
    }

    String getEventos() {
        return eventos;
    }

    /**
     * Convert a Date to a formatted XML Date String (yyyyMMdd)
     *
     * @param fecha To be converted
     * @return A XML Date String formatted. If <tt>fecha</tt> == null, returns null.
     */
    private String getXMLDate(Date fecha) {
        if (fecha == null) {
            return null;
        }
        return new SimpleDateFormat("yyyyMMdd").format(fecha);
    }

    /**
     * Campo CbteTipo sea: {@link FacturaElectronica#cbteTipo}
     *
     * @param o {@link FacturaVenta}, {@link NotaCredito}, {@link NotaDebito}
     * @return
     */
    public static final int getTipoComprobanteID(Object o) {
        String tipo = null;
        if (o instanceof FacturaVenta) {
            FacturaVenta oo = (FacturaVenta) o;
            tipo = String.valueOf(oo.getTipo()).toUpperCase();
            switch (tipo) {
                case "A":
                    return 1;
                case "B":
                    return 6;
                case "C":
                    return 11;
                case "M":
                    return 51;
                default:
            }
        } else if (o instanceof NotaDebito) {
            NotaDebito oo = (NotaDebito) o;
            tipo = String.valueOf(oo.getTipo()).toUpperCase();
            switch (tipo) {
                case "A":
                    return 2;
                case "B":
                    return 7;
                case "C":
                    return 12;
                case "M":
                    return 52;
                default:
            }
        } else if (o instanceof NotaCredito) {
            NotaCredito oo = (NotaCredito) o;
            tipo = String.valueOf(oo.getTipo()).toUpperCase();
            switch (tipo) {
                case "A":
                    return 3;
                case "B":
                    return 8;
                case "C":
                    return 13;
                case "M":
                    return 53;
                default:
            }
        }
        throw new IllegalArgumentException("Comprobante no identificado= " + tipo);
    }

    public void consultarCAEs() throws WSAFIPErrorResponseException {
        final PanelAFIPWSConsultarCbte p = new PanelAFIPWSConsultarCbte();
        UTIL.loadComboBox(p.getCbSucursal(), JGestionUtils.getWrappedSucursales(new UsuarioHelper().getSucursales()), false);
        UTIL.loadComboBox(p.getCbCbte(), aFIPClient.getComprobantesTipoList(), false);
        CustomABMJDialog abm = new CustomABMJDialog(null, p, "Consultar Comprobantes", true, "Bla bla bla..");
        abm.getBtnAceptar().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    FacturaElectronica feComprobante = getFEComprobante(
                            Integer.valueOf(p.getTfPtoVta().getText()),
                            Integer.valueOf(p.getTfNumero().getText()),
                            ((CbteTipo) p.getCbCbte().getSelectedItem()).getId());

                } catch (WSAFIPErrorResponseException ex) {
                    LOG.error(ex, ex);
                }
            }
        });
    }

    public List<Integer> getPuntoVentas() throws WSAFIPErrorResponseException {
        List<Integer> l = new ArrayList<>();
        for (PtoVenta pv : aFIPClient.getPtoVentaList()) {
            l.add((int) pv.getNro());
        }
        return l;
    }
}
