<DIV class="form-container">
<P class="legend"><STRONG>Read report</STRONG></P>

<FIELDSET><LEGEND>Report info</LEGEND>

<DIV><LABEL for="manager">Manager </LABEL>${DisplayVariable("manager", "false")}</DIV>

<DIV><LABEL for="staff">Report maker </LABEL>${DisplayVariable("staff", "false")}</DIV>

<DIV><LABEL for="report_theme">Report theme </LABEL>${DisplayVariable("report theme", "false")}</DIV>
</FIELDSET>

<FIELDSET><LEGEND>Report content</LEGEND>

<DIV>${DisplayVariable("report", "true")}</DIV>
</FIELDSET>
</DIV>
