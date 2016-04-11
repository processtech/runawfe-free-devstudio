<DIV class="form-container">
<P class="legend"><STRONG>Accept or decline the offering of overtime work</STRONG></P>

<FIELDSET><LEGEND>Proposition</LEGEND>

<DIV><LABEL for="manager">Manager</LABEL>${DisplayVariable("manager", "false")}</DIV>

<DIV><LABEL for="staff">Employee (staff)</LABEL>${DisplayVariable("staffrole", "false")}</DIV>

<DIV><LABEL for="since">Since</LABEL>${DisplayVariable("since", "false")}</DIV>

<DIV><LABEL for="till">Till</LABEL>${DisplayVariable("till", "false")}</DIV>

<DIV><LABEL for="reason">Reason</LABEL>${DisplayVariable("reason", "false")}</DIV>

<DIV><LABEL>Comments </LABEL>${DisplayVariable("comment", "true")}</DIV>
</FIELDSET>

<FIELDSET><LEGEND>Your comments</LEGEND>

<DIV>${InputVariable("staff_person_comment")}</DIV>
</FIELDSET>
</DIV>
