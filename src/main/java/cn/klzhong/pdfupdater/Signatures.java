package cn.klzhong.pdfupdater;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.CertificateInfo;
import com.itextpdf.text.pdf.security.CertificateVerification;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.PrivateKeySignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import com.itextpdf.text.pdf.security.VerificationException;

public class Signatures {
    /** The resulting PDF */
    public static String ORIGINAL = "results/hello.pdf";
    /** The resulting PDF */
    public static String SIGNED1 = "results/signature_1.pdf";
    /** The resulting PDF */
    public static String SIGNED2 = "results/signature_2.pdf";
    /** Info after verification of a signed PDF */
    public static String VERIFICATION = "results/verify.txt";
    /** The resulting PDF */
    public static String REVISION = "results/revision_1.pdf";

    /**
     * A properties file that is PRIVATE.
     * You should make your own properties file and adapt this line.
     */
    public static String PATH = "/key.properties";
    /** Some properties used when signing. */
    public static Properties properties = new Properties();
    /** One of the resources. */
    public static final String RESOURCE = "src/main/resources/test.png";

    /**
     * Creates a PDF document.
     * @param filename the path to the new PDF document
     * @throws DocumentException
     * @throws IOException
     */
    public void createPdf(String filename) throws IOException, DocumentException {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filename));
        document.open();
        document.add(new Paragraph("Hello World!"));
        document.close();
    }

    /**
     * Manipulates a PDF file src with the file dest as result
     * @param src the original PDF
     * @param dest the resulting PDF
     * @throws IOException
     * @throws DocumentException
     * @throws GeneralSecurityException
     */
    public void signPdfFirstTime(String src, String dest)
        throws IOException, DocumentException, GeneralSecurityException {
        String path = properties.getProperty("PRIVATE");
        String keystore_password = properties.getProperty("PASSWORD");
        String key_password = properties.getProperty("PASSWORD");
        KeyStore ks = KeyStore.getInstance("pkcs12", "BC");
        InputStream privateResource = this.getClass().getResourceAsStream(path);
        //ks.load(new FileInputStream(path), keystore_password.toCharArray());
        ks.load(privateResource, keystore_password.toCharArray());
        String alias = (String)ks.aliases().nextElement();
        PrivateKey pk = (PrivateKey) ks.getKey(alias, key_password.toCharArray());
        Certificate[] chain = ks.getCertificateChain(alias);
        // reader and stamper
        PdfReader reader = new PdfReader(src);
        FileOutputStream os = new FileOutputStream(dest);
        PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');
        // appearance
        PdfSignatureAppearance appearance = stamper .getSignatureAppearance();
        appearance.setImage(Image.getInstance(RESOURCE));
        appearance.setReason("I've written this.");
        appearance.setLocation("Foobar");
        appearance.setVisibleSignature(new Rectangle(72, 732, 144, 780), 1,    "first");
        // digital signature
        ExternalSignature es = new PrivateKeySignature(pk, "SHA-256", "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, es, chain, null, null, null, 0, MakeSignature.CryptoStandard.CMS);
    }

    /**
     * Manipulates a PDF file src with the file dest as result
     * @param src the original PDF
     * @param dest the resulting PDF
     * @throws IOException
     * @throws DocumentException
     * @throws GeneralSecurityException
     */
    // public void signPdfSecondTime(String src, String dest)
    //     throws IOException, DocumentException, GeneralSecurityException {
    //     String path = "src/main/resources/ia.p12";
    //     String keystore_password = "f00b4r";
    //     String key_password = "f1lmf3st";
    //     String alias = "foobar";
    //     KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
    //     ks.load(new FileInputStream(path), keystore_password.toCharArray());
    //     PrivateKey pk = (PrivateKey) ks.getKey(alias, key_password.toCharArray());
    //     Certificate[] chain = ks.getCertificateChain(alias);
    //     // reader / stamper
    //     PdfReader reader = new PdfReader(src);
    //     FileOutputStream os = new FileOutputStream(dest);
    //     PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0', null, true);
    //     // appearance
    //     PdfSignatureAppearance appearance = stamper
    //         .getSignatureAppearance();
    //     appearance.setReason("I'm approving this.");
    //     appearance.setLocation("Foobar");
    //     appearance.setVisibleSignature(new Rectangle(160, 732, 232, 780), 1, "second");
    //     // digital signature
    //     ExternalSignature es = new PrivateKeySignature(pk, "SHA-256", "BC");
    //     ExternalDigest digest = new BouncyCastleDigest();
    //     MakeSignature.signDetached(appearance, digest, es, chain, null, null, null, 0, MakeSignature.CryptoStandard.CMS);

    // }

    public void manipulatePdf(String src, String dest) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(src);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
        Map<String, String> info = reader.getInfo();
        info.put("Title", "Hello World stamped");
        info.put("Subject", "Hello World with changed metadata");
        info.put("Keywords", "iText in Action, PdfStamper");
        info.put("Creator", "Silly standalone example");
        info.put("Author", "Also Bruno Lowagie");
        stamper.setMoreInfo(info);
        stamper.close();
        reader.close();
    }

    /**
     * Verifies the signatures of a PDF we've signed twice.
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public void verifySignatures() throws GeneralSecurityException, IOException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        CertificateFactory cf = CertificateFactory.getInstance("X509");
        //FileInputStream is1 = new FileInputStream(properties.getProperty("ROOTCERT"));
        InputStream is1 = this.getClass().getResourceAsStream(properties.getProperty("ROOTCERT"));
        X509Certificate cert1 = (X509Certificate) cf.generateCertificate(is1);
        ks.setCertificateEntry("cacert", cert1);
        InputStream is2 = this.getClass().getResourceAsStream("/ia.crt");
        X509Certificate cert2 = (X509Certificate) cf.generateCertificate(is2);
        ks.setCertificateEntry("foobar", cert2);

        PrintWriter out = new PrintWriter(new FileOutputStream(VERIFICATION));
        PdfReader reader = new PdfReader(SIGNED2);
        AcroFields af = reader.getAcroFields();
        ArrayList<String> names = af.getSignatureNames();
        for (String name : names) {
            out.println("Signature name: " + name);
            out.println("Signature covers whole document: " + af.signatureCoversWholeDocument(name));
            out.println("Document revision: " + af.getRevision(name) + " of " + af.getTotalRevisions());
            PdfPKCS7 pk = af.verifySignature(name);
            Calendar cal = pk.getSignDate();
            Certificate[] pkc = pk.getCertificates();
            out.println("Subject: " + CertificateInfo.getSubjectFields(pk.getSigningCertificate()));
            out.println("Revision modified: " + !pk.verify());
            List<VerificationException> errors = CertificateVerification.verifyCertificates(pkc, ks, null, cal);
            if (errors.size() == 0)
                out.println("Certificates verified against the KeyStore");
            else
                out.println(errors);
        }
        out.flush();
        out.close();
    }

    /**
     * Extracts the first revision of a PDF we've signed twice.
     * @throws IOException
     */
    public void extractFirstRevision() throws IOException {
        PdfReader reader = new PdfReader(SIGNED2);
        AcroFields af = reader.getAcroFields();
        FileOutputStream os = new FileOutputStream(REVISION);
        byte bb[] = new byte[1028];
        InputStream ip = af.extractRevision("first");
        int n = 0;
        while ((n = ip.read(bb)) > 0)
            os.write(bb, 0, n);
        os.close();
        ip.close();
    }

    /**
     * Main method.
     *
     * @throws DocumentException
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static void test()
        throws IOException, DocumentException, GeneralSecurityException {
        Security.addProvider(new BouncyCastleProvider());
        properties.load(Signatures.class.getResourceAsStream(PATH));
        Signatures signatures = new Signatures();
        signatures.createPdf(ORIGINAL);
        signatures.signPdfFirstTime(ORIGINAL, SIGNED1);
        signatures.manipulatePdf(SIGNED1, SIGNED2);
        signatures.verifySignatures();
        signatures.extractFirstRevision();
    }
}
