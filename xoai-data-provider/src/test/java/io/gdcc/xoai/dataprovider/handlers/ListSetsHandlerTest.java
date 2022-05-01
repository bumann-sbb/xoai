/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package io.gdcc.xoai.dataprovider.handlers;

import io.gdcc.xoai.dataprovider.exceptions.DoesNotSupportSetsException;
import io.gdcc.xoai.dataprovider.exceptions.IllegalVerbException;
import io.gdcc.xoai.dataprovider.exceptions.NoMatchesException;
import io.gdcc.xoai.model.oaipmh.ListSets;
import io.gdcc.xoai.model.oaipmh.ResumptionToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.xmlunit.matchers.HasXPathMatcher.hasXPath;
import static io.gdcc.xoai.dataprovider.model.Set.set;
import static io.gdcc.xoai.model.oaipmh.Verb.Type.ListSets;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ListSetsHandlerTest extends AbstractHandlerTest {
    protected ListSetsHandler underTest;

    @BeforeEach
    public void setup() {
        underTest = new ListSetsHandler(aContext(), theRepository());
    }

    @Test
    public void setVerbExpected() throws Exception {
        assertThrows(IllegalVerbException.class,
            () -> underTest.handle(a(request())));
    }

    @Test
    public void emptyRepositoryShouldGiveNoMatches() throws Exception {
        assertThrows(NoMatchesException.class,
            () -> underTest.handle(a(request().withVerb(ListSets))));
    }

    @Test
    public void doesNotSupportSets() throws Exception {
        theSetRepository().doesNotSupportSets();
        assertThrows(DoesNotSupportSetsException.class,
            () -> underTest.handle(a(request().withVerb(ListSets))));
    }

    @Test
    public void validResponseWithOnlyOnePage() throws Exception {
        theRepositoryConfiguration().withMaxListSets(100);
        theSetRepository().withRandomSets(10);
        ListSets handle = underTest.handle(a(request().withVerb(ListSets)));
        String result = write(handle);

        assertThat(result, xPath("count(//set)", asInteger(equalTo(10))));
        assertThat(result, not(hasXPath("//resumptionToken")));
    }

    @Test
    public void showsVirtualSetsFirst () throws Exception {
        theSetRepository().withSet("set", "hello");
        theContext().withSet(set("virtual").withName("new").withCondition(alwaysFalseCondition()));

        ListSets handle = underTest.handle(a(request().withVerb(ListSets)));
        String result = write(handle);

        assertThat(result, xPath("count(//set)", asInteger(equalTo(2))));
        assertThat(result, xPath("//set[1]/setSpec", equalTo("virtual")));
        assertThat(result, xPath("//set[2]/setSpec", equalTo("hello")));
    }

    @Test
    public void firstPageOfValidResponseWithTwoPages() throws Exception {
        theRepositoryConfiguration().withMaxListSets(5);
        theSetRepository().withRandomSets(10);
        ListSets handle = underTest.handle(a(request().withVerb(ListSets)));
        String result = write(handle);

        assertThat(result, xPath("count(//set)", asInteger(equalTo(5))));
        assertThat(result, hasXPath("//resumptionToken"));
    }

    @Test
    public void lastPageOfVResponseWithTwoPages() throws Exception {
        theRepositoryConfiguration().withMaxListSets(5);
        theSetRepository().withRandomSets(10);
        ListSets handle = underTest.handle(a(request().withVerb(ListSets)
                .withResumptionToken(valueOf(new ResumptionToken.Value().withOffset(5)))));
        String result = write(handle);

        assertThat(result, xPath("count(//set)", asInteger(equalTo(5))));
        assertThat(result, xPath("//resumptionToken", is(equalTo(""))));
    }

}
