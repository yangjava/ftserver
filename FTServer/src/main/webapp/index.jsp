<%@page import="ftserver.*"%>
<%@page import="java.util.*"%>
<%@page contentType="text/html" pageEncoding="UTF-8" session="false"%>
<%@include  file="_taghelper.jsp" %>
<%    long begin = System.currentTimeMillis();
%>
<!DOCTYPE html>
<html>
    <head>        
        <meta http-equiv="content-type" content="text/html; charset=UTF-8">
        <meta name="description" content="iBoxDB NoSQL Database Full Text Search Server FTS">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Full Text Search Server</title>

        <link rel="stylesheet" type="text/css" href="css/semantic.min.css"> 

        <style>

            body {
                margin-top: 10px;
                padding: 10px;
            }

            .grid{
                max-width: 100%;
                width:100%;
                text-align: center;
            }
            .column {
            }

            .message{
                overflow-x: hidden;
            }

            .abw{
                word-wrap: break-word;
            }

        </style> 

    </head>
    <body> 


        <div class="grid">
            <div class="column"  >

                <h2 class="ui teal header" > 
                    <i class="disk outline icon" style="font-size:82px"></i> Full Text Search Server
                </h2>
                <form class="ui large form"  action="s.jsp"  onsubmit="formsubmit()"  >
                    <div class="ui label input">
                        <div class="ui action input">
                            <input name="q"  value=""  required onfocus="formfocus()"  dir="auto" />
                            <input id="btnsearch" type="submit"  class="ui teal right button big" 
                                   value="Search"    /> 
                        </div> 
                    </div>
                </form>
                <script>
                    function formsubmit() {
                        document.getElementById('btnsearch').disabled = "disabled";
                    }
                    function formfocus() {
                        document.getElementById('btnsearch').disabled = undefined;
                    }
                </script>

                <div class="ui message" style="text-align: left; font-size: 20px">
                    <a  href="admin.jsp" target="ADMIN_FTSERVER">Add Page Index</a><br>

                    <!-- <br>Recent Searches: -->
                    <br>
                    <% for (PageSearchTerm pst : IndexPage.getSearchTerm(10)) {
                            String str = pst.keywords;
                            if (str.equals(IndexPage.SystemShutdown)) {
                                continue;
                            }
                            try (Tag t = HTML.tag("a", "href:", "s.jsp?q=" + encode(str))) {
                                HTML.text("[" + str + "]");
                            }
                            HTML.text(" &nbsp; ");
                        }
                    %> 

                    <br>

                    <br><a  href="./">Refresh Discoveries:</a> &nbsp;  <br>
                    <%
                        for (String str : IndexPage.discover()) {
                            try (Tag t = HTML.tag("a", "href:", "s.jsp?q=" + encode(str))) {
                                HTML.text("[" + str + "]");
                            }
                            HTML.text(" &nbsp; ");
                        }

                        HTML.tag("br");

                    %>
                    <br>
                </div>
                <% HTML.text("Load Time: " + (System.currentTimeMillis() - begin) / 1000.0 + "s");
                %>
            </div>
        </div>

    </body>
    <!-- <%=version()%> -->
</html>