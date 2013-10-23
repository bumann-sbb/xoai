//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-147 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.01.13 at 08:24:23 PM WET 
//

package com.lyncode.xoai.dataprovider.xml.oaipmh;

import com.lyncode.xoai.dataprovider.exceptions.WritingXmlException;
import com.lyncode.xoai.dataprovider.xml.XMLWrittable;
import com.lyncode.xoai.util.DateUtils;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.math.BigInteger;
import java.util.Date;

/**
 * A resumptionToken may have 3 optional attributes and can be used in ListSets,
 * ListIdentifiers, ListRecords responses.
 * <p/>
 * <p/>
 * Java class for resumptionTokenType complex type.
 * <p/>
 * <p/>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p/>
 * <pre>
 * &lt;complexType name="resumptionTokenType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *       &lt;attribute name="expirationDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="completeListSize" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
 *       &lt;attribute name="cursor" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "resumptionTokenType", propOrder = {"value"})
public class ResumptionTokenType implements XMLWrittable {

    @XmlValue
    protected String value;
    @XmlAttribute(name = "expirationDate")
    @XmlSchemaType(name = "dateTime")
    protected Date expirationDate;
    @XmlAttribute(name = "completeListSize")
    @XmlSchemaType(name = "positiveInteger")
    protected Long completeListSize;
    @XmlAttribute(name = "cursor")
    @XmlSchemaType(name = "nonNegativeInteger")
    protected Long cursor;

    /**
     * Gets the value of the value property.
     *
     * @return possible object is {@link String }
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value allowed object is {@link String }
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the expirationDate property.
     *
     * @return possible object is {@link XMLGregorianCalendar }
     */
    public Date getExpirationDate() {
        return expirationDate;
    }

    /**
     * Sets the value of the expirationDate property.
     *
     * @param value allowed object is {@link XMLGregorianCalendar }
     */
    public void setExpirationDate(Date value) {
        this.expirationDate = value;
    }

    /**
     * Gets the value of the completeListSize property.
     *
     * @return possible object is {@link BigInteger }
     */
    public Long getCompleteListSize() {
        return completeListSize;
    }

    /**
     * Sets the value of the completeListSize property.
     *
     * @param value allowed object is {@link BigInteger }
     */
    public void setCompleteListSize(Number value) {
        this.completeListSize = value.longValue();
    }

    /**
     * Gets the value of the cursor property.
     *
     * @return possible object is {@link BigInteger }
     */
    public Long getCursor() {
        return cursor;
    }

    /**
     * Sets the value of the cursor property.
     *
     * @param value allowed object is {@link BigInteger }
     */
    public void setCursor(Number value) {
        this.cursor = value.longValue();
    }

    /*
     *
 *       &lt;attribute name="expirationDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="completeListSize" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
 *       &lt;attribute name="cursor" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
     */
    @Override
    public void write(XMLStreamWriter writter) throws WritingXmlException {
        try {
            if (this.expirationDate != null)
                writter.writeAttribute("expirationDate", DateUtils.format(expirationDate));
            if (this.completeListSize != null)
                writter.writeAttribute("completeListSize", "" + this.completeListSize);
            if (this.cursor != null)
                writter.writeAttribute("cursor", "" + this.cursor);
            if (this.value != null)
                writter.writeCharacters(this.value);
        } catch (XMLStreamException e) {
            throw new WritingXmlException(e);
        }
    }

}
