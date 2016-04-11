<DIV class="form-container">
<P class="legend"><STRONG>Vacation is rejected</STRONG></P>

<FIELDSET><LEGEND>Your request</LEGEND>

<DIV><LABEL for="requester">Employee </LABEL>${DisplayVariable("requester", "false")}</DIV>

<DIV><LABEL for="since">Since </LABEL>${DisplayVariable("since", "false")}</DIV>

<DIV><LABEL for="till">Till </LABEL>${DisplayVariable("till", "false")}</DIV>

<DIV><LABEL for="reason">Reason </LABEL>${DisplayVariable("reason", "false")}</DIV>

<DIV><LABEL>Comments</LABEL>${DisplayVariable("comment", "true")}</DIV>
</FIELDSET>

<FIELDSET><LEGEND>Comments</LEGEND>

<DIV><LABEL for="boss comment">Boss comments </LABEL>${DisplayVariable("boss_comment", "true")}</DIV>

<DIV><LABEL for="human resource inspector comment">Human resource inspector comments </LABEL>${DisplayVariable("human_resource_inspector_comment", "true")}</DIV>
</FIELDSET>
</DIV>
