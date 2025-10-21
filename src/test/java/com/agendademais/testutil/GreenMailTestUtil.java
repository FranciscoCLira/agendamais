package com.agendademais.testutil;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;

public class GreenMailTestUtil {

    public static GreenMailExtension createGreenMail() {
        return new GreenMailExtension(ServerSetupTest.SMTP);
    }

    public static String getHost() {
        return ServerSetupTest.SMTP.getBindAddress() == null ? "localhost" : ServerSetupTest.SMTP.getBindAddress();
    }

    public static int getPort() {
        return ServerSetupTest.SMTP.getPort();
    }
}
