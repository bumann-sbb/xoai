package io.gdcc.xoai.serviceprovider.parsers;

import static org.junit.jupiter.api.Assertions.*;

import io.gdcc.xoai.model.oaipmh.verbs.Identify;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IdentifyParserTest {

    private InputStream input;
    private IdentifyParser parser;

    @BeforeEach
    public void setUp() {
        input = getClass().getClassLoader().getResourceAsStream("test/identify.xml");
    }

    @Test
    void parse() {
        parser = new IdentifyParser(input);
        Identify identify = parser.parse();

        assertEquals("Demo Dataverse Dataverse OAI Archive", identify.getRepositoryName());
        assertFalse(identify.getCompressions().isEmpty());
        assertEquals(2, identify.getCompressions().size());
    }
}
