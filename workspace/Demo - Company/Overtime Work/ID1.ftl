<DIV class="form-container">
<P class="legend"><STRONG>Offer an overtime work</STRONG></P>

<DIV><LABEL for="staff">Employee</LABEL></DIV>

<DIV>${GroupMembers("staffrole", "value@staff", "all")}</DIV>

<DIV><LABEL for="since">Since</LABEL><EM><FONT size="-1">${InputVariable("since")} (dd.mm.yyyy)</FONT></EM></DIV>

<DIV><LABEL for="till">Till</LABEL><EM><FONT size="-1">${InputVariable("till")} (dd.mm.yyyy)</FONT></EM></DIV>

<DIV><LABEL for="reason">Reason</LABEL>${InputVariable("reason")}</DIV>

<DIV><LABEL for="comment">Comments</LABEL>${InputVariable("comment")}</DIV>
</DIV>
