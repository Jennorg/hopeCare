module com.esperanza.hopecare {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.esperanza.hopecare.main to javafx.fxml;
    opens com.esperanza.hopecare.modules.facturacion.view to javafx.fxml;
    opens com.esperanza.hopecare.modules.Auth.view to javafx.fxml;

    opens com.esperanza.hopecare.modules.citas_consultas.model to javafx.base;
    opens com.esperanza.hopecare.modules.pacientes_medicos.model to javafx.base;
    opens com.esperanza.hopecare.common.model to javafx.base;
    opens com.esperanza.hopecare.modules.facturacion.dto to javafx.base;
    opens com.esperanza.hopecare.modules.Auth.model to javafx.base;
    opens com.esperanza.hopecare.modules.Auth.dto to javafx.base;

    exports com.esperanza.hopecare.main;
}
