
package com.freemaf.agent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ContextPackagerTest {

    @Test

    void buildContextDoesNotThrow() {

        ContextPackager cp = new ContextPackager();

        assertNotNull(cp.buildContext());

    }

    @Test

    void listExistingFilesDoesNotThrow() {

        ContextPackager cp = new ContextPackager();

        assertNotNull(cp.listExistingFiles());

    }

}
