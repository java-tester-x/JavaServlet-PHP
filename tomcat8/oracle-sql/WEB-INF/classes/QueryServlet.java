import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;

import javax.json.*;
import javax.json.stream.*;
import javax.json.stream.JsonParser.*;


@WebServlet("/query") 
public class QueryServlet extends HttpServlet {

    final static String dsn = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=tcp)(HOST=10.10.10.222)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=XE)))";

    public void init() throws ServletException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        }
        catch (ClassNotFoundException e) {
            System.out.println("Couldn't load OracleDriver");
            throw new UnavailableException("Couldn't load OracleDriver");
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException
    {
        response.setContentType("application/json");

        PrintWriter out = response.getWriter();
        
        HttpSession session = request.getSession(true);
        Connection  con;

        synchronized (session) 
        {
            ConnectionHolder holder = (ConnectionHolder) session.getAttribute("servletapp.connection");

            if (holder == null) {
                try {
                    holder = new ConnectionHolder(DriverManager.getConnection(dsn, "hr", "hr"));
                    session.setAttribute("servletapp.connection", holder);
                }
                catch (SQLException e) {
                    log("Couldn't get db connection", e);
                }
            }

            con = holder.getConnection();
        }

        try {
            Statement stmt  = con.createStatement();
            String    query = request.getParameter("query");
            if (stmt.execute(query)) {
                JsonResultSet aJsonResultSet = new JsonResultSet(stmt.getResultSet());
                out.println(aJsonResultSet.toString());
            }
            else {
                out.println(
                        Json.createObjectBuilder()
                            .add("Records Affected", stmt.getUpdateCount())
                            .build()
                    );
            }
            
            if (stmt != null) stmt.close();
        }
        catch (Exception e) {
            try {
                // con.rollback();
                // session.removeAttribute("servletapp.connection");
            }
            catch (Exception ignored) { }
            out.println(
                    Json.createObjectBuilder()
                        .add("ERROR", e.getMessage())
                        .build()
                );
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        doPost(request, response);
    }
}


class ConnectionHolder implements HttpSessionBindingListener {
    private Connection con = null;

    public ConnectionHolder(Connection con) {
        // Save the Connection
        this.con = con;
        try {
            con.setAutoCommit(false);  // transactions can extend between web pages!
        }
        catch(SQLException e) {
            // Perform error handling
        }
    }

    public Connection getConnection() {
        return con;  // return the cargo
    }

    public void valueBound(HttpSessionBindingEvent event) {
        // Do nothing when added to a Session
    }

    public void valueUnbound(HttpSessionBindingEvent event) {
        // Roll back changes when removed from a Session
        // (or when the Session expires)
        try {
            if (con != null) {
                con.rollback();  // abandon any uncomitted data
                con.close();
            }
        }
        catch (SQLException e) {
            // Report it
        }
    }
}

class HtmlResultSet {

    private ResultSet rs;

    public HtmlResultSet(ResultSet rs) {
        this.rs = rs;
    }

    public String toString()
    {
        StringBuffer out = new StringBuffer();
     
        // Start a table to display the result set
        out.append("<TABLE cellpadding=0 cellspacing=0 border=1>\n");

        try {
            ResultSetMetaData rsmd = rs.getMetaData();

            int numcols = rsmd.getColumnCount();
        
            // Title the table with the result set's column labels
            out.append("<TR>");
            for (int i = 1; i <= numcols; i++) {
                out.append("<TH>" + rsmd.getColumnLabel(i));
            }
            out.append("</TR>\n");

            while(rs.next())
            {
                out.append("<TR>"); // start a new row
                for (int i = 1; i <= numcols; i++)
                {
                    out.append("<TD>"); // start a new data element
                    Object obj = rs.getObject(i);
                    if (obj != null)
                        out.append(obj.toString());
                    else
                    out.append("&nbsp;");
                } 
                out.append("</TR>\n");
            }

            // End the table
            out.append("</TABLE>\n");
        }
        catch (SQLException e) {
            out.append("</TABLE><H1>ERROR:</H1> " + e.getMessage() + "\n");
        }

        return out.toString();
    }
}

class JsonResultSet {
    private ResultSet rs;

    public JsonResultSet(ResultSet rs) {
        this.rs = rs;
    }

    public String toString()
    {
        JsonBuilderFactory factory = Json.createBuilderFactory(null);
        JsonArrayBuilder   jsonOut = factory.createArrayBuilder();

        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            int numcols = rsmd.getColumnCount();

            long rows = 0;
            while(rs.next())
            {
                JsonObjectBuilder row = factory.createObjectBuilder();
                for (int i = 1; i <= numcols; i++)
                {
                    Object obj         = rs.getObject(i);
                    String columnName  = rsmd.getColumnLabel(i);
                    String columnValue = (obj != null) ? obj.toString() : "";                    
                    row.add(columnName, columnValue);
                }
                jsonOut.add(row);
                rows++;
            }
        }
        catch(SQLException e) {
            jsonOut.add(factory.createObjectBuilder().add("ERROR", e.getMessage()));
        }

        JsonArray arr = jsonOut.build();
        return arr.toString();
    }
}