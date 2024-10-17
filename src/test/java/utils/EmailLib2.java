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

public class EmailLib2 {
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
        String ExecutionReport = Paths.get("target", "SparkReport", "ExtentSparkReport.html").toString();

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
            message.setSubject(sSub + " with suite as Regression started on " + sdf.format(today));

            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent("<p>Hi Team,<br/><br/><i>Greeting!</i><br/>"
                    + "Could you Please find the attached report for execution<b>:::"
                    + "<font color=\"red\">CompleteExecutionReport</font></b> which contains<br/> system info,Test step,"
                    + "description as well as execution time for every script<br/> and "
                    + "following is the overall execution report for execution::"
                    + "<table border=\"1\"><tr><th></b>EXECUTION STATUS</b></th>"
                    + "<b>RESULT COUNT</b><th></th></tr><tr><td>TEST SCRIPTS PASS</td><td align=\"center\"><font color=\"green\">"
                    + "<b>" + ExecutionStatus.get("TEST PASS")
                    + "</b></font></td></tr><tr><td>TEST SCRIPTS FAILED</td><td align=\"center\">"
                    + "<font color=\"red\"><b>" + ExecutionStatus.get("TEST FAILED") + "</b></font></td></tr>"
                    + "<tr><td>TEST SCRIPTS OTHERS<br/>(FATAL,WARNING,ERROR)</td><td align=\"center\"><b><font color=\"orange\">"
                    + ExecutionStatus.get("TEST OTHERS") + "</font></b></td></tr>"
                    + "<tr><td>TEST STEP PASSED</td><td align=\"center\"><b><font color=\"green\">"
                    + ExecutionStatus.get("TEST STEP PASS") + "</font></b>"
                    + "</td></tr><tr><td>TEST STEP FAILED</td><td align=\"center\"><b><font color=\"red\">"
                    + ExecutionStatus.get("TEST STEP FAILED") + "</font></b></td></tr>"
                    + "</tr><tr><td>TEST STEP OTHERS<br/>(INFO,FATAL,WARNING,ERROR)</td><td align=\"center\"><b><font color=\"orange\">"
                    + ExecutionStatus.get("TEST STEP OTHERS") + "</font></b></td></tr></table></b><br/><br/>"
                    + "Thanks,<p color=\"dark blue\"><b>Payment Vault Automation Team</b>", "text/html");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            MimeBodyPart attachPart = new MimeBodyPart();
            DataSource source = new FileDataSource(ExecutionReport);
            attachPart.setDataHandler(new DataHandler(source));
            attachPart.setFileName(new File(ExecutionReport).getName());
            multipart.addBodyPart(attachPart);

            message.setContent(multipart, "text/html");
            Transport.send(message);
            System.out.println("Test Report mail is sent successfully via - " + shost + "\n To:" + sRece);

        } catch (Exception e) {
            System.out.println("Test Report mail is not sent via - " + shost);
            e.printStackTrace();
        }
    }

    public static HashMap<String, Integer> getReportDetails() {
        String reportPath = System.getProperty("user.dir")+"/target/SparkReport/ExtentSparkReport.html";
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

            // Example: Adjust the selectors based on your report structure
            Elements featureElements = doc.select(".feature");
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
