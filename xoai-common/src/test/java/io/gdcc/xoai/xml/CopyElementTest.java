package io.gdcc.xoai.xml;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;
import static org.xmlunit.matchers.HasXPathMatcher.hasXPath;

import io.gdcc.xoai.util.ReplacingInputStream;
import io.gdcc.xoai.xmlio.exceptions.XmlWriteException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;
import javax.xml.stream.XMLStreamException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xmlunit.matchers.EvaluateXPathMatcher;

public class CopyElementTest {

    /**
     * A testing copy element variant, simple copying the input stream to the output.
     *
     * <p>This might be fine to use, when you guarantee not to send XML having a processing
     * instruction at the top! (Which is usually the case with pregenerated XML files)
     *
     * <p>Performance is best (obviously) and IO bound only.
     */
    public static class CopyElementStreamCopy extends CopyElement {
        public CopyElementStreamCopy(InputStream xmlInputStream) {
            super(xmlInputStream);
        }

        @Override
        protected void writeXml(XmlWriter writer) throws IOException {
            // Do not close the output stream in this try-with-resources, as the XmlWriter takes
            // care of
            // that.
            try (xmlInputStream) {
                // now lets copy the data
                xmlInputStream.transferTo(writer.getOutputStream());
            }
        }
    }

    /**
     * A testing copy element variant using the deprecated {@link ReplacingInputStream} originating
     * from Inbot and shipped with Apache POI. It is SLOW SLOW SLOW and should not be used. Here to
     * be used in JMH benchmarking {@link io.gdcc.xoai.tests.util.EchoElementBenchmark}.
     *
     * <p>Note also that when the XML processing instruction uses a different style (whitespace,
     * quotes, ...), this will fail to produce valid XML.
     *
     * <p>See also <a
     * href="http://github.com/IQSS/dataverse/blob/e8435ac1fe73cda2b0e1e50c398370b8aa5eb94a/src/main/java/edu/harvard/iq/dataverse/harvest/server/xoai/Xrecord.java#L88-L92">old
     * Dataverse Xrecord class</a>
     */
    @Deprecated
    public static class CopyElementStreamReplace extends CopyElement {
        public CopyElementStreamReplace(InputStream xmlInputStream) {
            super(xmlInputStream);
        }

        @Override
        protected void writeXml(XmlWriter writer) throws IOException {
            // Do not close the output stream in this try-with-resources, as the XmlWriter takes
            // care of
            // that.
            try (ReplacingInputStream inputStream =
                    new ReplacingInputStream(
                            xmlInputStream, "<?xml version='1.0' encoding='UTF-8'?>", ""); ) {
                // now lets read, filter and write data
                inputStream.transferTo(writer.getOutputStream());
            }
        }
    }

    /**
     * A testing copy element variant using a BufferedReader to filter for the processing
     * instruction. It's much slower than the stream copy as it is CPU bound, but it's still at
     * least 2.5x faster than {@link EchoElement} and still 42x faster than using {@link
     * ReplacingInputStream}. Note though, that by using the newline dependency, it might create
     * invalid XML by skipping more than just the XML processing instruction, and it needs to pass
     * every line into a String, which uses more memory than necessary and causes String creation
     * overhead.
     */
    public static class CopyElementBuffered extends CopyElement {
        public CopyElementBuffered(InputStream xmlInputStream) {
            super(xmlInputStream);
        }

        @Override
        protected void writeXml(XmlWriter writer) throws IOException {
            // Also InputStream.transferTo recommends closing both streams when an IOException
            // occurs,
            // we may not close the XMLWriter output stream, but we close the InputStream. The
            // stream writer may not be closed, as it would close the output stream, too!
            BufferedWriter streamWriter =
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    writer.getOutputStream(), StandardCharsets.UTF_8));
            try (xmlInputStream;
                    BufferedReader streamReader =
                            new BufferedReader(
                                    new InputStreamReader(
                                            xmlInputStream, StandardCharsets.UTF_8))) {
                // now lets read, filter and write data
                for (String line = streamReader.readLine();
                        line != null;
                        line = streamReader.readLine()) {
                    if (!line.contains("<?xml")) {
                        streamWriter.append(line);
                        streamWriter.newLine();
                    }
                }
                // flush the stream writer to make sure everything has been written out before the
                // XmlWriter
                // sends others
                streamWriter.flush();
            }
        }
    }

    @Test
    void writeStream() throws IOException, XMLStreamException, XmlWriteException {
        // given
        String sourceXml =
                "<?xml version='1.0' encoding='UTF-8'?><oai_dc:dc"
                    + " xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\""
                    + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                    + " xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/"
                    + " http://www.openarchives.org/OAI/2.0/oai_dc.xsd\">\n"
                    + "\t<dc:title xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Invasive"
                    + " Lithobates catesbeianus - American bullfrog occurrences in"
                    + " Flanders</dc:title>\n"
                    + "\t<dc:subject"
                    + " xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Occurrence</dc:subject>\n"
                    + "\t<dc:subject"
                    + " xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Observation</dc:subject>\n"
                    + "</oai_dc:dc>";
        String expectedXml =
                "<?xml version='1.0' encoding='UTF-8'?><metadata><oai_dc:dc"
                    + " xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\""
                    + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                    + " xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/"
                    + " http://www.openarchives.org/OAI/2.0/oai_dc.xsd\">\n"
                    + "\t<dc:title xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Invasive"
                    + " Lithobates catesbeianus - American bullfrog occurrences in"
                    + " Flanders</dc:title>\n"
                    + "\t<dc:subject"
                    + " xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Occurrence</dc:subject>\n"
                    + "\t<dc:subject"
                    + " xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Observation</dc:subject>\n"
                    + "</oai_dc:dc></metadata>";
        final ByteArrayOutputStream resultStream = new ByteArrayOutputStream();

        // when
        try (resultStream;
                InputStream stream =
                        new ByteArrayInputStream(sourceXml.getBytes(StandardCharsets.UTF_8));
                XmlWriter writer = new XmlWriter(resultStream)) {
            writer.writeStartDocument();
            writer.writeStartElement("metadata");
            writer.write(new CopyElement(stream));
            writer.writeEndElement();
            writer.writeEndDocument();
        }
        String result = resultStream.toString();

        // then
        assertThat("CopyElement handles InputStream correctly", result, isSimilarTo(expectedXml));
    }

    @Test
    void writeLargeStream() throws IOException, XMLStreamException, XmlWriteException {
        // given
        final Path sourceFile = Path.of("src", "test", "resources", "ddi-codebook-2.5-example.xml");
        final String sourceXml = Files.readString(sourceFile, StandardCharsets.UTF_8);
        final ByteArrayOutputStream resultStream = new ByteArrayOutputStream();

        // when
        try (resultStream;
                InputStream stream =
                        new ByteArrayInputStream(sourceXml.getBytes(StandardCharsets.UTF_8));
                XmlWriter writer = new XmlWriter(resultStream)) {
            writer.writeStartDocument();
            writer.writeStartElement("metadata");
            writer.write(new CopyElement(stream));
            writer.writeEndElement();
            writer.writeEndDocument();
        }
        String result = resultStream.toString();

        // then
        assertThat("Large stream XML wrapped in <metadata>", result, hasXPath("//metadata"));
    }

    /**
     * This test checks that we do not bodge multibyte UTF-8 chars from the input stream. See <a
     * href="https://github.com/gdcc/xoai/issues/188">issue 188 at gdcc/xoai</a>.
     */
    @Test
    void ensureIssue188Fixed() throws IOException, XMLStreamException {
        // given
        final Path sourceFile =
                Path.of("src", "test", "resources", "188-copy-element-multibyte.xml");
        final String sourceXml = Files.readString(sourceFile, StandardCharsets.UTF_8);
        final ByteArrayOutputStream resultStream = new ByteArrayOutputStream();

        final String description =
                "This dataset contains information of experiments carried out upland rice in two"
                    + " regions of Nicaragua (Caribbean and Pacific Region), as well as a"
                    + " compilation of soils data from different regions in Nicaragua collected"
                    + " from 2015 to 2019. The experiments were designed to explore the effects of"
                    + " micronutrients in the yield of upland rice. The experiments were carried"
                    + " out on farmer’s field during the 2018 production cycle, the dataset"
                    + " contains yield and aerial biomass of the experiments.";

        assertThat(
                "Description is intact in source",
                sourceXml,
                EvaluateXPathMatcher.hasXPath("//dc:description/text()", equalTo(description))
                        .withNamespaceContext(Map.of("dc", "http://purl.org/dc/elements/1.1/")));

        // when
        try (resultStream;
                InputStream stream =
                        new ByteArrayInputStream(sourceXml.getBytes(StandardCharsets.UTF_8));
                XmlWriter writer = new XmlWriter(resultStream)) {
            writer.writeStartDocument();
            writer.writeStartElement("metadata");
            writer.write(new CopyElement(stream));
            writer.writeEndElement();
            writer.writeEndDocument();
        }
        String result = resultStream.toString();

        // then
        assertThat("Large stream XML wrapped in <metadata>", result, hasXPath("//metadata"));
        assertThat(
                "Has a <dc:description> element",
                result,
                hasXPath("//dc:description")
                        .withNamespaceContext(Map.of("dc", "http://purl.org/dc/elements/1.1/")));
        assertThat(
                "Description is still intact",
                result,
                EvaluateXPathMatcher.hasXPath("//dc:description/text()", equalTo(description))
                        .withNamespaceContext(Map.of("dc", "http://purl.org/dc/elements/1.1/")));
    }

    static Stream<Arguments> multicharString() {
        return Stream.of(
                Arguments.of(4, "test", 0), // 1-byte ending
                // 2-byte ending (complete, but we can't know that!)
                Arguments.of(4, "testß", 0),
                // 2-byte ending on 1st byte
                Arguments.of(4, "testß", 1),
                // 3-byte ending on 2nd byte
                Arguments.of(4, "test’", 1),
                // 3-byte ending on 3rd byte (complete, but we can't know that!)
                Arguments.of(4, "test♥", 0),
                // 3-byte ending on 2nd byte
                Arguments.of(4, "test♥", 1),
                // 3-byte ending on 1st byte
                Arguments.of(4, "test♥", 2),
                // 4-byte ending on 3rd byte
                Arguments.of(4, "test\uD83D\uDD2B", 1));
    }

    @MethodSource("multicharString")
    @ParameterizedTest
    void ensureBufferAnalysisCorrectness(int expectedLength, String text, int bytesToCutOf)
            throws UnsupportedEncodingException {
        // given
        // System.out.println(convertToBinary(text));
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        // System.out.println(Arrays.toString(bytes));

        // when
        byte[] sut = Arrays.copyOf(bytes, bytes.length - bytesToCutOf);
        int length = CopyElement.maxBytesWithCompleteUTF8Chars(sut);

        // then
        assertEquals(expectedLength, length);
    }

    static String convertToBinary(String input) {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(" ", "0"));
            sb.append(" ");
        }
        return sb.toString();
    }
}
