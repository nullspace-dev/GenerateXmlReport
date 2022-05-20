package burp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.File;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.util.Properties;
import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class BurpExtender implements IBurpExtender, IExtensionStateListener, IHttpListener{
    private String folderPath = "/var/log/BurpSuiteEnterpriseEdition/scanResults";     // CHANGE ME!
    private String xmlFileName;
    private String htmlFileName;
    
    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;

    private PrintWriter stdout, stderr;
    private static String NAME = "Generate XML Report extension";
    private LocalDateTime extensionLoadedTime;
    private final List<LogEntry> log = new ArrayList<LogEntry>();
    private String siteName;
    
    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
    {
        this.callbacks = callbacks;
        // obtain an extension helpers object
        helpers = callbacks.getHelpers();

        stdout = new PrintWriter(callbacks.getStdout(), true);
        stderr = new PrintWriter(callbacks.getStderr(), true);

        callbacks.registerExtensionStateListener(this);
        callbacks.setExtensionName(NAME);

        
        extensionLoadedTime = LocalDateTime.now();

        stdout.println( NAME + " - Loaded");
    }

    @Override
    public void extensionUnloaded()
    {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
        
        xmlFileName = siteName + "xml-report-" + dtf.format(extensionLoadedTime) + ".xml";
        File xmlFile = new File(folderPath +  xmlFileName);
        
        htmlFileName = siteName + "xml-report-" + dtf.format(extensionLoadedTime) + ".xml";
        File htmlFile = new File(folderPath +  htmlFileName);

        IScanIssue[] scanIssues = callbacks.getScanIssues(null);
        
        try
        {
            callbacks.generateScanReport("XML", scanIssues, xmlFile);
            stdout.println( NAME + " - XML scan report saved at: " + xmlFile);
                      
        }
        catch (RuntimeException e)
        {
            stderr.println(NAME + " - XML report failed:\n" + e);
        }
        
        try {
            callbacks.generateScanReport("HTML", scanIssues, htmlFile);
            stdout.println( NAME + " - HTML scan report saved at: " + htmlFile);
        } catch (RuntimeException e)
        {
            stderr.println(NAME + " - HTML report failed:\n" + e);
        }
        
        
        String command = "sh /opt/burpsuite_enterprise/sendEmail.sh "+ xmlFullPath + " "+ htmlFullPath;
        
        try {
            Process process = Runtime.getRuntime().exec(command);
         
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
         
            reader.close();
         
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo)
    {
        // only process responses
        if (!messageIsRequest)
        {
            // create a new log entry with the message details
            synchronized(log)
            {
                
                log.add(new LogEntry(toolFlag, callbacks.saveBuffersToTempFiles(messageInfo), 
                        helpers.analyzeRequest(messageInfo).getUrl()));
                
            }
            if(siteName.length() != 0)
            	siteName = helpers.analyzeRequest(messageInfo).getUrl().getHost();
            
        }
    }
    
    private static class LogEntry
    {
        final int tool;
        final IHttpRequestResponsePersisted requestResponse;
        final URL url;

        LogEntry(int tool, IHttpRequestResponsePersisted requestResponse, URL url)
        {
            this.tool = tool;
            this.requestResponse = requestResponse;
            this.url = url;
        }
    }
    
    private static void sendMail(File file) {
        final String username = "your.mail.id@gmail.com";
        final String password = "your.password";
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.starttls.enable", true);
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("from.mail.id@gmail.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse("to.mail.id@gmail.com"));
            message.setSubject("Testing Subject");
            message.setText("Burp scan ");

            MimeBodyPart messageBodyPart = new MimeBodyPart();

            Multipart multipart = new MimeMultipart();
            
            //String file = "path of file to be attached";
            String fileName = "attachmentName";
            DataSource source = new FileDataSource(file);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(fileName);
            multipart.addBodyPart(messageBodyPart);

            message.setContent(multipart);

            System.out.println("Sending");

            Transport.send(message);
            System.out.println("Done");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    
    }
}
