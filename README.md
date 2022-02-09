# Generate XML Report
For Burp Suite Enterprise Edition

---
This extension will generate an XML report immediately after a scan has finished, containing all issues found. The file will be stored on the Scanning Machine that performed the scan.

The filename of the scan will be the start time of the scan, in the format `yyyy-MM-dd-HH-mm-ss`.

## Limitations
- File will be stored on Scanning Machine
- False positives marked in the Enterprise UI will not be marked as such in the XML report

## Usage
1. Download this repository, and check the `folderPath` variable in `BurpExtender.java` is pointed to a location where you have write permission **on the Scanning Machine**.
2. Build the extension using `gradle fatJar`.
3. Load the extension into Burp Enterprise, and add the extension to your Site Details page.
4. Run a scan as normal.

### Troubleshooting
If the report has not been generated, check your scan log for any exceptions - Scan > Reporting & logs > Scan debug log.
If you have received a `FileNotFoundException (Permission denied)`, then make sure that you are writing your report to a location where you have write permission.
