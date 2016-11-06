package cn.klzhong.pdfupdater;

public class Main {

    public static void main(String[] args) throws Exception {
        // test();
        // System.out.println("done!");
        Signatures.test();
    }

    private static void test() throws Exception {
        PdfUpdater updater = new PdfUpdater();
        updater.addImage("python_tutor.pdf", "python_tutor_updated.pdf", "test.png", 480.0f, 550f);
    }

}
