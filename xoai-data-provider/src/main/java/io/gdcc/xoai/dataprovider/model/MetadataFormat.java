/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package io.gdcc.xoai.dataprovider.model;

import io.gdcc.xoai.dataprovider.exceptions.InternalOAIException;
import io.gdcc.xoai.dataprovider.filter.Condition;
import io.gdcc.xoai.dataprovider.filter.Scope;
import io.gdcc.xoai.dataprovider.filter.ScopedFilter;
import io.gdcc.xoai.model.oaipmh.ResumptionToken;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

public class MetadataFormat {
    public static Transformer identity() {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            // Prohibit the use of all protocols by external entities.
            // Protecting from SSRF etc.
            // See
            // https://sonarcloud.io/organizations/gdcc/rules?open=java%3AS2755&rule_key=java%3AS2755
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            return factory.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new InternalOAIException("Could not setup the identity transformer", e);
        }
    }

    public static MetadataFormat metadataFormat(String prefix) {
        return new MetadataFormat().withPrefix(prefix);
    }

    private Condition condition;
    private String prefix;
    private Transformer transformer;
    private String namespace;
    private String schemaLocation;

    public String getPrefix() {
        return prefix;
    }

    public MetadataFormat withPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public Transformer getTransformer() {
        return transformer;
    }

    public MetadataFormat withTransformer(Transformer transformer) {
        this.transformer = transformer;
        return this;
    }

    public String getNamespace() {
        return namespace;
    }

    public MetadataFormat withNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public String getSchemaLocation() {
        return schemaLocation;
    }

    public MetadataFormat withSchemaLocation(String schemaLocation) {
        this.schemaLocation = schemaLocation;
        return this;
    }

    public MetadataFormat withCondition(Condition filter) {
        this.condition = filter;
        return this;
    }

    /**
     * Create a scoped {@link io.gdcc.xoai.dataprovider.filter.Filter} to hide items not matching
     * the {@link Condition}.
     *
     * @return The scoped filter used with {@link
     *     io.gdcc.xoai.dataprovider.repository.ItemRepository#getItems(List, MetadataFormat, int,
     *     ResumptionToken.Value)} or {@link
     *     io.gdcc.xoai.dataprovider.repository.ItemRepository#getItemIdentifiers(List,
     *     MetadataFormat, int, ResumptionToken.Value)}. Will default to a transparent filter by
     *     using {@link Condition#ALWAYS_TRUE}.
     */
    public ScopedFilter getScopedFilter() {
        // if no condition is present, make the filter transparent by using always true
        return new ScopedFilter(
                this.condition == null ? Condition.ALWAYS_TRUE : this.condition,
                Scope.MetadataFormat);
    }

    public boolean isItemShown(ItemIdentifier item) {
        // null item means false (not shown), otherwise true (no condition), when condition present
        // check filter
        return item != null && condition == null || condition.isItemShown(item);
    }

    public io.gdcc.xoai.model.oaipmh.results.MetadataFormat toOAIPMH() {
        return new io.gdcc.xoai.model.oaipmh.results.MetadataFormat()
                .withMetadataNamespace(this.namespace)
                .withMetadataPrefix(this.prefix)
                .withSchema(this.schemaLocation);
    }
}
