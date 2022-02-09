# Generate XML Report
For Burp Suite Enterprise Edition

Please note that extensions are written by third party users of Burp, and PortSwigger makes no warranty about their quality or usefulness for any particular purpose.

---
This extension will generate an XML report immediately after a scan has finished, containing all issues found. The file will be stored on the Scanning Machine that performed the scan.

The filename of the scan will be the start time of the scan, in the format `yyyy-MM-dd-HH-mm-ss`.

## Limitations
- File will be stored on Scanning Machine.
- False positives marked in the Enterprise UI will not be marked as such in the XML report.
- Previous reports will not be removed from the folder, so ensure that you are regularly cleaning up old files.

## Usage
1. Download this repository, and check the `folderPath` variable in `BurpExtender.java` is pointed to a location where you have write permission **on the Scanning Machine**.
2. Build the extension using `gradle fatJar`.
3. [Load the extension into Burp Enterprise](https://portswigger.net/burp/documentation/enterprise/working/scans/extensions), and add the extension to your Site Details page.
4. Run a scan as normal.
5. Retrieve your scan from your Scanning Machine - it will be located according to the `folderPath` that is set. If in doubt, the file location and name will be output in the Scan log.

### Troubleshooting
If the report has not been generated, check your scan log for any exceptions - Scan > Reporting & logs > Scan debug log.

If you have received a `FileNotFoundException (Permission denied)`, then make sure that you are writing your report to a location where you have write permission.
