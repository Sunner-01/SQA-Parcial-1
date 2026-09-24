package com.company.inventory.util;

import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class UtilAdditionalTest {
    @Test void binaryRoundTripAcrossSeveralBuffers() {
        byte[] input=new byte[8192]; new Random(2026).nextBytes(input);
        assertArrayEquals(input,Util.decompressZLib(Util.compressZLib(input)));
    }
    @Test void emptyRoundTrip() { assertArrayEquals(new byte[0],Util.decompressZLib(Util.compressZLib(new byte[0]))); }
    @Test void nullInputIsRejected() {
        assertThrows(NullPointerException.class,()->Util.compressZLib(null));
        assertThrows(NullPointerException.class,()->Util.decompressZLib(null));
    }
    // Caracterizacion del codigo original: formato invalido se silencia y devuelve vacio.
    @Test void malformedHeaderReturnsEmptyInsteadOfReportingError() { assertArrayEquals(new byte[0],Util.decompressZLib(new byte[]{1,2,3,4})); }
}
