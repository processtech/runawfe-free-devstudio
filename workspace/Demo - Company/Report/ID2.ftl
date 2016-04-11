<DIV class="form-container">
<P class="legend"><STRONG>Make report</STRONG></P>

<FIELDSET><LEGEND>Report request</LEGEND>

<DIV><LABEL for="manager">Manager </LABEL>${DisplayVariable("manager", "false")}</DIV>

<DIV><LABEL for="report_theme">Report theme </LABEL>${DisplayVariable("report theme", "false")}</DIV>
</FIELDSET>

<FIELDSET><LEGEND>Report content</LEGEND>

<DIV>${InputVariable("report")}</DIV>
</FIELDSET>
</DIV>
