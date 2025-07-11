module com.autotasker.autotasker {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires jakarta.validation;
    requires org.hibernate.orm.core;
    requires java.persistence;
    requires jbcrypt;
    requires mysql.connector.j;
    requires jakarta.mail;
    requires org.jsoup;
    requires simmetrics.core;


    opens com.autotasker.model to org.hibernate.orm.core, javafx.base;
    opens com.autotasker to javafx.fxml;
    exports com.autotasker;
    exports com.autotasker.controller;
    opens com.autotasker.controller to javafx.fxml;
    opens com.autotasker.dao to javafx.base, org.hibernate.orm.core;
}