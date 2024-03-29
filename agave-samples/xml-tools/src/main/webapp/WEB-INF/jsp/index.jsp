<%@ page contentType="application/xhtml+xml" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  <head>
    <meta http-equiv="Content-Type" content="application/xhtml+xml; charset=utf-8" />
    <title>XML Tools</title>
    <style type="text/css">
      @import "${pageContext.request.contextPath}/css/structure.css";
      @import "${pageContext.request.contextPath}/css/theme.css";
    </style>
    <script type="text/javascript">
      var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
      document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
    </script>
    <script type="text/javascript">
      try {
        var pageTracker = _gat._getTracker("UA-10687198-1");
        pageTracker._trackPageview();
      } catch(err) {}
    </script>
  </head>
  <body>
    <h1>XML Tools</h1>
    <ul>
      <li><a href="#reformat">XML Document Reformatter</a></li>
    </ul>
    <h2 id="reformat">XML Document Reformatter</h2>
    <p>Submit an XML document and have it reformatted according to the following
    rules. Please note that <em>your document will be discarded</em> and is not
    permanently saved.</p>
    <form
      action="${pageContext.request.contextPath}/reformat"
      method="post"
      enctype="multipart/form-data">
      <table>
        <tbody>
          <tr>
            <td>XML Document:</td>
            <td><input type="file" id="document" name="document" size="50" /></td>
          </tr>
          <tr>
            <td>Indent With:</td>
            <td>
              <select id="indentation" name="indentation">
                <option value="1s">1 space</option>
                <option value="2s" selected="selected">2 spaces</option>
                <option value="4s">4 spaces</option>
                <option value="8s">8 spaces</option>
                <option value="1t">1 tab</option>
              </select>
            </td>
          </tr>
          <tr>
            <td>Insert Newlines:</td>
            <td>
              <select id="newlines" name="newlines">
                <option value="true" selected="selected">Yes</option>
                <option value="false">No</option>
              </select>
            </td>
          </tr>
        </tbody>
      </table>
      <p>
        <input type="submit" />
      </p>
    </form>
    <div id="footer">
      <p>&copy; 2009 <a href="http://damiancarrillo.org">Damian Carrillo</a>.  All rights reserved.</p>
      <p><a href="http://code.google.com/p/agave-web-framework/"><img
              src="http://code.google.com/p/agave-web-framework/logo?logo_id=1253032226"
              alt="The Web on Agave"
              title="The Web on Agave" /></a></p>
    </div>
  </body>
</html>
