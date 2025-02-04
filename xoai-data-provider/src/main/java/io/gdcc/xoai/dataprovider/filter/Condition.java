/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package io.gdcc.xoai.dataprovider.filter;

import io.gdcc.xoai.dataprovider.model.ItemIdentifier;

/**
 * @author Development @ Lyncode
 * @version 3.1.0
 */
public interface Condition {
    Condition ALWAYS_TRUE = () -> Filter.ALWAYS_TRUE;
    Condition ALWAYS_FALSE = () -> Filter.ALWAYS_FALSE;

    Filter getFilter();

    default boolean isItemShown(ItemIdentifier item) {
        return item != null && getFilter() != null && getFilter().isItemShown(item);
    }
}
