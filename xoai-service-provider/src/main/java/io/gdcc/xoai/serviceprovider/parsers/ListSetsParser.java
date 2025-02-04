/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package io.gdcc.xoai.serviceprovider.parsers;

import static io.gdcc.xoai.model.oaipmh.Error.Code.NO_RECORDS_MATCH;
import static io.gdcc.xoai.model.oaipmh.Error.Code.NO_SET_HIERARCHY;
import static io.gdcc.xoai.xmlio.matchers.QNameMatchers.localPart;
import static io.gdcc.xoai.xmlio.matchers.XmlEventMatchers.aStartElement;
import static io.gdcc.xoai.xmlio.matchers.XmlEventMatchers.anEndElement;
import static io.gdcc.xoai.xmlio.matchers.XmlEventMatchers.elementName;
import static io.gdcc.xoai.xmlio.matchers.XmlEventMatchers.text;
import static io.gdcc.xoai.xmlio.matchers.XmlEventMatchers.theEndOfDocument;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;

import io.gdcc.xoai.model.oaipmh.results.Set;
import io.gdcc.xoai.serviceprovider.exceptions.EncapsulatedKnownException;
import io.gdcc.xoai.serviceprovider.exceptions.InvalidOAIResponse;
import io.gdcc.xoai.serviceprovider.exceptions.NoSetHierarchyException;
import io.gdcc.xoai.xmlio.XmlReader;
import io.gdcc.xoai.xmlio.exceptions.XmlReaderException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.events.XMLEvent;
import org.hamcrest.Matcher;

public class ListSetsParser {
    private final XmlReader reader;
    private boolean awaitingNextInvocation = false;

    public ListSetsParser(XmlReader reader) {
        this.reader = reader;
    }

    public boolean hasNext() throws XmlReaderException {
        if (!awaitingNextInvocation)
            reader.next(setElement(), errorElement(), resumptionToken(), theEndOfDocument());
        awaitingNextInvocation = true;
        if (reader.current(errorElement())) {
            String code = reader.getAttributeValue(localPart(equalTo("code")));
            if (equalTo(NO_RECORDS_MATCH.id()).matches(code)) return false;
            else if (equalTo(NO_SET_HIERARCHY.id()).matches(code))
                throw new EncapsulatedKnownException(new NoSetHierarchyException());
            else throw new InvalidOAIResponse("OAI responded with code: " + code);
        }
        return reader.current(setElement());
    }

    private Matcher<XMLEvent> resumptionToken() {
        return allOf(aStartElement(), elementName(localPart(equalTo("resumptionToken"))));
    }

    public Set next() throws XmlReaderException {
        if (!hasNext()) throw new XmlReaderException("No more identifiers available");
        awaitingNextInvocation = false;
        return parseSet();
    }

    @SuppressWarnings("unchecked")
    private Set parseSet() throws XmlReaderException {
        Set set = new Set();

        String setName = null;
        String setSpec = null;

        while (setName == null || setSpec == null) {
            reader.next(aStartElement());
            QName elementName = reader.getName();
            reader.next(text());
            String extractedText = reader.getText();
            while (reader.next(anEndElement(), text()).current(text())) {
                extractedText += reader.getText();
            }
            if (elementName.getLocalPart().equals("setName")) {
                setName = extractedText;
            } else if (elementName.getLocalPart().equals("setSpec")) {
                setSpec = extractedText;
            }
        }
        set.withName(setName);
        set.withSpec(setSpec);
        return set;
    }

    private Matcher<XMLEvent> errorElement() {
        return elementName(localPart(equalTo("error")));
    }

    private Matcher<XMLEvent> setElement() {
        return allOf(aStartElement(), elementName(localPart(equalTo("set"))));
    }

    private Matcher<XMLEvent> endSetElement() {
        return allOf(anEndElement(), elementName(localPart(equalTo("set"))));
    }

    /**
     * Parses its xml completely
     *
     * @return - All sets within the xml
     * @throws XmlReaderException
     */
    public List<Set> parse() throws XmlReaderException {
        List<Set> sets = new ArrayList<Set>();
        while (hasNext()) sets.add(next());
        return sets;
    }
}
