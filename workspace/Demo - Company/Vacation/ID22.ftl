<DIV class="form-container">
<P class="legend"><STRONG>Make an official order for vacation</STRONG></P>

<FIELDSET><LEGEND>Request</LEGEND>

<DIV><LABEL for="requester">Employee </LABEL>${DisplayVariable("requester", "false")}</DIV>

<DIV><LABEL for="since">Since </LABEL>${DisplayVariable("since", "false")}</DIV>

<DIV><LABEL for="till">Till </LABEL>${DisplayVariable("till", "false")}</DIV>

<DIV><LABEL for="reason">Reason </LABEL>${DisplayVariable("reason", "false")}</DIV>

<DIV><LABEL>Employee Comments</LABEL>${DisplayVariable("comment", "true")}</DIV>

<DIV><LABEL>Boss comments </LABEL>${DisplayVariable("boss_comment", "true")}</DIV>

<DIV><LABEL>Human resource inspector comments </LABEL>${DisplayVariable("human_resource_inspector_comment", "true")}</DIV>
</FIELDSET>

<FIELDSET><LEGEND>Give to request an official order</LEGEND>

<DIV><LABEL for="on">Official order number</LABEL>${InputVariable("official_order_number")}</DIV>

<DIV><LABEL for="od">Official order date</LABEL><EM><FONT size="-1"> ${InputVariable("official_order_date")} (dd.mm.yyyy)</FONT></EM></DIV>
</FIELDSET>
</DIV>
