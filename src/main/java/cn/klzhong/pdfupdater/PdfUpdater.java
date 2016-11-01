package cn.klzhong.pdfupdater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

public class PdfUpdater {

    public void addImage(String pdfSrc, String pdfDest, String imagePath, float posX, float posY)
        throws DocumentException, IOException {
        PdfReader reader = new PdfReader(pdfSrc);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(pdfDest));

        Image img = Image.getInstance(imagePath);
        img.setAbsolutePosition(posX, posY);

        int n = reader.getNumberOfPages();
        //Rectangle pagesize;
        for (int i = 1; i <= n; i++) {
            PdfContentByte over = stamper.getOverContent(i);
            // pagesize = reader.getPageSize(i);
            // float x = pagesize.getLeft() + posX;
            // float y = pagesize.getTop() + posY;
            over.addImage(img);
        }
        stamper.close();
        reader.close();
    }

}
