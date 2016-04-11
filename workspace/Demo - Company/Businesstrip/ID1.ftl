<DIV class="form-container">
<P class="legend"><STRONG>Send an employee to a business trip</STRONG></P>

<DIV><LABEL for="staffrole">Employee </LABEL>${GroupMembers("staffrole", "value@staff", "all")}</DIV>

<DIV><LABEL for="since">Since </LABEL>${InputVariable("since")}<EM><FONT size="-1">(dd.mm.yyyy)</FONT></EM></DIV>

<DIV><LABEL for="till">Till </LABEL>${InputVariable("till")}<EM><FONT size="-1">(dd.mm.yyyy)</FONT></EM></DIV>

<DIV><LABEL for="reason">Business trip type </LABEL><SELECT id="businessTripType" name="businessTripType"><OPTION selected="selected" value="local">local</OPTION><OPTION value="toAnotherRegion">to another region</OPTION> </SELECT></DIV>

<DIV><LABEL for="reason">Reason </LABEL>${InputVariable("reason")}</DIV>

<DIV><LABEL for="comment">Comments</LABEL>${InputVariable("comment")}</DIV>
</DIV>
