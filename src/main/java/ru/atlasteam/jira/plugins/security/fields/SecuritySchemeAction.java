package ru.atlasteam.jira.plugins.security.fields;

import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import ru.atlasteam.jira.plugins.security.common.Utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("ToArrayCallWithZeroLengthArrayArgument")
public class SecuritySchemeAction extends JiraWebActionSupport {
    private final ProjectRoleManager projectRoleManager;
    private final SecuritySchemeService securitySchemeService;

    private long fieldConfigId;
    private String allowedGroupNames;
    private String[] allowedProjectRoleIds = new String[0];

    public SecuritySchemeAction(ProjectRoleManager projectRoleManager, SecuritySchemeService securitySchemeService) {
        this.projectRoleManager = projectRoleManager;
        this.securitySchemeService = securitySchemeService;
    }

    @RequiresXsrfCheck
    @Override
    public String doDefault() throws Exception {
        SecurityScheme securityScheme = securitySchemeService.get(fieldConfigId);
        if (securityScheme != null) {
            allowedGroupNames = securityScheme.getAllowedGroupNames();
            allowedProjectRoleIds = Utils.split(securityScheme.getAllowedProjectRoleIds()).toArray(new String[0]);
        }
        return INPUT;
    }

    @RequiresXsrfCheck
    @Override
    public String doExecute() throws Exception {
        if (fieldConfigId > 0)
            securitySchemeService.update(fieldConfigId, allowedGroupNames, Utils.join(Arrays.asList(allowedProjectRoleIds)));
        return getRedirect(null);
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getFieldConfigId() {
        return String.valueOf(fieldConfigId);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setFieldConfigId(String fieldConfigId) {
        this.fieldConfigId = NumberUtils.toLong(fieldConfigId);
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getAllowedGroupNames() {
        return allowedGroupNames;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setAllowedGroupNames(String allowedGroupNames) {
        this.allowedGroupNames = Utils.join(Utils.split(allowedGroupNames));
    }

    @SuppressWarnings("UnusedDeclaration")
    public Collection<Pair<ProjectRole, Boolean>> getProjectRoles() {
        List<String> selectedIds = Arrays.asList(allowedProjectRoleIds);
        Collection<Pair<ProjectRole, Boolean>> result = new LinkedList<Pair<ProjectRole, Boolean>>();
        for (ProjectRole projectRole : projectRoleManager.getProjectRoles())
            result.add(new ImmutablePair<ProjectRole, Boolean>(projectRole, selectedIds.contains(projectRole.getId().toString())));
        return result;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setAllowedProjectRoleIds(String[] allowedProjectRoleIds) {
        this.allowedProjectRoleIds = allowedProjectRoleIds;
    }
}
