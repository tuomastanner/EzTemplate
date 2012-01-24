import java.io.*;
import java.text.ParseException;

/** - EzTemplate, by Tuomas Tanner
 *  - The poor man's content management system -
 *  
 *  - Usage -
 *  Create a template html file with the following tag somewhere at the start:
 *  <!--eztemplate_templatefilename.html-->
 *  
 *  Create editable areas by adding a start tag:
 *  <!--ezstart_areaname-->
 *  End your editable area with the an end tag:
 *  <!--ezend_areaname-->
 *  
 *  To create pages based on the template, just copy and rename the template file.
 *  You can then edit the contents inside the editable areas freely.
 *  If you change your template design, just run this tool on the file(s) you've created
 *  and the changes will be magically applied with the editable areas left intact.
 *  
 *  You can freely muck around with the non-editable areas in destination file, they will be overwritten on update.
 *  Don't worry: If there are any problems in the update process, an error is shown and the original file is left untouched.
 *  
 *  Tip: You can also include path information in the template filename tag, if you want to keep your template
 *  files in a different directory than your other site.
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
            System.out.println("E.g: java EzTemplate websitedir .html OR java EzTemplate . .html");
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
        //is file
        String filename = curFile.getName();
        if (!filename.contains(filter)) {
            return;
        }
        String curDir = curFile.getAbsolutePath();
        System.out.println("Processing file: " + curDir);
        //get only directory
        curDir = curDir.substring(0, curDir.lastIndexOf(File.separator)+1);
        
        
        String target = readFile(curFile);
        int namePos = target.indexOf(TMPL);
        if (namePos == -1) { //doesn't contain template reference
            ++stats[1];
            return;
        }
        String templateName = target.substring(namePos + TMPL.length(), target.indexOf("-->", namePos));
        if (templateName.contains(filename)) { //this is the template file itself - skip it
            return;
        }


        //load template file if not already loaded
        if (!curTmpl[0].equals(templateName)) { 
            curTmpl[1] = readFile(new File(curDir + templateName));
            curTmpl[0] = templateName;
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
        int tgStart;
        int tgEnd = 0;
        int tmplStart = 0;
        int tmplEnd = 0;
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
    
    public static String readFile(File file) throws IOException {
        BufferedReader fread = new BufferedReader(new FileReader(file));
        char[] cbuf = new char[(int)file.length()];
        fread.read(cbuf, 0, (int)file.length());
        return new String(cbuf);
    }
    
    public static void writeFile(File file, String contents) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(contents);
        writer.close();
    }
}