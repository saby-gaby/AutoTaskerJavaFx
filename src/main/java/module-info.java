module com.autotasker.autotasker {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.persistence;


    opens com.autotasker.model to org.hibernate.orm.core, javafx.base;
    opens com.autotasker to javafx.fxml;
    exports com.autotasker;
    exports com.autotasker.controller;
    opens com.autotasker.controller to javafx.fxml;
}