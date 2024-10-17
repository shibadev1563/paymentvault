package utils;

import com.sun.mail.util.MailSSLSocketFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class EmailLib {
    static String Dir = System.getProperty("user.dir");
    public static String shost = "smtp.office365.com"; // Outlook SMTP server
    public static String sSub = null;
    public static String sRece = "uchandrakar143@gmail.com,Rashmi1dhandar@gmail.com";
    public static String sFrom = "automationuser1507@outlook.com"; // Your Outlook email
    public static String sPass = "Auto1507@"; // Your Outlook password
    public static String sPort = "587"; // TLS port
    public static String sSMTP = "smtp.office365.com";
    public static String sTLS = "true"; // Use TLS

    public static void sendEmail() {
        String ExecutionReport = Paths.get(Dir, "target", "SparkReport", "ExtentSparkReport.html").toString();

        try {
            HashMap<String, Integer> ExecutionStatus = getReportDetails();
            Date today = Calendar.getInstance().getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");

            Properties props = new Properties();
            MailSSLSocketFactory sf = new MailSSLSocketFactory();
            sf.setTrustAllHosts(true);
            props.put("mail.smtp.ssl.trust", "*");
            props.put("mail.smtp.ssl.socketFactory", sf);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", shost);
            props.put("mail.smtp.port", sPort);
            props.put("mail.smtp.ssl.trust", shost);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2"); // Use TLSv1.2

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(sFrom, sPass);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sFrom));
            String[] recipientList = sRece.split(",");
            InternetAddress[] recipientAddress = new InternetAddress[recipientList.length];
            int counter = 0;
            for (String recipient : recipientList) {
                recipientAddress[counter] = new InternetAddress(recipient.trim());
                counter++;
            }

            message.setRecipients(Message.RecipientType.TO, recipientAddress);
            sSub = "Payment Vault Application";
            message.setSubject(sSub + " - Regression Suite Execution Report - " + sdf.format(today));

            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent("<p>Hi Team,<br/><br/><i>Greeting!</i><br/>"
                    + "Please find the attached report for the execution<b>:::"
                    + "<font color=\"red\">CompleteExecutionReport</font></b> which contains<br/> system info, test steps, "
                    + "description, as well as execution time for every script.<br/>"
                    + "The overall execution report is as follows:<br/>"
                    + "<table border=\"1\"><tr><th>EXECUTION STATUS</th><th>RESULT COUNT</th></tr>"
                    + "<tr><td>TEST SCRIPTS PASS</td><td align=\"center\"><font color=\"green\">"
                    + "<b>" + ExecutionStatus.getOrDefault("Passed Features", 0) + "</b></font></td></tr>"
                    + "<tr><td>TEST SCRIPTS FAILED</td><td align=\"center\"><font color=\"red\"><b>"
                    + ExecutionStatus.getOrDefault("Failed Features", 0) + "</b></font></td></tr>"
                    + "<tr><td>TEST SCRIPTS OTHERS<br/>(SKIPPED)</td><td align=\"center\"><b><font color=\"orange\">"
                    + (ExecutionStatus.getOrDefault("Total Features", 0) - ExecutionStatus.getOrDefault("Passed Features", 0) - ExecutionStatus.getOrDefault("Failed Features", 0)) + "</font></b></td></tr>"
                    + "<tr><td>TEST SCENARIOS PASSED</td><td align=\"center\"><b><font color=\"green\">"
                    + ExecutionStatus.getOrDefault("Passed Scenarios", 0) + "</font></b></td></tr>"
                    + "<tr><td>TEST SCENARIOS FAILED</td><td align=\"center\"><b><font color=\"red\">"
                    + ExecutionStatus.getOrDefault("Failed Scenarios", 0) + "</font></b></td></tr>"
                    + "<tr><td>TEST SCENARIOS OTHERS<br/>(SKIPPED)</td><td align=\"center\"><b><font color=\"orange\">"
                    + (ExecutionStatus.getOrDefault("Total Scenarios", 0) - ExecutionStatus.getOrDefault("Passed Scenarios", 0) - ExecutionStatus.getOrDefault("Failed Scenarios", 0)) + "</font></b></td></tr>"
                    + "<tr><td>TEST STEPS PASSED</td><td align=\"center\"><b><font color=\"green\">"
                    + ExecutionStatus.getOrDefault("Passed Steps", 0) + "</font></b></td></tr>"
                    + "<tr><td>TEST STEPS FAILED</td><td align=\"center\"><b><font color=\"red\">"
                    + ExecutionStatus.getOrDefault("Failed Steps", 0) + "</font></b></td></tr>"
                    + "<tr><td>TEST STEPS OTHERS<br/>(SKIPPED)</td><td align=\"center\"><b><font color=\"orange\">"
                    + (ExecutionStatus.getOrDefault("Total Steps", 0) - ExecutionStatus.getOrDefault("Passed Steps", 0) - ExecutionStatus.getOrDefault("Failed Steps", 0)) + "</font></b></td></tr>"
                    + "</table><br/><br/>"
                    + "Thanks,<br/><font color=\"dark blue\"><b>Payment Vault Automation Team</b></font></p>", "text/html");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            MimeBodyPart attachPart = new MimeBodyPart();
            DataSource source = new FileDataSource(ExecutionReport);
            attachPart.setDataHandler(new DataHandler(source));
            attachPart.setFileName(new File(ExecutionReport).getName());
            multipart.addBodyPart(attachPart);

            message.setContent(multipart, "text/html");
            Transport.send(message);
            System.out.println("Test Report mail sent successfully via " + shost + "\nTo: " + sRece);

        } catch (Exception e) {
            System.out.println("Test Report mail not sent via " + shost);
            e.printStackTrace();
        }
    }

    public static HashMap<String, Integer> getReportDetails() {
        String reportPath = Paths.get(System.getProperty("user.dir"), "target", "SparkReport", "ExtentSparkReport.html").toString();
        HashMap<String, Integer> reportDetails = new HashMap<>();

        try {
            File reportFile = new File(reportPath);
            Document doc = Jsoup.parse(reportFile, "UTF-8");

            // Initialize counters
            int totalFeatures = 0;
            int passedFeatures = 0;
            int failedFeatures = 0;
            int totalScenarios = 0;
            int passedScenarios = 0;
            int failedScenarios = 0;
            int totalSteps = 0;
            int passedSteps = 0;
            int failedSteps = 0;

            Elements featureElements = doc.select(".feature");  // Adjust the selector based on your report structure
            totalFeatures = featureElements.size();

            for (Element feature : featureElements) {
                String featureStatus = feature.select(".status").text();
                if ("pass".equalsIgnoreCase(featureStatus)) {
                    passedFeatures++;
                } else if ("fail".equalsIgnoreCase(featureStatus)) {
                    failedFeatures++;
                }

                Elements scenarioElements = feature.select(".scenario");
                totalScenarios += scenarioElements.size();

                for (Element scenario : scenarioElements) {
                    String scenarioStatus = scenario.select(".status").text();
                    if ("pass".equalsIgnoreCase(scenarioStatus)) {
                        passedScenarios++;
                    } else if ("fail".equalsIgnoreCase(scenarioStatus)) {
                        failedScenarios++;
                    }

                    Elements stepElements = scenario.select(".step");
                    totalSteps += stepElements.size();

                    for (Element step : stepElements) {
                        String stepStatus = step.select(".status").text();
                        if ("pass".equalsIgnoreCase(stepStatus)) {
                            passedSteps++;
                        } else if ("fail".equalsIgnoreCase(stepStatus)) {
                            failedSteps++;
                        }
                    }
                }
            }

            // Populate the result map
            reportDetails.put("Total Features", totalFeatures);
            reportDetails.put("Passed Features", passedFeatures);
            reportDetails.put("Failed Features", failedFeatures);
            reportDetails.put("Total Scenarios", totalScenarios);
            reportDetails.put("Passed Scenarios", passedScenarios);
            reportDetails.put("Failed Scenarios", failedScenarios);
            reportDetails.put("Total Steps", totalSteps);
            reportDetails.put("Passed Steps", passedSteps);
            reportDetails.put("Failed Steps", failedSteps);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return reportDetails;
    }
}
