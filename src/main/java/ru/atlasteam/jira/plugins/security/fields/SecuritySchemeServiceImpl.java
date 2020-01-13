package ru.atlasteam.jira.plugins.security.fields;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.sal.api.transaction.TransactionCallback;
import net.java.ao.Query;
import ru.atlasteam.jira.plugins.security.common.Utils;

public class SecuritySchemeServiceImpl implements SecuritySchemeService {
    private final ActiveObjects ao;
    private final GroupManager groupManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final ProjectRoleManager projectRoleManager;

    public SecuritySchemeServiceImpl(ActiveObjects ao, GroupManager groupManager, JiraAuthenticationContext jiraAuthenticationContext, ProjectRoleManager projectRoleManager) {
        this.ao = ao;
        this.groupManager = groupManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.projectRoleManager = projectRoleManager;
    }

    @Override
    public SecurityScheme get(final long fieldConfigId) {
        SecurityScheme[] securitySchemes = ao.executeInTransaction(new TransactionCallback<SecurityScheme[]>() {
            @Override
            public SecurityScheme[] doInTransaction() {
                return ao.find(SecurityScheme.class, Query.select().where("FIELD_CONFIG_ID = ?", fieldConfigId));
            }
        });
        switch (securitySchemes.length) {
            case 0:
                return null;
            case 1:
                return securitySchemes[0];
            default:
                throw new IllegalStateException(String.format("Multiple security schemes (%d) were found for field config id %s", securitySchemes.length, fieldConfigId));
        }
    }

    @Override
    public void update(final long fieldConfigId, final String allowedGroupNames, final String allowedProjectRoleIds) {
        final SecurityScheme storedSecurityScheme = get(fieldConfigId);

        ao.executeInTransaction(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction() {
                SecurityScheme securityScheme = storedSecurityScheme;
                if (securityScheme == null)
                    securityScheme = ao.create(SecurityScheme.class);

                securityScheme.setFieldConfigId(fieldConfigId);
                securityScheme.setAllowedGroupNames(allowedGroupNames);
                securityScheme.setAllowedProjectRoleIds(allowedProjectRoleIds);
                securityScheme.save();
                return null;
            }
        });
    }

    @Override
    public boolean isAccessibleForLoggedInUser(CustomField field, Issue issue) {
        ApplicationUser user = jiraAuthenticationContext.getUser();
        if (field != null && issue != null && user != null) {
            FieldConfig fieldConfig = field.getRelevantConfig(issue);
            if (fieldConfig != null) {
                SecurityScheme securityScheme = get(fieldConfig.getId());
                if (securityScheme != null) {
                    for (String allowedGroupName : Utils.split(securityScheme.getAllowedGroupNames()))
                        if (groupManager.isUserInGroup(user, groupManager.getGroup(allowedGroupName)))
                            return true;
                    for (String allowedProjectRoleId : Utils.split(securityScheme.getAllowedProjectRoleIds())) {
                        ProjectRole projectRole = projectRoleManager.getProjectRole(Long.parseLong(allowedProjectRoleId));
                        if (projectRole != null && projectRoleManager.isUserInProjectRole(user, projectRole, issue.getProjectObject()))
                            return true;
                    }
                }
            }
        }
        return false;
    }
}
