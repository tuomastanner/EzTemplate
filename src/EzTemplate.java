import java.io.*;
import java.text.ParseException;

/** EzTemplate by Tuomas Tanner
 *  - The poor man's content management system
 *  
 *  See README for usage instructions.
 * 
 *  This is released with an Apache style license: Feel free to distribute or modify this program. 
 *  Just give me credit if you do so.
 */
public class EzTemplate {
	public static final String TMPL = "<!--eztemplate_";
	public static final String START = "<!--ezstart_";
	public static final String END = "<!--ezend_";
	public static final int STARTL = START.length();
	public static final int ENDL = END.length();
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java EzTemplate <file or dir to process> [file ending filter]");
            System.out.println("E.g: java EzTemplate websitedir .html OR java EzTemplate . .html OR java EzTemplate index.html");
            return;
        }
        
        int[] stats = {0,0};
        String[] curTmpl = {"", ""};
        String filter = "";
        
        if (args.length > 1) {
            filter = args[1];
        }
        try {
            processFiles(new File(args[0]), filter, stats, curTmpl);
        } catch(IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
        }
        
        
        System.out.println("Done: " + stats[0] + " file(s) processed. " + stats[1] + " file(s) skipped."); 
    }
    
    private static void processFiles(File curFile, String filter, int[] stats, String[] curTmpl) throws IOException {
        //recurse through directories
        if (curFile.isDirectory()) {
            File[] dir = curFile.listFiles();
            for (File dirEntry : dir) {
                processFiles(dirEntry, filter, stats, curTmpl);
            }
            return;
        }
        //process current file
        
        if (!curFile.getName().contains(filter)) {
            ++stats[1];
            return;
        }
        String filePath = curFile.getCanonicalPath();
        String target = readFile(curFile);
        int namePos = target.indexOf(TMPL);
        if (namePos == -1) { //doesn't contain template reference, skip
            ++stats[1];
            return;
        }
        String curDir = filePath.substring(0, filePath.lastIndexOf(File.separator)+1);
        String templateName = target.substring(namePos + TMPL.length(), target.indexOf("-->", namePos));
        File templateFile = new File(curDir + templateName);
        if (!templateFile.isFile()) {
            System.err.println("Error: Template file \"" + templateName + "\" not found for " + filePath);
            ++stats[1];
            return;
        }
        String templatePath = templateFile.getCanonicalPath();
        if (templatePath.equals(filePath)) { //this is the template file itself - skip it
            ++stats[1];
            return;
        }
        
        System.out.println("Processing file: " + filePath);
        
        //load template file to memory if not already loaded
        if (!curTmpl[0].equals(templatePath)) { 
            curTmpl[1] = readFile(templateFile);
            curTmpl[0] = templatePath;
        }

        //apply template and write file
        try {
            String output = applyTemplate(curTmpl[1], target);
            writeFile(curFile, output);
            ++stats[0];
        } catch (Exception e) {
            System.err.println(e.getMessage() + " File: " + curFile.getAbsolutePath());
            ++stats[1];
        }
        
    }
    
    private static String applyTemplate(String template, String target) throws Exception {
        StringBuilder output = new StringBuilder();
        
        //process template tag, keeps path to template specified in target file
        int tgStart = target.indexOf(TMPL);
        int tmplStart = template.indexOf(TMPL);
        int tgEnd = target.indexOf("-->", tgStart) + 3;
        int tmplEnd = template.indexOf("-->", tmplStart) + 3;
        
        output.append(template.substring(0, tmplStart)); //print start from template
        output.append(target.substring(tgStart, tgEnd)); //print template reference from target
        
        //process all content areas
        while ((tgStart = target.indexOf(START, tgEnd)) != -1) {
            String areaName = target.substring(tgStart + STARTL, target.indexOf("-->", tgStart + 13));
            
            tmplStart = template.indexOf(START + areaName);
            if (tmplStart < 0) {
                throw new ParseException("Error: Area \"" + areaName + "\" not found in template.", tgStart);
            }
            //                             (the old end) 
            output.append(template.substring(tmplEnd, tmplStart)); //print from template
            tmplEnd = template.indexOf(END + areaName, tmplStart) ; //update template end
            if (tmplEnd < 0) {
                throw new ParseException("Error: End tag for area \"" + areaName + "\" missing in template.", tmplStart);
            }
            tmplEnd += ENDL + areaName.length() + 3;

            tgEnd = target.indexOf(END + areaName, tgStart); //target area end
            if (tgEnd < 0) {
                throw new ParseException("Error: End tag for area \"" + areaName + "\" missing in target.", tgStart);
            }
            tgEnd += ENDL + areaName.length() + 3; //add the end tag length so that it will be also copied
            output.append(target.substring(tgStart, tgEnd)); //print tags + content from target
        }
        output.append(template.substring(tmplEnd, template.length())); //add the final bit
        return output.toString();
    }
    
    private static String readFile(File file) throws IOException {
        BufferedReader fread = new BufferedReader(new FileReader(file));
        char[] cbuf = new char[(int)file.length()];
        fread.read(cbuf, 0, (int)file.length());
        return new String(cbuf);
    }
    
    private static void writeFile(File file, String contents) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(contents);
        writer.close();
    }
}