package org.anyline.pdf.util;

import org.anyline.entity.DataSet;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PDFUtil {

    public static String read(File file){
        FileInputStream in = null;
        String result = null;
        try {
            in = new FileInputStream(file);
            PDFParser parser = new PDFParser(new RandomAccessFile(file,"rw"));
            parser.parse();
            PDDocument doc = parser.getPDDocument();
            PDFTextStripper stripper = new PDFTextStripper();
            stripper .setSortByPosition(true);
            result = stripper.getText(doc);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                }
            }
        }
        return result;
    }

    /**
     * 按页读取文本
     * @return List
     */
    public static List<String> pages(){
        List<String> list = new ArrayList<>();
        return list;
    }
    /**
     * 读取表格
     * @return DataSet
     */
    public static DataSet table(){
        DataSet set = new DataSet();
        return set;
    }

    /**
     * 读取所有表格
     * @return List
     */
    public static List<DataSet> tables(){
        List<DataSet> list = new ArrayList<>();
        return list;
    }
}
