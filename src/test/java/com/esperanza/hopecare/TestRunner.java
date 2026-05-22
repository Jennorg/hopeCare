package com.esperanza.hopecare;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

public class TestRunner {
    public static void main(String[] args) {
        System.out.println("======================================================");
        System.out.println("   HopeCare - Reporte de Pruebas de Calidad          ");
        System.out.println("======================================================");

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(
                        DiscoverySelectors.selectPackage("com.esperanza.hopecare.tests")
                )
                .build();

        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);

        TestExecutionSummary summary = listener.getSummary();

        System.out.println("\n--- Resumen de Ejecución ---");
        System.out.println("Pruebas totales:    " + summary.getTestsStartedCount());
        System.out.println("Pruebas exitosas:   " + summary.getTestsSucceededCount());
        System.out.println("Pruebas fallidas:   " + summary.getTestsFailedCount());
        System.out.println("Pruebas abortadas:  " + summary.getTestsAbortedCount());
        System.out.println("Pruebas ignoradas:  " + summary.getTestsSkippedCount());
        System.out.println("Tiempo total:       " + (summary.getTimeFinished() - summary.getTimeStarted()) + " ms");

        if (summary.getTestsFailedCount() > 0) {
            System.out.println("\n--- Detalle de Fallos ---");
            summary.getFailures().forEach(failure -> {
                System.out.println("Prueba: " + failure.getTestIdentifier().getDisplayName());
                System.out.println("Error:  " + failure.getException().getMessage());
            });
            System.exit(1);
        } else {
            System.out.println("\nESTADO FINAL: TODAS LAS PRUEBAS PASARON CORRECTAMENTE.");
            System.exit(0);
        }
    }
}
