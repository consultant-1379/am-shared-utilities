/*
 * COPYRIGHT Ericsson 2024
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 */
package com.ericsson.am.shared.retries;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class TransactionTestData {

    @Transactional
    public void doSomeMagicInTransaction() {
        // do some magic in transaction
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void doSomeMagicInNewTransaction() {
        // do some magic in new transaction
    }

    public void doSomeMagicOnClassTransaction() {
        // do some magic on class transaction
    }

}
