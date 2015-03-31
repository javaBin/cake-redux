package no.javazone.cake.redux.whyda;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserXmlUtil {
    public static UserInfo read(String usertokenxml, String applicationId) {

        String username = elementValue("username",usertokenxml);
        String firstname = elementValue("firstname",usertokenxml);
        String lastname = elementValue("lastname",usertokenxml);
        String email = elementValue("email",usertokenxml);

        Optional<String> roleelem = elementWithAttribute("application", "ID", applicationId, usertokenxml);
        if (!roleelem.isPresent()) {
            return null;
        }
        if (roleelem.get().indexOf("<role name=\"Admin\" value=\"true\"/>") == -1) {
            return null;
        }

        return new UserInfo(username,firstname,lastname,email,"Admin");
    }

    private static String elementValue(String name,String xml) {
        List<String> elements = element(name, xml);
        if (elements.size() != 1) {
            return null;
        }
        String element = elements.get(0);
        return innerValue(name, element);
    }

    private static String innerValue(String name, String element) {
        int start=name.length()+2;
        int end = element.indexOf("</" + name +">");
        return element.substring(start,end);
    }

    private static Optional<String> elementWithAttribute(String name,String attribname,String attribvalue,String xml) {
        return element(name,xml).stream()
                .filter(el -> el.indexOf(attribname + "=\"" + attribvalue + "\"") != -1)
                .findAny();
    }

    private static List<String> element(String name,String xml) {
        List<String> result = new ArrayList<>();

        int pos = -1;
        while ((pos = xml.indexOf("<" +name,pos+1)) != -1) {
            String endtag = "</" + name + ">";
            int end = xml.indexOf(endtag,pos);
            if (end == -1) {
                return new ArrayList<>();
            }
            int endpos = end + endtag.length();
            result.add(xml.substring(pos,endpos));
        }
        return result;
    }
}
