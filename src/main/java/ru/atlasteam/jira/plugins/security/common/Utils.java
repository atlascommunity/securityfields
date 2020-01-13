package ru.atlasteam.jira.plugins.security.common;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class Utils {
    public static String join(List<String> stringList) {
        if (stringList == null)
            return "";
        return StringUtils.join(stringList, ", ");
    }

    public static List<String> split(String joinedString) {
        List<String> stringList = new LinkedList<String>();
        if (StringUtils.isNotBlank(joinedString))
            for (String s : joinedString.trim().split("\\s*,\\s*"))
                if (StringUtils.isNotEmpty(s))
                    stringList.add(s);
        return stringList;
    }
}
