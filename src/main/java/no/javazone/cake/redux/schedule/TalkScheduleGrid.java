package no.javazone.cake.redux.schedule;

import java.io.PrintStream;
import java.util.List;

public class TalkScheduleGrid {
    public final List<String> rooms;
    public final List<TalkScheduleRow> rows;

    public TalkScheduleGrid(List<String> rooms, List<TalkScheduleRow> rows) {
        this.rooms = rooms;
        this.rows = rows;
    }

    @Override
    public String toString() {
        return "TalkScheduleGrid{" +
                "rooms=" + rooms +
                ", rows=" + rows +
                '}';
    }

    public void asHtmlTable(PrintStream out) {
        out.println("<html><body>");
        out.println("<table>\n<tr>\n<th>x</th>\n");

        rooms.stream().forEach(room -> out.println("<th>" + room + "</th>"));
        out.println("</tr>");
        for (TalkScheduleRow talkScheduleRow : rows) {
            out.println("<tr>");
            out.println("<td>" + talkScheduleRow.displaySlot + "</td>");
            for (TalkScheduleCell cell : talkScheduleRow.cells) {
                out.println("<td></ul>");
                cell.contents.forEach(tscd -> out.println("<li>" + tscd.title + "</li>"));
                out.println("</ul></td>");
            }
            out.println("</tr>");
        }
        out.println("</table>");
        out.println("</body></html>");

    }
}
