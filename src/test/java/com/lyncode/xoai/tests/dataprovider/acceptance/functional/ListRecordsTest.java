package com.lyncode.xoai.tests.dataprovider.acceptance.functional;

import com.lyncode.xoai.dataprovider.exceptions.ConfigurationException;
import com.lyncode.xoai.dataprovider.exceptions.InvalidContextException;
import com.lyncode.xoai.dataprovider.exceptions.OAIException;
import com.lyncode.xoai.dataprovider.exceptions.WritingXmlException;
import com.lyncode.xoai.tests.dataprovider.acceptance.AbstractDataProviderTest;
import com.lyncode.xoai.tests.dataprovider.stubs.StubbedItem;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

import static com.lyncode.xoai.tests.SyntacticSugar.and;
import static com.lyncode.xoai.tests.SyntacticSugar.given;
import static org.hamcrest.MatcherAssert.assertThat;

public class ListRecordsTest extends AbstractDataProviderTest {
    @Test
    public void shouldReturnGivenItems () throws WritingXmlException, OAIException, InvalidContextException, IOException, XMLStreamException, ConfigurationException {
        given(theConfiguration().withMaxListRecords(10));
        and(given(theItemRepository()
                .withRandomItems(11)));

        afterHandling(aRequest().withVerb("ListRecords").withMetadataPrefix(theFormatPrefix()));

        assertThat(theResult(), hasXPath("//o:record/o:header/o:identifier"));
        assertThat(theResult(), hasXPath("//o:resumptionToken"));
    }

    private StubbedItem anItem() {
        return new StubbedItem();
    }
}
