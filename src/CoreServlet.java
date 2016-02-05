import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.ServerException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

/** Contains core functions for search engine:
 *  Displays text box for search queries and returns a partial search result.
 * @author raywang
 *
 */
@SuppressWarnings("serial")
public class CoreServlet extends HttpServlet{

	private static final String TITLE = "Search Engine";
	private static Logger logger = Log.getRootLogger();
	private final ThreadSafeInvertedIndex index;
	private MultithreadedPartialSearchBulider searcher;

	public CoreServlet(int threadCount, InvertedIndex index) {
		super();
		this.index = (ThreadSafeInvertedIndex) index;
		searcher = new MultithreadedPartialSearchBulider(threadCount, this.index);
	}

	@Override
	protected void doGet(
			HttpServletRequest request,
			HttpServletResponse response)
					throws ServerException, IOException {

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		logger.info("CoreServlet ID " + this.hashCode() + " handling GET request.");

		PrintWriter out = response.getWriter();
		String user = new LoginBaseServlet().getUsername(request);

		out.printf("<html>%n");
		out.printf("<head><title>%s</title></head>%n", TITLE);

		/* Background image */
		out.printf("<body background=http://i2.wp.com/planetdestiny.com/wp-content"
				+ "/uploads/2013/02/old_school_guardians_by_hylacola-d7p2174.png>%n");
		out.printf("<body>%n");

		/* Login/Logout */
		if (user == null) {
			user = "Guest";
		}

		out.printf("<div align = left> <p> Hello, %s.%n", user);

		if (user == "Guest") {
			out.printf("<a href = '/login'> Login/Register </a>");
		} else {
			out.printf("<a href = '/login'> Log Out </a>");
			user = "Guest";
		}

		/* Logo */
		out.printf("<center>%n");
		out.printf("<img src = \"http://shirta.ca/media/catalog/product/cache/2/"
				+ "small_image/295x295/9df78eab33525d08d6e5fb8d27136e95/c/o/"
				+ "code-blooded-d76483901.png\">%n");

		/* Search Box */
		printForm(request, response);

		String choice = request.getParameter("history");

		if (choice != null) {
			out.printf("<b> Search History </b>");
			LoginBaseServlet.dbhandler.getHistory(user, out);
		}

		/* Query Input */
		String query = request.getParameter("search");

		if (query == null || query.equals("")) {
			query = "";
		}

		query = StringEscapeUtils.escapeHtml4(query);
		searcher = new MultithreadedPartialSearchBulider(5, this.index);

		if (!query.equals(null) && !query.isEmpty()) {

			searcher.parseLine(query);
			searcher.finish();

			Map<String, List<SearchResult>> result = searcher.getResult();

			for (String aResult : result.keySet()) {
				List<SearchResult> list = result.get(aResult);

				if (list.size() == 0) {
					out.printf("<p>No result found</p>%n");
				}

				/* Prints results */
				out.printf("<div style=\"height: 200px; width: 600px; "
						+ "border:1px solid #ccc; overflow: auto; "
						+ "background-color: #B0B0B0;\">");
				for (SearchResult sResult : list) {
					out.printf("<p><a href=" + sResult.getPath() + ">" +sResult.getPath().toString() +
							"</a><p>%n");
					out.printf("\n");
				}
				out.printf("</div>");
			}
			System.out.println("Printing completed.");
		} /* End of result printing */

		LoginBaseServlet.dbhandler.addHistory(
				new LoginBaseServlet().getUsername(request), query);


		out.printf("</body>%n");
		out.printf("</html>%n");

		response.setStatus(HttpServletResponse.SC_OK);

	}


	private static void printForm(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		PrintWriter out = response.getWriter();
		out.printf("<form method=\"get\" action=\"%s\">%n",
				request.getServletPath());
		out.printf("<table cellspacing=\"0\" cellpadding=\"2\"%n");
		out.printf("<tr>%n");
		out.printf("\t<td nowrap></td>%n");
		out.printf("\t<td>%n");
		out.printf(
				"\t\t<input type=\"text\" name=\"search\" maxlength=\"70\" size=\"80\">%n");
		out.printf("\t</td>%n");
		out.printf("</tr>%n");

		out.printf("</table>%n");
		out.printf("<p><input type=\"submit\" value=\"Search\"></p>");

		out.printf("<form method=\"get\" action=\"%s\">%n",
				request.getServletPath());
		//		out.printf(
		//				"<input type=\"submit\" name=\"history\" value=\"History\">");

		out.printf("</form>\n%n");
	}

}