/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package io.gdcc.xoai.serviceprovider.parsers;

import static io.gdcc.xoai.xmlio.matchers.QNameMatchers.localPart;
import static io.gdcc.xoai.xmlio.matchers.XmlEventMatchers.aStartElement;
import static io.gdcc.xoai.xmlio.matchers.XmlEventMatchers.anEndElement;
import static io.gdcc.xoai.xmlio.matchers.XmlEventMatchers.elementName;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;

import io.gdcc.xoai.model.oaipmh.results.Record;
import io.gdcc.xoai.model.oaipmh.results.record.About;
import io.gdcc.xoai.model.oaipmh.results.record.Metadata;
import io.gdcc.xoai.serviceprovider.exceptions.InternalHarvestException;
import io.gdcc.xoai.serviceprovider.model.Context;
import io.gdcc.xoai.xml.XSLPipeline;
import io.gdcc.xoai.xmlio.XmlReader;
import io.gdcc.xoai.xmlio.exceptions.XmlReaderException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.TransformerException;
import org.hamcrest.Matcher;

public class RecordParser {
    private final Context context;
    private String metadataPrefix;

    public RecordParser(Context context, String metadataPrefix) {
        this.context = context;
        this.metadataPrefix = metadataPrefix;
    }

    public Record parse(XmlReader reader) throws XmlReaderException {
        HeaderParser headerParser = new HeaderParser();

        reader.next(elementName(localPart(equalTo("header"))));
        Record record = new Record().withHeader(headerParser.parse(reader));

        if (!record.getHeader().isDeleted()) {
            reader.next(elementName(localPart(equalTo("metadata")))).next(aStartElement());
            String content = reader.retrieveCurrentAsString();
            ByteArrayInputStream inputStream =
                    new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            XSLPipeline pipeline =
                    new XSLPipeline(inputStream, true)
                            .apply(context.getMetadataTransformer(metadataPrefix));

            if (context.hasTransformer()) pipeline.apply(context.getTransformer());

            try {
                record.withMetadata(new Metadata(new MetadataParser().parse(pipeline.process())));
            } catch (TransformerException e) {
                throw new InternalHarvestException("Unable to process transformer", e);
            }
        }

        if (reader.next(aboutElement(), endOfRecord()).current(aboutElement())) {
            reader.next(aStartElement());
            record.withAbout(new About(reader.retrieveCurrentAsString()));
        }

        return record;
    }

    private Matcher<XMLEvent> endOfRecord() {
        return allOf(anEndElement(), elementName(localPart(equalTo("record"))));
    }

    private Matcher<XMLEvent> aboutElement() {
        return elementName(localPart(equalTo("about")));
    }
}
