//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2017.08.30 um 05:56:22 AM CEST 
//


package situationtemplate.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für tSituation complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="tSituation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="situationNode" type="{}tSituationNode"/>
 *         &lt;element name="operationNode" type="{}tOperationNode" maxOccurs="unbounded"/>
 *         &lt;element name="contextNode" type="{}tContextNode" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tSituation", propOrder = {
    "situationNode",
    "operationNode",
    "contextNode"
})
public class TSituation {

    @XmlElement(required = true)
    protected TSituationNode situationNode;
    @XmlElement(required = true)
    protected List<TOperationNode> operationNode;
    @XmlElement(required = true)
    protected List<TContextNode> contextNode;
    @XmlAttribute(name = "name")
    protected String name;

    /**
     * Ruft den Wert der situationNode-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TSituationNode }
     *     
     */
    public TSituationNode getSituationNode() {
        return situationNode;
    }

    /**
     * Legt den Wert der situationNode-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TSituationNode }
     *     
     */
    public void setSituationNode(TSituationNode value) {
        this.situationNode = value;
    }

    /**
     * Gets the value of the operationNode property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the operationNode property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOperationNode().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TOperationNode }
     * 
     * 
     */
    public List<TOperationNode> getOperationNode() {
        if (operationNode == null) {
            operationNode = new ArrayList<TOperationNode>();
        }
        return this.operationNode;
    }

    /**
     * Gets the value of the contextNode property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the contextNode property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContextNode().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TContextNode }
     * 
     * 
     */
    public List<TContextNode> getContextNode() {
        if (contextNode == null) {
            contextNode = new ArrayList<TContextNode>();
        }
        return this.contextNode;
    }

    /**
     * Ruft den Wert der name-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Legt den Wert der name-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

}
