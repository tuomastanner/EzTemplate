EzTemplate by Tuomas Tanner
- The poor man's content management system

## Usage ##
Create a template html file with the following tag somewhere at the start:
```
<!--eztemplate_templatefilename.html-->
```

Create editable areas by adding a start tag:
```
<!--ezstart_areaname-->
```
End your editable area with the end tag:
```
<!--ezend_areaname-->
```

To create pages based on the template, just copy and rename the template file. You can then edit the contents inside the editable areas freely.

When you change your template design, just run this tool on the website folder or html file and the changes will be magically applied with the editable areas left intact.
Use the following command to update a single file:
java EzTemplate index.html

To update a directory recursively, give the directory name:
java EzTemplate websitedir

This will go through each file and search for the template tag. If it is found, it will load the template and apply it. If template tag is not found, the file will be left untouched. You can also process only certain files by partial filename like so:
java EzTemplate websitedir .html

To update current dir, use dot:
java EzTemplate . .html

## Tips ##
You can freely muck around with the non-editable areas in destination file, they will be overwritten on update. Don't worry: If there are any problems in the update process, an error is shown and the original file is left untouched. 

You can also add an editable area to the template. When you run EzTemplate, the new area will be copied to all web pages with its default content. The next time EzTemplate is run, the content inside the editable area will not be touched.

You can also include path information in the template filename tag, if you want to keep your template files in a different directory than your other site. See the testsite folder for an example. Here all the template files are in the templates directory, which the template tags in html files reference.

## License ##
This project is released with an Apache license:
Feel free to distribute or modify this program. Just give me credit if you do so.
