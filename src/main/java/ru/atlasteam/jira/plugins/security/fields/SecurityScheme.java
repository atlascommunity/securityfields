package ru.atlasteam.jira.plugins.security.fields;

import net.java.ao.Entity;
import net.java.ao.schema.StringLength;

@SuppressWarnings("UnusedDeclaration")
public interface SecurityScheme extends Entity {
    long getFieldConfigId();
    void setFieldConfigId(long fieldConfigId);

    @StringLength(StringLength.UNLIMITED)
    String getAllowedGroupNames();
    void setAllowedGroupNames(String allowedGroupNames);

    @StringLength(StringLength.UNLIMITED)
    String getAllowedProjectRoleIds();
    void setAllowedProjectRoleIds(String allowedProjectRoleIds);
}
