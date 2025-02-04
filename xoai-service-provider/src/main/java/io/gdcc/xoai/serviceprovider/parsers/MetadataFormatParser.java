/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package io.gdcc.xoai.serviceprovider.parsers;

import static io.gdcc.xoai.model.oaipmh.Error.Code.ID_DOES_NOT_EXIST;
import static io.gdcc.xoai.model.oaipmh.Error.Code.NO_METADATA_FORMATS;
import static io.gdcc.xoai.xmlio.matchers.QNameMatchers.localPart;
import static io.gdcc.xoai.xmlio.matchers.XmlEventMatchers.aStartElement;
import static io.gdcc.xoai.xmlio.matchers.XmlEventMatchers.elementName;
import static io.gdcc.xoai.xmlio.matchers.XmlEventMatchers.text;
import static io.gdcc.xoai.xmlio.matchers.XmlEventMatchers.theEndOfDocument;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;

import io.gdcc.xoai.model.oaipmh.results.MetadataFormat;
import io.gdcc.xoai.serviceprovider.exceptions.IdDoesNotExistException;
import io.gdcc.xoai.serviceprovider.exceptions.InvalidOAIResponse;
import io.gdcc.xoai.xmlio.XmlReader;
import io.gdcc.xoai.xmlio.exceptions.XmlReaderException;
import java.io.InputStream;
import javax.xml.stream.events.XMLEvent;
import org.hamcrest.Matcher;

public class MetadataFormatParser {
    private final XmlReader reader;
    private boolean awaitingNextInvocation = false;

    public MetadataFormatParser(InputStream inputStream) {
        try {
            reader = new XmlReader(inputStream);
        } catch (XmlReaderException e) {
            throw new InvalidOAIResponse(e);
        }
    }

    public boolean hasNext() throws XmlReaderException, IdDoesNotExistException {
        if (!awaitingNextInvocation) {
            reader.next(metadataElement(), errorElement(), theEndOfDocument());
            awaitingNextInvocation = true;
        }
        if (reader.current(errorElement())) {
            String code = reader.getAttributeValue(localPart(equalTo("code")));
            if (equalTo(NO_METADATA_FORMATS.id()).matches(code)) return false;
            else if (ID_DOES_NOT_EXIST.id().equals(code)) throw new IdDoesNotExistException();
            else throw new InvalidOAIResponse("OAI responded with code: " + code);
        }
        return reader.current(metadataElement());
    }

    private Matcher<XMLEvent> errorElement() {
        return elementName(localPart(equalTo("error")));
    }

    private Matcher<XMLEvent> metadataElement() {
        return allOf(aStartElement(), elementName(localPart(equalTo("metadataFormat"))));
    }

    public MetadataFormat next() throws XmlReaderException, IdDoesNotExistException {
        if (!hasNext()) throw new XmlReaderException("No more metadata elements available");
        awaitingNextInvocation = false;
        return new MetadataFormat()
                .withMetadataPrefix(
                        reader.next(elementName(localPart(equalTo("metadataPrefix"))))
                                .next(text())
                                .getText())
                .withSchema(
                        reader.next(elementName(localPart(equalTo("schema"))))
                                .next(text())
                                .getText())
                .withMetadataNamespace(
                        reader.next(elementName(localPart(equalTo("metadataNamespace"))))
                                .next(text())
                                .getText());
    }
}
