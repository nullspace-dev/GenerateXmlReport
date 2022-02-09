package burp;

import java.io.PrintWriter;
import java.io.File;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class BurpExtender implements IBurpExtender, IExtensionStateListener
{
    private String folderPath = "/var/log/BurpSuiteEnterpriseEdition/";     // CHANGE ME!
    private String fileName;

    private IBurpExtenderCallbacks callbacks;
    private PrintWriter stdout;
    private static String NAME = "Generate XML Report extension";
    private LocalDateTime extensionLoadedTime;

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
    {
        this.callbacks = callbacks;

        stdout = new PrintWriter(callbacks.getStdout(), true);

        callbacks.registerExtensionStateListener(this);
        callbacks.setExtensionName(NAME);

        extensionLoadedTime = LocalDateTime.now();

        stdout.println( NAME + " loaded");
    }

    @Override
    public void extensionUnloaded()
    {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

        fileName = "xml-report-" + dtf.format(extensionLoadedTime) + ".xml";

        File file = new File(folderPath + fileName);

        callbacks.generateScanReport("XML", callbacks.getScanIssues(null), file);

        stdout.println( NAME + " - XML scan report saved at: " + file);
    }
}
