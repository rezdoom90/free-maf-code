
package com.freemaf.agent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpeedModeTest {

    @Test

    void cyclesInDeclaredOrder() {

        assertEquals(SpeedMode.FAST, SpeedMode.INSTANT.next());

        assertEquals(SpeedMode.NORMAL, SpeedMode.FAST.next());

        assertEquals(SpeedMode.SLOW, SpeedMode.NORMAL.next());

        assertEquals(SpeedMode.VERY_SLOW, SpeedMode.SLOW.next());

        assertEquals(SpeedMode.INSTANT, SpeedMode.VERY_SLOW.next());

    }

    @Test

    void fromNameIsCaseInsensitiveWithInstantFallback() {

        assertEquals(SpeedMode.INSTANT, SpeedMode.fromName(null));

        assertEquals(SpeedMode.INSTANT, SpeedMode.fromName("garbage"));

        assertEquals(SpeedMode.FAST, SpeedMode.fromName("fast"));

        assertEquals(SpeedMode.VERY_SLOW, SpeedMode.fromName("VERY_SLOW"));

    }

}
