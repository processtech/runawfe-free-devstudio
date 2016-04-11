<DIV class="form-container">
<P class="legend"><STRONG>Evaluate a vacation request</STRONG></P>

<FIELDSET><LEGEND>User request</LEGEND>

<DIV><LABEL for="requester">Employee </LABEL>${DisplayVariable("requester", "false")}</DIV>

<DIV><LABEL for="since">Since </LABEL>${DisplayVariable("since", "false")}</DIV>

<DIV><LABEL for="till">Till </LABEL>${DisplayVariable("till", "false")}</DIV>

<DIV><LABEL for="reason">Reason </LABEL>${DisplayVariable("reason", "false")}</DIV>

<DIV><LABEL>Comments</LABEL>${DisplayVariable("comment", "true")}</DIV>

<DIV><LABEL for="comment">Human resource inspector comments </LABEL>${DisplayVariable("human_resource_inspector_comment", "true")}</DIV>
</FIELDSET>

<FIELDSET><LEGEND>Your decision</LEGEND>

<DIV><LABEL for="boss_comment">Boss comments </LABEL>${InputVariable("boss_comment")}</DIV>

<DIV class="controlset"><INPUT id="bossDecisionApprove" name="bossDecision" type="radio" value="true"/> <LABEL for="bossDecisionApprove">Approve</LABEL><INPUT id="bossDecisionDisapprove" name="bossDecision" type="radio" value="false"/> <LABEL for="bossDecisionDisapprove">Reject</LABEL></DIV>
</FIELDSET>
</DIV>
