package com.agendademais.services;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CryptoServiceTest {

    @Test
    public void decryptPlainReturnsPlainWhenNoMaster() {
        CryptoService cs = new CryptoService();
        String val = cs.decryptIfNeeded("plainpass");
        assertEquals("plainpass", val);
    }
}
