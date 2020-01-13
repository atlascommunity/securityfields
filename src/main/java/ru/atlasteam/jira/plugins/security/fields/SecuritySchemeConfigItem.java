package ru.atlasteam.jira.plugins.security.fields;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.util.I18nHelper;
import ru.atlasteam.jira.plugins.security.common.Utils;

import java.util.ArrayList;
import java.util.List;

public class SecuritySchemeConfigItem implements FieldConfigItemType {
    private final JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
    private final ProjectRoleManager projectRoleManager = ComponentAccessor.getOSGiComponentInstanceOfType(ProjectRoleManager.class);
    private final SecuritySchemeService securitySchemeService = ComponentAccessor.getOSGiComponentInstanceOfType(SecuritySchemeService.class);

    @Override
    public String getDisplayName() {
        return "Security Scheme";
    }

    @Override
    public String getDisplayNameKey() {
        return "ru.atlasteam.jira.plugins.security.fields.securityScheme";
    }

    private List<String> getProjectRoleNames(List<String> projectRoleIds) {
        List<String> result = new ArrayList<String>(projectRoleIds.size());
        for (String projectRoleId : projectRoleIds) {
            ProjectRole projectRole = projectRoleManager.getProjectRole(Long.parseLong(projectRoleId));
            if (projectRole != null)
                result.add(projectRole.getName());
        }
        return result;
    }

    @Override
    public String getViewHtml(FieldConfig config, FieldLayoutItem fieldLayoutItem) {
        I18nHelper i18n = jiraAuthenticationContext.getI18nHelper();
        SecurityScheme securityScheme = securitySchemeService.get(config.getId());

        StringBuilder sb = new StringBuilder("<dl>");
        if (securityScheme == null) {
            sb.append("<dt>").append(i18n.getText("ru.atlasteam.jira.plugins.security.fields.inaccessible")).append("</dt>");
        } else {
            List<String> groups = Utils.split(securityScheme.getAllowedGroupNames());
            List<String> projectRoles = getProjectRoleNames(Utils.split(securityScheme.getAllowedProjectRoleIds()));

            if (groups.isEmpty() && projectRoles.isEmpty())
                sb.append("<dt>").append(i18n.getText("ru.atlasteam.jira.plugins.security.fields.inaccessible")).append("</dt>");

            if (!groups.isEmpty()) {
                sb.append("<dt>").append(i18n.getText("common.words.groups")).append(":</dt>");
                for (String group : groups)
                    sb.append("<dd>").append(group).append("</dd>");
            }

            if (!projectRoles.isEmpty()) {
                sb.append("<dt>").append(i18n.getText("common.words.project.roles")).append(":</dt>");
                for (String projectRole : projectRoles)
                    sb.append("<dd>").append(projectRole).append("</dd>");
            }
        }
        sb.append("</dl>");
        return sb.toString();
    }

    @Override
    public String getObjectKey() {
        return "securityScheme";
    }

    @Override
    public Object getConfigurationObject(Issue issue, FieldConfig config) {
        return null;
    }

    @Override
    public String getBaseEditUrl() {
        return "SecurityScheme!default.jspa";
    }
}
