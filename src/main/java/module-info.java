module xyz.grantlmul.xbfl {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.eclipse.jetty.server;
    requires org.eclipse.jetty.util;
    requires com.google.gson;
    requires org.apache.commons.io;
    requires org.apache.httpcomponents.client5.httpclient5;
    requires java.net.http;
    requires java.desktop;
    opens xyz.grantlmul.xbfl;
    exports xyz.grantlmul.xbfl;
}
