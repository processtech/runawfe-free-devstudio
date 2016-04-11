<DIV class="form-container">
<P class="legend"><STRONG>Make a business trip official order</STRONG></P>
<FIELDSET><LEGEND>User request</LEGEND>
<DIV><LABEL>Boss </LABEL>${DisplayVariable("boss", "false")}</DIV>
<DIV><LABEL>Employee </LABEL>${DisplayVariable("staffrole", "false")}</DIV>
<DIV><LABEL>Since </LABEL>${DisplayVariable("since", "false")}</DIV>
<DIV><LABEL>Till </LABEL>${DisplayVariable("till", "false")}</DIV>
<DIV><LABEL>Business trip type </LABEL>${DisplayVariable("businessTripType", "false")}</DIV>
<DIV><LABEL>Reason </LABEL>${DisplayVariable("reason", "false")}</DIV>
<DIV><LABEL>Comments</LABEL>${DisplayVariable("comment", "true")}</DIV>
</FIELDSET> <FIELDSET><LEGEND>Order info</LEGEND>
<DIV><LABEL>Official order number </LABEL>${InputVariable("official_order_number")}</DIV>
<DIV><LABEL>Official order date </LABEL>${InputVariable("official_order_date")}</DIV>
</FIELDSET></DIV>
<P> </P>