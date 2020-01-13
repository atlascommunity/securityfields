package ru.atlasteam.jira.plugins.security.fields;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;

public interface SecuritySchemeService {
    SecurityScheme get(long fieldConfigId);
    void update(long fieldConfigId, String allowedGroupNames, String allowedProjectRoleIds);
    boolean isAccessibleForLoggedInUser(CustomField field, Issue issue);
}
