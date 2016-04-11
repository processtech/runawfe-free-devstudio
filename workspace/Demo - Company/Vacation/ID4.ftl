<DIV class="form-container">
<P class="legend"><STRONG>Check that all rules and technologies for vacation request are correct</STRONG></P>

<FIELDSET><LEGEND>Request</LEGEND>

<DIV><LABEL for="requester">Employee </LABEL>${DisplayVariable("requester", "false")}</DIV>

<DIV><LABEL for="since">Since </LABEL>${DisplayVariable("since", "false")}</DIV>

<DIV><LABEL for="till">Till </LABEL>${DisplayVariable("till", "false")}</DIV>

<DIV><LABEL for="reason">Reason </LABEL>${DisplayVariable("reason", "false")}</DIV>

<DIV><LABEL>Employee Comments</LABEL>${DisplayVariable("comment", "true")}</DIV>

<DIV><LABEL for="boss comment">Boss comments </LABEL>${DisplayVariable("boss_comment", "true")}</DIV>
</FIELDSET>

<FIELDSET><LEGEND>Your decision</LEGEND>

<DIV><LABEL for="human resource inspector comment">Comments </LABEL>${InputVariable("human_resource_inspector_comment")}</DIV>

<DIV class="controlset"><INPUT id="ok" name="humanResourceInspectorCheckResult" type="radio" value="true"/> <LABEL for="ok">Correct</LABEL> <INPUT id="no" name="humanResourceInspectorCheckResult" type="radio" value="false"/> <LABEL for="no">Not correct</LABEL></DIV>
</FIELDSET>
</DIV>
