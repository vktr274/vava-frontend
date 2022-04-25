module vava.project.main {
    requires javafx.graphics;
    requires javafx.fxml;
    requires httpcore;
    requires httpclient;
    requires httpmime;
    requires org.json;
    requires java.net.http;
    requires javafx.controls;

    exports sk.vava.zalospevaci;
    opens sk.vava.zalospevaci;
}